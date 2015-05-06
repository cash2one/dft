package com.fruit.query.data;
/**
 * 
 * @author wxh
 *2009-3-11
 *TODO 报表结果记录集的元数据信息
 */
public class MetaData {
	private int columnCount;
	private String[] columnNames;
	/**
	 * 记录集的列数。
	 * 这里的列数不一定等于报表定义的column数。
	 * @return 记录集的列数
	 */
	public int getColumnCount() {
		return columnCount;
	}
	/**
	 * 
	 * @param columnCount 记录集列数
	 */
	public void setColumnCount(int columnCount) {
		this.columnCount = columnCount;
	}
	/**
	 * 获取列名集合。
	 * @return 列名组成的数组
	 */
	public String[] getColumnNames() {
		return columnNames;
	}
	/**
	 * 
	 * @param columnNames 列名组成的数组
	 */
	public void setColumnNames(String[] columnNames) {
		this.columnNames = columnNames;
	}
	/**
	 * 根据索引号返回相应位置的字段名。
	 * 列索引从1开始。字段名集合为空，或索引无效，返回null。
	 * @param index 要查找的列索引号
	 * @return 指定索引位置的列名。
	 */
	public String getColumnNames(int index){
		if(columnNames!=null&&index<=columnNames.length&&index>0){
			return columnNames[index-1];
		}else{
			return null;
		}
	}
}
