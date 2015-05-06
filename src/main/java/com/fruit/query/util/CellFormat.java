package com.fruit.query.util;
import java.text.DecimalFormat;
public class CellFormat {
	/**
	 * 格式渲染函数，渲染为货币类型
	 * @param val 数据
	 * @return 渲染后的字符串。
	 */
	public static String regMoney(double val){
		String fval="";
		DecimalFormat dFormat = new DecimalFormat("##,###,###,###,###,##0.00");
		fval=dFormat.format(val);
		return fval;
	}
	/**
	 * 格式渲染为保留两位小数。
	 * @param val 数据
	 * @return 渲染后的字符串。
	 */
	public static String render2Decimal(double val){
		String fval="";
		DecimalFormat dFormat = new DecimalFormat("###0.00");
		fval=dFormat.format(val);
		return fval;
	}
	/**
	 * 格式渲染为整数
	 * @param val 数据
	 * @return 渲染后的字符串。
	 */
	public static String renderInt(double val){
		int ival=(int)val;
		return String.valueOf(ival);
	}
}
