package com.fruit.query.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.json.JSONArray;
import org.json.JSONObject;

import com.fruit.query.data.Column;
import com.fruit.query.data.OptionItem;
import com.fruit.query.data.ParaProcess;
import com.fruit.query.data.ProcedureBean;
import com.fruit.query.report.Chart;
import com.fruit.query.report.ColumnDefine;
import com.fruit.query.report.DataDefine;
import com.fruit.query.report.DefaultRuleDefine;
import com.fruit.query.report.Export;
import com.fruit.query.report.Foot;
import com.fruit.query.report.Head;
import com.fruit.query.report.Parameter;
import com.fruit.query.report.ParameterForFilter;
import com.fruit.query.report.ProParaIn;
import com.fruit.query.report.ProParaOut;
import com.fruit.query.report.Report;
import com.fruit.query.report.SubTitle;
import com.fruit.query.report.Title;
import org.apache.commons.lang.StringUtils;

public class ReportTemplateEncoder {
	private static Logger log = Logger.getLogger(ReportTemplateEncoder.class);
	private static ReportTemplateEncoder tmpEncoder;

	private ReportTemplateEncoder() {

	}

	public static ReportTemplateEncoder getEncoder() {
		if (tmpEncoder == null)
			tmpEncoder = new ReportTemplateEncoder();
		return tmpEncoder;
	}

	public Document encodeReport(Report rpt) {
		Document document = null;
		try {
			document = DocumentHelper.createDocument();
			Element root = document.addElement("report");
			encodeRoot(root, rpt);
			if (rpt.getParas() != null && rpt.getParas().size() > 0) {
				Element paramsNode = root.addElement("parameters");
				encodeParameters(paramsNode, rpt.getParas());
			}
			if (rpt.getParasForFilter() != null
					&& rpt.getParasForFilter().size() > 0) {
				Element pfNode = root.addElement("parametersForFilter");
				encodeParameters4Filter(pfNode, rpt.getParasForFilter());
			}
			if (rpt.getParaProcesses() != null) {
				Element ppNode = root.addElement("paraProcesses");
				encodeParaProcesses(ppNode, rpt.getParaProcesses());
			}
			if (rpt.getTitle() != null) {
				Element ttNode = root.addElement("title");
				encodeTitle(ttNode, rpt.getTitle());
			}
			if (rpt.getHead() != null) {
				Element headNode = root.addElement("head");
				encodeHead(headNode, rpt.getHead());
			}
			if (rpt.getColumnDef() != null) {
				Element colsNode = root.addElement("columns");
				encodeColumns(colsNode, rpt.getColumnDef());
			}
			if (rpt.getDefaultDataDef() != null) {
				Element mdNode = root.addElement("rptData");
				encodeMainData(mdNode, rpt.getDefaultDataDef());
			}
			if (rpt.getDataDefines() != null) {
				Element dtNode = root.addElement("DataSets");
				encodeDataSets(dtNode, rpt.getDataDefines());
			}
			if (rpt.getFoot() != null) {
				Element fNode = root.addElement("foot");
				encodeFoot(fNode, rpt.getFoot());
			}
			if (rpt.getChart() != null) {
				Element ctNode = root.addElement("chart");
				encodeChart(ctNode, rpt.getChart());
			}
			if (rpt.getExportInfo() != null) {
				Element exNode = root.addElement("export");
				encodeExport(exNode, rpt.getExportInfo());
			}
		} catch (Exception e) {
			log.error(e.toString());
		}
		return document;
	}

	// 根节点处理
	private Element encodeRoot(Element root, Report rpt) {
		root.addAttribute("id", rpt.getId());
		root.addAttribute("name", rpt.getName());
		Element desc = root.addElement("description");
		desc.addCDATA(rpt.getDescription());
		root.addAttribute("defaultUnit", rpt.getDefaultUnit());
		root.addAttribute("supportUnits", rpt.getSupportUnits());
		root.addAttribute("directExport", String.valueOf(rpt.getDirectExport()));
		root.addAttribute("hasChart", String.valueOf(rpt.getHasChart()));
		root.addAttribute("multiUnit", String.valueOf(rpt.getMultiUnit()));
		root.addAttribute("zeroCanHide", String.valueOf(rpt.getZeroCanHide()));
		return root;
	}

	private void encodeParameters(Element paramsNode, List params) {
		if (params == null) {
			return;
		}
		for (int i = 0; i < params.size(); i++) {
			Parameter para = (Parameter) params.get(i);
			Element pnode = paramsNode.addElement("para");
			encodeSinglePara(pnode, para);
		}
	}

	private void encodeParameters4Filter(Element fltParamsNode, List params) {
		if (params == null) {
			return;
		}
		for (int i = 0; i < params.size(); i++) {
			ParameterForFilter para = (ParameterForFilter) params.get(i);
			Element pnode = fltParamsNode.addElement("para");
			encodeSinglePara(pnode, para);
			pnode.addAttribute("filterFld", para.getFilterFld());
			pnode.addAttribute("valueOprator", para.getValueOprator());
		}
	}

	private void encodeParamDefaultDefine(Element dfrNode,DefaultRuleDefine drDefine) {
		DefaultRuleDefine drd = new DefaultRuleDefine();
		dfrNode.addAttribute("sourceType", String.valueOf(drd.getSourceType()));
		encodeDataDistill(drd.getSourceType(), dfrNode, drd.getSql(), drd
				.getProcedure(), drd.getImplClass());
	}

