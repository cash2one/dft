package com.fruit.query.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.fruit.query.data.OptionItem;
import com.fruit.query.data.ParaValue;
import com.fruit.query.data.PortalInfo;
import com.fruit.query.parser.TemplatesLoader;
import com.fruit.query.report.Parameter;
import com.fruit.query.report.ParameterForFilter;
import com.fruit.query.report.Report;
import com.fruit.query.service.ParaDefaultOptionService;
import com.fruit.query.service.ParaOptionsService;
import com.fruit.query.service.PortalService;
import com.fruit.query.util.IParaDataBind;
import com.ifugle.dft.utils.Configuration;
import com.ifugle.dft.utils.ContextUtil;
import com.ifugle.dft.utils.entity.TreeNode;
import com.ifugle.dft.query.entity.StoreResult;
import com.ifugle.dft.system.entity.User;
import com.ifugle.dft.utils.entity.SubmitResult;
import com.softwarementors.extjs.djn.config.annotations.DirectFormPostMethod;
import com.softwarementors.extjs.djn.config.annotations.DirectMethod;
import com.softwarementors.extjs.djn.servlet.ssm.WebContext;
import com.softwarementors.extjs.djn.servlet.ssm.WebContextManager;

public class PortalHandler {
	private static Logger log = Logger.getLogger(PortalHandler.class);
	private PortalService psvr;
	private Configuration cg ;
	WebContext context ;
	public PortalHandler(){
		psvr = (PortalService)ContextUtil.getBean("psvr");
		cg = (Configuration)ContextUtil.getBean("config");
		if(cg!=null){
			cg.getHandlersMap().put("PortalHandler","com.fruit.query.handler.PortalHandler");
		}
	}
	public void setWebContext(WebContext ctx){
		if(context==null){
			context = ctx;
		}
	}
	@SuppressWarnings("unchecked")
	@DirectMethod
	public List getOptionItems(String rptID,String paramName,String tmpPostVals){
		List opts = null;
		Report rpt = TemplatesLoader.getTemplatesLoader().getReportTemplate(rptID);
		if(rpt==null){
			return opts;
		}
		Parameter para = findPara(rpt,paramName);
		WebContext context =WebContextManager.get();
		HttpServletRequest request = context.getRequest();
		Map paraVals = parseParamValues(rpt,request,tmpPostVals);
		try{
			opts = ParaOptionsService.getParaOptionsService().getOptions(rpt, para, paraVals);
		}catch(Exception e){
			log.error(e.toString());
		}
		return opts;
	}
	@DirectMethod
	public List getOptionItemsOfTree(String nodeid,String rptID,String paramName,String tmpPostVals){
		List nodes = null;
		Report rpt = TemplatesLoader.getTemplatesLoader().getReportTemplate(rptID);
		if(rpt==null){
			return nodes;
		}
		Parameter para = findPara(rpt,paramName);
		WebContext context =WebContextManager.get();
		HttpServletRequest request = context.getRequest();
		Map paraVals = parseParamValues(rpt,request,tmpPostVals);
		String pid = "";
		if(nodeid!=null&&!"-1".equals(nodeid)){
			pid = nodeid;
		}
		if(paraVals==null){
			paraVals = new HashMap();
		}
		//本系统中强制分层加载。为兼容以往版本，不改变ParaOptionsService中方法参数，将两个分层加载要素加在paraVals中
		paraVals.put("pid", pid);
		paraVals.put("loadByLevel", "1");
		try{
			List opts = ParaOptionsService.getParaOptionsService().getOptions(rpt, para, paraVals);
			if(opts !=null){
				nodes = new ArrayList();
				for(int i = 0;i<opts.size();i++){
					TreeNode tnode = new TreeNode();
					OptionItem opt = (OptionItem)opts.get(i);
					tnode.setId(opt.getBm());
					tnode.setText(opt.getName());
					tnode.setLeaf(opt.getIsleaf()>0);
					tnode.setPid(opt.getPid());
					nodes.add(tnode);
				}
			}
		}catch(Exception e){
			log.error(e.toString());
		}
		
		return nodes;
	}
	private Parameter findPara(Report rpt,String paramName){
		Map params = rpt.getParasMap();
		Map paramsFlt = rpt.getParas4FilterMap();
		Parameter para = null;
		if(params!=null&&params.containsKey(paramName)){
			para = (Parameter)params.get(paramName);
		}else if(paramsFlt!=null&&paramsFlt.containsKey(paramName)){
			para = (Parameter)paramsFlt.get(paramName);
		}
		return para;
	}
	@SuppressWarnings("unchecked")
	@DirectMethod
	public StoreResult queryGeneralDataDynamic(String rptID,int start,int limit,String params){
		Report rpt = TemplatesLoader.getTemplatesLoader().getReportTemplate(rptID);
		if(rpt==null){
			return null;
		}
		WebContext context =WebContextManager.get();
		HttpServletRequest request = context.getRequest();
		JSONObject jparams = null;
		try{
			jparams = new JSONObject(params);
		}catch(Exception e){
		}
		//元数据加载（列，字段等信息）
		boolean metaDataLoaded = false;
		try{
			if(jparams!=null&&jparams.has("metaDataLoaded")&&jparams.getBoolean("metaDataLoaded")){
				metaDataLoaded = true;
			}
		}catch(Exception e){
		}
		Map paraVals = parseParamValues(rpt,request,params);
		//根据传递过来的参数，来包装sql
		String replaceSql = buildSqlByFilters(rpt,params,paraVals);
		log.info(replaceSql);
		StoreResult storeResult = new StoreResult();
		//添加分页参数
		paraVals.put("start", new ParaValue(String.valueOf(start),String.valueOf(start)));
		paraVals.put("limit", new ParaValue(String.valueOf(limit),String.valueOf(limit)));
		try{
			if(rpt.getRemoteSort()==1){
				if(jparams.has("sort")){
					paraVals.put("sort", new ParaValue(jparams.getString("sort"),jparams.getString("sort")));
				}
				if(jparams.has("dir")){
					paraVals.put("dir", new ParaValue(jparams.getString("dir"),jparams.getString("dir")));
				}
			}
		}catch(Exception e){
		}
		//获取数据集合
		psvr.queryGeneralDataDynamic(storeResult, rpt,paraVals,replaceSql);
		
		if (!metaDataLoaded) {
			Map<String, Object> storeMetaData = psvr.buildStoreMetaData(rpt,paraVals);
			storeResult.setMetaData(storeMetaData);
		}
		storeResult.setSuccess(true);
		return storeResult;
	}
	private String buildSqlByFilters(Report rpt, String params,Map paVals) {
		String sql = null;
		if(params==null||"".equals(params)){
		}
		JSONObject jttParams = null;
		if(params!=null){
			try{
				jttParams = new JSONObject(params);
			}catch(Exception e){
			}
		}
		if(jttParams==null){
			return null;
		}
		JSONObject jflts = null;
		try{
			jflts = jttParams.getJSONObject("filter");
		}catch(Exception e){
		}
		if(jflts == null){
			//没有外部传入参数时，检查默认参数并组织
			List pflts = rpt.getParasForFilter();
			if(pflts==null||pflts.size()==0){
				return null;
			}
			try{
				List fldNames = new ArrayList();
				List relations = new ArrayList();
				List fldValues = new ArrayList();
				List connections = new ArrayList();
				for(int i=0;i<pflts.size();i++){
					ParameterForFilter tmpPa = (ParameterForFilter)pflts.get(i);
					if(tmpPa.getDefaultValue()!=null&&!"".equals(tmpPa.getDefaultValue())){
						fldNames.add(tmpPa.getFilterFld());
						String rltn =StringUtils.isEmpty(tmpPa.getValueOprator())?"equ":tmpPa.getValueOprator();
						relations.add(rltn);
						fldValues.add(tmpPa.getDefaultValue());
						connections.add("_and");
					}else if(tmpPa.getDefaultRule()!=null&&!"".equals(tmpPa.getDefaultRule())){
						String rule = tmpPa.getDefaultRule();
						OptionItem op = null;
						try{
							List opts = ParaOptionsService.getParaOptionsService().getOptions(rpt, tmpPa, paVals);
							if("_first".equals(rule)){
								op = opts==null||opts.size()==0?null:(OptionItem)opts.get(0);
							}else if("_last".equals(rule)){
								op = opts==null||opts.size()==0?null:(OptionItem)opts.get(opts.size()-1);
							}else if(rule.startsWith("NO_")){
								String sidx = rule.substring(3);
								int idx = 0;
								try{
									idx = Integer.parseInt(sidx);
								}catch(Exception e){
								}
								op = opts==null||opts.size()==0?null:(OptionItem)opts.get(idx>opts.size()?opts.size()-1:idx-1);
							}
							fldNames.add(tmpPa.getFilterFld());
							String rltn =StringUtils.isEmpty(tmpPa.getValueOprator())?"equ":tmpPa.getValueOprator();
							relations.add(rltn);
							fldValues.add(op.getBm());
							connections.add("_and");
						}catch(Exception e){
						}
					}
				}
				if(connections.size()>0){
					jflts = new JSONObject();
					connections.set(connections.size()-1, "empty");
					jflts.put("fldNames", StringUtils.join(fldNames, ","));
					jflts.put("relations", StringUtils.join(relations, ","));
					jflts.put("fldValues", StringUtils.join(fldValues, ","));
					jflts.put("connections", StringUtils.join(connections, ","));
				}
			}catch(Exception e){
				log.error("组织默认的筛选参数值时发生错误："+e.toString());
			}
		}
		if(jflts!=null){
			sql =  rpt.getDefaultDataDef().getSql();
			sql = rebuildSql(sql,jflts);
		}
		return sql;
	}
	
