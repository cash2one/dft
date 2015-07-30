package com.fruit.query.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.*;

import com.fruit.query.data.Column;
import com.fruit.query.data.DataSet;
import com.fruit.query.data.ParaValue;
import com.fruit.query.data.Row;
import com.fruit.query.data.RptMultiHeader;
import com.fruit.query.data.Unit;
import com.fruit.query.parser.TemplatesLoader;
import com.fruit.query.report.Chart;
import com.fruit.query.report.ColumnDefine;
import com.fruit.query.report.DataDefine;
import com.fruit.query.report.Parameter;
import com.fruit.query.report.ParameterForFilter;
import com.fruit.query.report.Report;
import  com.ifugle.dft.query.entity.StoreResult;

public class PortalService {
	private static Logger log = Logger.getLogger(PortalService.class);
	private static PortalService portalService;
	private PortalService(){
	}
	public static PortalService getPortalService(){
		if(portalService==null)
			portalService=new PortalService();
		return portalService;
	}
	public String loadPortlets(String portalID) {
		String strPdesign = "";
		try{
			strPdesign = getPortalByID(portalID);
		}catch(Exception e){
			System.out.println(e.toString());
			return "{result:false,info:'加载portal设计信息时发生错误!'}";
		}
		JSONArray jpls = null;
		try{
			jpls = parse2Portlets(strPdesign);
		}catch(Exception e){
			System.out.println(e.toString());
			return "{result:false,info:'解析portal设计信息时发生错误!'}";
		}
		StringBuffer strPls = new StringBuffer("{result:true,columns:");
		strPls.append(jpls==null?"[]":jpls.toString()).append("}");
		return strPls.toString();
	}
	//按portal配置信息生成ext的panel成员
	public JSONArray parse2Portlets(String strPls)throws Exception {
		JSONObject jp = new JSONObject(strPls);
		//默认列宽和面板高度
		float dfColumnWidth =(float)(Math.round(100/jp.getInt("colCount")))/100;
		int dfHeight = 200;
		try{
			dfHeight = jp.has("defaultHeight")?jp.getInt("defaultHeight"):200;
		}catch(Exception e){
		}
		JSONArray jcols = jp.getJSONArray("columns");
		JSONArray colpanels = null;
		if(jcols!=null&&jcols.length()>0){
			//按列循环
			colpanels = new JSONArray();
			for(int i=0;i<jcols.length();i++){
				JSONObject jcol = jcols.getJSONObject(i);
				JSONObject colpanel = new JSONObject();
				colpanel.put("columnWidth", jcol.has("columnwidth")?jcol.getDouble("columnwidth"):dfColumnWidth);
				JSONArray jptls = jcol.getJSONArray("items");
				JSONArray ptls = null;
				if(jptls!=null){
					//一个列中的portlet面板循环
					ptls = new JSONArray();
					for(int j = 0;j<jptls.length();j++){
						JSONObject jptl = jptls.getJSONObject(j);
						JSONObject ptl = new JSONObject();
						ptl.put("layout", "fit");
						int h = 200;
						try{
							h=jptl.has("height")?jptl.getInt("height"):dfHeight;
						}catch(Exception e){
						}
						ptl.put("height", h);
						ptl.put("title", jptl.has("title")?jptl.getString("title"):"");
						String ptlType = jptl.has("type")?jptl.getString("type"):"text";
						ptl.put("ptype", ptlType);
						String id = "";
						if(jptl.has("id")){
							id = jptl.getString("id");
						}else if("report".equals(ptlType)){
							id = "report_"+i+j;
						}else if("chart".equals(ptlType)){
							id = "chart_"+i+j;
						}else{
							id = "text_"+i+j;
						}
						ptl.put("id", id);
						/*if(jptl.has("loadInPortal")){
							ptl.put("loadInPortal", jptl.getString("loadInPortal"));
						}else{
							ptl.put("loadInPortal", "");
						}*/
						String content = jptl.getString("content");
						parsePortletContent(ptl,content,ptlType);
						System.out.println("portlet("+id+"):"+ptl.toString());
						ptls.put(ptl);
					}
				}
				colpanel.put("items", ptls);
				System.out.println("colpanel("+i+"):"+colpanel.toString());
				colpanels.put(colpanel);
			}
		}
		System.out.println("最终输出总的面板:"+colpanels.toString());
		return colpanels;
	}
	//根据类型解析portlet的具体内容
	public void parsePortletContent(JSONObject ptl,String content,String ptlType)throws Exception{
		if("report".equals(ptlType)||"chart".equals(ptlType)){
			ptl.put("items", content);
			//ptl.put("html", content);
		}else {
			ptl.put("html", content);
		}
	}
	
