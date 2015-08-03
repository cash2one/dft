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
import com.fruit.query.parser.PortalInfoParser;
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
	public String getPortlets(String portalID) {
		JSONArray jpls = null;
		PortalInfoParser parser = PortalInfoParser.getParser();
		jpls = parser.getExtPortletsById(portalID);
		//如果还未找到缓存的、已构造的portlets，尝试重新按设计文件构造
		if(jpls==null){
			//缓存里找到设计信息
			JSONObject portalDesignInfo = null;
			try{
				portalDesignInfo =parser.getPortalDesignByID(portalID);
			}catch(Exception e){
				System.out.println(e.toString());
				return "{result:false,info:'加载portal设计信息时发生错误!'}";
			}
			//设计信息构造成Ext的portlet构件
			try{
				jpls = parser.parse2Portlets(portalDesignInfo);
			}catch(Exception e){
				System.out.println(e.toString());
				return "{result:false,info:'解析portal设计信息时发生错误!'}";
			}
		}
		StringBuffer strPls = new StringBuffer("{result:true,columns:");
		strPls.append(jpls==null?"[]":jpls.toString()).append("}");
		return strPls.toString();
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
			cont.put("id", "q_h_"+rptID+"_" + pa.getName());
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
			cont.put("id", "q_"+rptID+"_" + pa.getName());
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
			cont.put("id", "q_h_" +rptID+"_"+ pa.getName());
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
			cont.put("id", "q_" +rptID+"_"+ pa.getName());
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
			cont.put("id", "q_"+rptID+"_" + pa.getName());
			cont.put("width", 70);
			String d = pa.getDateFormat();
			cont.put("format", (d == null || "".equals(d)) ? "Y-m-d" : d);
			cont.put("filterFld", filterFld);
			cont.put("vop", valueOperator);
			cont.put("value", pa.getDefaultValue());
		} else {
			cont.put("rptID", rptID);
			cont.put("xtype", "textfield");
			cont.put("id", "q_" +rptID+"_"+ pa.getName());
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
