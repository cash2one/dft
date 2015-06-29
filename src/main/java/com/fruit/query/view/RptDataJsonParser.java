package com.fruit.query.view;
import org.apache.commons.lang.StringUtils;
import org.json.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

import com.fruit.query.util.DBConnectionFactory;
import com.fruit.query.util.ParseReportException;
import com.fruit.query.data.*;
import com.fruit.query.report.*;
/**
 * 
 * @author wxh
 *2009-3-17
 *TODO 将报表数据解析为json格式输出
 */
public class RptDataJsonParser {
	private static RptDataJsonParser dataParser;
	private RptDataJsonParser(){
		
	}
	/**
	 * 获取报表数据向JSON格式转化的解析器实例。
	 * singleton，每次调用，获取的是同一个实例。
	 * @return RptDataJsonParser实例。
	 */
	public static RptDataJsonParser getRptDataParser(){
		if(dataParser==null)
			dataParser=new RptDataJsonParser();
		return dataParser;
	}
	/**
	 * 报表数据解析成json输出。
	 * 按行的有序集合。
	 * @param rpt 报表模板，包含报表定义信息。
	 * @param dts 本次请求获取到的报表数据记录集。
	 * @return 由报表数据转化的JSONArray。
	 * @throws Exception
	 */
	public JSONArray parseReportData(Report rpt,DataSet dts,List gflds,String[] tflds)throws Exception{
		if(dts==null||dts.getRows()==null)return null;
		JSONArray jrows=new JSONArray();
		String[] allCols=dts.getMetaData().getColumnNames();
		try{
			List dtRows=dts.getRows();
			//是否报表解析引擎负责分组
			Grouping grp=null;
			String groupBy="";
			String preGroup="";
			int preGIndex=0;
			int groupCount=0;
			//先都按浮点数计算，到输出时，再转化
			double[] gcounts=null;
			if(rpt!=null&&rpt.getGroupDef()!=null&&rpt.getGroupDef().getEnabled()>0&&gflds!=null){
				grp=rpt.getGroupDef();
				gcounts=new double[gflds.size()];
				groupBy=grp.getGroupBy();
			}

			//是否有总计行,先都按浮点数计算，到输出时，再转化
			double[] tcounts=null;
			if(rpt.getTotalRow()>0&&tflds!=null){
				tcounts=new double[tflds.length];
			}
			
			//总计行位置在前，则先增加一行，合计值等循环结束后再修改。
			if(rpt.getTotalPosition()>0){
				JSONObject tRow=new JSONObject();
				for(int i=0;i<allCols.length;i++){
					if(allCols[i].equalsIgnoreCase(rpt.getTotalLabelIndex())){
						tRow.put(allCols[i],"总计" ); 
					}else{
						tRow.put(allCols[i], ""); 
					} 
				}
				jrows.put(tRow);
			}
			
			for(int i=0;i<dtRows.size();i++){
				Row row=(Row)dtRows.get(i);
				Map cells=row.getCells();
				if(cells==null){
					continue;
				}
				JSONObject jrow=new JSONObject();
				//如果有分组合计计算
				if(gcounts!=null){
					String gVal=(String)cells.get(groupBy);
					/* 因为假设记录集已经按分组字段排序，这里认为分组字段值的切换表示分组的更换
					 * 分组切换时，存放分组小计数据的double数组重新清空。输出一行。输出时要转换类型
					*/
					if(!preGroup.equals(gVal)){
						//如果分组行在明细前，则在分组切换时：插入下一分组的分组行，并将小计数组的信息写入上一分组行。
						if(grp.getGroupPosition()==1){
							//插入一分组行
							JSONObject gRow=new JSONObject();
							for(int k=0;k<allCols.length;k++){
								if(allCols[k].equalsIgnoreCase(grp.getLabelColIndex())){
									gRow.put(allCols[k], grp.getLabel()); 
								}else{
									gRow.put(allCols[k], ""); 
								}
							}
							jrows.put(gRow);
							//设置上一分组的值。第一个分组没有“上一分组”，不需要做
							if(!"".equals(preGroup)){
								//是否有插在前面的总计行，会影响行的索引号
								int pos=rpt.getTotalRow()>0&&rpt.getTotalPosition()>0?preGIndex+1:preGIndex;
								JSONObject preRow=jrows.getJSONObject(pos);
								for(int q=0;q<gflds.size();q++){
									String gfld=(String)gflds.get(q);
									preRow.put(gfld, gcounts[q]);
								}
							}
							//记录新的分组行的索引位置。
							groupCount++;
							preGIndex=groupCount+i-1;
						}else{
							//分组在明细后的，preGroup的初始值和第一条记录切换时不输出
							if(!"".equals(preGroup)){
								//如果分组行在明细之后，则在分组切换时插入一分组行，小计数组信息也写入这行。
								JSONObject gRow=new JSONObject();
								for(int k=0;k<allCols.length;k++){
									String cCol=allCols[k];
									if(cCol.equalsIgnoreCase(grp.getLabelColIndex())){
										gRow.put(cCol, grp.getLabel()); 
									}else{
										gRow.put(cCol, "");
									}
									for(int q=0;q<gflds.size();q++){
										String gfld=(String)gflds.get(q);
										if(cCol.equalsIgnoreCase(gfld)){
											gRow.put(cCol, gcounts[q]);
											break;
										}
									}
								}
								jrows.put(gRow);
							}
						}
						gcounts=new double[gflds.size()];
					}
					preGroup=gVal;
					for(int d=0;d<gcounts.length;d++){
						String sval=(String)cells.get((String)gflds.get(d));
						double dval=0;
						try{ dval=Double.parseDouble(sval);}catch(Exception e){}
						gcounts[d]+=dval;
					}
				}
				
				//如果有总计定义，计算总计列
				if(tcounts!=null){
					for(int p=0;p<tflds.length;p++){
						String tval=(String)cells.get(tflds[p]);
						try{
							double dval=Double.parseDouble(tval);
							tcounts[p]+=dval;
						}catch(Exception e){}
					}
				}	
				//各个行数据
				jrow.put("autoIndex", i+1);
				for(int j=0;j<allCols.length;j++){
					String col=allCols[j]==null?"":allCols[j];
					String val=(String)cells.get(col);
					jrow.put(col, val);
				}
				jrows.put(jrow);
			}
			//数据循环结束后，处理分组
			if(gcounts!=null){
				//如果分组在明细前，数据循环结束后，不需要输出新的分组行，但要设置上一分组的小计
				if(grp.getGroupPosition()==1){
					//是否有插在前面的总计行，会影响行的索引号
					int pos=rpt.getTotalRow()>0&&rpt.getTotalPosition()>0?preGIndex+1:preGIndex;
					JSONObject preRow=jrows.getJSONObject(pos);
					for(int q=0;q<gflds.size();q++){
						String gfld=(String)gflds.get(q);
						preRow.put(gfld, gcounts[q]);
					}
				}else{
					//如果分组行在明细之后，则在分组切换时插入一分组行，小计数组信息也写入这行。
					JSONObject gRow=new JSONObject();
					for(int k=0;k<allCols.length;k++){
						String cCol=allCols[k];
						if(cCol.equalsIgnoreCase(grp.getLabelColIndex())){
							gRow.put(cCol, grp.getLabel()); 
						}else{
							gRow.put(cCol, "");
						}
						for(int q=0;q<gflds.size();q++){
							String gfld=(String)gflds.get(q);
							if(cCol.equalsIgnoreCase(gfld)){
								gRow.put(cCol, gcounts[q]);
								break;
							}
						}	
					}
					jrows.put(gRow);
				}
			}
			//处理总计
			if(tcounts!=null){
				if(rpt.getTotalPosition()>0){
					JSONObject tRow=jrows.getJSONObject(0);
					for(int i=0;i<tflds.length;i++){
						tRow.put(tflds[i], tcounts[i]);
					}
				}else{
					JSONObject tRow=new JSONObject();
					for(int k=0;k<allCols.length;k++){
						String cCol=allCols[k];
						if(cCol.equalsIgnoreCase(rpt.getTotalLabelIndex())){
							tRow.put(cCol, "总计"); 
						}else{
							tRow.put(cCol, "");
						}
						for(int i=0;i<tflds.length;i++){
							if(cCol.equalsIgnoreCase(tflds[i])){
								tRow.put(cCol, tcounts[i]);
								break;
							}
						}
					}
					jrows.put(tRow);
				}
			}
		}catch(Exception e){
			throw new ParseReportException("将报表数据解析为JSON格式时发生错误："+e.toString());
		}
		return jrows;
	}
	/**
	 * 分页报表数据解析。
	 * 该解析方法不负责数据的进一步加工，比如添加分组行等，否则会影响记录数等属性。
	 * 因此一般在分页模式为“取数前分页”(pagingMode==0)时调用该解析方法。
	 * @param count 要取的记录数。
	 * @param rpt 报表模板，包含报表定义信息。 
	 * @param dts 本次请求获取到的报表数据记录集。
	 * @param start 本次取数的起始行。
	 * @return 指定范围的记录集解析后的json串。
	 * @throws Exception
	 */
	public String parseReportDataPaging(int count,Report rpt,DataSet dts,int start)throws Exception{
		if(dts==null||dts.getRows()==null)return "";
		JSONObject allRecords=new JSONObject();
		String[] allCols=dts.getMetaData().getColumnNames();
		try{
			allRecords.put("totalCount", count);
			JSONArray jrows=new JSONArray();
			List dtRows=dts.getRows();
			for(int i=0;i<dtRows.size();i++){
				Row row=(Row)dtRows.get(i);
				Map cells=row.getCells();
				if(cells==null){
					continue;
				}
				JSONObject jrow=new JSONObject();
				jrow.put("autoIndex", start+i+1);
				for(int j=0;j<allCols.length;j++){
					String col=allCols[j]==null?"":allCols[j];
					String val=(String)cells.get(col);
					jrow.put(col, val);
				}
				jrows.put(jrow);
			}
			allRecords.put("rptData", jrows);
		}catch(Exception e){
			throw new ParseReportException("将报表数据解析为JSON格式时发生错误："+e.toString());
		}
		return allRecords.toString();
	}
	/**
	 * 将参数待选项集合转换成json格式输出。
	 * 参数渲染为树时使用。
	 * @param items 参数待选项集合。
	 * @return 转换后的json格式串。
	 * @throws Exception
	 */
	public String parseOptionItemsToTree(List items,Parameter pa)throws Exception{
		JSONArray jItems = parseOptionItemsToTreeList(items,pa);
		return jItems==null?"":jItems.toString();
	}
	public JSONArray parseOptionItemsToTreeList(List items,Parameter pa)throws Exception{
		if(items==null)return null;
		JSONArray jItems=new JSONArray();
		try{
			//如果是单选的，以最后一个默认值为最终的默认值。
			if(pa.getIsMulti()==0){
				int defaultIndex=-1;
				if(items!=null){
					for(int j=0;j<items.size();j++){
						OptionItem oi=(OptionItem)items.get(j);
						if(oi.getIsDefault()>0){
							defaultIndex=j;
						}
						oi.setIsDefault(0);
					}
				}
				if(defaultIndex>=0){
					((OptionItem)items.get(defaultIndex)).setIsDefault(1);
				}
			}
			
			for(int i=0;i<items.size();i++){
				OptionItem oi=(OptionItem)items.get(i);
				if(StringUtils.isEmpty(oi.getPid())){
					jItems.put(parseOptionItem(items,oi));
				}
			}
		}catch(Exception e){
			throw new ParseReportException("参数待选项数据解析为JSON格式时发生错误："+e.toString());
		}
		return jItems;
	}
	