	//根据id获取portal配置信息
	public String getPortalByID(String pid)throws Exception{
		StringBuffer jstr = new StringBuffer("{id:'test',name:'测试',total:'5',colCount:'3',defaultHeight:'200',columns:[");
		jstr.append("{'columnwidth':'.33',items:[");
		jstr.append("{id:'text',title:'面板1',height:'300',type:'text',content:'just a minute!'},");
		jstr.append("{id:'tax',title:'面板2',height:'',type:'report',content:'test',loadInPortal:'lazy'}] },");
		jstr.append("{'columnwidth':'.33',items:[");
		jstr.append("{title:'面板3',height:'',type:'chart',content:'dj'},");
		jstr.append("{title:'面板4',height:'400',type:'report',content:'dj'}]");
		jstr.append("},{'columnwidth':'.33',items:[");
		jstr.append("{title:'面板5',height:'100',type:'text',content:'last one'}]}]}");
		return jstr.toString();
	}
	public String getChartInfo2Create(String rptID) {
		StringBuffer cinfo = new StringBuffer("");
		Report rpt=TemplatesLoader.getTemplatesLoader().getReportTemplate(rptID);
		if(rpt==null||rpt.getChart()==null){
			cinfo.append("{result:false}");
		}else{
			Chart chart = rpt.getChart();
			String swf =chart.getChartFile();
			String chartID = chart.getId();
			int width = chart.getWidth();
			int height = chart.getHeight();
			cinfo.append("{result:true,chartInfo:{swf:'").append(swf).append("',cid:'").append(chartID);
			cinfo.append("',width:").append(width).append(",height:").append(height).append(",dataUrl:'");
			cinfo.append("chart.query?doType=getChartData&rptID=").append(rptID).append("&dataTemplate=").append(chart.getDataTemplateName());
			cinfo.append("'}}");
		}
		return cinfo.toString();
	}
	
	public void queryGeneralDataDynamic(StoreResult storeResult, Report rpt,Map paraVals, String replaceSql) {
		// 记录条数
		int count = 0;
		// 本页记录
		List<Map<String, Object>> results = null;
		int start = 0;
		int limit = 0;
		try {
			ParaValue sp = (ParaValue) paraVals.get("start");
			start = Integer.parseInt(sp.getValue());
		} catch (Exception e) {
		}
		try {
			ParaValue lp = (ParaValue) paraVals.get("limit");
			limit = Integer.parseInt(lp.getValue());
		} catch (Exception e) {
		}
		DataDefine df = rpt.getDefaultDataDef();
		try {
			DataSet ds = null;
			try {
				if (replaceSql != null && !"".equals(replaceSql)) {
					ds = RptDataService.getReportDataService().getReportDataPaging(rpt, paraVals, replaceSql,start, limit);
				} else {
					ds = RptDataService.getReportDataService().getReportDataPaging(rpt, paraVals, start, limit);
				}
			} catch (Exception e) {
			}
			if (ds != null && ds.getRows().size() > 0) {
				results = new ArrayList<Map<String, Object>>();
				for (int i = 0; i < ds.getRows().size(); i++) {
					Row row = (Row) ds.getRows().get(i);
					Map cells = row.getCells();
					cells.put("autoIndex", start + i + 1);
					results.add(cells);
				}
				if (df.getSourceType() == 2) {
					count = ds.getTotalCount();
				} else {
					if(replaceSql!=null&&!"".equals(replaceSql)){
						count=RptDataService.getReportDataService().getTotalCount(rpt, paraVals,replaceSql);
					}else{
						count=RptDataService.getReportDataService().getTotalCount(rpt, paraVals);
					}
				}
			} else {
				count = 0;
				results = new ArrayList<Map<String, Object>>();
			}
		} catch (Exception e) {
			log.error(e.toString());
		}
		storeResult.setRecords(results);
		storeResult.setTotal(new Long(count));
	}