	private void encodeParaProcesses(Element ppNode, Map mpp) {
		if (mpp == null) {
			return;
		}
		Iterator it_pp = mpp.entrySet().iterator();
		while (it_pp.hasNext()) {
			Map.Entry e_pp = (Map.Entry) it_pp.next();
			ParaProcess pp = (ParaProcess) e_pp.getValue();
			if (pp != null) {
				Element pnode = ppNode.addElement("paraPro");
				pnode.addAttribute("name", pp.getName());
				pnode.addAttribute("desc", pp.getDesc());
				int proMode = pp.getProMode();
				pnode.addAttribute("proMode", String.valueOf(proMode));
				encodeDataDistill(proMode, pnode, pp.getSql(), pp
						.getProcedure(), pp.getImplClass());
			}
		}
	}

	private void encodeTitle(Element ttNode, Title tt) {
		ttNode.addCDATA(tt.getTitleExp());
	}

	private void encodeHead(Element hdNode, Head head) {
		if (head == null) {
			return;
		}
		hdNode.addAttribute("titleInHead", String
				.valueOf(head.getTitleInHead()));
		hdNode.addAttribute("height", String.valueOf(head.getHeight()));
		hdNode.addAttribute("style", head.getStyle());
		if (head.getSubTitle() != null) {
			Element stNode = hdNode.addElement("subTitle");
			SubTitle subTitle = head.getSubTitle();
			stNode.addAttribute("left", subTitle.getLeftExp());
			stNode.addAttribute("center", subTitle.getCenterExp());
			stNode.addAttribute("right", subTitle.getRightExp());
			stNode.addAttribute("lStyle", subTitle.getlStyle());
			stNode.addAttribute("cStyle", subTitle.getcStyle());
			stNode.addAttribute("rStyle", subTitle.getrStyle());
		}
	}

	private void encodeColumns(Element colsNode, ColumnDefine cd) {
		if (cd == null) {
			return;
		}
		colsNode.addAttribute("complex", String.valueOf(cd.getIsComplex()));
		int sType = cd.getSourceType();
		colsNode.addAttribute("sourceType", String.valueOf(sType));
		colsNode.addAttribute("rowNumber", String.valueOf(cd.getRowNumber()));
		if (sType == 0 && cd.getColumns() != null) {
			List cols = cd.getColumns();
			for (int i = 0; i < cols.size(); i++) {
				Element cnode = colsNode.addElement("column");
				Column col = (Column) cols.get(i);
				int cMode = col.getCalculate_mode();
				cnode.addAttribute("calculate_mode", String.valueOf(cMode));
				if (cMode == 1) {
					int fPos = col.getFuncPosition();
					cnode.addAttribute("funcPositon", String.valueOf(fPos));
				}
				cnode.addAttribute("colFunction", col.getColFunction());
				cnode.addAttribute("colId", col.getColId());
				cnode.addAttribute("colName", col.getColName());
				cnode.addAttribute("dataIndex", col.getDataIndex());
				cnode.addAttribute("dataType", String
						.valueOf(col.getDataType()));
				cnode.addAttribute("isleaf", String.valueOf(col.getIsleaf()));
				cnode.addAttribute("pid", col.getPid());
				cnode.addAttribute("renderer", col.getRenderer());
				cnode.addAttribute("width", String.valueOf(col.getWidth()));
				cnode.addAttribute("hidden", String.valueOf(col.getIsHidden()));
				cnode.addAttribute("readOnly", String
						.valueOf(col.getReadOnly()));
				cnode.addAttribute("isOrder", String.valueOf(col.getIsOrder()));
				cnode.addAttribute("editor", col.getEditor());
				cnode.addAttribute("isMultiUnit", String.valueOf(col
						.getIsMultiUnit()));
				cnode.addAttribute("isLink", String.valueOf(col.getIsLink()));
				cnode.addAttribute("linkParams", col.getLinkParams());
				cnode.addAttribute("target", col.getTarget());
				cnode.addAttribute("linkTo", col.getLinkTo());
				cnode.addAttribute("hideZero", String.valueOf(col.getHideZero()));
				cnode.addAttribute("align", col.getAlign());
				cnode.addAttribute("defaultHide", String.valueOf(col.getDefaultHide()));
			}
		} else {
			encodeDataDistill(sType, colsNode, cd.getSql(), cd.getProcedure(),
					cd.getImplClass());
		}
	}

	private void encodeMainData(Element mdNode, DataDefine df) {
		if (df == null) {
			return;
		}
		mdNode.addAttribute("canPaging", String.valueOf(df.getCanPaging()));
		mdNode.addAttribute("defaultPageSize", String.valueOf(df
				.getDefaultPageSize()));
		mdNode.addAttribute("maxSize", String.valueOf(df.getMaxSize()));
		int st = df.getSourceType();
		mdNode.addAttribute("sourceType", String.valueOf(st));
		mdNode.addAttribute("name", df.getName());
		encodeDataDistill(st, mdNode, df.getSql(), df.getProcedure(), df
				.getImplClass());
	}

	private void encodeDataSets(Element dtNode, List dtDefines) {
		if (dtDefines == null && dtDefines.size() == 0) {
			return;
		}
		for (int i = 0; i < dtDefines.size(); i++) {
			DataDefine subdtDef = (DataDefine) dtDefines.get(i);
			Element mdNode = dtNode.addElement("rptData");
			encodeMainData(mdNode, subdtDef);
		}
	}

