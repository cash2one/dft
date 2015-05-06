package com.fruit.query.report;
import com.fruit.query.data.*;
import java.util.*;
/**
 * 
 * @author wxh
 *2009-3-11
 *TODO 列的定义信息
 */
public class ColumnDefine {
	private int sourceType;  //列头数据取数方式。
	private int isComplex;   //是否复杂表头
	private String sql;
	private ProcedureBean procedure;
	//列头的构造方式。0：只构造一次，查询时共享表头。1：每次查询都重新构造。
	private int columnBuild;
	//列头节点集合，此处的列头，通常是静态的列头集合。
	private List columns;
	private int totalCol;  //是否自动总计，0：否，1：是。如果是，则会将所有非字段串类型的叶子列值相加
	private int totalPos;  
	private int totalColWidth=120;//自动总计列的宽度
	private String totalColRenderer;//自动总计列的渲染函数
	private String implClass;//列头取数由自定义类实现
	
	private int rowNumber;  
	public int getRowNumber() {
		return rowNumber;
	}
	public void setRowNumber(int rowNumber) {
		this.rowNumber = rowNumber;
	}
	/**
	 * 总计列数据渲染的格式。
	 * （一般有整数、小数、货币等分别）。是一js函数名。
	 * @return 渲染格式
	 * @see com.datanew.query.data.Column#getRenderer()
	 */
	public String getTotalColRenderer() {
		return totalColRenderer;
	}
	/**
	 * 
	 * @param totalColRenderer
	 * @see #getTotalColRenderer()
	 */
	public void setTotalColRenderer(String totalColRenderer) {
		this.totalColRenderer = totalColRenderer;
	}
	/**
	 * 总计列列宽，单位：像素
	 * @return 列宽值。
	 */
	public int getTotalColWidth() {
		return totalColWidth;
	}
	/**
	 * 
	 * @param totalColWidth
	 * @see #getTotalColWidth()
	 */
	public void setTotalColWidth(int totalColWidth) {
		this.totalColWidth = totalColWidth;
	}
	/**
	 * 获取是否自动总计的定义。
	 * 0：否，1：是。<br>
	 * 如果是，将自动添加总计列，值为所有非字符串类型的底级列的值相加。
	 * @return 是否总计。
	 */
	public int getTotalCol() {
		return totalCol;
	}
	/**
	 * 
	 * @param totalCol
	 * 
	 */
	public void setTotalCol(int totalCol) {
		this.totalCol = totalCol;
	}
	/**
	 * 总计列出现的位置。<br>
	 * 999表示总计列出现在最后，其他值则表示总计列在最终显示结果中的位置（非列节点树中的位置）。<br>
	 * 比如3，如果最终显示结果是6列，表示第3列是总计。计算总计列位置时要考虑到自动小计时插入的小计列数。
	 * @return 总计列在显示结果中的列序号。
	 */
	public int getTotalPos() {
		return totalPos;
	}
	/**
	 * 
	 * @param totalPos
	 */
	public void setTotalPos(int totalPos) {
		this.totalPos = totalPos;
	}
	/**
	 * 返回静态列节点集合。
	 * 如果列节点是设计文件中定义的静态节点，该方法将直接返回这些列集合。
	 * 如果是动态表头，则每次请求报表时重新构建，并且节点信息不存于ColumnDefine中的columns属性。<br>
	 * 因为ComumnDefine类是Report对象的一部分，记录的是报表模板信息，是单例的，静态的。
	 * @return 列节点集合。
	 */
	public List getColumns() {
		return columns;
	}
	/**
	 * 
	 * @param columns
	 */
	public void setColumns(List columns) {
		this.columns = columns;
	}
	/**
	 * 是否复杂表头。
	 * 0：否，1：是。
	 * @return 是否复杂表头。
	 */
	public int getIsComplex() {
		return isComplex;
	}
	/**
	 * 
	 * @param isComplex
	 */
	public void setIsComplex(int isComplex) {
		this.isComplex = isComplex;
	}
	/**
	 * 存储过程定义
	 * 列头如果是动态的、从存储过程取数，这里获取存储过程的名称
	 * @return ProcedureBean对象。
	 */
	public ProcedureBean getProcedure() {
		return procedure;
	}
	/**
	 * 
	 * @param procedure
	 */
	public void setProcedure(ProcedureBean procedure) {
		this.procedure = procedure;
	}
	/**
	 * 列集合的取数方式。
	 * 0：静态，在设计文件中定义；1：sql语句取数；2：存储过程取数；3：自定义类取数。类实现特定接口
	 * @see com.datanew.query.util.IColumnsService
	 * @return 取数方式。
	 */
	public int getSourceType() {
		return sourceType;
	}
	/**
	 * 
	 * @param sourceType
	 */
	public void setSourceType(int sourceType) {
		this.sourceType = sourceType;
	}
	/**
	 * 列节点集合由sql取数时的sql语句
	 * @return sql语句
	 */
	public String getSql() {
		return sql;
	}
	/**
	 * 
	 * @param sql
	 */
	public void setSql(String sql) {
		this.sql = sql;
	}
	/**
	 * 列信息取数的时机。
	 * 原本为了提高效率、更有效的利用缓存而设置，目前暂时无用。
	 * @return 列取数的时机
	 */
	public int getColumnBuild() {
		return columnBuild;
	}
	public void setColumnBuild(int columnBuild) {
		this.columnBuild = columnBuild;
	}
	/**
	 * 取数方式为自定义类实现时，具体取数类的全路径。
	 * @return 类全路径。
	 */
	public String getImplClass() {
		return implClass;
	}
	/**
	 * 
	 * @param implClass
	 */
	public void setImplClass(String implClass) {
		this.implClass = implClass;
	}
}
