package com.fruit.query.service;

import java.io.*;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.json.JSONObject;

import com.fruit.query.data.ChartDataInfo;
import com.fruit.query.data.ChartDataSet;
import com.fruit.query.data.DataSet;
import com.fruit.query.data.MetaData;
import com.fruit.query.data.OptionItem;
import com.fruit.query.data.ParaValue;
import com.fruit.query.data.ProcedureBean;
import com.fruit.query.data.Row;
import com.fruit.query.report.Chart;
import com.fruit.query.report.DataDefine;
import com.fruit.query.report.Parameter;
import com.fruit.query.report.ProParaIn;
import com.fruit.query.report.ProParaOut;
import com.fruit.query.report.Report;
import com.fruit.query.util.DBConnectionFactory;
import com.fruit.query.util.IDataService;
import com.fruit.query.util.IParaDataBind;
import com.fruit.query.util.RptServiceException;

public class ChartService {
	private static ChartService chartService;
	private static Logger log = Logger.getLogger(ChartService.class);
	private ChartService(){
	}
	public static ChartService getChartService(){
		if(chartService==null)
			chartService=new ChartService();
		return chartService;
	}
	
	public ChartDataInfo getChartData(Report rpt,Map paraVals)throws RptServiceException{
		ChartDataInfo dts=null;
		if(rpt==null)return null;
		Chart ct = rpt.getChart();
		if(ct==null)return null;
		if(ct.getSourceType()==1){
			dts=excuteSql(rpt,ct.getSql(),paraVals);
		}else if(ct.getSourceType()==2){
			dts=excuteProcedure(rpt,paraVals);
		}
		return dts;
	}
	//sql取数方式，执行sql
	private ChartDataInfo excuteSql(Report rpt,String sql,Map paraValues)throws RptServiceException{
		ChartDataInfo dt=null;
		Connection conn=null;
		PreparedStatement ps=null;
		ResultSet rs=null;
		if(sql==null)return null;
		Chart chart = rpt.getChart();
		try{
			conn=DBConnectionFactory.getConnection();
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
			dt=parseResultSet(chart,rs);
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
			throw new RptServiceException("获取图表数据时发生数据库错误。错误信息："+e.toString());
		}finally{
			DBConnectionFactory.endDBConnect(conn, rs, ps);
		}
		return dt;
	}
	