	public Map<String, Object> buildStoreMetaData(Report rpt,Map paraVals) {
		Map storeMetaData = new HashMap();
		List columns = new ArrayList();
		List fields = new ArrayList();
		List ttbars = new ArrayList();
		// 获取报表的列定义
		ColumnDefine cd = rpt.getColumnDef();
		List colNodes = null;
		try {
			colNodes = ColumnsService.getReportDataService().getColumnNodes(
					rpt, paraVals);
		} catch (Exception e) {
			log.error(e.toString());
		}
		if (colNodes == null) {
			return null;
		}
		RptMultiHeader header = new RptMultiHeader(colNodes, cd);
		for (int i = 0; i < header.getSortedNodes().size(); i++) {
			Column col = (Column) header.getSortedNodes().get(i);
			if (col == null || col.getIsleaf() == 0) {
				continue;
			}
			Map<String, Object> column = new HashMap<String, Object>();
			column.put("id", col.getColId());
			column.put("header", col.getColName());
			column.put("width", col.getWidth());
			column.put("dataIndex", col.getDataIndex() == null ? "" : col.getDataIndex());
			column.put("align", col.getDataType() > 0 ? "right" : "left");
			column.put("tooltip", col.getColName());
			column.put("hideable", true);
			column.put("hidden", col.getIsHidden() == 1 ? true : false);
			column.put("sortable",col.getIsOrder()==1);
			column.put("renderer", col.getRenderer());
			column.put("editor", col.getEditor());
			columns.add(column);
		}
		for (int i = 0; i < header.getHiddenNodes().size(); i++) {
			Column col = (Column) header.getHiddenNodes().get(i);
			if (col.getDataIndex() == null || "".equals(col.getDataIndex())) {
				continue;
			}
			Map<String, Object> field = new HashMap<String, Object>();
			field.put("name", col.getDataIndex());
			field.put("type", col.getDataType() == 1 ? "int" : (col
					.getDataType() == 2 ? "float"
					: (col.getDataType() == 9 ? "date" : "string")));
			if (col.getDataType() == 9) {
				field.put("dateFormat", "Y-m-d");
			}
			fields.add(field);
		}
		for (int i = 0; i < header.getSortedNodes().size(); i++) {
			Column col = (Column) header.getSortedNodes().get(i);
			if (col.getDataIndex() == null || "".equals(col.getDataIndex())) {
				continue;
			}
			Map<String, Object> field = new HashMap<String, Object>();
			field.put("name", col.getDataIndex());
			field.put("type", col.getDataType() == 1 ? "int" : (col
					.getDataType() == 2 ? "float"
					: (col.getDataType() == 9 ? "date" : "string")));
			if (col.getDataType() == 9) {
				field.put("dateFormat", "Y-m-d");
			}
			fields.add(field);
		}
		List params = rpt.getParas();
		List paramsFlt = rpt.getParasForFilter();
		if (params != null && params.size() > 0) {
			for (int i = 0; i < params.size(); i++) {
				Parameter pa = (Parameter) params.get(i);
				if (pa.getIsHidden() == 1) {
					continue;
				}
				addToolBarItem(rpt.getId(), ttbars, pa, "", "", paraVals);
			}
		}
		if (paramsFlt != null && paramsFlt.size() > 0) {
			for (int i = 0; i < paramsFlt.size(); i++) {
				ParameterForFilter pa = (ParameterForFilter) paramsFlt.get(i);
				if (pa.getShowMode() == 2) {
					addToolBarItem(rpt.getId(), ttbars, pa, pa.getFilterFld(),
							pa.getValueOprator(), paraVals);
				}
			}
		}
		boolean hasComplexFlt = false;
		storeMetaData.put("root", "records");
		storeMetaData.put("totalProperty", "total");
		storeMetaData.put("successProperty", "success");
		storeMetaData.put("messageProperty", "message");
		storeMetaData.put("fields", fields);
		storeMetaData.put("columns", columns);
		storeMetaData.put("ttbars", ttbars);
		return storeMetaData;
	}

