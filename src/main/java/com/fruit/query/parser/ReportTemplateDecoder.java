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

public class ReportTemplateDecoder {
	private static Logger log = Logger.getLogger(ReportTemplateDecoder.class);
	private static ReportTemplateDecoder tmpDecoder;

	private ReportTemplateDecoder() {
	}
	public static ReportTemplateDecoder getDecoder() {
		if (tmpDecoder == null)
			tmpDecoder = new ReportTemplateDecoder();
		return tmpDecoder;
	}
	//加载表头表尾
	public JSONObject loadHeadFoot(Report rpt) {
		JSONObject jhf = new JSONObject();
		try{
			if(rpt.getTitle()!=null){
				jhf.put("titleExp", rpt.getTitle().getTitleExp());
			}
			if(rpt.getHead()!=null){
				Head hd = rpt.getHead();
				JSONObject jhd = new JSONObject();
				jhd.put("titleInHead", hd.getTitleInHead());
				jhd.put("height", hd.getHeight());
				jhd.put("style", hd.getStyle());
				if(hd.getSubTitle()!=null){
					JSONObject jst= new JSONObject();
					SubTitle st = hd.getSubTitle();
					jst.put("left", st.getLeftExp());
					jst.put("center", st.getCenterExp());
					jst.put("right", st.getRightExp());
					jst.put("lStyle", st.getlStyle());
					jst.put("cStyle", st.getcStyle());
					jst.put("rStyle", st.getrStyle());
					jhd.put("subTitle", jst);
				}
				jhf.put("head", jhd);
			}
			if(rpt.getFoot()!=null){
				Foot ft = rpt.getFoot();
				JSONObject jft = new JSONObject();
				jft.put("height", ft.getHeight());
				jft.put("left", ft.getLeftExp());
				jft.put("center", ft.getCenterExp());
				jft.put("right", ft.getRightExp());
				jft.put("lStyle", ft.getlStyle());
				jft.put("cStyle", ft.getcStyle());
				jft.put("rStyle", ft.getrStyle());
				jhf.put("foot", jft);
			}
		}catch(Exception e){
			log.error(e.toString());
		}
		return jhf;
	}
	public JSONObject loadDirectExport(Report rpt) {
		JSONObject jhf = new JSONObject();
		try{
			if(rpt.getExportInfo()!=null){
				Export exp = rpt.getExportInfo();
				jhf.put("directExport", rpt.getDirectExport());
				jhf.put("expFileName", exp.getExpFileName());
				jhf.put("template", exp.getTemplate());
			}
		}catch(Exception e){
			log.error(e.toString());
		}
		return jhf;
	}
	public JSONObject loadChartInfo(Report rpt) {
		JSONObject jct = new JSONObject();
		try{
			if(rpt.getChart()!=null){
				Chart ct = rpt.getChart();
				jct.put("hasChart", rpt.getHasChart());
				jct.put("categoryIndex", ct.getCategoryIndex());
				jct.put("chartType", ct.getChartType());
				jct.put("dataFormat", ct.getDataFormat());
				jct.put("dataIndex", ct.getDataIndex());
				jct.put("dataTemplateName", ct.getDataTemplateName());
				jct.put("height", ct.getHeight());
				jct.put("id", ct.getId());
				jct.put("isMultiSeries", ct.getIsMultiSeries());
				jct.put("seriesIndex", ct.getSeriesIndex());
				jct.put("width", ct.getWidth());
				jct.put("sourceType", ct.getSourceType());
				jct.put("sql", ct.getSql());
				jct.put("procedure", loadProcedure(ct.getProcedure()));
			}
		}catch(Exception e){
			log.error(e.toString());
		}
		return jct;
	}
	private JSONObject loadProcedure(ProcedureBean pro){
		if(pro==null){
			return null;
		}
		JSONObject jp = new JSONObject();
		try{
			jp.put("dataSetIndex", pro.getDataSetIndex());
			jp.put("name", pro.getName());
			jp.put("outPutInfoIndex", pro.getOutPutInfoIndex());
			jp.put("totalIndex", pro.getTotalIndex());
			List inParas = pro.getInParas();
			if(inParas!=null){
				JSONArray ips = new JSONArray();
				for(int i=0;i<inParas.size();i++){
					JSONObject ip = new JSONObject();
					ProParaIn ppi=(ProParaIn)inParas.get(i);
					ip.put("referMode", ppi.getReferMode());
					ip.put("referTo", ppi.getReferTo());
					ip.put("value", ppi.getValue());
					ip.put("dataType", ppi.getDataType());
					ips.put(ip);
				}
				jp.put("inParas",ips);
			}
			List outParas = pro.getOutParas();
			if(outParas!=null){
				JSONArray ops = new JSONArray();
				for(int i=0;i<outParas.size();i++){
					ProParaOut ppo=(ProParaOut)outParas.get(i);
					JSONObject op = new JSONObject();
					op.put("dataType", ppo.getDataType());
					ops.put(op);
				}
				jp.put("outParas",ops);
			}
		}catch(Exception e){
			log.error(e.toString());
		}
		return jp;
	}
	public JSONObject loadDataSets(Report rpt) {
		JSONObject jdts = new JSONObject();
		try{
			if(rpt.getDefaultDataDef()!=null){
				DataDefine dfdf = rpt.getDefaultDataDef();
				JSONObject jdfdt = loadDataSet(dfdf);
				jdts.put("defaultDt", jdfdt);
			}
			List dts = rpt.getDataDefines();
			if(dts!=null&&dts.size()>0){
				JSONArray jdfs = new JSONArray();
				for(int i=0;i<dts.size();i++){
					DataDefine dt =(DataDefine)dts.get(i);
					JSONObject jdf = loadDataSet(dt);
					jdfs.put(jdf);
				}
				jdts.put("dataSets", jdfs);
			}
		}catch(Exception e){
			log.error(e.toString());
		}
		return jdts;
	}
	private JSONObject loadDataSet(DataDefine df){
		if(df==null){
			return null;
		}
		JSONObject jdt = new JSONObject();
		try{
			jdt.put("name", df.getName());
			jdt.put("canPaging", df.getCanPaging());
			jdt.put("defaultPageSize", df.getDefaultPageSize());
			jdt.put("maxSize", df.getMaxSize());
			int st = df.getSourceType();
			jdt.put("sourceType", st);
			if(st ==1){
				jdt.put("sql", df.getSql());
			}else if(st==2){
				jdt.put("procedure", loadProcedure(df.getProcedure()));
			}else if(st==3){
				jdt.put("implClass", df.getImplClass());
			}
		}catch(Exception e){
			log.error(e.toString());
		}
		return jdt;
	}
	public JSONObject loadColumnsDefine(Report rpt) {
		JSONObject jcol = new JSONObject();
		try{
			if(rpt.getColumnDef()!=null){
				ColumnDefine cdf = rpt.getColumnDef();
				jcol.put("isComplex", cdf.getIsComplex());
				int st =cdf.getSourceType();
				jcol.put("sourceType", st);
				if(st==0){
					jcol.put("columns", loadStaticColumns(cdf.getColumns()));
				}else if(st ==1){
					jcol.put("sql", cdf.getSql());
				}else if(st==2){
					jcol.put("procedure", loadProcedure(cdf.getProcedure()));
				}else if(st==3){
					jcol.put("implClass", cdf.getImplClass());
				}
			}
		}catch(Exception e){
			log.error(e.toString());
		}
		return jcol;
	} 
	private JSONArray loadStaticColumns(List cols){
		JSONArray jcols = null;
		try{
			if(cols!=null&&cols.size()>0){
				jcols = new JSONArray();
				for(int i=0;i<cols.size();i++){
					JSONObject jcol = new JSONObject();
					Column col=(Column)cols.get(i);
					jcol.put("colId", col.getColId());
					jcol.put("colName", col.getColName());
					jcol.put("dataIndex", col.getDataIndex());
					jcol.put("pid", col.getPid());
					jcol.put("isleaf", col.getIsleaf());
					jcol.put("dataType", col.getDataType());
					jcol.put("renderer", col.getRenderer());
					jcol.put("width", col.getWidth());
					jcol.put("isHidden", col.getIsHidden());
					jcol.put("isOrder", col.getIsOrder());
					jcol.put("isMultiUnit", col.getIsMultiUnit());
					jcol.put("isLink", col.getIsLink());
					jcol.put("linkParams", col.getLinkParams());
					jcol.put("target", col.getTarget());
					jcol.put("linkTo", col.getLinkTo());
					jcol.put("hideZero", col.getHideZero());
					jcol.put("align", StringUtils.isEmpty(col.getAlign())?"":col.getAlign());
					jcol.put("defaultHide", col.getDefaultHide());
					jcols.put(jcol);
				}
			}
		}catch(Exception e){
			log.error(e.toString());
		}
		return jcols;
	}
	//加载所有参数定义
	public JSONArray loadAllParameters(Report rpt) {
		JSONArray jps = new JSONArray();
		try{
			List params = rpt.getParas();
			List params4flt = rpt.getParasForFilter();
			if(params!=null&&params.size()>0){
				for(int i=0;i<params.size();i++){
					Parameter p = (Parameter)params.get(i);
					JSONObject jo=getParamSummaryInfo(p);
					if(jo!=null){
						jo.put("isFilter", 0);
						jps.put(jo);
					}
				}
			}
			if(params4flt!=null&&params4flt.size()>0){
				if(jps==null){
					jps = new JSONArray();
				}
				for(int i=0;i<params4flt.size();i++){
					Parameter p = (Parameter)params4flt.get(i);
					JSONObject jo=getParamSummaryInfo(p);
					if(jo!=null){
						jo.put("isFilter", 1);
						jps.put(jo);
					}
				}
			}
		}catch(Exception e){
			System.out.println(e.toString());
		}
		return jps;
	}
	private JSONObject getParamSummaryInfo(Parameter p){
		if(p==null){
			return null;
		}
		JSONObject jo = new JSONObject();
		try{
			jo.put("name", p.getName());
			jo.put("desc", p.getDesc());
			jo.put("isHidden", p.getIsHidden());
			jo.put("renderType", p.getRenderType());
			jo.put("dataType", p.getDataType());
			jo.put("width", p.getWidth());
		}catch(Exception e){
			log.error(e.toString());
		}
		return jo;
	}
	//加载单个参数定义 
	public JSONObject loadParameter(Report rpt,String pname) {
		JSONObject jp = null;
		Map mparams=rpt.getParasMap();
		Map mparams4flt = rpt.getParas4FilterMap();
		Parameter p = null;
		String fld = "",op="";
		try{
			if(mparams!=null&&mparams.containsKey(pname)){
				p = (Parameter)mparams.get(pname);
				jp = new JSONObject();
				jp.put("isFilter", 0);
			}
			if(p==null&&mparams4flt!=null&&mparams4flt.containsKey(pname)){
				ParameterForFilter pf = (ParameterForFilter)mparams4flt.get(pname);
				jp = new JSONObject();
				jp.put("isFilter", 1);
				fld =pf.getFilterFld();
				op=pf.getValueOprator();
				p= pf;
			}
			if(p!=null){
				jp.put("name", p.getName());
				jp.put("dataType",p.getDataType());
				jp.put("desc",p.getDesc());
				jp.put("width",p.getWidth());
				jp.put("isHidden",p.getIsHidden());
				jp.put("bindMode",p.getBindMode());
				jp.put("bindTo",p.getBindTo());
				jp.put("renderType",p.getRenderType());
				jp.put("dateFormat",p.getDateFormat());
				jp.put("showMode",p.getShowMode());
				jp.put("validates",p.getValidates());
				jp.put("isMulti",p.getIsMulti());
				jp.put("leafOnly",p.getLeafOnly());
				jp.put("autoAll",p.getAutoAll());
				jp.put("sourceType",p.getSourceType());
				jp.put("paraOpts",loadParaOptions(p.getParaOptions()));
				jp.put("paraSql",p.getSql());
				jp.put("paraPro",loadProcedure(p.getProcedure()));
				jp.put("defaultValBindMode",p.getDefaultValBindMode());
				jp.put("defaultValueBindTo",p.getDefaultValueBindTo());
				jp.put("defaultValue",p.getDefaultValue());
				jp.put("defaultRule",p.getDefaultRule());
				jp.put("affect",p.getAffect());
				jp.put("affectCallBack",p.getAffectCallBack());
				jp.put("affectedByParas",p.getAffectedByParas());
				jp.put("filterFld",fld);
				jp.put("valueOprator",op);
			}
		}catch(Exception e){
			log.error(e.toString());
		}
		return jp;
	}
	private JSONArray loadParaOptions(List opts){
		JSONArray jops= null;
		try{
			if(opts!=null&&opts.size()>0){
				jops = new JSONArray();
				for(int i=0;i<opts.size();i++){
					JSONObject jop = new JSONObject();
					OptionItem oi=(OptionItem)opts.get(i);
					jop.put("bm", oi.getBm());
					jop.put("name", oi.getName());
					jop.put("pid", oi.getPid());
					jop.put("isleaf", oi.getIsleaf());
					jop.put("isDefault", oi.getIsDefault());
					jops.put(jop);
				}
			}
		}catch(Exception e){
			log.error(e.toString());
		}
		return jops;
	}
}
