package com.fruit.query.data;
import com.fruit.query.report.*;
import java.util.*;
/**
 * 
 * @author wxh
 *2009-3-17
 *TODO 描述报表的（复杂）表头结构
 */
public class RptMultiHeader {
	private int maxLevel = 0;
	private int leafCount =0;
	private List sortedNodes;  //经过整理的有序节点集合，便于构造树
	private List initNodes;    //初始需加工的列节点集合
	private List sortedLeafNodes; //经过整理的有序的底级节点集合，该集合应在sortedNodes之后生成。
	private Map colsMap;   //以列的colId索引Column节点定义
	private List hiddenNodes;  //隐藏的列的集合
	private List displayNodes;  //显示的列的集合
	private Map di2cid;    //dataIndex对应的colId
	public RptMultiHeader(){
		
	}
	/**
	 * 
	 * 构造函数
	 * 根据节点集合和列定义信息构造复杂表头。包括对nodes中的节点重新排序
	 * @param nodes 表头所含的全部节点。
	 * @param colDf 列定义信息。
	 */
	public RptMultiHeader(List nodes,ColumnDefine colDf){
		initNodes=nodes;
		if(nodes!=null){
			hiddenNodes=new ArrayList();
			displayNodes=new ArrayList();
			for(int i=0;i<nodes.size();i++){
				Column col=(Column)nodes.get(i);
				if(col.getIsHidden()==0){
					displayNodes.add(col);
				}else{
					hiddenNodes.add(col);
				}
			}
		}
		sortNodes(colDf);
	}
	private void sortNodes(ColumnDefine colDf){
		if(displayNodes==null)return;
		List nodes=new ArrayList();
		int cLevel=0;
		String pid="";
		parseChildNodes(nodes,pid,cLevel);
		/**
		 * 自定义公式计算列，设计时已定义其公式、位置――存在真正的列节点。
		 * 自动小计的列，设计时没有节点定义，只在要小计的父节点上设置了自动小计开关。
		 * 排序后，对于有自动小计的父节点，在其下级增加小计列节点。
		 * 必是叶子节点，设置其level，并根据小计位置（前、后）设置计算节点位置，
		 * 
		 */
		if(nodes!=null){
			//如果有自动总计列
			Column tCol=new Column();
			if(colDf!=null&&colDf.getTotalCol()>0){
				tCol.setColName("总计");
				tCol.setColId("root_");
				tCol.setIsleaf(1);
				tCol.setLevel(1);
				tCol.setDataType(2);
				tCol.setWidth(colDf.getTotalColWidth());
				tCol.setRenderer(colDf.getTotalColRenderer());
				StringBuffer func=new StringBuffer("");
				for(int i=0;i<nodes.size();i++){
					Column col=(Column)nodes.get(i);
					if(col.getIsleaf()>0&&col.getDataIndex()!=null&&col.getDataType()!=0&&!"".equals(col.getDataIndex())&&!col.getColId().equalsIgnoreCase("autoIndex")){
						func.append("r.data.");
						func.append(col.getDataIndex());
						func.append("+");
					}
				}
				if(func.toString().length()>0){
					String sFun=func.toString();
					sFun=sFun.substring(0, sFun.length()-1);
					tCol.setColFunction(sFun);
				}
			}
			
			for(int i=0;i<nodes.size();i++){
				Column col=(Column)nodes.get(i);
				int lv=col.getLevel();
				//如果是自动小计的非叶子节点，且该节点不是最后一个（如果是，这个节点结构上是错误的），利用其子节点构造小计列
				if(col.getIsleaf()==0&&col.getCalculate_mode()==1&&i<nodes.size()-1){
					Column aCol=new Column();
					//构造小计列
					aCol.setColName("小计");
					aCol.setColId("auto_"+col.getColId());
					aCol.setDataType(2);
					aCol.setIsleaf(1);
					aCol.setLevel(lv+1);
					aCol.setWidth(120);
					String renderer="";
					StringBuffer func=new StringBuffer("");
					int lstChild=i+1;
					for(int j=i+1;j<nodes.size();j++){
						Column tmpCol=(Column)nodes.get(j);
						//后面的节点如果层次和要小计的父节点一样，则表示其子/孙节点遍历完毕
						if(tmpCol.getLevel()<=lv){
							break;
						}
						lstChild=j;
						if("".equals(renderer)){
							renderer=tmpCol.getRenderer();
						}
						//将其下面各个底级节点相加
						if(tmpCol.getIsleaf()>0&&tmpCol.getLevel()>lv&&tmpCol.getDataIndex()!=null&&!"".equals(tmpCol.getDataIndex())&&tmpCol.getDataType()!=0){
							func.append("r.data.");
							func.append(tmpCol.getDataIndex());
							func.append("+");
						}
					}
					aCol.setRenderer(renderer);
					if(func.toString().length()>0){
						String sFun=func.toString();
						sFun=sFun.substring(0, sFun.length()-1);
						aCol.setColFunction(sFun);
					}
					//插入相应的位置
					if(col.getFuncPosition()==1){
						nodes.add(++i,aCol);
						leafCount++;
					}else{
						nodes.add(lstChild+1,aCol);
						leafCount++;
					}
				}
			}
			//插入总计列
			if(colDf!=null&&colDf.getTotalCol()>0&&colDf.getTotalPos()!=999){
				nodes.add(colDf.getTotalPos()-1, tCol);
				leafCount++;
			}else if(colDf!=null&&colDf.getTotalCol()>0&&colDf.getTotalPos()==999){
				nodes.add(tCol);
				leafCount++;
			}
		}
		sortedNodes=nodes;
	}
	
