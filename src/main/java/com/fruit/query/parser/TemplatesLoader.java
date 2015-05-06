package com.fruit.query.parser;
import java.util.*;

import com.fruit.query.util.*;
import com.fruit.query.report.*;
import com.fruit.query.data.*;
import com.softwarementors.extjs.djn.StringUtils;

import java.io.*;
import java.net.URL;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
/**
 * 
 * @author wangxiaohui
 *2008-7-23
 *TODO 加载指定的文件
 */
public class TemplatesLoader {
	/**
	 * singleton，只需要一个模板加载器
	 */
	private static Map rptTemplatesMap;
	private static List rptTemplates;
	private static TemplatesLoader tLoader;
	private static List units;
	private static Map unitsMap;
	private static List charts;
	private static Map chartsMap;
	private TemplatesLoader(){};
	/**
	 * 获取模板加载器的实例。
	 * singleton，每次调用返回的是同一个模板加载器实例。
	 * @return 模板加载器实例。
	 */
	public static TemplatesLoader getTemplatesLoader(){
		if(tLoader!=null){
			return tLoader;
		}else{
			tLoader=new TemplatesLoader();
			return tLoader;
		}
	}
	/**
	 * 从指定路径加载模板集合。
	 * 加载指定路径下的所有模板，存入两个集合：rptTemplates，rptTemplatesMap两个成员变量赋值，
	 * 前者是模板对象的有序集合，后者是以模板ID索引的map
	 * @param rptsPath
	 * @return 无
	 * @throws TemplateLoadException
	 */
	public void loadTemplatesFromFile(String rptsPath){
		System.out.println("rptsPath in TemplatesLoader:"+rptsPath);
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
	//获取指定目录下的设计文件，递归
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
	/**
	 * 从数据库表中加载指定的模板集合。
	 * 这个表的结构是固定的。表中有clob字段存储设计内容
	 * @param tbName 存放报表设计信息的表
	 * @throws TemplateLoadException
	 */
	public void loadTemplatesFromDb(String tbName){
		//从数据库中读取指定表（表名由配置文件指定而非固定，是为了防止固定表名与其他应用的表名重复）
		
		//从RPT_CONTENT字段（clob）读取设计内容，转化为string作为参数传递给解析器。
	}
	/**
	 * 加载单个指定的报表
	 * @param tmp 模板定义内容，xml格式。
	 * @throws Exception
	 */
	public Report loadTemplate(String tmp)throws Exception{
		Report rpt=null;
		ReportTemplateParser parser=ReportTemplateParser.getParser();
		rpt=parser.parseTemplateToReport(tmp);
		return rpt;
	}
	/**
	 * 获取报表定义信息集合。
	 * 是Report对象的有序集合。
	 * @return 报表模板集合。
	 */
	public List getRptTemplates(){
		if(rptTemplates==null){
			int type=0;
	    	String sType=QueryConfig.getConfig().getString("rptRepositoryType", "0");
	    	try{
	    		type=Integer.parseInt(sType);
	    	}catch(Exception e){
	    		type=0;
	    	}
	    	String path=QueryConfig.getConfig().getString("rptRepositoryPath", "");
	    	if(type==0){
    			loadTemplatesFromFile(path);
    		}else{
    			loadTemplatesFromDb(path);
    		}
		}
		return rptTemplates;
	}
	/**
	 * 获取报表定义信息的map。
	 * @return 以Report对象的ID索引Report对象。
	 */
	public Map getRptTemplatesMap(){
		if(rptTemplatesMap==null){
			int type=0;
	    	String sType=QueryConfig.getConfig().getString("rptRepositoryType", "0");
	    	try{
	    		type=Integer.parseInt(sType);
	    	}catch(Exception e){
	    		type=0;
	    	}
	    	String path=QueryConfig.getConfig().getString("rptRepositoryPath", "");
	    	if(type==0){
    			loadTemplatesFromFile(path);
    		}else{
    			loadTemplatesFromDb(path);
    		}
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
			int type=0;
	    	String sType=QueryConfig.getConfig().getString("rptRepositoryType", "0");
	    	try{
	    		type=Integer.parseInt(sType);
	    	}catch(Exception e){
	    		type=0;
	    	}
	    	String path=QueryConfig.getConfig().getString("rptRepositoryPath", "");
	    	if(type==0){
    			loadTemplatesFromFile(path);
    		}else{
    			loadTemplatesFromDb(path);
    		}
		}
		Report rptTmplt=(Report)rptTemplatesMap.get(rptID);
		return rptTmplt;
	}
	
//	检查报表设计文件存储在何种介质。0：以文件的形式存储；1：以xml串的形式存放在数据库表中。表有固定的结构。
    public int getReportRepositoryType() throws Exception{
    	int type=0;
    	String sType=QueryConfig.getConfig().getString("rptRepositoryType", "0");
    	try{
    		type=Integer.parseInt(sType);
    	}catch(Exception e){
    		type=0;
    	}
    	return type;
    }
    
    //返回报表repository的路径。如果rptRepositoryType=0，此时返回的是报表文件s所在的路径，该路径是classes下的相对路径。1：表名
    public String getReportRepositoryPath() throws Exception{
    	String path="";
    	path=QueryConfig.getConfig().getString("rptRepositoryPath", "");
    	return path;
    }
    public String getReportRepositoryAbsolutePath(){
    	String rptsPath=QueryConfig.getConfig().getString("rptRepositoryPath", "");
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
				System.out.println("rootP is null!");
				return "";
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
		return rptsPath;
    }
    public void destroyTemplates(){
    	rptTemplates=null;
    	rptTemplatesMap=null;
    }
    
    public void loadUnitsRenders(){
    	System.out.println("载入报表金额单位配置信息：");
    	String unitPath = "";
		URL rootP=TemplatesLoader.class.getClassLoader().getResource(""); 
		if(rootP==null){
			System.out.println("rootP is null!");
			return;
		}
		try{
			unitPath=rootP.toURI().getPath();
		}catch(Throwable e){
			System.out.println("toURI转换错误："+e.toString());
			unitPath=rootP.getPath();
			unitPath = unitPath.replaceAll("%20", " ");
		}
		System.out.println("unit path:"+unitPath);
		InputStream is=null;
		try{
			//根据路径，获取其中所有的设计文件。
			java.io.File dir=new java.io.File(unitPath);
			File rptFile=new File((unitPath.endsWith("/")?unitPath:unitPath+"/")+"units.xml"); 
			is=new FileInputStream(rptFile) ;
			long contentLength = rptFile.length();
			byte[] ba = new byte[(int)contentLength];
			is.read(ba);
			String unitInfo = new String(ba,"utf-8");
			is.close();
			//设计内容，解析后形成Report对象，存于系统中
			Unit un=null;
			try{
				SAXReader reader = new SAXReader();
			    Document doc = reader.read(new ByteArrayInputStream(unitInfo.getBytes("utf-8")));
			    Element root = doc.getRootElement();
			    if(root==null)
			    	return ;
			    units = new ArrayList();
		    	unitsMap = new HashMap();
				for(Iterator it=root.elementIterator("unit");it.hasNext();){
					Element unode=(Element)it.next();
					un=new Unit();
				    un.setId(unode.attributeValue("id"));
				    un.setName(unode.attributeValue("name"));
				    un.setRenderFun(unode.attributeValue("renderFun"));
				    units.add(un);
				    unitsMap.put(un.getId(), un);
				}
			}catch(Exception e){
				System.out.println();
			}
		}catch(Exception e){
			if(is!=null){
				try{
					is.close();
				}catch(Exception ex){};
			}
			System.out.print("加载单位信息时发生错误："+e.toString());
		}finally{
			try{
				is.close();
			}catch(Exception e){
			}
		}
    }
    
    public List getUnits(){
    	if(units==null){
    		loadUnitsRenders();
    	}
    	return units;
    }
    public Map getUnitsMap(){
    	if(unitsMap==null){
    		loadUnitsRenders();
    	}
    	return unitsMap;
    }
    /**
     * 2014-06-04 加载图表报表的模板信息
     */
    public void loadChartTemplates(String chartPath){
    	System.out.println("chartPath :"+chartPath);
		if(chartPath==null||"".equals(chartPath)){
			System.out.print("图表目录未指定或为空，没有要加载的图表模板！");
			return;
		}
		String pre=chartPath.substring(0,1);
		String chartPathType = QueryConfig.getConfig().getString("chartPathType", "relative");
		if("relative".equals(chartPathType)){//相对路径模式
			if(!"/".equals(pre)){
				chartPath="/"+chartPath;
			}
			URL rootP=TemplatesLoader.class.getClassLoader().getResource(chartPath); 
			if(rootP==null){
				return ;
			}
			try{
				chartPath=rootP.toURI().getPath();
			}catch(Throwable e){
				System.out.println("toURI转换错误："+e.toString());
				chartPath=rootP.getPath();
				chartPath = chartPath==null?"":chartPath.replaceAll("%20", " ");
			}
		}
		if(StringUtils.isEmpty(chartPath)){
			return;
		}
		System.out.println("图表报表路径:"+chartPath);
		List chartPaths=new ArrayList();
		charts=new ArrayList();
		chartsMap=new HashMap();
		InputStream is=null;
		try{
			java.io.File dir=new java.io.File(chartPath);
			getAllFilesPath(dir,chartPaths);
			System.out.println("共找到"+chartPaths.size()+"个图表模板文件！");
			//各个图表设计文件循环解析、加载。
			if(chartPaths!=null&&chartPaths.size()>0){
				for(int i=0;i<chartPaths.size();i++){
					String xmlPath=(String)chartPaths.get(i);
					File chartFile=new File(xmlPath); 
					String fname = chartFile.getName();
					if(!StringUtils.isEmpty(fname)){
						int index = fname.indexOf(".");
						if(index>=0){
							if(!"xml".equals(fname.substring(index+1, fname.length()).toLowerCase())){
								continue;
							}
							fname = fname.substring(0, index);
						}
					}
					is=new FileInputStream(chartFile) ;
					long contentLength = chartFile.length();
					byte[] ba = new byte[(int)contentLength];
					is.read(ba);
					String chartDesignInfo = new String(ba,"utf-8");
					is.close();
					String chartXml=null;
					try{
						//chartXml=loadChartTemplate(chartDesignInfo);
						chartXml = chartDesignInfo;
					}catch(Exception e){
						System.out.println(e.toString());
					}
					if(chartXml==null){
						continue;
					}
					charts.add(chartXml);
					chartsMap.put(fname, chartXml);
				}
				System.out.println("共解载入"+rptTemplates.size()+"个图表报表模板！");
			}
		}catch(Exception e){
			if(is!=null){
				try{
					is.close();
				}catch(Exception ex){};
			}
			System.out.print("载入图表报表模板信息时发生错误："+e.toString());
		}finally{
			try{
				is.close();
			}catch(Exception e){
				
			}
		}
    }
    public List getCharts(){
    	if(charts==null){
    		int type=0;
	    	String sType=QueryConfig.getConfig().getString("chartRepositoryType", "0");
	    	try{
	    		type=Integer.parseInt(sType);
	    	}catch(Exception e){
	    		type=0;
	    	}
	    	String path=QueryConfig.getConfig().getString("chartRepositoryPath", "");
	    	if(type==0){
	    		loadChartTemplates(path);
    		}
    	}
    	return charts;
    }
   
	public Map getChartsMap(){
    	if(chartsMap==null){
    		int type=0;
	    	String sType=QueryConfig.getConfig().getString("chartRepositoryType", "0");
	    	try{
	    		type=Integer.parseInt(sType);
	    	}catch(Exception e){
	    		type=0;
	    	}
	    	String path=QueryConfig.getConfig().getString("chartRepositoryPath", "");
	    	if(type==0){
	    		loadChartTemplates(path);
    		}
    	}
    	return chartsMap;
    }
	public static void main(String[] args) throws Exception {  
        System.out.println("1:"+Thread.currentThread().getContextClassLoader().getResource(""));  
  
        System.out.println("2:"+TemplatesLoader.class.getClassLoader().getResource(""));  
  
        System.out.println("3:"+ClassLoader.getSystemResource("reportsEdit"));  
        System.out.println("4:"+TemplatesLoader.class.getResource(""));  
        System.out.println("5:"+TemplatesLoader.class.getResource("/"));
        //Class文件所在路径
        System.out.println("6:"+new File("yourconfig/yourconf.properties").getAbsolutePath());  
        System.out.println("7:"+System.getProperty("user.dir"));  
    }  
}
