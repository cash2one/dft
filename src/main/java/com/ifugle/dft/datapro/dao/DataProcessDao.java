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
//		try{
//			String msql = "insert into excelmap(tid,tdesc,tbname,excelcolindex,colname)values(?,?,?,?,?)";
//			for(int i=0;i<cols.size();i++){
//				DestField fld =(DestField)cols.get(i);
//				if(fld.getExcelcol()<0){
//					continue;
//				}
//				jdbcTemplate.update(msql,new Object[]{new Integer(tid+1),fld.getColdesc(),tbname,fld.getExcelcol(),fld.getColname().toUpperCase()});
//			}
//		}catch(Throwable e){
//			log.error(e.toString());
//			result.append("result:false,info:'增加表与Excel列对应关系是发生错误:").append(e.toString()).append("'}");
//			return result.toString();
//		} 
		//重新加载模板信息
		excelImp.initTemplatesMap();
		result.append("result:true}");
		return result.toString();
	}

	public boolean saveExtendTables(String tname,String tbinfo,String edtInfo) {
		//修改表信息
		if(!StringUtils.isEmpty(tbinfo)){
			JSONObject jtb = null;
			try{
				jtb = new JSONObject(tbinfo);
			}catch(Exception e){
				log.error(e.toString());
			}
			if(jtb!=null){
				String tbdesc = "";
				try{
					tbdesc = jtb.getString("tbdesc");
				}catch(Exception e){
					log.error(e.toString());
				}
				String proname =""; 
				try{
					proname = jtb.getString("proname");
				}catch(Exception e){
					log.error(e.toString());
				}
				String remark = "";
				try{
					remark = jtb.getString("remark");
				}catch(Exception e){
					log.error(e.toString());
				}
				String tbname = "";
				try{
					tbname = jtb.getString("tbname");
				}catch(Exception e){
					log.error(e.toString());
				}
				String tsql = "update exceltables set tbdesc=?,proname=?,remark=? where tbname=?";
				jdbcTemplate.update(tsql,new Object[]{tbdesc,proname,remark,tbname});
			}
		}
		//修改列信息
		if(!StringUtils.isEmpty(edtInfo)){
			JSONObject jedt = null;
			try{
				jedt = new JSONObject(edtInfo);
			}catch(Exception e){
				log.error(e.toString());
			}
			if(jedt!=null){
				JSONArray edtCol = null;
				JSONArray edtDesc = null;
				JSONArray edtType = null;
				JSONArray newCols = null;
				try{
					edtCol = jedt.getJSONArray("edtCol");
				}catch(Exception e){
					log.error(e.toString());
				}
				try{
					edtDesc = jedt.getJSONArray("edtDesc");
				}catch(Exception e){
					log.error(e.toString());
				}
				try{
					edtType = jedt.getJSONArray("edtType");
				}catch(Exception e){
					log.error(e.toString());
				}
				try{
					newCols = jedt.getJSONArray("newCols");
				}catch(Exception e){
					log.error(e.toString());
				}
				//注意先后顺序。由于列名要作为条件，列名的修改最后做。提交上来的信息要确保原始列名是窗体加载时的初始列名。
				if(newCols!=null){
					addColumn(tname,newCols);
				}
				if(edtDesc!=null){
					modifyColDesc(tname,edtDesc);
				}
				if(edtType !=null){
					modifyColType(tname,edtType);
				}
				if(edtCol!=null){
					modifyColName(tname,edtCol);
				}
			}
		}
		//重新加载模板信息
		excelImp.initTemplatesMap();
		return true;
	}
	//增加列
	private void addColumn(String tbname,JSONArray newCols) {
		if(newCols==null||newCols.length()==0){
			return;
		}
		for(int i=0;i<newCols.length();i++){
			JSONObject col = null;
			try{
				col = newCols.getJSONObject(i);
				if(col==null){
					continue;
				}
				StringBuffer sql = new StringBuffer("alter table ");
				StringBuffer tmpsql = new StringBuffer("alter table TMP_");
				sql.append(tbname).append(" add ").append(col.getString("colname"));
				tmpsql.append(tbname).append(" add ").append(col.getString("colname"));
				if(col.getInt("coltype")==0){
					sql.append(" VARCHAR2(100)");
					tmpsql.append(" VARCHAR2(100)");
				}else if(col.getInt("coltype")==1){
					sql.append(" NUMBER(9) ");
					tmpsql.append(" VARCHAR2(20)");
				}else if(col.getInt("coltype")==2){
					sql.append(" VARCHAR2(2000)");
					tmpsql.append(" VARCHAR2(2000)");
				}else if(col.getInt("coltype")==3){
					sql.append(" NUMBER(16,2)");
					tmpsql.append(" VARCHAR2(30)");
				}
				//增加列
				jdbcTemplate.execute(sql.toString());
				jdbcTemplate.execute(tmpsql.toString());
				//增加配置信息
				String csql = "insert into exceltb_columns(tbname,colname,coldesc,coltype,showorder,rptkey,isrindex)values(?,?,?,?,?,?,?)";
				Object[] params = new Object[]{tbname,col.getString("colname"),col.getString("coldesc"),
						col.getInt("coltype"),col.getInt("showorder"),0,0};
				jdbcTemplate.update(csql,params);
			}catch(Exception e){
				log.error(e.toString());
			}
		}
	}
	//修改列中文描述
	private void modifyColDesc(String tbname,JSONArray edtDesc) {
		if(edtDesc==null||edtDesc.length()==0){
			return;
		}
		for(int i=0;i<edtDesc.length();i++){
			JSONObject col = null;
			try{
				col = edtDesc.getJSONObject(i);
				if(col==null){
					continue;
				}
				//只需修改配置信息
				String csql = "update exceltb_columns set coldesc=? where tbname=? and colname=?";
				Object[] params = new Object[]{col.getString("newVal"),tbname,col.getString("col")};
				jdbcTemplate.update(csql,params);
			}catch(Exception e){
				log.error(e.toString());
			}
		}
	}
	//修改列类型
	private void modifyColType(String tbname,JSONArray edtType) {
		if(edtType==null||edtType.length()==0){
			return;
		}
		for(int i=0;i<edtType.length();i++){
			JSONObject col = null;
			try{
				col = edtType.getJSONObject(i);
				if(col==null){
					continue;
				}
				StringBuffer sql = new StringBuffer("alter table ");
				StringBuffer tmpsql = new StringBuffer("alter table TMP_");
				sql.append(tbname).append(" modify ").append(col.getString("col"));
				tmpsql.append(tbname).append(" modify ").append(col.getString("col"));
				if(col.getInt("newVal")==0){
					sql.append(" VARCHAR2(100)");
					tmpsql.append(" VARCHAR2(100)");
				}else if(col.getInt("newVal")==1){
					sql.append(" NUMBER(9) ");
					tmpsql.append(" VARCHAR2(20)");
				}else if(col.getInt("newVal")==2){
					sql.append(" VARCHAR2(2000)");
					tmpsql.append(" VARCHAR2(2000)");
				}else if(col.getInt("newVal")==3){
					sql.append(" NUMBER(16,2)");
					tmpsql.append(" VARCHAR2(30)");
				}
				//修改列类型
				jdbcTemplate.execute(sql.toString());
				jdbcTemplate.execute(tmpsql.toString());
				//修改配置信息
				String csql = "update exceltb_columns set coltype=? where tbname=? and colname=?";
				Object[] params = new Object[]{col.getInt("newVal"),tbname,col.getString("col")};
				jdbcTemplate.update(csql,params);
			}catch(Exception e){
				log.error(e.toString());
			}
		}
	}
	//修改列名
	private void modifyColName(String tbname,JSONArray edtCol) {
		if(edtCol==null||edtCol.length()==0){
			return;
		}
		for(int i=0;i<edtCol.length();i++){
			JSONObject col = null;
			try{
				col = edtCol.getJSONObject(i);
				if(col==null){
					continue;
				}
				StringBuffer sql = new StringBuffer("alter table ");
				StringBuffer tmpsql = new StringBuffer("alter table TMP_");
				sql.append(tbname).append(" rename column ").append(col.getString("col")).append(" to ").append(col.getString("newVal"));
				tmpsql.append(tbname).append(" rename column ").append(col.getString("col")).append(" to ").append(col.getString("newVal"));
				//修改列名
				jdbcTemplate.execute(sql.toString());
				jdbcTemplate.execute(tmpsql.toString());
				//修改配置信息
				String csql = "update exceltb_columns set colname=? where tbname=? and colname=?";
				Object[] params = new Object[]{col.getString("newVal"),tbname,col.getString("col")};
				jdbcTemplate.update(csql,params);
			}catch(Exception e){
				log.error(e.toString());
			}
		}
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
	
	//及时更新
	public boolean matchEn(String tb,int xh,int iid,String userid,String swdjzh) {
		StringBuffer sql = new StringBuffer("update ").append(tb).append(" set swdjzh=? where userid=? and xh=? and iid=?");
		jdbcTemplate.update(sql.toString(),new Object[]{swdjzh,userid,xh,iid});
		return true;
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
	public boolean deleteColumn(String tb, String col) {
		StringBuffer dsql = new StringBuffer("alter table ");
		dsql.append(tb).append(" drop column ").append(col);
		jdbcTemplate.execute(dsql.toString());
		dsql = new StringBuffer("alter table tmp_");
		dsql.append(tb).append(" drop column ").append(col);
		jdbcTemplate.execute(dsql.toString());
		dsql = new StringBuffer("delete from exceltb_columns where tbname=? and colname=?");
		jdbcTemplate.update(dsql.toString(),new Object[]{tb,col});
		dsql = new StringBuffer("delete from excelmap where tbname=? and colname=?");
		jdbcTemplate.update(dsql.toString(),new Object[]{tb,col});
		return true;
	}
	
}
