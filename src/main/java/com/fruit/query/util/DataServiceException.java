package com.fruit.query.util;

public class DataServiceException extends Exception{

	public DataServiceException(){}
	/**
	 * 取数时发生错误抛出的异常。
	 * @param msg 错误信息。
	 */
	  public DataServiceException(String msg)
	  {
	    super(msg);
	  }

}
