package com.fruit.query.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.dom4j.*;
import org.dom4j.io.*;
import org.json.JSONArray;
import org.json.JSONObject;

import com.fruit.query.data.Unit;
import com.fruit.query.parser.ReportTemplateDecoder;
import com.fruit.query.parser.ReportTemplateEncoder;
import com.fruit.query.parser.TemplatesLoader;
import com.fruit.query.parser.TemplatesToEditLoader;
import com.fruit.query.report.Parameter;
import com.fruit.query.report.Report;
import com.fruit.query.report.ReportBase;
import com.fruit.query.util.QueryConfig;
import com.jspsmart.upload.SmartUpload;
import com.softwarementors.extjs.djn.StringUtils;

public class RptDesignService {
	private static RptDesignService rptDesignService;
	private static List allRpts;
	private static Map allRptsMap;
	private static String rptRoot="";
	private static Logger log = Logger.getLogger(RptDesignService.class);
	public static RptDesignService getRptDesignService(){
		if(rptDesignService==null){
			rptDesignService = new RptDesignService();
		}
		return rptDesignService;
	}
	private RptDesignService(){
	}
	
	public synchronized List getAllReports(){
		if(allRpts!=null){
			return allRpts;
		}
		TemplatesToEditLoader ltmp=TemplatesToEditLoader.getTemplatesToEditLoader();
    	try{
    		String path=ltmp.getReportRepositoryPath();
    		ltmp.loadTemplatesToEditFromFile(path);
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    	allRpts = ltmp.getRptTemplates();
    	allRptsMap = ltmp.getRptTemplatesMap();
    	return allRpts;
	}
	public synchronized Map getAllReportsMap(){
		if(allRptsMap!=null){
			return allRptsMap;
		}
		TemplatesToEditLoader ltmp=TemplatesToEditLoader.getTemplatesToEditLoader();
    	try{
    		String path=ltmp.getReportRepositoryPath();
    		ltmp.loadTemplatesToEditFromFile(path);
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    	allRpts = ltmp.getRptTemplates();
    	allRptsMap = ltmp.getRptTemplatesMap();
    	return allRptsMap;
	}
	public boolean checkRptIdDuplicated(String rptid) {
		boolean dup=false;
		if(allRptsMap==null){
			getAllReportsMap();
		}
		if(allRptsMap!=null){
			dup=allRptsMap.containsKey(rptid);
		}
		return dup;
	}

	public synchronized boolean saveReport(Report rpt,int saveType) {
		String outFileName = "";
		String fileName = rpt.getSaveFileName();
		if(StringUtils.isEmpty(fileName)){
			fileName = rpt.getId();
		}
		if(!fileName.endsWith(".xml")&&!fileName.endsWith(".XML")){
			fileName+=".xml";
		}
		if(StringUtils.isEmpty(rptRoot)){
			TemplatesToEditLoader ltmp=TemplatesToEditLoader.getTemplatesToEditLoader();
			rptRoot = ltmp.getReportRepositoryAbsolutePath();
		}
		outFileName = (rptRoot==null?"":rptRoot)+(rptRoot.endsWith("/")?"":"/")+fileName;
		try{
			ReportTemplateEncoder te = ReportTemplateEncoder.getEncoder();
			Document document = te.encodeReport(rpt);
			OutputFormat format = OutputFormat.createPrettyPrint();
			XMLWriter writer = new XMLWriter(new FileOutputStream(new File(outFileName)), format);
			writer.write(document);
			writer.close();
		} catch (Exception ex) {
			ex.printStackTrace();
			log.error(ex.toString());
			return false;
		}
		if(allRpts==null){
			allRpts = new ArrayList();
		}
		if(allRptsMap==null){
			allRptsMap = new HashMap();
		}
		allRptsMap.put(rpt.getId(), rpt);
		//新增的，往集合里增加
		if(saveType==0){
			allRpts.add(rpt);
		}else{
			for(int i=0;i<allRpts.size();i++){
				Report trpt = (Report)allRpts.get(i);
				if(trpt.getId().equals(rpt.getId())){
					allRpts.set(i, rpt);
					break;
				}
			}
		}
		return true;
	}
	
	public String getAllUnits(String checkedUnits) {
		String strUnits = "[]";
		TemplatesLoader tl = TemplatesLoader.getTemplatesLoader();
		try{
			List units = tl.getUnits();
			if(units!=null){
				Map cumap = new HashMap();
				if(!StringUtils.isEmpty(checkedUnits)){
					String[] arrcu = checkedUnits.split(",");
					if(arrcu!=null&&arrcu.length>0){
						for(int i=0;i<arrcu.length;i++){
							cumap.put(arrcu[i],arrcu[i]);
						}
					}
				}
				JSONArray jus = new JSONArray();
				for(int i=0;i<units.size();i++){
					Unit u = (Unit)units.get(i);
					JSONObject ji=new JSONObject();
					ji.put("id", u.getId());
					ji.put("text", u.getName());
					ji.put("leaf", true);
					ji.put("cls","file");	
					ji.put("checked", cumap.containsKey(u.getId()));
					jus.put(ji);
				}
				strUnits = jus.toString();
			}
		}catch(Exception e){
			System.out.println(e.toString());
		}
		return strUnits;
	}
	//加载报表基础信息。不直接返回缓存的report对象，是为了构建一些额外的单位名称信息，缩小、分段json串的内容
	public ReportBase loadRptBaseInfo(String rptid) {
		ReportBase rb = null;
		if(allRptsMap==null){
			getAllReportsMap();
		}
		if(allRptsMap!=null){
			Report rpt = (Report)allRptsMap.get(rptid);
			if(rpt!=null){
				rb = new ReportBase();
				rb.setDefaultUnit(rpt.getDefaultUnit());
				rb.setDescription(rpt.getDescription());
				rb.setDirectExport(rpt.getDirectExport());
				rb.setHasChart(rpt.getHasChart());
				rb.setId(rpt.getId());
				rb.setMultiUnit(rpt.getMultiUnit());
				rb.setName(rpt.getName());
				rb.setSupportUnits(rpt.getSupportUnits());
				rb.setZeroCanHide(rpt.getZeroCanHide());
			}
		}
		if(rb!=null&&!StringUtils.isEmpty(rb.getSupportUnits())){
			TemplatesLoader tl = TemplatesLoader.getTemplatesLoader();
			Map allunits = tl.getUnitsMap();
			if(allunits!=null){
				String strUnits = rb.getSupportUnits()==null?"":rb.getSupportUnits();
				String[] units = strUnits.split(",");
				if(units!=null&&units.length>0){
					StringBuffer unames = new StringBuffer();
					for(int i=0;i<units.length;i++){
						String u = units[i];
						Unit ut = (Unit)allunits.get(u);
						unames.append(ut==null?"未知":ut.getName());
						if(i<units.length-1){
							unames.append(",");
						}
					}
					rb.setSupportUnitsName(unames.toString());
				}
			}
		}
		return rb;
	}
	//加载报表的局部信息，根据partName加载相应部分。
	public JSONObject loadRptPartInfo(String rptid,String partName) {
		JSONObject jpart = null;
		ReportTemplateDecoder de = ReportTemplateDecoder.getDecoder();
		Report rpt = null;
		if(allRptsMap==null){
			getAllReportsMap();
		}
		if(allRptsMap!=null){
			rpt = (Report)allRptsMap.get(rptid);
		}
		if(rpt ==null){
			jpart = new JSONObject();
			try{
				jpart.put("NO_SUCH_REPORT", false);
			}catch(Exception e){}
			return jpart;
		}
		if("headFoot".equals(partName)){
			jpart = de.loadHeadFoot(rpt);
		}else if("dirExport".equals(partName)){
			jpart = de.loadDirectExport(rpt);
		}else if("chart".equals(partName)){
			jpart = de.loadChartInfo(rpt);
		}else if("dataSets".equals(partName)){
			jpart = de.loadDataSets(rpt);
		}else if("columnDefine".equals(partName)){
			jpart = de.loadColumnsDefine(rpt);
		}else if("parameters".equals(partName)){
			//参数定义为grid，由其store的proxy进行加载
		}
		return jpart;
	}
	//修改报表基础内容（顶级节点属性）时，构造信息
	public Report buildReportBaseInfo(HttpServletRequest request) {
		Report rpt = null;
		String rptid = request.getParameter("rptId");
		if(allRptsMap==null){
			getAllReportsMap();
		}
		if(allRptsMap!=null){
			rpt = (Report)allRptsMap.get(rptid);
		}
		if(rpt ==null){
			rpt = new Report();
		}
		ReportTemplateEncoder re = ReportTemplateEncoder.getEncoder();
		rpt = re.buildReportBaseInfo(rpt, request);
		return rpt;
	}
	public static void main(String[] args){
		String fileName = "testAddNew.xml";
		RptDesignService rs = RptDesignService.getRptDesignService();
    	Report rpt = new Report();
    	rpt.setId("testAddNew_id");
    	rpt.setName("测试新增");
    	rpt.setDescription("服务端构造，输出xml");
    	rs.saveReport(rpt,0);
	}
	public Report buildReportPartInfo(HttpServletRequest request) {
		Report rpt = null;
		String rptid = request.getParameter("rptId");
		if(allRptsMap==null){
			getAllReportsMap();
		}
		if(allRptsMap!=null){
			rpt = (Report)allRptsMap.get(rptid);
		}
		if(rpt ==null){
			rpt = new Report();
		}
		ReportTemplateEncoder re = ReportTemplateEncoder.getEncoder();
		rpt = re.buildReportPart(rpt, request);
		return rpt;
	}
	//加载所有参数的简略情况，按普通参数在前，筛选参数在后的顺序
	public String getAllParams(String rptId) {
		String strParams = "[]";
		Report rpt = null;
		if(allRptsMap==null){
			getAllReportsMap();
		}
		if(allRptsMap!=null){
			rpt = (Report)allRptsMap.get(rptId);
		}
		if(rpt ==null){
			return strParams;
		}
		ReportTemplateDecoder de = ReportTemplateDecoder.getDecoder();
		JSONArray jps =de.loadAllParameters(rpt);
		if(jps!=null){
			strParams = jps.toString();
		}
		return strParams;
	}
	
	public boolean deleteParamDefine(String rptId, String pname) {
		if(StringUtils.isEmpty(rptId)||StringUtils.isEmpty(pname)){
			return false;
		}
		boolean done =false;
		Report rpt = null;
		if(allRptsMap==null){
			getAllReportsMap();
		}
		if(allRptsMap!=null){
			rpt = (Report)allRptsMap.get(rptId);
		}
		if(rpt ==null){
			return false;
		}
		try{
			List params = rpt.getParas();
			Map mparams = rpt.getParasMap();
			List params4flt = rpt.getParasForFilter();
			Map mparams4flt = rpt.getParas4FilterMap();
			if(params!=null&&params.size()>0){
				for(int i=0;i<params.size();i++){
					Parameter p = (Parameter)params.get(i);
					if(p!=null&&p.getName().equals(pname)){
						params.remove(i);
						mparams.remove(p.getName());
						done = true;
						break;
					}
				}
			}
			if(params4flt!=null&&params4flt.size()>0){
				for(int i=0;i<params4flt.size();i++){
					Parameter p = (Parameter)params4flt.get(i);
					if(p!=null&&p.getName().equals(pname)){
						params4flt.remove(i);
						mparams4flt.remove(p.getName());
						done = true;
						break;
					}
				}
			}
		}catch(Exception e){
			System.out.println(e.toString());
		}
		return done;
	}
	//获取单个的参数信息，调用decoder
	public JSONObject getParamDefine(String rptid, String pname) {
		JSONObject jparam = null;
		ReportTemplateDecoder de = ReportTemplateDecoder.getDecoder();
		Report rpt = null;
		if(allRptsMap==null){
			getAllReportsMap();
		}
		if(allRptsMap!=null){
			rpt = (Report)allRptsMap.get(rptid);
		}
		if(rpt ==null){
			jparam = new JSONObject();
			try{
				jparam.put("NO_SUCH_REPORT", false);
			}catch(Exception e){}
			return jparam;
		}
		jparam = de.loadParameter(rpt,pname);
		return jparam;
	}
	//修改时要注意，是否筛选的改变，要改变参数所在的List和Map（从原来的List和Map中去除，并插入到新的List和Map中）
	//调用Encoder
	public Report updateParam(HttpServletRequest request) {
		String rptId = request.getParameter("rptId");
		String pname = request.getParameter("pname");
		if(StringUtils.isEmpty(rptId)||StringUtils.isEmpty(pname)){
			return null;
		}
		boolean done =false;
		Report rpt = null;
		if(allRptsMap==null){
			getAllReportsMap();
		}
		if(allRptsMap!=null){
			rpt = (Report)allRptsMap.get(rptId);
		}
		if(rpt ==null){
			rpt = new Report();
		}
		ReportTemplateEncoder re = ReportTemplateEncoder.getEncoder();
		done = re.updateParam(rpt,request);
		return rpt;
	}
	//删除报表。删除全局List和Map中的内容，同时也删除模板文件,根据synDelete判断是否也删除生产库的同id文件
	public boolean deleteReport(String rptId,boolean synDelete) {
		//删除编辑库中的文件
		if(StringUtils.isEmpty(rptRoot)){
			TemplatesToEditLoader ltmp=TemplatesToEditLoader.getTemplatesToEditLoader();
			rptRoot = ltmp.getReportRepositoryAbsolutePath();
		}
		boolean done = deleteRptTemplate(allRpts,allRptsMap,rptRoot, rptId);
		if(synDelete){
			String proRoot = "";
			TemplatesLoader tl = TemplatesLoader.getTemplatesLoader();
			proRoot = tl.getReportRepositoryAbsolutePath();
			List rptlst = tl.getRptTemplates();
			Map rptMap = tl.getRptTemplatesMap();
			done = deleteRptTemplate(rptlst,rptMap,proRoot, rptId);
		}
		return done;
	}
	private boolean deleteRptTemplate(List rptList,Map rptMap,String root,String rptId){
		Report rpt = null;
		if(rptMap==null||rptList==null||StringUtils.isEmpty(rptId)){
			return true;
		}
		if(rptMap!=null){
			rpt = (Report)rptMap.get(rptId);
		}
		if(rpt==null){
			return true;
		}
		rptList.remove(rpt);
		rptMap.remove(rptId);
		
		String absFname = (root==null?"":root)+(root.endsWith("/")?"":"/")+rpt.getSaveFileName();
		File file = new File(absFname);  
	    if(!file.exists()){
	        return true;  
	    }else{  
	    	if (file.isFile()){
	        	return file.delete();   
	        }
	    }
		return true;
	}
	//
	public String[] commitRpt(String rptid,String dir){
		String[] result = new String[]{"1",""};
		Report erpt = null;
		if(allRptsMap==null){
			getAllReportsMap();
		}
		if(allRptsMap!=null){
			erpt = (Report)allRptsMap.get(rptid);
		}
		if("edit2product".equals(dir)&&erpt ==null){
			return new String[]{"9","未找到指定ID为"+rptid+"的报表！"};
		}
		TemplatesLoader tl = TemplatesLoader.getTemplatesLoader();
		Report proRpt = tl.getReportTemplate(rptid);
		if("product2edit".equals(dir)&&proRpt ==null){
			return new String[]{"9","未找到指定ID为"+rptid+"的报表！"};
		}
		if(StringUtils.isEmpty(rptRoot)){
			TemplatesToEditLoader ltmp=TemplatesToEditLoader.getTemplatesToEditLoader();
			rptRoot = ltmp.getReportRepositoryAbsolutePath();
		}
		String edtRoot= (rptRoot==null?"":rptRoot)+(rptRoot.endsWith("/")?"":"/");
		//目标库的模板集合、根目录等
		List rptlst = tl.getRptTemplates();
		Map rptMap = tl.getRptTemplatesMap();
		String proRoot = tl.getReportRepositoryAbsolutePath();
		proRoot = (proRoot==null?"":proRoot)+(proRoot.endsWith("/")?"":"/");
		//增加指定id的文件，按生产库的文件名
		if("edit2product".equals(dir)){
			result = synChronizeRpt(rptid,erpt,edtRoot,rptlst,rptMap,proRoot);
		}else{
			result = synChronizeRpt(rptid,proRpt,proRoot,allRpts,allRptsMap,edtRoot);
		}
		return result;
	}
	//将源库中的模板复制到目标库。覆盖同ID（非同名）文件：先删后增的方式。
	public String[] synChronizeRpt(String rptid,Report srcRpt,String srcRoot,List destLst,Map destMap,String destRoot) {
		String[] result = new String[]{"1",""};
		boolean exist = destMap.containsKey(rptid);
		Report destRpt = (Report)destMap.get(rptid);
		//如果目标库中已有该id的报表，替换之，否则就增加
		if(exist){
			for(int i=0;i<destLst.size();i++){
				Report trpt = (Report)destLst.get(i);
				if(trpt.getId().equals(rptid)){
					destLst.set(i, srcRpt);
					break;
				}
			}
		}else{
			destLst.add(srcRpt);
		}
		destMap.put(rptid, srcRpt);
		//处理文件
		//如果目标库已经有该id的文件，删除之
		if(exist){
			String delPath = destRoot+destRpt.getSaveFileName();
			File file = new File(delPath);  
		    if(file.exists()&&file.isFile()){
		       file.delete();   
		    }
		}
		String srcPath = srcRoot+srcRpt.getSaveFileName();
		String newPath = destRoot+srcRpt.getSaveFileName();
		try{ 
			int bytesum = 0; 
			int byteread = 0; 
			File oldfile = new File(srcPath); 
			if (oldfile.exists()) { 
				InputStream inStream = new FileInputStream(srcPath); //读入原文件 
				FileOutputStream fs = new FileOutputStream(newPath); 
				byte[] buffer = new byte[1444]; 
				while ( (byteread = inStream.read(buffer)) != -1) { 
					bytesum += byteread;
					fs.write(buffer, 0, byteread); 
				} 
				inStream.close(); 
				fs.close();
			} 
		}catch (Exception e) { 
			result[0]="9";
			result[1]=e.toString();
			log.error(e.toString());
		}
		return result;
	}
	public List getAllReportsOfProduct() {
		List rpts = null;
		TemplatesLoader tl = TemplatesLoader.getTemplatesLoader();
		rpts = tl.getRptTemplates();
		return rpts;
	}
	
	public String saveUploadedFile(SmartUpload mySmartUpload,ServletConfig svlConfig,HttpServletRequest request,HttpServletResponse response){
		String trace=QueryConfig.getConfig().getString("rptEditRepositoryPath", "");
		com.jspsmart.upload.File myFile=null;
		try{
		    mySmartUpload.upload();
		    myFile = mySmartUpload.getFiles().getFile(0);
		    if (myFile.isMissing()){
		    	throw new Exception(); 
		    }
		    //取得上载的文件的文件名
		    String myFileName=myFile.getFileName();
		    myFileName=new String(myFileName.getBytes("GBK"), "UTF-8");
		    //如果编辑库里已经有同名的文件，检查旧文件的模板ID，从缓存中去除该id的模板（因为模板文件要被覆盖掉了）
		    Report oldRpt = loadTemplateByFileName(myFileName);
		    if(oldRpt!=null){
		    	deleteFromCache(oldRpt.getId());
		    }
		    //保存新模板
		    if(StringUtils.isEmpty(rptRoot)){
				TemplatesToEditLoader ltmp=TemplatesToEditLoader.getTemplatesToEditLoader();
				rptRoot = ltmp.getReportRepositoryAbsolutePath();
			}
		    trace = (rptRoot==null?"":rptRoot)+(rptRoot.endsWith("/")?"":"/")+myFileName;
		    myFile.saveAs(trace,SmartUpload.SAVE_PHYSICAL);
		    //解析新上传的模板
		    Report newRpt = loadTemplateByFileName(myFileName);
			if(newRpt==null){
				throw new Exception("上传文件的格式不正确，无法解析为报表模板！");
			}else{
				newRpt.setSaveFileName(myFileName);
				String nrid = newRpt.getId();
				boolean exist = allRptsMap!=null&&allRptsMap.containsKey(nrid);
				Report orpt = null;
				if(exist){
					orpt = (Report)allRptsMap.get(nrid);
					for(int i=0;i<allRpts.size();i++){
						Report trpt = (Report)allRpts.get(i);
						if(trpt.getId().equals(nrid)){
							allRpts.set(i, newRpt);
							break;
						}
					}
					//如果目标库同id的文件不同名，删除旧有文件。同名则不需处理，上传时已经覆盖。
					if(!myFileName.equals(orpt.getSaveFileName())){
						String delPath = rptRoot+orpt.getSaveFileName();
						File file = new File(delPath);  
					    if(file.exists()&&file.isFile()){
					       file.delete();   
					    }
					}
				}else{
					allRpts.add(newRpt);
				}
				allRptsMap.put(nrid, newRpt);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return trace;
	}
	
	private void deleteFromCache(String rptid){
		allRptsMap.remove(rptid);
		for(int i=0;i<allRpts.size();i++){
			Report trpt = (Report)allRpts.get(i);
			if(trpt.getId().equals(rptid)){
				allRpts.remove(i);
				break;
			}
		}
	}
	//根据文件名加载模板
	private Report loadTemplateByFileName(String fname){
		Report rpt = null;
		TemplatesToEditLoader ltmp=TemplatesToEditLoader.getTemplatesToEditLoader();
	    if(StringUtils.isEmpty(rptRoot)){
			rptRoot = ltmp.getReportRepositoryAbsolutePath();
		}
	    String absPath = (rptRoot==null?"":rptRoot)+(rptRoot.endsWith("/")?"":"/")+fname;
	    try{
		    File tmpFile=new File(absPath); 
		    if(tmpFile.exists()&&tmpFile.isFile()){
		    	InputStream is=new FileInputStream(tmpFile) ;
				long contentLength = tmpFile.length();
				byte[] ba = new byte[(int)contentLength];
				is.read(ba);
				String rptDesignInfo = new String(ba,"utf-8");
				is.close();
				try{
					rpt=ltmp.loadTemplate(rptDesignInfo);
				}catch(Exception e){
					System.out.println(e.toString());
				} 
			}
	    }catch(Exception e){
	    	log.error(e.toString());
	    }
		return rpt;
	}
	public File findTemplateFile(String rptid) {
		File file =null;
		if(allRptsMap.containsKey(rptid)){
			Report rpt = (Report)allRptsMap.get(rptid);
			if(StringUtils.isEmpty(rptRoot)){
				TemplatesToEditLoader ltmp=TemplatesToEditLoader.getTemplatesToEditLoader();
				rptRoot = ltmp.getReportRepositoryAbsolutePath();
			}
			String absPath = (rptRoot==null?"":rptRoot)+(rptRoot.endsWith("/")?"":"/")+rpt.getSaveFileName();
			file= new File(absPath); 
		}
		return file;
	}
	
}
