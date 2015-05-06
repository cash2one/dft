package com.fruit.query.view;
import java.net.URLEncoder;
import java.util.regex.*;
import java.util.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.commons.lang.*;

import com.fruit.query.util.*;
import com.fruit.query.data.*;
import com.fruit.query.report.*;
import com.fruit.query.service.*;
/**
 * 
 *@author wxh
 *2009-4-2
 *TODO 报表模板解析为Excel
 */
public class RptDataExcelParser{
	private static RptDataExcelParser excelParser;
	private RptDataExcelParser(){
		
	}
	/**
	 * 获取报表向Excel转化的解析器实例。
	 * singleton，每次调用，获取的是同一个实例。
	 * @return RptDataExcelParser实例。
	 */
	public static RptDataExcelParser getExcelParser(){
		if(excelParser==null)
			excelParser=new RptDataExcelParser();
		return excelParser;
	} 
	/**
	 * 报表数据输出为Excel文件。
	 * @param out 输出流
	 * @param rpt 报表模板，包含报表定义信息。
	 * @param paraVals 本次请求传递的参数，参数名索引ParaValue形成的map。
	 * @return 是否导出成功。
	 * @throws ParseReportToExcelException
	 */
	public boolean exportToExcel(String eFormat,HttpServletRequest request,HttpServletResponse response,Report rpt,Map paraVals,String replaceSql,int start,int limit,String unit)
		throws ParseReportToExcelException{
		boolean exp=false;
		Style style=new Style();
		ServletOutputStream out = null;
		try{
			out = response.getOutputStream();
			//根据表头定义组织表头节点
			ColumnDefine cd=rpt.getColumnDef();
			List colNodes=ColumnsService.getReportDataService().getColumnNodes(rpt, paraVals);
			RptMultiHeader mheader=new RptMultiHeader(colNodes,cd);
			List columns=mheader.getSortedNodes();
			//获取报表的数据
			DataSet ds=getData(rpt,paraVals,replaceSql,start,limit);
			//excel工作簿
			Workbook wb=null;
			if("0".equals(eFormat)){
				wb = new HSSFWorkbook();
			}else{
				wb = new XSSFWorkbook();
			}
			org.apache.poi.ss.usermodel.Row row = null;
			Cell cell = null;
			CellStyle csTitle = style.getTitleStyle(wb);
		    CellStyle csTbHead = style.getTbHeadStyle(wb);
		    Sheet sheet = wb.createSheet(rpt.getName());
		    // 构建标题
			Title title=rpt.getTitle();
			String titleText = buildTitle(title,rpt,paraVals);
			row = sheet.createRow(0);
		    row.setHeight((short) Style.TITLE_HEIGHT);
		    cell = row.createCell((short) 0);
		    cell.setCellValue(titleText);
		    sheet.addMergedRegion(new CellRangeAddress(0,0,0,(mheader.getLeafCount()-1)));//标题跨的行和列
		    cell.setCellStyle(csTitle);
		    int tbStartRow = 1;
		    if(rpt.getHead()!=null&&rpt.getHead().getSubTitle()!=null){
		    	tbStartRow=2;
		    	SubTitle sbTitle = rpt.getHead().getSubTitle();
		    	String stText = buildSubTitle(sbTitle,rpt,paraVals);
		    	row = sheet.createRow(1);
		    	CellStyle csSubTitle = style.getSubHeadStyle(wb);
			    row.setHeight((short) Style.SUBHEAD_HEIGHT);
			    cell = row.createCell((short) 0);
			    cell.setCellValue(stText);
			    sheet.addMergedRegion(new CellRangeAddress(1,1,0,(mheader.getLeafCount()-1)));
			    cell.setCellStyle(csSubTitle);
		    }
		    //构建表头
		    int maxLevel=mheader.getMaxLevel();
		    buildHeader(tbStartRow,maxLevel,sheet,mheader,csTbHead);
		    //底级列
		    List leafCols=new ArrayList();
		    for(int j=0;j<columns.size();j++){
	    		Column col=(Column)columns.get(j);
	    		if(col.getIsleaf()>0){
	    			leafCols.add(col);
	    		}
	    	}
		    Map mIndex=getColIndexMap(leafCols);
		    //----------------------------------------开始输出数据--------------------------------------
		    //数据内容的起始行
		    int startRow=maxLevel+tbStartRow;
			//按底级列数创建单元格，不能超过255列
		    int MAXCOLS=250;
		    int leafCounts=leafCols.size();
		    if(leafCounts>MAXCOLS+1){
		    	leafCounts=MAXCOLS+1;
		    }
		    String[] preValue = new String[leafCounts];
		    List gstartRows = new ArrayList();
		    Map gstartMap = new HashMap();
		    gstartRows.add(startRow);
		    gstartMap.put(startRow, startRow);
		    //数据集
		    List dtRows=ds.getRows();
		    for(int i=0;i<dtRows.size();i++){
		    	Row crow=(Row)dtRows.get(i);
		    	Map cells=crow.getCells();
				//各个行数据
				row = sheet.createRow(startRow+i);
			    row.setHeight((short) Style.TABLECONTENT_HEIGHT);
			    for(int j=0;j<leafCounts;j++){
			    	Column col=(Column)leafCols.get(j);
			    	CellStyle cst=cellStyleFormat(col,wb,true,style,unit);
			    	cell = row.createCell((short)j);
			    	String fldName=col.getDataIndex();
			    	String val= "";
			    	if(fldName!=null&&!"".equals(fldName)){
			    		val=(String)cells.get(fldName);
			    		if(col.getDataType()==0){
			    			cell.setCellValue(val);
			    		}else{
			    			double dtmp=0;
			    			try{
			    				dtmp=Double.parseDouble(val);
			    			}catch(Exception e){}
			    			if(unit!=null&&unit.startsWith("wan")&&col.getIsMultiUnit()==1){
			    				dtmp = dtmp/10000;
			    			}else if(!StringUtils.isEmpty(col.getRenderer())&&col.getRenderer().startsWith("rWan")){
			    				dtmp = dtmp/10000;
			    			}
			    			cell.setCellValue(dtmp);
			    		}
			    	}else if(!StringUtils.isEmpty(col.getColFunction())){
			    		String func=col.getColFunction();
			    		String newFunc=parseFunction(func,mIndex,row.getRowNum());
						cell.setCellFormula(newFunc);
			    	}else{
			    		cell.setCellValue("");
			    	}
			    	if("autoIndex".equals(col.getColId())){
			    		cell.setCellValue(row.getRowNum()-maxLevel);
					}
			    	cell.setCellStyle(cst);
			    	if(col.getIsGroup()>0){
			    		if(preValue[j]!=null&&!preValue[j].equals(val)){
			    			if(!gstartMap.containsKey(startRow+i)){
			    				gstartMap.put(startRow + i, startRow + i);
			    				gstartRows.add(startRow + i);
			    			}
			    		}
			    	}
					preValue[j]=val;
			    }
		    }
		    gstartRows.add(startRow+dtRows.size());
		    for(int i=0;i<leafCounts;i++){
		    	Column col=(Column)leafCols.get(i);
		    	if(col.getIsGroup()==0){
		    		continue;
		    	}
		    	for(int j=1;j<gstartRows.size();j++){
		    		int gstart = ((Integer)gstartRows.get(j-1)).intValue();
		    		int gend = ((Integer)gstartRows.get(j)).intValue()-1;
		    		sheet.addMergedRegion(new CellRangeAddress(gstart,gend , i, i));
		    	}
		    }
		    if(rpt.getFoot()!=null){
		    	row = sheet.createRow(startRow+dtRows.size());
			    row.setHeight((short) Style.TABLECONTENT_HEIGHT);
			    String ftText = buildFoot(rpt.getFoot(),rpt,paraVals);
			    CellStyle csFoot = style.getSubHeadStyle(wb);
			    cell = row.createCell((short) 0);
			    cell.setCellValue(ftText);
			    sheet.addMergedRegion(new CellRangeAddress(startRow+dtRows.size(),startRow+dtRows.size(),0,(mheader.getLeafCount()-1)));
			    cell.setCellStyle(csFoot);
		    }
		    if(wb!=null){
		    	String filename = titleText==null?"导出":titleText;
		    	if("0".equals(eFormat)){
		    		filename += ".xls";
		    	}else{
		    		filename += ".xlsx";
		    	}
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
				if("0".equals(eFormat)){
					response.setContentType("application/vnd.ms-excel");
				}else{
					response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
				}
				wb.write(out);
				out.flush();
		    }
		}catch(Exception e){
			System.out.println(e.toString());
			try{
				if(out!=null)out.close();
			}catch(Exception oe){
				out=null;
			}
			throw new ParseReportToExcelException("导出Excel发生错误："+e.toString());
		}finally{
	    	out=null;
	    }
		return exp;
	}
	
