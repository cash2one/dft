package com.fruit.query.service;
import com.fruit.query.data.*;
import com.fruit.query.parser.TemplatesLoader;
import com.fruit.query.report.*;
import com.fruit.query.util.*;

import java.util.*;
import java.util.regex.*;
import java.sql.*;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.*;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * 
 * @author wxh
 *2009-3-16
 *TODO 报表数据的获取
 */
public class RptDataService {
	private static RptDataService rptDataService;
	private RptDataService(){
		
	}
	private static Logger log = Logger.getLogger(RptDataService.class);
	/**
	 * 获取报表取数服务的实例。
	 * singleton，每次调用，获取的是同一个实例。
	 * @return RptDataService实例。
	 */
	public static RptDataService getReportDataService(){
		if(rptDataService==null)
			rptDataService=new RptDataService();
		return rptDataService;
	}
	/**
	 * 2010-08-24 增加方法，如果有替换的sql,用替换sql取数，否则仍用原方法
	* @param rpt
	* @param paraVals
	* @param replaceSql
	* @return
	* @throws RptServiceException
	 */
	public DataSet getReportData(Report rpt,Map paraVals,String replaceSql)throws RptServiceException{
		DataSet dts=null;
		if(rpt==null)return null;
		DataDefine df=rpt.getDefaultDataDef();
		if(df==null&&StringUtils.isEmpty(replaceSql))return null;
		//根据取数方式不同调用不同的方法
		if(!StringUtils.isEmpty(replaceSql)){
			dts=excuteSql(rpt,replaceSql,paraVals);
		}else if(df.getSourceType()==1){
			dts=excuteSql(rpt,df.getSql(),paraVals);	
		}else if(df.getSourceType()==2){
			dts=excuteProcedure(rpt,df,paraVals);
		}else if(df.getSourceType()==3){
			try{
				String path=df.getImplClass();
				IDataService dataInstance=(IDataService)Class.forName(path).newInstance();
				dts=dataInstance.getReportData(rpt, paraVals);
			}catch(Exception e){
				throw new RptServiceException("自定义类取数时发生错误："+e.toString());
			}
		}else{
			//静态数据不在此处取数，日后如有必要再扩充，应在模板加载时获取。
		}
		return dts;
	}
	
	/**
	 * 获取报表记录集。
	 * @param rpt 报表模板，包含报表定义信息。
	 * @param paraVals 本次请求传递的参数，参数名索引ParaValue形成的map。
	 * @return 报表记录集对象 DataSet
	 * @throws RptServiceException
	 */
	public DataSet getReportData(Report rpt,Map paraVals)throws RptServiceException{
		DataSet dts=null;
		if(rpt==null)return null;
		DataDefine df=rpt.getDefaultDataDef();
		if(df==null)return null;
		//根据取数方式不同调用不同的方法
		if(df.getSourceType()==1){
			dts=excuteSql(rpt,df.getSql(),paraVals);
		}else if(df.getSourceType()==2){
			dts=excuteProcedure(rpt,df,paraVals);
		}else if(df.getSourceType()==3){
			try{
				String path=df.getImplClass();
				IDataService dataInstance=(IDataService)Class.forName(path).newInstance();
				dts=dataInstance.getReportData(rpt, paraVals);
			}catch(Exception e){
				throw new RptServiceException("自定义类取数时发生错误："+e.toString());
			}
		}else{
			//静态数据不在此处取数，日后如有必要再扩充，应在模板加载时获取。
		}
		return dts;
	}
	