	//存储过程取数方式，执行存储过程
	private ChartDataInfo excuteProcedure(Report rpt,Map paraValues)throws RptServiceException{
		Connection conn=null;
		CallableStatement cs = null;
		ResultSet rs=null;
		if(rpt==null||rpt.getDefaultDataDef()==null)
			return null;
		ChartDataInfo dt=new ChartDataInfo();
		Chart chart = rpt.getChart();
		try{
			conn=DBConnectionFactory.getConnection();
			ProcedureBean pro=(ProcedureBean)rpt.getDefaultDataDef().getProcedure();
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
							throw new RptServiceException("图表取数存储过程中引用的参数"+pi.getReferTo()+"，未在参数定义中找到！");
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
            rs = (ResultSet)cs.getObject(oStart-1+pro.getDataSetIndex());
            //将数据库记录集解析成报表的记录集对象。
			dt=parseResultSet(chart,rs);
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
	public ChartDataInfo parseResultSet(Chart chart,ResultSet rs)throws Exception{
		if(rs==null)return null;
		if(chart ==null){
			return null;
		}
		ChartDataInfo dt = null;
		String ctFld = chart.getCategoryIndex();
		String sFld = chart.getSeriesIndex();
		String dFld = chart.getDataIndex();
		if(chart.getIsMultiSeries()>0&&StringUtils.isEmpty(sFld)){
			throw new RptServiceException("图表部分缺少seriesIndex性的设置！");
		}
		if(StringUtils.isEmpty(ctFld)){
			throw new RptServiceException("图表部分缺少categoryIndex属性的设置！");
		}
		if(StringUtils.isEmpty(dFld)){
			throw new RptServiceException("图表部分缺少dataIndex属性的设置！");
		}
		if(chart.getIsMultiSeries()>0){
			dt = parseMsData(rs,ctFld,sFld, dFld);
		}else{
			dt = parseSsData(rs,ctFld,dFld);
		}
		return dt;
	}
	
	private ChartDataInfo parseSsData(ResultSet rs, String ctFld, String dFld)throws SQLException{
		ChartDataInfo dt = new ChartDataInfo();
		List<String> categories = new ArrayList<String>();
		//单个统计对象时，dataSet实际上只有一个元素
		List<ChartDataSet> dataSets = new ArrayList<ChartDataSet>();
		ChartDataSet cdSet = new ChartDataSet();
		Map<String,String> dtMap = new HashMap<String,String>();
		while(rs.next()){
			String val=rs.getString(dFld);
			String ctVal = rs.getString(ctFld);
			categories.add(ctVal);
			dtMap.put(ctVal, val);
		}
		cdSet.setData(dtMap);
		dataSets.add(cdSet);
		dt.setCategories(categories);
		dt.setDataSets(dataSets);
		return dt;
	}
	private ChartDataInfo parseMsData(ResultSet rs,String ctFld,String sFld,String dFld)throws SQLException{
		ChartDataInfo dt = new ChartDataInfo();
		List<String> categories = new ArrayList<String>();
		Map<String,String> cMap = new HashMap<String,String>();
		List<ChartDataSet> dataSets = new ArrayList<ChartDataSet>();
		Map<String,ChartDataSet> dsMap = new HashMap<String,ChartDataSet>();
		while(rs.next()){
			String val=rs.getString(dFld);
			String ctVal = rs.getString(ctFld);
			String sVal = rs.getString(sFld);
			//如果是新的category，则放入categories集合中
			if(!cMap.containsKey(ctVal)){
				categories.add(ctVal);
				cMap.put(ctVal, ctVal);
			}
			//如果是新的seriesName，增加一个新的数据集
			if(!dsMap.containsKey(sVal)){
				ChartDataSet cdSet = new ChartDataSet();
				cdSet.setSeriesName(sVal);
				cdSet.setData(new HashMap<String,String>());
				dataSets.add(cdSet);
				dsMap.put(sVal, cdSet);
			}
			ChartDataSet tset = dsMap.get(sVal);
			Map<String,String> dtmap =tset.getData();
			dtmap.put(ctVal, val);
		}
		dt.setCategories(categories);
		dt.setDataSets(dataSets);
		return dt;
	}
	
	public static void main(String[] args){
		String templateFile = "/test.vm";
		try{  
			Properties p=new Properties(); 
			p.setProperty("file.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader"); 
			Velocity.init(p);  
  
            VelocityContext context = new VelocityContext(); 
            List cts = new ArrayList();
            cts.add("01");
            cts.add("02");
            cts.add("03");
            context.put("categories", cts);  
            Map srs = new HashMap();
            List datas = new ArrayList();
            datas.add("1974");
            datas.add("1735");
            datas.add("1775");
            srs.put("0", datas);
            datas = new ArrayList();
            datas.add("28");
            datas.add("95");
            datas.add("151");
            srs.put("1", datas);
            datas = new ArrayList();
            datas.add("138");
            datas.add("249");
            datas.add("115");
            srs.put("2", datas);
            datas = new ArrayList();
            datas.add("");
            datas.add("");
            datas.add("");
            srs.put("3", datas);
            context.put("series", srs); 
            Template template = null;  
            try {  
                template = Velocity.getTemplate(templateFile,"UTF-8");  
            } catch (ResourceNotFoundException rnfe) {  
                System.out.println("Example : error : cannot find template "  
                        + templateFile);  
            } catch (ParseErrorException pee) {  
                System.out.println("Example : Syntax error in template "  
                        + templateFile + ":" + pee);  
            }  
            BufferedWriter writer = new BufferedWriter(  
                    new OutputStreamWriter(System.out));  
  
            if (template != null)  
                template.merge(context, writer);  
            writer.flush();  
            writer.close();  
        } catch (Exception e) {  
            System.out.println(e);  
        }  
	}
}
