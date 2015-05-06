package com.fruit.query.service;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import com.fruit.query.data.OptionItem;
import com.fruit.query.data.ParaValue;
import com.fruit.query.data.ProcedureBean;
import com.fruit.query.report.DefaultRuleDefine;
import com.fruit.query.report.Parameter;
import com.fruit.query.report.ProParaIn;
import com.fruit.query.report.ProParaOut;
import com.fruit.query.report.Report;
import com.fruit.query.util.DBConnectionFactory;
import com.fruit.query.util.IParaDefaultOption;
import com.fruit.query.util.RptServiceException;

public class ParaDefaultOptionService implements IParaDefaultOption{
	private static ParaDefaultOptionService paraDefaultOptionService;
	private ParaDefaultOptionService(){
		
	}
	public static ParaDefaultOptionService getParaDefaultOptionService(){
		if(paraDefaultOptionService==null)
			paraDefaultOptionService=new ParaDefaultOptionService();
		return paraDefaultOptionService;
	}
	public OptionItem getParaDefaultOption(Report rpt,Parameter para,Map paraVals)throws RptServiceException{
		OptionItem op = null;
		if(rpt==null||para==null)return null;
		DefaultRuleDefine drd = para.getDefaultRuleDefine();
		if(drd==null)return null;
		//根据取数方式不同调用不同的方法
		if(drd.getSourceType()==1){
			op=excuteSql(rpt,drd.getSql(),paraVals);			
		}else if(para.getSourceType()==2){
			op=excuteProcedure(rpt,para,paraVals);
		}else{
			try{
				String path=drd.getImplClass();
				IParaDefaultOption pdInstance=(IParaDefaultOption)Class.forName(path).newInstance();
				op=pdInstance.getParaDefaultOption(rpt,para,paraVals);
			}catch(Exception e){
				throw new RptServiceException("自定义类加载参数待选项时发生错误："+e.toString());
			}
		}
		return op;
	}
	//sql取数方式，执行sql
	private OptionItem excuteSql(Report rpt,String sql,Map paraValues)throws RptServiceException{
		OptionItem option=null;
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
			//如果有参数引用――{}，替换成?，并提取其中的参数
			String[] paras=StringUtils.substringsBetween(sql, "{", "}");
			
			//2009-04-28为适应like中的%%
			String[] has2pers=StringUtils.substringsBetween(sql,"%{","}%");
			sql=sql.replaceAll("%\\{\\w*\\}%","?");
			String[] hasFpers=StringUtils.substringsBetween(sql,"%{","}");
			sql=sql.replaceAll("%\\{\\w*\\}","?");
			String[] hasTpers=StringUtils.substringsBetween(sql,"{","}%");
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
			//根据参数中是否分层来加工sql
			if(paraValues!=null&&paraValues.containsKey("loadByLevel")
					&&"1".equals((String)paraValues.get("loadByLevel"))){
				String pid = (String)paraValues.get("pid");
				StringBuffer isql = new StringBuffer("select * from (");
				isql.append(sql).append(" )where pid ");
				if(pid==null||"".equals(pid)){
					isql.append(" is null");
				}else{
					isql.append(" = '").append(pid).append("'");
				}
				sql = isql.toString();
			}
			//如果没有参数引用
			if(paras==null||paras.length==0){
				ps=conn.prepareStatement(sql);
				rs=ps.executeQuery();
			}else{
				ps=conn.prepareStatement(sql);
				parseSqlParameter(rpt,paraValues,paras,paraSearchModes,ps);
				rs=ps.executeQuery();
			}
			//将数据库记录集解析成选项集合。
			option=parseParaOption(rs);
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
			throw new RptServiceException("获取参数待选项数据时发生错误。错误信息："+e.toString());
		}finally{
			DBConnectionFactory.endDBConnect(conn, rs, ps);
		}
		return option;
	}
	
	//存储过程取数方式，执行存储过程
	private OptionItem excuteProcedure(Report rpt,Parameter paraToHandle,Map paraValues)throws RptServiceException{
		OptionItem paraItem=null;
		Connection conn=null;
		CallableStatement cs = null;
		ResultSet rs=null;
		if(rpt==null||paraToHandle==null)
			return null;
		DefaultRuleDefine drd = paraToHandle.getDefaultRuleDefine();
		if(drd==null)return null;
		try{
			conn=DBConnectionFactory.getConnection();
			ProcedureBean pro=drd.getProcedure();
			if(pro==null){
				throw new RptServiceException("未找到默认值的取数定义！");
			}
			String proName=pro.getName();
			if(proName==null){
				throw new RptServiceException("未指定用于构造默认值的存储过程名称！");
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
						}else if(pi.getDataType()==2){
							double dval=0;
							try{dval=Double.parseDouble(pi.getValue());}
							catch(Exception e){}
							cs.setDouble(i+1, dval);
						}else{
							cs.setString(i+1, pi.getValue());
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
							throw new RptServiceException("参数"+paraToHandle.getName()+"待选项集合的取数过程中引用了参数"+pi.getReferTo()+"，未在参数定义中找到！");
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
						}else if(para.getDataType()==2){
							double dVal=0;
							try{
								dVal=Double.parseDouble((String)val.getValue());
							}catch(Exception e){}
							cs.setDouble(i+1, dVal);
						}else{
							cs.setString(i+1,(String)val.getValue());
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
            //将数据库记录集解析成待选项集合。
            paraItem=parseParaOption(rs);
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
			throw new RptServiceException("获取参数待选项数据时发生数据库错误。待解析参数："+paraToHandle.getName()+"错误信息："+e.toString());
		}finally{
			DBConnectionFactory.endDBConnect(conn, rs, cs);
		}
		return paraItem;
	}
	
	/**
	 * 数据库记录集-->参数待选项集合的解析
	 * @param rs 数据库记录集
	 * @return 参数待选项集合
	 * @throws SQLException
	 */
	public OptionItem parseParaOption(ResultSet rs)throws SQLException{
		OptionItem it=null;
		if(rs==null)return null;
		int i=0;
		while(rs.next()){
			if(i>0){
				break;
			}
			it = new OptionItem();
			it.setBm(rs.getString("BM"));
			it.setName(rs.getString("NAME"));
			try{
				it.setPid(rs.getString("PID")==null?"":rs.getString("PID"));
			}catch(Exception e){}
			try{
				it.setIsleaf(rs.getInt("ISLEAF"));
			}catch(Exception e){}
			try{
				it.setIsDefault(rs.getInt("ISDEFAULT"));
			}catch(Exception e){}
			i++;
		}
		return it;
	}
	/**
	 * 根据参数配置信息，设置sql语句中的参数
	 * @param rpt
	 * @param paraValues
	 * @param paras
	 * @param ps
	 * @throws Exception
	 */
	private void parseSqlParameter(Report rpt,Map paraValues,String[] paras,Map fuzzySearchPara,PreparedStatement ps)throws Exception{
		Map parasDef=rpt.getParasMap();
		//如果没有引用参数，可以直接返回
		if(paras==null||paras.length==0)return;
		//如果引用了参数，但参数定义为null，则抛出异常
		if(parasDef==null){
			throw new RptServiceException("设计文件中缺少参数定义部分！");
		}
		for(int i=0;i<paras.length;i++){
			Parameter para=(Parameter)parasDef.get(paras[i]);
			if(para==null){
				throw new RptServiceException("参数待选项取数sql语句中引用的参数"+paras[i]+"，未在参数定义中找到！");
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
			}else if(para.getDataType()==2){
				double dVal=0;
				try{
					dVal=Double.parseDouble((String)val.getValue());
				}catch(Exception e){}
				ps.setDouble(i+1, dVal);
			}else{
				if(fuzzySearchPara.containsKey(para.getName())){
					if("2".equals(fuzzySearchPara.get(para.getName()))){
						ps.setString(i+1,"%"+val.getValue()+"%");
					}else if("0".equals(fuzzySearchPara.get(para.getName()))){
						ps.setString(i+1,"%"+val.getValue());
					}else if("1".equals(fuzzySearchPara.get(para.getName()))){
						ps.setString(i+1,val.getValue()+"%");
					}else{
						ps.setString(i+1,(String)val.getValue());
					}
				}else{
					ps.setString(i+1,(String)val.getValue());
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
}
