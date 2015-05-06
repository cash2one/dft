package com.ifugle.dft.utils;

import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.PrintSetup;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.fruit.query.view.Style;
import com.ifugle.dft.utils.entity.Column;

public class ExportExcelView extends AbstractExcelView{
	private static Logger logger = Logger.getLogger(ExportExcelView.class);
	private static final String CONTENT_TYPE = "application/vnd.ms-excel";
	@SuppressWarnings("deprecation")
	public void exportExcel(Map model,HttpServletRequest request, HttpServletResponse response){
		try {
			String title = (String)model.get("title");
			List columns = (List) model.get("columns");
			List records = (List) model.get("records");
			if (null == columns || null == records) {
				return;
			}
			String subTitle = (String)model.get("subTitle");
			String foot = (String)model.get("subTitle");
			Workbook workbook = new HSSFWorkbook();
			int columnCount = columns.size();
			Sheet sheet;
			if (StringUtils.isNotBlank(title)) {
				sheet = workbook.createSheet(title);
			} else {
				sheet = workbook.createSheet();
			}
			DataFormat format = workbook.createDataFormat();
			sheet.setDefaultColumnWidth(16);

			int topRows = 0;

			Cell cell;
			//标题
			if (StringUtils.isNotBlank(title)) {
				sheet.addMergedRegion(new CellRangeAddress(topRows,topRows,0,(columnCount - 1)));

				Font titleFont = workbook.createFont();
				titleFont.setFontName("隶书");
				titleFont.setFontHeightInPoints((short) 18);
				titleFont.setBoldweight(Font.BOLDWEIGHT_BOLD);

				CellStyle titleStyle = workbook.createCellStyle();
				titleStyle.setFont(titleFont);
				titleStyle.setAlignment(CellStyle.ALIGN_CENTER);
				titleStyle.setFillForegroundColor(HSSFColor.LEMON_CHIFFON.index);// 设置背景色HSSFColor.LEMON_CHIFFON
				titleStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);

				cell = getCell(sheet, topRows, 0);
				Row row = sheet.getRow(topRows);
				row.setHeight((short) Style.TITLE_HEIGHT);
				cell.setCellStyle(titleStyle);
				setText(cell, StringEscapeUtils.unescapeHtml(title));
				topRows++;
			}
			if(StringUtils.isNotBlank(subTitle)){
				sheet.addMergedRegion(new CellRangeAddress(topRows,topRows,0,(columnCount - 1)));
				Font stFont = workbook.createFont();
				stFont.setFontName("隶书");
				stFont.setFontHeightInPoints((short) 10);
				stFont.setBoldweight(Font.BOLDWEIGHT_BOLD);

				CellStyle stStyle = workbook.createCellStyle();
				stStyle.setFont(stFont);
				stStyle.setAlignment(CellStyle.ALIGN_CENTER);
				stStyle.setFillForegroundColor(HSSFColor.LEMON_CHIFFON.index);
				stStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);

				cell = getCell(sheet, topRows, 0);
				Row row = sheet.getRow(topRows);
				row.setHeight((short) Style.SUBHEAD_HEIGHT);
				cell.setCellStyle(stStyle);
				setText(cell, StringEscapeUtils.unescapeHtml(subTitle));
				topRows++;
			}
			Font headerFont = workbook.createFont();
			headerFont.setFontName("宋体");
			headerFont.setFontHeightInPoints((short) 12);
			headerFont.setBoldweight(Font.BOLDWEIGHT_BOLD);

			CellStyle headerStyle = workbook.createCellStyle();
			headerStyle.setBorderBottom(CellStyle.BORDER_THIN);
			headerStyle.setBorderLeft(CellStyle.BORDER_THIN);
			headerStyle.setBorderRight(CellStyle.BORDER_THIN);
			headerStyle.setBorderTop(CellStyle.BORDER_THIN);

			headerStyle.setFillForegroundColor(new HSSFColor.GREY_25_PERCENT().getIndex());
			headerStyle.setFillForegroundColor(HSSFColor.YELLOW.index);// 设置背景色黄色
			headerStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
			headerStyle.setFont(headerFont);
			headerStyle.setAlignment(CellStyle.ALIGN_CENTER);
			
			List<List<Column>> groupRows = (List<List<Column>>) model.get("groupRows");
			//各个底级列的跨行数，0不跨行，0以上表示除本行以外所跨的行数。
			int[] rsps = new int[columnCount];
			if(groupRows!=null&&groupRows.size()>0){
				for(int i=0;i<groupRows.size();i++){
					List headCols = (List)groupRows.get(i);
					int skip=0;
					for(int j=0;j<headCols.size();j++){
						Column col=(Column)headCols.get(j);
						if (col.getColspan() > 1) {
							skip+=col.getColspan()-1;
							for (int k = j; k <= j+skip; k++) {
								cell = getCell(sheet, topRows, k);
								cell.setCellStyle(headerStyle);
								setText(cell, StringEscapeUtils.unescapeHtml(col.getHeader()));
							}
							sheet.addMergedRegion(new CellRangeAddress(topRows,topRows, j, j+skip));
						} else {
							cell = getCell(sheet, topRows, j+skip);
							cell.setCellStyle(headerStyle);
							setText(cell, StringEscapeUtils.unescapeHtml(col.getHeader()));
							rsps[j+skip]++;
						}
						Row row = sheet.getRow(topRows);
						row.setHeight((short) Style.TABLEHEAD_HEIGHT);
					}
					topRows++;
				}
			}
			