	public DataSet getReportDataPaging(Report rpt,Map paraVals,String replaceSql,int start,int limit)throws RptServiceException{
		DataSet dts=null;
		if(rpt==null)return null;
		DataDefine df=rpt.getDefaultDataDef();
		if(df==null&&StringUtils.isEmpty(replaceSql))return null;
		//分页时，sql取数，预加工取数sql，限定记录范围。过程取数，取全部记录集的一部分。
		if(!StringUtils.isEmpty(replaceSql)||df.getSourceType()==1){
			String sql="";
			//如果有替代的sql语句，使用替代的
			if(!StringUtils.isEmpty(replaceSql)){
				sql = replaceSql;
			}else{
				sql=df.getSql();
			}
			if(StringUtils.isEmpty(sql))return null;
			//如果有远程排序
			if(rpt.getRemoteSort()==1){
				if(paraVals!=null&&paraVals.get("sort")!=null){
					ParaValue pv=(ParaValue)paraVals.get("sort");
					ParaValue dpv=(ParaValue)paraVals.get("dir");
					sql="SELECT * FROM ("+sql+") ORDER BY "+pv.getValue()+" "+dpv.getValue();
				}
			}
			StringBuffer qSql = new StringBuffer("SELECT * FROM (SELECT A.*, rownum r FROM (");
	        qSql.append(sql);
	        qSql.append(") A WHERE rownum<=");
	        qSql.append((start+limit));
	        qSql.append(") B WHERE r>");
	        qSql.append(start);
	        dts=excuteSql(rpt,qSql.toString(),paraVals);
		}
		return dts;
	}
	/**
	 * 分页数据的获取。
	 * @param rpt 报表模板，包含报表定义信息。
	 * @param paraVals 本次请求传递的参数，参数名索引ParaValue形成的map。
	 * @param start 本次取数范围的起始点。
	 * @param limit 本次取数的记录条数。
	 * @return 指定范围的记录子集。
	 * @throws RptServiceException
	 */
	public DataSet getReportDataPaging(Report rpt,Map paraVals,int start,int limit)throws RptServiceException{
		DataSet dts=null;
		if(rpt==null)return null;
		DataDefine df=rpt.getDefaultDataDef();
		if(df==null)return null;
		//分页时，sql取数，预加工取数sql，限定记录范围。过程取数，取全部记录集的一部分。
		if(df.getSourceType()==1){
			String sql=df.getSql();
			if(sql==null)return null;
			//如果有远程排序
			if(rpt.getRemoteSort()==1){
				if(paraVals!=null&&paraVals.get("sort")!=null){
					ParaValue pv=(ParaValue)paraVals.get("sort");
					ParaValue dpv=(ParaValue)paraVals.get("dir");
					sql="SELECT * FROM ("+sql+") ORDER BY "+pv.getValue()+" "+dpv.getValue();
				}
			}
			StringBuffer qSql = new StringBuffer("SELECT * FROM (SELECT A.*, rownum r FROM (");
	        qSql.append(sql);
	        qSql.append(") A WHERE rownum<=");
	        qSql.append((start+limit));
	        qSql.append(") B WHERE r>");
	        qSql.append(start);
	        dts=excuteSql(rpt,qSql.toString(),paraVals);
		}else if(df.getSourceType()==2){
			paraVals.put("start", new ParaValue(String.valueOf(start),String.valueOf(start)));
			paraVals.put("limit", new ParaValue(String.valueOf(limit),String.valueOf(limit)));
			DataSet allDts=excuteProcedure(rpt,df,paraVals);
			dts=allDts;
			/*if(allDts!=null){
				List rows=allDts.getRows();
				if(rows!=null){
					try{
						int ei=Math.min(rows.size(),start+limit);
						List subRows=rows.subList(start, ei);
						dts=new DataSet();
						dts.setMetaData(allDts.getMetaData());
						dts.setRows(subRows);
					}catch(Exception e){dts=null;}
				}
			}*/
		}else if(df.getSourceType()==3){
			try{
				String path=df.getImplClass();
				IDataService dataInstance=(IDataService)Class.forName(path).newInstance();
				dts=dataInstance.getReportDataPaging(rpt, paraVals, start, limit);
			}catch(Exception e){
				throw new RptServiceException("自定义类取数时发生错误："+e.toString());
			}
		}else{
			//静态数据不在此处取数，日后如有必要再扩充，应在模板加载时获取。
		}
		return dts;
	}
	/**
	 * 分页时，获取总记录数。
	 * @param rpt 报表模板，包含报表定义信息。
	 * @param paraValues 本次请求传递的参数，参数名索引ParaValue形成的map。
	 * @return 记录总数。
	 * @throws RptServiceException
	 */
	public int getTotalCount(Report rpt,Map paraValues)throws RptServiceException{
		int count=0;
		if(rpt==null)return 0;
		DataDefine df=rpt.getDefaultDataDef();
		if(df==null)return 0;
		Connection conn=null;
		PreparedStatement ps=null;
		ResultSet rs=null;
		try{
			if(df.getSourceType()==1){
				String sql=df.getSql();
				if(sql==null)return 0;
				/*2013-12-25增加参数值的直接替换功能，直接替换的参数用[]引用，
				 * 不影响{}的引用方式。
				 * 先处理两旁都有%的，再处理单边有%的，最后处理无%的
				 */
				Map parasDef=rpt.getParasMap();
				String[] rpl2pers = StringUtils.substringsBetween(sql,"%[","]%");
				if(rpl2pers!=null&&rpl2pers.length>0){
					for(int i=0;i<rpl2pers.length;i++){
						sql = replaceParamValue(sql,parasDef,rpl2pers[i],paraValues,2);
					}
				}
				String[] rplFpers=StringUtils.substringsBetween(sql,"%[","]");
				if(rplFpers!=null&&rplFpers.length>0){
					for(int i=0;i<rplFpers.length;i++){
						sql = replaceParamValue(sql,parasDef,rplFpers[i],paraValues,0);
					}
				}
				String[] rplTpers=StringUtils.substringsBetween(sql,"[","]%");
				if(rplTpers!=null&&rplTpers.length>0){
					for(int i=0;i<rplTpers.length;i++){
						sql = replaceParamValue(sql,parasDef,rplTpers[i],paraValues,1);
					}
				}
				String[] rplParas = StringUtils.substringsBetween(sql, "[", "]");
				if(rplParas!=null&&rplParas.length>0){
					for(int i=0;i<rplParas.length;i++){
						sql = replaceParamValue(sql,parasDef,rplParas[i],paraValues,9);
					}
				}
				//如果有参数引用――{abc..}，替换成?，并提取其中的参数
				String[] paras=StringUtils.substringsBetween(sql, "{", "}");
				
				//2009-04-28为适应like中的%%
				String[] has2pers=StringUtils.substringsBetween(sql,"%{","}%");
				sql=sql.replaceAll("%\\{\\w*\\}%","?");
				String[] hasFpers=StringUtils.substringsBetween(sql,"%{","}");
				sql=sql.replaceAll("%\\{\\w*\\}","?");
				//String[] hasTpers=StringUtils.substringsBetween(sql,"{","}%");
				List lstTpers = new ArrayList();
				String tmpSql = sql;
				while(true){
					int end= tmpSql.indexOf("}%");
					if(end<0){
						break;
					}
					String preSql = tmpSql.substring(0,end);
					int start = preSql.lastIndexOf("{");
					String p = preSql.substring(start+1,end);
					tmpSql = tmpSql.substring(end+1);
					lstTpers.add(p);
				}
				String[] hasTpers =lstTpers.size()==0?null:new String[lstTpers.size()]; 
				for(int i=0;i<lstTpers.size();i++){
					hasTpers[i]=(String)lstTpers.get(i);
				}
				sql=sql.replaceAll("\\{\\w*\\}%","?");
				Map paraSearchModes=new HashMap();
				if(has2pers!=null){
					for(int i=0;i<has2pers.length;i++){
						paraSearchModes.put(has2pers[i], "2");
					}
				}
				if(hasFpers!=null){
					for(int i=0;i<hasFpers.length;i++){
						paraSearchModes.put(hasFpers[i], "0");
					}
				}
				if(hasTpers!=null){
					for(int i=0;i<hasTpers.length;i++){
						paraSearchModes.put(hasTpers[i], "1");
					}
				}
				
				sql=sql.replaceAll("\\{\\w*\\}", "?");
				StringBuffer qSql = new StringBuffer("SELECT COUNT(*) AS RCOUNT FROM(");
		        qSql.append(sql);
		        qSql.append(")");
		        conn=DBConnectionFactory.getConnection();
				//如果没有参数引用
				if(paras==null||paras.length==0){
					ps=conn.prepareStatement(qSql.toString());
					log.info(qSql);
					rs=ps.executeQuery();
				}else{
					ps=conn.prepareStatement(qSql.toString());
					log.info(qSql);
					parseSqlParameter(rpt,paraValues,paras,paraSearchModes,ps);
					rs=ps.executeQuery();
				}
				if(rs.next()){
					count=rs.getInt("RCOUNT");
				}
				rs.close();
				ps.close();
				conn.close();
			}else if(df.getSourceType()==2){
				DataSet allDts=excuteProcedure(rpt,df,paraValues);
				if(df.getPagingMode()==1){//取数后分页
					if(allDts!=null&&allDts.getRows()!=null){
						count=allDts.getRows().size();
					}
				}else{//取数前分页 2010-07-01
					if(allDts!=null){
						count = allDts.getTotalCount();
					}
				}
				
			}else if(df.getSourceType()==3){
				try{
					String path=df.getImplClass();
					IDataService dataInstance=(IDataService)Class.forName(path).newInstance();
					count=dataInstance.getTotalCount(rpt, paraValues);
				}catch(Exception e){
					throw new RptServiceException("自定义类取数时发生错误："+e.toString());
				}
			}
		}catch(Exception e){
			try{
	    		if(rs!=null){
	    			rs.close();
	    		}
	    		if(ps!=null){
	    			ps.close();
	    		}
	    		if(conn!=null){
	    			conn.close();
	    		}	
	    	}catch(Exception ex){
	    		
	    	}
	    	throw new RptServiceException("获取数据的总记录数时发生错误。错误信息："+e.toString());
		}finally{
			DBConnectionFactory.endDBConnect(conn, rs, ps);
		}
		return count;
	}
	/**
	 * 分页时，获取总记录数。适用于原始sql被包装过的情况
	 */
	public int getTotalCount(Report rpt,Map paraValues,String replaceSql)throws RptServiceException{
		int count=0;
		if(rpt==null)return 0;
		DataDefine df=rpt.getDefaultDataDef();
		if(df==null)return 0;
		Connection conn=null;
		PreparedStatement ps=null;
		ResultSet rs=null;
		try{
			if(df.getSourceType()==1){
				String sql=df.getSql();
				if(sql==null)return 0;
				//如果有替代的sql语句，使用替代的
				if(replaceSql!=null&&!"".equals(replaceSql)){
					sql = replaceSql;
				}
				/*2013-12-25增加参数值的直接替换功能，直接替换的参数用[]引用，
				 * 不影响{}的引用方式。
				 * 先处理两旁都有%的，再处理单边有%的，最后处理无%的
				 */
				Map parasDef=rpt.getParasMap();
				String[] rpl2pers = StringUtils.substringsBetween(sql,"%[","]%");
				if(rpl2pers!=null&&rpl2pers.length>0){
					for(int i=0;i<rpl2pers.length;i++){
						sql = replaceParamValue(sql,parasDef,rpl2pers[i],paraValues,2);
					}
				}
				String[] rplFpers=StringUtils.substringsBetween(sql,"%[","]");
				if(rplFpers!=null&&rplFpers.length>0){
					for(int i=0;i<rplFpers.length;i++){
						sql = replaceParamValue(sql,parasDef,rplFpers[i],paraValues,0);
					}
				}
				String[] rplTpers=StringUtils.substringsBetween(sql,"[","]%");
				if(rplTpers!=null&&rplTpers.length>0){
					for(int i=0;i<rplTpers.length;i++){
						sql = replaceParamValue(sql,parasDef,rplTpers[i],paraValues,1);
					}
				}
				String[] rplParas = StringUtils.substringsBetween(sql, "[", "]");
				if(rplParas!=null&&rplParas.length>0){
					for(int i=0;i<rplParas.length;i++){
						sql = replaceParamValue(sql,parasDef,rplParas[i],paraValues,9);
					}
				}
				//如果有参数引用――{abc..}，替换成?，并提取其中的参数
				String[] paras=StringUtils.substringsBetween(sql, "{", "}");
				//2009-04-28为适应like中的%%
				String[] has2pers=StringUtils.substringsBetween(sql,"%{","}%");
				sql=sql.replaceAll("%\\{\\w*\\}%","?");
				String[] hasFpers=StringUtils.substringsBetween(sql,"%{","}");
				sql=sql.replaceAll("%\\{\\w*\\}","?");
				//String[] hasTpers=StringUtils.substringsBetween(sql,"{","}%");
				List lstTpers = new ArrayList();
				String tmpSql = sql;
				while(true){
					int end= tmpSql.indexOf("}%");
					if(end<0){
						break;
					}
					String preSql = tmpSql.substring(0,end);
					int start = preSql.lastIndexOf("{");
					String p = preSql.substring(start+1,end);
					tmpSql = tmpSql.substring(end+1);
					lstTpers.add(p);
				}
				String[] hasTpers =lstTpers.size()==0?null:new String[lstTpers.size()]; 
				for(int i=0;i<lstTpers.size();i++){
					hasTpers[i]=(String)lstTpers.get(i);
				}
				sql=sql.replaceAll("\\{\\w*\\}%","?");
				Map paraSearchModes=new HashMap();
				if(has2pers!=null){
					for(int i=0;i<has2pers.length;i++){
						paraSearchModes.put(has2pers[i], "2");
					}
				}
				if(hasFpers!=null){
					for(int i=0;i<hasFpers.length;i++){
						paraSearchModes.put(hasFpers[i], "0");
					}
				}
				if(hasTpers!=null){
					for(int i=0;i<hasTpers.length;i++){
						paraSearchModes.put(hasTpers[i], "1");
					}
				}
				sql=sql.replaceAll("\\{\\w*\\}", "?");
				StringBuffer qSql = new StringBuffer("SELECT COUNT(*) AS RCOUNT FROM(");
		        qSql.append(sql);
		        qSql.append(")");
		        conn=DBConnectionFactory.getConnection();
				//如果没有参数引用
				if(paras==null||paras.length==0){
					ps=conn.prepareStatement(qSql.toString());
					log.info(qSql);
					rs=ps.executeQuery();
				}else{
					ps=conn.prepareStatement(qSql.toString());
					log.info(qSql);
					parseSqlParameter(rpt,paraValues,paras,paraSearchModes,ps);
					rs=ps.executeQuery();
				}
				if(rs.next()){
					count=rs.getInt("RCOUNT");
				}
				rs.close();
				ps.close();
				conn.close();
			}
		}catch(Exception e){
			try{
	    		if(rs!=null){
	    			rs.close();
	    		}
	    		if(ps!=null){
	    			ps.close();
	    		}
	    		if(conn!=null){
	    			conn.close();
	    		}	
	    	}catch(Exception ex){
	    		
	    	}
	    	throw new RptServiceException("获取数据的总记录数时发生错误。错误信息："+e.toString());
		}finally{
			DBConnectionFactory.endDBConnect(conn, rs, ps);
		}
		return count;
	}
	//sql取数方式，执行sql
	private DataSet excuteSql(Report rpt,String sql,Map paraValues)throws RptServiceException{
		DataSet dt=null;
		Connection conn=null;
		PreparedStatement ps=null;
		ResultSet rs=null;
		if(sql==null)return null;
		try{
			conn=DBConnectionFactory.getConnection();
			/*2013-12-25增加参数值的直接替换功能，直接替换的参数用[]引用，
			 * 不影响{}的引用方式。
			 * 先处理两旁都有%的，再处理单边有%的，最后处理无%的
			 */
			Map parasDef=rpt.getParasMap();
			String[] rpl2pers = StringUtils.substringsBetween(sql,"%[","]%");
			if(rpl2pers!=null&&rpl2pers.length>0){
				for(int i=0;i<rpl2pers.length;i++){
					sql = replaceParamValue(sql,parasDef,rpl2pers[i],paraValues,2);
				}
			}
			String[] rplFpers=StringUtils.substringsBetween(sql,"%[","]");
			if(rplFpers!=null&&rplFpers.length>0){
				for(int i=0;i<rplFpers.length;i++){
					sql = replaceParamValue(sql,parasDef,rplFpers[i],paraValues,0);
				}
			}
			String[] rplTpers=StringUtils.substringsBetween(sql,"[","]%");
			if(rplTpers!=null&&rplTpers.length>0){
				for(int i=0;i<rplTpers.length;i++){
					sql = replaceParamValue(sql,parasDef,rplTpers[i],paraValues,1);
				}
			}
			String[] rplParas = StringUtils.substringsBetween(sql, "[", "]");
			if(rplParas!=null&&rplParas.length>0){
				for(int i=0;i<rplParas.length;i++){
					sql = replaceParamValue(sql,parasDef,rplParas[i],paraValues,9);
				}
			}
			//如果有参数引用――{abc..}，替换成?，并提取其中的参数
			String[] paras=StringUtils.substringsBetween(sql, "{", "}");
			//2009-04-28为适应like中的%%
			String[] has2pers=StringUtils.substringsBetween(sql,"%{","}%");
			sql=sql.replaceAll("%\\{\\w*\\}%","?");
			String[] hasFpers=StringUtils.substringsBetween(sql,"%{","}");
			sql=sql.replaceAll("%\\{\\w*\\}","?");
			//String[] hasTpers=StringUtils.substringsBetween(sql,"{","}%");
			List lstTpers = new ArrayList();
			String tmpSql = sql;
			while(true){
				int end= tmpSql.indexOf("}%");
				if(end<0){
					break;
				}
				String preSql = tmpSql.substring(0,end);
				int start = preSql.lastIndexOf("{");
				String p = preSql.substring(start+1,end);
				tmpSql = tmpSql.substring(end+1);
				lstTpers.add(p);
			}
			String[] hasTpers =lstTpers.size()==0?null:new String[lstTpers.size()]; 
			for(int i=0;i<lstTpers.size();i++){
				hasTpers[i]=(String)lstTpers.get(i);
			}
			sql=sql.replaceAll("\\{\\w*\\}%","?");
			Map paraSearchModes=new HashMap();
			if(has2pers!=null){
				for(int i=0;i<has2pers.length;i++){
					paraSearchModes.put(has2pers[i], "2");
				}
			}
			if(hasFpers!=null){
				for(int i=0;i<hasFpers.length;i++){
					paraSearchModes.put(hasFpers[i], "0");
				}
			}
			if(hasTpers!=null){
				for(int i=0;i<hasTpers.length;i++){
					paraSearchModes.put(hasTpers[i], "1");
				}
			}
			sql=sql.replaceAll("\\{\\w*\\}","?");
			//如果有远程排序
			if(rpt.getRemoteSort()==1){
				if(paraValues!=null&&paraValues.get("sort")!=null){
					ParaValue pv=(ParaValue)paraValues.get("sort");
					ParaValue dpv=(ParaValue)paraValues.get("dir");
					sql="SELECT * FROM ("+sql+") ORDER BY "+pv.getValue()+" "+dpv.getValue();
				}
			}
			//如果没有参数引用
			if(paras==null||paras.length==0){
				ps=conn.prepareStatement(sql);
				log.info(sql);
				rs=ps.executeQuery();
			}else{
				ps=conn.prepareStatement(sql);
				log.info(sql);
				parseSqlParameter(rpt,paraValues,paras,paraSearchModes,ps);
				rs=ps.executeQuery();
			}
			//将数据库记录集解析成报表的记录集对象。
			dt=parseResultSet(null,rs);
			//释放资源
			rs.close();
			ps.close();
			conn.close();
		}catch(Exception e){
			try{
				if(rs!=null){
					rs.close();
				}
				if(ps!=null){
					ps.close();
				}
				if(conn!=null){
					conn.close();
				}
			}catch(Exception ex){
				
			}
			throw new RptServiceException("获取数据时发生数据库错误。错误信息："+e.toString());
		}finally{
			DBConnectionFactory.endDBConnect(conn, rs, ps);
		}
		return dt;
	}
	
