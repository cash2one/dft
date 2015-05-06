package com.fruit.query.view;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import com.fruit.query.data.Column;
import com.fruit.query.data.DataSet;
import com.fruit.query.data.ParaProcess;
import com.fruit.query.data.ParaValue;
import com.fruit.query.data.Row;
import com.fruit.query.data.RptMultiHeader;
import com.fruit.query.report.ColumnDefine;
import com.fruit.query.report.Report;
import com.fruit.query.report.Title;
import com.fruit.query.service.ColumnsService;
import com.fruit.query.service.ParaProcessService;
import com.fruit.query.service.RptDataService;
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

public class RptDataPdfParser {
	private static RptDataPdfParser pftParser;
	private RptDataPdfParser(){
		
	}
	
	public static RptDataPdfParser getPdfParser(){
		if(pftParser==null)
			pftParser=new RptDataPdfParser();
		return pftParser;
	} 
	
	public void exportPdf(OutputStream out,Report rpt,Map paraVals,String replaceSql){
		try {
			//根据表头定义组织表头节点
			ColumnDefine cd=rpt.getColumnDef();
			List colNodes=ColumnsService.getReportDataService().getColumnNodes(rpt, paraVals);
			RptMultiHeader mheader=new RptMultiHeader(colNodes,cd);
			List columns=mheader.getSortedNodes();
			//pdf文档
			Rectangle rectPageSize = new Rectangle(PageSize.A4);
			//rectPageSize = rectPageSize.rotate();
			Document document = new Document(rectPageSize,50,50,50,50);
			setPageSize(document, columns);
			PdfWriter writer = PdfWriter.getInstance(document, out);
			writer.setViewerPreferences(PdfWriter.ALLOW_PRINTING | PdfWriter.PageLayoutSinglePage);
			document.open();
			Title title=rpt.getTitle();
			buildTitle(document,title,rpt,mheader.getLeafCount(),paraVals);
			// 添加一张表格，使用PdfPTable
			PdfPTable table = new PdfPTable(mheader.getLeafCount());
			table.setWidthPercentage((float)90);
			table.setWidths(getWidthPercentages(columns));
			int maxLevel=mheader.getMaxLevel();
			table.setHeaderRows(2);
			buildHeader(maxLevel,table,mheader);
			
	        //整理底级列定义
		    List leafCols=new ArrayList();
		    for(int j=0;j<columns.size();j++){
	    		Column col=(Column)columns.get(j);
	    		if(col.getIsleaf()>0){
	    			leafCols.add(col);
	    		}
	    	}
		    //----------------------------------------开始输出数据--------------------------------------
		    //获取报表的数据
			DataSet ds=getData(rpt,paraVals,replaceSql);
			//按底级列数创建单元格，不能超过255列
		    int leafCounts=leafCols.size();
		    if(ds!=null&&ds.getRows()!=null&&ds.getRows().size()>0){
		    	BaseFont bfChinese = BaseFont.createFont("STSong-Light","UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
		    	Font fontChinese = new Font(bfChinese, 14, Font.NORMAL);
		        List dtRows=ds.getRows();
			    for(int i=0;i<dtRows.size();i++){
			    	Row crow=(Row)dtRows.get(i);
			    	Map cells=crow.getCells();
					//各个行数据
				    for(int j=0;j<leafCounts;j++){
				    	Column col=(Column)leafCols.get(j);
				    	String val=(String)cells.get(col.getDataIndex());
				    	PdfPCell cell = new PdfPCell(new Phrase(val, fontChinese));
				    	if(col.getDataType()==0){
							cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						} else{
							cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
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
	private void buildHeader(int maxLevel,PdfPTable table,RptMultiHeader mheader){
		List columns=mheader.getSortedNodes();
		try{
			BaseFont bfChinese = BaseFont.createFont("STSong-Light","UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
			Font fontHeader = new Font(bfChinese, 14, Font.BOLD);
			if(maxLevel>1){
				for(int i=1;i<=maxLevel;i++){
				    for(int j=0;j<columns.size();j++){
				    	Column col=(Column)columns.get(j);
				    	if(col.getLevel()!=i){
				    		continue;
				    	}
				    	PdfPCell cell = new PdfPCell(new Phrase(col.getColName(), fontHeader));
						cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
						cell.setHorizontalAlignment(Element.ALIGN_CENTER);
						cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
				    	if(col.getIsleaf()>0&&(maxLevel-i)>0){
				    		cell.setRowspan(maxLevel-i+1);
				    	}else{
				    		int cspan = mheader.getColSpan(j+1, col.getLevel());
				    		if(cspan>1){
				    			cell.setColspan(cspan);
				    		}
				    	}
				    	table.addCell(cell);
				    }
				}
			}else{
				for(int j=0;j<columns.size();j++){
				    Column col=(Column)columns.get(j);
				    PdfPCell cell = new PdfPCell(new Phrase(col.getColName(), fontHeader));
					cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
					cell.setHorizontalAlignment(Element.ALIGN_CENTER);
					cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
				    table.addCell(cell);
				}
			}
		}catch(Exception e){
			
		}
	}
	private void buildTitle(Document doc,Title title,Report rpt,int leafcount,Map mParas)throws Exception{
		String texp=title.getTitleExp();
		//分别找出表头中参数值、参数描述引用
		String[] tPas=StringUtils.substringsBetween(texp, "{", "}");
		String[] tDes=StringUtils.substringsBetween(texp, "$", "$");
		String[] tPros=StringUtils.substringsBetween(texp, "@", "@");
		//参数值解析
		if(tPas!=null){
			for(int i=0;i<tPas.length;i++){
				ParaValue pv=(ParaValue)mParas.get(tPas[i]);
				texp=texp.replaceAll("\\{"+tPas[i]+"\\}",pv==null?"":((String)pv.getValue()==null?"":(String)pv.getValue()));
			}
		}
		//参数描述（中文名称）解析
		if(tDes!=null){
			for(int i=0;i<tDes.length;i++){
				ParaValue pv=(ParaValue)mParas.get(tDes[i]);
				texp=texp.replaceAll("\\$"+tDes[i]+"\\$",pv==null?"":(pv.getDesc()==null?"":pv.getDesc()));
			}
		}
		if(tPros!=null){
			Map paraProsMap=rpt.getParaProcesses();
			ParaProcessService ppService=ParaProcessService.getParaProcessService();
			if(paraProsMap!=null){
				for(int i=0;i<tPros.length;i++){
					ParaProcess paraPro=(ParaProcess)paraProsMap.get(tPros[i]);
					String parsedVal=ppService.getProcessedParaValue(rpt,paraPro,mParas);
					texp=texp.replaceAll("\\@"+tPros[i]+"\\@",parsedVal==null?"":parsedVal);
				}
			}
		}
		BaseFont bfChinese = BaseFont.createFont("STSong-Light","UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
		Font fontTitle = new Font(bfChinese, 24, Font.BOLD);
		if (StringUtils.isNotBlank(texp)) {
			doc.addTitle(texp);
			Paragraph p = new Paragraph(texp, fontTitle);
			p.setAlignment(Element.ALIGN_CENTER);
			p.setSpacingAfter(20);
			doc.add(p);
		}
	}
	
	private void setPageSize(Document document, List columns) {
		if (null == columns) {
			return;
		}
		int totalWidth = 0;
		for (int i=0;i<columns.size();i++) {
			Column col =(Column) columns.get(i);
			if (col.getIsleaf()==1&&col.getIsHidden()==0) {
				int w = 100;
				if (col.getWidth() > 0) {
					w = col.getWidth();
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
	
	private float[] getWidthPercentages(List columns){
		float[] pts = null;
		if(columns==null||columns.size()==0){
			return pts;
		}
		int cc = 0;
		int totalWidth = 0;
		for (int i=0;i<columns.size();i++) {
			Column col =(Column) columns.get(i);
			int w = 100;
			if (col.getIsleaf()==1&&col.getWidth() > 0) {
				cc++;
				w = col.getWidth();
				System.out.println("第"+i+"列："+col.getWidth());
			}
			totalWidth = totalWidth + w;
		}
		pts  = new float[cc];
		cc = 0;
		for(int i=0;i<columns.size();i++){
			Column col =(Column) columns.get(i);
			int w = 100;
			if (col.getIsleaf()==1&&col.getWidth() > 0) {
				cc++;
				w = col.getWidth();
				pts[cc-1] = Math.round(w*100/totalWidth);
			}
		}
		return pts;
	}
	private DataSet getData(Report rpt,Map paraVals,String replaceSql){
		DataSet ds = null;
		try{
			if(replaceSql==null||"".equals(replaceSql)){
				ds=RptDataService.getReportDataService().getReportData(rpt, paraVals);
			}else{
				ds=RptDataService.getReportDataService().getReportData(rpt, paraVals, replaceSql);
			}
		}catch(Exception e){
			System.out.println(e.toString());
		}
		return ds;
	}
}