	private JSONObject parseOptionItem(List initItems,OptionItem oi)throws Exception{
		if(oi==null)return null;
		JSONObject ji=new JSONObject();
		ji.put("id", oi.getBm());
		ji.put("text", oi.getName());
		ji.put("leaf", true);
		ji.put("cls","file");	
		ji.put("checked", oi.getIsDefault()>0);
		//检查当前节点的子节点
		String nextPid=oi.getBm();
		JSONArray cArray=new JSONArray();
		for(int i=0;i<initItems.size();i++){
			OptionItem item=(OptionItem)initItems.get(i);
			if(item!=null&&nextPid.equals(item.getPid())){
				if(oi.getIsleaf()==0){
					ji.put("leaf", false);
					ji.put("cls","folder");
					cArray.put(parseOptionItem(initItems,item));
				}
			}
		}
		if(cArray.length()>0){
			ji.put("children", cArray);
		}
		return ji;
	}
	public String parseUnits(List items)throws Exception{
		JSONArray jarrUnits = null;
		try{
			if(items==null||items.size()==0){
				return "";
			}
			jarrUnits=new JSONArray();
			for(int i=0;i<items.size();i++){
				Unit un=(Unit)items.get(i);
				JSONObject ji = new JSONObject();
				ji.put("id", un.getId());
				ji.put("name",un.getName());
				ji.put("renderFun", un.getRenderFun());
				jarrUnits.put(ji);
			}
		}catch(Exception e){
			System.out.println(e.toString());
		}
		return jarrUnits.toString();
	}
	