	//存储过程取数方式，执行存储过程
	private DataSet excuteProcedure(Report rpt,DataDefine df,Map paraValues)throws RptServiceException{
		Connection conn=null;
		CallableStatement cs = null;
		ResultSet rs=null;
		if(rpt==null||df==null)
			return null;
		DataSet dt=new DataSet();
		try{
			conn=DBConnectionFactory.getConnection();
			ProcedureBean pro=df.getProcedure();
			if(pro==null){
				throw new RptServiceException("未能获取报表取数定义！");
			}
			String proName=pro.getName();
			if(proName==null){
				throw new RptServiceException("未指定报表取数的存储过程名称！");
			}
			List parasIn=pro.getInParas();
			StringBuffer proStmt=new StringBuffer("{call ");
			proStmt.append(proName);
			//根据输入参数定义的个数设置?
			if(parasIn!=null&&parasIn.size()>0){
				proStmt.append("(");
				for(int i=0;i<parasIn.size();i++){
					proStmt.append("?");
					if(i<parasIn.size()-1){
						proStmt.append(",");
					}
				}
			}
			//根据输出参数定义继续设置?
			List parasOut=pro.getOutParas();
			if(parasOut!=null&&parasOut.size()>0){
				if(parasIn==null||parasIn.size()==0){
					proStmt.append("(");
				}else{
					proStmt.append(",");
				}
				for(int i=0;i<parasOut.size();i++){
					proStmt.append("?");
					if(i<parasOut.size()-1){
						proStmt.append(",");
					}else{
						proStmt.append(")");
					}
				}
			}else{
				if(parasIn!=null&&parasIn.size()>0){
					proStmt.append(")");
				}
			}
			
			proStmt.append("}");
			cs = conn.prepareCall(proStmt.toString());
			//如果有输入参数
			if(parasIn!=null&&parasIn.size()>0){
				for(int i=0;i<parasIn.size();i++){
					//过程参数引用方式分直接引用固定值和引用参数两种
					ProParaIn pi=(ProParaIn)parasIn.get(i);
					if(pi!=null&&pi.getReferMode()==0){
						if(pi.getDataType()==1){
							int ival=0;
							try{ival=Integer.parseInt(pi.getValue());}
							catch(Exception e){}
							cs.setInt(i+1, ival);
							log.info("参数(整型)"+pi.getReferTo()+":"+ival);
						}else if(pi.getDataType()==2){
							double dval=0;
							try{dval=Double.parseDouble(pi.getValue());}
							catch(Exception e){}
							cs.setDouble(i+1, dval);
							log.info("参数(小数)"+pi.getReferTo()+":"+dval);
						}else{
							cs.setString(i+1, pi.getValue());
							log.info("参数(字符串)"+pi.getReferTo()+":"+pi.getValue());
						}
					}else{
						Map parasMap=rpt.getParasMap();
						if(parasMap==null){
							cs.close();
							conn.close();
							throw new RptServiceException("设计文件中缺少参数定义部分！");
						}
						//找出输入参数的定义
						Parameter para=(Parameter)parasMap.get(pi.getReferTo());
						if(para==null){
							cs.close();
							conn.close();
							throw new RptServiceException("报表取数存储过程中引用的参数"+pi.getReferTo()+"，未在参数定义中找到！");
						}
						ParaValue val=(ParaValue)paraValues.get(pi.getReferTo());
						if(val==null){
							cs.close();
							conn.close();
							throw new RptServiceException("缺少参数"+pi.getReferTo()+"的值！");
						}
						if(para.getDataType()==1){
							int iVal=0;
							try{
								iVal=Integer.parseInt((String)val.getValue());
							}catch(Exception e){}
							cs.setInt(i+1, iVal);
							log.info("参数(整型)"+pi.getReferTo()+":"+iVal);
						}else if(para.getDataType()==2){
							double dVal=0;
							try{
								dVal=Double.parseDouble((String)val.getValue());
							}catch(Exception e){}
							cs.setDouble(i+1, dVal);
							log.info("参数(小数)"+pi.getReferTo()+":"+dVal);
						}else if(para.getDataType()==9){//2010-07-01 日期类型
							java.util.Date pvdt=(java.util.Date)val.getComposedValue();
							java.sql.Date sdt = new java.sql.Date(pvdt.getTime());
							cs.setDate(i+1, sdt);
							log.info("参数(日期)"+pi.getReferTo()+":"+sdt);
						}else{
							cs.setString(i+1,(String)val.getValue());
							log.info("参数(字符串)"+pi.getReferTo()+":"+(String)val.getValue());
						}
					}
				}
			}
			//注册输出参数
			int oStart=parasIn==null?1:parasIn.size()+1;
			if(parasOut!=null){
				for(int i=0;i<parasOut.size();i++){
					ProParaOut po=(ProParaOut)parasOut.get(i);
					if(po.getDataType()==1||po.getDataType()==2){
						cs.registerOutParameter(oStart+i, Types.NUMERIC);
					}else if(po.getDataType()==0){
						cs.registerOutParameter(oStart+i, Types.VARCHAR);
					}else{
						cs.registerOutParameter(oStart+i, oracle.jdbc.OracleTypes.CURSOR);
					}
				}
			}
			conn.setAutoCommit(false);
            cs.execute();
            //如果取数前分页 2010-07-01
            int ti=0;
            if(df.getCanPaging()==1&&df.getPagingMode()==0){
            	ti=df.getProcedure().getTotalIndex();
            	dt.setTotalCount(cs.getInt(oStart-1+ti));
            }
            rs = (ResultSet)cs.getObject(oStart-1+pro.getDataSetIndex());
            //将数据库记录集解析成报表的记录集对象。
			dt=parseResultSet(dt,rs);
			conn.commit();
			rs.close();
			cs.close();
			conn.close();
		}catch(Exception e){
			try{
				if(rs!=null){
					rs.close();
				}
				if(cs!=null){
					cs.close();
				}
				if(conn!=null){
					conn.rollback();
					conn.close();
				}
			}catch(Exception ex){
				
			}
			throw new RptServiceException("获取数据时发生数据库错误。错误信息："+e.toString());
		}finally{
			DBConnectionFactory.endDBConnect(conn, rs, cs);
		}
		return dt;
	}
	
