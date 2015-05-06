package com.ifugle.dft.query.handler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.fruit.query.data.OptionItem;
import com.fruit.query.data.ParaValue;
import com.fruit.query.parser.TemplatesLoader;
import com.fruit.query.report.Parameter;
import com.fruit.query.report.ParameterForFilter;
import com.fruit.query.report.Report;
import com.fruit.query.service.ParaDefaultOptionService;
import com.fruit.query.service.ParaOptionsService;
import com.fruit.query.util.IParaDataBind;
import com.ifugle.dft.query.dao.QueryDao;
import com.ifugle.dft.query.entity.QueryPlan;
import com.ifugle.dft.query.entity.StoreResult;
import com.ifugle.dft.system.entity.User;
import com.ifugle.dft.utils.Configuration;
import com.ifugle.dft.utils.ContextUtil;
import com.ifugle.dft.utils.entity.TreeNode;
import com.softwarementors.extjs.djn.config.annotations.DirectMethod;
import com.softwarementors.extjs.djn.servlet.ssm.WebContext;
import com.softwarementors.extjs.djn.servlet.ssm.WebContextManager;

public class QueryHandler {
	private static Logger log = Logger.getLogger(QueryHandler.class);
	WebContext context ;
	private Configuration cg ;
	private QueryDao qryDao;
	@SuppressWarnings("unchecked")
	public QueryHandler(){
		qryDao = (QueryDao)ContextUtil.getBean("qryDao");
		cg = (Configuration)ContextUtil.getBean("config");
		if(cg!=null){
			cg.getHandlersMap().put("QueryHandler","com.ifugle.dft.query.handler.QueryHandler");
		}
	}
	public void setWebContext(WebContext ctx){
		if(context==null){
			context = ctx;
		}
	}
	
