package com.ifugle.dft.utils;

import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.ifugle.dft.check.dao.CheckDao;
import com.ifugle.dft.query.entity.StoreResult;
import com.ifugle.dft.utils.entity.Column;
import com.softwarementors.extjs.djn.config.annotations.DirectMethod;
import com.softwarementors.extjs.djn.servlet.ssm.WebContextManager;

public class ExportController {
	private static Logger logger = Logger.getLogger(ExportController.class);
	private Configuration cg ;
	public ExportController(){
		cg = (Configuration)ContextUtil.getBean("config");
	}
	@SuppressWarnings("unchecked")
	@DirectMethod
	public void doExport(HttpServletRequest request,HttpServletResponse response){
		Map model = buildExportInfo(request);
		List columns = model==null?null:(List)model.get("columns");
		String format = (String)model.get("format");
		if ("pdf".equalsIgnoreCase(format)) {
			ExportPdfView pdf = ExportPdfView.getpdfView();
			pdf.exportPdf(response, model);
		} else if (format.startsWith("excel")) {
			if ("excel2007".equals(format)||(columns!=null&&columns.size() >= 255)){
				ExportExcelXssfView xssf = new ExportExcelXssfView();
				xssf.exportExcel(model, request, response);
			}else{
				ExportExcelView xls = new ExportExcelView();
				xls.exportExcel(model, request, response);
			}
		} else {
			//其他格式。。。。
		}
	}
	