	/**
	 * 数据库记录集--报表记录集的解析
	 * @param rs
	 * @return
	 * @throws SQLException
	 */
	public DataSet parseResultSet(DataSet dt,ResultSet rs)throws SQLException{
		if(rs==null)return null;
		if(dt==null){
			dt = new DataSet();
		}
		ResultSetMetaData rsmd=rs.getMetaData();
		//获取元信息
		int colNum=rsmd.getColumnCount();
		String[] colNames=new String[colNum];
		for(int i=1;i<=colNum;i++){
			colNames[i-1]=rsmd.getColumnName(i);
		}
		MetaData md=new MetaData();
		md.setColumnCount(colNum);
		md.setColumnNames(colNames);
		dt.setMetaData(md);
		//记录集循环，每条记录构造成一个Row，每个row内部是各个字段的名-值对map。
		List rows=new ArrayList();
		while(rs.next()){
			Row row=new Row();
			Map cells=new HashMap();
			for(int i=1;i<=colNum;i++){
				String sVal=rs.getString(i);
				cells.put(colNames[i-1], sVal);
			}
			row.setCells(cells);
			rows.add(row);
		}
		dt.setRows(rows);
		return dt;
	}
	/**
	 * 根据参数配置信息，设置sql语句中的参数
	 * @param rpt
	 * @param paraValues
	 * @param paras
	 * @param ps
	 * @throws Exception
	 */
	private void parseSqlParameter(Report rpt,Map paraValues,String[] paras,Map fuzzySearchPara, PreparedStatement ps)throws Exception{
		Map parasDef=rpt.getParasMap();
		//如果没有引用参数，可以直接返回
		if(paras==null||paras.length==0)return;
		if(paraValues==null){
			throw new RptServiceException("缺少参数值的Map！");
		}
		//如果引用了参数，但参数定义为null，则抛出异常
		if(parasDef==null){
			throw new RptServiceException("设计文件中缺少参数定义部分！");
		}
		for(int i=0;i<paras.length;i++){
			Parameter para=(Parameter)parasDef.get(paras[i]);
			if(para==null){
				throw new RptServiceException("报表取数sql语句中引用的参数"+paras[i]+"，未在参数定义中找到！");
			}
			ParaValue val=(ParaValue)paraValues.get(paras[i]);
			if(val==null){
				throw new RptServiceException("缺少参数"+paras[i]+"的值！");
			}
			if(para.getDataType()==1){
				int iVal=0;
				try{
					iVal=Integer.parseInt((String)val.getValue());
				}catch(Exception e){}
				ps.setInt(i+1, iVal);
				log.info("参数(整型)"+para.getName()+":"+iVal);
			}else if(para.getDataType()==2){
				double dVal=0;
				try{
					dVal=Double.parseDouble((String)val.getValue());
				}catch(Exception e){}
				ps.setDouble(i+1, dVal);
				log.info("参数(小数)"+para.getName()+":"+dVal);
			}else{
				if(fuzzySearchPara.containsKey(para.getName())){
					if("2".equals(fuzzySearchPara.get(para.getName()))){
						ps.setString(i+1,"%"+val.getValue()+"%");
						log.info("参数(字符串)"+para.getName()+":"+"%"+val.getValue()+"%");
					}else if("0".equals(fuzzySearchPara.get(para.getName()))){
						ps.setString(i+1,"%"+val.getValue());
						log.info("参数(字符串)"+para.getName()+":"+"%"+val.getValue());
					}else if("1".equals(fuzzySearchPara.get(para.getName()))){
						ps.setString(i+1,val.getValue()+"%");
						log.info("参数(字符串)"+para.getName()+":"+val.getValue()+"%");
					}else{
						ps.setString(i+1,(String)val.getValue());
						log.info("参数(字符串)"+para.getName()+":"+val.getValue());
					}
				}else{
					ps.setString(i+1,(String)val.getValue());
					log.info("参数(字符串)"+para.getName()+":"+val.getValue());
				}
			}
		}
	 }
	
