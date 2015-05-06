package com.fruit.query.data;
/**
 * 
 * @author wxh
 *2009-3-16
 *TODO 装载参数的值
 */
public class ParaValue {
	//参数值
	private String value;
	//参数值的描述，一般的，desc用于有中文显示内容与其值匹配的编码参数
	private String desc;
	
	private String clsName;  //参数值为复杂类型是，该类型
	public String getClsName() {
		return clsName;
	}
	public void setClsName(String clsName) {
		this.clsName = clsName;
	}
	public Object getComposedValue() {
		return composedValue;
	}
	public void setComposedValue(Object composedValue) {
		this.composedValue = composedValue;
	}
	private Object composedValue;  //参数值比较复杂，一般为某个对象
	/**
	 * 返回参数的中文名、描述性的内容。
	 * 对于文本框类型的参数，其值同value。
	 * @return 参数中文名。
	 */
	public String getDesc() {
		return desc;
	}
	/**
	 * 
	 * @param desc 中文名。
	 */
	public void setDesc(String desc) {
		this.desc = desc;
	}
	/**
	 * 返回参数的值。
	 * 对于预先提供“选项”的参数，value就是某个OptionItem的bm。
	 * @return 参数值
	 */
	public String getValue() {
		if("null".equalsIgnoreCase(value)){
			return "";
		}
		return value;
	}
	/**
	 * 
	 * @param value
	 */
	public void setValue(String value) {
		this.value = value;
	}
	/**
	 * 
	 * 默认构造函数
	 */
	public ParaValue(){
		
	}
	/**
	 * 
	 * @param value 参数值
	 * @param desc  参数值对应的中文名、描述信息
	 */
	public ParaValue(String value,String desc){
		this.value=value;
		this.desc=desc;
	}
}
