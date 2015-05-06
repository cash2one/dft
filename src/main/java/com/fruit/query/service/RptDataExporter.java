package com.fruit.query.service;

import java.io.*;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONObject;

import com.fruit.query.data.DataSet;
import com.fruit.query.data.ParaValue;
import com.fruit.query.parser.TemplatesLoader;
import com.fruit.query.report.*;
import com.fruit.query.util.QueryConfig;

public class RptDataExporter {
	private static Logger logger = Logger.getLogger(RptDataExporter.class);
	private static final String CONTENT_TYPE_XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
	private static final String CONTENT_TYPE = "application/vnd.ms-excel";
	private String templatesPath = "xlsTemplates";
	private String tmpName ="";
	private Workbook wbTemplate = null ; 
    private Workbook workbook = null ;
    private Sheet stTemplate = null ;
    private int format = 0;
    private List dynRowIncrements = new ArrayList();
    private Report report;
    private Map paraValues;
	private Map<String ,List>  dataSets;
	public RptDataExporter(){
    }  
    
    public void setFormat(int format){
    	this.format = format;
    	if(format==0){
			workbook = new HSSFWorkbook();
		}else{
			workbook = new XSSFWorkbook();
		}
    }
    /**
     * 无report模板时的构造函数。
     * 此时excel模板文件名从外部传入，通过模板路径+tmpName读取。
     * @param format
     */
    public RptDataExporter(int format){
    	this.format = format;
    	if(format==0){
			workbook = new HSSFWorkbook();
		}else{
			workbook = new XSSFWorkbook();
		}
    }
    /**
     * 设置模板集中存放的根目录
    * @param path
     */
    public void setTemplatesPath(String path){
    	this.templatesPath = path;
    }
    public String getTemplatesPath(){
    	String rootPath="";
    	if(StringUtils.isEmpty(this.templatesPath)){
    		rootPath = QueryConfig.getConfig().getString("xlsTemplatesPath", "xlsTemplates");
    	}else{
    		rootPath = this.templatesPath;
    	}
		String pre=rootPath.substring(0,1);
		if(!"/".equals(pre)){
			rootPath="/"+rootPath;
		}
		return rootPath;
    }
    /**
     * 设置本次导出的模板文件名
    * @param tmpName
     */
    public void setTmpName(String tmpName){
    	this.tmpName = tmpName;
    }
    /**
     * 有模板信息时的构造函数
     * @param rpt  导出模板名称、数据集取数描述都在report模板中。
     * @param format
     */
    public RptDataExporter(Report rpt,int format){
    	this.report = rpt;
    	this.tmpName = (rpt==null||rpt.getExportInfo()==null)?"":rpt.getExportInfo().getTemplate();
    	if(format==0){
			workbook = new HSSFWorkbook();
		}else{
			workbook = new XSSFWorkbook();
		}
    	
    }
    public RptDataExporter(Report rpt,int format,Map paraValues){
    	this.report = rpt;
    	this.tmpName = (rpt==null||rpt.getExportInfo()==null)?"":rpt.getExportInfo().getTemplate();
    	this.paraValues=paraValues;
    	if(format==0){
			workbook = new HSSFWorkbook();
		}else{
			workbook = new XSSFWorkbook();
		}
    	
    }
    public void setReportInfo(Report rpt){
    	this.report = rpt;
    }
    /**
     * 设置本次导出查询参数值
    * @param paraValues
     */
	public void setParaValues(Map paraValues) {
		this.paraValues = paraValues;
	}
	/**
	 * 设置本次导出的记录集。多个记录集时，以记录集名称索引记录集
	* @param dataSets
	 */
	public void setDataSets(Map<String ,List> dataSets) {
		this.dataSets = dataSets;
	}
    /**
     * 构造输出文件名
    * @return
     */
    public String getTitle(){
		String title = "";
		if(this.report!=null){
			if(report.getExportInfo()!=null&&report.getExportInfo().getExpFileName()!=null){
				title = report.getExportInfo().getExpFileName();
			}else if(report.getTitle()!=null){
				title = report.getTitle().getTitleExp();
			}
		}else{
			title  = this.tmpName;
		}
		RptDataService rsvr = RptDataService.getReportDataService();
		String parsedTitle = title;
		try{
			parsedTitle = rsvr.parseParaExp(title,this.report,paraValues);
		}catch(Exception e){
			logger.error("导出Excel，解析导出文件名时发生错误："+e.toString());
		}
    	return parsedTitle;
	}
    public void loadTemplate(String path) throws Exception {  
        wbTemplate = WorkbookFactory.create(new File(path)) ;  
        stTemplate = wbTemplate.getSheetAt(0);
    }
    /**
     * 导出描述在报表模板中的情形。先根据描述获取数据。
     * 无取数模板时，需要在导出前设置记录集。setDataSets
    * @param agent
    * @param response
    * @param rpt
    * @param paraVals
     */
    public void export(String agent,HttpServletResponse response,Report rpt,Map paraVals){
		if(rpt==null){
			logger.error("未设置报表模板定义！导出中断！");
			return;
		}
    	if(rpt.getExportInfo()==null){
    		logger.error("未定义导出信息！导出中断！");
			return;
    	}
    	if(StringUtils.isEmpty(rpt.getExportInfo().getTemplate())){
    		logger.error("未设置导出模板名称！导出中断！");
			return;
    	}
    	this.report=rpt;
    	this.tmpName = rpt.getExportInfo().getTemplate();
    	this.paraValues = paraVals;
    	//导出
    	export(agent, response);
	}
    public void export(String agent,HttpServletResponse response){
    	String path = getTemplatesPath();
    	URL rootP=RptDataExporter.class.getClassLoader().getResource(path); 
		if(rootP==null){
			System.out.println("Excel模板目录为空！");
			return;
		}
    	try{
    		path = rootP.toURI().getPath();
        	path =path+(path.endsWith("/")?"":"/")+tmpName;
	    	loadTemplate(path);
	    	Sheet tsheet = workbook.createSheet();
	    	int startRow = stTemplate.getFirstRowNum();
	        int rowNum = stTemplate.getLastRowNum();
	        int addedRows = 0;
	        short rowHeight = 0;
			for(int i = startRow; i <= rowNum; i++) {
				Row row = stTemplate.getRow(i);
				if(row==null){
					continue;
				}
				int baseRIndex = addedRows+i;
				Row newRow = tsheet.createRow(baseRIndex);
				int colNum = row.getPhysicalNumberOfCells();
				rowHeight = row.getHeight();
				newRow.setHeight(rowHeight);
				//模板中的列复制到目标sheet
				copyRow(workbook, tsheet,row,newRow,null,null,true);
				for (int j = 0; j < colNum; j++) {
					Cell cell = newRow.getCell(j);
					if(cell==null){
						continue;
					}
					String result = cell.getStringCellValue();
					if(!StringUtils.isEmpty(result)){
						if(result.startsWith("$")){
							int dotIdx = result.indexOf(".");
							String strCls = result.substring(1,dotIdx);
							String pro = result.substring(dotIdx+1);
							Map md = getMapData(strCls);
							setValueOfCell(md,pro,cell);
						}else if(result.startsWith("#")){
							//遇到#开头的字段，就开始构造动态行，并停止当前行的cell循环，统一在动态行中处理。
							int dotIdx = result.indexOf(".");
							String strCls = result.substring(1,dotIdx);
							List lst = getListData(strCls);
							List rows = new ArrayList();
							rows.add(newRow);
							//构造新的动态行，构造的数量要减去原有的配置 行（一行）
							if(lst!=null&&lst.size()>1){
								//把模板行的各个cellStyle、合并单元格区域缓存，动态扩展就不需要反复创建。
								Map stylesMap = getCellStyleMap(row);
								List mRegions = getMergeRegions(row);
								for(int p=1; p<lst.size();p++){
									Row extraRow = tsheet.createRow(baseRIndex+p);
									extraRow.setHeight(rowHeight);
									copyRow(workbook,tsheet,row,extraRow,stylesMap,mRegions,true);
									addedRows++;
									rows.add(extraRow);
								}
								int[] arr = new int[]{i,addedRows};
								dynRowIncrements.add(arr);
							}
							setValueOfDynamicRow(lst,rows);
							break;
						}				
					}
				}
			}
			//mergerRegion(stTemplate, tsheet);
			writeToStream(agent,response);
    	}catch(Exception e){
    		logger.error("导出Excel过程中发生错误："+e.toString());
    	}
    }
    private Map getCellStyleMap(Row row){
    	Map csMap = new HashMap();
    	for (Iterator cellIt = row.cellIterator(); cellIt.hasNext();) {  
            Cell srcCell = (Cell) cellIt.next();  
            int cidx = srcCell.getColumnIndex();
            CellStyle newCellStyle = workbook.createCellStyle();
            newCellStyle.cloneStyleFrom(srcCell.getCellStyle());
            csMap.put(cidx, newCellStyle);
        }  
    	return csMap;
    }
    private List getMergeRegions(Row row){
    	List mrs = new ArrayList();
    	
    	return mrs;
    }
    
