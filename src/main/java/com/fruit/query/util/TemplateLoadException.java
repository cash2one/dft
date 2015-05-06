package com.fruit.query.util;
/**
 * 
 * @author wxh
 *2009-3-19
 *TODO 报表设计信息加载时抛出的异常
 */
public class TemplateLoadException  extends Exception{
	public TemplateLoadException(){}
	  /**
	   * 加载报表模板时发生的异常
	   * @param msg 异常信息
	   */
	  public TemplateLoadException(String msg)
	  {
	    super(msg);
	  }
}
