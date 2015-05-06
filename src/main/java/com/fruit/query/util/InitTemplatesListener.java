package com.fruit.query.util;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.servlet.ServletContextEvent; 
import javax.servlet.ServletContextListener; 
import com.fruit.query.parser.*;
/**
 * 
 * @author wxh
 *2009-3-12
 *TODO 初始加载报表模板
 */
public class InitTemplatesListener implements ServletContextListener{ 
    /**
     * 监听，加载报表模板。
     */
	public void contextInitialized(ServletContextEvent event){ 
    	TemplatesLoader ltmp=TemplatesLoader.getTemplatesLoader();
    	try{
    		int repoType=ltmp.getReportRepositoryType();
    		String path=ltmp.getReportRepositoryPath();
    		if(repoType==0){
    			ltmp.loadTemplatesFromFile(path);
    		}else{
    			ltmp.loadTemplatesFromDb(path);
    		}
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    	//2013-08-22 增加显示单位的配置信息
		ltmp.loadUnitsRenders();
		
		//2014-06-04 增加图表模板的载入
    	int type=0;
    	String sType=QueryConfig.getConfig().getString("chartRepositoryType", "0");
    	try{
    		type=Integer.parseInt(sType);
    	}catch(Exception e){
    		type=0;
    	}
    	String path=QueryConfig.getConfig().getString("chartRepositoryPath", "chartTemplates");
    	if(type==0){
    		ltmp.loadChartTemplates(path);
		}
    } 

    public void contextDestroyed(ServletContextEvent event){ 
    } 
} 