			CellStyle[] styles = new CellStyle[columnCount];
			int[] types = new int[columnCount];
			String munit = (String)model.get("moneyUnit");
			for (int i = 0; i < columnCount; i++) {
				Column col = (Column)columns.get(i);
				if (logger.isDebugEnabled()) {
					logger.debug("col:" + col.toString());
				}
				if (col.isHidden()) {
					sheet.setColumnWidth(i, 0);
				} else if (col.getWidth() > 0) {
					sheet.setColumnWidth(i, col.getWidth() * 32);
				}
				cell = getCell(sheet, topRows, i);
				cell.setCellStyle(headerStyle);
				setText(cell, StringEscapeUtils.unescapeHtml(col.getHeader()));
	
				if(rsps[i]>0){
					cell = getCell(sheet, topRows-rsps[i], i);
					setText(cell, StringEscapeUtils.unescapeHtml(col.getHeader()));
					sheet.addMergedRegion(new CellRangeAddress(topRows-rsps[i],topRows, i, i));
				}
				int type = Cell.CELL_TYPE_STRING;
				if (col.getDataType()>0) {
					type = Cell.CELL_TYPE_NUMERIC;
				} 
				types[i] = type;
	
				CellStyle style = workbook.createCellStyle();
				style.setBorderBottom(CellStyle.BORDER_THIN);
				style.setBorderLeft(CellStyle.BORDER_THIN);
				style.setBorderRight(CellStyle.BORDER_THIN);
				Font font = workbook.createFont();
				font.setFontName("宋体");
				font.setFontHeightInPoints((short) 12);
				style.setFont(font);
				if(col.getIsGroup()>0){
					style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
					style.setAlignment(CellStyle.ALIGN_CENTER);
				}else if (StringUtils.equals("left", col.getAlign())) {
					style.setAlignment(CellStyle.ALIGN_LEFT);
				} else if (StringUtils.equals("right", col.getAlign())) {
					style.setAlignment(CellStyle.ALIGN_RIGHT);
				} else if (StringUtils.equals("center", col.getAlign())) {
					style.setAlignment(CellStyle.ALIGN_CENTER);
				}
				if(!StringUtils.isEmpty(munit)&&col.getIsMultiUnit()==1){
					if("yuan2Decimals".equals(munit)||"wan2Decimals".equals(munit)){
						style.setDataFormat(HSSFDataFormat.getBuiltinFormat("#,##0.00"));
					}else if("yuanInt".equals(munit)||"wanInt".equals(munit)){
						style.setDataFormat(HSSFDataFormat.getBuiltinFormat("0"));
					}
				}else if(!StringUtils.isEmpty(col.getRenderer())){
					if("rMoney".equals(col.getRenderer())||"rWan2Decimals".equals(munit)){
						style.setDataFormat(HSSFDataFormat.getBuiltinFormat("#,##0.00"));
					}else if("renderInt".equals(col.getRenderer())||"rWanInt".equals(col.getRenderer())){
						style.setDataFormat(HSSFDataFormat.getBuiltinFormat("0"));
					}
				}
				styles[i] = style;
			}
			sheet.createFreezePane(0, topRows + 1);
			
