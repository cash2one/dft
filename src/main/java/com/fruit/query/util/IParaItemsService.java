package com.fruit.query.util;

import java.util.List;
import java.util.Map;

import com.fruit.query.report.Report;
/**
 * 
 * @author wxh
 *2009-4-17
 *TODO 获取参数待选项集合数据的自定义类要实现的接口。
 */
public interface IParaItemsService {
	/**
	 * 获取参数待选项集合。
	 * @param rpt 报表模板，包含报表定义信息。
	 * @param paraVals 本次请求传递的参数，参数名索引ParaValue形成的map。
	 * @return 参数待选项集合。
	 * @throws DataServiceException
	 */
	public List getParaOptions(Report rpt,Map paraVals)throws DataServiceException;
}