	private void encodeChart(Element ctNode, Chart chart) {
		if (chart == null) {
			return;
		}
		ctNode.addAttribute("id", chart.getId());
		ctNode.addAttribute("chartType", chart.getChartType());
		ctNode.addAttribute("width", String.valueOf(chart.getWidth()));
		ctNode.addAttribute("height", String.valueOf(chart.getHeight()));
		ctNode.addAttribute("dataTemplateName", chart.getDataTemplateName());
		// 数据描述
		Element dnode = ctNode.addElement("data");
		dnode.addAttribute("dataFormat", String.valueOf(chart.getDataFormat()));
		dnode.addAttribute("isMultiSeries", String.valueOf(chart
				.getIsMultiSeries()));
		dnode.addAttribute("categoryIndex", chart.getCategoryIndex());
		dnode.addAttribute("dataIndex", chart.getDataIndex());
		dnode.addAttribute("seriesIndex", chart.getSeriesIndex());
		int sourceType = chart.getSourceType();
		dnode.addAttribute("sourceType", String.valueOf(sourceType));
		encodeDataDistill(sourceType, dnode, chart.getSql(), chart
				.getProcedure(), "");
	}

	private void encodeFoot(Element fNode, Foot foot) {
		if (foot == null) {
			return;
		}
		fNode.addAttribute("height", String.valueOf(foot.getHeight()));
		fNode.addAttribute("left", foot.getLeftExp());
		fNode.addAttribute("center", foot.getCenterExp());
		fNode.addAttribute("right", foot.getRightExp());
		fNode.addAttribute("lStyle", foot.getlStyle());
		fNode.addAttribute("cStyle", foot.getcStyle());
		fNode.addAttribute("rStyle", foot.getrStyle());
	}

	private void encodeExport(Element exNode, Export exp) {
		if (exp == null) {
			return;
		}
		exNode.addAttribute("template", exp.getTemplate());
		exNode.addAttribute("expName", exp.getExpFileName());
	}

	private void encodeProcedure(Element proNode, ProcedureBean pro) {
		proNode.addAttribute("name", pro.getName());
		proNode.addAttribute("datasetIndex", String.valueOf(pro
				.getDataSetIndex()));
		proNode.addAttribute("totalIndex", String.valueOf(pro.getTotalIndex()));
		proNode.addAttribute("outPutInfoIndex", String.valueOf(pro
				.getOutPutInfoIndex()));
		List proIns = pro.getInParas();
		if (proIns != null && proIns.size() > 0) {
			for (int i = 0; i < proIns.size(); i++) {
				Element piNode = proNode.addElement("in");
				ProParaIn ppi = (ProParaIn) proIns.get(i);
				int refMode = ppi.getReferMode();
				piNode.addAttribute("referMode", String.valueOf(refMode));
				if (refMode == 1) {
					piNode.addAttribute("referTo", ppi.getReferTo());
				} else {
					piNode.addAttribute("value", ppi.getValue());
					piNode.addAttribute("dataType", String.valueOf(ppi
							.getDataType()));
				}
			}
		}
		List proOuts = pro.getOutParas();
		if (proOuts != null && proOuts.size() > 0) {
			for (int i = 0; i < proOuts.size(); i++) {
				Element poNode = proNode.addElement("out");
				ProParaOut ppo = (ProParaOut) proOuts.get(i);
				poNode.addAttribute("dataType", String.valueOf(ppo
						.getDataType()));
			}
		}
	}

	private void encodeDataDistill(int sourceType, Element node, String sql,
			ProcedureBean pro, String cls) {
		if (sourceType == 1) {
			Element sqlNode = node.addElement("sql");
			sqlNode.addCDATA(sql);
		} else if (sourceType == 2 && pro != null) {// 如果是存储过程取数
			Element proNode = node.addElement("procedure");
			encodeProcedure(proNode, pro);
		} else {
			Element clNode = node.addElement("class");
			clNode.addAttribute("path", cls);
		}
	}