	private String rebuildSql(String oSql,JSONObject jcdts){
		if(jcdts==null){
			return oSql;
		}
		String[] flds=null,ops = null,vals = null,rltns = null;
		StringBuffer sql = new StringBuffer("select * from(");
		sql.append(oSql).append(")");
		try{
			if(jcdts.has("fldNames")){
				String paraFlds = jcdts.getString("fldNames");
				flds = "".equals(paraFlds)?null:paraFlds.split(",");
			}
			if(jcdts.has("relations")){
				String paraOps = jcdts.getString("relations");
				ops = "".equals(paraOps)?null:paraOps.split(",");
			}
			if(jcdts.has("fldValues")){
				String paraVals = jcdts.getString("fldValues");
				vals = "".equals(paraVals)?null:paraVals.split(",");
			}
			if(jcdts.has("connections")){
				String paraRltns = jcdts.getString("connections");
				rltns = "".equals(paraRltns)?null:paraRltns.split(",");
			}
			if(flds==null||ops==null||vals==null||rltns==null){
				return oSql;
			}else{
				String sVal="";
				String sFld="";
				//如果有筛选条件：
				sql.append(" where (");
				String[] tOps=transOpsSymbol(ops);
				String[] tRltns=transRltnsSymbol(rltns);
				for (int i=0;i<flds.length;i++){
					sVal=vals[i]==null?"":vals[i].replace("'", "''");
					sFld=flds[i]==null?"":flds[i].toUpperCase();
					if("IN".equals(tOps[i].toUpperCase())){
						sVal=sVal.replace(",", "','");
						sVal=" ('"+sVal+"') ";
					}else if("LIKE".equals(tOps[i].toUpperCase())){
						sVal=" '%"+sVal+"%' ";
					}else{
						sVal=" '"+sVal+"' ";
					}
					sql.append(flds[i]).append(" ").append(tOps[i]).append(" ");
					sql.append(sVal).append(" ").append(tRltns[i]).append(" ");
				}
				sql.append(")");
			}
		}catch(Exception e){
			log.error(e.toString());
		}
		return sql.toString();
	}
	/**
	 * 将请求中的操作符参数转化为实际的参数
	 * @param ops
	 * @return
	 */
	private String[] transOpsSymbol(String[] ops){
		String[] sTrans=null;
		if (ops==null) return null;
		sTrans=new String[ops.length];
		for(int i=0;i<ops.length;i++){
			if("equ".equals(ops[i]))sTrans[i]="=";
			if("gt".equals(ops[i]))sTrans[i]=">";
			if("lt".equals(ops[i]))sTrans[i]="<";
			if("gt_e".equals(ops[i]))sTrans[i]=">=";
			if("lt_e".equals(ops[i]))sTrans[i]="<=";
			if("not_e".equals(ops[i]))sTrans[i]="<>";
			if("like".equals(ops[i]))sTrans[i]="LIKE";
			if("in".equals(ops[i]))sTrans[i]="IN";	
		}
		return sTrans;
	}
	/**
	 * 将请求中的关系符参数转化为实际的参数
	 * @param rltns
	 * @return
	 */
	private String[] transRltnsSymbol(String[] rltns){
		String[] sTrans=null;
		if (rltns==null) return null;
		sTrans=new String[rltns.length];
		for(int i=0;i<rltns.length;i++){
			if("empty".equals(rltns[i]))sTrans[i]="";
			if("_and".equals(rltns[i]))sTrans[i]="AND";
			if("_or".equals(rltns[i]))sTrans[i]="OR";
			if("andL".equals(rltns[i]))sTrans[i]="AND (";
			if("orL".equals(rltns[i]))sTrans[i]="OR (";
			if("Rand".equals(rltns[i]))sTrans[i]=") AND";
			if("Ror".equals(rltns[i]))sTrans[i]=") OR";
			if("RandL".equals(rltns[i]))sTrans[i]=") AND (";	
			if("RorL".equals(rltns[i]))sTrans[i]=") OR (";
			if("RBr".equals(rltns[i]))sTrans[i]=")";	
		}
		return sTrans;
	}
	@SuppressWarnings("unchecked")
	private Map parseParamValues(Report rpt,HttpServletRequest request,String tmpPostVals){
		Map paVals=new HashMap();
		List allParams = new ArrayList();
		List defaultParams = new ArrayList();//默认值依赖其他参数值而变的，在后期处理，确定其默认值。
		try{
			if(rpt.getParas()!=null&&rpt.getParas().size()>0){
				allParams.addAll(rpt.getParas());
			}
			if(rpt.getParasForFilter()!=null&&rpt.getParasForFilter().size()>0){
				allParams.addAll(rpt.getParasForFilter());
			}
		}catch(Exception e){
		}
		for(int k=0;k<allParams.size();k++){
			Parameter tmpPa=(Parameter)allParams.get(k);
			if(tmpPa.getIsHidden()==1){
				String sDesc="",sVal="";
				if(tmpPa.getBindMode()==0){
					sDesc=sVal=tmpPa.getBindTo();
				}else if(tmpPa.getBindMode()==1){
					sVal=request.getParameter(tmpPa.getName());
					sDesc=request.getParameter(tmpPa.getName()+"_desc");
				}else if(tmpPa.getBindMode()==2){
					sDesc=sVal=(String)request.getSession().getAttribute(tmpPa.getBindTo());
				}else{
					String path=tmpPa.getBindTo();
					try{
						IParaDataBind pdGetInstance=(IParaDataBind)Class.forName(path).newInstance();
						ParaValue tpv=pdGetInstance.getParaValue(request, rpt, tmpPa);
						if(tpv!=null){
							sVal=tpv.getValue();
							sDesc=tpv.getDesc();
						}
					}catch(Exception e){
						System.out.println("未能正确加载报表取值类!错误信息:"+path+e.toString());
					}
				}
				ParaValue pv=new ParaValue(sVal,sDesc);
				paVals.put(tmpPa.getName(),pv);
			}else if(tmpPa.getDefaultValue()!=null&&!"".equals(tmpPa.getDefaultValue())){
				ParaValue pv=new ParaValue(tmpPa.getDefaultValue(),tmpPa.getDefaultValue());
				paVals.put(tmpPa.getName(),pv);
			}else if(!StringUtils.isEmpty(tmpPa.getDefaultRule())){
				String rule = tmpPa.getDefaultRule();
				if("_first".equals(rule)){
					try{
						List opts = ParaOptionsService.getParaOptionsService().getOptions(rpt, tmpPa, paVals);
						OptionItem op = opts==null||opts.size()==0?null:(OptionItem)opts.get(0);
						ParaValue pv=new ParaValue(op==null?"":op.getBm(),op==null?"":op.getName());
						paVals.put(tmpPa.getName(),pv);
					}catch(Exception e){
					}
				}else if("_last".equals(rule)){
					try{
						List opts = ParaOptionsService.getParaOptionsService().getOptions(rpt, tmpPa, paVals);
						OptionItem op = opts==null||opts.size()==0?null:(OptionItem)opts.get(opts.size()-1);
						ParaValue pv=new ParaValue(op==null?"":op.getBm(),op==null?"":op.getName());
						paVals.put(tmpPa.getName(),pv);
					}catch(Exception e){
					}
				}else if(rule.startsWith("NO_")){
					try{
						List opts = ParaOptionsService.getParaOptionsService().getOptions(rpt, tmpPa, paVals);
						String sidx = rule.substring(3);
						int i = 0;
						try{
							i = Integer.parseInt(sidx);
						}catch(Exception e){
						}
						OptionItem op = opts==null||opts.size()==0?null:(OptionItem)opts.get(i>opts.size()?opts.size()-1:i-1);
						ParaValue pv=new ParaValue(op==null?"":op.getBm(),op==null?"":op.getName());
						paVals.put(tmpPa.getName(),pv);
					}catch(Exception e){
					}
				}
			}else if(tmpPa.getDefaultRuleDefine()!=null){
				defaultParams.add(tmpPa);
			}
		}
		//通过交互传递的参数值。如果与之前处理的参数值同（前面处理过默认值），会被本次传递值覆盖。
		JSONObject jttParams = null;
		JSONObject jparams = null;
		if(tmpPostVals!=null){
			try{
				jttParams = new JSONObject(tmpPostVals);
			}catch(Exception e){
			}
		}
		if(jttParams!=null&&jttParams.has("macroParams")){
			try{
				jparams = jttParams.getJSONObject("macroParams");
			}catch(Exception e){
			}
		}
		//对外部传入“参数名-值”对进行解析，也放入可用参数值Map中
		if(jparams!=null){
			Iterator keys = jparams.keys();
			Map paraMap = rpt.getParasMap();
			while(keys.hasNext()){
				String pname = (String)keys.next();
				Parameter p = null;
				if(paraMap!=null&&paraMap.containsKey(pname)){
					p=(Parameter)paraMap.get(pname);
				}
				if(p==null){
					continue;
				}
		        String val = null;
		        if(p.getDataType()==2){
		        	try{
		        		Double dval = jparams.getDouble(pname);
				        val = dval.toString();
				    }catch(Exception e){
				    }
		        }else if(p.getDataType()==1){
		        	try{
			           Integer ival = jparams.getInt(pname);
			           val = ival.toString();
			        }catch(Exception e){
			        }
		        }else{
		        	try{
			             val = jparams.getString(pname);
			        }catch(Exception e){
			        }
		        }
				ParaValue pv=new ParaValue(val,val);
				paVals.put(pname,pv);
			}
		}
		if(defaultParams!=null&&defaultParams.size()>0){
			for(int i=0;i<defaultParams.size();i++){
				Parameter p = (Parameter)defaultParams.get(i);
				//如果已经取得了该参数的值，不处理默认值。
				if(paVals.containsKey(p.getName())){
					continue;
				}
				try{
					OptionItem op = ParaDefaultOptionService.getParaDefaultOptionService().getParaDefaultOption(rpt,p,paVals);
					if(op!=null){
						ParaValue pv=new ParaValue(op.getBm(),op.getName());
						paVals.put(p.getName(),pv);
					}
				}catch(Exception e){
				}
			}
		}
		return paVals;
	}
	@DirectMethod
	public List getUnits(String rptID){
		List units = null;
		Report rpt = TemplatesLoader.getTemplatesLoader().getReportTemplate(rptID);
		if(rpt==null){
			return units;
		}
		try{
			units = psvr.getUnits(rpt);
		}catch(Exception e){
			log.error(e.toString());
		}
		return units;
	}
	@DirectMethod
	public List getPortals(){
		List portals = null;
		try{
			List jpts = psvr.getPortals();
			if(jpts!=null){
				portals = new ArrayList();
				for(int i=0;i<jpts.size();i++){
					JSONObject jp = (JSONObject)jpts.get(i);
					PortalInfo tp = new PortalInfo();
					tp.setId(jp.getString("id"));
					tp.setName(jp.getString("name"));
					tp.setRemark(jp.has("remark")?jp.getString("remark"):"");
					int tt = 0,cc = 0;
					try{
						String stt = jp.has("total")?jp.getString("total"):"0";
						tt = Integer.parseInt(stt);
					}catch(Exception e){
					}
					try{
						String scc = jp.has("colCount")?jp.getString("colCount"):"1";
					}catch(Exception e){
					}
					tp.setTotal(tt);
					tp.setColCount(cc);
					portals.add(tp);
				}
			}
		}catch(Exception e){
			log.error(e.toString());
		}
		return portals;
	}
	@DirectMethod
	public String deletePortalDesign(String pid){
		StringBuffer json = new StringBuffer("{result:");
		boolean done = psvr.deletePortaDesign(pid);
		json.append(done).append("}");
		return json.toString();
	}
	@DirectFormPostMethod
	public SubmitResult savePortal(Map params,Map fileFields){
		SubmitResult result = new SubmitResult();
		Map errors = new HashMap();	
		String pid = (String)params.get("portalid");
		try{
			boolean done =psvr.savePortal(params, pid);
			result.setSuccess(done);
			Map infos = new HashMap();
			infos.put("msg", "保存portal配置信息成功！");
			result.setInfos(infos);
		}catch(Throwable e){
			result.setSuccess(false);
			errors.put("msg", "保存portal配置信息时发生错误："+e.toString());
		}
		return result;
	}
	@DirectMethod
	public String checkPortalid(String id){
		StringBuffer result = new StringBuffer("{duplicate:");
		boolean duplicate =  psvr.checkPortalid(id);
		result.append(duplicate).append("}");
		return result.toString();
	}
	@DirectMethod
	public String getPortalDesign(String id){
		JSONObject info = psvr.getPortalDesign(id);
		return info==null?"{}":info.toString();
	}
}
