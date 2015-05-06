package com.fruit.query.util;
/**
 * 
 * @author wxh
 *2009-3-19
 *TODO 报表数据获取时抛出的异常
 */
public class RptServiceException extends Exception{
	public RptServiceException(){}
	  /**
	   * 
	   * @param msg
	   */
	  public RptServiceException(String msg)
	  {
	    super(msg);
	  }
}
