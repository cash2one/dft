package com.fruit.query.util;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
/**
 * 
 * @author wxh
 *2009-3-19
 *TODO 获取本查询模块所需的配置信息
 */
public class QueryConfig {
	private static QueryConfig systemConfig = null;
	private static ResourceBundle resources = null;
	private static ResourceBundle chartResources = null;
	private QueryConfig()
    {
      try
        {
            resources = ResourceBundle.getBundle("Resource", Locale.getDefault());
            chartResources = ResourceBundle.getBundle("chartType", Locale.getDefault());
        }
        catch(MissingResourceException mre)
        {
          System.out.println(mre.toString());
        }

    }
	/**
	 * 获取配置信息
	 * @return 配置信息集合对象
	 */
    public static QueryConfig getConfig()
    {
        if(systemConfig == null)
            systemConfig = new QueryConfig();
        return systemConfig;
    }
    //检查是否获得正确的资源文件
    private static boolean checkResources()
    {
        boolean result = true;
        if(resources == null)
        {
            result = false;
        }
        return result;
    }

   /**
    * 获取指定配置项的值
    * @param key 配置项名
    * @param defaultValue 默认值。
    * @return 配置项的值。如找不到该项，则使用默认值。
    */
    public String getString(String key, String defaultValue)
    {
        String result = null;
        try
        {
            if(checkResources())
                result = resources.getString(key);
            else
                result = defaultValue;
        }
        catch(MissingResourceException mre)
        {
            result = defaultValue;
        }
        return result;
    }
    /**
     * 获取指定配置项的值。
     * 该方法不提供默认值。
     * @param key 配置项名
     * @return 配置项的值。
     */
    public String getString(String key){
        return getString(key, null);
    }
    
    public ResourceBundle getChartTypes(){
    	return chartResources;
    }
    //2014-06-03 根据指定的图表报表名称查找对应的文件名
    public String getChartFile(String chartType){
    	String result = null;
        try{
        	result = chartResources.getString(chartType);
        }
        catch(MissingResourceException mre){
            result = "";
        }
        return result;
    }
}