	@SuppressWarnings("unchecked")
	public Map buildExportInfo(HttpServletRequest request){
		Map model = new HashMap();
		String format =request.getParameter("format")==null?"":request.getParameter("format");
		String filename=request.getParameter("filename")==null?"":request.getParameter("filename");
		String columns=request.getParameter("columns")==null?"":request.getParameter("columns");
		String rows=request.getParameter("rows")==null?"":request.getParameter("rows");
		String groupRows = request.getParameter("groupRows")==null?"":request.getParameter("groupRows");
		String data=request.getParameter("data")==null?"":request.getParameter("data");
		String title=request.getParameter("title")==null?"":request.getParameter("title");
		String subject=request.getParameter("subject")==null?"":request.getParameter("subject");
		String description=request.getParameter("description")==null?"":request.getParameter("description");
		String subTitle = request.getParameter("subTitle")==null?"":request.getParameter("subTitle");
		subTitle = subTitle.replace("|", "      ");
		String foot = request.getParameter("foot")==null?"":request.getParameter("foot");
		foot = foot.replace("|", "      ");
		String action=request.getParameter("action")==null?"":request.getParameter("action");
		String method=request.getParameter("method")==null?"":request.getParameter("method");
		String params=request.getParameter("params")==null?"":request.getParameter("params");
		String paramOrder = request.getParameter("paramOrder")==null?"":request.getParameter("paramOrder");
		String sMaxExportRows=request.getParameter("maxExportRows")==null?"60000":request.getParameter("maxExportRows");
		String sRangeMode = request.getParameter("rangeMode")==null?"0":request.getParameter("rangeMode");
		String sExpStart = request.getParameter("expStart")==null?"0":request.getParameter("expStart");
		String sExpEnd = request.getParameter("expEnd")==null?"0":request.getParameter("expEnd");
		int maxExportRows = 0;
		try{
			maxExportRows = Integer.parseInt(sMaxExportRows);
		}catch(Exception e){
			maxExportRows =60000;
		}
		int rangeMode = 0;
        try{
        	rangeMode = Integer.parseInt(sRangeMode);
        }catch(Exception e){
        	rangeMode = 0;
		}
        int expStart = 0;
        try{
        	expStart = rangeMode==0?0:Integer.parseInt(sExpStart); 
        }catch(Exception e){
        	expStart = 0;
		}
        int expEnd = maxExportRows;
        try{
        	expEnd = rangeMode==0?maxExportRows:Integer.parseInt(sExpEnd); 
        }catch(Exception e){
        	expEnd = maxExportRows; 
		}
		if (logger.isDebugEnabled()) {
			logger.debug("filename = {}"+filename);
			logger.debug("columns = {}"+columns);
			logger.debug("rows = {}"+ rows);
			logger.debug("data = {}"+ data);
			logger.debug("title = {}"+ title);
			logger.debug("subject = {}"+ subject);
			logger.debug("description = {}"+ description);
			logger.debug("action = {}"+ action);
			logger.debug("method = {}"+ method);
			logger.debug("params = {}"+ params);
			logger.debug("paramOrder = {}"+ paramOrder);
			logger.debug("rangeMode = {}"+ rangeMode);
			logger.debug("expStart = {}"+ expStart);
			logger.debug("expEnd = {}"+ expEnd);
			logger.debug("subTitle = {}"+ subTitle);
			logger.debug("foot = {}"+ foot);
		}
		try{
			model.put("format", format);
			model.put("filename", filename);
			model.put("title", title);
			String moneyUnit = "";
			try{
				JSONObject jparams = new JSONObject(params);
				if(jparams!=null&&jparams.has("condition")){
					String strCon = jparams.getString("condition");
					JSONObject jcon = new JSONObject(strCon);
					moneyUnit = jcon.getString("moneyUnit");
				}
			}catch(Exception e){
			}
			
			model.put("moneyUnit", moneyUnit);
			//列解析
			List cols = parseColumns(columns);
			model.put("columns", cols);
			List gRows = parseGroupRows(groupRows);
			model.put("groupRows", gRows);
			
			model.put("subTitle", subTitle);
			model.put("foot", foot);
			//数据获取
			List recs = null;
			if (StringUtils.isNotEmpty(action)) {
				recs = parseData(action,method,params,paramOrder,expStart,expEnd);
			}
			model.put("records", recs);
		}catch(Exception e){
			logger.error(e.toString());
		}
		return model;
	}
	@SuppressWarnings("unchecked")
	private List parseColumns(String columns){
		List cols = new ArrayList();
		if(columns==null||"".equals(columns)){
			return cols;
		}
		try{
			JSONArray jcols = new JSONArray(columns);
			if(jcols==null||jcols.length()==0){
				return cols;
			}
			for(int i=0;i<jcols.length();i++){
				JSONObject jc = jcols.getJSONObject(i);
				if(jc!=null){
					/*if(jc.getBoolean("hidden")){
						continue;
					}*/
					Column col = new Column();
					col.setHeader(jc.getString("header"));
					col.setDataIndex(jc.getString("dataIndex"));
					try{	
						col.setAlign(jc.getString("align"));
					}catch(Exception e){
						col.setAlign("left");
					}
					try{	
						col.setWidth(jc.getInt("width"));
					}catch(Exception e){
						col.setWidth(100);
					}
					try{	
						col.setHidden(jc.getBoolean("hidden"));
					}catch(Exception e){
						col.setHidden(false);
					}
					try{	
						col.setIsGroup(jc.getInt("isGroup"));
					}catch(Exception e){
						col.setIsGroup(0);
					}
					try{
						col.setDataType(jc.getInt("dataType"));
					}catch(Exception e){
						col.setDataType(0);
					}
					try{
						col.setIsMultiUnit(jc.getInt("isMultiUnit"));
					}catch(Exception e){
						col.setIsMultiUnit(0);
					}
					try{
						col.setRenderer(jc.getString("renderer"));
					}catch(Exception e){
					}
					cols.add(col);
				}
			}
		}catch(Exception e){
			logger.error(e.toString());
		}
		return cols;
	}
	@SuppressWarnings("unchecked")
	private List parseGroupRows(String strRows){
		List grows = new ArrayList();
		if(strRows==null||"".equals(strRows)){
			return grows;
		}
		try{
			JSONArray jrows = new JSONArray(strRows);
			if(jrows==null||jrows.length()==0){
				return grows;
			}
			for(int i=0;i<jrows.length();i++){
				JSONArray jcs = jrows.getJSONArray(i);
				List cols = new ArrayList();
				if(jcs!=null){
					for(int j = 0;j<jcs.length();j++){
						JSONObject jc = jcs.getJSONObject(j);
						if(jc==null){
							continue;
						}
						Column col = new Column();
						col.setHeader(jc.getString("header"));
						col.setColspan(jc.getInt("colspan"));
						cols.add(col);
					}
				}
				grows.add(cols);
			}
		}catch(Exception e){
			logger.error(e.toString());
		}
		return grows;
	}
	@SuppressWarnings("unchecked")
	private List parseData(String action,String method,String params,String paramOrder,int expStart,int expEnd){
		List recs = null;
		try{
			List pVals = parseParameters(params,paramOrder,expStart,expEnd);
			String actPath=(String)cg.getHandlersMap().get(action);
			if(actPath==null||"".equals(actPath)){
				return recs;
			}
			Class c = Class.forName(actPath);
			Object obj = c.newInstance();
			Method[] ms = c.getDeclaredMethods();
			Method rm = null;
			for(int i=0;i<ms.length;i++){
				Method m = ms[i];
				Class[] cs = m.getParameterTypes();
				if(m.getName().equals(method)&&cs.length==pVals.size()){//只需方法名一致且参数个数一致，便认为找到了。
					rm = m;
					break;
				}
			}
			if(rm!=null){
				Class[] pts = rm.getParameterTypes();
				//处理参数
				Object[] vals = new Object[pts==null?0:pts.length];
				if(vals.length==0){
					vals = new Object[] {};
				}else{
					for(int i=0;i<vals.length;i++){
						Object val = pVals.get(i);
						System.out.println(val.getClass().getName());
						vals[i]=val;
					}
				}
				Object result = rm.invoke(obj, vals);
				List lResults = null;
				if(result.getClass().getName().equals(HashMap.class.getName())){
					lResults = (List)((HashMap)result).get("rows");
				}else if(result.getClass().getName().equals(StoreResult.class.getName())){
					lResults = (List)((StoreResult)result).getRecords();
				}else{
					lResults = (List)result;
				}
				recs = convertToList(lResults);
			}
		}catch(Exception e){
			logger.error(e.toString());
		}
		return recs;
	}
	@SuppressWarnings("unchecked")
	private List parseParameters(String params,String paramOrder,int expStart,int expEnd){
		List paras = new ArrayList();
		if(params==null||paramOrder==null||"".equals(params)||"".equals(paramOrder)){
			return paras;
		}
		try{
			String[] jorder = paramOrder.split(",");
			JSONObject jparams = new JSONObject(params);
			if(jorder.length>0){
				paras = new ArrayList();
				for(int i=0;i<jorder.length;i++){
					String p = jorder[i];
					if (p.equals("limit")&&expEnd > 0) {
						paras.add(new Integer(expEnd-expStart+1));
					}else if(p.equals("start")){
						paras.add(new Integer(expStart-1));
					}else{
						Object val = jparams.get(p);
						paras.add(val);
					}
				}
			}
		}catch(Exception e){
			logger.error(e.toString());
		}
		return paras;
	}
	@SuppressWarnings("unchecked")
	private List convertToList(List results){
		List recs = new ArrayList();
		if(results==null||results.size()==0){
			return recs;
		}
		for(int i=0;i<results.size();i++){
			Object value = (Object)results.get(i);
			Map row = null;
			//判断集合中记录的类型
			if (value instanceof Map) {
	            row = (Map)value;
			}else{
				row = reflectObject(value);
			}
			recs.add(row);
		}
		return recs;
	}
	@SuppressWarnings("unchecked")
	private Map reflectObject(Object bean){
		Map mapVals = new HashMap();
        Class klass = bean.getClass();
        Method[] methods = klass.getMethods();
        for (int i = 0; i < methods.length; i += 1) {
            try {
                Method method = methods[i];
                String name = method.getName();
                String key = "";
                if (name.startsWith("get")) {
                    key = name.substring(3);
                } else if (name.startsWith("is")) {
                    key = name.substring(2);
                }
                if (key.length() > 0 &&
                        Character.isUpperCase(key.charAt(0)) &&
                        method.getParameterTypes().length == 0) {
                    if (key.length() == 1) {
                        key = key.toLowerCase();
                    } else if (!Character.isUpperCase(key.charAt(1))) {
                        key = key.substring(0, 1).toLowerCase() +
                            key.substring(1);
                    }
                    Object[] paras = null;
                    Object valObj = method.invoke(bean,paras);
                    mapVals.put(key, valObj);
                }
            } catch (Exception e) {
                logger.error(e.toString());
            }
        }
        return mapVals;
    }
}