	/**
	 * 
	* @param rptID 报表ID
	* @param paramName 要获取选项的参数名
	* @param tmpPostVals json格式的串。如果是联动参数，取数受其他参数影响，此处的json串传递“主动”参数及值
	* @return
	 */
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
		//本系统中sql取数的方式，则强制分层加载。为兼容以往版本，不改变ParaOptionsService中方法参数，将两个分层加载要素加在paraVals中
		boolean loadByLevel = false;
		if(para.getSourceType()==1){
			paraVals.put("pid", pid);
			paraVals.put("loadByLevel", "1");
			loadByLevel = true;
		}
		try{
			List opts = ParaOptionsService.getParaOptionsService().getOptions(rpt, para, paraVals);
			if(opts !=null){
				if(para.getIsMulti()==0){
					int defaultIndex=-1;
					if(opts!=null){
						for(int j=0;j<opts.size();j++){
							OptionItem oi=(OptionItem)opts.get(j);
							if(oi.getIsDefault()>0){
								defaultIndex=j;
							}
							oi.setIsDefault(0);
						}
					}
					if(defaultIndex>=0){
						((OptionItem)opts.get(defaultIndex)).setIsDefault(1);
					}
				}
				nodes = new ArrayList();
				if(loadByLevel){
					for(int i = 0;i<opts.size();i++){
						TreeNode tnode = new TreeNode();
						OptionItem opt = (OptionItem)opts.get(i);
						tnode.setId(opt.getBm());
						tnode.setText(opt.getName());
						tnode.setLeaf(opt.getIsleaf()>0);
						tnode.setPid(opt.getPid());
						nodes.add(tnode);
					}
				}else{//一次取出所有节点的，递归整理好树层次结构
					for(int i = 0;i<opts.size();i++){
						OptionItem oi=(OptionItem)opts.get(i);
						if("".equals(oi.getPid())){
							nodes.add(parseOptionItem(opts,oi));
						}
					}
				}
			}
		}catch(Exception e){
			log.error(e.toString());
		}
		return nodes;
	}
	private TreeNode parseOptionItem(List initItems,OptionItem oi)throws Exception{
		if(oi==null)return null;
		TreeNode ji=new TreeNode();
		ji.setId(oi.getBm());
		ji.setText(oi.getName());
		ji.setLeaf(oi.getIsleaf()>0);
		ji.setPid(oi.getPid());
		ji.setCls(oi.getIsleaf()>0?"file":"folder");	
		//ji.setChecked(oi.getIsDefault()>0?true:false);
		//检查当前节点的子节点
		String nextPid=oi.getBm();
		ArrayList cArray=new ArrayList();
		for(int i=0;i<initItems.size();i++){
			OptionItem item=(OptionItem)initItems.get(i);
			if(item!=null&&nextPid.equals(item.getPid())){
				cArray.add(parseOptionItem(initItems,item));
			}
		}
		if(cArray.size()>0){
			ji.setLeaf(false);
			ji.setCls("folder");
			ji.setChildren(cArray);
		}else{
			ji.setChildren(cArray);
		}
		
		return ji;
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
	/**
	 * 参数选项取值时，可用的参数值的解析。来自两部分：
	 * 1、不需要交互的隐藏参数，一般已经具备了值；
	 * 2、联动参数模式中，引起联动的“主”参数的值。这部分的“参数-值”对，由页面负责组织传递
	* @param rpt
	* @param request
	* @param tmpPostVals
	* @return
	 */
	@SuppressWarnings("unchecked")
	private Map parseParamValues(Report rpt,HttpServletRequest request,String tmpPostVals){
		Map paVals=new HashMap();
		List allParams = new ArrayList();
		List defaultParams = new ArrayList();//默认值依赖其他参数值而变的，在后期处理，确定其默认值。
		try{
			allParams.addAll(rpt.getParas());
			allParams.addAll(rpt.getParasForFilter());
		}catch(Exception e){
		}
		for(int k=0;k<allParams.size();k++){
			Parameter tmpPa=(Parameter)allParams.get(k);
			if(tmpPa.getIsHidden()==1){
				String sDesc="",sVal="";
				if(tmpPa.getBindMode()==0){
					sDesc=sVal=tmpPa.getBindTo();
				}else if(tmpPa.getBindMode()==1){
					sVal=request.getParameter(tmpPa.getBindTo());
					sDesc=request.getParameter(tmpPa.getBindTo()+"_desc");
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
			}else if(tmpPa.getDefaultRule()!=null&&!"".equals(tmpPa.getDefaultRule())){
				String rule = tmpPa.getDefaultRule();
				if("_first".equals(rule)){
					try{
						List opts = ParaOptionsService.getParaOptionsService().getOptions(rpt, tmpPa, paVals);
						OptionItem op = opts==null||opts.size()==0?null:(OptionItem)opts.get(0);
						ParaValue pv=new ParaValue(op==null?"":op.getBm(),op==null?"":op.getName());
						paVals.put(tmpPa.getName(),pv);
					}catch(Exception e){
					}
				}else{
					defaultParams.add(tmpPa);
				}
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
		        String val = "";
		        String textVal = "";
		       /* if(p.getDataType()==2){
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
		        }*/
		        
		        
		        try{
		             val = jparams.getString(pname);
		        }catch(Exception e){
		        }
		        
		        
		        try{
		        	textVal = jparams.getString(pname+"_desc");
		        }catch(Exception e){
		        }
				ParaValue pv=new ParaValue(val,textVal);
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
	public List getFieldsOfComplexFilter(String rptID){
		List flds = null;
		Report rpt = TemplatesLoader.getTemplatesLoader().getReportTemplate(rptID);
		if(rpt==null){
			return flds;
		}
		List fparams = rpt.getParasForFilter();
		if(fparams==null){
			return flds;
		}
		flds = new ArrayList();
		for(int i=0;i<fparams.size();i++){
			ParameterForFilter pa = (ParameterForFilter)fparams.get(i);
			if(pa.getShowMode()==2){
				continue;
			}
			flds.add(pa);
		}
		return flds;
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
		User user = (User)request.getSession().getAttribute("user");
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
		boolean loadDefaultMeata = false;
		try{
			if(jparams!=null&&jparams.has("loadDefaultMeata")&&jparams.getBoolean("loadDefaultMeata")){
				loadDefaultMeata = true;
			}
		}catch(Exception e){
		}
		boolean loadUserMeata = true;
		try{
			if(jparams!=null&&jparams.has("loadUserMeata")&&jparams.getBoolean("loadUserMeata")){
				loadUserMeata = jparams.getBoolean("loadUserMeata");
			}
		}catch(Exception e){
		}
		Map paraVals = null;
		JSONObject jttParams = null,jflts = null;
		try{
			jttParams = new JSONObject(params);
			jflts = jttParams.getJSONObject("filter");
		}catch(Exception e){
		}
		QueryPlan sQueryplan =new QueryPlan();
		JSONObject jQueryplan = null;
		//如果是加载查询方案，通过另外的方式解析参数
		if(!metaDataLoaded&&loadUserMeata){
			int qpid = -1;
			try{
				if(jparams!=null&&jparams.has("QPid")){
					qpid = jparams.getInt("QPid");
				}
			}catch(Exception e){
			}
			sQueryplan.setId(qpid);
			String qpParams = "";
			sQueryplan = qryDao.getQueryPlan(qpid,rptID,user.getUserid());
			try{
				jQueryplan = new JSONObject(sQueryplan==null?"":sQueryplan.getQpcontent());
			}catch(Exception e){}
			boolean noPlan = jQueryplan==null;
			//如果最终还是没有方案，还是采用默认的方式解析参数（相当于loadUserMeata=false）
			if(noPlan){
				paraVals = parseParamValues(rpt,request,params);
			}else{
				try{
					qpParams =jQueryplan.getString("condition");
					JSONObject jcondition = new JSONObject(qpParams);
					jflts = jcondition.getJSONObject("filter");
				}catch(Exception e){
					log.error(e.toString());
				}
				paraVals = parseParamValues(rpt,request,qpParams);
			}
		}else{
			paraVals = parseParamValues(rpt,request,params);
		}
		request.getSession().setAttribute("paraVals",paraVals);
		String replaceSql = buildSqlByFilters(rpt,jflts,paraVals);
		log.info(replaceSql);
		StoreResult storeResult = new StoreResult();
		//添加分页参数
		paraVals.put("start", new ParaValue(String.valueOf(start),String.valueOf(start)));
		paraVals.put("limit", new ParaValue(String.valueOf(limit),String.valueOf(limit)));
		//添加单位参数
		try{
			paraVals.put("unit", new ParaValue(jparams.getString("moneyUnit"),jparams.getString("moneyUnit")));
		}catch(Exception e){
		}
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
		qryDao.queryGeneralDataDynamic(storeResult, rpt, paraVals,replaceSql);
		if (!metaDataLoaded) {
			Map<String, Object> storeMetaData = qryDao.buildStoreMetaData(rpt,user,paraVals,loadDefaultMeata,loadUserMeata,sQueryplan);
			storeResult.setMetaData(storeMetaData);
		}
		try{
			String titleExp = rpt.getTitle()==null?"":rpt.getTitle().getTitleExp();
			String title = qryDao.parseParaExp(titleExp,rpt, paraVals);
			storeResult.setTitle(title);
		}catch(Exception e){}
		if(rpt.getHead()!=null&&rpt.getHead().getSubTitle()!=null){
			try{
				String stLeftExp = rpt.getHead().getSubTitle().getLeftExp();
				String stLeft = qryDao.parseParaExp(stLeftExp,rpt, paraVals);
				storeResult.setSubTitleLeft(stLeft);
			}catch(Exception e){}
			try{
				String stCenterExp = rpt.getHead().getSubTitle().getCenterExp();
				String stCenter =qryDao.parseParaExp(stCenterExp,rpt, paraVals);
				storeResult.setSubTitleCenter(stCenter);
			}catch(Exception e){}
			try{
				String stRightExp = rpt.getHead().getSubTitle().getRightExp();
				String stRight =qryDao.parseParaExp(stRightExp,rpt, paraVals);
				storeResult.setSubTitleRight(stRight);
			}catch(Exception e){}
		}
		if(rpt.getFoot()!=null){
			try{
				String fLeftExp = rpt.getFoot().getLeftExp();
				String fLeft =qryDao.parseParaExp(fLeftExp,rpt, paraVals);
				storeResult.setFootLeft(fLeft);
			}catch(Exception e){}
			try{
				String fCenterExp = rpt.getFoot().getCenterExp();
				String fCenter =qryDao.parseParaExp(fCenterExp,rpt, paraVals);
				storeResult.setFootCenter(fCenter);
			}catch(Exception e){}
			try{
				String fRightExp = rpt.getFoot().getRightExp();
				String fRight =qryDao.parseParaExp(fRightExp,rpt, paraVals);
				storeResult.setFootRight(fRight);
			}catch(Exception e){}
		}
		return storeResult;
	}
	
	private String buildSqlByFilters(Report rpt, JSONObject jflts,Map paVals) {
		String sql = null;
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
						if("_first".equals(rule)){
							try{
								List opts = ParaOptionsService.getParaOptionsService().getOptions(rpt, tmpPa, paVals);
								OptionItem op = opts==null||opts.size()==0?null:(OptionItem)opts.get(0);
								fldNames.add(tmpPa.getFilterFld());
								String rltn =StringUtils.isEmpty(tmpPa.getValueOprator())?"equ":tmpPa.getValueOprator();
								relations.add(rltn);
								fldValues.add(op.getBm());
								connections.add("_and");
							}catch(Exception e){
							}
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
						sVal=sVal.replace("|", "','");
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
	@DirectMethod
	public List getQueryPlans(String rptID){
		List qps = null;
		if(rptID==null||"".equals(rptID)){
			return qps;
		}
		WebContext context = WebContextManager.get();
		HttpServletRequest request = context.getRequest();
		String cUser = (String)request.getSession().getAttribute("userid");
		try{
			qps = qryDao.getQueryPlans(rptID,cUser);
		}catch(Exception e){
			log.error(e.toString());
		}
		return qps;		
	}
	@DirectMethod
	public String saveQueryPlan(int qpid,boolean onlyClob,String rptID,String qpname,String remark,int isdefault,String cfg){
		StringBuffer result = new StringBuffer("{result:");
		if(rptID==null||"".equals(rptID)||cfg==null||"".equals(cfg)){
			result.append("'0'}");
			return result.toString();
		}
		WebContext context = WebContextManager.get();
		HttpServletRequest request = context.getRequest();
		String cUser = (String)request.getSession().getAttribute("userid");
		long lqpid = qpid;
		if(qpid<0){
			lqpid = qryDao.addQueryPlan(rptID,qpname,remark,isdefault,cfg,cUser);
		}else{
			qryDao.saveQueryPlan(qpid,rptID,qpname,remark,isdefault,cfg,onlyClob);
		}
		result.append(lqpid).append("}");
		return result.toString();
	}
	@DirectMethod
	public String deleteQueryPlan(int qpid){
		StringBuffer result = new StringBuffer("{result:");
		boolean done = false;
		done = qryDao.deleteQueryPlan(qpid);
		result.append(done).append("}");
		return result.toString();
	}
	@DirectMethod
	public List getUnits(String rptID){
		List units = null;
		Report rpt = TemplatesLoader.getTemplatesLoader().getReportTemplate(rptID);
		if(rpt==null){
			return units;
		}
		try{
			units = qryDao.getUnits(rpt);
		}catch(Exception e){
			log.error(e.toString());
		}
		return units;
	}
}