	 public static void main(String[] args){
		 String sql="SELECT A.BM,MC,PID,ISLEAF FROM BM_CONT A,USERS_XZ B WHERE A.TABLE_BM=%{ftest} AND A.T={ttest}% AND A.BM=B.CZFPBM AND B.USERID=%{userid}%";
		 
		 String sql1 = sql.replaceAll("\\{\\w*\\}","?");
		 String sql2=sql.replaceAll("\\{\\w\\}", "?");
		 //System.out.println("sql1:"+sql1);
		 //System.out.println("sql2:"+sql2);
		 String[] has2pers=StringUtils.substringsBetween(sql,"%{","}%");
		sql=sql.replaceAll("%\\{\\w*\\}%","?");
			System.out.println("2sql:"+sql);	
			
			String[] hasFpers=StringUtils.substringsBetween(sql,"%{","}");
			sql=sql.replaceAll("%\\{\\w*\\}","?");
			System.out.println("fsql:"+sql);	
			
			String[] hasTpers=StringUtils.substringsBetween(sql,"{","}%");
			sql=sql.replaceAll("\\{\\w*\\}%","?");
			System.out.println("tsql:"+sql);	
		String rvalue="01','02','03";	
		String rsql = "select swdjzh,mc,dz,fddbr, czfpbm from DJ_CZ where czfpbm in([czfpbm])order by czfpbm";
		rsql=rsql.replace("[czfpbm]","'"+rvalue+"'");
		System.out.println("rsql:"+rsql);	
		 String exp="$czfpbm$测试表一{year}";
		 exp=exp.replaceAll("\\$czfpbm\\$","下城区");
		 System.out.println("exp:"+exp);
		 
		 Pattern p=Pattern.compile("r.data.[a-zA-Z]\\w*");
		 Matcher m=p.matcher("r.data.x3_xer3+r.data.yyy");
		 String re="";
		 while(m.find()){
		    re+=m.group()+'\n';
		    
		 }
		 System.out.print(re);
		 System.out.print(re.replaceAll("r.data.", ""));
		 byte si='A';
		 String colIndex="";
		 for(int i=0;i<90;i++){
		    int fi=i/26;
		    int li=i%26;
		    if(fi>0){
		    	colIndex=String.valueOf((char)(fi-1+si))+String.valueOf((char)(li+si));
		    }else{
		    	colIndex=String.valueOf((char)(li+si));
		    }
		    System.out.print(colIndex+",");
		 }
		 String oFunc="(r.data.CSHALFYEAR-r.data.LSHALFYEAR)*100/r.data.LSHALFYEAR)";
		 List cols=new ArrayList();
		 Column col=new Column();
		 col.setColId("autoIndex");
		 col.setDataIndex("autoIndex");
		 cols.add(col);
		 col=new Column();
		 col.setColId("ENAME");
		 col.setDataIndex("ENAME");
		 cols.add(col);
		col=new Column();
		 col.setColId("cFHalf");
		 col.setDataIndex("CFHALFYEAR");
		 cols.add(col);
		 col=new Column();
		 col.setColId("cSHalf");
		 col.setDataIndex("CSHALFYEAR");
		 cols.add(col);
		 col=new Column();
		 col.setColId("lFHalf");
		 col.setDataIndex("LFHALFYEAR");
		 cols.add(col);
		 col=new Column();
		 col.setColId("lSHalf");
		 col.setDataIndex("LSHALFYEAR");
		 cols.add(col);
		 Map colMap=getColIndexMap(cols);
		 String newFunc=parseFunction(oFunc,colMap,4);
		 System.out.print(newFunc);
	 }
	 
	 
	 private static Map getColIndexMap(List leafCols){
		 //存放不同的列所在的列索引A、B、C......
	    Map mapColIndex=new HashMap();
	    byte si='A';
	    String colIndex="";
	    for(int i=0;i<leafCols.size();i++){
	    	Column col=(Column)leafCols.get(i);
	    	if(col.getDataIndex()!=null&&!"".equals(col.getDataIndex())){
	    		int fi=i/26;
		    	int li=i%26;
		    	if(fi>0){
		    		colIndex=String.valueOf((char)(fi-1+si))+String.valueOf((char)(li+si));
		    	}else{
		    		colIndex=String.valueOf((char)(li+si));
		    	}
	    		mapColIndex.put(col.getDataIndex(), String.valueOf(colIndex));
	    	}
	    }
	    return mapColIndex;
	}
	
