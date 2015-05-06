package com.ifugle.dft.utils;

import java.util.*;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.ParameterizedBeanPropertyRowMapper;
import com.ifugle.dft.check.entity.En_field;
import com.ifugle.dft.system.dao.CodeDao;
import com.ifugle.dft.treasury.entity.TreasuryImpMap;
import com.ifugle.dft.utils.entity.SystemSet;

public class Configuration {
	protected JdbcTemplate jdbcTemplate;
	@Autowired
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate){
		this.jdbcTemplate = jdbcTemplate;
	}
	private static Logger log = Logger.getLogger(Configuration.class);
	private static Configuration systemConfig = null;
	private List systemSets = null;
	private Map mapSystemSets = null;
	private Map fieldsMapByTable = null;
	private Map fieldsMapByTableShow = null;
	private Map financeFieldsMap= null;
	private String[] defaultDJfldsArray = null;
	private Map treasuryImpMaps = null;
	private static ResourceBundle resources = null;
	//所有有列表数据查询的handler都应该向这个map注册自己。便于系统“自动感知”各个handler，便于导出excel的反射
	private Map handlersMap = new HashMap();
	private Configuration(){
		try{
            resources = ResourceBundle.getBundle("Resource", Locale.getDefault());
        }
        catch(MissingResourceException mre){
          System.out.println(mre.toString());
        }
	}
	public static Configuration getConfig(){
        if(systemConfig == null)
            systemConfig = new Configuration();
        return systemConfig;
    }
	
	 public String getString(String key, String defaultValue){
	    String result = null;
	    try{
	        if(mapSystemSets==null){
	        	loadSystemSets();
	        }
	        if(mapSystemSets!=null){
	        	SystemSet sysSet = (SystemSet)mapSystemSets.get(key);
	        	result = sysSet==null?null:sysSet.getIvalue();
	        }
	        if(result==null){
	        	result=resources.getString(key);
	        }
	        if(result==null){
	        	result = defaultValue;
	        }
	    }catch(Exception e){
	        result = defaultValue;
	    }
	    return result;
	}

	public String getString(String key){
	    return getString(key, null);
	}
	//加载系统设置
	private void loadSystemSets(){
		Object[] params = null;
		StringBuffer sql = new StringBuffer("select item,iname,ivalue,remark from systemset order by item");
		systemSets=jdbcTemplate.query(sql.toString(),params, ParameterizedBeanPropertyRowMapper.newInstance(SystemSet.class));
		if(systemSets!=null&&systemSets.size()>0){
			mapSystemSets = new HashMap();
	    	for(int i=0;i<systemSets.size();i++){
	    		SystemSet ss = (SystemSet)systemSets.get(i);
	    		mapSystemSets.put(ss.getItem(), ss);
	    	}
		}
	}
	
	public void reloadSystemSets(){
		loadSystemSets();
	}
	
	public void loadEn_Dictionary(){
		Object[] params = null;
		StringBuffer sql = new StringBuffer("select distinct(tname) tname from en_dictionary");
		List tbnames=jdbcTemplate.queryForList(sql.toString(),params, String.class);
		if(tbnames!=null&&tbnames.size()>0){
			fieldsMapByTable = new HashMap();
			fieldsMapByTableShow = new HashMap();
	    	for(int i=0;i<tbnames.size();i++){
	    		String tb = (String)tbnames.get(i);
	    		StringBuffer allsql = new StringBuffer("select tname,field,mc,f_type,val_src,mapbm,showmod,isrtk,sort from en_dictionary where tname='");
	    		allsql.append(tb).append("' order by sort");
	    		List allflds = jdbcTemplate.query(allsql.toString(),params, ParameterizedBeanPropertyRowMapper.newInstance(En_field.class));;
	    		fieldsMapByTable.put(tb, allflds);
	    		StringBuffer showsql = new StringBuffer("select tname,field,mc,f_type,val_src,mapbm,showmod,isrtk,sort from en_dictionary where tname='");
	    		showsql.append(tb).append("' and showmod>0 order by sort");
	    		List showflds = jdbcTemplate.query(showsql.toString(),params, ParameterizedBeanPropertyRowMapper.newInstance(En_field.class));
	    		fieldsMapByTableShow.put(tb, showflds);
	    	}
		}
	}
	public void reloadEn_Dictionary(){
		loadEn_Dictionary();
	}
	public Map getDJDictionary(){
		if(fieldsMapByTable==null){
			loadEn_Dictionary();
		}
		return fieldsMapByTable;
	}
	
	public Map getDJFieldsToShow(){
		if(fieldsMapByTableShow==null){
			loadEn_Dictionary();
		}
		return fieldsMapByTableShow;
	}
	/**
	 * 跟据字典配置获取默认的表头
	* @return
	 */
	public String[] getDefaultDJfldsArray() {
		if(defaultDJfldsArray==null){
			if(fieldsMapByTableShow==null){
				loadEn_Dictionary();
			}
			if(fieldsMapByTableShow==null){
				return null;
			}
			List showflds = (List)fieldsMapByTableShow.get("DJ_CZ");
			if(showflds==null||showflds.size()==0){
				return null;
			}
			defaultDJfldsArray = new String[showflds.size()];
			for(int i=0;i<showflds.size();i++){
				En_field fld = (En_field)showflds.get(i);
				defaultDJfldsArray[i]=fld.getField();
			}
			
		}
		return defaultDJfldsArray;
	}
	
	public String[] getDefaultFldsArray() {
		String[] heads=new String[]{"XH","SWDJZH","MC","CZFPBM","FDDBR","DZ"};
		return heads;
	}
	
	public Map getFinanceFieldsMap(){
		if(financeFieldsMap==null){
			if(fieldsMapByTable==null){
				loadEn_Dictionary();
			}
			if(fieldsMapByTable!=null&&fieldsMapByTable.get("DJ_CZ")!=null){
				financeFieldsMap = new HashMap();
				List allFlds = (List)fieldsMapByTable.get("DJ_CZ");
				for(int i=0;i<allFlds.size();i++){
					En_field fld = (En_field)allFlds.get(i);
					financeFieldsMap.put(fld.getField(), fld);
				}
			}
		}
		return financeFieldsMap;
	}
	public Map getHandlersMap(){
		return handlersMap;
	}

	public Map getTreasuryImpMaps() {
		if(treasuryImpMaps==null){
			loadTreasuryImpMaps();
		}
		return treasuryImpMaps;
	}

	@SuppressWarnings("unchecked")
	private void loadTreasuryImpMaps() {
		Object[] params = null;
		StringBuffer sql = new StringBuffer("select distinct skgkdm_jk,mtype from yssr_imp_set");
		List jks=jdbcTemplate.query(sql.toString(),params, ParameterizedBeanPropertyRowMapper.newInstance(TreasuryImpMap.class));
		if(jks!=null&&jks.size()>0){
			treasuryImpMaps = new HashMap();
	    	for(int i=0;i<jks.size();i++){
	    		TreasuryImpMap tm = (TreasuryImpMap)jks.get(i);
	    		String jk = tm.getSkgkdm_jk();
	    		int mtype = tm.getMtype();
	    		StringBuffer allsql = new StringBuffer("select skgkdm_jk,mtype,fnode,colname,rptkeyflag,dtype from yssr_imp_set where skgkdm_jk='");
	    		allsql.append(jk).append("' and mtype=").append(mtype);
	    		List mapInfo = jdbcTemplate.query(allsql.toString(),params, ParameterizedBeanPropertyRowMapper.newInstance(TreasuryImpMap.class));
	    		StringBuffer k = new StringBuffer("").append(jk);
	    		k.append("_").append(mtype);
	    		treasuryImpMaps.put(k.toString(), mapInfo);
	    	}
		}
	}
}