	//按单元格的渲染设置，进一步加工样式
	private CellStyle cellStyleFormat(Column col,Workbook wb,boolean isContent,Style style,String unit){
		CellStyle cs=null;
		if(col.getDataType()==0){
			if(isContent){
				if("right".equals(col.getAlign())){
					cs = style.getStrContStyle_R(wb);
				}else if("center".equals(col.getAlign())){
					cs=style.getStrContStyle_C(wb);
				}else{
					cs=style.getStrContStyle(wb);
				}
			}else{
				cs=style.getStrSecStyle(wb);
			}
		}else{
			String render=col.getRenderer();
			if(!StringUtils.isEmpty(render)){
				if(isContent){
			    	if("rMoney".equals(render)||"rWan2Decimals".equals(render)){
			    		if("center".equals(col.getAlign())){
			    			cs=style.getMoneyContStyle_C(wb);
			    		}else if("left".equals(col.getAlign())){
			    			cs=style.getMoneyContStyle_L(wb);
			    		}else{
			    			cs=style.getMoneyContStyle(wb);
			    		}
			    	}else if("renderInt".equals(render)||"rWanInt".equals(render)){
			    		if("center".equals(col.getAlign())){
			    			cs=style.getIntContStyle_C(wb);
			    		}else if("left".equals(col.getAlign())){
			    			cs=style.getIntContStyle_L(wb);
			    		}else{
			    			cs=style.getIntContStyle(wb);
			    		}
			    	}else{
			    		if("center".equals(col.getAlign())){
			    			cs=style.getNumContStyle_C(wb);
			    		}else if("left".equals(col.getAlign())){
			    			cs=style.getNumContStyle_L(wb);
			    		}else{
			    			cs=style.getNumContStyle(wb);
			    		}
			    	}
				}else{
					if("rMoney".equals(render)||"rWan2Decimals".equals(render)){
			    		cs=style.getMoneySecStyle(wb);
			    	}else if("renderInt".equals(render)||"rWanInt".equals(render)){
			    		cs=style.getIntSecStyle(wb);
			    	}else{
			    		cs=style.getNumSecStyle(wb);
			    	}
				}
			}else if(col.getIsMultiUnit()==1){
				if("yuan2Decimals".equals(unit)||"wan2Decimals".equals(unit)){
					if(isContent){
						if("center".equals(col.getAlign())){
			    			cs=style.getMoneyContStyle_C(wb);
			    		}else if("left".equals(col.getAlign())){
			    			cs=style.getMoneyContStyle_L(wb);
			    		}else{
			    			cs=style.getMoneyContStyle(wb);
			    		}
					}else{
						cs=style.getMoneySecStyle(wb);
					}
				}else if("yuanInt".equals(unit)||"wanInt".equals(unit)){
					if(isContent){
						if("center".equals(col.getAlign())){
			    			cs=style.getIntContStyle_C(wb);
			    		}else if("left".equals(col.getAlign())){
			    			cs=style.getIntContStyle_L(wb);
			    		}else{
			    			cs=style.getIntContStyle(wb);
			    		}
					}else{
						cs=style.getIntSecStyle(wb);
					}
				}
			}else{
				if(isContent){
					if("center".equals(col.getAlign())){
		    			cs=style.getNumContStyle_C(wb);
		    		}else if("left".equals(col.getAlign())){
		    			cs=style.getNumContStyle_L(wb);
		    		}else{
		    			cs=style.getNumContStyle(wb);
		    		}
				}else{
					cs=style.getNumSecStyle(wb);
				}
			}
		}
		return cs;
	}
	//根据排序好的底级列集合，形成一个map，以列的数据字段（DataIndex）索引其在excel文档中的列号（A,B,C.....）
	private Map getColIndexMap(List leafCols){
		 //存放不同的列所在的列索引A、B、C......
	    Map mapColIndex=new HashMap();
	    byte si='A';
	    String colIndex="";
	    for(int i=0;i<leafCols.size();i++){
	    	Column col=(Column)leafCols.get(i);
	    	if(col.getDataIndex()!=null&&!"".equals(col.getDataIndex())){
	    		int fi=i/26;
		    	int li=i%26;
		    	if(fi>0){
		    		colIndex=String.valueOf((char)(fi-1+si))+String.valueOf((char)(li+si));
		    	}else{
		    		colIndex=String.valueOf((char)(li+si));
		    	}
	    		mapColIndex.put(col.getDataIndex(), String.valueOf(colIndex));
	    	}
	    }
	    return mapColIndex;
	}
	//将设计时的公式改写为单元格公式
	private String parseFunction(String oFunc,Map colMap,int rowIndex){
		if(oFunc==null||"".equals(oFunc)){
			return "";
		}
		String newFunc=oFunc;
		Pattern p=Pattern.compile("r.data.[a-zA-Z]\\w*");
		Matcher m=p.matcher(oFunc);
		while(m.find()){
			String re=m.group();
			String aCol=re.replaceAll("r.data.", "");
			String pos=(String)colMap.get(aCol)+(rowIndex+1);
			newFunc=newFunc.replaceAll("r.data."+aCol, pos);
		}
		return newFunc;
	}
	