	private void encodeSinglePara(Element pnode, Parameter para) {
		pnode.addAttribute("name", para.getName());
		pnode.addAttribute("desc", para.getDesc());
		pnode.addAttribute("hidden", String.valueOf(para.getIsHidden()));
		pnode.addAttribute("bindMode", String.valueOf(para.getBindMode()));
		pnode.addAttribute("bindTo", para.getBindTo());
		pnode.addAttribute("autoAll", String.valueOf(para.getAutoAll()));
		pnode.addAttribute("dataType", String.valueOf(para.getDataType()));
		pnode.addAttribute("defaultValue", para.getDefaultValue());
		pnode.addAttribute("defaultValueBindTo", para.getDefaultValueBindTo());
		pnode.addAttribute("defaultValBindMode", String.valueOf(para
				.getDefaultValBindMode()));
		pnode.addAttribute("defaultRule", para.getDefaultRule());
		if (para.getDefaultRuleDefine() != null) {
			Element dfrNode = pnode.addElement("defaultRule");
			encodeParamDefaultDefine(dfrNode, para.getDefaultRuleDefine());
		}
		pnode.addAttribute("affectCallBack", para.getAffectCallBack());
		pnode.addAttribute("affect", para.getAffect());
		pnode.addAttribute("affectedByParas", para.getAffectedByParas());
		String[] vds = para.getValidates();
		if (vds != null && vds.length > 0) {
			String strVds = org.apache.commons.lang.StringUtils.join(vds, ",");
			pnode.addAttribute("validates", strVds);
		}
		pnode.addAttribute("renderType", String.valueOf(para.getRenderType()));
		pnode.addAttribute("dateFormat", para.getDateFormat());
		pnode.addAttribute("showMode", String.valueOf(para.getShowMode()));
		pnode.addAttribute("width", String.valueOf(para.getWidth()));
		if (para.getRenderType() == 1 || para.getRenderType() == 2) {
			Element pd = pnode.addElement("paraDetail");
			pd.addAttribute("multi", String.valueOf(para.getIsMulti()));
			pd.addAttribute("leafOnly", String.valueOf(para.getLeafOnly()));
			pd.addAttribute("sourceType", String.valueOf(para.getSourceType()));
			// 静态选项
			if (para.getSourceType() == 0 && para.getParaOptions() != null) {
				Element pitem = pd.addElement("paraItems");
				List paraOptions = para.getParaOptions();
				for (int j = 0; j < paraOptions.size(); j++) {
					OptionItem oi = (OptionItem) paraOptions.get(j);
					Element pi = pitem.addElement("item");
					pi.addAttribute("bm", oi.getBm());
					pi.addAttribute("name", oi.getName());
					pi.addAttribute("pid", oi.getPid());
					pi.addAttribute("isleaf", String.valueOf(oi.getIsleaf()));
					pi.addAttribute("isDefault", String.valueOf(oi
							.getIsDefault()));
				}
			} else {
				encodeDataDistill(para.getSourceType(), pd, para.getSql(), para
						.getProcedure(), para.getImplClass());
			}
		}
	}
	public Report buildReportBaseInfo(Report rpt,HttpServletRequest request) {
		String rptid = request.getParameter("rptId");
		String rptName = request.getParameter("rptName");
		String description  = request.getParameter("description");
		if(StringUtils.isEmpty(description)||"null".equalsIgnoreCase(description)){
			description = "";
		}
		rpt.setId(rptid);
		rpt.setName(rptName);
		rpt.setDescription(description);
		String sUnits = request.getParameter("multiUnit");
		int multiUnit = 0;
		if("true".equals(sUnits)||"on".equalsIgnoreCase(sUnits)||"1".equals(sUnits)){
			multiUnit = 1;
		}
		rpt.setMultiUnit(multiUnit);
		String supportedUnits = request.getParameter("supportUnits");
		rpt.setSupportUnits(supportedUnits);
		String defaultUnit = request.getParameter("defaultUnit");
		rpt.setDefaultUnit(defaultUnit);
		
		/*String sHasChart = request.getParameter("hasChart");
		int hasChart = 0;
		if("true".equals(sHasChart)||"on".equalsIgnoreCase(sHasChart)||"1".equals(sHasChart)){
			hasChart = 1;
		}
		rpt.setHasChart(hasChart);*/
		
		String sZeroCanHide = request.getParameter("zeroCanHide");
		int zeroCanHide = 0;
		if("true".equals(sZeroCanHide)||"on".equalsIgnoreCase(sZeroCanHide)||"1".equals(sZeroCanHide)){
			zeroCanHide = 1;
		}
		rpt.setZeroCanHide(zeroCanHide);
		
		/*String sDirExport = request.getParameter("directExport");
		int dirExport = 0;
		if("true".equals(sDirExport)||"on".equalsIgnoreCase(sDirExport)||"1".equals(sDirExport)){
			dirExport = 1;
		}
		rpt.setDirectExport(dirExport);*/
		
		return rpt;
	}
	//构造报表的各个部分，更新报表对象。一个tab中可能包括多个Report的部分。
	//看提交上来的json中，相应部分的内容是否为空判断要不要做解析和处理，节省多余的调用
	public Report buildReportPart(Report rpt, HttpServletRequest request) {
		String rptPart = request.getParameter("rptPart");
		String updateInfo = request.getParameter("updateInfo");
		JSONObject jo = parseObj2Update(updateInfo);
		if("parameters".equals(rptPart)||"all".equals(rptPart)){
			//负责处理普通参数、筛选参数、参数加工
			/*if(jo!=null&&jo.has("parameters")){
				JSONArray paramsObj =null;
				try{
					paramsObj= jo.getJSONArray("parameters");
				}catch(Exception e){}
				List paras = buildParamsInfo(paramsObj);
				rpt.setParas(paras);
				if(paras!=null){
			    	Map parasMap=new HashMap();
			    	for(int i=0;i<paras.size();i++){
			    		Parameter para=(Parameter)paras.get(i);
			    		if(para==null){
			    			continue;
			    		}
			    		parasMap.put(para.getName(), para);
			    	}
			    	rpt.setParasMap(parasMap);
			    }
			}
			if(jo!=null&&jo.has("parameters4Filter")){
				JSONArray params4FltObj =null;
				try{
					params4FltObj= jo.getJSONArray("parameters4Filter");
				}catch(Exception e){}
				List params4flt = buildParams4Filter(params4FltObj);
				rpt.setParasForFilter(params4flt);
				if(params4flt!=null){
			    	Map fparasMap=new HashMap();
			    	for(int i=0;i<params4flt.size();i++){
			    		ParameterForFilter fpara=(ParameterForFilter)params4flt.get(i);
			    		if(fpara==null){
			    			continue;
			    		}
			    		fparasMap.put(fpara.getName(), fpara);
			    	}
			    	rpt.setParas4FilterMap(fparasMap);
			    }
			}
			if(jo!=null&&jo.has("paraProcesses")){
				JSONArray ppsObj =null;
				try{
					ppsObj= jo.getJSONArray("paraProcesses");
				}catch(Exception e){}
				Map ppMap = buildParaProcesses(ppsObj);
				rpt.setParaProcesses(ppMap);
			}*/
		}else if("headFoot".equals(rptPart)||"all".equals(rptPart)){
			//负责处理Head，Foot，Title。
			if(jo!=null&&jo.has("head")){
				JSONObject hObj =null;
				try{
					hObj= jo.getJSONObject("head");
				}catch(Exception e){}
				Head head = buildHead(hObj);
				rpt.setHead(head);
			}else{
				rpt.setHead(null);
			}
			if(jo!=null&&jo.has("foot")){
				JSONObject fObj =null;
				try{
					fObj= jo.getJSONObject("foot");
				}catch(Exception e){}
				Foot foot = buildFoot(fObj);
				rpt.setFoot(foot);
			}else{
				rpt.setFoot(null);
			}
			if(jo!=null&&jo.has("title")){
				JSONObject ttObj =null;
				try{
					ttObj= jo.getJSONObject("title");
				}catch(Exception e){}
				Title tt = buildTitle(ttObj);
				rpt.setTitle(tt);
			}
		}else if("columnDefine".equals(rptPart)||"all".equals(rptPart)){
			if(jo!=null&&jo.has("columnDefine")){
				JSONObject colsObj =null;
				try{
					colsObj= jo.getJSONObject("columnDefine");
				}catch(Exception e){}
				ColumnDefine coldef = buildColumns(colsObj);
				rpt.setColumnDef(coldef);
			}
		}else if("dataSets".equals(rptPart)||"all".equals(rptPart)){
			//负责处理默认数据集，多数据集
			if(jo!=null&&jo.has("defaultDt")){
				JSONObject dfdtObj =null;
				try{
					dfdtObj= jo.getJSONObject("defaultDt");
				}catch(Exception e){}
				DataDefine df = buildDefaultDt(dfdtObj);
				rpt.setDefaultDataDef(df);
			}
			if(jo!=null&&jo.has("dataSets")){
				String strPro = "";
				try{
					strPro = jo.getString("dataSets");
				}catch(Exception e){
					log.error(e.toString());
				}
				JSONArray dtsObj =null;
				try{
					dtsObj=new JSONArray(strPro);
				}catch(Exception e){
					log.error(e.toString());
				}
				List dts = buildDataSets(dtsObj);
				rpt.setDataDefines(dts);
			}
		}else if("chart".equals(rptPart)||"all".equals(rptPart)){
			//负责处理图表
			try{
				if(jo!=null&&jo.getBoolean("hasChart")){
					rpt.setHasChart(1);
				}else{
					rpt.setHasChart(0);
				}
				if(jo!=null&&jo.has("chart")){
					JSONObject chartObj =null;
					chartObj= jo.getJSONObject("chart");
					Chart chart = buildChart(chartObj);
					rpt.setChart(chart);
				}else{
					rpt.setChart(null);
				}
			}catch(Exception e){
			}
		}else if("dirExport".equals(rptPart)||"all".equals(rptPart)){
			//负责处理导出
			try{
				if(jo!=null&&jo.getBoolean("directExport")){
					rpt.setDirectExport(1);
				}else{
					rpt.setDirectExport(0);
				}
				if(jo!=null&&jo.has("exporter")){
					JSONObject expObj =null;
					try{
						expObj= jo.getJSONObject("exporter");
					}catch(Exception e){}
					Export exp = buildExport(expObj);
					rpt.setExportInfo(exp);
				}else{
					rpt.setExportInfo(null);
				}
			}catch(Exception e){
			}
		}
		return rpt;
	}
	private JSONObject parseObj2Update(String updateInfo) {
		JSONObject jo = null;
		try{
			jo = new JSONObject(updateInfo);
		}catch(Exception e){
			log.error(e.toString());
		}
		return jo;
	}
	
