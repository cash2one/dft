package com.fruit.query.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fruit.query.data.ReportFile;
import com.fruit.query.report.Report;
import com.fruit.query.util.QueryConfig;

public class TemplatesToEditLoader {
	private static Map rptTemplatesMap;
	private static List rptTemplates;
	private static TemplatesToEditLoader tLoader;
	private TemplatesToEditLoader(){};
	public static TemplatesToEditLoader getTemplatesToEditLoader(){
		if(tLoader!=null){
			return tLoader;
		}else{
			tLoader=new TemplatesToEditLoader();
			return tLoader;
		}
	}
	public void loadTemplatesToEditFromFile(String rptsPath){
		System.out.println("rptsToEditPath in TemplatesToEditLoader:"+rptsPath);
		if(rptsPath==null||"".equals(rptsPath)){
			System.out.print("报表编辑目录未指定或为空，没有要加载的报表模板！");
		}
		String pre=rptsPath.substring(0,1);
		String rptPathType = QueryConfig.getConfig().getString("rptPathType", "relative");
		if("relative".equals(rptPathType)){//相对路径模式
			if(!"/".equals(pre)){
				rptsPath="/"+rptsPath;
			}
			URL rootP=TemplatesLoader.class.getClassLoader().getResource(rptsPath); 
			if(rootP==null){
				System.out.println("rootP is null!");
				return;
			}
			try{
				System.out.println("rootP.getPath:"+rootP.getPath());
				rptsPath=rootP.toURI().getPath();
			}catch(Throwable e){
				System.out.println("toURI转换错误："+e.toString());
				rptsPath=rootP.getPath();
				rptsPath = rptsPath.replaceAll("%20", " ");
			}
		}
		System.out.println("rptRoot path:"+rptsPath);
		List rptPaths=new ArrayList();
		rptTemplates=new ArrayList();
		rptTemplatesMap=new HashMap();
		InputStream is=null;
		try{
			//根据路径，获取其中所有的设计文件。
			java.io.File dir=new java.io.File(rptsPath);
			getAllFilesPath(dir,rptPaths);
			System.out.println("共找到"+rptPaths.size()+"个文件！");
			//各个报表设计文件循环解析、加载。
			if(rptPaths!=null&&rptPaths.size()>0){
				for(int i=0;i<rptPaths.size();i++){
					//文件流转化成string作为参数传递给解析器
					String xmlPath=(String)rptPaths.get(i);
					File rptFile=new File(xmlPath); 
					String fname = rptFile.getName();
					is=new FileInputStream(rptFile) ;
					long contentLength = rptFile.length();
					byte[] ba = new byte[(int)contentLength];
					is.read(ba);
					String rptDesignInfo = new String(ba,"utf-8");
					is.close();
					//设计内容，解析后形成Report对象，存于系统中
					Report rpt=null;
					try{
						rpt=loadTemplate(rptDesignInfo);
					}catch(Exception e){
						System.out.println(e.toString());
					}
					if(rpt==null){
						continue;
					}
					rpt.setSaveFileName(fname);
					rptTemplates.add(rpt);
					rptTemplatesMap.put(rpt.getId(), rpt);
				}
				System.out.println("共解析"+rptTemplates.size()+"个报表模板！");
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
	}
	private void getAllFilesPath(File dir,List pathList)throws Exception{
		File[] fs = dir.listFiles(); 
		if(fs==null||fs.length==0)return;
		//报表库整体的文件结构初始化
		for(int i=0; i<fs.length; i++){ 
			ReportFile rptFile=new ReportFile();
			if(fs[i].isDirectory()){
				//记录文件结构
				rptFile.setIsFile(0);
				System.out.println(fs[i].getAbsolutePath());
				//递归获取报表文件
				getAllFilesPath(fs[i],pathList); 
			}else{
				//记录文件结构
				rptFile.setIsFile(1);
				rptFile.setPath(fs[i].getAbsolutePath());
				//添加报表文件。
				pathList.add(fs[i].getAbsolutePath());
				System.out.println(fs[i].getAbsolutePath());
			}
		} 
	}
	public Report loadTemplate(String tmp)throws Exception{
		Report rpt=null;
		ReportTemplateParser parser=ReportTemplateParser.getParser();
		rpt=parser.parseTemplateToReport(tmp);
		return rpt;
	}
	public List getRptTemplates(){
		if(rptTemplates==null){
	    	String path=QueryConfig.getConfig().getString("rptEditRepositoryPath", "");
	    	loadTemplatesToEditFromFile(path);
		}
		return rptTemplates;
	}
	/**
	 * 获取报表定义信息的map。
	 * @return 以Report对象的ID索引Report对象。
	 */
	public Map getRptTemplatesMap(){
		if(rptTemplatesMap==null){
	    	String path=QueryConfig.getConfig().getString("rptEditRepositoryPath", "");
	    	loadTemplatesToEditFromFile(path);
		}
		return rptTemplatesMap;
	}
	/**
	 * 根据指定的报表id获取报表对象
	 * @param rptID
	 * @return 指定id的报表模板对象。
	 */
	public Report getReportTemplate(String rptID){
		if(rptID==null||"".equals(rptID))
			return null;
		if(rptTemplatesMap==null){
	    	String path=QueryConfig.getConfig().getString("rptEditRepositoryPath", "");
	    	loadTemplatesToEditFromFile(path);
		}
		Report rptTmplt=(Report)rptTemplatesMap.get(rptID);
		return rptTmplt;
	}
    
    //返回报表repository的路径。如果rptRepositoryType=0，此时返回的是报表文件s所在的路径，该路径是classes下的相对路径。1：表名
    public String getReportRepositoryPath() throws Exception{
    	String path="";
    	path=QueryConfig.getConfig().getString("rptEditRepositoryPath", "");
    	return path;
    }
    public String getReportRepositoryAbsolutePath(){
    	String rptsPath=QueryConfig.getConfig().getString("rptEditRepositoryPath", "");
		if(rptsPath==null||"".equals(rptsPath)){
			System.out.print("报表目录未指定或为空，没有要加载的报表模板！");
		}
		String pre=rptsPath.substring(0,1);
		String rptPathType = QueryConfig.getConfig().getString("rptPathType", "relative");
		if("relative".equals(rptPathType)){//相对路径模式
			if(!"/".equals(pre)){
				rptsPath="/"+rptsPath;
			}
			URL rootP=TemplatesLoader.class.getClassLoader().getResource(rptsPath); 
			if(rootP==null){
				return "";
			}
			try{
				rptsPath=rootP.toURI().getPath();
			}catch(Throwable e){
				rptsPath=rootP.getPath();
				rptsPath = rptsPath.replaceAll("%20", " ");
			}
		}
		return rptsPath;
    }
}
