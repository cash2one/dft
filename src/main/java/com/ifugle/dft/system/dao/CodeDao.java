package com.ifugle.dft.system.dao;

import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.ParameterizedBeanPropertyRowMapper;
import org.springframework.transaction.annotation.Transactional;

import com.ifugle.dft.check.dao.CheckDao;
import com.ifugle.dft.dao.BaseDao;
import com.ifugle.dft.system.entity.AidItem;
import com.ifugle.dft.system.entity.Code;
import org.apache.commons.lang.StringUtils;

@Transactional
public class CodeDao extends BaseDao{
	private static Logger log = Logger.getLogger(CodeDao.class);
	@Autowired
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate){
		this.jdbcTemplate = jdbcTemplate;
	}
	
	public boolean deleteCodeTable(String table_bm,int who){
		boolean done = true;
		Object[] paras = new Object[]{table_bm};
		String sql="delete from "+((who==0||who==1)?"bm_cont_tax":"bm_cont")+" where table_bm=?";
		doUpdate(sql, paras);
			//仅财政端编码表删除时连带删除映射关系
		if(who==9){
			sql="delete from bm where name=?";
			doUpdate(sql, paras);
		}
		sql = "delete from "+((who==0||who==1)?"bm_index_tax":"bm_index")+" where table_bm=?";
		doUpdate(sql, paras);
		done = true;
		return done;
	}
	
	public boolean saveCodeTableMapping(String table,String ftable,int who){
		boolean done = false;
		Object[] dparas = new Object[]{table,new Integer(who)};
		String sql="delete from bm_table_map where t_table=? and who=?";
		doUpdate(sql, dparas);
		dparas = new Object[]{ftable,new Integer(who)};
		sql="delete from bm_table_map where f_table=? and who=?";
		doUpdate(sql, dparas);
		sql = "insert into bm_table_map(f_table,t_table,who)values(?,?,?)";
		Object[] iparas = new Object[]{ftable,table,new Integer(who)};
		doUpdate(sql, iparas);
		done = true;
		return done;
	}
	public Code getCode(String sql){
		Code cd = null;
		try{
			cd = (Code)jdbcTemplate.queryForObject(sql,ParameterizedBeanPropertyRowMapper.newInstance(Code.class));
		}catch(Exception e){
			cd = null;
		}
		return cd;
	}
	
	public boolean saveMappingT2F(int who,String table,String tTable,String fbm,String t_bm)throws Exception {
		boolean done = false;
		StringBuffer sql = new StringBuffer("delete from bm where name=? and to_char(t_bm)=? and who=? ");
		jdbcTemplate.update(sql.toString(),new Object[]{table,t_bm,new Integer(who)});
		//将税务编码状态置为0
		StringBuffer usql = new StringBuffer("update bm_cont_tax set status=0 where table_bm=? and to_char(bm)=?");
		jdbcTemplate.update(usql.toString(),new Object[]{tTable,t_bm});
		if(fbm!=null&&!"".equals(fbm)){
			sql = new StringBuffer("insert into bm(name,t_bm,who,f_bm,t_mc)");
			sql.append(" select ?,bm,who,?,mc from bm_cont_tax where table_bm=? and who=? and bm=?");
			//将税务编码状态置为1
			usql = new StringBuffer("update bm_cont_tax set status=1 where table_bm=? and bm=?");
			jdbcTemplate.update(sql.toString(),new Object[]{table,fbm,tTable,new Integer(who),t_bm});
			jdbcTemplate.update(usql.toString(),new Object[]{tTable,t_bm});
		}
		done=true;
		return done;
	}
	
	public boolean deleteCode(String ftable,String table,String bm,int who,String pid){
		boolean done = false;
		pid = pid==null?"":pid;
		StringBuffer msql = new StringBuffer("delete from bm where ").append("name='").append(ftable).append("'");
		if(who<9){
			msql.append(" and to_char(t_bm)=? and who=").append(who);
		}else{
			msql.append(" and to_char(f_bm)=?");
		}
		String mainTb = "bm_cont";
		if(who<9){
			mainTb="bm_cont_tax";
		}
		StringBuffer sql = new StringBuffer("delete from ").append(mainTb).append(" where table_bm=? and bm in ");
		sql.append("(select bm from (select * from ").append(mainTb).append(" where table_bm=?) connect by prior bm=pid start with to_char(bm)=?)");
		doUpdate(msql.toString(),new Object[]{bm});
		doUpdate(sql.toString(),new Object[]{table,table,bm});
		sql = new StringBuffer("select bm from ").append(mainTb).append(" where table_bm='").append(table).append("' and to_char(pid) ");
		if(pid ==null ||pid.equals("")){
			sql.append("is null");
		}else{
			sql.append("='").append(pid).append("'");
		}
		sql.append(" and to_char(bm)<>'").append(bm).append("'");
		int leafCount = queryCount(sql.toString());
		if(leafCount == 0){//删除后，父节点无其他子节点，则把该父节点的isleaf设置为1
			sql = new StringBuffer("update ").append(mainTb).append(" set isleaf=1 where table_bm=? and bm=?");
			doUpdate(sql.toString(),new Object[]{table,pid});
		}
		done = true;
		return done;
	}
    
	public boolean saveCode(Map params,Map fileFields){
		boolean done = false;
		String table_bm = (String)params.get("table_bm");
		String bm = (String)params.get("bm");
		String mc = (String)params.get("mc");
		String pid = (String)params.get("pid") ==null?"":(String)params.get("pid");
		String sqy = (String)params.get("qybj");
		int qybj = 0;
		try{
			qybj = Integer.parseInt(sqy);
		}catch(Exception e){}
		String sisleaf = (String)params.get("isleaf");
		int isleaf = 0;
		try{
			isleaf = Integer.parseInt(sisleaf);
		}catch(Exception e){}
		String slevel =(String)params.get("codeLevel");
		int codeLevel = 3;
		try{
			codeLevel = Integer.parseInt(slevel);
		}catch(Exception e){}
		String cMode = (String)params.get("cMode");
		String swho = (String)params.get("who");
		int who = 9;
		try{
			who = Integer.parseInt(swho);
		}catch(Exception e){}
		String mainTb ="bm_cont";
		if(who<9){
			mainTb="bm_cont_tax";
		}
		StringBuffer sql = new StringBuffer("update ").append(mainTb).append(" set mc=?,pid=?,qybj=?,isleaf=?,codelevel=? where table_bm=? and bm=?");
		
		Object[] paras = new Object[]{mc,pid,new Integer(qybj),new Integer(isleaf),new Integer(codeLevel),table_bm,bm};
		if(cMode!=null&&"add".equals(cMode)){
			if(who<9){
				sql = new StringBuffer("insert into bm_cont_tax(mc,pid,qybj,isleaf,codelevel,table_bm,bm,who,status)values(?,?,?,?,?,?,?,");
				sql.append(who).append(",0)");
			}else{
				sql = new StringBuffer("insert into bm_cont(mc,pid,qybj,isleaf,codelevel,table_bm,bm)values(?,?,?,?,?,?,?)");
			}
		}
		doUpdate(sql.toString(), paras);
		sql = new StringBuffer("update ").append(mainTb).append(" set isleaf=0 where table_bm=? and bm=?");
		doUpdate(sql.toString(), new Object[]{table_bm,pid});
		done = true;
		return done;
	}
	
	public boolean saveMappingF2T(String table,String dsTable,String gsTable,String fbm,JSONArray jdss,JSONArray jgss){
		boolean done = false;
		fbm = fbm==null?"":fbm;
		//以下4步骤的操作可以确保映射关系解除、添加等操作的完整性，包括税务编码的映射状态。
		//1、将当前财政编码映射的所有税务编码状态改为未映射。由于税务只能映射最多一个财政编码，从财政端解除映射关系，就表示税务编码无任何映射。
		StringBuffer usql = new StringBuffer("update bm_cont_tax a set a.status =0 where exists(select 1 from bm b where name=? and to_char(f_bm)=? and to_char(a.bm)=to_char(b.t_bm))");
		doUpdate(usql.toString(),new Object[]{table,fbm});
		//2、删除当前财政编码的所有“映射关系”。
		StringBuffer sql = new StringBuffer("delete from bm where name=? and to_char(f_bm)=? and who=?");
		doUpdate(sql.toString(),new Object[]{table,fbm,new Integer(0)});
		doUpdate(sql.toString(),new Object[]{table,fbm,new Integer(1)});
		//3、插入本次提交的映射关系。
		sql = new StringBuffer("insert into bm(name,t_bm,who,f_bm,t_mc)");
		sql.append(" select ?,bm,who,?,mc from bm_cont_tax where table_bm=? and who=? and bm=?");
		//4、将本次映射上的税务编码的映射状态改为1。
		usql = new StringBuffer("update bm_cont_tax set status =1 where table_bm=? and bm=?");
		if(jdss!=null&&jdss.length()>0){
			for(int i=0;i<jdss.length();i++){
				String tbm = "";
				try{
					tbm = jdss.getString(i);
				}catch(Exception e){}
				doUpdate(sql.toString(),new Object[]{table,fbm,dsTable,new Integer(0),tbm});
				doUpdate(usql.toString(), new Object[]{dsTable,tbm});
			}
		}
		if(jgss!=null&&jgss.length()>0){
			for(int i=0;i<jgss.length();i++){
				String tbm = "";
				try{
					tbm = jgss.getString(i);
				}catch(Exception e){}
				doUpdate(sql.toString(),new Object[]{table,fbm,gsTable,new Integer(1),tbm});
				doUpdate(usql.toString(), new Object[]{gsTable,tbm});
			}
		}
		done = true;
		return done;
	}
	//移动节点的层级关系
	public boolean moveCode(int who, String table_bm,String nodeId, String oldPid, String newPid) {
		boolean done = false;
		//更新指定节点的pid
		StringBuffer sql = new StringBuffer("update bm_cont_tax set pid=? where who=");
		sql.append(who).append(" and table_bm=? and bm=?");
		if(who==9){
			sql = new StringBuffer("update bm_cont set pid = ? where table_bm=? and bm=?");
		}
		Object [] params = new Object[]{newPid,table_bm,nodeId};
		if(newPid==null||newPid.startsWith("tree-root")){
			params = new Object[]{"",table_bm,nodeId};
		}
		doUpdate(sql.toString(), params);
		//将新的父节点的isleaf属性设置为0
		sql = new StringBuffer("update bm_cont_tax set isleaf = 0 where who=");
		sql.append(who).append(" and table_bm=? and bm=?");
		if(who==9){
			sql = new StringBuffer("update bm_cont set isleaf = 0 where table_bm=? and bm=?");
		}
		doUpdate(sql.toString(), new Object[]{table_bm,newPid});
		//旧的父节点，如果其下没有子节点了，将isleaf设置为1
		sql = new StringBuffer("select bm from ");
		if(who!=9){
			sql.append("bm_cont_tax where who=").append(who).append(" and ");
		}else{
			sql.append("bm_cont where ");
		}
		sql.append(" table_bm='").append(table_bm).append("' and pid ='" ).append(oldPid).append("'");
		int count = queryCount(sql.toString());
		if(count==0){
			sql = new StringBuffer("update bm_cont_tax set isleaf = 1 where who=");
			sql.append(who).append(" and table_bm=? and bm=?");
			if(who==9){
				sql = new StringBuffer("update bm_cont set isleaf = 1 where table_bm=? and bm=?");
			}
			doUpdate(sql.toString(), new Object[]{table_bm,oldPid});
		}
		done= true;
		return done;
	}

	public boolean delAidItems(String iid, String pid) {
		pid = pid==null?"":pid;
		StringBuffer sql = new StringBuffer("delete from items where id in ");
		sql.append("(select id from items connect by prior id=pid start with id=").append(iid).append(")");
		jdbcTemplate.update(sql.toString());
		sql = new StringBuffer("select id from items where pid ");
		if(pid ==null ||pid.equals("")||"0".equals(pid)){
			sql.append("is null");
		}else{
			sql.append("=").append(pid);
		}
		sql.append(" and id<>").append(iid);
		int leafCount = queryCount(sql.toString());
		if(leafCount == 0){//删除后，父节点无其他子节点，则把该父节点的isleaf设置为1
			sql = new StringBuffer("update items set isleaf=1 where id=").append(pid);
			jdbcTemplate.update(sql.toString());
		}
		return true;
	}

	public AidItem getAidItem(String nodeid) {
		AidItem ai = null;
		StringBuffer sql = new StringBuffer("select a.id,a.pid,a.isleaf,a.mc pname,gid,b.mc grade,fileno,remark from items a,");
		sql.append(" (select * from bm_cont where table_bm='BM_GRADE')b where a.id=? and a.gid=b.bm(+)");
		int id = 0;
		try{
			id = Integer.parseInt(nodeid);
		}catch(Exception e){}
		try{
			ai = (AidItem)jdbcTemplate.queryForObject(sql.toString(),new Object[]{id},ParameterizedBeanPropertyRowMapper.newInstance(AidItem.class));
		}catch(Exception e){
			ai = null;
		}
		return ai;
	}

	public int saveAidItem(Map params, Map fileFields) {
		String sid = (String)params.get("id");
		String spid = (String)params.get("pid");
		String sIsleaf = (String)params.get("isleaf");
		String pname = (String)params.get("pname");
		String grade = (String)params.get("gid");
		String fileno = (String)params.get("fileno");
		String remark = (String)params.get("remark");
		int id = 0;
		try{
			id = Integer.parseInt(sid);
		}catch(Exception e){}
		int isleaf = 0;
		try{
			isleaf = Integer.parseInt(sIsleaf);
		}catch(Exception e){}
		
		if(id<=0){
			id =jdbcTemplate.queryForInt("select SQ_CZFCITEMS_ID.nextval from dual");
		}
		Object[] paras = new Object[]{pname,spid,isleaf,grade,fileno,remark,id};
		String cMode = (String)params.get("saveMode");
		StringBuffer sql = new StringBuffer("update items set mc=?,pid=?,isleaf=?,gid=?,fileno=?,remark=? where id=?");
		if(cMode!=null&&"add".equals(cMode)){
			sql = new StringBuffer("insert into items(mc,pid,isleaf,gid,fileno,remark,id)values(?,?,?,?,?,?,?)");
		}
		jdbcTemplate.update(sql.toString(), paras);
		if(!StringUtils.isEmpty(spid)){
			sql = new StringBuffer("update items set isleaf=0 where id=?");
			jdbcTemplate.update(sql.toString(), new Object[]{spid});
		}
		return id;
	}
}