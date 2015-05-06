package com.fruit.query.util;

import java.util.*;
import com.fruit.query.report.Report;
/**
 * 
 * @author wxh
 *2009-4-17
 *TODO 列节点集合取数要实现的接口
 */
public interface IColumnsService {
	/**
	 * 列取数要实现的接口。返回列节点集合。
	 * @param rpt 报表模板，包含报表定义信息。
	 * @param paraVals 本次请求传递的参数，参数名索引ParaValue形成的map。
	 * @return 列节点集合
	 * @throws DataServiceException
	 */
	public List getColumns(Report rpt,Map paraVals)throws DataServiceException;
}
