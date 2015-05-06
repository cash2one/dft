package com.ifugle.dft.income.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.ifugle.dft.check.entity.Enterprise;
import com.ifugle.dft.check.entity.Rtk_Hdlr;
import com.ifugle.dft.income.dao.IncomeDao;
import com.ifugle.dft.income.entity.EnHasRules;
import com.ifugle.dft.income.entity.RuleOfP;
import com.ifugle.dft.utils.Configuration;
import com.ifugle.dft.utils.ContextUtil;
import com.softwarementors.extjs.djn.config.annotations.DirectMethod;
import com.softwarementors.extjs.djn.servlet.ssm.WebContext;
import com.softwarementors.extjs.djn.servlet.ssm.WebContextManager;

public class IncomeHandler {
	private static Logger log = Logger.getLogger(IncomeHandler.class);
	private IncomeDao icmDao;
	private Configuration cg ;
	WebContext context ;
	@SuppressWarnings("unchecked")
	public IncomeHandler(){
		icmDao = (IncomeDao)ContextUtil.getBean("icmDao");
		cg = (Configuration)ContextUtil.getBean("config");
		if(cg!=null){
			cg.getHandlersMap().put("IncomeHandler","com.ifugle.dft.income.handler.IncomeHandler");
		}
	}
	public void setWebContext(WebContext ctx){
		if(context==null){
			context = ctx;
		}
	}
	
	@SuppressWarnings("unchecked")
	@DirectMethod
	public Map getPRuleEns(int start,int limit){
		Map infos = new HashMap();
		StringBuffer sql = new StringBuffer("select distinct a.xh,to_char(a.dzdah)dzdah,to_char(a.qynm)qynm,a.swdjzh,a.mc,a.dz,c.bm as czfpbm_bm,c.mc czfpbm from ");
	    sql.append(" (select d.*,qynm||'_'||dzdah enid from dj_cz d ) a,rules b,(select bm,mc from bm_cont where table_bm='BM_CZFP')c ");
	    sql.append(" where b.objaffect=a.enid(+) and a.czfpbm=c.bm(+) and b.ruletype=1 order by swdjzh");
		int count = icmDao.queryCount(sql.toString());
		infos.put("totalCount", new Integer(count));
		List ens = icmDao.getPRuleEns(sql.toString(),start,limit,EnHasRules.class);
		infos.put("rows", ens);
		return infos ;
	}
	@DirectMethod
	public String deletePRulesOfEn(String strQynms,String strDzdahs){
		StringBuffer json = new StringBuffer("{result:");
		if(strQynms==null||"".equals(strQynms)){
			json.append("true}");
			return json.toString();
		}
		String[] qynms = strQynms.split(",");
		String[] dzdahs = strDzdahs.split(",");
		if(qynms==null||qynms.length==0){
			json.append("true}");
			return json.toString();
		}
		boolean done = icmDao.deletePRulesOfEn(qynms,dzdahs);
		json.append(done).append("}");
		return json.toString();
	}
	@DirectMethod
	public List getPRulesOfEn(String qynm,String dzdah){
		List rules = null;
		rules = icmDao.getPRulesOfEn(qynm,dzdah);
		return rules;
		
	}
	@DirectMethod
	public String deletePRulesByID(String strRids){
		StringBuffer json = new StringBuffer("{result:");
		if(strRids==null||"".equals(strRids)){
			json.append("true}");
			return json.toString();
		}
		String[] ruleIds = strRids.split(",");
		if(ruleIds==null||ruleIds.length==0){
			json.append("true}");
			return json.toString();
		}
		boolean done = icmDao.deletePRulesByID(ruleIds);
		json.append(done).append("}");
		return json.toString();
	}
	@DirectMethod
	public String toggleRuleQybj(int ruleid,int qybj){
		StringBuffer json = new StringBuffer("{result:");
		if(ruleid<=0){
			json.append("true}");
			return json.toString();
		}
		String[] result = new String[]{"1",""};
		String sql = "";
		if(qybj==0){
			String proName = cg.getString("proEnableRule_pation","PKG_TAXADJUST.ENABLE_FC_RULE");
			sql = "{call "+proName+"(?,?,?)}"; 
			
		}else{
			String proName = cg.getString("proDisableRule_pation","PKG_TAXADJUST.DISABLE_FC_RULE");
			sql = "{call "+proName+"(?,?,?)}"; 
		}
		result = icmDao.toggleRule(sql,ruleid);
		json.append("1".equals(result[0])).append(",info:'").append(result[1]).append("'}");
		return json.toString();
	}
	@DirectMethod
	public Map getEnsToAddPRules(int start,int limit,String paraMc){
		Map infos = new HashMap();
		StringBuffer sql = new StringBuffer("select decode(objaffect,null,-1,1) as hasrule,a.xh,to_char(a.qynm)qynm,to_char(a.dzdah)dzdah,");
	    sql.append(" a.swdjzh,a.mc,a.dz,a.czfpbm czfpbm_bm,c.mc czfpbm from dj_cz a,(select distinct objaffect from rules where ruletype=1)b,");
	    sql.append(" (select bm,mc from bm_cont where table_bm='BM_CZFP')c where a.swdjzh=b.objaffect(+) and a.czfpbm=c.bm(+)");
	    if(paraMc!=null&&!"".equals(paraMc)){
	    	sql.append(" and a.mc like '%").append(paraMc).append("%'");
	    }
		int count = icmDao.queryCount(sql.toString());
		infos.put("totalCount", new Integer(count));
		List ens = icmDao.getEnsToAddPRules(sql.toString(),start,limit,EnHasRules.class);
		infos.put("rows", ens);
		return infos ;
	}
	@DirectMethod
	public String saveRuleDetails(String qynm,String dzdah,String swdjzh,String timeType,String stdate,String edate,String rules){
		StringBuffer results = new StringBuffer("{result:");
		List lstRules = parseRuleDeatail(rules);
		boolean done = icmDao.saveRuleDetails(qynm,dzdah,swdjzh,timeType,stdate,edate,lstRules);
		results.append(done).append("}");
		return results.toString();
	}
	
	private List parseRuleDeatail(String strRules){
		List rdts = null;
		if(strRules==null||"".equals(strRules)){
			return null;
		}
		try{
			JSONArray jarr = new JSONArray(strRules);
			if(jarr!=null&&jarr.length()>0){
				rdts = new ArrayList();
				for(int i=0;i<jarr.length();i++){
					JSONObject jobj = jarr.getJSONObject(i);
					RuleOfP rtk = new RuleOfP();
					rtk.setCzfpbm(jobj.getString("czfpbm"));
					rtk.setSzbm(jobj.getString("szbm"));
					rtk.setSebl(jobj.getDouble("sebl"));
					rdts.add(rtk);
				}
			}
		}catch(Exception e){
		}
		return rdts;
	}
}