	private Export buildExport(JSONObject jexp) {
		Export exp = new Export();
		try{
			exp.setExpFileName(jexp.getString("expFileName"));
			exp.setTemplate(jexp.getString("template"));
		}catch(Exception e){
			log.error(e.toString());
		}
		return exp;
	}

	private Chart buildChart(JSONObject jo) {
		Chart chart = new Chart();
		try{
			chart.setCategoryIndex(jo.getString("categoryIndex"));
			chart.setChartType(jo.getString("chartType"));
			chart.setDataFormat(jo.getInt("dataFormat"));
			chart.setDataIndex(jo.getString("dataIndex"));
			chart.setDataTemplateName(jo.getString("dataTemplateName"));
			chart.setHeight(jo.getInt("height"));
			chart.setId(jo.getString("id"));
			chart.setIsMultiSeries(jo.getInt("isMultiSeries"));
			chart.setSeriesIndex(jo.getString("seriesIndex"));
			chart.setWidth(jo.getInt("width"));
			int sourceType = jo.getInt("sourceType");
			chart.setSourceType(sourceType);
			if(sourceType==1){
				chart.setSql(jo.getString("sql"));
			}else if(sourceType==2){
				String strPro = jo.getString("procedure");
				JSONObject pjo = null;
				try{
					pjo=new JSONObject(strPro);
				}catch(Exception e){
					log.error(e.toString());
				}
				if(pjo!=null){
					ProcedureBean pro = buildPorcedure(pjo);
					chart.setProcedure(pro);
				}
			}
		}catch(Exception e){
			log.error(e.toString());
		}
		return chart;
	}
	
	private List buildDataSets(JSONArray jdts) {
		if(jdts==null||jdts.length()==0){
			return null;
		}
		List dts = new ArrayList();
		try{
			for(int i=0;i<jdts.length();i++){
				JSONObject jdt = jdts.getJSONObject(i);
				DataDefine dt = buildDefaultDt(jdt);
				dts.add(dt);
			}
		}catch(Exception e){
			log.error(e.toString());
		}
		return dts;
	}

