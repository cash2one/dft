package com.ifugle.dft.query.dao;

import java.io.FileReader;
import java.io.Reader;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.ParameterizedBeanPropertyRowMapper;
import org.springframework.jdbc.core.support.AbstractLobCreatingPreparedStatementCallback;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobCreator;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.transaction.annotation.Transactional;

import com.fruit.query.data.Column;
import com.fruit.query.data.DataSet;
import com.fruit.query.data.LinkTab;
import com.fruit.query.data.OptionItem;
import com.fruit.query.data.ParaProcess;
import com.fruit.query.data.ParaValue;
import com.fruit.query.data.Row;
import com.fruit.query.data.RptMultiHeader;
import com.fruit.query.data.Unit;
import com.fruit.query.parser.TemplatesLoader;
import com.fruit.query.report.ColumnDefine;
import com.fruit.query.report.Parameter;
import com.fruit.query.report.DataDefine;
import com.fruit.query.report.ParameterForFilter;
import com.fruit.query.report.Report;
import com.fruit.query.report.Title;
import com.fruit.query.service.ColumnsService;
import com.fruit.query.service.ParaProcessService;
import com.fruit.query.service.RptDataService;
import com.ifugle.dft.dao.BaseDao;
import com.ifugle.dft.income.dao.IncomeDao;
import com.ifugle.dft.query.entity.QueryPlan;
import com.ifugle.dft.query.entity.StoreResult;
import com.ifugle.dft.system.entity.Code;
import com.ifugle.dft.system.entity.User;

