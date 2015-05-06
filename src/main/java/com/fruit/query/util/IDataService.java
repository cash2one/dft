package com.fruit.query.util;

import java.util.*;

import com.fruit.query.report.Report;
import com.fruit.query.data.*;
/**
 * 
 * @author wxh
 *2009-4-17
 * TODO 报表取数实现的接口
 */
public interface IDataService {
	/**
	 * 报表取数要实现的接口。
	 * @param rpt 报表模板，包含报表定义信息。
	 * @param paraVals 本次请求传递的参数，参数名索引ParaValue形成的map。
	 * @return 报表记录集。
	 * @throws DataServiceException
	 */
	public DataSet getReportData(Report rpt,Map paraVals)throws DataServiceException;
	/**
	 * 报表取数要实现的接口。用于分页报表。
	 * @param rpt 报表模板，包含报表定义信息。
	 * @param paraVals 本次请求传递的参数，参数名索引ParaValue形成的map。
	 * @param start 取数范围，起始记录号；
	 * @param limit 本次要取的记录数。
	 * @return 指定范围的报表记录集。
	 * @throws DataServiceException
	 */
	public DataSet getReportDataPaging(Report rpt,Map paraVals,int start,int limit)throws DataServiceException;
	/**
	 * 报表取数要实现的接口。用于分页报表，获取记录数。
	 * @param rpt 报表模板，包含报表定义信息。
	 * @param paraValues 本次请求传递的参数，参数名索引ParaValue形成的map。
	 * @return 记录总数
	 * @throws DataServiceException
	 */
	public int getTotalCount(Report rpt,Map paraValues)throws DataServiceException;
}