    //获取列表数据
    private List getListData(String strCls)throws Exception{
    	List lst = null;
    	if(dataSets==null){
    		dataSets = new HashMap<String,List>();
    	}
    	if(!dataSets.containsKey(strCls)){	
    		loadNamedData(strCls);
    	}
    	if(dataSets!=null&&dataSets.containsKey(strCls)){
    		lst = dataSets.get(strCls);
    	}
    	return lst;
    }
   //获取单个对象数据。记录集如果已缓存，直接使用，如果没有，则按名称查询想要的记录集
    private Map getMapData(String strCls)throws Exception{
    	Map m = null;
    	if(dataSets==null){
    		dataSets = new HashMap<String,List>();
    	}
    	if(!dataSets.containsKey(strCls)){	
    		loadNamedData(strCls);
    	}
    	if(dataSets!=null&&dataSets.containsKey(strCls)){
    		m = ((com.fruit.query.data.Row)(dataSets.get(strCls)).get(0)).getCells();
    	}
    	return m;
    }
    
    private void loadNamedData(String dtName)throws Exception{
    	//在默认数据集合、其他数据集中，寻找指定名称的记录集并加载
		if(this.report!=null&&this.report.getDefaultDataDef()!=null&&dtName.equals(this.report.getDefaultDataDef().getName())){
			List dt = queryData(this.report.getDefaultDataDef());
			dataSets.put(dtName, dt);
		}else if(this.report!=null&&this.report.getDataDefines()!=null){
			List dfs = this.report.getDataDefines();
			for(int i=0;i<dfs.size();i++){
				DataDefine df = (DataDefine)dfs.get(i);
				if(dtName.equals(df.getName())){
					List dt =  queryData(df);
					dataSets.put(dtName, dt);
					break;
				}
			}
		}
    }
    /**
     * 查询指定的记录集
    * @param rpt
    * @param df
    * @param paraVals
    * @return
    * @throws Exception
     */
    public List queryData(DataDefine df)throws Exception{
    	DataSet ds = null;
    	if(df!=null&&df.getCanPaging()==1){
			ds=RptDataService.getReportDataService().getReportDataPagingByDataDefine(this.report,df, paraValues, 0, 65536);
		}else{
			ds=RptDataService.getReportDataService().getReportDataByDataDefine(this.report,df,paraValues);
		}
    	return ds==null?null:ds.getRows();
    }
    
