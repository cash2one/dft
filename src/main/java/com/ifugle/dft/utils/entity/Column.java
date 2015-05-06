package com.ifugle.dft.utils.entity;

public class Column {
	private String header;
	private String dataIndex;
	private int width;
	private String align;
	private boolean hidden;
	private int dataType;
	private int colspan;
	private int isGroup;
	private int isMultiUnit;
	private String renderer ;
	public int getIsGroup() {
		return isGroup;
	}
	public int getIsMultiUnit() {
		return isMultiUnit;
	}
	public String getRenderer() {
		return renderer;
	}
	public void setRenderer(String renderer) {
		this.renderer = renderer;
	}
	public void setIsMultiUnit(int isMultiUnit) {
		this.isMultiUnit = isMultiUnit;
	}
	public void setIsGroup(int isGroup) {
		this.isGroup = isGroup;
	}
	public int getDataType() {
		return dataType;
	}
	public int getColspan() {
		return colspan;
	}
	public void setColspan(int colspan) {
		this.colspan = colspan;
	}
	public void setDataType(int dataType) {
		this.dataType = dataType;
	}
	public boolean isHidden() {
		return hidden;
	}
	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}
	public String getHeader() {
		return header;
	}
	public void setHeader(String header) {
		this.header = header;
	}
	public String getDataIndex() {
		return dataIndex;
	}
	public void setDataIndex(String dataIndex) {
		this.dataIndex = dataIndex;
	}
	public int getWidth() {
		return width;
	}
	public void setWidth(int width) {
		this.width = width;
	}
	public String getAlign() {
		return align;
	}
	public void setAlign(String align) {
		this.align = align;
	}
}
