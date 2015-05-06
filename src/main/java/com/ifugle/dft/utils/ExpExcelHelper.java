package com.ifugle.dft.utils;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.ParameterizedBeanPropertyRowMapper;

import com.ifugle.dft.utils.entity.DestField;
import com.ifugle.dft.utils.entity.ExcelTable;

public class ExpExcelHelper {
	protected JdbcTemplate jdbcTemplate;
	private Configuration cg ;
	private static final String CONTENT_TYPE_XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
	private static final String CONTENT_TYPE = "application/vnd.ms-excel";
	@Autowired
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate){
		this.jdbcTemplate = jdbcTemplate;
	}
	@Autowired
	public void setConfiguration(Configuration config){
		this.cg = config;
	}
	private static Logger log = Logger.getLogger(ImpExcelHelper.class);
	private static ExpExcelHelper expExcelHelper;
	private ExpExcelHelper(){
		cg = (Configuration)ContextUtil.getBean("config");
	}
	public static ExpExcelHelper getExpExcelHelper(){
		if(expExcelHelper==null){
			expExcelHelper=new ExpExcelHelper();
		}
		return expExcelHelper;
	}
	public void exportFile(int tid,int format,String agent,HttpServletResponse response){
		Workbook workbook = null;
    	if(format==0){
    		workbook = new HSSFWorkbook();
		}else{
			workbook = new XSSFWorkbook();
		}
    	Sheet sheet = workbook.createSheet();
    	sheet.setDefaultColumnWidth(16);
    	//取表头信息
    	List cols = null;
    	StringBuffer sql = new StringBuffer("select c.tbdesc tbname,a.excelcolindex excelcol,a.colname,b.coldesc,b.coltype ");
    	sql.append(" from EXCELMAP a, EXCELTB_COLUMNS b,EXCELTABLES c where a.colname=b.colname and a.tbname=b.tbname and a.tid=?");
    	sql.append(" and c.tbname=a.tbname order by excelcol");
		try{
			cols = jdbcTemplate.query(sql.toString(),new Object[]{tid}, ParameterizedBeanPropertyRowMapper.newInstance(DestField.class));
		}catch(Throwable e){
			log.error(e.toString());
		}  
		String title = "";
		if(cols!=null&&cols.size()>0){
			//输出标题
			Font titleFont = workbook.createFont();
			titleFont.setFontName("隶书");
			titleFont.setFontHeightInPoints((short) 20);
			titleFont.setBoldweight(Font.BOLDWEIGHT_BOLD);

			CellStyle titleStyle = workbook.createCellStyle();
			titleStyle.setFont(titleFont);
			titleStyle.setAlignment(CellStyle.ALIGN_CENTER);
			
			DestField df = (DestField)cols.get(0);
			Row row = sheet.createRow(0);
			row.setHeight((short) 600);
	    	sheet.addMergedRegion(new CellRangeAddress(0, 0, 0,cols.size() - 1));
	    	Cell cell = row.createCell(0,Cell.CELL_TYPE_STRING);
	    	cell.setCellStyle(titleStyle);
	    	title = df.getTbname();
			cell.setCellValue(title);
			
			//列头
			Font headerFont = workbook.createFont();
			headerFont.setFontName("宋体");
			headerFont.setFontHeightInPoints((short) 12);
			headerFont.setBoldweight(Font.BOLDWEIGHT_BOLD);

			CellStyle headerStyle = workbook.createCellStyle();
			headerStyle.setBorderBottom(CellStyle.BORDER_THIN);
			headerStyle.setBorderLeft(CellStyle.BORDER_THIN);
			headerStyle.setBorderRight(CellStyle.BORDER_THIN);
			headerStyle.setBorderTop(CellStyle.BORDER_THIN);

			headerStyle.setFont(headerFont);
			headerStyle.setAlignment(CellStyle.ALIGN_CENTER);
			//输出模板列。按最大（最后）的单元格的索引号创建cell。最大的单元格索引决定列数
			row = sheet.createRow(1);
			row.setHeight((short) 400);
			int maxCindex = ((DestField)cols.get(cols.size()-1)).getExcelcol();
			for(int i=0;i<=maxCindex;i++){
				cell = row.createCell(i,Cell.CELL_TYPE_STRING);
				cell.setCellStyle(headerStyle);
			}
			for(int i=0;i<cols.size();i++){
				df = (DestField)cols.get(i);
				cell = row.getCell(df.getExcelcol());
				cell.setCellValue(df.getColdesc());
			}
		}
    	ServletOutputStream out = null;
        try {  
        	out = response.getOutputStream();
        	if(format==0){
        		title += ".xls";
	    	}else{
	    		title += ".xlsx";
	    	}
			if (null != agent && -1 != agent.indexOf("MSIE")) {
				title = URLEncoder.encode(title, "UTF8");
			} else if (null != agent && -1 != agent.indexOf("Mozilla")) {
				title = new String(title.getBytes("UTF-8"), "ISO8859-1");
			} else {
				title = URLEncoder.encode(title, "UTF8");
			}
			response.setContentType("application/x-download");
			response.setHeader("Content-Disposition", "attachment;filename="+ title);
			if(format==0){
				response.setContentType(CONTENT_TYPE);
			}else{
				response.setContentType(CONTENT_TYPE_XLSX);
			}
			workbook.write(out);
			out.flush();
        }catch(IOException e) {  
            e.printStackTrace();  
            throw new RuntimeException("文件输出异常!请检查.") ;  
        }finally {  
            try {  
                if(out != null) {  
                    out.close();  
                    out = null; 
                }  
            } catch (IOException e) {  
                e.printStackTrace();  
            }  
        }  
	}
}
