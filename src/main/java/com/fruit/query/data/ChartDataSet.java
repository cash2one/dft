package com.fruit.query.data;

import java.util.Map;

public class ChartDataSet{
	private String seriesName;
	//以category索引的数据值
	private Map<String,String> data;
	public String getSeriesName() {
		return seriesName;
	}
	public void setSeriesName(String seriesName) {
		this.seriesName = seriesName;
	}
	public Map<String,String> getData() {
		return data;
	}
	public void setData(Map<String,String> data) {
		this.data = data;
	}
}
