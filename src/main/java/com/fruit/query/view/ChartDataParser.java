package com.fruit.query.view;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.fruit.query.data.ChartDataInfo;
import com.fruit.query.data.ChartDataSet;
import com.fruit.query.parser.TemplatesLoader;
import com.fruit.query.report.Chart;
import com.fruit.query.util.QueryConfig;
import org.apache.commons.lang.StringUtils;

public class ChartDataParser {
	private static ChartDataParser parser;
	private ChartDataParser(){
	}
	public static ChartDataParser getParser(){
		if(parser==null)
			parser=new ChartDataParser();
		return parser;
	}
	private static Logger log = Logger.getLogger(ChartDataParser.class);
	
	public String buildXmlData(Chart chart,ChartDataInfo dt){
		String xml = "";
		Map charts = TemplatesLoader.getTemplatesLoader().getChartsMap();
		if(charts==null){
			return "";
		}
		String strTemplate = (String)charts.get(chart.getDataTemplateName());
		if(StringUtils.isEmpty(strTemplate)){
			return "";
		}
		SAXReader reader = new SAXReader();
		Document doc = null;
		try{
		    doc = reader.read(new ByteArrayInputStream(strTemplate.getBytes("utf-8")));
		    if(chart.getIsMultiSeries()==0){
		    	buildSingleSeriesData(doc,dt);
		    }else{
		    	buildMultiSeriesData(doc,dt,chart);
		    }
		    xml = doc.asXML();
		}catch(Exception e){
			log.error(e.toString());
		}
		System.out.println(xml);
		return xml;
	}
	
	private void buildMultiSeriesData(Document doc,ChartDataInfo dt,Chart chart) {
		if(doc==null||dt ==null){
			return ;
		}
		Element root = doc.getRootElement();
		if(root==null){
			return ;
		}
		//如果原模板中无categories节点，创建一个，如已有，则清空其下的内容
		Element ctsNode = root.element("categories");
		if(ctsNode==null){
			ctsNode = root.addElement("categories");
		}else{
			List ctNodes = ctsNode.elements();
			clearNodes(ctsNode,ctNodes);
		}
		//循环创建category节点
		List cts = dt.getCategories();
		if(cts!=null){
			for(int i =0;i<cts.size();i++){
				String ct = (String)cts.get(i);
				Element ctnode = ctsNode.addElement("category");
				ctnode.addAttribute("label", ct);
			}
		}
		//如果原模板中已有dataset节点，清空之
		List dsNodes = root.elements("dataset");
		clearNodes(root,dsNodes);
		Map rmap = chart.getRenderMap();
		//循环创建dataset节点
		List dts = dt.getDataSets();
		if(dts!=null){
			for(int i =0;i<dts.size();i++){
				ChartDataSet cd = (ChartDataSet)dts.get(i);
				Element dsnode = root.addElement("dataset");
				String sname = cd.getSeriesName();
				dsnode.addAttribute("seriesName", sname);
				//2015-08-20 for Combination Charts
				if(rmap!=null&&rmap.containsKey(sname)){
					String r = (String)rmap.get(sname);
					dsnode.addAttribute("renderAs", r);
				}
				if(cts!=null){
					Map data = cd.getData();
					for(int j=0;j<cts.size();j++){
						String ct = (String)cts.get(j);
						String v=(String)data.get(ct);
						Element snode = dsnode.addElement("set");
						snode.addAttribute("value", v);
					}
				}
			}
		}
	}
	
	private void buildSingleSeriesData(Document doc,ChartDataInfo dt) {
		if(doc==null||dt ==null){
			return ;
		}
		Element root = doc.getRootElement();
		if(root==null){
			return ;
		}
		//如已有set节点，则清空其下的内容
		List sNodes = root.elements("set");
		clearNodes(root,sNodes);
		List cts = dt.getCategories();
		List dts = dt.getDataSets();
		if(cts!=null&&dts!=null){
			ChartDataSet cds = (ChartDataSet)dts.get(0);
			Map data = cds.getData();
			for(int i=0;i<cts.size();i++){
				String ct = (String)cts.get(i);
				String v=(String)data.get(ct);
				Element snode = root.addElement("set");
				snode.addAttribute("label", ct);
				snode.addAttribute("value", v);
			}
		}
	}
	
	private boolean clearNodes(Element pnode,List nodes){
		if(nodes!=null&&nodes.size()>0){
			for(int i=0;i<nodes.size();i++){
				Element de = (Element)nodes.get(i);
				pnode.remove(de);
			}
		}
		return true;
	}
	
	public String buildXmlDataForVelocity(Chart chart,ChartDataInfo dt,HttpServletResponse response){
		if(chart==null||dt ==null){
			return "";
		}
		String xml = "";
		String tmpName = chart.getDataTemplateName();
		if(StringUtils.isEmpty(tmpName)){
			return "";
		}
		String chartPathType = QueryConfig.getConfig().getString("chartPathType", "relative");
		if("relative".equals(chartPathType)){
			Properties p=new Properties(); 
			p.setProperty("file.resource.loader.cache","true");
			p.setProperty("resource.loader", "class");
	        p.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
	        Velocity.init(p);
		}else{
			Properties p=new Properties(); 
			p.setProperty("file.resource.loader.cache","true");
			p.setProperty("resource.loader", "file");
	        p.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.FileResourceLoader");
	        Velocity.init(p);  
		}
		try{
			String chartsPath = QueryConfig.getConfig().getString("chartRepositoryPath", "chartTemplates");
			boolean endSlash=chartsPath.endsWith("/");
			StringBuffer absTmpName = new StringBuffer(chartsPath);
			if(!endSlash){
				absTmpName.append("/").append(tmpName);
			}else{
				absTmpName.append(tmpName);
			}
			absTmpName.append(".vm");
			//模板
			Template template = null;  
            try {  
                template = Velocity.getTemplate(absTmpName.toString(),"UTF-8");  
            } catch (ResourceNotFoundException rnfe) {  
               log.error("未找到指定的图表模板："  + absTmpName.toString());  
            } catch (ParseErrorException pee) {  
            	log.error("解析图表模板发生错误，模板：" + absTmpName.toString() + "。错误信息:" + pee);  
            }
            VelocityContext context = new VelocityContext(); 
            addContext(context,chart,dt);
            
            response.setContentType("text/xml;charset=UTF-8");
            PrintWriter writer = response.getWriter();
            BufferedWriter logger = new BufferedWriter(  new OutputStreamWriter(System.out));  
            if (template != null){
            	template.merge(context, writer); 
            	template.merge(context, logger); 
            }
            writer.flush();  
            writer.close(); 
            logger.flush();
            logger.close();
		}catch(Exception e){
			log.error(e.toString());
		}
		System.out.println(xml);
		return xml;
	}
	private void addContext(VelocityContext context,Chart chart,ChartDataInfo chartDt){
		List cts = chartDt.getCategories();
		context.put("categories", cts); 
		List dts = chartDt.getDataSets();
		context.put("series", dts);
	}
}