	@SuppressWarnings("unchecked")
	private void addToolBarItem(String rptID, List ttbars, Parameter pa,
			String filterFld, String valueOperator, Map paraVals) {
		Map tbitem = new HashMap<String, Object>();
		tbitem.put("xtype", "label");
		tbitem.put("text", pa.getDesc() + ":");
		ttbars.add(tbitem);
		Map cont = new HashMap<String, Object>();
		if (pa.getRenderType() == 1) {
			cont.put("xtype", "hidden");
			cont.put("id", "q_h_" + pa.getName());
			cont.put("filterFld", filterFld);
			cont.put("vop", valueOperator);
			ParaValue op = null;
			if (pa.getDefaultRule() != null && !"".equals(pa.getDefaultRule())) {
				op = (ParaValue) paraVals.get(pa.getName());
				cont.put("value", op == null ? "" : op.getValue());
			}
			ttbars.add(cont);
			cont = new HashMap<String, Object>();
			cont.put("rptID", rptID);
			cont.put("xtype", "combo");
			cont.put("id", "q_" + pa.getName());
			cont.put("width", 60);
			cont.put("hiddenName", pa.getName());
			cont.put("valueField", "bm");
			cont.put("displayField", "name");
			cont.put("editable", false);
			cont.put("isMulti", pa.getIsMulti());
			cont.put("filterFld", filterFld);
			cont.put("vop", valueOperator);
			cont.put("affect", pa.getAffect());
			cont.put("affectedBy", pa.getAffectedByParas());
			cont.put("value", op == null ? "" : op.getDesc());
		} else if (pa.getRenderType() == 2) {
			cont.put("xtype", "hidden");
			cont.put("id", "q_h_" + pa.getName());
			cont.put("filterFld", filterFld);
			cont.put("vop", valueOperator);
			ParaValue op = null;
			if (pa.getDefaultRule() != null && !"".equals(pa.getDefaultRule())) {
				op = (ParaValue) paraVals.get(pa.getName());
				cont.put("value", op == null ? "" : op.getValue());
			}
			ttbars.add(cont);
			cont = new HashMap<String, Object>();
			cont.put("rptID", rptID);
			cont.put("xtype", "trigger");
			cont.put("id", "q_" + pa.getName());
			cont.put("width", 60);
			cont.put("isMulti", pa.getIsMulti());
			cont.put("filterFld", filterFld);
			cont.put("vop", valueOperator);
			cont.put("affect", pa.getAffect());
			cont.put("affectedBy", pa.getAffectedByParas());
			cont.put("value", op == null ? "" : op.getDesc());
		} else if (pa.getRenderType() == 3) {
			cont.put("rptID", rptID);
			cont.put("xtype", "datefield");
			cont.put("id", "q_" + pa.getName());
			cont.put("width", 70);
			String d = pa.getDateFormat();
			cont.put("format", (d == null || "".equals(d)) ? "Y-m-d" : d);
			cont.put("filterFld", filterFld);
			cont.put("vop", valueOperator);
			cont.put("value", pa.getDefaultValue());
		} else {
			cont.put("rptID", rptID);
			cont.put("xtype", "textfield");
			cont.put("id", "q_" + pa.getName());
			cont.put("width", 80);
			cont.put("filterFld", filterFld);
			cont.put("vop", valueOperator);
			cont.put("value", pa.getDefaultValue());
		}
		ttbars.add(cont);
	}
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
}
