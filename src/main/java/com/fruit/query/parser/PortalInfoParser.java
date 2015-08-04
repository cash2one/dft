package com.fruit.query.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.*;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.json.JSONArray;
import org.json.JSONObject;

import com.fruit.query.report.Report;
import com.fruit.query.util.QueryConfig;
import com.softwarementors.extjs.djn.StringUtils;

public class PortalInfoParser {
	private static Logger log = Logger.getLogger(PortalInfoParser.class);
	private static PortalInfoParser pps;

	private PortalInfoParser() {
	}
	public static PortalInfoParser getParser() {
		if (pps == null)
			pps = new PortalInfoParser();
		return pps;
	}
	private List infos = null;
	private Map infosMap = null;
	private Map extPortletsMap = null;
	//载入指定目录下的设计文件，缓存，并解析成ext组件，也缓存
	public void loadPortalInfosFromFs(){
		String path=QueryConfig.getConfig().getString("portalInfoPath", "portalInfos");
		String pathType = QueryConfig.getConfig().getString("rptPathType", "relative");
		String pre=path.substring(0,1);
		if("relative".equals(pathType)){//相对路径模式
			if(!"/".equals(pre)){
				path="/"+path;
			}
			URL rootP=TemplatesLoader.class.getClassLoader().getResource(path); 
			if(rootP==null){
				log.info("root of portalInfo is null!");
				return;
			}
			try{
				System.out.println("root of portalInfo:"+rootP.getPath());
				path=rootP.toURI().getPath();
			}catch(Throwable e){
				System.out.println("toURI转换错误："+e.toString());
				path=rootP.getPath();
				path = path.replaceAll("%20", " ");
			}
		}
		List portalPaths=new ArrayList();
		infos=new LinkedList();
		infosMap=new HashMap();
		extPortletsMap =new HashMap();
		InputStream is=null;
		try{
			java.io.File dir=new java.io.File(path);
			getAllFilesPath(dir,portalPaths);
			System.out.println("共找到"+portalPaths.size()+"个文件！");
			//各个报表设计文件循环解析、加载。
			if(portalPaths!=null&&portalPaths.size()>0){
				for(int i=0;i<portalPaths.size();i++){
					//文件流转化成string作为参数传递给解析器
					String jPath=(String)portalPaths.get(i);
					File ptFile=new File(jPath); 
					is=new FileInputStream(ptFile) ;
					long contentLength = ptFile.length();
					byte[] ba = new byte[(int)contentLength];
					is.read(ba);
					String ptDesignInfo = new String(ba,"utf-8");
					is.close();
					JSONObject jp=null;
					try{
						jp = new JSONObject(ptDesignInfo);
					}catch(Exception e){
						System.out.println(e.toString());
					}
					if(jp==null){
						continue;
					}else{
						JSONArray portlets =null;
						try{
							portlets =parse2Portlets(jp);
						}catch(Exception e){
						}
						if(portlets!=null){
							extPortletsMap.put(jp.getString("id"),portlets);
						}
						infos.add(jp);
						infosMap.put(jp.getString("id"), jp);
					}
					
				}
				System.out.println("共解析"+infos.size()+"个portal设计文件！");
			}
		}catch(Exception e){
			if(is!=null){
				try{
					is.close();
				}catch(Exception ex){};
			}
			System.out.print("加载报表设计文件信息时发生错误："+e.toString());
		}finally{
			try{
				is.close();
			}catch(Exception e){
				
			}
		}
		return ;
	}
	//按portal配置信息生成ext的panel成员
	public JSONArray parse2Portlets(JSONObject jp)throws Exception {
		//默认列宽和面板高度
		int cc = 0,dfHeight = 0;
		try{
			String scc = jp.has("colCount")?jp.getString("colCount"):"0";
			cc =Integer.parseInt(scc);
		}catch(Exception e){
		}
		try{
			String sdf = jp.has("defaultHeight")?jp.getString("defaultHeight"):"200";
			dfHeight = Integer.parseInt(sdf);
		}catch(Exception e){
		}
		float dfColumnWidth =(float)(Math.round(100/cc))/100;
		JSONArray jcols = jp.getJSONArray("columns");
		JSONArray colpanels = null;
		if(jcols!=null&&jcols.length()>0){
			//按列循环
			colpanels = new JSONArray();
			for(int i=0;i<jcols.length();i++){
				JSONObject jcol = jcols.getJSONObject(i);
				JSONObject colpanel = new JSONObject();
				colpanel.put("columnWidth", jcol.has("columnWidth")?jcol.getDouble("columnWidth"):dfColumnWidth);
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
							String sh = jptl.has("height")?jptl.getString("height"):"200";
							h=Integer.parseInt(sh);
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
	private void parsePortletContent(JSONObject ptl,String content,String ptlType)throws Exception{
		if("report".equals(ptlType)||"chart".equals(ptlType)){
			ptl.put("items", content);
		}else {
			ptl.put("html", content);
		}
	}
	//返回设计信息
	public JSONObject getPortalDesignByID(String id){
		JSONObject pi = null;
		if(infosMap ==null){
			loadPortalInfosFromFs();
		}
		if(infosMap!=null){
			pi = (JSONObject)infosMap.get(id);
		}
		return pi;
	}
	//返回构造好的portlets
	public JSONArray getExtPortletsById(String id){
		JSONArray pts = null;
		if(extPortletsMap ==null){
			loadPortalInfosFromFs();
		}
		if(extPortletsMap!=null){
			pts = (JSONArray)extPortletsMap.get(id);
		}
		return pts;
	}
	public List getPortalDesigns(){
		if(infos==null){
			loadPortalInfosFromFs();
		}
		return infos;
	}
	public Map getPortalDesignsMap(){
		if(infosMap==null){
			loadPortalInfosFromFs();
		}
		return infosMap;
	}
	public Map getAllParsedPortletsMap(){
		if(extPortletsMap==null){
			loadPortalInfosFromFs();
		}
		return extPortletsMap;
	}
	public void loadPortalInfosFromDb(){
		//stub
	}
	private void getAllFilesPath(File dir,List pathList)throws Exception{
		File[] fs = dir.listFiles(); 
		if(fs==null||fs.length==0)return;
		for(int i=0; i<fs.length; i++){ 
			if(fs[i].isDirectory()){
				getAllFilesPath(fs[i],pathList); 
			}else{
				pathList.add(fs[i].getAbsolutePath());
				System.out.println(fs[i].getAbsolutePath());
			}
		} 
	}
	public synchronized boolean deletePortalDesign(String pid) {
		if(infos ==null){
			return true;
		}
		int pl = infos.size();
		for(int i=0;i<pl;i++){
			JSONObject jp = (JSONObject)infos.get(i);
			String id = "";
			try{
				id = jp.getString("id");
			}catch(Exception e){
			}
			if(pid.equals(id)){
				infos.remove(i);
				infosMap.remove(id);
				extPortletsMap.remove(id);
				break;
			}
		}
		String root =getPortalInfoAbsolutePath();
		StringBuffer absFname = new StringBuffer(root==null?"":root);
		absFname.append(root.endsWith("/")?"":"/").append(pid).append(".JSON");
		File file = new File(absFname.toString());  
	    if(!file.exists()){
	        return true;  
	    }else{  
	    	if (file.isFile()){
	        	return file.delete();   
	        }
	    }
	    return true;
	}
	public synchronized void refreshPortalByID(String pid,JSONObject jp){
		if(infos==null){
			loadPortalInfosFromFs();
		}
		try{
			if(infosMap.containsKey(pid)){
				for(int i=0;i<infos.size();i++){
					JSONObject tjp = (JSONObject)infos.get(i);
					if(pid.equals(tjp.getString("id"))){
						infos.set(i, jp);
						break;
					}
				}
			}else{
				infos.add(jp);
			}
			infosMap.put(pid, jp);
			JSONArray portlets =parse2Portlets(jp);
			if(portlets!=null){
				extPortletsMap.put(pid,portlets);
			}
			output2Fs(jp);
		}catch(Exception e){
		}
	}
	//输出到文件系统
	public synchronized boolean output2Fs(JSONObject jp) {
		StringBuffer outFileName = new StringBuffer("");
		String id = "" ;
		try{
			id = jp.getString("id");
		}catch(Exception e){
		}
		String fileName = id +".JSON";
		String ptRoot = getPortalInfoAbsolutePath();
		outFileName.append(ptRoot==null?"":ptRoot).append(ptRoot.endsWith("/")?"":"/");
		outFileName.append(fileName);
		try{
			String content = jp.toString();
			FileWriter fw = new FileWriter(outFileName.toString());
		    PrintWriter out = new PrintWriter(fw);
		    out.write(content);
		    out.println();
		    fw.close();
		    out.close();
		} catch (Exception ex) {
			ex.printStackTrace();
			log.error(ex.toString());
			return false;
		}
		return true;
	}
	public String getPortalInfoAbsolutePath(){
    	String path=QueryConfig.getConfig().getString("portalInfoPath", "portalInfos");
		if(path==null||"".equals(path)){
			System.out.print("portal保存目录未指定或为空！");
		}
		String rptPathType = QueryConfig.getConfig().getString("rptPathType", "relative");
		if("relative".equals(rptPathType)){
			String pre=path.substring(0,1);
			if(!"/".equals(pre)){
				path="/"+path;
			}
			URL rootP=TemplatesLoader.class.getClassLoader().getResource(path); 
			if(rootP==null){
				return "";
			}
			try{
				path=rootP.toURI().getPath();
			}catch(Throwable e){
				path=rootP.getPath();
				path = path.replaceAll("%20", " ");
			}
		}
		return path;
    }
}
