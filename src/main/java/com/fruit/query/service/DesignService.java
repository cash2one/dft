package com.fruit.query.service;

import java.util.*;

public class DesignService {
	private static DesignService dService;
	private static Map tmplatesMap = new HashMap();
	private static List tmplates = new ArrayList();
	private DesignService(){
	}
	public static synchronized DesignService getDesignService(){
		if(dService==null){
			dService=new DesignService();
		}
		return dService;
	}
	/**
	 * 
	* @param forceReload :本次取模板是否强制重新加载。0：否，1：是。默认：否
	* 一般情况下，模板有修改，会及时同步更新已经载入的模板内存模型。不需要强制重新加载。
	* @return
	 */
	public List getTmplates(int forceReload){
		synchronized(this){
			if(tmplates==null||forceReload==1){
				
			}
		}
		return tmplates;
	}
	
	public Map getTmplatesMap(int forceReload){
		synchronized(this){
			if(tmplatesMap==null||forceReload==1){
				
			}
		}
		return tmplatesMap;
	}
	private void loadRptTemplates(){
		
	}
	public synchronized boolean saveRptBaseInfo(){
		boolean saved  =  false;
		return saved ;
	}
	
	public synchronized boolean saveRptParams(){
		boolean saved  =  false;
		return saved ;
	} 
}
