package com.ifugle.dft.treasury.dao;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.transaction.annotation.Transactional;

import com.ifugle.dft.dao.BaseDao;
import com.ifugle.dft.treasury.entity.TreasuryImpMap;
import com.ifugle.dft.utils.Configuration;
import com.ifugle.dft.utils.ContextUtil;
@Transactional
public class TreasuryDao extends BaseDao{
	private static Logger log = Logger.getLogger(TreasuryDao.class);
	@Autowired
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate){
		this.jdbcTemplate = jdbcTemplate;
	}
	@SuppressWarnings("unchecked")
	public Map importData(String path,String paramJk,String mtype, String userid) {
		Map infos = new HashMap();
		Configuration cg = (Configuration)ContextUtil.getBean("config");
		Map colMaps = cg.getTreasuryImpMaps();
		if(colMaps==null){
			infos.put("exceptionInfo","缺少金库导入的配置信息！");
			return infos;
		}
		if("eksml".equals(mtype)){
			importData_XML(path,paramJk,colMaps,infos,userid);
		}else{
			importData_TXT(path,paramJk,colMaps,infos,userid);
		}
		return infos;
	}
	
	@SuppressWarnings("unchecked")
	private void importData_XML(String path,String paramJk,Map colMaps,Map infos,String userid){
		SAXReader reader = new SAXReader();
		Document doc = null;
		try{
			doc = reader.read(new File(path));
		}catch(DocumentException e){
			log.error(e.toString());
			infos.put("exceptionInfo","读取文件时，发生IO错误！");
			return ;
		}
	    List mapInfos = (List)colMaps.get(paramJk+"_1");
	    String cJk="",cZwrq="",dtRootPath="";
	    List dtMap = new ArrayList();
	    StringBuffer preSql = new StringBuffer("insert into yssr_tmp(userid,skgkdm_jk,zwrq_jk");
	    StringBuffer sSql = new StringBuffer("");
	    if(mapInfos==null){
	    	infos.put("exceptionInfo","缺少指定金库的导入配置信息，金库号："+paramJk);
			return ;
	    }
	    //先获取账务日期和金库
	    for(int i=0;i<mapInfos.size();i++){
	        TreasuryImpMap tm = (TreasuryImpMap)mapInfos.get(i);
	        if(tm.getRptkeyflag()==0){
	        	preSql.append(",").append(tm.getColname());
	        	sSql.append(",?");
	        	dtMap.add(tm);
	        	if(!"".equals(dtRootPath)){
	        		continue;
	        	}else{
	        		String npath = tm.getFnode();
	    	        dtRootPath = npath.substring(0,npath.lastIndexOf("/"));
	        	}
	        }else{
		        List results = doc.selectNodes(tm.getFnode()); 
		        if(results!=null){
		        	Node node = (Node) results.get(0); 
		        	if(tm.getRptkeyflag()==1){
		        		cJk = node.getText();
		        		if(!paramJk.equals(cJk)){
			        		infos.put("exceptionInfo", "外部选择的金库与文件中的金库代码不相符！文件中读取的金库代码:"+cJk+"，外部选择的金库:"+paramJk);
			        		return;
			        	}
		        	}else{
		        		cZwrq = node.getText();
		        	}
		        }
	        }
	    }
	    if("".equals(dtRootPath)){
	    	infos.put("exceptionInfo", "导入配置中未正确设置明细节点的对应关系！");
    		return;
	    }
	    preSql.append(")values('").append(userid).append("','").append(cJk).append("',").append(cZwrq);
	    preSql.append(sSql).append(")");
	    String  sql= preSql.toString();
	    Connection connection =null;
    	PreparedStatement ps = null;
	    try{
		    List details = doc.selectNodes(dtRootPath);
		    if(details!=null){
		    	//删除数据
		    	StringBuffer dsql =new StringBuffer("delete from yssr_tmp where userid='").append(userid).append("' and skgkdm_jk='").append(cJk);
		    	dsql.append("' and zwrq_jk=").append(cZwrq);
		    	//jdbcTemplate.update(dsql.toString());
		    	//jdbcTemplate.update("commit");
		    	//插入
		    	connection = jdbcTemplate.getDataSource().getConnection();
		    	ps = connection.prepareStatement(dsql.toString());
		    	ps.executeUpdate();
		    	ps.close();
		    	
		    	ps = connection.prepareStatement(sql);
		    	for(int i=0;i<details.size();i++){
		    		Node node = (Node) details.get(i); 
		    		for(int j=0;j<dtMap.size();j++){
		    	        TreasuryImpMap tm = (TreasuryImpMap)dtMap.get(j);
		    	        String nName = tm.getFnode().substring(tm.getFnode().lastIndexOf("/")+1,tm.getFnode().length());
		    	        String val= node.selectSingleNode(nName).getText();
		    	        if(tm.getDtype()==1){
		    	        	int ival = 0;
		    	        	try{
		    	        		ival = Integer.parseInt(val);
		    	        	}catch(Exception e){
		    	        	}
		    	        	ps.setInt(j+1,ival);
		    	        }else if(tm.getDtype()==2){
		    	        	double dval = 0;
		    	        	try{
		    	        		dval = Double.parseDouble(val);
		    	        	}catch(Exception e){
		    	        	}
		    	        	ps.setDouble(j+1,dval);
		    	        }else{
		    	        	ps.setString(j+1,val);
		    	        }
		    		}
		    		ps.addBatch();
		    		if (0 == i % 50) {
		    			ps.executeBatch();
		    		}
		    	}
		    	ps.executeBatch();
		    	ps.close();
		    	//impLog(userid, cJk, cZwrq);
		    	//插入的是过渡表，再调用存储过程：向正式表中插入数据
		    	callProcedure(connection,cJk,cZwrq,userid);
		    	connection.commit();
		    }
		    infos.put("count", details.size());
		    infos.put("skgkdm", cJk);
		    infos.put("zwrq", cZwrq);
	    }catch(SQLException e){
	    	try{
	    		connection.rollback();
	    	}catch(Exception ex){
	    	}
	    	infos.put("exceptionInfo", "批量导入数据错误："+ e.toString());
	    	log.error("批量导入数据错误:"+ e.toString());
	    } finally {
	    	JdbcUtils.closeStatement(ps);
	    	JdbcUtils.closeConnection(connection);
	    }
	}
	@SuppressWarnings("unchecked")
	private void importData_TXT(String path,String paramJk,Map colMaps,Map infos,String userid){
		BufferedReader br;  
	    String line = "";  
		File file = null;
		FileReader fr = null;
		List validColIndex = null; 
		List validMapInfos = null;
	    String cJk=null,cZwrq=null;
	    int jkIndex = -1,zwrqIndex = -1;
	    Map minfoMap = new HashMap();
	    List mapInfos = (List)colMaps.get(paramJk+"_0");
	    if(mapInfos==null){
	    	infos.put("exceptionInfo","缺少指定金库的导入配置信息，金库号："+paramJk);
			return ;
	    }
	    for(int i=0;i<mapInfos.size();i++){
	        TreasuryImpMap tm = (TreasuryImpMap)mapInfos.get(i);
	        minfoMap.put(tm.getFnode(), tm);
	    }
		try{
			file = new File(path);
			fr = new FileReader(file);
		}catch(FileNotFoundException e){
			infos.put("exceptionInfo", "未找到文件！");
			log.error(e.toString());
    		return;
		}
        br = new BufferedReader(fr);
        StringBuffer preSql = new StringBuffer("insert into yssr_tmp(userid,");
        StringBuffer sSql = new StringBuffer("");
        String sql = "";
        Connection connection =null;
    	PreparedStatement ps = null;
    	int count = 0;
        try{
	        while ((line = br.readLine())!= null){  
	            if(validColIndex==null){
	            	//读取第一行表头，保存有导入关系映射的列序号即关系集合）。
	            	validColIndex = new ArrayList();
	            	validMapInfos = new ArrayList();
	            	String[] headers = line.split(",");
	            	for(int i=0;i<headers.length;i++){
	            		String h = headers[i];
	            		if(minfoMap.containsKey(h)){
	            			validColIndex.add(i);
	            			TreasuryImpMap tm = (TreasuryImpMap)minfoMap.get(h);
	            			validMapInfos.add(tm);
	            			String col = tm.getColname();
	            			preSql.append(",").append(col);
	        	        	sSql.append(",?");
	            			if(tm.getRptkeyflag()==1){
	            				jkIndex = i;
	            			}else if(tm.getRptkeyflag()==9){
	            				zwrqIndex = i;
	            			}
	            		}
	            	}
	            	//根据表头构建插入语句
	            	preSql.append(")values('").append(userid).append("'");
	        	    preSql.append(sSql).append(")");
	            	sql = preSql.toString();
	            }else{
	            	String[] values = line.split(",");
	            	if(cJk==null){
	            		cJk = values[jkIndex];
	            		cZwrq = values[zwrqIndex];
	            		if(!paramJk.equals(cJk)){
	            			infos.put("exceptionInfo", "外部选择的金库与文件中的金库代码不相符！文件中读取的金库代码:"+cJk+"，外部选择的金库:"+paramJk);
			        		return;
			        	}
	            		//删除数据
				    	StringBuffer dsql =new StringBuffer("delete from yssr_tmp where userid='").append(userid).append("'");
				    	dsql.append(" and skgkdm_jk='").append(cJk).append("' and zwrq_jk=").append(cZwrq);
				    	//jdbcTemplate.update(dsql.toString());
				    	//插入
				    	connection = jdbcTemplate.getDataSource().getConnection();
				    	ps = connection.prepareStatement(dsql.toString());
				    	ps.executeUpdate();
				    	ps.close();
				    	jdbcTemplate.update("commit");
				    	ps = connection.prepareStatement(sql);
	            	} 
			    	for(int j=0;j<validMapInfos.size();j++){
			    	    TreasuryImpMap tm = (TreasuryImpMap)validMapInfos.get(j);
			    	    Integer ii = (Integer)validColIndex.get(j);
			    	    String val= values[ii.intValue()];
			    	    if(tm.getDtype()==1){
			    	        int ival = 0;
			    	        try{
			    	        	ival = Integer.parseInt(val);
			    	        }catch(Exception e){
			    	        }
			    	        ps.setInt(j+1,ival);
			    	    }else if(tm.getDtype()==2){
			    	        double dval = 0;
			    	        try{
			    	        	dval = Double.parseDouble(val);
			    	        }catch(Exception e){
			    	        }
			    	        ps.setDouble(j+1,dval);
			    	    }else{
			    	        ps.setString(j+1,val);
			    	    }
			    	}
			    	ps.addBatch();
			    	if (0 == count % 50) {
			    		ps.executeBatch();
			    	}
			    	count++;
			    }
	        } 
	        ps.executeBatch();
	    	ps.close();
	    	//impLog(userid, cJk, cZwrq);
	    	//插入的是过渡表，再调用存储过程：向正式表中插入数据
	    	callProcedure(connection,cJk,cZwrq,userid);
	    	connection.commit();
		    infos.put("count", count);
		    infos.put("skgkdm", cJk);
		    infos.put("zwrq", cZwrq);
        }catch(IOException e){
        	infos.put("exceptionInfo", "读取文件时发生IO错误！");
        	log.error(e.toString());
    		return;
        }catch(SQLException e){
        	try{
	    		connection.rollback();
	    	}catch(Exception ex){
	    	}
	    	infos.put("exceptionInfo", "批量导入数据错误："+ e.toString());
	    	log.error("批量导入数据错误:"+ e.toString());
	    } finally {
	    	JdbcUtils.closeStatement(ps);
	    	JdbcUtils.closeConnection(connection);
	    }
	}

	
	private void callProcedure(Connection connection,String cJk,String cZwrq,String userid)throws SQLException {
		CallableStatement cs  = null;
		try{
			Configuration cg = (Configuration)ContextUtil.getBean("config");
	    	String proName = cg.getString("pro_impTreasury","PKG_TREASURY.IMP_JK");
			String proSql = "{call "+proName+"(?,?,?,?,?)}"; 
			cs = connection.prepareCall(proSql.toString());
			cs.setString(1, cJk);
			cs.setString(2, cZwrq); 
			cs.setString(3, userid);
			cs.registerOutParameter(4,Types.VARCHAR);
	        cs.registerOutParameter(5,Types.VARCHAR);  
	        cs.execute();
	        String r_flag = (String)cs.getObject(4);
	        String rinfo = (String)cs.getObject(5);
	        log.info("jk: "+cJk+";zwrq: "+cZwrq+";userid: "+userid+";r_flag:"+r_flag+";rinfo:"+rinfo);
		} finally {
	    	JdbcUtils.closeStatement(cs);
	    }
	}
}
