package com.ifugle.dft.utils;

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.*;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * Excel导出使用的格式信息
 */
public class Style{
  public static short TITLE_HEIGHT=600;
  public static short SUBHEAD_HEIGHT = 400;
  public static short TABLEHEAD_HEIGHT=400;
  public static short TABLECONTENT_HEIGHT=500;
  private CellStyle titleStyle;
  private CellStyle tbHeadStyle;
  private CellStyle numContStyle;
  private CellStyle numContStyle_C;
  private CellStyle numContStyle_L;
  private CellStyle moneyContStyle;
  private CellStyle moneyContStyle_C;
  private CellStyle moneyContStyle_L;
  private CellStyle numSecStyle;
  private CellStyle moneySecStyle;
  private CellStyle strContStyle;
  private CellStyle strContStyle_C;
  private CellStyle strContStyle_R;
  private CellStyle strSecStyle;
  private CellStyle subHeadStyle;
  private CellStyle intContStyle;
  private CellStyle intContStyle_C;
  private CellStyle intContStyle_L;
  private CellStyle intSecStyle;
  private CellStyle longStrStyle;
  /**
   * 表头标题格式，水平，垂直居中，无表格线，加粗
   * @param wb Workbook
   * @return CellStyle
   */
  public CellStyle getTitleStyle(Workbook wb){
	  if(titleStyle==null){
		  titleStyle = wb.createCellStyle();
		  Font fTitle = wb.createFont();
		  fTitle.setFontHeightInPoints((short) 16);
		  fTitle.setColor(Font.COLOR_NORMAL );
		  fTitle.setBoldweight(Font.BOLDWEIGHT_BOLD);
		  titleStyle.setFont(fTitle);
		  titleStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("text"));
		  titleStyle.setAlignment(CellStyle.ALIGN_CENTER);
		  titleStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
	  }
	  return titleStyle;
  }
  /**
   * 表头格式，水平、垂直居中，有表格线，加粗，自动换行
   * @param wb Workbook
   * @return CellStyle
   */
  public CellStyle getTbHeadStyle(Workbook wb){
	if(tbHeadStyle==null){
		tbHeadStyle = wb.createCellStyle();
	    Font fHead  = wb.createFont();
	    fHead.setFontHeightInPoints((short) 10);
	    fHead.setColor(Font.COLOR_NORMAL );
	    tbHeadStyle.setFont(fHead);
	    fHead.setBoldweight(Font.BOLDWEIGHT_BOLD);       //加粗
	    tbHeadStyle.setAlignment(CellStyle.ALIGN_CENTER);         //水平居中
	    tbHeadStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
	    tbHeadStyle.setWrapText(true);
	    tbHeadStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("text"));
	    tbHeadStyle.setBorderBottom(CellStyle.BORDER_THIN);
	    tbHeadStyle.setBorderLeft(CellStyle.BORDER_THIN);
	    tbHeadStyle.setBorderRight(CellStyle.BORDER_THIN);
	    tbHeadStyle.setBorderTop(CellStyle.BORDER_THIN);
	}
    return tbHeadStyle;
  }

  /**
   * 数字正文内容格式，水平靠右，垂直居中，有表格线，字体为Arial
   * @param wb Workbook
   * @return CellStyle
   */
  public CellStyle getNumContStyle(Workbook wb){
	  if(numContStyle==null){
		  numContStyle = wb.createCellStyle();
		  Font fLnode = wb.createFont();
		  fLnode.setFontName("Arial");
		  fLnode.setFontHeightInPoints( (short) 9);
		  fLnode.setBoldweight(Font.BOLDWEIGHT_NORMAL);       //不加粗
		  numContStyle.setFont(fLnode);
		  numContStyle.setAlignment(CellStyle.ALIGN_RIGHT);             //水平靠右
		  numContStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		  numContStyle.setBorderBottom(CellStyle.BORDER_THIN);
		  numContStyle.setBorderLeft(CellStyle.BORDER_THIN);
		  numContStyle.setBorderRight(CellStyle.BORDER_THIN);
		  numContStyle.setBorderTop(CellStyle.BORDER_THIN);
		  numContStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("General"));
	  }
	  return numContStyle;
  }
  public CellStyle getNumContStyle_C(Workbook wb){
	  if(numContStyle_C==null){
		  numContStyle_C = wb.createCellStyle();
		  Font fLnode = wb.createFont();
		  fLnode.setFontName("Arial");
		  fLnode.setFontHeightInPoints( (short) 9);
		  fLnode.setBoldweight(Font.BOLDWEIGHT_NORMAL);       //不加粗
		  numContStyle_C.setFont(fLnode);
		  numContStyle_C.setAlignment(CellStyle.ALIGN_CENTER);             //水平靠右
		  numContStyle_C.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		  numContStyle_C.setBorderBottom(CellStyle.BORDER_THIN);
		  numContStyle_C.setBorderLeft(CellStyle.BORDER_THIN);
		  numContStyle_C.setBorderRight(CellStyle.BORDER_THIN);
		  numContStyle_C.setBorderTop(CellStyle.BORDER_THIN);
		  numContStyle_C.setDataFormat(HSSFDataFormat.getBuiltinFormat("General"));
	  }
	  return numContStyle_C;
  }
  public CellStyle getNumContStyle_L(Workbook wb){
	  if(numContStyle_L==null){
		  numContStyle_L = wb.createCellStyle();
		  Font fLnode = wb.createFont();
		  fLnode.setFontName("Arial");
		  fLnode.setFontHeightInPoints( (short) 9);
		  fLnode.setBoldweight(Font.BOLDWEIGHT_NORMAL);       //不加粗
		  numContStyle_L.setFont(fLnode);
		  numContStyle_L.setAlignment(CellStyle.ALIGN_LEFT);             //水平靠右
		  numContStyle_L.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		  numContStyle_L.setBorderBottom(CellStyle.BORDER_THIN);
		  numContStyle_L.setBorderLeft(CellStyle.BORDER_THIN);
		  numContStyle_L.setBorderRight(CellStyle.BORDER_THIN);
		  numContStyle_L.setBorderTop(CellStyle.BORDER_THIN);
		  numContStyle_L.setDataFormat(HSSFDataFormat.getBuiltinFormat("General"));
	  }
	  return numContStyle_L;
  }
  /**
   * 货币正文内容格式，水平靠右，垂直居中，有表格线，字体为Arial
   * @param wb
   * @return CellStyle
   */
  public CellStyle getMoneyContStyle(Workbook wb){
	  if(moneyContStyle==null){
		  moneyContStyle = wb.createCellStyle();
	      Font fLnode = wb.createFont();
	      fLnode.setFontName("Arial");
	      fLnode.setFontHeightInPoints( (short) 9);
	      fLnode.setBoldweight(Font.BOLDWEIGHT_NORMAL);       //不加粗
	      moneyContStyle.setFont(fLnode);
	      moneyContStyle.setAlignment(CellStyle.ALIGN_RIGHT);             //水平靠右
	      moneyContStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
	      moneyContStyle.setBorderBottom(CellStyle.BORDER_THIN);
	      moneyContStyle.setBorderLeft(CellStyle.BORDER_THIN);
	      moneyContStyle.setBorderRight(CellStyle.BORDER_THIN);
	      moneyContStyle.setBorderTop(CellStyle.BORDER_THIN);
	      moneyContStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("#,##0.00"));
	  }
      return moneyContStyle;
  }
  public CellStyle getMoneyContStyle_C(Workbook wb){
	  if(moneyContStyle_C==null){
		  moneyContStyle_C = wb.createCellStyle();
	      Font fLnode = wb.createFont();
	      fLnode.setFontName("Arial");
	      fLnode.setFontHeightInPoints( (short) 9);
	      fLnode.setBoldweight(Font.BOLDWEIGHT_NORMAL);       //不加粗
	      moneyContStyle_C.setFont(fLnode);
	      moneyContStyle_C.setAlignment(CellStyle.ALIGN_CENTER);             //水平靠右
	      moneyContStyle_C.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
	      moneyContStyle_C.setBorderBottom(CellStyle.BORDER_THIN);
	      moneyContStyle_C.setBorderLeft(CellStyle.BORDER_THIN);
	      moneyContStyle_C.setBorderRight(CellStyle.BORDER_THIN);
	      moneyContStyle_C.setBorderTop(CellStyle.BORDER_THIN);
	      moneyContStyle_C.setDataFormat(HSSFDataFormat.getBuiltinFormat("#,##0.00"));
	  }
      return moneyContStyle_C;
  }
  public CellStyle getMoneyContStyle_L(Workbook wb){
	  if(moneyContStyle_L==null){
		  moneyContStyle_L = wb.createCellStyle();
	      Font fLnode = wb.createFont();
	      fLnode.setFontName("Arial");
	      fLnode.setFontHeightInPoints( (short) 9);
	      fLnode.setBoldweight(Font.BOLDWEIGHT_NORMAL);       //不加粗
	      moneyContStyle_L.setFont(fLnode);
	      moneyContStyle_L.setAlignment(CellStyle.ALIGN_LEFT);             //水平靠右
	      moneyContStyle_L.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
	      moneyContStyle_L.setBorderBottom(CellStyle.BORDER_THIN);
	      moneyContStyle_L.setBorderLeft(CellStyle.BORDER_THIN);
	      moneyContStyle_L.setBorderRight(CellStyle.BORDER_THIN);
	      moneyContStyle_L.setBorderTop(CellStyle.BORDER_THIN);
	      moneyContStyle_L.setDataFormat(HSSFDataFormat.getBuiltinFormat("#,##0.00"));
	  }
      return moneyContStyle_L;
  }
  /**
   * 货币正文内容格式，保留6位小数（适用于单位为万元）
   * @param wb
   * @return CellStyle
   */
  public CellStyle getMoneyContStyle_w(Workbook wb){
	  if(moneyContStyle==null){
		  moneyContStyle = wb.createCellStyle();
	      Font fLnode = wb.createFont();
	      fLnode.setFontName("Arial");
	      fLnode.setFontHeightInPoints( (short) 9);
	      fLnode.setBoldweight(Font.BOLDWEIGHT_NORMAL);       //不加粗
	      moneyContStyle.setFont(fLnode);
	      moneyContStyle.setAlignment(CellStyle.ALIGN_RIGHT);             //水平靠右
	      moneyContStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
	      moneyContStyle.setBorderBottom(CellStyle.BORDER_THIN);
	      moneyContStyle.setBorderLeft(CellStyle.BORDER_THIN);
	      moneyContStyle.setBorderRight(CellStyle.BORDER_THIN);
	      moneyContStyle.setBorderTop(CellStyle.BORDER_THIN);
	      moneyContStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("#,##0.000000"));
	  }
      return moneyContStyle;
  }
  
  /**
   * 
   * @param wb
   * @return CellStyle
   */
  public CellStyle getIntContStyle(Workbook wb){
	  if(intContStyle==null){
		  intContStyle = wb.createCellStyle();
		  Font fLnode = wb.createFont();
		  fLnode.setFontName("Arial");
		  fLnode.setFontHeightInPoints( (short) 9);
		  fLnode.setBoldweight(Font.BOLDWEIGHT_NORMAL);       //不加粗
		  intContStyle.setFont(fLnode);
		  intContStyle.setAlignment(CellStyle.ALIGN_RIGHT);             //水平靠右
		  intContStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		  intContStyle.setBorderBottom(CellStyle.BORDER_THIN);
		  intContStyle.setBorderLeft(CellStyle.BORDER_THIN);
		  intContStyle.setBorderRight(CellStyle.BORDER_THIN);
		  intContStyle.setBorderTop(CellStyle.BORDER_THIN);
		  intContStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("0"));
	  }
	  return intContStyle;
  }
  public CellStyle getIntContStyle_C(Workbook wb){
	  if(intContStyle_C==null){
		  intContStyle_C = wb.createCellStyle();
		  Font fLnode = wb.createFont();
		  fLnode.setFontName("Arial");
		  fLnode.setFontHeightInPoints( (short) 9);
		  fLnode.setBoldweight(Font.BOLDWEIGHT_NORMAL);       //不加粗
		  intContStyle_C.setFont(fLnode);
		  intContStyle_C.setAlignment(CellStyle.ALIGN_CENTER);             //水平靠右
		  intContStyle_C.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		  intContStyle_C.setBorderBottom(CellStyle.BORDER_THIN);
		  intContStyle_C.setBorderLeft(CellStyle.BORDER_THIN);
		  intContStyle_C.setBorderRight(CellStyle.BORDER_THIN);
		  intContStyle_C.setBorderTop(CellStyle.BORDER_THIN);
		  intContStyle_C.setDataFormat(HSSFDataFormat.getBuiltinFormat("0"));
	  }
	  return intContStyle_C;
  }
  public CellStyle getIntContStyle_L(Workbook wb){
	  if(intContStyle_L==null){
		  intContStyle_L = wb.createCellStyle();
		  Font fLnode = wb.createFont();
		  fLnode.setFontName("Arial");
		  fLnode.setFontHeightInPoints( (short) 9);
		  fLnode.setBoldweight(Font.BOLDWEIGHT_NORMAL);       //不加粗
		  intContStyle_L.setFont(fLnode);
		  intContStyle_L.setAlignment(CellStyle.ALIGN_LEFT);             //水平靠右
		  intContStyle_L.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		  intContStyle_L.setBorderBottom(CellStyle.BORDER_THIN);
		  intContStyle_L.setBorderLeft(CellStyle.BORDER_THIN);
		  intContStyle_L.setBorderRight(CellStyle.BORDER_THIN);
		  intContStyle_L.setBorderTop(CellStyle.BORDER_THIN);
		  intContStyle_L.setDataFormat(HSSFDataFormat.getBuiltinFormat("0"));
	  }
	  return intContStyle_L;
  }
  /**
   * 数字小节格式，水平靠右，垂直居中，有表格线，加粗，字体为Arial
   * @param wb Workbook
   * @return CellStyle
   */
  public CellStyle getNumSecStyle(Workbook wb){
	  if(numSecStyle==null){
		  numSecStyle = wb.createCellStyle();
	      Font fPnode = wb.createFont();
	      fPnode.setFontHeightInPoints( (short) 9);
	      fPnode.setFontName("Arial");
	      fPnode.setBoldweight(Font.BOLDWEIGHT_BOLD);        //加粗
	      numSecStyle.setFont(fPnode);
	      numSecStyle.setAlignment(CellStyle.ALIGN_RIGHT);             //水平靠右
	      numSecStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
	      numSecStyle.setBorderBottom(CellStyle.BORDER_THIN);
	      numSecStyle.setBorderLeft(CellStyle.BORDER_THIN);
	      numSecStyle.setBorderRight(CellStyle.BORDER_THIN);
	      numSecStyle.setBorderTop(CellStyle.BORDER_THIN);
	      numSecStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("General"));
	  }
      return numSecStyle;
  }
  /**
   * 货币正文内容格式，水平靠右，垂直居中，有表格线，字体为Arial
   * @param wb
   * @return CellStyle
   */
  public CellStyle getMoneySecStyle(Workbook wb){
	  if(moneySecStyle==null){
		  moneySecStyle= wb.createCellStyle();
		  Font fPnode = wb.createFont();
		  fPnode.setFontHeightInPoints( (short) 9);
		  fPnode.setFontName("Arial");
		  fPnode.setBoldweight(Font.BOLDWEIGHT_BOLD);        //加粗
		  moneySecStyle.setFont(fPnode);
		  moneySecStyle.setAlignment(CellStyle.ALIGN_RIGHT);             //水平靠右
		  moneySecStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		  moneySecStyle.setBorderBottom(CellStyle.BORDER_THIN);
		  moneySecStyle.setBorderLeft(CellStyle.BORDER_THIN);
		  moneySecStyle.setBorderRight(CellStyle.BORDER_THIN);
		  moneySecStyle.setBorderTop(CellStyle.BORDER_THIN);
		  moneySecStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("#,##0.00"));
	  }
	  return moneySecStyle;
  }
 