	//递归的将列头节点整理成先序树的结构（未必有根节点）。
	private void parseChildNodes(List nodes,String pid,int cLevel){
		if(pid==null)return;
		for(int i=0;i<displayNodes.size();i++){
			Column col=(Column)displayNodes.get(i);
			if(col!=null&&pid.equals(col.getPid())){
				int nl=cLevel+1;
				if(nl>maxLevel){
					maxLevel=nl;
				}
				col.setLevel(nl);
				//如果是底级列，直接加入集合
				if(col.getIsleaf()>0){
					leafCount++;
					nodes.add(col);
				}else{
					String cid=col.getColId();
					nodes.add(col);
					parseChildNodes(nodes,cid,nl);
				}
			}
		}
	}
	/**
	 * 节点所跨的行。
	 * 根据节点所在的层，判断其跨多少行。只有底级列才需要判断跨行。
	 * @param nowLevel 当前叶子节点的层次
	 * @return 节点所跨行数。最少为1层。
	 */
	public int getRowSpan(int nowLevel){
		int rowSpan= maxLevel - nowLevel + 1;
		return rowSpan;
	}
	/**
	 * 获取指定节点在排序后的集合中的位置。
	 * @param node
	 * @return 指定列的列序号
	 */
	public int getColIndex(Column node){
		int colIndex=0;
		if(sortedNodes==null||sortedNodes.size()==0)
			return 0;
		for(int i=0;i<sortedNodes.size();i++){
			Column col=(Column)sortedNodes.get(i);
			if(node.getColId().equals(col.getColId())){
				break;
			}else{
				if(col.getIsleaf()==1){
					colIndex++;
				}
			}
		}
		return colIndex;
	}
	/**
	 * 节点所跨的列数
	 * 只有非叶子节点需要跨列。对于先序排列树，遇到层次大于等于当前节点的，为其子/孙节点
	 * @param colIndex 开始查找的索引号，本方法要求从当前节点的<b>下一索引号</b>开始查找。
	 * @param nowLevel <b>当前</b>节点的层次
	 * @return 节点所跨的列数
	 */
	public int getColSpan(int colIndex,int nowLevel){
		int colSpan=0;
		for(int i = colIndex; i < sortedNodes.size(); i++){
			Column col=(Column)sortedNodes.get(i);
			if(col.getLevel()>nowLevel){
				if(col.getIsleaf()>0){
					colSpan++;
				}
			}else{
				break;
			}
	    }
		return colSpan;
	}
	/**
	 * 排序后的节点集合。
	 * 复杂表头节点集合排列为先序树。
	 * @return 排序后的节点集合
	 */
	public List getSortedNodes(){
		return sortedNodes;
	}
	/**
	 * 显示列集合中，底级节点的总数，不包括隐藏列。
	 * @return 显示列集合中底级节点总数。
	 */
	public int getLeafCount() {
		return leafCount;
	}
	/**
	 * 排序后，列结点树的最大深度。
	 * @return 列结点树的最大深度
	 */
	public int getMaxLevel() {
		return maxLevel;
	}
	public List getSortedLeafNodes(){
		if(sortedNodes==null){
			return null;
		}
		if(sortedLeafNodes==null){
			sortedLeafNodes=new ArrayList();
			for(int i=0;i<sortedNodes.size();i++){
				Column col=(Column)sortedNodes.get(i);
				if(col!=null&&col.getIsleaf()==1){
					sortedLeafNodes.add(col);
				}
			}
		}
		return sortedLeafNodes;
	}
	/**
	 * 获取所有底级节点的map，以colId索引列节点。包括隐藏列。
	 * 自动构造的计算列，其colId是“auto_”+父列colId。自动总计列的colId为root_
	 * @return
	 */
	public Map getColsMap(){
		if(sortedNodes==null){
			return null;
		}
		if(colsMap==null){
			colsMap=new HashMap();
			for(int i=0;i<sortedNodes.size();i++){
				Column col=(Column)sortedNodes.get(i);
				if(col!=null&&col.getIsleaf()==1){
					colsMap.put(col.getColId(), col);
				}
			}
			//包括隐藏列，外部可以籍此索引到隐藏列的定义。
			if(hiddenNodes!=null){
				for(int i=0;i<hiddenNodes.size();i++){
					Column col=(Column)hiddenNodes.get(i);
					if(col!=null&&col.getIsleaf()==1){
						colsMap.put(col.getColId(), col);
					}
				}
			}
		}
		return colsMap;
	}
	
	public Map getDataIndex2ColIdMap(){
		if(di2cid==null){
			if(colsMap==null){
				getColsMap();
			}
			di2cid=new HashMap();
			Iterator cit=colsMap.entrySet().iterator();
	    	while(cit.hasNext()){
	    		Map.Entry cpairs = (Map.Entry)cit.next();    
	    		Column col=(Column)cpairs.getValue();
	    		if(col==null){
	    			continue;
	    		}
	    		if(col.getDataIndex()!=null&&!"".equals(col.getDataIndex())){
	    			di2cid.put(col.getDataIndex(), col.getColId());
	    		}
	    	}
		}
		return di2cid;
	}
	/**
	 * 隐藏的列节点集合。隐藏的列节点不排序，不显示，也不参与自动列的插入布局、计算
	 * @return 隐藏的列节点集合。
	 */
	public List getHiddenNodes() {
		return hiddenNodes;
	}
	/**
	 * 
	 * @param hiddenNodes
	 */
	public void setHiddenNodes(List hiddenNodes) {
		this.hiddenNodes = hiddenNodes;
	}
}
