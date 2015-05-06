package com.ifugle.dft.system.entity;

import java.util.List;

public class User {
	private String userid;
	private String name;
	private String userDesc;
	private String password;
	private int isManager;
	private String fjmc;
	private int qybj;
	private List xzs;     //用户可管辖的乡镇
	public int getIsManager() {
		return isManager;
	}
	public void setIsManager(int isManager) {
		this.isManager = isManager;
	}
	public String getFjmc() {
		return fjmc;
	}
	public void setFjmc(String fjmc) {
		this.fjmc = fjmc;
	}
	public int getQybj() {
		return qybj;
	}
	public void setQybj(int qybj) {
		this.qybj = qybj;
	}
	public String getUserDesc() {
		return userDesc;
	}
	public void setUserDesc(String userDesc) {
		this.userDesc = userDesc;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getUserid() {
		return userid;
	}
	public void setUserid(String userid) {
		this.userid = userid;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List getXzs() {
		return xzs;
	}
	public void setXzs(List xzs) {
		this.xzs = xzs;
	}
}
