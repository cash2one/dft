package com.fruit.query.report;
import java.util.*;
/**
 * 
 * @author wxh
 *2009-3-11
 *TODO 分组信息定义
 */
public class Grouping {
	private int enabled;   
	private String groupBy;  //按哪个字段分组
	private String label;    //分组行显示的标识性文字，如“小计”
	private String labelColIndex; //分组label显示在哪列
	private int groupPosition;    //分组头显示位置。0：明细之后，1：明细之前
	private List feidsCalculated; //要进行分组小计的字段
	/**
	 * 获取需要计算分组小计的字段集合。
	 * 分组行中，这些字段（列）位置会出现相应的小计值。
	 * @return 小计字段集合
	 */
	public List getFeidsCalculated() {
		return feidsCalculated;
	}
	/**
	 * 
	 * @param feidsCalculated
	 */
	public void setFeidsCalculated(List feidsCalculated) {
		this.feidsCalculated = feidsCalculated;
	}
	/**
	 * 是否启用分组。
	 * 0：禁用；1：启用。
	 * @return 是否启用分组
	 */
	public int getEnabled() {
		return enabled;
	}
	/**
	 * 
	 * @param enabled
	 */
	public void setEnabled(int enabled) {
		this.enabled = enabled;
	}
	/**
	 * 获取分组字段。
	 * 系统目前只支持按单个字段分组。
	 * 原始记录集应该已经按该字段排序――相同字段值的记录排在一起。
	 * @return 分组字段(某个Column的DataIndex)。
	 */
	public String getGroupBy() {
		return groupBy;
	}
	/**
	 * 
	 * @param groupBy
	 */
	public void setGroupBy(String groupBy) {
		this.groupBy = groupBy;
	}
	/**
	 * 获取分组行出现的位置。
	 * 0：默认，分组行在相应明细内容之后；1：分组行在明细内容前。
	 * @return 分组行位置（0/1）。
	 */
	public int getGroupPosition() {
		return groupPosition;
	}
	/**
	 * 
	 * @param groupPosition
	 */
	public void setGroupPosition(int groupPosition) {
		this.groupPosition = groupPosition;
	}
	/**
	 * 获取分组行的标识内容。
	 * @return 一般是"小计"。
	 */
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	/**
	 * 分组行标识内容出现在哪个列。
	 * @return “小计”字符串出现在哪个字段中。
	 */
	public String getLabelColIndex() {
		return labelColIndex;
	}
	/**
	 * 
	 * @param labelColIndex
	 */
	public void setLabelColIndex(String labelColIndex) {
		this.labelColIndex = labelColIndex;
	}
}