	private DataDefine buildDefaultDt(JSONObject jo) {
		DataDefine df = new DataDefine();
		try{
			try{
				df.setCanPaging(jo.getInt("canPaging"));
			}catch(Exception e){
				log.error(e.toString());
			}
			try{
				df.setDefaultPageSize(jo.getInt("defaultPageSize"));
			}catch(Exception e){
				log.error(e.toString());
			}
			try{
				df.setMaxSize(jo.getInt("maxSize"));
			}catch(Exception e){
				log.error(e.toString());
			}
			int st = jo.getInt("sourceType");
			df.setSourceType(st);
			df.setName(jo.getString("name"));
			if(st==1){
				df.setSql(jo.getString("sql"));
			}else if(st==2){
				String strPro = jo.getString("procedure");
				JSONObject pjo = null;
				try{
					pjo=new JSONObject(strPro);
				}catch(Exception e){
					log.error(e.toString());
				}
				if(pjo!=null){
					ProcedureBean pro = buildPorcedure(pjo);
					df.setProcedure(pro);
				}
			}else if(st==3){
				df.setImplClass(jo.getString("implClass"));
			}
		}catch(Exception e){
			log.error(e.toString());
		}
		return df;
	}

	private ColumnDefine buildColumns(JSONObject jo) {
		ColumnDefine cdf = new ColumnDefine();
		try{
			cdf.setIsComplex(jo.getInt("complex"));
			int st = jo.getInt("sourceType");
			cdf.setSourceType(st);
			if (st == 0 && jo.has("cols")) {
				JSONArray jcols =jo.getJSONArray("cols");
				List cols = new ArrayList();
				for (int i = 0; i < jcols.length(); i++) {
					JSONObject jc = jcols.getJSONObject(i);
					Column col = new Column();
					col.setColId(jc.getString("colId"));
					col.setColName(jc.getString("colName"));
					col.setDataIndex(jc.getString("dataIndex"));
					col.setDataType(jc.getInt("dataType"));
					col.setHideZero(jc.getInt("hideZero"));
					col.setIsHidden(jc.getInt("isHidden"));
					col.setIsleaf(jc.getInt("isleaf"));
					col.setIsLink(jc.getInt("isLink"));
					col.setIsMultiUnit(jc.getInt("isMultiUnit"));
					col.setIsOrder(jc.getInt("isOrder"));
					col.setLinkParams(jc.getString("linkParams"));
					col.setLinkTo(jc.getString("linkTo"));
					col.setPid(jc.getString("pid"));
					col.setRenderer(jc.getString("renderer"));
					col.setTarget(jc.getString("target"));
					col.setWidth(jc.getInt("width"));
					col.setAlign(jc.getString("align"));
					col.setDefaultHide(jc.getInt("defaultHide"));
					cols.add(col);
				}
				cdf.setColumns(cols);
			} else if (st == 1&& jo.has("sql")) {
				cdf.setSql(jo.getString("sql"));
			} else if (st == 2 && jo.has("procedure")) {
				String strPro = jo.getString("procedure");
				JSONObject pjo = null;
				try{
					pjo=new JSONObject(strPro);
				}catch(Exception e){
					log.error(e.toString());
				}
				if(pjo!=null){
					ProcedureBean pb = buildPorcedure(pjo);
					cdf.setProcedure(pb);
				}
			} else if(st==3&&jo.has("implClass")){
				cdf.setImplClass(jo.getString("implClass"));
			}
		}catch(Exception e){
			log.error(e.toString());
		}
		return cdf;
	}

	private Title buildTitle(JSONObject jo) {
		Title tt = new Title();
		try{
			tt.setTitleExp(jo.getString("titleExp"));
		}catch(Exception e){
			log.error(e.toString());
		}
		return tt;
	}

	private Foot buildFoot(JSONObject jo) {
		Foot ft = new Foot();
		try{
			ft.setHeight(jo.getInt("height"));
			ft.setLeftExp(jo.getString("left"));
			ft.setRightExp(jo.getString("right"));
			ft.setCenterExp(jo.getString("center"));
			ft.setlStyle(jo.getString("lStyle"));
			ft.setrStyle(jo.getString("rStyle"));
			ft.setcStyle(jo.getString("cStyle"));
		}catch(Exception e){
			log.error(e.toString());
		}
		return ft;
	}