	private static String parseFunction(String oFunc,Map colMap,int rowIndex){
		String newFunc=oFunc;
		Pattern p=Pattern.compile("r.data.[a-zA-Z]\\w*");
		Matcher m=p.matcher(oFunc);
		while(m.find()){
			String re=m.group();
			String aCol=re.replaceAll("r.data.", "");
			String pos=(String)colMap.get(aCol)+rowIndex;
			newFunc=newFunc.replaceAll("r.data."+aCol, pos);
		}
		/*for(int i=0;i<colNames.size();i++){
			String aCol=(String)colNames.get(i);
			String pos=(String)colMap.get(aCol)+rowIndex;
			nf=oFunc.replaceAll("r.data."+aCol, pos);
		}*/
		return newFunc;
	}
	public List getReportUnits(Report rpt){
		List units = null;
		Map muts = TemplatesLoader.getTemplatesLoader().getUnitsMap();
		String supportUnits = rpt.getSupportUnits();
		String[] rptUnits = supportUnits==null?null:supportUnits.split(",");
		boolean hasOriginal = false;
		if(rptUnits!=null&&rptUnits.length>0){
			units = new ArrayList();
			Unit oun =(Unit)muts.get("original");
			units.add(oun);
			for(int i=0;i<rptUnits.length;i++){
				String sun = rptUnits[i];
				Unit un = (Unit)muts.get(sun);
				if(un!=null&&!un.getId().equals("original")){
					units.add(un);
				}
			}
		}else{
			units = TemplatesLoader.getTemplatesLoader().getUnits();
		}
		return units;
	}
	/**
	 * * 获取报表的信息
	* @param rpt 指定的报表
	* @param tmpCdt 
	* @return
	 */
	public String getReportInfo(Report rpt,Map paraVals) {
		if(rpt==null){
			return null;
		}
		StringBuffer json =new StringBuffer("{desc:'");
		String desc = rpt.getDescription();
		json.append(StringUtils.isEmpty(desc)?"":desc).append("',paramVals:");
		JSONArray jarr = new JSONArray();
		List allParams = new ArrayList();
		try{
			allParams.addAll(rpt.getParas());
			allParams.addAll(rpt.getParasForFilter());
		}catch(Exception e){
		}
		if(allParams==null||paraVals==null){
			json.append("}");
			return json.toString();
		}
		try{
			for (int i = 0; i < allParams.size(); i++) {
				Parameter para = (Parameter) allParams.get(i);
				if (para == null || StringUtils.isEmpty(para.getDesc())) {
					continue;
				}
				JSONObject jobj = new JSONObject();
				ParaValue val = (ParaValue) paraVals.get(para.getName());
				String jv = "";
				if (val != null && val.getValue() != null) {
					if (val.getValue().equals(val.getDesc())) {
						jv = val.getValue();
					} else {
						jv = val.getDesc() + "(" + val.getValue() + ")";
					}
				} else {
					continue;
				}
				jobj.put("pname", para.getName());
				jobj.put("ptext", para.getDesc());
				jobj.put("pvalue", jv);
				jarr.put(jobj);
			}
		}catch(Exception e){
			log.error(e.toString());
		}
		json.append(jarr.toString()).append("}");
		return json.toString();
	}
	private Map parseParamValues(Report rpt,HttpServletRequest request,String tmpPostVals){
		Map paVals=new HashMap();
		List allParams = new ArrayList();
		List defaultParams = new ArrayList();//默认值依赖其他参数值而变的，在后期处理，确定其默认值。
		try{
			allParams.addAll(rpt.getParas());
			allParams.addAll(rpt.getParasForFilter());
		}catch(Exception e){
		}
		for(int k=0;k<allParams.size();k++){
			Parameter tmpPa=(Parameter)allParams.get(k);
			if(tmpPa.getIsHidden()==1){
				String sDesc="",sVal="";
				if(tmpPa.getBindMode()==0){
					sDesc=sVal=tmpPa.getBindTo();
				}else if(tmpPa.getBindMode()==1){
					sVal=request.getParameter(tmpPa.getName());
					sDesc=request.getParameter(tmpPa.getName()+"_desc");
				}else if(tmpPa.getBindMode()==2){
					sDesc=sVal=(String)request.getSession().getAttribute(tmpPa.getBindTo());
				}else{
					String path=tmpPa.getBindTo();
					try{
						IParaDataBind pdGetInstance=(IParaDataBind)Class.forName(path).newInstance();
						ParaValue tpv=pdGetInstance.getParaValue(request, rpt, tmpPa);
						if(tpv!=null){
							sVal=tpv.getValue();
							sDesc=tpv.getDesc();
						}
					}catch(Exception e){
						System.out.println("未能正确加载报表取值类!错误信息:"+path+e.toString());
					}
				}
				ParaValue pv=new ParaValue(sVal,sDesc);
				paVals.put(tmpPa.getName(),pv);
			}else if(tmpPa.getDefaultValue()!=null&&!"".equals(tmpPa.getDefaultValue())){
				ParaValue pv=new ParaValue(tmpPa.getDefaultValue(),tmpPa.getDefaultValue());
				paVals.put(tmpPa.getName(),pv);
			}else if(tmpPa.getDefaultRule()!=null&&!"".equals(tmpPa.getDefaultRule())){
				String rule = tmpPa.getDefaultRule();
				if("_first".equals(rule)){
					try{
						List opts = ParaOptionsService.getParaOptionsService().getOptions(rpt, tmpPa, paVals);
						OptionItem op = opts==null||opts.size()==0?null:(OptionItem)opts.get(0);
						ParaValue pv=new ParaValue(op==null?"":op.getBm(),op==null?"":op.getName());
						paVals.put(tmpPa.getName(),pv);
					}catch(Exception e){
					}
				}else{
					defaultParams.add(tmpPa);
				}
			}
		}
		//通过交互传递的参数值。如果与之前处理的参数值同（前面处理过默认值），会被本次传递值覆盖。
		JSONObject jttParams = null;
		JSONObject jparams = null;
		if(tmpPostVals!=null){
			try{
				jttParams = new JSONObject(tmpPostVals);
			}catch(Exception e){
			}
		}
		if(jttParams!=null&&jttParams.has("macroParams")){
			try{
				jparams = jttParams.getJSONObject("macroParams");
			}catch(Exception e){
			}
		}
		//对外部传入“参数名-值”对进行解析，也放入可用参数值Map中
		if(jparams!=null){
			Iterator keys = jparams.keys();
			Map paraMap = rpt.getParasMap();
			while(keys.hasNext()){
				String pname = (String)keys.next();
				Parameter p = null;
				if(paraMap!=null&&paraMap.containsKey(pname)){
					p=(Parameter)paraMap.get(pname);
				}
				if(p==null){
					continue;
				}
		        String val = null;
		        if(p.getDataType()==2){
		        	try{
		        		Double dval = jparams.getDouble(pname);
				        val = dval.toString();
				    }catch(Exception e){
				    }
		        }else if(p.getDataType()==1){
		        	try{
			           Integer ival = jparams.getInt(pname);
			           val = ival.toString();
			        }catch(Exception e){
			        }
		        }else{
		        	try{
			             val = jparams.getString(pname);
			        }catch(Exception e){
			        }
		        }
				ParaValue pv=new ParaValue(val,val);
				paVals.put(pname,pv);
			}
		}
		if(defaultParams!=null&&defaultParams.size()>0){
			for(int i=0;i<defaultParams.size();i++){
				Parameter p = (Parameter)defaultParams.get(i);
				//如果已经取得了该参数的值，不处理默认值。
				if(paVals.containsKey(p.getName())){
					continue;
				}
				try{
					OptionItem op = ParaDefaultOptionService.getParaDefaultOptionService().getParaDefaultOption(rpt,p,paVals);
					if(op!=null){
						ParaValue pv=new ParaValue(op.getBm(),op.getName());
						paVals.put(p.getName(),pv);
					}
				}catch(Exception e){
				}
			}
		}
		return paVals;
	}
	