    private void setValueOfCell(Map md,String pro,Cell cell) {
		if(md!=null&&md.containsKey(pro)){  
            String strVal = md.get(pro)==null?"":(String)md.get(pro);
            CellStyle cst = cell.getCellStyle();
            int cType = cell.getCellType();
            if (cType == Cell.CELL_TYPE_NUMERIC) {  
                if (DateUtil.isCellDateFormatted(cell)) {  
                    cell.setCellValue(strVal);  
                } else {
                	int dotIdx = strVal.indexOf(".");
                	if(dotIdx>=0){
                		double dval = 0;
                		try{
                			dval=Double.parseDouble(strVal);
                		}catch(Exception e){
                		}
                		 cell.setCellValue(dval); 
                	}else{
                		int ival = 0;
                		try{
                			ival = Integer.parseInt(strVal);
                		}catch(Exception e){
                		}
                		cell.setCellValue(ival); 
                	}
                   
                }  
            } else if (cType == Cell.CELL_TYPE_STRING) {  
                cell.setCellValue(strVal);  
            } else if (cType == Cell.CELL_TYPE_BLANK) { 
            	cell.setCellValue("");
            }else{
            	cell.setCellValue(strVal);
            }
        }
	}
    public void setValueOfDynamicRow(List datas,List rows) {  
        if(datas == null) return ;  
        for(int i=0;i<rows.size();i++) {
        	Row row = (Row)rows.get(i);
        	com.fruit.query.data.Row dtrow=(com.fruit.query.data.Row)datas.get(i);
        	Map md = dtrow.getCells();
            for(Cell cell : row) {  
            	String result = cell.getStringCellValue();
            	if("".equals(result)){
            		continue;
            	}
            	int dotIdx = result.indexOf(".");
				String pro = result.substring(dotIdx+1);
            	setValueOfCell(md,pro,cell);
            }  
        }  
    }    
    /** 
     * 复制一个单元格样式到目的单元格样式 
     * @param fromStyle 
     * @param toStyle 
     */  
//    public void copyCellStyle(CellStyle fromStyle,CellStyle toStyle) {  
//        toStyle.setAlignment(fromStyle.getAlignment());  
//        //边框和边框颜色  
//        toStyle.setBorderBottom(fromStyle.getBorderBottom());  
//        toStyle.setBorderLeft(fromStyle.getBorderLeft());  
//        toStyle.setBorderRight(fromStyle.getBorderRight());  
//        toStyle.setBorderTop(fromStyle.getBorderTop());  
//        toStyle.setTopBorderColor(fromStyle.getTopBorderColor());  
//        toStyle.setBottomBorderColor(fromStyle.getBottomBorderColor());  
//        toStyle.setRightBorderColor(fromStyle.getRightBorderColor());  
//        toStyle.setLeftBorderColor(fromStyle.getLeftBorderColor());
//        //toStyle.setFont(wbTemplate.getFontAt(fromStyle.getFontIndex()));
//        Font srcf = wbTemplate.getFontAt(fromStyle.getFontIndex());
//        Font f = workbook.createFont(); 
//        f.setFontHeightInPoints(srcf.getFontHeightInPoints()); //设置字体大小 
//        f.setColor(srcf.getColor()); //设置字体颜色 
//        f.setFontName(srcf.getFontName()); //设置子是什么字体（如宋体） 
//        f.setBoldweight(srcf.getBoldweight()); //设置粗体  
//        f.setUnderline(srcf.getUnderline());
//        f.setItalic(srcf.getItalic());
//        f.setFontName(srcf.getFontName());
//        toStyle.setFont(f);
//		toStyle.setDataFormat(fromStyle.getDataFormat());
//		toStyle.setAlignment(fromStyle.getAlignment());
//		toStyle.setVerticalAlignment(fromStyle.getVerticalAlignment());
//        //背景和前景  
//        toStyle.setFillBackgroundColor(fromStyle.getFillBackgroundColor());  
//        toStyle.setFillForegroundColor(fromStyle.getFillForegroundColor());  
//        toStyle.setDataFormat(fromStyle.getDataFormat());  
//        toStyle.setFillPattern(fromStyle.getFillPattern());  
//        toStyle.setHidden(fromStyle.getHidden());  
//        toStyle.setIndention(fromStyle.getIndention());//首行缩进  
//        toStyle.setLocked(fromStyle.getLocked());  
//        toStyle.setRotation(fromStyle.getRotation());//旋转  
//        toStyle.setVerticalAlignment(fromStyle.getVerticalAlignment());  
//        toStyle.setWrapText(fromStyle.getWrapText());  
//    }  
    /** 
     * 行复制功能 
     * @param fromRow 
     * @param toRow 
     */  
    public void copyRow(Workbook wb,Sheet tsheet,Row fromRow,Row toRow,Map stylesMap,List mRegions,boolean copyValueFlag){  
        for (Iterator cellIt = fromRow.cellIterator(); cellIt.hasNext();) {  
            Cell tmpCell = (Cell) cellIt.next();  
            int cidx = tmpCell.getColumnIndex();
            Cell newCell = toRow.createCell(cidx); 
            tsheet.setColumnWidth(cidx,stTemplate.getColumnWidth(cidx));
            copyCell(wb, tmpCell, newCell,cidx,stylesMap,copyValueFlag);  
        } 
        if(mRegions==null||mRegions.size()==0){
	        //源行中如果有合并单元格的内容，也复制过来
	        for (int i = 0; i < stTemplate.getNumMergedRegions(); i++) {
	            CellRangeAddress cra = stTemplate.getMergedRegion(i);
	            if (cra.getFirstRow() == fromRow.getRowNum()) {
	                CellRangeAddress nCra = new CellRangeAddress(toRow.getRowNum(),
	                        (toRow.getRowNum() +
	                                (cra.getLastRow() - cra.getFirstRow()
	                                        )),
	                        cra.getFirstColumn(),
	                        cra.getLastColumn());
	                tsheet.addMergedRegion(nCra);
	            }
	        }
        }else{
        	for(int i=0;i<mRegions.size();i++){
        		CellRangeAddress cra = (CellRangeAddress)mRegions.get(i);
        		CellRangeAddress nCra = new CellRangeAddress(toRow.getRowNum(),
                        (toRow.getRowNum() +
                                (cra.getLastRow() - cra.getFirstRow()
                                        )),
                        cra.getFirstColumn(),
                        cra.getLastColumn());
                tsheet.addMergedRegion(nCra);
        	}
        }
    }  
    /** 
    * 复制原有sheet的合并单元格到新创建的sheet 
    *  
    * @param sheetCreat 新创建sheet 
    * @param sheet      原有的sheet 
    */  
    public void mergerRegion(Sheet fromSheet,Sheet toSheet) {  
        int sheetMergerCount = fromSheet.getNumMergedRegions();  
        for (int i = 0; i < sheetMergerCount; i++) {  
        	CellRangeAddress mergedRegion= fromSheet.getMergedRegion(i); 
        	int startRow =  mergedRegion.getFirstRow();
        	int endRow = mergedRegion.getLastRow();
        	int incre= 0;
        	for(int j=0;j<dynRowIncrements.size();j++){
        		int[] arr = (int[])dynRowIncrements.get(j);
        		if(startRow>arr[0]){
        			incre = incre +arr[1];
        		}
        	}
        	startRow = startRow + incre;
        	endRow = endRow + incre;
        	mergedRegion.setFirstRow(startRow);
        	mergedRegion.setLastRow(endRow);
            toSheet.addMergedRegion(mergedRegion);  
       }  
    }  
    /** 
     * 复制单元格 
     * 
     * @param srcCell 
     * @param distCell 
     * @param copyValueFlag 
     * true则连同cell的内容一起复制 
     */  
    public void copyCell(Workbook wb,Cell srcCell, Cell distCell,int cidx,Map stylesMap,boolean copyValueFlag) {  
        //复制样式、注释，链接等
        CellStyle newCellStyle = null;
        if(stylesMap==null){
        	newCellStyle = wb.createCellStyle();
        	newCellStyle.cloneStyleFrom(srcCell.getCellStyle());
        }else{
        	newCellStyle = (CellStyle)stylesMap.get(cidx);
        }
        distCell.setCellStyle(newCellStyle);
        if (srcCell.getCellComment() != null) {
        	distCell.setCellComment(srcCell.getCellComment());
        }
        if (srcCell.getHyperlink() != null) {
        	distCell.setHyperlink(srcCell.getHyperlink());
        }
        distCell.setCellType(srcCell.getCellType());
       
        // 不同数据类型处理  
        int srcCellType = srcCell.getCellType();  
        //distCell.setCellType(srcCellType);  
        if(copyValueFlag) {  
            if (srcCellType == Cell.CELL_TYPE_NUMERIC) {  
                if (DateUtil.isCellDateFormatted(srcCell)) {  
                    distCell.setCellValue(srcCell.getDateCellValue());  
                } else {  
                    distCell.setCellValue(srcCell.getNumericCellValue());  
                }  
            } else if (srcCellType == Cell.CELL_TYPE_STRING) {  
                distCell.setCellValue(srcCell.getRichStringCellValue());  
            } else if (srcCellType == Cell.CELL_TYPE_BLANK) {  
            } else if (srcCellType == Cell.CELL_TYPE_BOOLEAN) {  
                distCell.setCellValue(srcCell.getBooleanCellValue());  
            } else if (srcCellType == Cell.CELL_TYPE_ERROR) {  
                distCell.setCellErrorValue(srcCell.getErrorCellValue());  
            } else if (srcCellType == Cell.CELL_TYPE_FORMULA) {  
                distCell.setCellFormula(srcCell.getCellFormula());  
            } else {
            }  
        }  
    } 
    /** 
     * 输出文件,根据流输出 
     * @param stream OutputStream 
     */  
    public void writeToStream(String agent,HttpServletResponse response) {  
    	ServletOutputStream out = null;
    	String tt = getTitle();
        try {  
        	out = response.getOutputStream();
        	if(format==1){
	    		tt += ".xlsx";
	    	}else{
	    		tt += ".xls";
	    	}
			if (null != agent && -1 != agent.indexOf("MSIE")) {
				tt = URLEncoder.encode(tt, "UTF8");
			} else if (null != agent && -1 != agent.indexOf("Mozilla")) {
				tt = new String(tt.getBytes("UTF-8"), "ISO8859-1");
			} else {
				tt = URLEncoder.encode(tt, "UTF8");
			}
			
			response.setContentType("application/x-download");
			response.setHeader("Content-Disposition", "attachment;filename="+ tt);
			if(format==1){
				response.setContentType(CONTENT_TYPE_XLSX);
			}else{
				response.setContentType(CONTENT_TYPE);
			}
			workbook.write(out);
			out.flush();
        } catch (IOException e) {  
            e.printStackTrace();  
            throw new RuntimeException("文件输出异常!请检查.") ;  
        } finally {  
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
    
//    public static void main(String[] args){
//    	Map dts = new HashMap();
//    	List sgRd = new ArrayList();
//    	Map bd = new HashMap();
//    	bd.put("zjz", "建筑物11");
//    	bd.put("zjzmj", "建筑总面积20000亩");
//    	bd.put("yj", "已建11");
//    	bd.put("zj", "在建22");
//    	bd.put("wj", "未建33");
//    	bd.put("yjmj", "已建面积10000亩");
//    	bd.put("zjmj", "在建面积4000亩");
//    	bd.put("wjmj", "未建面积6000亩");
//    	sgRd.add(bd);
//    	dts.put("BuildingProgress", sgRd);
//    	List mtRd = new ArrayList();
//    	Map fp = new HashMap();
//    	fp.put("ptime", "2010-10-01");
//    	fp.put("remark", "2010年进展");
//    	Map sp = new HashMap();
//    	fp.put("ptime", "2011-11-01");
//    	fp.put("", "2011年进展");
//    	Map tp = new HashMap();
//    	tp.put("ptime", "2012-11-01");
//    	tp.put("", "2012年进展");
//    	mtRd.add(fp);
//    	mtRd.add(sp);
//    	mtRd.add(tp);
//    	dts.put("ProjectProgress", mtRd);
//    	try{
//    		URL rootP=RptDataExporter.class.getClassLoader().getResource("xlsTemplates"); 
//    		String rpath =rootP.toURI().getPath();
//    		String path=rpath+"/test.xls";
//    		String npath = rpath+"/test_"+System.currentTimeMillis()+".xls";
//    		try {  
//                java.io.FileInputStream in = new java.io.FileInputStream(path);  
//                java.io.FileOutputStream out = new java.io.FileOutputStream(npath);  
//                byte[] bt = new byte[1024];  
//                int count;  
//                while ((count = in.read(bt)) > 0) {  
//                    out.write(bt, 0, count);  
//                }  
//                in.close();  
//                out.close();  
//            } catch (IOException ex) {  
//            }  
//	    	Workbook nw = WorkbookFactory.create(new File(npath)); 
//	    	Sheet ns =  nw.getSheetAt(0);
//	    	int startRow = ns.getFirstRowNum();
//	        int rowNum = ns.getLastRowNum();
//	        for(int i = startRow; i <= rowNum; i++) {
//	        	Row row = ns.getRow(i);
//				if(row==null){
//					continue;
//				}
//				int colNum = row.getPhysicalNumberOfCells();
//				for (int j = 0; j < colNum; j++) {
//					Cell cell = row.getCell(j);
//					if(cell==null){
//						continue;
//					}
//					String result = cell.getStringCellValue();
//					if(!StringUtils.isEmpty(result)){
//						if(result.startsWith("$")){
//							int dotIdx = result.indexOf(".");
//							String strCls = result.substring(1,dotIdx);
//							String pro = result.substring(dotIdx+1);
//							Map md = (Map)((List)dts.get(strCls)).get(0);
//							if(md!=null&&md.containsKey(pro)){  
//					            cell.setCellValue(md.get(pro)==null?"":md.get(pro).toString()) ;  
//					        }
//						}else if(result.startsWith("#")){
//							//遇到#开头的字段，就开始构造动态行，并停止当前行的cell循环，统一在动态行中处理。
//							int dotIdx = result.indexOf(".");
//							String strCls = result.substring(1,dotIdx);
//							String pro = result.substring(dotIdx+1);
//							List lst = (List)dts.get(strCls);
//							if(lst!=null&&lst.size()>0){
//						        for(int k=1;k<lst.size();k++){
//									copyRow(nw,ns,i,i+k);
//								}
//								//值替换
//								for(int ri=i;ri<i+lst.size();ri++){
//									Map md = (Map)lst.get(ri-i);
//									Row dRow = ns.getRow(ri);
//									int dcNum = dRow.getPhysicalNumberOfCells();
//									for(int di = 0;di<dcNum;di++){
//										Cell newCell = dRow.getCell(di);
//										String cstr = newCell.getStringCellValue();
//										int dIdx = cstr.indexOf(".");
//										String fld = cstr.substring(dIdx+1);
//										String sval = md.get(fld)==null?"":md.get(fld).toString();
//										if(md!=null&&md.containsKey(fld)){  
//											switch (newCell.getCellType()) {
//								                case Cell.CELL_TYPE_BLANK:
//								                    newCell.setCellValue("");
//								                    break;
//								                case Cell.CELL_TYPE_NUMERIC:
//								                	double dval = 0;
//								                	try{
//								                		dval = Double.parseDouble(sval);
//								                	}catch(Exception e){
//								                	}
//								                    newCell.setCellValue(dval);
//								                    break;
//								                case Cell.CELL_TYPE_STRING:
//								                    newCell.setCellValue(sval);
//								                    break;
//											} 
//										}
//							        }
//								}
//								rowNum +=lst.size();
//								i=i+lst.size()-1;
//							}else{
//								cell.setCellValue("");
//							}
//						}
//					}
//				}
//			}
//	        FileOutputStream fos = null;  
//	        try{  
//	            fos = new FileOutputStream("xxx.xls") ;  
//	            nw.write(fos) ;  
//	        }catch(FileNotFoundException e) {  
//	            e.printStackTrace();  
//	            throw new RuntimeException("找不到文件!请检查.") ;  
//	        }catch(IOException e) {  
//	            e.printStackTrace();  
//	            throw new RuntimeException("文件输出异常!请检查.") ;  
//	        }finally{  
//	            try {  
//	                if(fos != null) {  
//	                    fos.close() ;  
//	                    fos = null ;  
//	                }  
//	            } catch (IOException e) {  
//	                e.printStackTrace();  
//	            }  
//	        }  
//    	}catch(Exception e){
//    	}
//    }
//
//    private static void copyRow(Workbook workbook, Sheet worksheet, int sourceRowNum, int destinationRowNum) {
//        Row newRow = worksheet.getRow(destinationRowNum);
//        Row sourceRow = worksheet.getRow(sourceRowNum);
//        //如果目标行已存在，把它下移一行。如果目标行不存在，则新建一行
//        if (newRow != null) {
//            worksheet.shiftRows(destinationRowNum, worksheet.getLastRowNum(), 1);
//        } else {
//            newRow = worksheet.createRow(destinationRowNum);
//        }
//        //循环复制源行的单元格
//        for (int i = 0; i < sourceRow.getLastCellNum(); i++) {
//            Cell oldCell = sourceRow.getCell(i);
//            Cell newCell = newRow.createCell(i);
//            if (oldCell == null) {
//                newCell = null;
//                continue;
//            }
//            //复制样式、注释，链接等
//            CellStyle newCellStyle = workbook.createCellStyle();
//            newCellStyle.cloneStyleFrom(oldCell.getCellStyle());
//            newCell.setCellStyle(newCellStyle);
//            if (oldCell.getCellComment() != null) {
//                newCell.setCellComment(oldCell.getCellComment());
//            }
//            if (oldCell.getHyperlink() != null) {
//                newCell.setHyperlink(oldCell.getHyperlink());
//            }
//            newCell.setCellType(oldCell.getCellType());
//            switch (oldCell.getCellType()) {
//                case Cell.CELL_TYPE_BLANK:
//                    newCell.setCellValue(oldCell.getStringCellValue());
//                    break;
//                case Cell.CELL_TYPE_BOOLEAN:
//                    newCell.setCellValue(oldCell.getBooleanCellValue());
//                    break;
//                case Cell.CELL_TYPE_ERROR:
//                    newCell.setCellErrorValue(oldCell.getErrorCellValue());
//                    break;
//                case Cell.CELL_TYPE_FORMULA:
//                    newCell.setCellFormula(oldCell.getCellFormula());
//                    break;
//                case Cell.CELL_TYPE_NUMERIC:
//                    newCell.setCellValue(oldCell.getNumericCellValue());
//                    break;
//                case Cell.CELL_TYPE_STRING:
//                    newCell.setCellValue(oldCell.getRichStringCellValue());
//                    break;
//            }
//        }
//        //源行中如果有合并单元格的内容，也复制过来
//        for (int i = 0; i < worksheet.getNumMergedRegions(); i++) {
//            CellRangeAddress cellRangeAddress = worksheet.getMergedRegion(i);
//            if (cellRangeAddress.getFirstRow() == sourceRow.getRowNum()) {
//                CellRangeAddress newCellRangeAddress = new CellRangeAddress(newRow.getRowNum(),
//                        (newRow.getRowNum() +
//                                (cellRangeAddress.getLastRow() - cellRangeAddress.getFirstRow()
//                                        )),
//                        cellRangeAddress.getFirstColumn(),
//                        cellRangeAddress.getLastColumn());
//                worksheet.addMergedRegion(newCellRangeAddress);
//            }
//        }
//    }
}