	private Head buildHead(JSONObject jo) {
		Head hd = new  Head();
		try{
			hd.setHeight(jo.getInt("height"));
			hd.setStyle(jo.getString("style"));
			hd.setTitleInHead(jo.getInt("titleInHead"));
			if(jo.has("subTitle")){
				JSONObject stObj = jo.getJSONObject("subTitle");
				SubTitle st = new SubTitle();
				st.setLeftExp(stObj.getString("left"));
				st.setRightExp(stObj.getString("right"));
				st.setCenterExp(stObj.getString("center"));
				st.setlStyle(stObj.getString("lStyle"));
				st.setrStyle(stObj.getString("rStyle"));
				st.setcStyle(stObj.getString("cStyle"));
				hd.setSubTitle(st);
			}
		}catch(Exception e){
			log.error(e.toString());
		}
		return hd;
	}
	private Parameter buildParameter(HttpServletRequest rq){
		Parameter pa = new Parameter();
		ParameterForFilter fpa = null;
		int forFilter = 0;
		try{
			pa.setName(rq.getParameter("pname"));
			pa.setDesc(rq.getParameter("desc"));
			String shidden = rq.getParameter("isHidden");
			int isHidden = ("on".equals(shidden)||"1".equals(shidden)||"true".equals(shidden))?1:0;
			pa.setIsHidden(isHidden);
			String sbindMode = rq.getParameter("bindMode");
			int bindMode = 0;
			try{
				bindMode = Integer.parseInt(sbindMode);
			}catch(Exception e){
			}
			pa.setBindMode(bindMode);
			pa.setBindTo(rq.getParameter("bindTo"));
			
			String sautoAll = rq.getParameter("autoAll");
			int autoAll = ("on".equals(sautoAll)||"1".equals(sautoAll)||"true".equals(sautoAll))?1:0;
			pa.setAutoAll(autoAll);
			String sdataType = rq.getParameter("dataType");
			int dataType = 0;
			try{
				dataType = Integer.parseInt(sdataType);
			}catch(Exception e){
			}
			pa.setDataType(dataType);
			//处理默认值
			String sdfbm = rq.getParameter("defaultValBindMode");
			int defaultValBindMode = 0;
			try{
				defaultValBindMode = Integer.parseInt(sdfbm);
			}catch(Exception e){
			}
			pa.setDefaultValBindMode(defaultValBindMode);
			if(defaultValBindMode==0){
				pa.setDefaultValue(rq.getParameter("defaultValueBindTo"));
				pa.setDefaultValueBindTo("");
				pa.setDefaultRule("");
			}else if(defaultValBindMode==9){
				pa.setDefaultValue("");
				pa.setDefaultValueBindTo("");
				pa.setDefaultRule(rq.getParameter("defaultValueBindTo"));
			}else{
				pa.setDefaultValue("");
				pa.setDefaultValueBindTo(rq.getParameter("defaultValueBindTo"));
				pa.setDefaultRule("");
			}
			pa.setAffectCallBack(rq.getParameter("affectCallBack"));
			pa.setAffect(rq.getParameter("affect"));
			pa.setAffectedByParas(rq.getParameter("affectedByParas"));
			String sValidates = rq.getParameter("validates");
			if(sValidates!=null){
				String[] vds = sValidates.split(",");
				pa.setValidates(vds);
			}
			pa.setDateFormat(rq.getParameter("dateFormat"));
			String sShowmode = rq.getParameter("showMode");
			int showMode = 0;
			try{
				showMode = Integer.parseInt(sShowmode);
			}catch(Exception e){
			}
			pa.setShowMode(showMode);
			String sWidth = rq.getParameter("width");
			int width = 0;
			try{
				width = Integer.parseInt(sWidth);
			}catch(Exception e){
			}
			pa.setWidth(width);
			String sRenderType = rq.getParameter("renderType");
			int renderType = 0;
			try{
				renderType = Integer.parseInt(sRenderType);
			}catch(Exception e){
			}
			pa.setRenderType(renderType);
			if (renderType == 1 || renderType == 2) {
				String sMulti = rq.getParameter("isMulti");
				int multi = ("on".equals(sMulti)||"1".equals(sMulti)||"true".equals(sMulti))?1:0;
				pa.setIsMulti(multi);
				String sLeaf = rq.getParameter("leafOnly");
				int leafOnly = ("on".equals(sLeaf)||"1".equals(sLeaf)||"true".equals(sLeaf))?1:0;
				pa.setLeafOnly(leafOnly);
				String sSource = rq.getParameter("sourceType");
				int st = 0;
				try{
					st = Integer.parseInt(sSource);
				}catch(Exception e){
				}
				pa.setSourceType(st);
				// 静态选项
				if (st == 0) {
					String strOpts = rq.getParameter("paraOpts");
					JSONArray jpos = null;
					try{
						jpos=new JSONArray(strOpts);
					}catch(Exception e){
						log.error(e.toString());
					}
					List paraOptions = new ArrayList();
					for (int i = 0; i < jpos.length(); i++) {
						JSONObject joi = jpos.getJSONObject(i);
						OptionItem oi = new OptionItem();
						oi.setBm(joi.getString("bm"));
						oi.setName(joi.getString("name"));
						oi.setIsDefault(joi.getInt("isDefault"));
						oi.setIsleaf(joi.getInt("isleaf"));
						oi.setPid(joi.getString("pid"));
						paraOptions.add(oi);
					}
					pa.setParaOptions(paraOptions);
				}else if (st == 1) {
					String strSql = rq.getParameter("paraSql");
					pa.setSql(strSql);
				} else if (st == 2 ) {
					String strPro = rq.getParameter("paraPro");
					JSONObject pjo = null;
					try{
						pjo=new JSONObject(strPro);
					}catch(Exception e){
						log.error(e.toString());
					}
					if(pjo!=null){
						ProcedureBean pb = buildPorcedure(pjo);
						pa.setProcedure(pb);
					}
				}
			}
			String sFlt = rq.getParameter("isFilter");
			forFilter = ("on".equals(sFlt)||"1".equals(sFlt)||"true".equals(sFlt))?1:0;
			if(forFilter==1){
				fpa = new ParameterForFilter(pa);
				fpa.setFilterFld(rq.getParameter("filterFld"));
				fpa.setValueOprator(rq.getParameter("valueOprator"));
			}
		}catch(Exception e){
			log.error(e.toString());
		}
		return forFilter==1?fpa:pa;
	}
	
