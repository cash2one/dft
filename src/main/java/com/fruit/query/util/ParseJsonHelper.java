package com.fruit.query.util;

import java.util.*;

import org.apache.log4j.Logger;
import org.json.*;

import com.fruit.query.util.ParseReportException;
import com.fruit.query.data.Row;
import java.lang.reflect.*;

public class ParseJsonHelper {
	private static Logger log = Logger.getLogger(ParseJsonHelper.class);
	private static ParseJsonHelper instance = null;
    public ParseJsonHelper(){
    	
    }
    public static ParseJsonHelper getParseJsonHelper(){
    	if(instance==null){
    		instance = new ParseJsonHelper();
    	}
    	return instance;
    }
	/** 
     * 代理类时做的检查.返回应该检查的对象.
     * @param bean
     * @return
     */
    protected Object proxyCheck(Object bean){
        return bean;
    }

    public static String toJSONString(Object obj) throws JSONException{
        return toJSONString(obj, false);
    }
    
    public static String toJSONString(Object obj, boolean useClassConvert) throws JSONException{
        if(instance == null)
            instance = new ParseJsonHelper();
        return instance.getJSONObject(obj, useClassConvert).toString();
    }
    /**
     * 有序集合对象的转换
    * @param arrayObj
    * @param useClassConvert
    * @return
    * @throws JSONException
     */
    private String getJSONArray(Object arrayObj, boolean useClassConvert) throws JSONException{
        if(arrayObj == null)
            return "null";
        arrayObj = proxyCheck(arrayObj);
        JSONArray jSONArray = new JSONArray();
        if(arrayObj instanceof Collection){//集合内元素循环
            Iterator iterator = ((Collection)arrayObj).iterator();
            while(iterator.hasNext()){
                Object rowObj = iterator.next();
                if(rowObj == null){
                    jSONArray.put(new JSONStringObject(null));
                } else if(rowObj.getClass().isArray() || rowObj instanceof Collection){//嵌套集合元素，递归
                    jSONArray.put(getJSONArray(rowObj, useClassConvert));
                }else{
                    jSONArray.put(getJSONObject(rowObj, useClassConvert));
                }
            }
        }
        if(arrayObj.getClass().isArray()){
            int arrayLength = Array.getLength(arrayObj);
            for(int i = 0; i < arrayLength; i ++){
                Object rowObj = Array.get(arrayObj, i);
                if(rowObj == null){
                    jSONArray.put(new JSONStringObject(null));
            	}else if(rowObj.getClass().isArray() || rowObj instanceof Collection){
                    jSONArray.put(getJSONArray(rowObj, useClassConvert));
                }else{
                    jSONArray.put(getJSONObject(rowObj, useClassConvert));
                }
            }
        }
        return jSONArray.toString();
    }

    public JSONStringObject getJSONObject(Object value, boolean useClassConvert) throws JSONException{
        //处理原始类型
        if (value == null) {
            return new JSONStringObject("null");
        }
        value = proxyCheck(value);
        if (value instanceof JSONString) {
            Object o;
            try {
                o = ((JSONString)value).toJSONString();
            } catch (Exception e) {
                throw new JSONException(e);
            }
            throw new JSONException("Bad value from toJSONString: " + o);
        }
        if (value instanceof Number) {
            return new JSONStringObject(JSONObject.numberToString((Number) value));
        }
        if (value instanceof Boolean || value instanceof JSONObject ||
                value instanceof JSONArray) {
            return new JSONStringObject(value.toString());
        }
        if (value instanceof String)
            return new JSONStringObject(JSONObject.quote(value.toString()));
        if (value instanceof Map) {
            JSONObject jSONObject = new JSONObject();
            Iterator iterator = ((Map)value).keySet().iterator();
            while(iterator.hasNext()){
                String key = iterator.next().toString();
                Object valueObj = ((Map)value).get(key);
                jSONObject.put(key, getJSONObject(valueObj, useClassConvert));
            }
            return new JSONStringObject(jSONObject.toString());
        }
        //class
        if(value instanceof Class)
            return new JSONStringObject(JSONObject.quote(((Class)value).getName()));
        //数组
        if (value instanceof Collection || value.getClass().isArray()) {
            return new JSONStringObject(getJSONArray(proxyCheck(value), useClassConvert));
        }
        return reflectObject(value, useClassConvert);
    }

    private JSONStringObject reflectObject(Object bean, boolean useClassConvert){
        JSONObject jSONObject = new JSONObject();

        Class klass = bean.getClass();
        Method[] methods = klass.getMethods();
        for (int i = 0; i < methods.length; i += 1) {
            try {
                Method method = methods[i];
                String name = method.getName();
                String key = "";
                if (name.startsWith("get")) {
                    key = name.substring(3);
                } else if (name.startsWith("is")) {
                    key = name.substring(2);
                }
                if (key.length() > 0 &&
                        Character.isUpperCase(key.charAt(0)) &&
                        method.getParameterTypes().length == 0) {
                    if (key.length() == 1) {
                        key = key.toLowerCase();
                    } else if (!Character.isUpperCase(key.charAt(1))) {
                        key = key.substring(0, 1).toLowerCase() +
                            key.substring(1);
                    }
                    Object elementObj = method.invoke(bean, null);
                    if(!useClassConvert && elementObj instanceof Class)
                        continue;

                    jSONObject.put(key, getJSONObject(elementObj, useClassConvert));
                }
            } catch (Exception e) {
                /**//* forget about it */
            }
        }
        return new JSONStringObject(jSONObject.toString());
    }
    
    /**
	 * 解析数据结合为json格式
	 * @param count
	 * @param rows
	 * @return
	 * @throws Exception
	 */
	public String parseJsonRowsPaging(int count,List rows)throws Exception{
		String jstr="";
		if(rows==null||rows.size()==0){
			return "{rows:[],totalCount:0}";
		}
		JSONObject jobj=new JSONObject();
		JSONArray jrows=new JSONArray();
		try{
			jobj.put("totalCount", count);
			for(int i=0;i<rows.size();i++){
				Row row=(Row)rows.get(i);
				Map cells=row.getCells();
				if(cells==null){
					continue;
				}
				JSONObject jrow=new JSONObject();
				jrow.put("lineNum", i+1);
				Iterator it=cells.entrySet().iterator();
				while(it.hasNext()){
					Map.Entry pairs =(Map.Entry)it.next(); 
					String col=(String)pairs.getKey();
					String val=(String)pairs.getValue();
					jrow.put(col, val);
				}
				jrows.put(jrow);
			}
			jobj.put("rows", jrows);
		}catch(Exception e){
			System.out.print(e.toString());
		}
		jstr=jobj.toString();
		return jstr;
	}
	
	public String parseJsonRows(List rows)throws Exception{
		String jstr="";
		if(rows==null||rows.size()==0)return "[]";
		JSONArray jrows=new JSONArray();
		try{
			for(int i=0;i<rows.size();i++){
				Row row=(Row)rows.get(i);
				Map cells=row.getCells();
				if(cells==null){
					continue;
				}
				JSONObject jrow=new JSONObject();
				jrow.put("lineNum", String.valueOf(i+1));
				Iterator it=cells.entrySet().iterator();
				while(it.hasNext()){
					Map.Entry pairs =(Map.Entry)it.next(); 
					String col=(String)pairs.getKey();
					String val=(String)pairs.getValue();
					jrow.put(col, val);
				}
				jrows.put(jrow);
			}
		}catch(Exception e){
			System.out.println(e.toString());
		}
		jstr=jrows.toString();
		return jstr;
	}
	
	public static void main(String[] args){
		
	}
}
