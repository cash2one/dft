package com.fruit.query.util;

import java.util.Map;

import com.fruit.query.data.ParaProcess;
import com.fruit.query.report.Report;
/**
 * 
 * @author wxh
 *2009-5-14
 *TODO 参数加工的扩展类要实现的接口。
 */
public interface IParaProcess {
	/**
	 * 加工参数，返回参数解析后的内容
	 * @param rpt 所在的报表模板
	 * @param pro 要执行的加工过程
	 * @param paraVals 本次报表请求所组织的参数值集合
	 * @return 加工后的内容
	 * @throws DataServiceException
	 */
	public String processPara(Report rpt,ParaProcess pro,Map paraVals)throws DataServiceException;
}
