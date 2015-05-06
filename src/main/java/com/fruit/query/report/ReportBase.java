package com.fruit.query.report;
public class ReportBase implements Comparable{
	private String id;
	private String name;
	//2013-08-15 报表说明，描述
	private String description;
	//2013-08-15 报表数据（一般是金额）列是否能用多种单位查看
	private int multiUnit;
	//2013-08-15 报表能切换多种金额单位时，默认初始单位
	private String defaultUnit;
	private String supportUnits;
	private int zeroCanHide;
	//2014-06-03 是否有图表
	private int hasChart;
	private int directExport;  //2014-11-13 是否用于直接导出结果，0：（默认）否。1：是。无web页面的展示，直接导出Excel文件。
	private String supportUnitsName;
	public String getSupportUnitsName() {
		return supportUnitsName;
	}
	public void setSupportUnitsName(String supportUnitsName) {
		this.supportUnitsName = supportUnitsName;
	}
	public int getDirectExport() {
		return directExport;
	}
	public void setDirectExport(int directExport) {
		this.directExport = directExport;
	}
	public int getHasChart() {
		return hasChart;
	}
	public void setHasChart(int hasChart) {
		this.hasChart = hasChart;
	}
	public int getZeroCanHide() {
		return zeroCanHide;
	}
	public void setZeroCanHide(int zeroCanHide) {
		this.zeroCanHide = zeroCanHide;
	}
	public String getSupportUnits() {
		return supportUnits;
	}
	public void setSupportUnits(String supportUnits) {
		this.supportUnits = supportUnits;
	}
	public int getMultiUnit() {
		return multiUnit;
	}
	public void setMultiUnit(int multiUnit) {
		this.multiUnit = multiUnit;
	}
	public String getDefaultUnit() {
		return defaultUnit;
	}
	public void setDefaultUnit(String defaultUnit) {
		this.defaultUnit = defaultUnit;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	/**
	 * 获取报表的唯一标识。
	 * @return 报表ID
	 */
	public String getId() {
		return id;
	}
	/**
	 * 
	 * @param id
	 */
	public void setId(String id) {
		this.id = id;
	}
	/**
	 * 获取报表的描述性名称。
	 * @return 报表名。
	 */
	public String getName() {
		return name;
	}
	/**
	 * 
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public int compareTo(Object o) {
		return this.getId().compareTo(((ReportBase)o).getId());
	}
}