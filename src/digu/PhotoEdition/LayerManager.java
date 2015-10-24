package digu.PhotoEdition;

import java.util.ArrayList;
import java.util.HashMap;

import android.graphics.RectF;

public class LayerManager {
	//图层类
	public class Layer {
		//物体类型，外部识别用
		private final String type;
		//物体id，具有不变性
		private final long oid;
		
		//可选区域群
		private ArrayList<RectF> rects;
		//全局可选区域
		private RectF wholeRect;
		
		//构造函数
		public Layer(String type, long oid, ArrayList<RectF> rects, RectF wholerect) {
			this.type = type;
			this.oid = oid;
			
			this.rects = new ArrayList<RectF>();
			if(rects != null) {
				for(int i = 0; i < rects.size(); i++) {
					this.rects.add(new RectF(rects.get(i)));
				}//for i
			}
			if(wholerect == null)
				wholeRect = new RectF();
			else
				wholeRect = new RectF(wholerect);
		}
		//复制构造函数
		public Layer(Layer layer) {
			type = layer.type;
			oid = layer.oid;
			
			rects = new ArrayList<RectF>();
			for(int i = 0; i < layer.rects.size(); i++) {
				rects.add(new RectF(layer.rects.get(i)));
			}//for i
			wholeRect = new RectF(layer.wholeRect);
		}
		//克隆函数
		public Layer clone() {
			Layer layer = new Layer(this);
			return layer;
		}

		//改变可选区域
		public void changeRects(ArrayList<RectF> rects, RectF wholerect) {
			this.rects.clear();
			if(rects != null) {
				for(int i = 0; i < rects.size(); i++) {
					this.rects.add(new RectF(rects.get(i)));
				}//for i
			}
			wholeRect.left = wholerect.left;
			wholeRect.top = wholerect.top;
			wholeRect.right = wholerect.right;
			wholeRect.bottom = wholerect.bottom;
		}
		//获取类型
		public String getType() {
			return type;
		}
		//获取id
		public long getOid() {
			return oid;
		}
		
		//获取可选区域群
		public ArrayList<RectF> getRects() {
			return rects;
		}
		//获取全局可选区域
		public RectF getWholeRect() {
			return wholeRect;
		}
	}
	
	private HashMap<Long, Layer> layers;
	//图层id
	private ArrayList<Long> lidList;
	private long lidNumber;
	
	//构造函数
	public LayerManager() {
		layers = new HashMap<Long, Layer>();
		lidList = new ArrayList<Long>();
		lidNumber = 0;
	}
	//获取图层总数
	public int getSum() {
		return lidList.size();
	}
	
	//根据图层id，获取图层
	public Layer getLayer(long lid) {
		return layers.get(lid);
	}
	//获取位于level层的图层id
	public long getLid(int level) {
		if(level < 0 && level >= lidList.size())
			return -1;

		return lidList.get(level);
	}
	//获取图层所在层数，-1不在
	public int getLevel(long lid) {
		return lidList.indexOf(lid);
	}
	//获取点选图层id
	public long getTouchedLid(float x, float y) {
		//判断可选区域群
		for(int i = lidList.size() - 1; i >= 0; i--) {
			Layer layer = layers.get(lidList.get(i));
			ArrayList<RectF> rects = layer.getRects();
			for(RectF rect : rects) {
				if(x >= rect.left && x <= rect.right &&
						y >= rect.top && y <= rect.bottom) {
					return lidList.get(i);
				}
			}//for rect
		}//for i
		
		//判断全局区域
		for(int i = lidList.size() - 1; i >= 0; i--) {
			Layer layer = layers.get(lidList.get(i));
			RectF rect = layer.getWholeRect();
			if(x >= rect.left && x <= rect.right &&
					y >= rect.top && y <= rect.bottom) {
				return lidList.get(i);
			}
		}//for i
		
		return -1;
	}
	
	//添加图层，返回所在层数
	public long addLayer(String type, long oid, ArrayList<RectF> rects, RectF wholerect) {
		Layer newlayer = new Layer(type, oid, rects, wholerect);
		long newlid = -1;
		for(long lid : layers.keySet()) {
			Layer layer = layers.get(lid);
			if(newlayer.oid == layer.oid && newlayer.type == layer.type) {
				newlid = lid;
				layers.remove(lid);
				break;
			}
		}//for lid
		if(newlid < 0) {
			newlid = lidNumber;
			lidNumber++;
		}
		layers.put(newlid, newlayer);
		
		return newlid;		
	}
	//替换图层
	public void subLayer(long lid, Layer layer) {
		layers.remove(lid);
		layers.put(lid, layer);
	}
	//提图层到顶层（可撤销），返回所在层数
	public int setLayerTop(long lid) {
		if(lid < 0)
			return -1;
		lidList.remove(lid);
		lidList.add(lid);
		
		return lidList.indexOf(lid);
	}
	//提图层到level层（可撤销），返回所在层数
	public int setLayerLevel(long lid, int level) {
		if(lid <0 || level < 0)
			return -1;
		
		lidList.remove(lid);
		if(level >= lidList.size())
			lidList.add(lid);
		else 
			lidList.add(level, lid);
		
		return lidList.indexOf(lid);
	}
	//更新图层（可撤销）
	public void updateLayer(long lid, ArrayList<RectF> rects, RectF wholerect) {
		if(lid < 0)
			return;
		Layer layer = layers.get(lid);
		if(layer == null)
			return;
		layer.changeRects(rects, wholerect);
	}
	//移除图层（可撤销），返回所在层数
	public int removeLayer(long lid) {
		if(lid < 0)
			return -1;
		int level = lidList.indexOf(lid);
		lidList.remove(lid);
		
		return level;
	}
	
	//清空图层与映射关系
	public void clear() {
		lidList.clear();
		layers.clear();
		lidNumber = 0;
	}
}