			String[] preValue = new String[columnCount];
			int[] groupStartRow = new int[columnCount];
			for (int i = 0; i < columnCount; i++){
				groupStartRow[i]=topRows + 1;
			}
			for (int j = 0; j < records.size(); j++) {
				Map rec = (Map)records.get(j);
				for (int i = 0; i < columnCount; i++) {
					Column col = (Column)columns.get(i);
					if(col.getDataIndex()==null||"".equals(col.getDataIndex())){
						continue;
					}
					String strV = "";
					try{
						strV = (String)rec.get(col.getDataIndex());
					}catch(Exception se){
						try{
							Integer iV = null;
							iV = (Integer)rec.get(col.getDataIndex());
							strV = iV.toString();
						}catch(Exception e){
							try{
								Double dV = null;
								dV = (Double)rec.get(col.getDataIndex());
								strV = dV.toString();
							}catch(Exception ex){}
						}
					}
					cell = getCell(sheet, topRows + j + 1, i);
					cell.setCellType(types[i]);
					cell.setCellStyle(styles[i]);
					if (col.getDataType()==1){
						int iv = 0;
						try{
							iv = Integer.parseInt(strV);
						}catch(Exception e){}
						if(munit!=null&&munit.startsWith("wan")&&col.getIsMultiUnit()==1){
		    				iv = iv/10000;
		    			}else if(!StringUtils.isEmpty(col.getRenderer())&&col.getRenderer().startsWith("rWan")){
		    				iv = iv/10000;
		    			}
						cell.setCellValue(iv);
					}if (col.getDataType()==2) {
						double v = 0;
						try{
							v = Double.parseDouble(strV);
						}catch(Exception e){}
						if(munit!=null&&munit.startsWith("wan")&&col.getIsMultiUnit()==1){
		    				v = v/10000;
		    			}else if(!StringUtils.isEmpty(col.getRenderer())&&col.getRenderer().startsWith("rWan")){
		    				v = v/10000;
		    			}
						cell.setCellValue(v);
					} else {
						cell.setCellValue(strV==null?"":strV);
					}
					if(col.getIsGroup()>0){
						//合并单元格
						if(preValue[i]!=null&&!preValue[i].equals(strV)){
							sheet.addMergedRegion(new CellRangeAddress(groupStartRow[i],topRows + j , i, i));
							groupStartRow[i]=topRows + j + 1;
						}else if(j==records.size()-1){
							sheet.addMergedRegion(new CellRangeAddress(groupStartRow[i],topRows + j+1 , i, i));
						}
					}
					preValue[i]=strV;
				}
			}
			if(StringUtils.isNotBlank(foot)){
				sheet.addMergedRegion(new CellRangeAddress(topRows+1+records.size(),topRows+1+records.size(),0,(columnCount - 1)));
				Font fFont = workbook.createFont();
				fFont.setFontName("隶书");
				fFont.setFontHeightInPoints((short) 10);
				fFont.setBoldweight(Font.BOLDWEIGHT_BOLD);

				CellStyle ftStyle = workbook.createCellStyle();
				ftStyle.setFont(fFont);
				ftStyle.setAlignment(CellStyle.ALIGN_CENTER);
				ftStyle.setFillForegroundColor(HSSFColor.LEMON_CHIFFON.index);
				ftStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);

				cell = getCell(sheet, topRows+1+records.size(), 0);
				Row row = sheet.getRow(topRows+1+records.size());
				row.setHeight((short) Style.SUBHEAD_HEIGHT);
				cell.setCellStyle(ftStyle);
				setText(cell, StringEscapeUtils.unescapeHtml(foot));
				topRows++;
			}
			PrintSetup ps = sheet.getPrintSetup();
			ps.setLandscape(true); // 横向(L)
			ps.setScale((short) 60);// 缩放比例(A)
			ps.setFitWidth((short) 1);// 页宽
			ps.setFitHeight((short) 1);// 页高
			ps.setPaperSize(PrintSetup.A4_PAPERSIZE);// 纸张大小(Z) A4纸
			ps.setPageStart((short) 0); // 起始页码(R)
	
			workbook.setRepeatingRowsAndColumns(0, -1, -1, 0, topRows);
				
			sheet.setPrintGridlines(false); // 网格线(G)
			ps.setNoColor(false); // 单色打印(B)
			ps.setDraft(false); // 按草稿方式(Q)
				
			ps.setLeftToRight(false);// 打印顺序
	
			// 设置【页边距】
			sheet.setMargin(Sheet.TopMargin, (double) 0.4);// 上(T)
			sheet.setMargin(Sheet.BottomMargin, (double) 0.4);// 下(B)
			sheet.setMargin(Sheet.LeftMargin, (double) 0.35);// 左(L)
			sheet.setMargin(Sheet.RightMargin, (double) 0.35);// 右(R)
			ps.setHeaderMargin((double) 0.5); // 页眉(A)
			ps.setFooterMargin((double) 0.5); // 页脚(F)
			sheet.setHorizontallyCenter(true); // 水平(Z)
			sheet.setVerticallyCenter(false); // 垂直(V)
	
			String filename = (String)model.get("filename");
			filename = filename==null?"export":filename + ".xls";
			String agent = request.getHeader("USER-AGENT");
	
			if (null != agent && -1 != agent.indexOf("MSIE")) {
				filename = URLEncoder.encode(filename, "UTF8");
			} else if (null != agent && -1 != agent.indexOf("Mozilla")) {
				filename = new String(filename.getBytes("UTF-8"), "ISO8859-1");
			} else {
				filename = URLEncoder.encode(filename, "UTF8");
			}
			response.setContentType("application/x-download");
			response.setHeader("Content-Disposition", "attachment;filename="+ filename);
			response.setContentType(CONTENT_TYPE);
			ServletOutputStream out = response.getOutputStream();
			workbook.write(out);
			out.flush();
		} catch (Throwable e) {
			logger.error("导出Excel时发生错误："+e.toString());
		}
	}
}