	public static void main(String[] args){
		Connection conn=null;
		PreparedStatement ps=null;
		ResultSet rs=null;
		List items=new ArrayList();
		try{
			Class.forName("oracle.jdbc.driver.OracleDriver").newInstance();
			conn = DriverManager.getConnection("jdbc:oracle:thin:@144.20.80.119:1521:dnft", "dnft_xs", "dnft_xs");
			String sql="SELECT BM,MC,PID,ISLEAF FROM BM_CONT WHERE TABLE_BM='BM_CZFP'";
			ps=conn.prepareStatement(sql);
			rs=ps.executeQuery();
			while(rs.next()){
				OptionItem o=new OptionItem();
				o.setBm(rs.getString("bm"));
				o.setName(rs.getString("mc"));
				o.setIsleaf(rs.getInt("isleaf"));
				o.setPid(rs.getString("pid")==null?"":rs.getString("pid"));
				items.add(o);
			}
			rs.close();
			ps.close();
			conn.close();
			String json=RptDataJsonParser.getRptDataParser().parseOptionItemsToTree(items,new Parameter());
			System.out.print(json);
		}catch(Exception e){
			e.printStackTrace();
			System.out.print("field test not exist");
		}finally{
			DBConnectionFactory.endDBConnect(conn, rs, ps);
		}
	}
}
