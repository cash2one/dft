package com.fruit.query.service;

import java.util.logging.Logger;

import org.json.*;

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
		StringBuffer strPls = new StringBuffer("{result:true,portlets:");
		strPls.append(jpls==null?"[]":jpls.toString()).append("}");
		return strPls.toString();
	}
	//按portal配置信息生成ext的panel成员
	public JSONArray parse2Portlets(String strPls)throws Exception {
		JSONObject jp = new JSONObject(strPls);
		
		return null;
	}
	//根据id获取portal配置信息
	public String getPortalByID(String pid)throws Exception{
		return "";
	}
}
