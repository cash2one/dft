package com.fruit.query.util;
/**
 * 
 * @author wxh
 *2009-3-19
 *TODO 报表数据解析成特定输出格式时抛出的异常
 */
public class ParseReportException extends Exception{
	public ParseReportException(){}
	  /**
	   * 报表解析过程中的异常。
	   * @param msg 错误信息。
	   */
	  public ParseReportException(String msg)
	  {
	    super(msg);
	  }
}