	public String parseParaExp(String exp,Report rpt,Map mParas)throws Exception{
		if(StringUtils.isEmpty(exp)){
			return "";
		}
		String[] tPas=StringUtils.substringsBetween(exp, "{", "}");
		String[] tDes=StringUtils.substringsBetween(exp, "$", "$");
		String[] tPros=StringUtils.substringsBetween(exp, "@", "@");
		//参数值解析
		if(tPas!=null){
			for(int i=0;i<tPas.length;i++){
				ParaValue pv=(ParaValue)mParas.get(tPas[i]);
				exp=exp.replaceAll("\\{"+tPas[i]+"\\}",pv==null?"":((String)pv.getValue()==null?"":(String)pv.getValue()));
			}
		}
		//参数描述（中文名称）解析
		if(tDes!=null){
			for(int i=0;i<tDes.length;i++){
				ParaValue pv=(ParaValue)mParas.get(tDes[i]);
				exp=exp.replaceAll("\\$"+tDes[i]+"\\$",pv==null?"":(pv.getDesc()==null?"":pv.getDesc()));
			}
		}
		if(tPros!=null){
			Map paraProsMap=rpt.getParaProcesses();
			ParaProcessService ppService=ParaProcessService.getParaProcessService();
			if(paraProsMap!=null){
				for(int i=0;i<tPros.length;i++){
					ParaProcess paraPro=(ParaProcess)paraProsMap.get(tPros[i]);
					String parsedVal=ppService.getProcessedParaValue(rpt,paraPro,mParas);
					exp=exp.replaceAll("\\@"+tPros[i]+"\\@",parsedVal==null?"":parsedVal);
				}
			}
		}
		return exp;
	}
	private String replaceParamValue(String sql,Map parasDef,String pName,Map paraValues,int rmode){
		Parameter para=(Parameter)parasDef.get(pName);
		if(para==null){
			return sql;
		}
		String rvalue = "";
		ParaValue val=(ParaValue)paraValues.get(pName);
		if(val==null){
			rvalue="";
		}else{
			rvalue = val.getValue();
		}
		//按参数类型转变值
		if(para.getDataType()==0){
			if(rmode==9){//如果不是like，('%%')的形式，需转化成每个逗号之间都插入单引号
				rvalue = rvalue.replaceAll(",", "','");
			}
		}else{
			rvalue =(String)val.getValue();
		}
		//值的替换，有like操作的，一律当字符串处理，加''，非like操作，要根据数据类型确定是否添加''
		if(rmode==2){//%在两头
			sql=sql.replace("%["+pName+"]%","'%"+rvalue+"%'");
		}else if(rmode==0){//%在前
			sql=sql.replace("%["+pName+"]","'%"+rvalue+"'");
		}else if(rmode==1){//%在后
			sql=sql.replace("["+pName+"]%","'"+rvalue+"%'");
		}else{//无%
			if(para.getDataType()==0){
				sql=sql.replace("["+pName+"]","'"+rvalue+"'");
			}else{
				sql=sql.replace("["+pName+"]",rvalue);
			}
		}
		return sql;
	}
	