	private Map buildParaProcesses(JSONArray jpps) {
		if(jpps==null||jpps.length()==0){
			return null;
		}
		Map ppMap = new HashMap();
		try{
			for(int i=0;i<jpps.length();i++){
				JSONObject jo = jpps.getJSONObject(i);
				ParaProcess pro=new ParaProcess();
				pro.setName(jo.getString("name"));
				pro.setDesc(jo.getString("desc"));
				int proMode=1;
				pro.setProMode(proMode);
				if(proMode==1){
					pro.setSql(jo.getString("sql"));
				}else if(proMode==2){
					String strPro = jo.getString("procedure");
					JSONObject pjo = null;
					try{
						pjo=new JSONObject(strPro);
					}catch(Exception e){
						log.error(e.toString());
					}
					if(pjo!=null){
						ProcedureBean pb = buildPorcedure(pjo);
						pro.setProcedure(pb);
					}
				}else{
					pro.setImplClass(jo.getString("implClass"));
				}
				ppMap.put(pro.getName(), pro);
			}
		}catch(Exception e){
			log.error(e.toString());
		}
		return ppMap;
	}
	private ProcedureBean buildPorcedure(JSONObject pjo){
		ProcedureBean pro = new ProcedureBean();
		try{
			try{
				pro.setDataSetIndex(pjo.getInt("dataSetIndex"));
			}catch(Exception e){}
			pro.setName(pjo.getString("name"));
			try{
				pro.setOutPutInfoIndex(pjo.getInt("outPutInfoIndex"));
			}catch(Exception e){}
			try{
				pro.setTotalIndex(pjo.getInt("totalIndex"));
			}catch(Exception e){}
			JSONArray ips = pjo.getJSONArray("inParas");
			JSONArray ops = pjo.getJSONArray("outParas");
			if(ips!=null){
				List inParas = new ArrayList();
				for(int i=0;i<ips.length();i++){
					JSONObject ip = ips.getJSONObject(i);
					ProParaIn ppi=new ProParaIn();
					ppi.setReferMode(ip.getInt("referMode"));
					if(ip.getInt("referMode")==1){
						ppi.setReferTo(ip.getString("referTo"));
					}else{
						ppi.setValue(ip.getString("value"));
						ppi.setDataType(ip.getInt("dataType"));
					}
					inParas.add(ppi);
				}
				pro.setInParas(inParas);
			}
			if(ops!=null){
				List outParas = new ArrayList();
				for(int i=0;i<ops.length();i++){
					JSONObject op = ops.getJSONObject(i);
					ProParaOut ppo=new ProParaOut();
					ppo.setDataType(op.getInt("dataType"));
					outParas.add(ppo);
				}
				pro.setOutParas(outParas);
			}
		}catch(Exception e){
			log.error(e.toString());
		}
		return pro;
	}

	public boolean updateParam(Report rpt,HttpServletRequest request) {
		String pname = request.getParameter("pname");
		String saveType = request.getParameter("saveType");
		boolean isNew = "add".equals(saveType);
		Map mparams = rpt.getParasMap();
		Map mparams4flt = rpt.getParas4FilterMap();
		List params = rpt.getParas();
		List params4flt =rpt.getParasForFilter();
		//提交的是否筛选
		boolean isFilter = false;
		String sFlt = request.getParameter("isFilter");
		if("1".equals(sFlt)||"on".equals(sFlt)||"true".equals(sFlt)){
			isFilter = true;
		}
		Parameter p = buildParameter(request);
		//构造
		if(isFilter){
			if(mparams4flt==null){
				mparams4flt = new HashMap();
				rpt.setParas4FilterMap(mparams4flt);
			}
			if(params4flt==null){
				params4flt = new ArrayList();
				rpt.setParasForFilter(params4flt);
			}
			if(isNew){
				addNewParameter(mparams4flt,params4flt, p);
			}else{
				//如果原先是非筛选的，在筛选集合中增加，删除原先在非筛选集合中的位置。
				if(mparams!=null&&mparams.containsKey(pname)){
					addNewParameter(mparams4flt,params4flt, p);
					deleteOldParameter(mparams,params,p);
				}else{
					updateOldParameter(mparams4flt,params4flt, p);
				}
			}
		}else{
			if(mparams==null){
				mparams = new HashMap();
				rpt.setParasMap(mparams);
			}
			if(params==null){
				params = new ArrayList();
				rpt.setParas(params);
			}
			if(isNew){
				addNewParameter(mparams,params, p);
			}else{
				//如果原先是筛选的，先在非筛选集合中增加，删除原先在非筛选集合中的位置。
				if(mparams4flt!=null&&mparams4flt.containsKey(pname)){
					addNewParameter(mparams,params, p);
					deleteOldParameter(mparams4flt,params4flt,p);
				}else{
					updateOldParameter(mparams,params, p);
				}
			}
		}
		return true;
	}
	
	private void addNewParameter(Map pmap,List plist,Parameter p){
		pmap.put(p.getName(), p);
		plist.add(p);
	}
	private void updateOldParameter(Map pmap,List plist,Parameter p){
		pmap.put(p.getName(), p);
		for(int i=0;i<plist.size();i++){
			Parameter tp = (Parameter)plist.get(i);
			if(tp.getName().equals(p.getName())){
				plist.set(i, p);
			}
		}
	}
	private void deleteOldParameter(Map pmap,List plist,Parameter p){
		pmap.remove(p.getName());
		for(int i=0;i<plist.size();i++){
			Parameter tp = (Parameter)plist.get(i);
			if(tp.getName().equals(p.getName())){
				plist.remove(i);
			}
		}
	}
}
