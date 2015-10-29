package com.fruit.query.data;

/**
 * 2015-10-29 描述链接报表如果以弹出窗体的方式打开，允许链接多张报表，分tab显示，这个类用于描述每个链接Tab
 */
public class LinkTab {
	private String title;
	private String linkTo;
	private String linkParams;
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getLinkTo() {
		return linkTo;
	}
	public void setLinkTo(String linkTo) {
		this.linkTo = linkTo;
	}
	public String getLinkParams() {
		return linkParams;
	}
	public void setLinkParams(String linkParams) {
		this.linkParams = linkParams;
	}
}