/**
 * 
 * @param wb
 * @return CellStyle
 */
public CellStyle getIntSecStyle(Workbook wb){
	  if(intSecStyle==null){
		  intSecStyle = wb.createCellStyle();
	      Font fPnode = wb.createFont();
	      fPnode.setFontHeightInPoints( (short) 9);
	      fPnode.setFontName("Arial");
	      fPnode.setBoldweight(Font.BOLDWEIGHT_BOLD);        //加粗
	      intSecStyle.setFont(fPnode);
	      intSecStyle.setAlignment(CellStyle.ALIGN_RIGHT);             //水平靠右
	      intSecStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
	      intSecStyle.setBorderBottom(CellStyle.BORDER_THIN);
	      intSecStyle.setBorderLeft(CellStyle.BORDER_THIN);
	      intSecStyle.setBorderRight(CellStyle.BORDER_THIN);
	      intSecStyle.setBorderTop(CellStyle.BORDER_THIN);
	      intSecStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("0"));
	  }
    return intSecStyle;
}
  /**
   * 字符内容格式，水平靠左，垂直居中，有表格线
   * @param wb Workbook
   * @return CellStyle
   */
public CellStyle getStrContStyle(Workbook wb){
	if(strContStyle==null){
		strContStyle = wb.createCellStyle();
	    Font fLnode = wb.createFont();
	    fLnode.setFontName("Arial");
	    fLnode.setFontHeightInPoints( (short)9);
	    fLnode.setBoldweight(Font.BOLDWEIGHT_NORMAL);      //不加粗
	    strContStyle.setFont(fLnode);
	    strContStyle.setAlignment(CellStyle.ALIGN_LEFT);             //水平靠左
	    strContStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
	    strContStyle.setBorderBottom(CellStyle.BORDER_THIN);
	    strContStyle.setBorderLeft(CellStyle.BORDER_THIN);
	    strContStyle.setBorderRight(CellStyle.BORDER_THIN);
	    strContStyle.setBorderTop(CellStyle.BORDER_THIN);
	    strContStyle.setWrapText(true);
	}
    return strContStyle;
}
public CellStyle getStrContStyle_C(Workbook wb){
	if(strContStyle_C==null){
		strContStyle_C = wb.createCellStyle();
	    Font fLnode = wb.createFont();
	    fLnode.setFontName("Arial");
	    fLnode.setFontHeightInPoints( (short)9);
	    fLnode.setBoldweight(Font.BOLDWEIGHT_NORMAL);      //不加粗
	    strContStyle_C.setFont(fLnode);
	    strContStyle_C.setAlignment(CellStyle.ALIGN_CENTER);             //水平靠左
	    strContStyle_C.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
	    strContStyle_C.setBorderBottom(CellStyle.BORDER_THIN);
	    strContStyle_C.setBorderLeft(CellStyle.BORDER_THIN);
	    strContStyle_C.setBorderRight(CellStyle.BORDER_THIN);
	    strContStyle_C.setBorderTop(CellStyle.BORDER_THIN);
	    strContStyle_C.setWrapText(true);
	}
    return strContStyle_C;
}
public CellStyle getStrContStyle_R(Workbook wb){
	if(strContStyle_R==null){
		strContStyle_R = wb.createCellStyle();
	    Font fLnode = wb.createFont();
	    fLnode.setFontName("Arial");
	    fLnode.setFontHeightInPoints( (short)9);
	    fLnode.setBoldweight(Font.BOLDWEIGHT_NORMAL);      //不加粗
	    strContStyle_R.setFont(fLnode);
	    strContStyle_R.setAlignment(CellStyle.ALIGN_RIGHT);             //水平靠左
	    strContStyle_R.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
	    strContStyle_R.setBorderBottom(CellStyle.BORDER_THIN);
	    strContStyle_R.setBorderLeft(CellStyle.BORDER_THIN);
	    strContStyle_R.setBorderRight(CellStyle.BORDER_THIN);
	    strContStyle_R.setBorderTop(CellStyle.BORDER_THIN);
	    strContStyle_R.setWrapText(true);
	}
    return strContStyle_R;
}

  /**
   * 字符小节格式，水平靠左，垂直居中，有表格线，加粗
   * @param wb Workbook
   * @return CellStyle
   */

  public CellStyle getStrSecStyle(Workbook wb){
	  if(strSecStyle==null){
		  strSecStyle = wb.createCellStyle();
		  Font fPnode = wb.createFont();
		  fPnode.setFontHeightInPoints( (short) 9);
		  fPnode.setBoldweight(Font.BOLDWEIGHT_BOLD);      //加粗
		  strSecStyle.setFont(fPnode);
		  strSecStyle.setAlignment(CellStyle.ALIGN_LEFT);           //水平靠左
		  strSecStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		  strSecStyle.setBorderBottom(CellStyle.BORDER_THIN);
		  strSecStyle.setBorderLeft(CellStyle.BORDER_THIN);
		  strSecStyle.setBorderRight(CellStyle.BORDER_THIN);
		  strSecStyle.setBorderTop(CellStyle.BORDER_THIN);
	  }
    return strSecStyle;
  }

  /**
 * 副表头格式，水平靠左，垂直居中，无表格线，一般用于写制表单位等
 * @param wb Workbook
 * @return CellStyle
 */
  public CellStyle getSubHeadStyle(Workbook wb){
	  if(subHeadStyle==null){
		  subHeadStyle = wb.createCellStyle();
          Font fPnode = wb.createFont();
          fPnode.setFontHeightInPoints( (short) 9);
          fPnode.setBoldweight(Font.BOLDWEIGHT_BOLD);      //加粗
          subHeadStyle.setFont(fPnode);
          subHeadStyle.setAlignment(CellStyle.ALIGN_LEFT);           //水平靠左
          subHeadStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
	  }
      return subHeadStyle;
  }
/**
 * 长字符串内容格式，水平靠左，垂直居中，有表格线，自动换行
 * @param wb Workbook
 * @return CellStyle
 */
  public CellStyle getLongStrStyle(Workbook wb){
	  if(longStrStyle==null){
		  longStrStyle = wb.createCellStyle();
          Font fLnode = wb.createFont();
          fLnode.setFontHeightInPoints( (short) 9);
          fLnode.setBoldweight(Font.BOLDWEIGHT_NORMAL);      //不加粗
          longStrStyle.setFont(fLnode);
          longStrStyle.setAlignment(CellStyle.ALIGN_LEFT);             //水平靠左
          longStrStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
          longStrStyle.setWrapText(true);
          longStrStyle.setBorderBottom(CellStyle.BORDER_THIN);
          longStrStyle.setBorderLeft(CellStyle.BORDER_THIN);
          longStrStyle.setBorderRight(CellStyle.BORDER_THIN);
          longStrStyle.setBorderTop(CellStyle.BORDER_THIN);
	  }
      return longStrStyle;
  }

}

