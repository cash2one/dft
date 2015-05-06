package com.ifugle.dft.check.dao;

import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.ParameterizedBeanPropertyRowMapper;
import org.springframework.transaction.annotation.Transactional;
import com.ifugle.dft.check.entity.Enterprise;
import com.ifugle.dft.check.entity.Rtk_Hdlr;
import com.ifugle.dft.dao.BaseDao;
import com.ifugle.dft.utils.Configuration;
import com.ifugle.dft.utils.ContextUtil;
@Transactional
public class EnDao extends BaseDao{
	private static Logger log = Logger.getLogger(CheckDao.class);
	protected Configuration cg;
	@Autowired
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate){
		this.jdbcTemplate = jdbcTemplate;
	}
	@Autowired
	public void setConfiguration(Configuration config){
		this.cg = config;
	}
	public String[] autoMapEnterprises(String sql) {
		String[] results = null;
		results = (String[])jdbcTemplate.execute(sql,new CallableStatementCallback() {
			public Object doInCallableStatement(CallableStatement cs)throws SQLException, DataAccessException {
	            cs.registerOutParameter(1,Types.VARCHAR);//输出参数  
	            cs.registerOutParameter(2,Types.VARCHAR);//输出参数  
	            cs.execute();  
	            String flag = cs.getString(1);
	            String info = cs.getString(2);
	            String[] tr = new String[]{"0",""};
	            tr[0]=flag;
	            tr[1]=info;
	            return tr;  
			} 
		});
		return results;
	}
	
	public String[] undoMapping(String sql,String swdjzhs) {
		final String fEnXhs=swdjzhs;
		String[] results = null;
		results = (String[])jdbcTemplate.execute(sql,new CallableStatementCallback() {
			public Object doInCallableStatement(CallableStatement cs)throws SQLException, DataAccessException {
				cs.setString(1, fEnXhs);    
	            cs.registerOutParameter(2,Types.VARCHAR);  
	            cs.registerOutParameter(3,Types.VARCHAR);  
	            cs.execute();  
	            String flag = cs.getString(2);
	            String info = cs.getString(3);
	            String[] tr = new String[]{"0",""};
	            tr[0]=flag;
	            tr[1]=info;
	            return tr;  
			} 
		});
		return results;
	}
	public String[] mapEns(String sql, int dsxh, int gsxh) {
		final int fDsxh=dsxh;
		final int fGsxh=gsxh;
		String[] results = null;
		results = (String[])jdbcTemplate.execute(sql,new CallableStatementCallback() {
			public Object doInCallableStatement(CallableStatement cs)throws SQLException, DataAccessException {
				cs.setInt(1, fDsxh); 
				cs.setInt(2, fGsxh); 
	            cs.registerOutParameter(3,Types.VARCHAR);  
	            cs.registerOutParameter(4,Types.VARCHAR);  
	            cs.execute();  
	            String flag = cs.getString(3);
	            String info = cs.getString(4);
	            String[] tr = new String[]{"0",""};
	            tr[0]=flag;
	            tr[1]=info;
	            return tr;  
			} 
		});
		return results;
	}
	//删除虚拟企业
	public String[] deleteVEn(String sql,String xhs) {
		int ixh = 0;
		try{
			ixh = Integer.parseInt(xhs);
		}catch(Throwable e){
		}
		final int fxh = ixh;
		String[] results = null;
		results = (String[])jdbcTemplate.execute(sql,new CallableStatementCallback() {
			public Object doInCallableStatement(CallableStatement cs)throws SQLException, DataAccessException {
				cs.setInt(1, fxh); 
	            cs.registerOutParameter(2,Types.VARCHAR);  
	            cs.registerOutParameter(3,Types.VARCHAR);  
	            cs.execute();  
	            String flag = cs.getString(2);
	            String info = cs.getString(3);
	            String[] tr = new String[]{"0",""};
	            tr[0]=flag;
	            tr[1]=info;
	            return tr;  
			} 
		});
		return results;
	}
	/**
	 * 检查虚拟企业的税号是否重复
	* @param xh 当前记录的id；
	* 如果是修改模式，id>0，swdjzh值不能存在于dj_cz表中（当前记录除外）。
	* 如果是新增模式，id<0，swdjzh值不能存在于dj_cz表中。
	* @param swdjzh 传入的待检查的税号。
	* @return
	 */
	public boolean checkSwdjzh(int xh, String swdjzh) {
		boolean dup = false;
		StringBuffer sql = new StringBuffer("select count(xh) count from dj_cz where swdjzh=?");
		if(xh>0){
			sql.append(" and xh<>").append(xh);
		}
		int count = jdbcTemplate.queryForInt(sql.toString(), new Object[]{swdjzh});
		dup = count>0;
		return dup;
	}
	/**
	 * 保存虚拟企业
	* @param mode
	* @param swdjzh
	* @param mc
	* @param czfpbm
	* @return
	 */
	public boolean saveVirtualEn(Map params,Map fileFields) {
		boolean done = false;
		String mode = (String)params.get("cMode") ==null?"add":(String)params.get("cMode");
		String xh = (String)params.get("xh") ==null?"":(String)params.get("xh");
		String swdjzh = (String)params.get("swdjzh") ==null?"":(String)params.get("swdjzh");
		String mc = (String)params.get("mc") ==null?"":(String)params.get("mc");
		String dz = (String)params.get("dz") ==null?"":(String)params.get("dz");
		String fddbr = (String)params.get("fddbr") ==null?"":(String)params.get("fddbr");
		String sbgrq = (String)params.get("bgrq") ==null?"0":(String)params.get("bgrq");
		String ztbm = (String)params.get("ztbm_bm") ==null?"":(String)params.get("ztbm_bm");
		String jjxzbm = (String)params.get("jjxzbm_bm") ==null?"":(String)params.get("jjxzbm_bm");
		String hybm = (String)params.get("hybm_bm") ==null?"":(String)params.get("hybm_bm");
		String czfpbm = (String)params.get("czfpbm_bm") ==null?"":(String)params.get("czfpbm_bm");
		StringBuffer sql = new StringBuffer("insert into dj_cz(xh,qynm,dzdah,swdjzh,mc,dz,fddbr,bgrq,qyzt,ismap,qysx,ztbm,jjxzbm,hybm,czfpbm)");
		sql.append(" values(SQ_DJ_CZ.nextval,?,?,?,?,?,?,?,0,2,3,?,?,?,?)");
		if("modify".equals(mode)){
			sql = new StringBuffer("update dj_cz set qynm=?,dzdah=?,swdjzh=?,mc=?,dz=?,fddbr=?,bgrq=?,ztbm=?,jjxzbm=?,hybm=?,czfpbm=? where xh=");
			sql.append(xh);
		}
		int bgrq = 0;
		try{
			bgrq = Integer.parseInt(sbgrq);
		}catch(Exception e){
		}
		jdbcTemplate.update(sql.toString(),new Object[]{swdjzh+"_DS",swdjzh+"_GS",swdjzh,mc,dz,fddbr,new Integer(bgrq),ztbm,jjxzbm,hybm,czfpbm});
		done = true;
		return done;
	}
	/**
	 * 获取指定税号的企业信息
	* @param sql
	* @param swdjzh
	* @return
	 */
	public Map getVirtualEnInfo(String sql, String swdjzh) {
		Map enInfos = null;
		enInfos = jdbcTemplate.queryForMap(sql, new Object[]{swdjzh});
		return enInfos;
	}
	/**
	 * 删除指定凭证号的凭证明细
	* @param swdjzh
	* @param pzh
	* @return
	 */
	public String[] deletePz(String swdjzh, String pzh) {
		String[] result=new String[2];
		//调用存储过程
        cg = (Configuration)ContextUtil.getBean("config");
		String proName = cg.getString("proDeletePz","PKG_CHECK.DELETEPZ");
		if(proName==null||"".equals(proName)){
			return new String[]{"1","未找到用于删除凭证的存储过程！"};
		}
		StringBuffer dsql = new StringBuffer("{call ");
		dsql.append(proName).append("(?,?,?,?)}");
		String flag = "0";
		final String fSwdjzh= swdjzh;
		final String fPzh = pzh;
		final String[] fInfos =new String[2];
		flag = (String)jdbcTemplate.execute(dsql.toString(),new CallableStatementCallback() {
			public Object doInCallableStatement(CallableStatement cs)throws SQLException, DataAccessException {
				cs.setString(1, fSwdjzh);
				cs.setString(2,fPzh);
	            cs.registerOutParameter(3,Types.VARCHAR);  
	            cs.registerOutParameter(4,Types.VARCHAR);  
	            cs.execute();  
	            String flag = cs.getString(3);
	            String infos = cs.getString(4);
	            if(!"1".equals(flag)){
	                log.error(infos);
	            }
	            fInfos[0]=flag;
	            fInfos[1]="1".equals(flag)?"删除凭证成功！":"删除凭证时发生错误。"+infos;
	            return flag;  
			} 
		});
		result = fInfos;
		return result;
	}
	/**
	 * 获取某单位所有的手工录入凭证号
	* @param swdjzh
	* @return
	 */
	public List getEnPzhs(String swdjzh) {
		List infos = null; 
		StringBuffer sql = new StringBuffer("select a.*,b.name username from (");
		sql.append("select distinct(sph) sph,to_char(rkrq)rkrq,remark,userid from rtk_hdlr where swdjzh=? )a,");
		sql.append("users b where a.userid=b.userid(+) order by sph desc");
		try{
			infos = jdbcTemplate.query(sql.toString(),new Object[]{swdjzh},ParameterizedBeanPropertyRowMapper.newInstance(Rtk_Hdlr.class));
		}catch(Throwable e){
			log.error(e.toString());
		}
		return infos;
	}
	/**
	 * 获取指定单位指定凭证号的凭证明细
	* @param swdjzh
	* @param pzh
	* @return
	 */
	@SuppressWarnings("unchecked")
	public List getPzDetails(String swdjzh, String pzh) {
		List infos = null; 
		StringBuffer sql = new StringBuffer("select czfpbm fpbm,c.mc fpmc,szbm,d.mc szmc,ysjcbm,b.mc ysjc,yskmbm,a.mc yskm,se je,r.xh from ");
		sql.append("rtk_hdlr r,(select bm,mc from bm_cont where table_bm = 'BM_YSKM')a,(select bm,mc from bm_cont where table_bm = 'BM_YSJC')b,");
		sql.append("(select bm,mc from bm_cont where table_bm = 'BM_CZFP')c,(select bm,mc from bm_cont where table_bm = 'BM_SZ')d ");
		sql.append(" where r.yskmbm=a.bm(+) and r.ysjcbm=b.bm(+) and r.czfpbm=c.bm(+) and r.szbm=d.bm(+) ");
		sql.append(" and swdjzh=? and sph=? ");
		try{
			infos = jdbcTemplate.query(sql.toString(),new Object[]{swdjzh,pzh},ParameterizedBeanPropertyRowMapper.newInstance(Rtk_Hdlr.class));
		}catch(Throwable e){
			log.error(e.toString());
		}
		return infos;
	}
	@SuppressWarnings("unchecked")
	public String[] savePzDetail(String swdjzh,String sph, String strRkrq, String remark,String userid,List pzs) {
		String[] result = new String[]{"-1",sph};
		//先插入rtk_hdlr
		StringBuffer sql = new StringBuffer("select to_char(to_number(to_char(sysdate,'yyyy'))*10000+nvl(max(to_number(substr(sph,5,4))),0)+1)");
		sql.append(" as sph from rtk_hdlr where substr(sph,1,4)=to_char(sysdate,'yyyy')");
		StringBuffer isql = new StringBuffer(" insert into rtk_hdlr(swdjzh,rkrq,yskmbm,ysjcbm,czfpbm,szbm,se,key,userid,sph,xh,remark)values(");
		isql.append(" ?,?,?,?,?,?,?,sq_rtk_hdlr.nextval,?,?,?,?)");

		if(sph==null||"".equals(sph)){
			sph = queryForString(sql.toString());
		}else{
			jdbcTemplate.update("delete from rtk_hdlr where swdjzh=? and sph=?",new Object[]{swdjzh,sph});
		}
		for(int i = 0 ;i<pzs.size();i++){
			Rtk_Hdlr hd = (Rtk_Hdlr)pzs.get(i);
			Object[] paras = new Object[]{swdjzh,strRkrq,hd.getYskmbm(),hd.getYsjcbm(),hd.getFpbm(),
					hd.getSzbm(),hd.getJe(),userid,sph,String.valueOf(i+1),remark};
			jdbcTemplate.update(isql.toString(),paras);
		}
		//调用存储过程，从rtk_hdlr向rtk搬迁
		cg = (Configuration)ContextUtil.getBean("config");
		String proName = cg.getString("proSavePz2RTK","PKG_CHECK.SAVEPZ2RTK");
		if(proName==null||"".equals(proName)){
			return new String[]{"-1",sph,"凭证已保存，但未找到将凭证信息保存到入退库的存储过程！"};
		}
		StringBuffer dsql = new StringBuffer("{call ");
		dsql.append(proName).append("(?,?,?,?)}");
		String flag = "0";
		final String fSwdjzh= swdjzh;
		final String fSph = sph;
		final String[] fInfos =new String[3];
		flag = (String)jdbcTemplate.execute(dsql.toString(),new CallableStatementCallback() {
			public Object doInCallableStatement(CallableStatement cs)throws SQLException, DataAccessException {
					cs.setString(1, fSwdjzh);
					cs.setString(2,fSph);
		            cs.registerOutParameter(3,Types.VARCHAR);  
		            cs.registerOutParameter(4,Types.VARCHAR);  
		            cs.execute();  
		            String flag = cs.getString(3);
		            String infos = cs.getString(4);
		            if(!"1".equals(flag)){
		                log.error(infos);
		            }
		        fInfos[0]=flag;
		        fInfos[1]=fSph;
		        fInfos[2]="1".equals(flag)?"保存凭证成功！":"凭证信息迁移到RTK时发生错误:"+infos;
		        return flag;  
			} 
		});
		result = fInfos;
		return result;
	}
	public List getNewPzDetail(String userid) {
		List dts = null;
	    StringBuffer sql = new StringBuffer("select c.bm fpbm,c.mc fpmc,d.bm szbm,d.mc szmc,b.bm ysjcbm,b.mc ysjc,a.bm yskmbm,a.mc yskm,se je,");
		sql.append("to_char(r.xh)xh from rtk_hdlr_imp r,(select bm,mc from bm_cont where table_bm = 'BM_YSKM')a,");
		sql.append("(select bm,mc from bm_cont where table_bm = 'BM_YSJC')b,");
		sql.append("(select bm,mc from bm_cont where table_bm = 'BM_CZFP')c,(select bm,mc from bm_cont where table_bm = 'BM_SZ')d ");
		sql.append(" where r.yskmbm=a.mc(+) and r.ysjcbm=b.mc(+) and r.czfpbm=c.mc(+) and r.szbm=d.mc(+) ");
		sql.append(" and userid=? order by xh");
		try{
			dts = jdbcTemplate.query(sql.toString(),new Object[]{userid},ParameterizedBeanPropertyRowMapper.newInstance(Rtk_Hdlr.class));
			//jdbcTemplate.update("delete from rtk_hdlr_imp where userid=?",new Object[]{userid});
		}catch(Throwable e){
			log.error(e.toString());
		}
		return dts;
	}
	
	public int getImportedCount(String userid) {
		int cc = 0;
		StringBuffer sql = new StringBuffer("select count(*) from rtk_hdlr_imp where userid='").append(userid).append("'");
		try{
			cc = queryCount(sql.toString());
		}catch(Throwable e){
			log.error(e.toString());
		}
		return cc;
	}
	public List getEns(String sql, int start, int limit) {
		List rsts = null;
		try{
			rsts = queryForPage(sql,start,limit,Enterprise.class);
		}catch(Throwable e){
			log.error(e.toString());
		}
		return rsts;
	}
}
