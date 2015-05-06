package com.fruit.query.report;

public class Head {
	private int height=50;
	private int titleInHead = 1;
	private SubTitle subTitle;
	private String style = "head";
	public String getStyle() {
		return style;
	}
	public void setStyle(String style) {
		this.style = style;
	}
	public int getTitleInHead() {
		return titleInHead;
	}
	public SubTitle getSubTitle() {
		return subTitle;
	}
	public void setSubTitle(SubTitle subTitle) {
		this.subTitle = subTitle;
	}
	public void setTitleInHead(int titleInHead) {
		this.titleInHead = titleInHead;
	}
	public int getHeight() {
		return height;
	}
	public void setHeight(int height) {
		this.height = height;
	}
}
