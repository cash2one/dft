package com.fruit.query.service;

import org.json.*;

import com.fruit.query.parser.TemplatesLoader;
import com.fruit.query.report.Report;

public class PortalService {
	private static PortalService portalService;
	private PortalService(){
	}
	public static PortalService getPortalService(){
		if(portalService==null)
			portalService=new PortalService();
		return portalService;
	}
	public String loadPortlets(String portalID) {
		String strPdesign = "";
		try{
			strPdesign = getPortalByID(portalID);
		}catch(Exception e){
			System.out.println(e.toString());
			return "{result:false,info:'加载portal设计信息时发生错误!'}";
		}
		JSONArray jpls = null;
		try{
			jpls = parse2Portlets(strPdesign);
		}catch(Exception e){
			System.out.println(e.toString());
			return "{result:false,info:'解析portal设计信息时发生错误!'}";
		}
		StringBuffer strPls = new StringBuffer("{result:true,columns:");
		strPls.append(jpls==null?"[]":jpls.toString()).append("}");
		return strPls.toString();
	}
	//按portal配置信息生成ext的panel成员
	public JSONArray parse2Portlets(String strPls)throws Exception {
		JSONObject jp = new JSONObject(strPls);
		//默认列宽和面板高度
		float dfColumnWidth =(float)(Math.round(100/jp.getInt("colCount")))/100;
		int dfHeight = jp.has("defaultHeight")?jp.getInt("defaultHeight"):200;
		JSONArray jcols = jp.getJSONArray("columns");
		JSONArray colpanels = null;
		if(jcols!=null&&jcols.length()>0){
			//按列循环
			colpanels = new JSONArray();
			for(int i=0;i<jcols.length();i++){
				JSONObject jcol = jcols.getJSONObject(i);
				JSONObject colpanel = new JSONObject();
				colpanel.put("columnWidth", jcol.has("columnwidth")?jcol.getDouble("columnwidth"):dfColumnWidth);
				JSONArray jptls = jcol.getJSONArray("items");
				JSONArray ptls = null;
				if(jptls!=null){
					//一个列中的portlet面板循环
					ptls = new JSONArray();
					for(int j = 0;j<jptls.length();j++){
						JSONObject jptl = jptls.getJSONObject(j);
						JSONObject ptl = new JSONObject();
						ptl.put("layout", "fit");
						ptl.put("height", jptl.has("height")?jptl.getInt("height"):dfHeight);
						ptl.put("title", jptl.has("title")?jptl.getString("title"):"");
						String ptlType = ptl.has("type")?ptl.getString("type"):"text";
						ptl.put("ptype", ptlType);
						String id = "";
						if(ptl.has("id")){
							id = ptl.getString("id");
						}else if("report".equals(ptlType)){
							id = "report_"+i+j;
						}else if("chart".equals(ptlType)){
							id = "chart_"+i+j;
						}else{
							id = "text_"+i+j;
						}
						ptl.put("id", id);
						String content = ptl.getString("content");
						parsePortletContent(ptl,content,ptlType);
						ptls.put(ptl);
					}
				}
				colpanel.put("items", ptls);
			}
		}
		return colpanels;
	}
	//根据类型解析portlet的具体内容
	public void parsePortletContent(JSONObject ptl,String content,String ptlType)throws Exception{
		if("text".equals(ptlType)){
			ptl.put("html", content);
		}else if("report".endsWith(ptlType)){
			ptl.put("items", "grid_"+content);
		}else if("chart".equals(ptlType)){
			ptl.put("items", "chart_"+content);
		}
	}
	
	//根据id获取portal配置信息
	public String getPortalByID(String pid)throws Exception{
		StringBuffer jstr = new StringBuffer("{id:'test',name:'测试',total:'5',colCount:'3',defaultHeight:'200',columns:[");
		jstr.append("{'columnwidth':'.33',items:[");
		jstr.append("{id:'text',title:'面板1',height:'300',type:'text',content:'just a minute!'},");
		jstr.append("{id:'tax',title:'面板2',height:'',type:'report',content:'En_tax'}] },");
		jstr.append("{'columnwidth':'.33',items:[");
		jstr.append("{title:'面板3',height:'',type:'chart',content:'testChart'},");
		jstr.append("{title:'面板4',height:'400',type:'report',content:'dj'}]");
		jstr.append("},{'columnwidth':'.33',items:[");
		jstr.append("{title:'面板5',height:'100',type:'text',content:'last one'}]}]}");
		return jstr.toString();
	}
}
