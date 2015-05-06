package com.fruit.query.util;
/**
 * 
 * @author wxh
 *2009-4-2
 *TODO 报表解析成excel时发生的异常
 */
public class ParseReportToExcelException extends Exception{
	public ParseReportToExcelException(){}
	/**
	 * 报表解析为excel时发生的异常
	 * @param msg 错误信息
	 */	 
	public ParseReportToExcelException(String msg)
		  {
		    super(msg);
		  }
}