	public DataSet getReportDataByDataDefine(Report rpt,DataDefine df,Map paraVals)throws RptServiceException{
		DataSet dts=null;
		if(rpt==null)return null;
		if(df==null)return null;
		//根据取数方式不同调用不同的方法
		if(df.getSourceType()==1){
			dts=excuteSql(rpt,df.getSql(),paraVals);
		}else if(df.getSourceType()==2){
			dts=excuteProcedure(rpt,df,paraVals);
		}else if(df.getSourceType()==3){
			try{
				String path=df.getImplClass();
				IDataService dataInstance=(IDataService)Class.forName(path).newInstance();
				dts=dataInstance.getReportData(rpt, paraVals);
			}catch(Exception e){
				throw new RptServiceException("自定义类取数时发生错误："+e.toString());
			}
		}else{
			//静态数据不在此处取数，日后如有必要再扩充，应在模板加载时获取。
		}
		return dts;
	}
	public DataSet getReportDataPagingByDataDefine(Report rpt,DataDefine df,Map paraVals,int start,int limit)throws RptServiceException{
		DataSet dts=null;
		if(rpt==null)return null;
		if(df==null)return null;
		//分页时，sql取数，预加工取数sql，限定记录范围。过程取数，取全部记录集的一部分。
		if(df.getSourceType()==1){
			String sql=df.getSql();
			if(sql==null)return null;
			StringBuffer qSql = new StringBuffer("SELECT * FROM (SELECT A.*, rownum r FROM (");
	        qSql.append(sql);
	        qSql.append(") A WHERE rownum<=");
	        qSql.append((start+limit));
	        qSql.append(") B WHERE r>");
	        qSql.append(start);
	        dts=excuteSql(rpt,qSql.toString(),paraVals);
		}else if(df.getSourceType()==2){
			paraVals.put("start", new ParaValue(String.valueOf(start),String.valueOf(start)));
			paraVals.put("limit", new ParaValue(String.valueOf(limit),String.valueOf(limit)));
			DataSet allDts=excuteProcedure(rpt,df,paraVals);
			dts=allDts;
			/*if(allDts!=null){
				List rows=allDts.getRows();
				if(rows!=null){
					try{
						int ei=Math.min(rows.size(),start+limit);
						List subRows=rows.subList(start, ei);
						dts=new DataSet();
						dts.setMetaData(allDts.getMetaData());
						dts.setRows(subRows);
					}catch(Exception e){dts=null;}
				}
			}*/
		}else if(df.getSourceType()==3){
			try{
				String path=df.getImplClass();
				IDataService dataInstance=(IDataService)Class.forName(path).newInstance();
				dts=dataInstance.getReportDataPaging(rpt, paraVals, start, limit);
			}catch(Exception e){
				throw new RptServiceException("自定义类取数时发生错误："+e.toString());
			}
		}else{
			//静态数据不在此处取数，日后如有必要再扩充，应在模板加载时获取。
		}
		return dts;
	}
}
