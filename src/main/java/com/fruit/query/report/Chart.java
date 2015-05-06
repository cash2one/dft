package com.fruit.query.report;

import com.fruit.query.data.ProcedureBean;

public class Chart {
	private String id;
	//取数来源。0：静态文本；1：sql取数；2：存储过程取数
	private int sourceType;
	//取数用sql，可以含{}方式的参数
	private String sql;
	//取数用存储过程时，该存储过程的描述
	private ProcedureBean procedure;
	private String chartType;
	//图表报表使用的swf或js文件
	private String chartFile;
	//图表报表所关联的数据模板。
	private String dataTemplateName;
	private int width;
	private int height;
	//数据文件的格式。默认0：xml格式，1：json
	private int dataFormat;
	//是否多系列数据图表，即统计多组对象。
	private int isMultiSeries;
	//多指标图表报表的category数据取自哪个字段
	private String categoryIndex;
	//图表报表的值取那个字段的数据
	private String dataIndex;
	//多指标图表报表的series数据取自哪个字段
	private String seriesIndex;
	public int getDataFormat() {
		return dataFormat;
	}
	public void setDataFormat(int dataFormat) {
		this.dataFormat = dataFormat;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public int getWidth() {
		return width;
	}
	public void setWidth(int width) {
		this.width = width;
	}
	public int getHeight() {
		return height;
	}
	public void setHeight(int height) {
		this.height = height;
	}
	public int getSourceType() {
		return sourceType;
	}
	public void setSourceType(int sourceType) {
		this.sourceType = sourceType;
	}
	public String getSql() {
		return sql;
	}
	public void setSql(String sql) {
		this.sql = sql;
	}
	public ProcedureBean getProcedure() {
		return procedure;
	}
	public void setProcedure(ProcedureBean procedure) {
		this.procedure = procedure;
	}
	public String getChartType() {
		return chartType;
	}
	public void setChartType(String chartType) {
		this.chartType = chartType;
	}
	public String getChartFile() {
		return chartFile;
	}
	public void setChartFile(String chartFile) {
		this.chartFile = chartFile;
	}
	public String getDataTemplateName() {
		return dataTemplateName;
	}
	public void setDataTemplateName(String dataTemplateName) {
		this.dataTemplateName = dataTemplateName;
	}

	public String getCategoryIndex() {
		return categoryIndex;
	}
	public void setCategoryIndex(String categoryIndex) {
		this.categoryIndex = categoryIndex;
	}
	public String getDataIndex() {
		return dataIndex;
	}
	public void setDataIndex(String dataIndex) {
		this.dataIndex = dataIndex;
	}
	public String getSeriesIndex() {
		return seriesIndex;
	}
	public void setSeriesIndex(String seriesIndex) {
		this.seriesIndex = seriesIndex;
	}
	public int getIsMultiSeries() {
		return isMultiSeries;
	}
	public void setIsMultiSeries(int isMultiSeries) {
		this.isMultiSeries = isMultiSeries;
	}
}
