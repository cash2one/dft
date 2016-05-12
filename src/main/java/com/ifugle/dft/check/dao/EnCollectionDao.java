package com.ifugle.dft.check.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.ParameterizedBeanPropertyRowMapper;
import org.springframework.transaction.annotation.Transactional;
import com.ifugle.dft.check.entity.EnCollection;
import com.ifugle.dft.check.entity.EnterpriseOfJh;
import com.ifugle.dft.dao.BaseDao;
import com.ifugle.dft.system.entity.User;
import com.ifugle.dft.utils.entity.SimpleValue;
import com.ifugle.dft.utils.entity.TreeNode;
@Transactional
public class EnCollectionDao extends BaseDao{
	private static Logger log = Logger.getLogger(EnCollectionDao.class);
	@Autowired
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate){
		this.jdbcTemplate = jdbcTemplate;
	}
	public List getEnCollection(String nodeid,User user) {
		StringBuffer sql = new StringBuffer("select id,pid,name text,isleaf leaf,decode(isleaf,1,'file','folder') cls,0 expanded,creator");
		sql.append(" from (select distinct id,pid,name,isleaf,createtime,creator from (select * from qyjh connect by prior pid=id ");
		if(user.getIsManager()!=1){
			sql.append(" start with creator='").append(user.getUserid()).append("' or isprivate=0 ");
		}
		sql.append(" )) where pid ");
		if(nodeid==null||"en_root_value".equals(nodeid)){
			//sql.append(" is null");
			sql.append(" is null");
		}else{
			sql.append("=").append(nodeid);
		}
		List nodes = queryForList(sql.toString(),TreeNode.class);
		return nodes;
	}
	
	public EnCollection getEnCollectionById(String id) {
		EnCollection ec = null;
		StringBuffer sql = new StringBuffer("select q.id,q.pid,q.name,q.isleaf,nvl(u.name,q.creator) creator,q.isprivate,to_char(q.createtime)createtime,q.remark ");
		sql.append(" from qyjh q,users u where id=").append(id).append(" and creator=u.userid(+)");
		ec = (EnCollection)jdbcTemplate.queryForObject(sql.toString(),ParameterizedBeanPropertyRowMapper.newInstance(EnCollection.class));
		return ec;
	}
	public boolean deleteEnCollection(String id) {
		boolean done = false;
		int iId = 0;
		try{
			iId = Integer.parseInt(id);
		}catch(Exception e){}
		StringBuffer sql = new StringBuffer("select id from qyjh where pid =?");
		List eids=jdbcTemplate.queryForList(sql.toString(),new Object[]{new Integer(iId)}, String.class);
		StringBuffer ddsql = new StringBuffer("delete from qyjh_detail where jhid=?");
		StringBuffer dsql = new StringBuffer("delete from qyjh where id=?");
		if(eids!=null&&eids.size()>0){
			for(int i=0;i<eids.size();i++){
				String eid = (String)eids.get(i);
				jdbcTemplate.update(ddsql.toString(),new Object[]{eid});
				jdbcTemplate.update(dsql.toString(),new Object[]{eid});
			}
		}
		jdbcTemplate.update(ddsql.toString(),new Object[]{new Integer(iId)});
		jdbcTemplate.update(dsql.toString(),new Object[]{new Integer(iId)});
		done = true;
		return done;
	}
	public int checkCode(String tmpBm, String bm) {
		int exist = 0;
		StringBuffer sql = new StringBuffer("select count(id) count from qyjh where id=? ");
		if(bm!=null&&!"".equals(bm)){
			sql.append(" and id<>'").append(bm).append("'");
		}
		int count = jdbcTemplate.queryForInt(sql.toString(), new Object[]{tmpBm});
		exist = count>0?1:0;
		return exist;
	}
	@SuppressWarnings("unchecked")
	public int[] saveEnCollection(Map params, Map fileFields,String userid) {
		int[] result = new int[]{-1,-1};
		String id = (String)params.get("id") ==null?"":(String)params.get("id");
		String pid = (String)params.get("pid") ==null?"":(String)params.get("pid");
		String name = (String)params.get("name") ==null?"":(String)params.get("name");
		String remark = (String)params.get("remark") ==null?"":(String)params.get("remark");
		String sPrivate = (String)params.get("isprivate") ==null?"0":(String)params.get("isprivate");
		String sIsleaf = (String)params.get("isleaf") ==null?"0":(String)params.get("isleaf");
		int isprivate ="on".equals(sPrivate)?1:0;
		int isleaf = 0;
		try{
			isleaf = Integer.parseInt(sIsleaf);
		}catch(Exception e){}
		Integer iPid = null;
		try{
			iPid = new Integer(pid);
		}catch(Exception e){
		}
		Integer iId = null;
		try{
			iId = new Integer(id);
		}catch(Exception e){
		}
		Object[] paras = new Object[]{iPid,name,new Integer(isleaf),new Integer(isprivate),remark,iId};
		StringBuffer sql = new StringBuffer("update qyjh set pid=?,name=?,isleaf=?,isprivate=?,remark=? where id=?");
		String saveMode = (String)params.get("saveMode") ==null?"add":(String)params.get("saveMode");
		int newId = iId==null?-1:iId.intValue();
		if(saveMode!=null&&"add".equals(saveMode)){
			String sqsql = "select sq_qyjh.nextval from dual";
			newId =jdbcTemplate.queryForInt(sqsql);
			sql = new StringBuffer("insert into qyjh(pid,name,isleaf,isprivate,remark,id,createtime,creator)values(?,?,?,?,?,?,sysdate,'");
			sql.append(userid).append("')");
			paras = new Object[]{iPid,name,new Integer(isleaf),new Integer(isprivate),remark,new Integer(newId)};
		}
		jdbcTemplate.update(sql.toString(), paras);
		result =new int[]{1,newId};
		return result;
	}
	@SuppressWarnings("unchecked")
	public Map getCollectionEns(int start, int limit,String pField,String pValue, String enId,User user) {
		Map infos = new HashMap();
		StringBuffer sql = new StringBuffer("select distinct swdjzh,mc,fddbr,dz,a.qybj,nvl(a.showorder,0)showorder from ");
		sql.append("qyjh_detail a,(select max(xh)xh,swdjzh,max(mc) mc,max(fddbr) fddbr,max(dz) dz from dj_cz ");
		//检查乡镇权限
		if(user.getIsManager()!=1){//如果不是管理员，则检查用户能操作哪些乡镇数据
			List xzs=user.getXzs();
			if(xzs==null||xzs.size()==0){                        //如果不是管理员，且没有对应乡镇，则没有任何乡镇数据可看
				sql.append(" where 1=2");
			}else{
				sql.append(" where czfpbm in(");
				for(int i=0;i<xzs.size();i++){
					sql.append("'");
					sql.append(((SimpleValue)xzs.get(i)).getBm());
					if(i!=xzs.size()-1)
						sql.append("',");
					else
						sql.append("'");
				}
				sql.append(")");
			}
		}
		sql.append(" group by swdjzh)b where a.eid=b.swdjzh and a.jhid='").append(enId).append("' ");
		if(pField != null && !"".equals(pField)){
			sql.append(" and b. ").append(pField).append(" like '%").append(pValue).append("%'");
		}	
		sql.append(" order by showorder");
		int count = queryCount(sql.toString());
		infos.put("totalCount", new Integer(count));
		List ens = queryForPage(sql.toString(),start,limit,EnterpriseOfJh.class);
		infos.put("rows", ens);
		return infos;
	}
	public boolean removeEn(String ecid, String[] eids) {
		boolean done= false;
		Integer iJhid = null;
		try{
			iJhid = new Integer(ecid);
		}catch(Exception e){}
		StringBuffer sql = new StringBuffer("delete from qyjh_detail where jhid = ? and eid=?");
		for(int i=0;i<eids.length;i++){
			String eid = eids[i];
			Object[] paras = new Object[]{iJhid,eid};
			jdbcTemplate.update(sql.toString(), paras);
		}
		done = true;
		return done;
	}
	public boolean addEn(String cid, String[] eids) {
		boolean done= false;
		Integer iJhid = null;
		try{
			iJhid = new Integer(cid);
		}catch(Exception e){}
		StringBuffer sql = new StringBuffer("delete from qyjh_detail where jhid = ? and eid=?");
		StringBuffer isql = new StringBuffer("insert into qyjh_detail(jhid,eid,qybj,showorder)select ?,?,1,nvl(max(showorder),0)+1 ");
		isql.append(" from qyjh_detail where jhid=?");
		for(int i=0;i<eids.length;i++){
			String eid = eids[i];
			Object[] paras = new Object[]{iJhid,eid};
			jdbcTemplate.update(sql.toString(), paras);
			Object[] iparas = new Object[]{iJhid,eid,iJhid};
			jdbcTemplate.update(isql.toString(), iparas);
		}
		done = true;
		return done;
	}
	public boolean toggleQybj(String cid, String swdjzh, int cQybj) {
		boolean done= false;
		Integer iJhid = null;
		try{
			iJhid = new Integer(cid);
		}catch(Exception e){}
		StringBuffer sql = new StringBuffer("update qyjh_detail set qybj=? where jhid = ? and eid=?");
		int nqybj = cQybj==0?1:0;
		Object[] paras = new Object[]{new Integer(nqybj),iJhid,swdjzh};
		jdbcTemplate.update(sql.toString(), paras);
		done = true;
		return done;
	}
	public boolean saveEnOrder(String cid, JSONArray jarr) {
		boolean done= false;
		Integer iJhid = null;
		try{
			iJhid = new Integer(cid);
		}catch(Exception e){}
		StringBuffer sql = new StringBuffer("update qyjh_detail set showorder=? where jhid = ? and eid=?");
		for(int i=0;i<jarr.length();i++){
			Object[] paras = null;
			try{
				JSONObject job = jarr.getJSONObject(i);
				int showorder = job.getInt("showorder");
				String eid = job.getString("swdjzh");
				paras = new Object[]{new Integer(showorder),iJhid,eid};
			}catch(Exception e){}
			jdbcTemplate.update(sql.toString(), paras);
		}
		done = true;
		return done;
	}
	@SuppressWarnings("unchecked")
	public Map getEnterprisesToAdd(int start, int limit, String pField,String pValue, String cID,User user) {
		Map infos = new HashMap();
		StringBuffer sql = new StringBuffer("select t1.swdjzh,t1.mc,t1.fddbr,t1.dz,jhid from (select d.* from dj_cz d");//
		//检查乡镇权限
		if(user.getIsManager()!=1){//如果不是管理员，则检查用户能操作哪些乡镇数据
			List xzs=user.getXzs();
			sql.append(",(select bm,pid from bm_cont where table_bm='BM_CZFP') bm_czfp_qx where bm_czfp_qx.bm=d.czfpbm ");
			if(xzs==null||xzs.size()==0){                        //如果不是管理员，且没有对应乡镇，则没有任何乡镇数据可看
				sql.append(" and 1=2");
			}else{
				sql.append(" and bm_czfp_qx.bm in(");
				for(int i=0;i<xzs.size();i++){
					sql.append("'");
					sql.append(((SimpleValue)xzs.get(i)).getBm());
					if(i!=xzs.size()-1)
						sql.append("',");
					else
						sql.append("'");
				}
				sql.append(")");
			}
		}
		sql.append(") t1 ,(select * from qyjh_detail where jhid=").append(cID).append(") t2 where t1.swdjzh = t2.eid(+)");
		if(pField != null && !"".equals(pField)){
			sql.append(" and t1. ").append(pField).append(" like '%").append(pValue).append("%'");
		}		
		sql.append(" order by t1.swdjzh");
		int count = queryCount(sql.toString());
		infos.put("totalCount", new Integer(count));
		List ens = queryForPage(sql.toString(),start,limit,EnterpriseOfJh.class);
		infos.put("rows", ens);
		return infos;
	}
	@SuppressWarnings("unchecked")
	public Map getImportedEns(int start, int limit, User user) {
		Map infos = new HashMap();
		StringBuffer  sql = new StringBuffer("select match_type from match_excel_type where s_id=? and entype='encollection'");
		List modes = jdbcTemplate.queryForList(sql.toString(),new Object[]{user.getUserid()}, String.class);
		String matchMode = modes==null||modes.size()==0?"mc":(String)modes.get(0);
		//根据匹配模式构造查询语句
		sql = new StringBuffer("select a.*,c.jhid from(select ");
		if("mc".equals(matchMode)){
			sql.append("e.swdjzh,t.mc,");
		}else{
			sql.append("t.swdjzh,e.mc,");
		}
		sql.append("e.dz,e.fddbr,to_char(decode(e.swdjzh,null,0,1)) ismatch,t.xh showorder from match_excel_ens t,");
		sql.append("dj_cz e where ");
		if("mc".equals(matchMode)){
			sql.append("t.mc=e.mc(+)");
		}else if("sh".equals(matchMode)){
			sql.append("t.swdjzh=e.swdjzh(+)");
		}
		sql.append(" and t.s_id='").append(user.getUserid()).append("' )a,");
		sql.append("(select distinct eid,jhid from qyjh_detail)c where a.swdjzh=c.eid(+) order by showorder");
		int count = queryCount(sql.toString());
		infos.put("totalCount", new Integer(count));
		List ens = queryForPage(sql.toString(),start,limit,EnterpriseOfJh.class);
		infos.put("rows", ens);
		return infos;
	}
	public boolean delImportedEns(String[] xhs, String userid) {
		boolean done= false;
		StringBuffer sql = new StringBuffer("delete from match_excel_ens where s_id=? and xh=?");
		for(int i=0;i<xhs.length;i++){
			String xh = xhs[i];
			int ixh = 0;
			try{
				ixh = Integer.parseInt(xh);
			}catch(Exception e){}
			Object[] paras = new Object[]{userid,new Integer(ixh)};
			jdbcTemplate.update(sql.toString(), paras);
		}
		done = true;
		return done;
	}
	public boolean addExcelMatchEns(String cid, String[] swdjzhs, String[] xhs,String userid) {
		boolean done= false;
		StringBuffer dsql = new StringBuffer("delete from qyjh_detail where jhid=? and eid =? ");
		StringBuffer isql= new StringBuffer("insert into qyjh_detail(jhid,eid,qybj,showorder)");
		isql.append("select ?,?,1,nvl(max(showorder),0)+1 from qyjh_detail where jhid=?");
		StringBuffer dtmpsql = new StringBuffer("delete from match_excel_ens where s_id=? and xh =?");
		for(int i=0;i<xhs.length;i++){
			String eid = swdjzhs[i];
			String xh = xhs[i];
			int ixh = 0;
			try{
				ixh = Integer.parseInt(xh);
			}catch(Exception e){}
			Object[] paras = new Object[]{cid,eid};
			jdbcTemplate.update(dsql.toString(), paras);
			jdbcTemplate.update(isql.toString(), new Object[]{cid,eid,cid});
			paras = new Object[]{userid,new Integer(ixh)};
			jdbcTemplate.update(dtmpsql.toString(), paras);
		}
		done = true;
		return done;
	}
	public int importEnExcel(List matchFlds, String userid, String dType) {
		int result = 0;
		if((matchFlds==null||matchFlds.size()==0)){
			return 0;
		}
		if("".equals(userid)){
			return 0;
		}
		//删除原先的匹配模式记录
		StringBuffer bSql=new StringBuffer("delete from match_excel_type where s_id=? and entype='encollection'");
		Object[] paras = new Object[]{userid};
		jdbcTemplate.update(bSql.toString(), paras);
		//记录新的匹配模式记录
		bSql = new StringBuffer("insert into match_excel_type(s_id,entype,match_type)values(?,'encollection',?)");
		paras = new Object[]{userid,dType};
		jdbcTemplate.update(bSql.toString(), paras);
		//删除原先的临时导入记录,与用户相关
		bSql=new StringBuffer("delete from match_excel_ens where s_id=?");
		paras = new Object[]{userid};
		jdbcTemplate.update(bSql.toString(), paras);
		//匹配模式不同，写入的字段不同
		if("sh".equals(dType)){
			bSql=new StringBuffer("insert into match_excel_ens (s_id,swdjzh,xh)values(?,?,?)");
		}else{
			bSql=new StringBuffer("insert into match_excel_ens (s_id,mc,xh)values(?,?,?)");
		}
		int i=0;
		for(i=0;i<matchFlds.size();i++){
			String v = (String)matchFlds.get(i);
			paras = new Object[]{userid,v,new Integer(i)};
			jdbcTemplate.update(bSql.toString(), paras);
		}
		result = i;
		return result;
	}
	//指定的节点是否有下级节点
	public boolean hasChildren(String id) {
		boolean hasChildren = false;
		StringBuffer sql = new StringBuffer("select * from qyjh where pid = '").append(id).append("'");
		int cc = queryCount(sql.toString());
		hasChildren = cc>0;
		return hasChildren;
	}
	public boolean isPrivateNode(String id) {
		boolean isPrivateNode = false;
		StringBuffer sql = new StringBuffer("select isprivate from qyjh where id = '").append(id).append("'");
		int pp = jdbcTemplate.queryForInt(sql.toString());
		isPrivateNode = pp>0;
		return isPrivateNode;
	}
}
