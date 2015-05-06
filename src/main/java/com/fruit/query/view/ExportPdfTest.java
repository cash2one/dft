package com.fruit.query.view;

import java.io.FileOutputStream;
import java.io.OutputStream;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

public class ExportPdfTest {
	public static void main(String[] args) {
		int cc = 10;//列
		int cr = 40;//行
		try{
		    OutputStream out = new FileOutputStream("E://b.pdf");
		    Document document = new Document(PageSize.A3.rotate());
		    PdfPTable table = new PdfPTable(cc);
		    try {
		        // 将PDF文档写出到out所关联IO设备上的书写对象
		        PdfWriter.getInstance(document, out);
		        // 添加文档元数据信息
		        document.addTitle("这是一个测试");
		        document.addSubject("export information");
		        document.addAuthor("leno");
		        document.addCreator("leno");
		        document.addKeywords("pdf itext");
		        document.open();
		        table.setWidthPercentage(10 * cc);
		        // 产生表格标题行
		        BaseFont bfChinese = BaseFont.createFont("STSong-Light","UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
		        Font fontHeader = new Font(bfChinese, 14, Font.BOLD);
				Font fontChinese = new Font(bfChinese, 14, Font.NORMAL);
				
				PdfPCell tcell = new PdfPCell(new Paragraph("第1列", fontHeader));
				tcell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
				tcell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
				tcell.setBackgroundColor(BaseColor.CYAN);
				tcell.setBorderColor(BaseColor.GREEN);
				tcell.setRowspan(2);
	            table.addCell(tcell);
	            tcell = new PdfPCell(new Paragraph("第2列", fontHeader));
				tcell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
				tcell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
				tcell.setBackgroundColor(BaseColor.CYAN);
				tcell.setBorderColor(BaseColor.GREEN);
				tcell.setRowspan(2);
	            table.addCell(tcell);
				for (int i = 0; i < 4; i++) {
					PdfPCell cell = new PdfPCell(new Paragraph("第"+i+"列", fontHeader));
		            cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
		            cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
		            cell.setBackgroundColor(BaseColor.CYAN);
		            cell.setBorderColor(BaseColor.GREEN);
		            cell.setColspan(2);
		            table.addCell(cell);
				}
		        for (int i = 0; i < cc-2; i++) {
		            PdfPCell cell = new PdfPCell(new Paragraph("第"+i+"列", fontHeader));
		            cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
		            cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
		            cell.setBackgroundColor(BaseColor.CYAN);
		            cell.setBorderColor(BaseColor.GREEN);
		            table.addCell(cell);
		        }
		        for(int i=0;i<cr;i++) {
		            for (int j = 0; j < cc;j++) {
		                PdfPCell cell = null;
		                String textValue = "{"+i+","+j+"}";
		                cell = new PdfPCell(new Paragraph(textValue,fontChinese));
		                cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
		                cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
		                cell.setBorderColor(BaseColor.GREEN);
		                table.addCell(cell);
		            }
		        }
		    } catch (SecurityException e) {
		        e.printStackTrace();
		    }finally {
		                   
		    }
		    document.add(table);
		    document.close();
		    out.close();
		    System.out.println("pdf导出成功！");
	    }catch (Exception e) {
	       // TODO Auto-generated catch block
	       e.printStackTrace();
	    }
	}
}
