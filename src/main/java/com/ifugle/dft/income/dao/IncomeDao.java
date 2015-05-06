package com.ifugle.dft.income.dao;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.text.DecimalFormat;
import java.util.List;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.ParameterizedBeanPropertyRowMapper;
import org.springframework.transaction.annotation.Transactional;

import com.ifugle.dft.check.entity.Rtk_Hdlr;
import com.ifugle.dft.dao.BaseDao;
import com.ifugle.dft.income.entity.EnHasRules;
import com.ifugle.dft.income.entity.Rule;
import com.ifugle.dft.income.entity.RuleOfP;
@Transactional
public class IncomeDao extends BaseDao{
	private static Logger log = Logger.getLogger(IncomeDao.class);
	@Autowired
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate){
		this.jdbcTemplate = jdbcTemplate;
	}
	@SuppressWarnings("unchecked")
	public List getPRuleEns(String sql, int start, int limit,Class cls) {
		List rsts = null;
		StringBuffer dsql = new StringBuffer("select a.ruleid,nvl(c.qybj,0) inuse,a.czfpbm,a.sebl,b.mc czfpmc,a.szbm,s.mc szmc from qyfcxx a,");
		dsql.append("(select * from bm_cont where table_bm='BM_CZFP')b,(select * from bm_cont where table_bm='BM_SZ')s,rules c ");
		dsql.append("where a.czfpbm=b.bm(+) and a.qynm||'_'||a.dzdah=c.objaffect(+) and a.szbm=s.bm(+) ");
		dsql.append(" and a.ruleid=c.ruleid(+) and a.swdjzh=? order by c.qybj desc,ruleid desc");
		try{
			rsts = queryForPage(sql,start,limit,cls);
			if(rsts!=null&&rsts.size()>0){
				for(int i=0;i<rsts.size();i++){
					EnHasRules en = (EnHasRules)rsts.get(i);
					if(en==null){
						continue;
					}
					String swdjzh = en.getSwdjzh();
					List dt = jdbcTemplate.query(dsql.toString(),new Object[]{swdjzh},ParameterizedBeanPropertyRowMapper.newInstance(RuleOfP.class));
					int preRuleid = -1, ruleid = -1,inuse = 0;
					StringBuffer details = new StringBuffer("");
					DecimalFormat df = new DecimalFormat("#0.00");
					if(dt!=null){
						for(int j=0;j<dt.size();j++){
							RuleOfP rp = (RuleOfP)dt.get(j);
							ruleid = rp.getRuleid();
							if(preRuleid!=-1&& preRuleid!=ruleid){
								break;
							}
							details.append(rp.getCzfpmc()).append("(").append(rp.getSzmc()==null?"":rp.getSzmc()).append("):").append(df.format(rp.getSebl())).append("%");
							preRuleid = rp.getRuleid();
							inuse = rp.getInuse();
						}
					}
					en.setRule(details.toString());
					en.setInuse(inuse);
				}
			}
		}catch(Throwable e){
			log.error(e.toString());
		}
		return rsts;
	}
	//删除指定企业的所有按比例分成规则
	public boolean deletePRulesOfEn(String[] qynms,String[] dzdahs) {
		boolean done  = false;
		
		StringBuffer dtsql = new StringBuffer("delete from qyfcxx where qynm =? and dzdah=?");
		StringBuffer sql = new StringBuffer("delete from rules where objaffect =? and ruletype=1");
		for(int i = 0;i<qynms.length;i++){
			String sqynm = qynms[i];
			String sdzdah = dzdahs[i];
			jdbcTemplate.update(dtsql.toString(),new Object[]{sqynm,sdzdah});
			jdbcTemplate.update(sql.toString(),new Object[]{sqynm+"_"+sdzdah});
		}
		done = true;
		return done;
	}
	//获取指定企业的所有规则
	public List getPRulesOfEn(String qynm,String dzdah) {
		List rsts = null;
		StringBuffer sql = new StringBuffer("select ruleid,qybj,begindate,enddate from rules where objaffect=? order by qybj desc,ruleid desc");
		StringBuffer dsql = new StringBuffer("select czfpbm,szbm,sebl,b.mc czfpmc,c.mc szmc from qyfcxx a,(select * from bm_cont where table_bm='BM_CZFP')b ");
		dsql.append(",(select * from bm_cont where table_bm='BM_SZ')c where a.czfpbm = b.bm(+) and a.szbm=c.bm(+) and a.ruleid=?");
		try{
			rsts = jdbcTemplate.query(sql.toString(),new Object[]{qynm+"_"+dzdah},ParameterizedBeanPropertyRowMapper.newInstance(Rule.class));
			int ruleid = -1;
			DecimalFormat df = new DecimalFormat("#0.00");
			if(rsts!=null){
				for(int i=0;i<rsts.size();i++){
					Rule r = (Rule)rsts.get(i);
					ruleid = r.getRuleid();
					StringBuffer details = new StringBuffer("");
					List dts = jdbcTemplate.query(dsql.toString(),new Object[]{new Integer(ruleid)},ParameterizedBeanPropertyRowMapper.newInstance(RuleOfP.class));
					if(dts==null||dts.size()==0){
						continue;
					}
					for(int j=0;j<dts.size();j++){
						RuleOfP rp = (RuleOfP)dts.get(j);
						details.append(rp.getCzfpmc()).append("(").append(rp.getSzmc()==null?"":rp.getSzmc()).append("):").append(df.format(rp.getSebl())).append("%");
					}
					r.setDescription(details.toString());
				}
			}
		}catch(Throwable e){
			log.error(e.toString());
		}
		return rsts;
	}
	//删除指定ruleid的规则
	public boolean deletePRulesByID(String[] ruleIds) {
		boolean done  = false;
		StringBuffer dtsql = new StringBuffer("delete from qyfcxx where ruleid =?");
		StringBuffer sql = new StringBuffer("delete from rules where ruleid =? ");
		for(int i = 0;i<ruleIds.length;i++){
			String strRid = ruleIds[i];
			Integer irid = null;
			try{
				irid = new Integer(strRid);
			}catch(Exception e){
					continue;
			}
			jdbcTemplate.update(dtsql.toString(),new Object[]{irid});
			jdbcTemplate.update(sql.toString(),new Object[]{irid});
		}
		done = true;
		return done;
	}
	
	//获取企业列表，用于添加规则。
	public List getEnsToAddPRules(String sql, int start, int limit,Class<EnHasRules> cls) {
		List ens = null;
		try{
			ens = queryForPage(sql,start,limit,cls);
		}catch(Throwable e){
			log.error(e.toString());
		}
		return ens;
	}
	//启用/禁用规则
	public String[] toggleRule(String sql,int ruleid) {
		final int fRuleid=ruleid;
		String flag = "1";
		final String[] results = new String[]{"0","未执行成功！"};
		try{
			flag = (String)jdbcTemplate.execute(sql,new CallableStatementCallback() {
				public Object doInCallableStatement(CallableStatement cs)throws SQLException, DataAccessException {
					cs.setInt(1, fRuleid);    
	                cs.registerOutParameter(2,Types.VARCHAR);  
	                cs.registerOutParameter(3,Types.VARCHAR);  
	                cs.execute();  
	                String tmpflag = cs.getString(2);
	                String tmpInfo = cs.getString(3);
	                if(!"1".equals(tmpflag)){
	                	log.error(tmpInfo);
	                }
	                results[0] = tmpflag;
	                results[1] = tmpInfo;
	                return tmpflag;  
				} 
			});
		}catch(Throwable e){
			log.error(e.toString());
		}
		return results;
	}
	
	public boolean saveRuleDetails(String qynm,String dzdah,String swdjzh,String timeType,String stdate, String edate, List rules) {
		boolean done = false;
		StringBuffer sql=new StringBuffer("insert into rules(ruleid,ruletype,qybj,detailtb,objaffect,begindate,enddate)");
		sql.append("values(sq_rules.nextval,1,0,'QYFCXX',?,?,?)");
		StringBuffer isql = new StringBuffer("insert into qyfcxx(ruleid,qynm,dzdah,swdjzh,czfpbm,szbm,sebl)select max(ruleid),?,?,?,?,?,? from rules");
		if("0".equals(timeType)){
			jdbcTemplate.update(sql.toString(),new Object[]{qynm+"_"+dzdah,new Integer(0),new Integer(99999999)});
		}else{
			int istart = 0,iend = 0;
			try{
				istart = Integer.parseInt(stdate);
			}catch(Exception e){
			}
			try{
				iend = Integer.parseInt(edate);
			}catch(Exception e){
			}
			jdbcTemplate.update(sql.toString(),new Object[]{qynm+"_"+dzdah,new Integer(istart),new Integer(iend)});
		}
		for(int i = 0 ;i<rules.size();i++){
			RuleOfP rp = (RuleOfP)rules.get(i);
			String czfpbm = rp.getCzfpbm();
			String szbm = rp.getSzbm();
			double sebl = rp.getSebl();
			BigDecimal bbl = new BigDecimal(sebl);
			double psebl= bbl.setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();
			Object[] paras = new Object[]{qynm,dzdah,swdjzh,czfpbm,szbm,new Double(psebl)};
			jdbcTemplate.update(isql.toString(),paras);
		}
		done = true;
		return done;
	}
}
