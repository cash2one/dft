package com.ifugle.dft.datapro.dao;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import com.ifugle.dft.check.entity.Enterprise;
import com.ifugle.dft.dao.BaseDao;
import com.ifugle.dft.datapro.entity.*;
import com.ifugle.dft.system.entity.User;
import com.ifugle.dft.utils.ImpExcelHelper;
import com.ifugle.dft.utils.entity.DestField;
import com.ifugle.dft.utils.entity.ExcelTable;
import com.ifugle.dft.utils.entity.ExcelTemplate;
import com.ifugle.dft.utils.entity.SimpleValue;
import org.apache.commons.lang.StringUtils;
@Transactional
public class DataProcessDao extends BaseDao{
	private static Logger log = Logger.getLogger(DataProcessDao.class);
	private ImpExcelHelper excelImp;
	@Autowired
	public void setExcelImp(ImpExcelHelper excelImp){
		this.excelImp = excelImp;
	}
	@Autowired
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate){
		this.jdbcTemplate = jdbcTemplate;
	}
	
	public List getTbs(String sql, int start, int limit,Class cls){
		List infos = null;
		try{
			infos = queryForPage(sql,start,limit,ExcelTable.class);
		}catch(Throwable e){
			log.error(e.toString());
		}  
		return infos;
	}
	
	public boolean deleteTables(String[] tbs) {
		boolean done = false;
		String msql = "delete from excelmap where tbname=?";
		String csql = "delete from exceltb_columns where tbname=?";
		String tsql = "delete from exceltables where tbname=?";
		for(int i=0;i<tbs.length;i++){
			String tb = tbs[i].toUpperCase();
			String tmp_tb = "TMP_"+tb;
			String tmpsql = "drop table "+tmp_tb+" cascade constraints";
			String tbsql = "drop table "+tb+" cascade constraints";
			jdbcTemplate.update(msql,new Object[]{tmp_tb});
			jdbcTemplate.update(csql,new Object[]{tmp_tb});
			jdbcTemplate.update(tsql,new Object[]{tmp_tb});
			jdbcTemplate.update(tmpsql,new Object[]{});
			jdbcTemplate.update(tbsql,new Object[]{});
		}
		//重新加载模板信息
		excelImp.initTemplatesMap();
		done = true;
		return done;
	}
	
	public List getTbCols(String sql, Class class1) {
		List cols = null;
		try{
			cols = queryForList(sql,class1);
		}catch(Throwable e){
			log.error(e.toString());
		}  
		return cols;
	}
	
	public String addExtendTables(ExcelTemplate tbtmp) {
		StringBuffer result = new StringBuffer("{");
		StringBuffer checksql = new StringBuffer("select tbname from exceltables where tbname='");
		checksql.append(tbtmp.getTb().getTbname()).append("'");
		int cc = queryCount(checksql.toString());
		if(cc>0){
			result.append("result:false,info:'名称为").append(tbtmp.getTb().getTbname()).append("的表已存在，不能重复创建！'}");
			return result.toString();
		}
		List cols = tbtmp.getColumns();
		int tbtype = tbtmp.getTb().getTtype();
		ExcelTable tb = tbtmp.getTb();
		if(cols==null||cols.size()==0){
			result.append("result:false,info:'缺少列定义，不能增加表！'}");
			return result.toString();
		}
		int tid = -1 ;
		String tbname = "TMP_"+tb.getTbname().toUpperCase();
		try{
			StringBuffer sql = new StringBuffer("create table ").append(tb.getTbname().toUpperCase()).append("(");
			StringBuffer tmpsql = new StringBuffer("create table ").append(tbname).append("(userid varchar2(50),xh number(5),");
			if(tbtype==1){
				sql.append("NIAN NUMBER(4),YUE NUMBER(2),");
				tmpsql.append("NIAN NUMBER(4),YUE NUMBER(2),");
			}else if(tbtype==2){
				sql.append("NIAN NUMBER(4),JI NUMBER(2),");
				tmpsql.append("NIAN NUMBER(4),JI NUMBER(2),");
			}else if(tbtype==3){
				sql.append("NIAN NUMBER(4),");
				tmpsql.append("NIAN NUMBER(4),");
			}
			for(int i=0;i<cols.size();i++){
				DestField fld =(DestField)cols.get(i);
				sql.append(fld.getColname());
				tmpsql.append(fld.getColname());
				if(fld.getColtype()==0){
					sql.append(" VARCHAR2(100)");
					tmpsql.append(" VARCHAR2(100)");
				}else if(fld.getColtype()==1){
					sql.append(" NUMBER(9) ");
					tmpsql.append(" VARCHAR2(20)");
				}else if(fld.getColtype()==2){
					sql.append(" VARCHAR2(2000)");
					tmpsql.append(" VARCHAR2(2000)");
				}else if(fld.getColtype()==3){
					sql.append(" NUMBER(16,2)");
					tmpsql.append(" VARCHAR2(30)");
				}
				if(i<cols.size()-1){
					sql.append(",");
					tmpsql.append(",");
				}
			}
			sql.append(")");
			tmpsql.append(")");
			jdbcTemplate.execute(sql.toString());
			jdbcTemplate.execute(tmpsql.toString());
			
			tid =jdbcTemplate.queryForInt("select nvl(max(tid),0) mtid from exceltables");
			String tsql = "insert into exceltables(tbname,tbdesc,tid,proname,ttype,remark)values(?,?,?,?,?,?)";
			jdbcTemplate.update(tsql,new Object[]{tbname,tb.getTbdesc(),new Integer(tid+1),tb.getProname(),tb.getTtype(),tb.getRemark()});
			String csql = "insert into exceltb_columns(tbname,colname,coldesc,coltype,showorder,rptkey,isrindex)values(?,?,?,?,?,?,?)";
			int sidx = 1;
			if(tbtype==1){
				jdbcTemplate.update(csql,new Object[]{tbname,"NIAN","年",1,1,1,0});
				jdbcTemplate.update(csql,new Object[]{tbname,"YUE","月",1,2,1,0});
				sidx = 3;
			}else if(tbtype==2){
				jdbcTemplate.update(csql,new Object[]{tbname,"NIAN","年",1,1,1,0});
				jdbcTemplate.update(csql,new Object[]{tbname,"JI","季度",1,2,1,0});
				sidx = 3;
			}else if(tbtype==3){
				jdbcTemplate.update(csql,new Object[]{tbname,"NIAN","年",1,1,1,0});
				sidx = 2;
			}
			jdbcTemplate.update(csql,new Object[]{tbname,"XH","序号",1,0,0,1});
			for(int i=0;i<cols.size();i++){
				DestField fld =(DestField)cols.get(i);
				jdbcTemplate.update(csql,new Object[]{tbname,fld.getColname().toUpperCase(),fld.getColdesc(),fld.getColtype(),new Integer(i+sidx),0,0});
			}
		}catch(Throwable e){
			log.error(e.toString());
			result.append("result:false,info:'增加表时发生错误:").append(e.toString()).append("'}");
			return result.toString();
		} 
		try{
			String msql = "insert into excelmap(tid,tdesc,tbname,excelcolindex,colname)values(?,?,?,?,?)";
			for(int i=0;i<cols.size();i++){
				DestField fld =(DestField)cols.get(i);
				if(fld.getExcelcol()<0){
					continue;
				}
				jdbcTemplate.update(msql,new Object[]{new Integer(tid+1),fld.getColdesc(),tbname,fld.getExcelcol(),fld.getColname().toUpperCase()});
			}
		}catch(Throwable e){
			log.error(e.toString());
			result.append("result:false,info:'增加表与Excel列对应关系是发生错误:").append(e.toString()).append("'}");
			return result.toString();
		} 
		//重新加载模板信息
		excelImp.initTemplatesMap();
		result.append("result:true}");
		return result.toString();
	}

	public boolean saveExtendTables(ExcelTemplate tbtmp) {
		ExcelTable tb = tbtmp.getTb();
		String tbname = "TMP_"+tb.getTbname().toUpperCase();
		String tsql = "update exceltables set tbdesc=?,proname=?,remark=? where tbname=?";
		String dsql = "delete from excelmap where tbname=? ";
		String isql = "insert into excelmap(tid,tdesc,tbname,excelcolindex,colname)values(?,?,?,?,?)";
		jdbcTemplate.update(tsql,new Object[]{tb.getTbdesc(),tb.getProname(),tb.getRemark(),tbname});
		jdbcTemplate.update(dsql,new Object[]{tbname});
		List cols = tbtmp.getColumns();
		if(cols!=null&&cols.size()>0){
			for(int i=0;i<cols.size();i++){
				DestField fld =(DestField)cols.get(i);
				if(fld.getExcelcol()<0){
					continue;
				}
				jdbcTemplate.update(isql,new Object[]{tb.getTid(),tb.getTbdesc(),tbname,fld.getExcelcol(),fld.getColname()});
			}
		}
		//重新加载模板信息
		excelImp.initTemplatesMap();
		return true;
	}

	public boolean checkTbname(String tbname) {
		boolean dup = false;
		int cc = 0;
		String sql = "select count(table_name)cc from user_tables where table_name IN(?,?)";
		cc = jdbcTemplate.queryForInt(sql,new Object[]{"TMP_"+tbname.toUpperCase(),tbname.toUpperCase()});
		dup=cc>0;
		return dup;
	}
	//检查模板是否有列对应关系从而可以形成表头模板
	public boolean CheckTemplateDownload(int tid) {
		boolean hasRd = false; 
		String sql = "select count(*) from EXCELMAP where tid=?";
		int cc = jdbcTemplate.queryForInt(sql,new Object[]{tid});
		hasRd=cc>0;
		return hasRd;
	}
	public int CheckAppCount(int iid) {
		int count = 0;
		String sql = "select count(*)cc from batchapps where iid=?";
		count = jdbcTemplate.queryForInt(sql,new Object[]{iid});
		return count;
	}
	//组织申报信息grid的元数据
	public String getAppTemplate(int iid,String appTb) {
		String infos = "{}";
		JSONObject obj = new JSONObject();
		StringBuffer sql = new StringBuffer("select a.colname,a.coldesc,nvl(a.coltype,0)coltype,nvl(b.excelcolindex,0) excelcol from ");
		sql.append("exceltb_columns a,excelmap b where a.tbname=b.tbname and a.colname=b.colname and a.tbname='");
		sql.append(appTb).append("' and a.rptkey=0 and a.isrindex=0 order by showorder");
		List lst = queryForList(sql.toString(),DestField.class);
		JSONArray cols = buildColumnModel(lst);
		try{
			obj.put("columnModel", cols);
		}catch(Exception e){
			log.error(e.toString());
		}
		JSONArray sts = buildStore(lst);
		try{
			obj.put("store",sts);
		}catch(Exception e){
			log.error(e.toString());
		}
		infos = obj.toString();
		return infos;
	}
	private JSONArray buildColumnModel(List cols){
		JSONArray arrs = new JSONArray();
		if(cols==null||cols.size()==0){
			return arrs;
		}
		try{
			for(int i=0;i<cols.size();i++){
				JSONObject ejo = new JSONObject();
				DestField df=(DestField)cols.get(i);
				ejo.put("id",df.getColname());
				ejo.put("header", df.getColdesc());
				ejo.put("dataIndex",df.getColname());
				ejo.put("align", df.getColtype()==0?"left":"right");
				ejo.put("width", df.getColtype()==0?new Integer(150):new Integer(90));
				arrs.put(ejo);
			}
		}catch(Exception e){
		}
		return arrs;
	}
	private JSONArray buildStore(List cols){
		JSONArray jarr = new JSONArray();
		try{
			JSONObject jo = new JSONObject();
			jo.put("name","RINDEX");
			jo.put("type","int"); 
			jarr.put(jo);
			jo = new JSONObject();
			jo.put("name","NOMATCH");
			jo.put("type","int"); 
			jarr.put(jo);
			jo = new JSONObject();
			jo.put("name","CZFP");
			jo.put("type","string"); 
			jarr.put(jo);
			for(int i=0;i<cols.size();i++){
				jo = new JSONObject();
				DestField df=(DestField)cols.get(i);
				jo.put("name",df.getColname());
				jo.put("type", df.getColtype()==1?"int":df.getColtype()==2?"float":"string");
				jarr.put(jo);
			}
		}catch(Exception e){
		}
		return jarr;
	}
	//删除正式申报数据
	public boolean deleteFormalApps(String tb,int delType,int iid, String delRows) {
		StringBuffer sql = new StringBuffer("delete from ").append(tb);
		if(delType==0){
			sql.append(" where id=?");
			if(!StringUtils.isEmpty(delRows)){
				String[] rows = delRows.split(",");
				for(int i=0;i<rows.length;i++){
					int id = 0;
					try{
						id = Integer.parseInt(rows[i]);
						jdbcTemplate.update(sql.toString(),new Object[]{id});
					}catch(Exception e){}
				}
			}
		}else{
			sql.append(" where iid=?");
			jdbcTemplate.update(sql.toString(),new Object[]{iid});
		}
		return true;
	}
	//删除临时数据
	public boolean deleteImportedApps(int iid,String userid,String tb,String delRows) {
		if(StringUtils.isEmpty(delRows)){
			return true;
		}
		StringBuffer sql = new StringBuffer("delete from ").append(tb).append(" where iid=? and userid=? and xh=?");
		String[] rows = delRows.split(",");
		for(int i=0;i<rows.length;i++){
			int xh = 0;
			try{
				xh = Integer.parseInt(rows[i]);
				jdbcTemplate.update(sql.toString(),new Object[]{iid,userid,xh});
			}catch(Exception e){}
		}
		return true;
	}
	//保存临时数据
	public boolean saveTempApps(String tb,int iid,String strRows) {
		if(StringUtils.isEmpty(strRows)){
			return true;
		}
		JSONArray jarr = null;
		try{
			jarr = new JSONArray(strRows);
		}catch(Exception e){}
		if(jarr==null||jarr.length()==0){
			return true;
		}
		StringBuffer sql = new StringBuffer("update ").append(tb).append(" set swdjzh=? where xh=? and iid=?");
		try{
			for(int i=0;i<jarr.length();i++){
				JSONObject jo = jarr.getJSONObject(i);
				int idx = jo.getInt("xh");
				String swdjzh = jo.getString("swdjzh");
				jdbcTemplate.update(sql.toString(),new Object[]{swdjzh,idx,iid});
			}
		}catch(Exception e){}
		return true;
	}
	//从临时表向正式表迁移
	public String[] saveFormalApps(String userid,String proName,int iid, String strRows) {
		if(proName==null||"".equals(proName)){
			return null;
		}
		StringBuffer sql = new StringBuffer("{call ");
		sql.append(proName).append("(?,?,?,?,?)}");
		String flag = "1";
		final String[] results = new String[2];
		try{
			final String fUser = userid;
			final String fIid = String.valueOf(iid);
			final String fRows = strRows;
			flag = (String)jdbcTemplate.execute(sql.toString(),new CallableStatementCallback() {
				public Object doInCallableStatement(CallableStatement cs)throws SQLException, DataAccessException {
					cs.setString(1,fUser);
					cs.setString(2,String.valueOf(fIid));
					cs.setString(3,String.valueOf(fRows));
	                cs.registerOutParameter(4,Types.VARCHAR);  
	                cs.registerOutParameter(5,Types.VARCHAR);  
	                cs.execute();  
	                results[0] = cs.getString(4);
	                results[1] = cs.getString(5)==null?"":cs.getString(5);
	                if(!"1".equals(results[0])){
	                	log.error(results[1]);
	                }
	                return results[0];  
				} 
			});
		}catch(Throwable e){
			results[0] = "-1";
            results[1] = "从临时表向正式表迁移申报数据时发生数据库错误！"+e.toString();
			log.error(e.toString());
		}
		return results;
	}
	//及时更新
	public boolean matchEn(String tb,int xh,int iid,String userid,String swdjzh) {
		StringBuffer sql = new StringBuffer("update ").append(tb).append(" set swdjzh=? where userid=? and xh=? and iid=?");
		jdbcTemplate.update(sql.toString(),new Object[]{swdjzh,userid,xh,iid});
		return true;
	}
	//获取已有的相同企业历史申报记录数（正式记录）
	public int getSameAppCount(String tb,int iid, String swdjzh) {
		StringBuffer sql = new StringBuffer("select count(*) cc from ").append(tb);
		sql.append(" where swdjzh=?");
		int count = jdbcTemplate.queryForInt(sql.toString(),new Object[]{swdjzh});
		return count;
	}
	//获取导入的申报信息
	public Map getImportedAppData(String userid,String appTmpTb,String appTb,int iid, String matchType, int year,int start,int limit) {
		Map infos = new HashMap();
		StringBuffer sql = new StringBuffer("select t.xh,nvl(enappcount,0)enappcount,");
		if("mc".equals(matchType)){
			sql.append("e.swdjzh,t.qymc,");
		}else{
			sql.append("t.swdjzh,e.mc qymc,");
		}
		sql.append("to_char(decode(e.swdjzh,null,1,0)) nomatch,p.mc czfp,t.item_content itemcont,nvl(gx.qgx,0) contribute,nvl(gx.snqgx,0)");
		sql.append(" contribute_lst,t.approvaldate,investment,nvl(t.money,0)money,nvl(sszj,0)sszj,nvl(qptzj,0)qptzj,t.remark from ");
		sql.append(appTmpTb).append(" t,dj_cz e,(select nvl(qgx,0)qgx,nvl(snqgx,0)snqgx,swdjzh,mc from bb_srqc_czfc where nf='").append(year);
		sql.append("')gx,(select ").append("mc".equals(matchType)?"qymc":"swdjzh").append(",count(*)enappcount from ");
		sql.append("(select d.mc qymc,a.* from dj_cz d,").append(appTb).append(" a where a.swdjzh=d.swdjzh(+)) group by ");
		sql.append("mc".equals(matchType)?"qymc":"swdjzh");
		sql.append(")n,(select distinct bm,mc from bm_cont where table_bm='BM_CZFP')p where e.czfpbm=p.bm(+) and ");
		if("mc".equals(matchType)){
			sql.append("t.qymc=e.mc(+) and t.qymc = n.qymc(+) and t.qymc=gx.mc(+)");
		}else{
			sql.append("t.swdjzh=e.swdjzh(+) and t.swdjzh = n.swdjzh(+) and t.swdjzh=gx.swdjzh(+)");
		}
		sql.append(" and t.userid='").append(userid).append("' and t.iid=").append(iid).append(" order by t.xh");
		int count = queryCount(sql.toString());
		infos.put("totalCount", new Integer(count));
		List apps = queryForPage(sql.toString(),start,limit,Application.class);
		infos.put("rows", apps);
		return infos;
	}
	//获取单位信息
	public Map getEns(User user,int start, int limit, String pField, String pValue) {
		Map infos = new HashMap();
		StringBuffer sql = new StringBuffer("select t1.swdjzh,t1.mc,t1.fddbr,t1.dz from (select d.* from dj_cz d");
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
		sql.append(") t1 where 1=1 ");
		if(pField != null && !"".equals(pField)){
			sql.append(" and t1. ").append(pField).append(" like '%").append(pValue).append("%'");
		}		
		sql.append(" order by t1.swdjzh");
		int count = queryCount(sql.toString());
		infos.put("totalCount", new Integer(count));
		List ens = queryForPage(sql.toString(),start,limit,Enterprise.class);
		infos.put("rows", ens);
		return infos;
	}
	//获取指定项目、支付依据的已资助记录数
	public Map CheckDoneAidInfo(int iid, String pfileno) {
		String sql = "select count(*)cc,nvl(sum(hj),0)hj from aid where iid=? and pfileno=?";
		Map infos = jdbcTemplate.queryForMap(sql, new Object[]{iid,pfileno});
		return infos;
	}
	//获取已经支付的信息
	public Map getFormalAidData(String appTb, int iid, String pfileno,int start, int limit) {
		Map infos =new HashMap();
		StringBuffer sql = new StringBuffer("select max(id)id from ").append(appTb).append(" where iid=? and pfileno=?");
		int aid = jdbcTemplate.queryForInt(sql.toString(),new Object[]{iid,pfileno});
		if(aid<=0){
			infos.put("totalCount", new Integer(0)); 
			infos.put("rows", new ArrayList());
			return infos;
		}
		sql = new StringBuffer("select id aid,z.mc zjtype,b.mc dep,pdate,k.mc kmmc,hj,u.name userid,");
		sql.append("nvl(cc,0)encount,to_char(inputtime,'yyyy-MM-dd HH24:mi:ss')inputtime from aid a,");
		sql.append(" (select aid,count(*)cc from aid_detail group by aid)s,(select * from bm_cont where table_bm='BM_DEP')b,");
		sql.append("(select * from bm_cont where table_bm='BM_ZJTYPE')z,");
		sql.append("(select * from bm_cont where table_bm='BM_ZCKM')k,users u where a.dep=b.bm(+) and a.zjtype=z.bm(+) ");
		sql.append(" and a.kmbm=k.bm(+) and a.id=s.aid(+) and a.userid=u.userid(+) and iid=");
		sql.append(iid).append(" and pfileno='").append(pfileno).append("'");
		int count = queryCount(sql.toString());
		infos.put("totalCount", new Integer(count));
		List ens = queryForPage(sql.toString(),start,limit,Aid.class);
		infos.put("rows", ens);
		return infos;
	}
	//删除正式表的资助记录
	public boolean deleteFormalAids(String tb,int delType,int iid,String pfileno,String delRows) {
		//删除明细记录
		StringBuffer sql = new StringBuffer("delete from aid_detail where aid=?");
		StringBuffer tsql = new StringBuffer("delete from aid where id=?");
		if(delType==0){
			if(!StringUtils.isEmpty(delRows)){
				String[] rows = delRows.split(",");
				for(int i=0;i<rows.length;i++){
					int aid = 0;
					try{
						aid = Integer.parseInt(rows[i]);
						jdbcTemplate.update(sql.toString(),new Object[]{aid});
						jdbcTemplate.update(tsql.toString(),new Object[]{aid});
					}catch(Exception e){}
				}
			}
		}else{
			String asql = "delete from aid_detail where aid in(select id from aid where iid=? and pfileno=?)";
			jdbcTemplate.update(asql,new Object[]{iid,pfileno});
			jdbcTemplate.update("delete from aid where iid=? and pfileno=?",new Object[]{iid,pfileno});
		}
		return true;
	}
	//获取导入的临时资助信息
	public Map getImportedAidData(String userid,String aidTmpTb,String aidTb,String pfileno,int iid,String matchType,int start,int limit) {
		Map infos = new HashMap();
		StringBuffer sql = new StringBuffer("select t.xh,");
		if("mc".equals(matchType)){
			sql.append("e.swdjzh,t.qymc,");
		}else{
			sql.append("t.swdjzh,e.mc qymc,");
		}
		sql.append("to_char(decode(e.swdjzh,null,1,0)) nomatch,nvl(je,0)je from ");
		sql.append(aidTmpTb).append(" t,dj_cz e where ");
		if("mc".equals(matchType)){
			sql.append("t.qymc=e.mc(+) ");
		}else{
			sql.append("t.swdjzh=e.swdjzh(+) ");
		}
		sql.append(" and t.userid='").append(userid).append("' and iid=" ).append(iid).append(" and pfileno='").append(pfileno);
		sql.append("' order by t.xh");
		int count = queryCount(sql.toString());
		infos.put("totalCount", new Integer(count));
		List apps = queryForPage(sql.toString(),start,limit,Aid.class);
		infos.put("rows", apps);
		return infos;
	}
	public boolean deleteImportedAids(String userid, int iid, String pfileno,String aidTmpTb, String delRows) {
		if(StringUtils.isEmpty(delRows)){
			return true;
		}
		StringBuffer sql = new StringBuffer("delete from ").append(aidTmpTb).append(" where iid=? and pfileno=? and userid=? and xh=?");
		String[] rows = delRows.split(",");
		for(int i=0;i<rows.length;i++){
			int xh = 0;
			try{
				xh = Integer.parseInt(rows[i]);
				jdbcTemplate.update(sql.toString(),new Object[]{iid,pfileno,userid,xh});
			}catch(Exception e){}
		}
		return true;
	}
	public boolean matchEnOfTmpAid(String aidTmpTb, String fileno, int iid,int xh, String userid, String swdjzh) {
		StringBuffer sql = new StringBuffer("update ").append(aidTmpTb).append(" set swdjzh=? where userid=? and iid=? and pfileno=? and xh=?");
		jdbcTemplate.update(sql.toString(),new Object[]{swdjzh,userid,iid,fileno,xh});
		return true;
	}
	//保存临时资助信息
	public boolean saveTempAids(String tb,String aidInfo, String strRows) {
		if(StringUtils.isEmpty(strRows)){
			return true;
		}
		JSONObject jobj = null;
		try{
			jobj = new JSONObject(aidInfo);
		}catch(Exception e){
			log.error(e.toString());
		}
		if(jobj==null){
			return false;
		}
		JSONArray jarr = null;
		try{
			jarr = new JSONArray(strRows);
		}catch(Exception e){
			log.error(e.toString());
		}
		if(jarr==null||jarr.length()==0){
			return true;
		}
		String pfileno="",kmbm="",lzqd="" ,pdate="",dep="",zjtype="";
		int iid =0;
		try{
			pfileno = jobj.getString("pfileno");
			kmbm = jobj.getString("kmbm");
			lzqd = jobj.getString("lzqd");
			pdate = jobj.getString("pdate");
			dep = jobj.getString("dep");
			zjtype = jobj.getString("zjtype");
			iid = jobj.getInt("iid");
		}catch(Exception e){
			log.error(e.toString());
		}
		StringBuffer sql = new StringBuffer("update ").append(tb).append(" set swdjzh=?,kmbm=?,lzqd=?,pdate=?,dep=?,zjtype=? ");
		sql.append(" where xh=? and iid=? and pfileno=?");
		try{
			for(int i=0;i<jarr.length();i++){
				JSONObject jo = jarr.getJSONObject(i);
				int idx = jo.getInt("xh");
				String swdjzh = jo.getString("swdjzh");
				jdbcTemplate.update(sql.toString(),new Object[]{swdjzh,kmbm,lzqd,pdate,dep,zjtype,idx,iid,pfileno});
			}
		}catch(Exception e){
			log.error(e.toString());
			return false;
		}
		return true;
	}
	public String[] saveFormalAids(String userid, String proName,String aidInfo, String strRows) {
		final String[] results = new String[2];
		if(proName==null||"".equals(proName)){
			results[0] = "-1";
            results[1] = "迁移资助信息的存储过程未定义！";
			return results;
		}
		JSONObject jobj = null;
		try{
			jobj = new JSONObject(aidInfo);
		}catch(Exception e){
			log.error(e.toString());
		}
		if(jobj==null){
			results[0] = "-1";
            results[1] = "未找到资助的主表信息，如支付依据，项目等信息！";
            return results;
		}
		String pfileno="";
		int iid =0;
		try{
			pfileno = jobj.getString("pfileno");
			iid = jobj.getInt("iid");
		}catch(Exception e){
			log.error(e.toString());
		}
		StringBuffer sql = new StringBuffer("{call ");
		sql.append(proName).append("(?,?,?,?,?,?)}");
		String flag = "1";
		try{
			final String fUser = userid;
			final String fIid = String.valueOf(iid);
			final String fRows = strRows;
			final String fPfileno = pfileno;
			
			flag = (String)jdbcTemplate.execute(sql.toString(),new CallableStatementCallback() {
				public Object doInCallableStatement(CallableStatement cs)throws SQLException, DataAccessException {
					cs.setString(1,fUser);
					cs.setString(2,String.valueOf(fIid));
					cs.setString(3,fPfileno);
					cs.setString(4,String.valueOf(fRows));
	                cs.registerOutParameter(5,Types.VARCHAR);  
	                cs.registerOutParameter(6,Types.VARCHAR);  
	                cs.execute();  
	                results[0] = cs.getString(5);
	                results[1] = cs.getString(6)==null?"":cs.getString(6);
	                if(!"1".equals(results[0])){
	                	log.error(results[1]);
	                }
	                return results[0];  
				} 
			});
		}catch(Throwable e){
			results[0] = "-1";
            results[1] = "从临时表向正式表迁移资助数据时发生数据库错误！"+e.toString();
			log.error(e.toString());
		}
		return results;
	}
	public List getEnHistoryData(String proName,String swdjzh) {
		if(proName==null||"".equals(proName)){
			return null;
		}
		StringBuffer sql = new StringBuffer("{call ");
		sql.append(proName).append("(?,?)}");
		final List infos = new ArrayList();
		try{
			final String fswdjzh = swdjzh;
			jdbcTemplate.execute(sql.toString(),new CallableStatementCallback() {
				public Object doInCallableStatement(CallableStatement cs)throws SQLException, DataAccessException {
					cs.setString(1,fswdjzh);
	                cs.registerOutParameter(2,oracle.jdbc.OracleTypes.CURSOR);  
	                cs.execute();  
	                ResultSet rs = (ResultSet) cs.getObject(2); 
	                if(rs==null){
	                	return infos;
	                }
	                while (rs.next()) {
	                	EnHistory info = new EnHistory();
	                	info.setItemname(rs.getString("itemname"));
	                	info.setItemcont(rs.getString("itemcont"));
	                	info.setApprovaldate(rs.getString("approvaldate"));
	                	info.setTaxyear(rs.getString("taxyear"));
	                	info.setTax(rs.getDouble("tax"));
	                	info.setMoney(rs.getDouble("money"));
	                	info.setQgx(rs.getDouble("qgx"));
	                	infos.add(info);
	                }
	                return infos;
				} 
			});
		}catch(Throwable e){
			log.error(e.toString());
		}
		return infos;
	}
	public Map queryContributeOfEn(String year, String swdjzh) {
		String sql = "select nvl(qgx,0)qgx,nvl(snqgx,0)snqgx,swdjzh,mc from bb_srqc_czfc where nf=? and swdjzh=?";
		Map infos = jdbcTemplate.queryForMap(sql, new Object[]{year,swdjzh});
		return infos;
	}
}
