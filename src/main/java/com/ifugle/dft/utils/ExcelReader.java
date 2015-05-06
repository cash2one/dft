package com.ifugle.dft.utils;

import java.io.FileInputStream;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.usermodel.DateUtil;

public class ExcelReader {
	private static Logger logger = Logger.getLogger(ExcelReader.class);

	public static List<List<String>> getSheetData(String filePath,
			int sheetIndex, int startRow) {
		List<List<String>> rowValues = new ArrayList<List<String>>();
		if (startRow <= 0) {
			startRow = 0;
		}
		try {
			InputStream in = new FileInputStream(filePath);
			Workbook book = WorkbookFactory.create(in);
			Sheet sheet = book.getSheetAt(sheetIndex);
			int rowNum = sheet.getPhysicalNumberOfRows() - 1;

			boolean isNotBlank = false;
			for (int j = startRow; j < rowNum; j++) {
				List<String> rowValue = new ArrayList<String>();
				Row row = sheet.getRow(j);
				isNotBlank = false;
				int colNum = row.getPhysicalNumberOfCells();
				for (int k = 0; k < colNum; k++) {
					Cell cell = row.getCell(k);
					cell.setCellType(Cell.CELL_TYPE_STRING);
					String result = cell.getStringCellValue();
					if (StringUtils.isNotBlank(result)) {
						isNotBlank = true;
					}
					rowValue.add(result);
				}
				if (isNotBlank) {
					rowValues.add(rowValue);
				}
			}
			in.close();
		} catch (Exception e) {
			logger.error("读Excel文件出错", e);
		}
		return rowValues;
	}
	
	public static List readExcelLine(String filePath,int lineNum) {
		return readExcelLine(filePath,0,lineNum);
	}
	
	public static List readExcelLine(String filePath,int sheetNum, int lineNum) {
		if (sheetNum < 0 || lineNum < 0)
			return null;
		List rowValue = null;
		try {
			InputStream in = new FileInputStream(filePath);
			Workbook wb = WorkbookFactory.create(in);
			Sheet sheet = wb.getSheetAt(sheetNum);
			Row row = sheet.getRow(lineNum);
			if (row == null)
				return null;

			int colNum = row.getPhysicalNumberOfCells();
			rowValue = new ArrayList();
			for (int i = 0; i < colNum; i++) {
				Cell cell = row.getCell(i);
				String result = "";
				if(cell!=null){
					result = getCellValue(cell);
					rowValue.add(result);
				}
			}
		} catch (Exception e) {
			logger.error(e.toString());
		}
		return rowValue;
	}

	public static List readExcelColumn(String filePath,int startRow,int colNum) {
		return readExcelColumn(filePath, 0,startRow, colNum);
	}
	
	public static List readExcelColumn(String filePath,int sheetNum,int startRow,int colNum) {
		if (sheetNum < 0 || colNum < 0)
			return null;
		List colValues = null;
		try {
			InputStream in = new FileInputStream(filePath);
			Workbook wb = WorkbookFactory.create(in);
			Sheet sheet = wb.getSheetAt(sheetNum);
			int rowNum = sheet.getFirstRowNum();
			int lstRnum = sheet.getLastRowNum();
			int sRow = rowNum>startRow? rowNum : startRow;
			colValues = new ArrayList();
			for(int i=sRow;i<=lstRnum;i++){
				Row row = sheet.getRow(i);
				if(row==null){
					continue;
				}
				Cell cell = row.getCell(colNum);
				String result = "";
				if(cell!=null){
					result = getCellValue(cell);
					if(result==null||"".equals(result)){
						continue;
					}
					result = result.replace("'","''");
					colValues.add(result);
				}
			}
		} catch (Exception e) {
			logger.error(e.toString());
		}
		return colValues;
	} 
	
	private static String getCellValue(Cell cell) {
		String result = "";
		switch (cell.getCellType()) {
		case Cell.CELL_TYPE_FORMULA:
			result = "FORMULA ";
			break;
		case Cell.CELL_TYPE_NUMERIC:
			if (DateUtil.isCellDateFormatted(cell)) {
				double d = cell.getNumericCellValue();
				Date date = DateUtil.getJavaDate(d);
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				result = sdf.format(date);
			} else {
				result = String.valueOf(cell.getNumericCellValue());
			}
			break;
		case Cell.CELL_TYPE_STRING:
			result = cell.getStringCellValue();
			break;
		case Cell.CELL_TYPE_BLANK:
			result = "";
			break;
		default:
			result = "";
			break;
		}
		// 如果读取的是科学计数法的格式，则转换为普通格式
		if (null != result && result.indexOf(".") != -1&& result.indexOf("E") != -1) {
			DecimalFormat df = new DecimalFormat();
			try{
				result = df.parse(result).toString();
			}catch(Exception e){
			}
		}
		return result;
	}
}
