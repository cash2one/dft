package com.fruit.query.data;
import java.util.*;
/**
 * 
 * @author wxh
 *2009-3-11
 *TODO 报表查询结果数据集
 */
public class DataSet {
	private MetaData metaData;
	private List rows;
	
	private int totalCount ; //2010-07-01 总记录数
	public int getTotalCount() {
		return totalCount;
	}
	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}
	/**
	 * 报表记录集的元信息
	 * @return MetaData
	 * @see MetaData
	 */
	public MetaData getMetaData() {
		return metaData;
	}
	/**
	 * 
	 * @param metaData MetaData对象
	 * 
	 */
	public void setMetaData(MetaData metaData) {
		this.metaData = metaData;
	}
	/**
	 * 报表记录集中的行集合。记录集的主要内容。
	 * @return 行数据集合。
	 * @see Row
	 */
	public List getRows() {
		return rows;
	}
	/**
	 * 
	 * @param rows 行集合
	 */
	public void setRows(List rows) {
		this.rows = rows;
	}
}