@Transactional
public class QueryDao extends BaseDao{
	private static Logger log = Logger.getLogger(QueryDao.class);
	@Autowired
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate){
		this.jdbcTemplate = jdbcTemplate;
	}
	/**
	 * 根据定义获取数据集
	* @param storeResult
	* @param rpt
	* @param paraVals
	 */
	public void queryGeneralDataDynamic(StoreResult storeResult,Report rpt,Map paraVals,String replaceSql){
		//记录条数
		int count = 0;
		//本页记录
		List<Map<String, Object>> results  = null;
		int start = 0;
		int limit = 0;
		try{
			ParaValue sp = (ParaValue)paraVals.get("start");
			start = Integer.parseInt(sp.getValue());
		}catch(Exception e){
		}
		try{
			ParaValue lp = (ParaValue)paraVals.get("limit");
			limit = Integer.parseInt(lp.getValue());
		}catch(Exception e){
		}
		DataDefine df = rpt.getDefaultDataDef();
		try{
			DataSet ds=null;
			try{
			if(replaceSql!=null&&!"".equals(replaceSql)){
				if(rpt.getDefaultDataDef().getCanPaging()==1){
					ds = RptDataService.getReportDataService().getReportDataPaging(rpt, paraVals,replaceSql, start, limit);
				}else{
					ds = RptDataService.getReportDataService().getReportData(rpt, paraVals, replaceSql);
				}
			}else{
				if(rpt.getDefaultDataDef().getCanPaging()==1){
					ds = RptDataService.getReportDataService().getReportDataPaging(rpt, paraVals, start, limit);
				}else{
					ds = RptDataService.getReportDataService().getReportData(rpt, paraVals);
				}
			}
			}catch(Exception e){
			}
			if(ds!=null&&ds.getRows().size()>0){
				results = new ArrayList<Map<String, Object>>();
				for(int i=0;i<ds.getRows().size();i++){
					Row row=(Row)ds.getRows().get(i);
					Map cells = row.getCells();
					cells.put("autoIndex", start + i + 1);
					results.add(cells);
				}
				if(df.getSourceType()==2){
					count=ds.getTotalCount();
				}else {
					if(replaceSql!=null&&!"".equals(replaceSql)){
						count=RptDataService.getReportDataService().getTotalCount(rpt, paraVals,replaceSql);
					}else{
						count=RptDataService.getReportDataService().getTotalCount(rpt, paraVals);
					}
				}
			}else{
				count=0;
				results = new ArrayList<Map<String, Object>>();
			}
		}catch(Exception e){
			log.error(e.toString());
		}
		storeResult.setRecords(results);
		storeResult.setTotal(new Long(count));
	}
	/**
	 * 根据模板定义组织表头等元数据
	* @param rpt
	* @param paraVals
	* @return
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> buildStoreMetaData(Report rpt,User user,Map paraVals,boolean loadDefaultMeata,boolean loadUserMeata,QueryPlan sQueryplan) {
		Map storeMetaData = new HashMap();
		List columns = new ArrayList();
		List fields = new ArrayList();
		List ttbars = new ArrayList();
		List paramsInForm = new ArrayList();
		String filters = "";
		ColumnDefine cd = rpt.getColumnDef();
		//获取报表的列定义
		List colNodes = null;
		try{
			colNodes=ColumnsService.getReportDataService().getColumnNodes(rpt, paraVals);
		}catch(Exception e){
			log.error(e.toString());
		}
		if(colNodes==null){
			return null;
		}
		String unit = rpt.getDefaultUnit();
		RptMultiHeader header=new RptMultiHeader(colNodes,cd);
		//查询是否有用户的列配置信息，无则加载默认的表头
		
		Map muts = TemplatesLoader.getTemplatesLoader().getUnitsMap(); 
		if(loadDefaultMeata||sQueryplan==null||sQueryplan.getQpcontent()==null){
			ParaValue punit = (ParaValue)paraVals.get("unit");
			if(punit!=null){
				unit = punit.getValue();
			}
			for(int i=0;i<header.getSortedNodes().size();i++){
				Column col=(Column)header.getSortedNodes().get(i);
				if(col==null||col.getIsleaf()==0){
					continue;
				}
				Map<String, Object> column = new HashMap<String, Object>();
				column.put("id", col.getColId());
				column.put("header", col.getColName());
				column.put("width",col.getWidth());
				column.put("dataIndex",col.getDataIndex()==null?"":col.getDataIndex());
				column.put("align",StringUtils.isEmpty(col.getAlign())?(col.getDataType()>0?"right":"left"):col.getAlign());
				column.put("dataType", col.getDataType());
				column.put("tooltip",col.getColName());
				column.put("hideable",true);
				column.put("sortable",col.getIsOrder()==1);
				column.put("hidden",col.getDefaultHide()==1?true:false);
				//如果设置了报表的默认单位，可切换单位的列，默认使用指定的默认单位
				if(col.getIsMultiUnit()>0){
					String render = unit;
					if(StringUtils.isEmpty(render)){
						render = rpt.getDefaultUnit();
					}
					Unit un = muts==null?null:(Unit)muts.get(render);
					column.put("renderer",un==null?"renderFoo":un.getRenderFun());
				}else{
					column.put("renderer",StringUtils.isEmpty(col.getRenderer())?"renderFoo":col.getRenderer());
				}
				column.put("isMultiUnit",col.getIsMultiUnit());
				column.put("isLink",col.getIsLink());
				column.put("linkParams",col.getLinkParams());
				column.put("target",col.getTarget());
				column.put("linkTo",col.getLinkTo());
				column.put("linkAction", col.getLinkAction());
				column.put("popHeight", col.getPopHeight());
				column.put("popWidth", col.getPopWidth());
				ArrayList ltabs = col.getLinkTabs();
				if(ltabs!=null&&ltabs.size()>0){
					JSONArray jtbs = new JSONArray();
					for(int j = 0; j<ltabs.size(); j++){
						LinkTab ltb = (LinkTab)ltabs.get(j);
						JSONObject jtb = new JSONObject();
						try{
							jtb.put("title", ltb.getTitle());
							jtb.put("linkTo", ltb.getLinkTo());
							jtb.put("linkParams", ltb.getLinkParams());
						}catch(Exception e){
						}
						jtbs.put(jtb);
					}
					String strTbs = jtbs.toString();
					column.put("linkTabs", strTbs);
				}
				column.put("isGroup", col.getIsGroup());
				column.put("hideZero", col.getHideZero());
				columns.add(column);
			}
		}else{
			try{
				JSONObject jcfg = new JSONObject(sQueryplan==null?"":sQueryplan.getQpcontent());
				String sCon = jcfg.getString("condition");
				JSONObject condition = new JSONObject(sCon);
				filters = condition.getString("filter");
				if(condition!=null){
					unit = condition.getString("moneyUnit");
				}
				JSONArray  jcols= null;
				jcols = jcfg.getJSONArray("cols");
				if(jcols!=null){
					for(int i=0;i<jcols.length();i++){
						JSONObject col = jcols.getJSONObject(i);
						Map<String, Object> column = new HashMap<String, Object>();
						column.put("id", col.has("id")?col.getString("id"):"");
						column.put("header", col.has("header")?col.getString("header"):"");
						column.put("width",col.has("width")?col.getInt("width"):70);
						column.put("dataIndex",col.has("dataIndex")?col.getString("dataIndex"):"");
						column.put("align",col.has("align")?col.getString("align"):"right");
						column.put("dataType", col.has("dataType")?col.getString("dataType"):"0");
						column.put("tooltip",col.has("tooltip")?col.getString("tooltip"):"");
						column.put("hideable",col.has("hideable")?col.getBoolean("hideable"):true);
						column.put("sortable",col.has("sortable")?col.getBoolean("sortable"):false);
						column.put("hidden",col.has("hidden")?col.getBoolean("hidden"):false);
						//如果设置了报表的默认单位，可切换单位的列，默认使用指定的默认单位
						if(col.has("isMultiUnit")&&col.getInt("isMultiUnit")>0){
							String render = rpt.getDefaultUnit();
							Unit un = muts==null?null:(Unit)muts.get(render);
							column.put("renderer",un==null?"renderFoo":un.getRenderFun());
						}else{
							column.put("renderer",col.has("renderer")?col.getString("renderer"):"renderFoo");
						}
						column.put("isMultiUnit",col.has("isMultiUnit")?col.getInt("isMultiUnit"):"0");
						column.put("isLink",col.has("isLink")?col.getInt("isLink"):"0");
						column.put("linkParams",col.has("linkParams")?col.getString("linkParams"):"");
						column.put("target",col.has("target")?col.getString("target"):"");
						column.put("linkTo",col.has("linkTo")?col.getString("linkTo"):"");
						column.put("linkAction", col.has("linkAction")?col.getInt("linkAction"):"0");
						column.put("popHeight",col.has("popHeight")?col.getInt("popHeight"):"480");
						column.put("popWidth", col.has("popWidth")?col.getInt("popWidth"):"640");
						column.put("linkTabs", col.has("linkTabs")?col.getString("linkTabs"):"");
						column.put("isGroup", col.has("isGroup")?col.getInt("isGroup"):"0");
						column.put("hideZero", col.has("hideZero")?col.getInt("hideZero"):"0");
						columns.add(column);
					}
				}
			}catch(Exception e){
				log.error(e.toString());
			}
		}
		for(int i=0;i<header.getHiddenNodes().size();i++){
    		Column col=(Column)header.getHiddenNodes().get(i);
    		if(col.getDataIndex()==null||"".equals(col.getDataIndex())){
    			continue;
    		}
    		Map<String, Object> field = new HashMap<String, Object>();
			field.put("name", col.getDataIndex());
			field.put("type", col.getDataType()==1?"int":(col.getDataType()==2?"float":(col.getDataType()==9?"date":"string")));
			if(col.getDataType()==9){
				field.put("dateFormat","Y-m-d");
			}
			fields.add(field);
		}
    	for(int i=0;i<header.getSortedNodes().size();i++){
    		Column col=(Column)header.getSortedNodes().get(i);
    		if(col.getDataIndex()==null||"".equals(col.getDataIndex())){
    			continue;
    		}
    		Map<String, Object> field = new HashMap<String, Object>();
			field.put("name", col.getDataIndex());
			field.put("type", col.getDataType()==1?"int":(col.getDataType()==2?"float":(col.getDataType()==9?"date":"string")));
			if(col.getDataType()==9){
				field.put("dateFormat","Y-m-d");
			}
			fields.add(field);
    	}
    	List params = rpt.getParas();
    	List paramsFlt = rpt.getParasForFilter();
    	boolean hasComplexParams = false;
    	if(params!=null&&params.size()>0){
    		for(int i=0;i<params.size();i++){
    			Parameter pa = (Parameter)params.get(i);
    			if(pa.getIsHidden()==1){
	    			continue;
    			}
    			if(pa.getShowMode()==1){
    				hasComplexParams = true;
    				addParamItem(paramsInForm,pa,"","",paraVals,false,loadUserMeata);
    			}else{
    				addParamItem(ttbars,pa,"","",paraVals,true,loadUserMeata);
    			}
    		}
    	}
    	boolean hasComplexFlt = false;
    	if(paramsFlt!=null&&paramsFlt.size()>0){
    		hasComplexFlt = checkPermissionOfFilter(user);
    		for(int i=0;i<paramsFlt.size();i++){
    			ParameterForFilter pa = (ParameterForFilter)paramsFlt.get(i);
    			if(pa.getShowMode()==2){
    				addParamItem(ttbars,pa,pa.getFilterFld(),pa.getValueOprator(),paraVals,true,loadUserMeata);
    			}
    		}
    	}
		storeMetaData.put("root", "records");
		storeMetaData.put("totalProperty", "total");
		storeMetaData.put("successProperty", "success");
		storeMetaData.put("messageProperty", "message");
		storeMetaData.put("fields", fields);
		storeMetaData.put("columns", columns);
		storeMetaData.put("ttbars",ttbars);
		storeMetaData.put("paramsInForm",paramsInForm);
		storeMetaData.put("hasComplexFlt",hasComplexFlt);
		storeMetaData.put("hasComplexParams", hasComplexParams);
		storeMetaData.put("multiUnit", rpt.getMultiUnit()>0);
		storeMetaData.put("unit", unit);
		storeMetaData.put("filters", filters);
		storeMetaData.put("cQPid", sQueryplan==null?-1:sQueryplan.getId());
		storeMetaData.put("zeroCanHide", rpt.getZeroCanHide()>0);
		
		return storeMetaData;
	}
	public String parseParaExp(String exp,Report rpt,Map mParas)throws Exception{
		if(StringUtils.isEmpty(exp)){
			return "";
		}
		String[] tPas=StringUtils.substringsBetween(exp, "{", "}");
		String[] tDes=StringUtils.substringsBetween(exp, "$", "$");
		String[] tPros=StringUtils.substringsBetween(exp, "@", "@");
		//参数值解析
		if(tPas!=null){
			for(int i=0;i<tPas.length;i++){
				ParaValue pv=(ParaValue)mParas.get(tPas[i]);
				exp=exp.replaceAll("\\{"+tPas[i]+"\\}",pv==null?"":((String)pv.getValue()==null?"":(String)pv.getValue()));
			}
		}
		//参数描述（中文名称）解析
		if(tDes!=null){
			for(int i=0;i<tDes.length;i++){
				ParaValue pv=(ParaValue)mParas.get(tDes[i]);
				exp=exp.replaceAll("\\$"+tDes[i]+"\\$",pv==null?"":(pv.getDesc()==null?"":pv.getDesc()));
			}
		}
		if(tPros!=null){
			Map paraProsMap=rpt.getParaProcesses();
			ParaProcessService ppService=ParaProcessService.getParaProcessService();
			if(paraProsMap!=null){
				for(int i=0;i<tPros.length;i++){
					ParaProcess paraPro=(ParaProcess)paraProsMap.get(tPros[i]);
					String parsedVal=ppService.getProcessedParaValue(rpt,paraPro,mParas);
					exp=exp.replaceAll("\\@"+tPros[i]+"\\@",parsedVal==null?"":parsedVal);
				}
			}
		}
		return exp;
	}
	
	@SuppressWarnings("unchecked")
	private void addParamItem(List flds,Parameter pa,String filterFld,String valueOperator,Map paraVals,boolean istbar,boolean loadUserMeta){
		Map cont = new HashMap<String, Object>();
		Map tbitem = new HashMap<String, Object>();
		if(istbar){
			tbitem.put("xtype","label");
			tbitem.put("text", pa.getDesc()+"：");
			flds.add(tbitem);
		}
		if(pa.getRenderType()==1){
			cont.put("xtype","hidden");
			cont.put("id", "q_h_"+pa.getName());
			cont.put("filterFld",filterFld);
			cont.put("vop", valueOperator);
			ParaValue op = null;
			if(loadUserMeta){
				op = (ParaValue)paraVals.get(pa.getName());
				cont.put("value", op==null?"":op.getValue());
			}else{
				if(pa.getDefaultRule()!=null&&!"".equals(pa.getDefaultRule())){
					op = (ParaValue)paraVals.get(pa.getName());
					cont.put("value", op==null?"":op.getValue());
				}
			}
			flds.add(cont);
			cont = new HashMap<String, Object>();
			cont.put("xtype","combo");
			cont.put("id", "q_"+pa.getName());
			cont.put("width", pa.getWidth());
			cont.put("hiddenName", pa.getName());
			cont.put("valueField", "bm");
			cont.put("displayField", "name");
			cont.put("editable", false);
			cont.put("isMulti", pa.getIsMulti());
			cont.put("filterFld",filterFld);
			cont.put("vop", valueOperator);
			cont.put("affect", pa.getAffect());
			cont.put("affectedBy", pa.getAffectedByParas());
			cont.put("value", op==null?"":op.getDesc());
		}else if(pa.getRenderType()==2){
			cont.put("xtype","hidden");
			cont.put("id", "q_h_"+pa.getName());
			cont.put("filterFld",filterFld);
			cont.put("vop", valueOperator);
			ParaValue op = null;
			if(loadUserMeta){
				op = (ParaValue)paraVals.get(pa.getName());
				cont.put("value", op==null?"":op.getValue());
			}else{
				if(pa.getDefaultRule()!=null&&!"".equals(pa.getDefaultRule())){
					op = (ParaValue)paraVals.get(pa.getName());
					cont.put("value", op==null?"":op.getValue());
				}
			}
			flds.add(cont);
			cont = new HashMap<String, Object>();
			cont.put("xtype","trigger");
			cont.put("id", "q_"+pa.getName());
			cont.put("width", pa.getWidth());
			cont.put("isMulti", pa.getIsMulti());
			cont.put("filterFld",filterFld);
			cont.put("vop", valueOperator);
			cont.put("affect", pa.getAffect());
			cont.put("affectedBy", pa.getAffectedByParas());
			cont.put("value", op==null?"":op.getDesc());
			cont.put("onlyLeaf", pa.getLeafOnly()==1);
		}else if(pa.getRenderType()==3){
			cont.put("xtype","datefield");
			cont.put("id", "q_"+pa.getName());
			cont.put("width", pa.getWidth());
			String d = pa.getDateFormat();
			cont.put("format", (d==null||"".equals(d))?"Y-m-d":d); 
			cont.put("filterFld",filterFld);
			cont.put("vop", valueOperator);
			if(loadUserMeta){
				ParaValue op = (ParaValue)paraVals.get(pa.getName());
				cont.put("value", op==null?"":op.getValue());
			}else{
				cont.put("value", pa.getDefaultValue());
			}
		}else{
			cont.put("xtype","textfield");
			cont.put("id", "q_"+pa.getName());
			cont.put("width", pa.getWidth());
			cont.put("filterFld",filterFld);
			cont.put("vop", valueOperator);
			if(loadUserMeta){
				ParaValue op = (ParaValue)paraVals.get(pa.getName());
				cont.put("value", op==null?"":op.getValue());
			}else{
				cont.put("value", pa.getDefaultValue());
			}
		}
		if(!istbar){
			cont.put("fieldLabel", pa.getDesc());
		}
		flds.add(cont);
	}
	private boolean checkPermissionOfFilter(User user) {
		boolean can = true;
		return can;
	}
	
	/**
	 * 根据报表定义获取报表支持的金额单位列表。
	 * 如有指定支持的单位，按指定的单位组织列表，如未指定，则将配置的全部单位选项返回。
	* @param rpt
	* @return
	 */
	public List getUnits(Report rpt) {
		List units = null;
		Map muts = TemplatesLoader.getTemplatesLoader().getUnitsMap();
		String supportUnits = rpt.getSupportUnits();
		String[] rptUnits = supportUnits==null?null:supportUnits.split(",");
		boolean hasOriginal = false;
		if(rptUnits!=null&&rptUnits.length>0){
			units = new ArrayList();
			Unit oun =(Unit)muts.get("original");
			units.add(oun);
			for(int i=0;i<rptUnits.length;i++){
				String sun = rptUnits[i];
				Unit un = (Unit)muts.get(sun);
				if(un!=null&&!un.getId().equals("original")){
					units.add(un);
				}
			}
		}else{
			units = TemplatesLoader.getTemplatesLoader().getUnits();
		}
		return units;
	}
	public long addQueryPlan(String rptid,String qpname, String remark,int isdefault,String cfg,String userid) {
		if(cfg!=null){
			cfg = cfg.replace("'", "\"");
		}
		String sql = "select sq_queryplan.nextval from dual";
		long id =jdbcTemplate.queryForLong(sql);
		sql = "insert into queryplan(id,userid,rptid,name,remark,qpcontent,isdefault,savetime)values(?,?,?,?,?,?,?,sysdate)";
		final long fid = id;
		final String fuid= userid,frptid = rptid,fqpname = qpname,fremark=remark,fcgf = cfg;
		final int fdefault = isdefault;
		LobHandler lobHandler=new DefaultLobHandler();
		jdbcTemplate.execute(sql,
			new AbstractLobCreatingPreparedStatementCallback(lobHandler){
				protected void setValues(PreparedStatement ps,LobCreator lobCreator) throws SQLException {
					ps.setLong(1, fid);
					ps.setString(2, fuid);
					ps.setString(3, frptid);
					ps.setString(4, fqpname);
					ps.setString(5, fremark);
					lobCreator.setClobAsString(ps, 6, fcgf);
					ps.setInt(7, fdefault);
				}
			}
		);
		
		if(isdefault==1){
			sql = "update queryplan set isdefault=0 where id<>? and rptid=?";
			jdbcTemplate.update(sql,new Object[] {id,rptid});
		}
		return id;
	}
	
	public List getQueryPlans(String rptID, String cUser) {
		List qps = null;
		StringBuffer sql = new StringBuffer("select id,name,remark,isdefault,savetime from queryplan where rptid=? and userid=?");
		qps = jdbcTemplate.query(sql.toString(),new Object[]{rptID,cUser},ParameterizedBeanPropertyRowMapper.newInstance(QueryPlan.class));
		return qps;
	}
	/**
	 * 如无指定的方案，加载默认的方案，如无默认，则加载最近的
	* @param pid
	* @param rptID
	* @param userid
	* @return
	 */
	public QueryPlan getQueryPlan(int pid,String rptID,String userid){
		StringBuffer sql = new StringBuffer("select id,qpcontent from queryplan where id=").append(pid);
		if(pid<0){
			sql = new StringBuffer("select id,qpcontent from queryplan where userid='");
			sql.append(userid).append("' and rptid='").append(rptID).append("' and isdefault=1");
		}
		QueryPlan queryPlan = null;
		try{
			final LobHandler lobHandler=new DefaultLobHandler();
			List qps = (List)jdbcTemplate.query(sql.toString(),new Object[] {},
				new RowMapper() {
					public Object mapRow(ResultSet rs, int i) throws SQLException {
						QueryPlan qp = new QueryPlan();
						long id = rs.getLong(1);
				        String cfg = lobHandler.getClobAsString(rs, "qpcontent");
				        qp.setId(id);
				        qp.setQpcontent(cfg);
				        return qp;
				    }
			});
			if(qps!=null&&qps.size()>0){
				queryPlan = (QueryPlan)qps.get(0);
			}else{
				sql = new StringBuffer("select id,qpcontent from queryplan where userid='");
				sql.append(userid).append("' and rptid='").append(rptID).append("' order by id desc");
				qps = (List)jdbcTemplate.query(sql.toString(),new Object[] {},
						new RowMapper() {
							public Object mapRow(ResultSet rs, int i) throws SQLException {
								QueryPlan qp = new QueryPlan();
								long id = rs.getInt(1);
						        String cfg = lobHandler.getClobAsString(rs, "qpcontent");
						        qp.setId(id);
						        qp.setQpcontent(cfg);
						        return qp;
						    }
					});
					if(qps!=null&&qps.size()>0){
						queryPlan = (QueryPlan)qps.get(0);
					}
			}
		}catch(Exception e){
			queryPlan = null;
    	}
		return queryPlan;
	}
	//保存，更新查询方案。包含两类：一类是只更新方案本身，一类是更新方案的名称、说明等外围内容
	public boolean saveQueryPlan(int qpid, String rptid,String qpname, String remark,int isdefault, String cfg, boolean onlyClob) {
		String sql = "update queryplan set name = ?, remark=?,isdefault=? where id=?";
		if(onlyClob){
			sql = "update queryplan set qpcontent=? where id=?";
			final long fid = qpid;
			final String fcgf = cfg;
			LobHandler lobHandler=new DefaultLobHandler();
			jdbcTemplate.execute(sql,
				new AbstractLobCreatingPreparedStatementCallback(lobHandler){
					protected void setValues(PreparedStatement ps,LobCreator lobCreator) throws SQLException {
						lobCreator.setClobAsString(ps, 1, fcgf);
						ps.setLong(2, fid);
					}
				}
			);
		}else{
			jdbcTemplate.update(sql,new Object[] {qpname,remark,isdefault,qpid});
			if(isdefault==1){
				sql = "update queryplan set isdefault=0 where id<>? and rptid=?";
				jdbcTemplate.update(sql,new Object[] {qpid,rptid});
			}
		}
		return true;
	}
	//删除指定的查询方案
	public boolean deleteQueryPlan(int qpid) {
		String sql = "delete from queryplan where id=? ";
		jdbcTemplate.update(sql,new Object[] {qpid});
		return true;
	}
}