	private String buildTitle(Title title,Report rpt,Map mParas){
		String texp="";
		try{
			texp = RptDataService.getReportDataService().parseParaExp(title.getTitleExp(), rpt, mParas);
		}catch(Exception e){}
		return texp;
	}
	private String buildSubTitle(SubTitle sbTitle,Report rpt,Map mParas){
		StringBuffer strSt=new StringBuffer("");
		String expl="",expc="",expr="";
		try{
			String l = sbTitle.getLeftExp();
			expl=RptDataService.getReportDataService().parseParaExp(l, rpt, mParas);
		}catch(Exception e){}
		try{
			String c = sbTitle.getCenterExp();
			expc=RptDataService.getReportDataService().parseParaExp(c, rpt, mParas);
		}catch(Exception e){}
		try{
			String r = sbTitle.getRightExp();
			expr=RptDataService.getReportDataService().parseParaExp(r, rpt, mParas);
		}catch(Exception e){}
		strSt.append(expl).append("     ").append(expc).append("     ").append(expr);
		return strSt.toString();
	}
	private String buildFoot(Foot foot,Report rpt,Map mParas){
		StringBuffer strFt=new StringBuffer("");
		String expl="",expc="",expr="";
		try{
			String l = foot.getLeftExp();
			expl=RptDataService.getReportDataService().parseParaExp(l, rpt, mParas);
		}catch(Exception e){}
		try{
			String c = foot.getCenterExp();
			expc=RptDataService.getReportDataService().parseParaExp(c, rpt, mParas);
		}catch(Exception e){}
		try{
			String r = foot.getRightExp();
			expr=RptDataService.getReportDataService().parseParaExp(r, rpt, mParas);
		}catch(Exception e){}
		strFt.append(expl).append("     ").append(expc).append("     ").append(expr);
		return strFt.toString();
	}
	//构造表头
	private void buildHeader(int startRow,int maxLevel,Sheet sheet,RptMultiHeader mheader,CellStyle csTbHead){
		List columns=mheader.getSortedNodes();
		short leafIndex=0;
		int MAXCOLS=250;
		int leafCounts = mheader.getLeafCount();
		//按层次创建行
		for(int i=startRow;i<maxLevel+startRow;i++){
			org.apache.poi.ss.usermodel.Row row = sheet.createRow(i);
		    row.setHeight((short) Style.TABLEHEAD_HEIGHT);
		    //按底级列数创建单元格，不能超过255列
		    if(leafCounts>MAXCOLS+1){
		    	leafCounts=MAXCOLS+1;
		    }
		    for(int j=0;j<leafCounts;j++){
		    	Cell cell = row.createCell((short)j);
			    cell.setCellStyle(csTbHead);
		    }
		}
		for(int i=startRow;i<maxLevel+startRow;i++){
			org.apache.poi.ss.usermodel.Row row = sheet.getRow(i);
			leafIndex=0;
		    for(int j=0;j<columns.size();j++){
		    	Column col=(Column)columns.get(j);
		    	if(col.getIsleaf()>0){
		    		leafIndex++;
		    	}
		    	if(leafIndex>=MAXCOLS)
		    		break;
		    	if(col.getLevel()!=i-startRow+1){
		    		continue;
		    	}
		    	Cell cell =null;
		    	if(col.getIsleaf()>0){
		    		cell= row.getCell((short)((leafIndex-1)>=MAXCOLS?MAXCOLS:(leafIndex-1)));
		    		sheet.setColumnWidth((leafIndex-1)>=MAXCOLS?MAXCOLS:(leafIndex-1), 35*col.getWidth());
		    	}else{
		    		cell= row.getCell((short)(leafIndex>=MAXCOLS?MAXCOLS:leafIndex));
		    	}
		    	cell.setCellValue(col.getColName());
		    	//底级节点才有跨行，非底级节点才有跨列
		    	if(col.getIsleaf()>0){
		    		sheet.addMergedRegion(new CellRangeAddress(i,maxLevel+startRow-1,(leafIndex-1)>=MAXCOLS?MAXCOLS:(leafIndex-1),(leafIndex-1)>=MAXCOLS?MAXCOLS:(leafIndex-1)));
		    	}else{
		    		int cspan=mheader.getColSpan(j+1,col.getLevel());
		    		sheet.addMergedRegion(new CellRangeAddress(i,i,leafIndex>=MAXCOLS?MAXCOLS:leafIndex,(leafIndex+cspan-1)>=MAXCOLS?MAXCOLS:(leafIndex+cspan-1)));
		    	}
		    }
		}
	}
	
	public DataSet getData(Report rpt,Map paraVals,String replaceSql,int start,int limit){
		DataSet ds = null;
		try{
			if(replaceSql==null||"".equals(replaceSql)){
				ds=RptDataService.getReportDataService().getReportDataPaging(rpt, paraVals, start, limit);
			}else{
				ds=RptDataService.getReportDataService().getReportDataPaging(rpt, paraVals, replaceSql, start, limit);
			}
		}catch(Exception e){
			System.out.println(e.toString());
		}
		return ds;
	}
}
