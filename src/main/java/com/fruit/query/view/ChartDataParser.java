package com.fruit.query.view;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.fruit.query.data.ChartDataInfo;
import com.fruit.query.data.ChartDataSet;
import com.fruit.query.parser.TemplatesLoader;
import com.fruit.query.report.Chart;
import com.softwarementors.extjs.djn.StringUtils;

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
		    	buildMultiSeriesData(doc,dt);
		    }
		    xml = doc.asXML();
		}catch(Exception e){
			log.error(e.toString());
		}
		return xml;
	}
	
	private void buildMultiSeriesData(Document doc,ChartDataInfo dt) {
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
		//循环创建dataset节点
		List dts = dt.getDataSets();
		if(dts!=null){
			for(int i =0;i<dts.size();i++){
				ChartDataSet cd = (ChartDataSet)dts.get(i);
				Element dsnode = root.addElement("dataset");
				String sname = cd.getSeriesName();
				dsnode.addAttribute("seriesName", sname);
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
}
