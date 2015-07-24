package com.ifugle.dft.utils;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.ifugle.dft.utils.entity.*;
public class ExportPdfView {
	private static ExportPdfView pdfView;
	private ExportPdfView(){
		
	}
	
	public static ExportPdfView getpdfView(){
		if(pdfView==null)
			pdfView=new ExportPdfView();
		return pdfView;
	} 
	
	@SuppressWarnings("unchecked")
	public void exportPdf(HttpServletResponse response,Map model){
		try {
			String title = (String)model.get("title");
			response.setContentType("application/x-download");
			response.setHeader("Content-Disposition", "attachment;filename="+ title);
			response.setContentType("application/pdf");
			OutputStream out = response.getOutputStream();
			
			Rectangle rectPageSize = new Rectangle(PageSize.A4);
			//rectPageSize = rectPageSize.rotate();
			Document document = new Document(rectPageSize,50,50,50,50);
			List columns = (List)model.get("columns");
			setPageSize(document, columns);
			PdfWriter writer = PdfWriter.getInstance(document, out);
			writer.setViewerPreferences(PdfWriter.ALLOW_PRINTING | PdfWriter.PageLayoutSinglePage);
			document.open();
			
			buildTitle(document,title);
			// 添加一张表格，使用PdfPTable
			int colCount = columns==null?0:columns.size();
			PdfPTable table = new PdfPTable(colCount);
			table.setWidthPercentage((float)90);
			table.setWidths(getWidthPercentages(columns));
			List<List<Column>> groupRows = (List<List<Column>>) model.get("groupRows");
			buildHeader(table, groupRows,columns);
	        //整理底级列定义
		    List leafCols=new ArrayList();
		    for(int j=0;j<columns.size();j++){
	    		Column col=(Column)columns.get(j);
	    			leafCols.add(col);
	    	}
		    //----------------------------------------开始输出数据--------------------------------------
		    //获取报表的数据
		    List ds = (List) model.get("records");
			//按底级列数创建单元格，不能超过255列
		    int leafCounts=leafCols.size();
		    if(ds!=null&&ds.size()>0){
		    	BaseFont bfChinese = BaseFont.createFont("STSong-Light","UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
		    	Font fontChinese = new Font(bfChinese, 14, Font.NORMAL);
			    for(int i=0;i<ds.size();i++){
			    	Map cells=(Map)ds.get(i);
					//各个行数据
				    for(int j=0;j<leafCounts;j++){
				    	Column col=(Column)leafCols.get(j);
				    	String val=(String)cells.get(col.getDataIndex());
				    	PdfPCell cell = new PdfPCell(new Phrase(val, fontChinese));
				    	if (StringUtils.equals("left", col.getAlign())) {
							cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						} else if (StringUtils.equals("right", col.getAlign())) {
							cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						} else if (StringUtils.equals("center", col.getAlign())) {
							cell.setHorizontalAlignment(Element.ALIGN_CENTER);
						}
						cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
						table.addCell(cell);
				    }		
			    }
		    }
	       document.add(table);
	       document.close();
	    } catch (Exception e) {
	       e.printStackTrace();
	    }
	}
	//构造表头
	@SuppressWarnings("unchecked")
	private void buildHeader(PdfPTable table,List groupRows,List columns){
		try{
			BaseFont bfChinese = BaseFont.createFont("STSong-Light","UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
			Font fontHeader = new Font(bfChinese, 14, Font.BOLD);
			Map usedIndex = new HashMap();
			if(groupRows!=null&&groupRows.size()>0){
				for(int i=0;i<groupRows.size();i++){
					List headCols = (List)groupRows.get(i);
					int skip=0;
					for(int j=0;j<headCols.size();j++){
						Column col=(Column)headCols.get(j);
						PdfPCell cell = null;
						if (col.getColspan() > 1) {
							skip+=col.getColspan()-1;
							cell = new PdfPCell(new Phrase(col.getHeader(), fontHeader));
							cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
							cell.setHorizontalAlignment(Element.ALIGN_CENTER);
							cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
							cell.setColspan(col.getColspan());
						}else{
							if(!usedIndex.containsKey(new Integer(j+skip))){
								Column cc=(Column)columns.get(j+skip);
								String ht = cc.getHeader();
								cell = new PdfPCell(new Phrase(ht, fontHeader));
								cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
								cell.setHorizontalAlignment(Element.ALIGN_CENTER);
								cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
								cell.setRowspan(groupRows.size()-i+1);
								usedIndex.put(new Integer(j+skip), cc);
							}
						}
				    	table.addCell(cell);
					}
				}
			}
			for(int i=0;i<columns.size();i++){
				if(!usedIndex.containsKey(new Integer(i))){
					Column col=(Column)columns.get(i);
					PdfPCell cell = new PdfPCell(new Phrase(col.getHeader(), fontHeader));
					cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
					cell.setHorizontalAlignment(Element.ALIGN_CENTER);
					cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
					table.addCell(cell);
				}
			}
		}catch(Exception e){
			
		}
	}
	private void buildTitle(Document doc,String title)throws Exception{
		BaseFont bfChinese = BaseFont.createFont("STSong-Light","UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
		Font fontTitle = new Font(bfChinese, 24, Font.BOLD);
		if (StringUtils.isNotBlank(title)) {
			doc.addTitle(title);
			Paragraph p = new Paragraph(title, fontTitle);
			p.setAlignment(Element.ALIGN_CENTER);
			p.setSpacingAfter(20);
			doc.add(p);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void setPageSize(Document document, List columns) {
		if (null == columns) {
			return;
		}
		double totalWidth = 0;
		for (int i=0;i<columns.size();i++) {
			Column col =(Column) columns.get(i);
			if (!col.isHidden()) {
				double w = 100;
				if (col.getWidth() > 0) {
					w = col.getWidth()*1.5;
				}
				totalWidth = totalWidth + w;
			}
		}
		if (totalWidth < PageSize.A4.getWidth()) {
			document.setPageSize(PageSize.A4);
		} else if (totalWidth >= PageSize.A4.getWidth()
				&& totalWidth < PageSize.A3.getWidth()) {
			document.setPageSize(PageSize.A4.rotate());
		} else if (totalWidth >= PageSize.A3.getWidth()
				&& totalWidth < PageSize.A2.getWidth()) {
			document.setPageSize(PageSize.A3.rotate());
		} else if (totalWidth >= PageSize.A2.getWidth()
				&& totalWidth < PageSize.A1.getWidth()) {
			document.setPageSize(PageSize.A2.rotate());
		} else if (totalWidth >= PageSize.A1.getWidth()
				&& totalWidth < PageSize.A0.getWidth()) {
			document.setPageSize(PageSize.A1.rotate());
		} else {
			document.setPageSize(PageSize.A0.rotate());
		}
	}
	
	@SuppressWarnings("unchecked")
	private float[] getWidthPercentages(List columns){
		float[] pts = null;
		if(columns==null||columns.size()==0){
			return pts;
		}
		int cc = 0;
		double totalWidth = 0;
		for (int i=0;i<columns.size();i++) {
			Column col =(Column) columns.get(i);
			double w = 100;
			if (col.getWidth() > 0) {
				cc++;
				w = col.getWidth()*1.5;
			}
			totalWidth = totalWidth + w;
		}
		pts  = new float[cc];
		cc = 0;
		for(int i=0;i<columns.size();i++){
			Column col =(Column) columns.get(i);
			double w = 100;
			if (col.getWidth() > 0) {
				cc++;
				w = col.getWidth()*1.5;
				pts[cc-1] = Math.round(w*100/totalWidth);
			}
		}
		return pts;
	}
}
