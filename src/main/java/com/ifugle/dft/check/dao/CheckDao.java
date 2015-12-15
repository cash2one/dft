package com.ifugle.dft.check.dao;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.ParameterizedBeanPropertyRowMapper;
import org.springframework.transaction.annotation.Transactional;

import com.ifugle.dft.system.entity.Code;
import com.ifugle.dft.utils.*;
import com.ifugle.dft.check.entity.ChangeInfo;
import com.ifugle.dft.check.entity.En_field;
import com.ifugle.dft.check.entity.Hd_log;
import com.ifugle.dft.dao.BaseDao;
@Transactional
public class CheckDao extends BaseDao{
	private static Logger log = Logger.getLogger(CheckDao.class);
	@Autowired
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate){
		this.jdbcTemplate = jdbcTemplate;
	}
	public List getEns(String sql, int start, int limit,Class cls) {
		final List rsts = new ArrayList();
		try{
			//rsts = queryForPage(sql,start,limit,cls);
			StringBuffer rSql = new StringBuffer("SELECT * FROM (SELECT A.*, rownum r FROM (");
			rSql.append(sql);
			rSql.append(") A WHERE rownum<=");
			rSql.append((start+limit));
			rSql.append(") B WHERE r>");
			rSql.append(start);
			jdbcTemplate.query(rSql.toString(),new RowCallbackHandler() {  
			    @Override  
			    public void processRow(ResultSet rs) throws SQLException {
			    	ResultSetMetaData rsmd=rs.getMetaData();
			  		int colNum=rsmd.getColumnCount();
			  		Map row = new HashMap(); 
			  		for(int i=1;i<=colNum;i++){
			  			String cn = rsmd.getColumnName(i).toLowerCase();
			  			row.put(cn, rs.getString(cn)); 
			  		}
			        rsts.add(row);  
			  }});  

		}catch(Throwable e){
			log.error(e.toString());
		}
		return rsts;
	}

	public Map getEnInfo(String sql) {
		Map enInfos = null;
		try{
			enInfos = jdbcTemplate.queryForMap("select * from ("+sql+") where rownum<2", null);
		}catch (EmptyResultDataAccessException e) {  
            return null;  
        }  
		return enInfos;
	}
	//执行核定
	public String[] excuteCheck(String sql,String userid,String xh,String fldName,String newVal,int affect,int from,int to,int affactMode,int exeType,String ip,String mac,String checkType,String remark){
		final String fUser=userid;
		final String fXh=xh;
		final String fFldName=fldName;
		final String fNewVal=newVal;
		final int fExeType=exeType;
		final int fAffect=affect;
		final int fFrom=from;
		final int fTo=to;
		final int fAffactMode=affactMode;
		final String fIp = ip;
		final String fMac = mac;
		final String fCheckType = checkType;
		final String fRemark = remark;
		String flag = "1";
		final String[] results = new String[2];
		try{
			flag = (String)jdbcTemplate.execute(sql,new CallableStatementCallback() {
				public Object doInCallableStatement(CallableStatement cs)throws SQLException, DataAccessException {
					cs.setInt(1, fExeType);
					cs.setString(2, fXh);    // 输入参数  
	                cs.setString(3, fFldName);   // 输入参数  
	                cs.setString(4, fNewVal);  // 输入参数  
	                cs.setInt(5, fAffect);   // 输入参数  
	                cs.setInt(6, fFrom);       // 输入参数  
	                cs.setInt(7, fTo); 
	                cs.setInt(8, fAffactMode);
	                cs.setString(9, fUser);
	                cs.setString(10, fIp);
	                cs.setString(11, fMac);
	                cs.setString(12, fCheckType);
	                cs.setString(13, fRemark);
	                cs.registerOutParameter(14,Types.VARCHAR);//输出参数  
	                cs.registerOutParameter(15,Types.VARCHAR);//输出参数  
	                cs.execute();  
	                String flag = cs.getString(14);
	                String tmpInfo = cs.getString(15);
	                results[0] = flag;
	                results[1] = tmpInfo;
	                return flag;  
				} 
			});
		}catch(Throwable e){
			log.error(e.toString());
			results[0] = "9";
            results[1] = "执行存储过程时发生错误！";
		}
		return results;
	}

	public boolean saveCheckTask(String sql, Object[] params) {
		boolean done = false;
		jdbcTemplate.update(sql,params);
		done = true;
		return done;
	}
	/**
	 * 尝试对sql进行查询，如果查询不成功，返回false
	* @param sql
	* @return
	 */
	public boolean tryExcute(String sql) {
		try{
			String countsql = "select count(*) count from ("+sql+")";
			jdbcTemplate.queryForInt(countsql);
		}catch(Exception e){
			log.error(e.toString());
			return false;
		}
		return true;
	}
	
	public List readExcel(String filePath,int beginRow,int matchCol)throws Exception{
		if(filePath==null||"".equals(filePath)){
			throw new Exception("请指定待打开的excel文件目录！"); 
		}
		List qymc = ExcelReader.readExcelColumn(filePath, beginRow-1, matchCol-1);
		return qymc;
	}
	/**
	 * 
	* @param matchFlds
	* @param userid
	* @param type
	* @return
	 */
	public int matchData(List matchFlds,String userid,String type) {
		int count = 0;

		StringBuffer bSql=new StringBuffer("delete from match_enterprise where s_id=?");
		Object[] params = new Object[]{userid};
		jdbcTemplate.update(bSql.toString(),params);
		if(type.equals("sh")){
			bSql=new StringBuffer("insert into match_enterprise(s_id,swdjzh,xh)values(?,?,?)");
		}else{
			bSql=new StringBuffer("insert into match_enterprise(s_id,qymc,xh)values(?,?,?)");
		}
		for(int i=0;i<matchFlds.size();i++){
			String str = (String)matchFlds.get(i);
			jdbcTemplate.update(bSql.toString(),new Object[]{userid,str,i+1});
		}
		//转换数据
		if(type.equals("sh")){
			bSql = new StringBuffer("update match_enterprise m set qymc = (SELECT max(mc) from dj_cz e WHERE m.swdjzh=e.swdjzh) where exists (select 1 from dj_cz e WHERE m.swdjzh=e.swdjzh)");
		}else{
			bSql = new StringBuffer("update match_enterprise m set swdjzh = (SELECT max(swdjzh) from dj_cz e WHERE m.qymc=e.mc) where exists (select 1 from dj_cz e WHERE m.qymc=e.mc)");
		}
		params = null;
		jdbcTemplate.update(bSql.toString(),params);
		//查询转换结果
		if(type.equals("sh")){
			bSql = new StringBuffer("select count(1) from match_enterprise where qymc is not null and s_id=?");
		}else{
			bSql = new StringBuffer("select count(1) from match_enterprise where swdjzh is not null and s_id=?");
		}
		params = new Object[]{userid};
		count = jdbcTemplate.queryForInt(bSql.toString(),params);
		return count;
	}
	
	public boolean acceptEnterprise(String[] xhs,int aType){
		StringBuffer sql = new StringBuffer("update dj_cz set qyzt = 0 where xh=? ");
		for(int i=0;i<xhs.length;i++){
			String xh = xhs[i];
			int ixh = 0;
			try{
				ixh = Integer.parseInt(xh);
			}catch(Exception e){
			}
			jdbcTemplate.update(sql.toString(),new Object[]{new Integer(ixh)});
		}
		//如果是变更接收，将该企业的变更信息status设置为0.
		if(aType ==2){
			for(int i=0;i<xhs.length;i++){
				String xh = xhs[i];
				int ixh = 0;
				try{
					ixh = Integer.parseInt(xh);
				}catch(Exception e){
				}
				jdbcTemplate.update("update bg_info set status=0 where xh=?",new Object[]{new Integer(ixh)});
			}
		}
		return true;
	}
	/**
	 * 获取变更信息
	* @param swdjzh
	* @return
	 */
	public List getChangeInfos(int xh) {
		List infos = null;
		Configuration cg = (Configuration)ContextUtil.getBean("config");
		Map mFlds =cg.getFinanceFieldsMap();
		StringBuffer sql = new StringBuffer("select b.xh,b.swdjzh,b.c_field,d.mc c_fldname,b.wdly,f_init,f_dest,t_init,t_dest,b.bgrq,b.status flag ");
		sql.append("from bg_info b,en_dictionary d where b.xh=? and b.status=1 and b.c_field=d.field and d.tname='DJ_CZ'");
		infos = jdbcTemplate.query(sql.toString(),new Object[]{new Integer(xh)},ParameterizedBeanPropertyRowMapper.newInstance(ChangeInfo.class));
		if(infos!=null&&infos.size()>0){
			for(int i=0;i<infos.size();i++){
				ChangeInfo ci = (ChangeInfo)infos.get(i);
				String fld = ci.getC_field();
				//如果字段的值是来自某编码表的，那么获取名称
				En_field ffld=(En_field)mFlds.get(fld);
				if(ffld==null)
					continue;
				StringBuffer cSql = new StringBuffer("select to_char(").append(ffld.getField()).append(") from dj_cz where xh=?");
				String result = (String)jdbcTemplate.queryForObject(cSql.toString(),new Object[]{new Integer(xh)},String.class);
				ci.setF_now(result);
				if(ffld.getVal_src()!=1&&!"".equals(ffld.getMapbm())){
					String mapbm = ffld.getMapbm();
					String fival = ci.getF_init();
					String fdval = ci.getF_dest();
					String tival = ci.getT_init();
					String tdval = ci.getT_dest();
					int who = ci.getWdly();
					StringBuffer fsql = new StringBuffer("select bm,mc from bm_cont where table_bm=? and bm in (?,?,?)");
					StringBuffer tsql = new StringBuffer("select t_bm bm,t_mc mc from bm where name=? and t_bm in (?,?) and who=?");
					List flst = jdbcTemplate.query(fsql.toString(),new Object[]{mapbm,fival,fdval,result},ParameterizedBeanPropertyRowMapper.newInstance(Code.class));
					List tlst = jdbcTemplate.query(tsql.toString(),new Object[]{mapbm,tival,tdval,new Integer(who)},ParameterizedBeanPropertyRowMapper.newInstance(Code.class));
					if(flst!=null&&flst.size()>0){
						for(int j=0;j<flst.size();j++){
							Code cd = (Code)flst.get(j);
							if(fival.equals(cd.getBm())){
								ci.setFiname(cd.getMc());
							}
							if(fdval.equals(cd.getBm())){
								ci.setFdname(cd.getMc());
							}
							if(result!=null&&result.equals(cd.getBm())){
								ci.setFnname(cd.getMc());
							}
						}
					}
					if(tlst!=null&&tlst.size()>0){
						for(int j=0;j<tlst.size();j++){
							Code cd = (Code)tlst.get(j);
							if(tival.equals(cd.getBm())){
								ci.setTiname(cd.getMc());
							}
							if(tdval.equals(cd.getBm())){
								ci.setTdname(cd.getMc());
							}
						}
					}
				}else{
					ci.setFiname(ci.getF_init());
					ci.setFdname(ci.getF_dest());
					ci.setTiname(ci.getT_init());
					ci.setTdname(ci.getT_dest());
					ci.setFnname(ci.getF_now());
				}
			}
		}
		
		return infos;
	}
	/**
	 * 变更信息接受新值
	* @param xh 登记表记录的xh
	* @param fld
	* @param newVal
	* @return
	 */
	public boolean checkChangeEn(int xh, String fld, String newVal) {
		final int[] ftype =new int[]{0};
		// 确定字段的类型
		StringBuffer sType = new StringBuffer("select ");
		sType.append(fld).append(" from dj_cz ").append(" where xh=?");
		jdbcTemplate.query(sType.toString(),new Object[]{new Integer(xh)},new RowCallbackHandler(){
			public void processRow(ResultSet rs) throws SQLException {
				ResultSetMetaData rsmd = rs.getMetaData();
				int tt=rsmd.getColumnType(1);
				ftype[0]=tt;
			}
		});
		Object[] params = null;
		StringBuffer sql=new StringBuffer("update dj_cz set ").append(fld);
		sql.append("=? where xh=?");
		// 根据字段类型确定
		if (ftype[0] == java.sql.Types.VARCHAR){
			params = new Object[]{newVal,new Integer(xh)};
		}
		if (ftype[0] == java.sql.Types.NUMERIC|| ftype[0] == java.sql.Types.INTEGER){
			params = new Object[]{new Integer(newVal),new Integer(xh)};
		}
		jdbcTemplate.update(sql.toString(), params);
		return true;
	}
	public Map getCheckLogInfo(String sql) {
		Map infos = null;
		try{
			infos = jdbcTemplate.queryForMap(sql, null);
		}catch (EmptyResultDataAccessException e) {  
            return null;  
        }  
		return infos;
	}
}
