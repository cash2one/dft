package com.ifugle.dft.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.ParameterizedBeanPropertyRowMapper;

import com.ifugle.dft.check.entity.En_field;
import com.ifugle.dft.utils.entity.*;
import com.ifugle.dft.utils.exception.*;
import com.softwarementors.extjs.djn.StringUtils;

public class ImpExcelHelper {
	protected JdbcTemplate jdbcTemplate;
	private Configuration cg ;
	@Autowired
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate){
		this.jdbcTemplate = jdbcTemplate;
	}
	@Autowired
	public void setConfiguration(Configuration config){
		this.cg = config;
	}
	private static Logger log = Logger.getLogger(ImpExcelHelper.class);
	private static ImpExcelHelper impExcelHelper;
	//excel导入模板的集合，按模板id进行hash
	private Map templatesMap;
	private ImpExcelHelper(){
		cg = (Configuration)ContextUtil.getBean("config");
	}
	public static ImpExcelHelper getImpExcelHelper(){
		if(impExcelHelper==null){
			impExcelHelper=new ImpExcelHelper();
		}
		return impExcelHelper;
	}
	//根据模板id获取模板信息
	public ExcelTemplate getTemplate(String tid){
		ExcelTemplate tmpInfo = null;
		if(templatesMap==null||!templatesMap.containsKey(tid)){
			initTemplatesMap();
		}
		try{
			tmpInfo = (ExcelTemplate)templatesMap.get(tid);
		}catch(Throwable e){
			log.error("查找excel导入模板时发生错误，模板ID："+tid+"。错误:"+e.toString());
		}
		return tmpInfo;
	}
	
	@SuppressWarnings("unchecked")
	private void addTemplate(String tid,ExcelTemplate tmp){
		if(templatesMap==null){
			templatesMap = new HashMap();
		}
		templatesMap.put(tid, tmp);
	}
	
	public void initTemplatesMap(){
		try {
			StringBuffer sql = new StringBuffer("select tid,tbname,tbdesc,proname,ttype,remark from exceltables");
			List tbs=jdbcTemplate.query(sql.toString(),new Object[]{}, ParameterizedBeanPropertyRowMapper.newInstance(ExcelTable.class));
			if(tbs!=null&&tbs.size()>0){
				templatesMap = new HashMap();
				//每个模板循环获取具体的映射信息
		    	for(int i=0;i<tbs.size();i++){
		    		ExcelTable tb = (ExcelTable)tbs.get(i);
		    		int tid = tb.getTid();
		    		ExcelTemplate tmp=new ExcelTemplate();
		    		tmp.setTb(tb);
		    		StringBuffer msql = new StringBuffer("select nvl(m.excelcolindex,-1) excelcol,c.colname,c.coldesc,c.coltype,c.tbname,c.rptkey,c.isrindex,");
		    		msql.append(" c.showorder from excelmap m,(select * from exceltb_columns where tbname='");
		    		msql.append(tb.getTbname()).append("') c where c.tbname= m.tbname(+) and c.colname=m.colname(+) order by showorder ");
		    		List flds = jdbcTemplate.query(msql.toString(),new Object[]{}, ParameterizedBeanPropertyRowMapper.newInstance(DestField.class));
		    		if(flds!=null&&flds.size()>0){
		    			Map colsMap = new HashMap();
			    		for(int j=0;j<flds.size();j++){
			    			DestField f = (DestField)flds.get(j);
			    			int colindex = f.getExcelcol();
			    			colsMap.put(String.valueOf(colindex), f);
			    		}
			    		//保存单个模板的信息，包括模板列的有序集合，以及按excel列序号索引数据库列的散列集合。
			    		tmp.setColmaps(colsMap);
			    		tmp.setColumns(flds);
			    		tmp.setTid(String.valueOf(tid));
			    		tmp.setTbname(tb.getTbname());
		    		}
		    		addTemplate(String.valueOf(tid), tmp);
		    	}
			}
		}catch (Throwable e) {
			log.error(e.toString());
		}
	}
	
	public String[] ImportRecordFromXsl(String filePath,ExcelTemplate tmplate,int startIndex,String userid,List keyVals)throws ImportDataException{
		String[] done=new String[]{"-1","0",""};
		if(filePath==null||"".equals(filePath)){
			done=new String[]{"-2","0","excel文件路径不存在！"};
			return done;
		}
		if(tmplate==null){
			done=new String[]{"-3","0","未找到导入时的列匹配信息！"};
			return done; 
		}
		String destTb = tmplate.getTbname();
		Workbook wb = null;
		File f = null;
		InputStream in = null;
		int sCount=0;
		try{
			f = new File(filePath);
			if(f.exists()) {
                in = new FileInputStream(filePath);
    			wb = WorkbookFactory.create(in);
    			Sheet sheet = wb.getSheetAt(0);
                Map colmaps = tmplate.getColmaps();
                List columns = tmplate.getColumns(); 
                int[] cols2imp=new int[columns.size()];
                for(int i=0;i<columns.size();i++){
                	DestField c=(DestField)columns.get(i);
                	int ci=c.getExcelcol();
                	cols2imp[i]=ci;
                }
                //先删除相同键值列的报表数据，再插入。
                StringBuffer dsql=new StringBuffer("delete from ");
                dsql.append(destTb).append(" where userid='").append(userid).append("'");
                jdbcTemplate.update(dsql.toString(),new Object[]{});
	            //动态构建sql语句
                String sql=buildSql(columns,destTb,userid);
                //逐行循环。******外部输入的行序号是从1开始的：i-1*****。
                boolean end=false;
                int si=startIndex;
                while(!end){
                	Row row = sheet.getRow(si-1);
                	if(row==null){
                		break;
                	}
                	System.out.println();
                	System.out.print("第"+si+"行：");
                	Object[] params = new Object[cols2imp.length];
                	for(int j=0;j<cols2imp.length;j++){
                		String val="";
                		if(cols2imp[j]<0){
                			DestField fld=(DestField)columns.get(j);
                			if(fld.getIsrindex()==1){
                				params[j]=new Integer(sCount);
                				val = String.valueOf(sCount);
                			}else{
                				if(keyVals!=null){
		                			for(int k=0;k<keyVals.size();k++){
		                				KeyValuePair kv = (KeyValuePair)keyVals.get(k);
		                				if(kv.getKey().equals(fld.getColname())){
		                					val= kv.getValue();
		                					break;
		                				}
		                			}
		                			if(fld.getColtype()==1){
		                				int iVal = 0;
		                				try{
		                					iVal = Integer.parseInt(val);
		                				}catch(Exception e){}
		                				params[j]=new Integer(iVal);
		                			}else if(fld.getColtype()==3){
		                				double dVal=0;
		                				try{
		                					dVal = Double.parseDouble(val);
		                				}catch(Exception e){}
		                				params[j]=new Double(dVal);
		                			}else{
		                				params[j]=val;
		                			}
                				}else{
                					if(fld.getColtype()==1){
		                				params[j]=new Integer(0);
		                			}else if(fld.getColtype()==3){
		                				params[j]=new Double(0);
		                			}else{
		                				params[j]="";
		                			}
                				}
                			}
                		}else{
                			Cell cell=row.getCell(cols2imp[j]);
	                		//获取当前excel列所对应的数据库列
	                		DestField fld=(DestField)colmaps.get(String.valueOf(cols2imp[j]));
	                		if(fld.getColtype()==1){
	                			int iVal=0;
	                			double tmpdv=0;
	                			if(cell!=null){
	                				try{
	                					tmpdv = cell.getNumericCellValue();
	                				}catch(Exception e){
	                					System.out.println("行号:"+si+",列号:"+cols2imp[j]+",错误:"+e.toString());
	                				}
	                			}
	                			iVal = new Double(tmpdv).intValue();
	                			params[j]=new Integer(iVal);
	                			val = String.valueOf(iVal);
	                		}else if(fld.getColtype()==3){
	                			double dVal=0;
	                			if(cell!=null){
	                				try{
	                					dVal=cell.getNumericCellValue();
	                				}catch(Exception e){
	                					System.out.println("行号:"+si+",列号:"+cols2imp[j]+",错误:"+e.toString());
	                				}
	                			}
	                			params[j]=new Double(dVal);
	                			val = String.valueOf(dVal);
	                		}else{
	                			if(cell!=null){
		                			try{
		                				if(cell.getCellType()==HSSFCell.CELL_TYPE_NUMERIC){
		                					if (HSSFDateUtil.isCellDateFormatted(cell)) {   
		                				        double d = cell==null?0:cell.getNumericCellValue();   
		                				        Date date = HSSFDateUtil.getJavaDate(d); 
		                				        SimpleDateFormat sdf= new SimpleDateFormat("yyyy-MM-dd");
		                				        val = sdf.format(date);
		                				    }else{
		                				    	val = String.valueOf(cell.getNumericCellValue());
		                				    }
		                				}else if(cell.getCellType()==HSSFCell.CELL_TYPE_BLANK){
		                					val="";	
		                				}else{
		                					val = cell.getRichStringCellValue().getString();
		                				}
				                        if (null != val &&val.indexOf(".") != -1 && val.indexOf("E") != -1) {
				                            DecimalFormat df = new DecimalFormat();
				                            val = df.parse(val).toString();
				                        }
				                        if (null != val &&val.endsWith(".0")) {
				                            int size = val.length();
				                            val = val.substring(0, size - 2);
				                        }
		                			}catch(Exception e){
		                			}
	                			}
	                			params[j]=val;
	                		}
	                	}
                		System.out.print(val+";");
                	}
                	//执行
                	jdbcTemplate.update(sql.toString(),params);
                    si++;
                	sCount++;
                }
                String proname = tmplate.getTb().getProname();
                if(!StringUtils.isEmpty(proname)){
	                //调用存储过程
	                String[] results = doAfterImport(destTb,proname,keyVals,userid);
	                if(results!=null&&results[0]!=null&&"1".equals(results[0])){
	                	done=new String[]{"1",String.valueOf(sCount),results[1]};
	                }else{
	                	done=new String[]{"-4",String.valueOf(sCount),results[1]};
	                }
                }else{
                	done=new String[]{"1",String.valueOf(sCount),""};
                }
                in.close();
            }else{
            	done=new String[]{"-9",String.valueOf(sCount),"要导入的文件不存在或被删除！"};
            }
		}catch(Exception e) {
			System.out.print("向中间表导入Excel记录时发生错误！中断发生在第"+(sCount)+"条记录之后！");
            e.printStackTrace();
            try{
	            if(in!=null){
	            	in.close();
	            }
            }catch(Exception ex){}
            done=new String[]{"-1",String.valueOf(sCount),"中断发生在第"+(sCount)+"条记录之后！"};
        }finally{
        	try{
	            if(in!=null){
	            	in.close();
	            }
            }catch(Exception ex){}
        }
		return done;
	}
	/**
	 * 构造导入excel的sql语句
	* @param columns
	* @param destTb
	* @param userid
	* @return
	 */
	private String buildSql(List columns,String destTb,String userid){
		StringBuffer sql=new StringBuffer("insert into ").append(destTb);
		sql.append(" (");
		//插入对应列
		for(int i=0;i<columns.size();i++){
			DestField df = (DestField)columns.get(i);
			String tbCol=df.getColname();
			if(tbCol==null||"".equals(tbCol)){
				continue;
			}
			sql.append(tbCol);
			sql.append(",");
		}
		sql.append("userid) values (");
		for(int i=0;i<columns.size();i++){
			sql.append("?,");
		}
		sql.append("'").append(userid).append("')");
		return sql.toString();
	}
	
	public String saveEnExcel(FileItem fi,String dirItem){
		String trace = cg.getString("filePathExcel", "c:/eninfo_upload/hd");
		trace = trace.endsWith("/") ? trace : (trace + "/");
		try{
			//在指定的上传目录中创建文件。
		    java.io.File dir=new java.io.File(trace);
		    if(!dir.exists()){//检查目录是否存在
		    	dir.mkdir();
		    }
		    java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("yyyy-MM-dd");     
			java.util.Date cDate = new java.util.Date();     
			String cTime=formatter.format(cDate);
		    trace=trace+dirItem+"_"+cTime+".xls";
		    long ss = System.currentTimeMillis();
			File f = new File(trace);  
			FileUtils.copyInputStreamToFile(fi.getInputStream(),f);
			long es = System.currentTimeMillis();
			log.info("上传共费时:"+(es-ss)+"毫秒");
		}catch(Exception e){
			log.error(e.toString());
			return "";
		}
		return trace;
	}
	private String[] doAfterImport(String destTb,String proName, List keyVals,String userid) {
		if(proName==null||"".equals(proName)){
			return new String[]{"0","存储过程名称未设置！"};
		}
		StringBuffer sql = new StringBuffer("{call ");
		sql.append(proName).append("(");
		for(int i=0;i<keyVals.size();i++){
			sql.append("?,");
		}
		sql.append("?,?,?)}");
		String flag = "1";
		final String[] results = new String[2];
		try{
			final String[] fStrs= new String[keyVals==null?0:keyVals.size()];
			final String fUser = userid;
			for(int i=0;i<keyVals.size();i++){
				KeyValuePair kv = (KeyValuePair)keyVals.get(i);
				fStrs[i] = kv.getValue();
			}
			flag = (String)jdbcTemplate.execute(sql.toString(),new CallableStatementCallback() {
				public Object doInCallableStatement(CallableStatement cs)throws SQLException, DataAccessException {
					for(int i=0;i<fStrs.length;i++){
						cs.setString(i+1, fStrs[i]);
					}
					cs.setString(fStrs.length+1,fUser);
	                cs.registerOutParameter(fStrs.length+2,Types.VARCHAR);  
	                cs.registerOutParameter(fStrs.length+3,Types.VARCHAR);  
	                cs.execute();  
	                String tmpflag = cs.getString(fStrs.length+2);
	                String tmpInfo = cs.getString(fStrs.length+3);
	                if(!"1".equals(tmpflag)){
	                	log.error(tmpInfo);
	                }
	                results[0] = tmpflag;
	                results[1] = tmpInfo;
	                return tmpflag;  
				} 
			});
		}catch(Throwable e){
			results[0] = "9";
			results[1] = e.toString();
			log.error(e.toString());
		}
		return results;
	}
}
