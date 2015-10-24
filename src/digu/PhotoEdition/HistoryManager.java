package digu.PhotoEdition;

import java.util.ArrayList;

import android.util.Log;

public class HistoryManager {
	public static enum ObjType {PENDANT, MAGICWAND}
	//内部历史类（结构体）
	public class History {
		private long lid;
		private LayerManager.Layer lastLayer;
		private LayerManager.Layer currLayer;
		private int lastLevel;
		private int currLevel;
		
		private long oid;
		private Object lastObject;
		private Object currObject;
		private ObjType objType;
		
		//构造函数
		public History(long lid, LayerManager.Layer lastlayer, LayerManager.Layer currlayer, int lastlevel, int currlevel, 
				long oid, Object lastobject, Object currobject, ObjType objtype) {
			this.lid = lid;
			lastLayer = lastlayer;
			currLayer = currlayer;
			lastLevel = lastlevel;
			currLevel = currlevel;
			
			this.oid = oid;
			lastObject = lastobject;
			currObject = currobject;
			objType = objtype;
		}
	}
	
	private ArrayList<History> historyList;
	private int time;
	private LayerManager layerManager;
	private PendantManager pendantManager;
	private MagicwandManager magicwandManager;
	
	//构造函数
	public HistoryManager(LayerManager layermanager, 
			PendantManager pendantmanager, MagicwandManager magicwandmanager) {
		historyList = new ArrayList<History>();
		time = 0;
		layerManager = layermanager;
		pendantManager = pendantmanager;
		magicwandManager = magicwandmanager;
	}
	//添加历史
	private void addHistory(History history) {
		int sum = historyList.size();

		for(int i = sum - 1; i >= time; i--) {
			historyList.remove(i);
		}//for i
		historyList.add(history);
		time++;
	}

	//记录图层置换操作
	public void recordChangeLevel(long lid, int lastlevel, int currlevel) {
		History history = new History(lid, null, null, lastlevel, currlevel, 
				-1, null, null, null);
		addHistory(history);
	}
	//记录图层更新操作
	public void recordUpdate(long lid, LayerManager.Layer lastlayer, LayerManager.Layer currlayer, 
			Object lastobject, Object currobject) {
		long oid = layerManager.getLayer(lid).getOid();
		String type = layerManager.getLayer(lid).getType();
		ObjType objtype;
		if(type.intern() == "PENDANT")
			objtype = ObjType.PENDANT;
		else if(type.intern() == "MAGICWAND")
			objtype = ObjType.MAGICWAND;
		else
			objtype = null;
		History history = new History(lid, lastlayer, currlayer, -1, -1, 
				oid, lastobject, currobject, objtype);
		addHistory(history);
		Log.i("history", "update: "+lid+","+time);
	}
	
	//恢复操作
	public boolean undo() {
		if(time <= 0)
			return false;
		
		History history = historyList.get(time - 1);

		if(history.lid >= 0) {
			if(history.lastLayer != null)
				layerManager.subLayer(history.lid, history.lastLayer);
			else {
				if(history.lastLevel >= 0)
					layerManager.setLayerLevel(history.lid, history.lastLevel);
				else
					layerManager.removeLayer(history.lid);
			}
						
		}
		
		if(history.oid >=0) {
			if(history.lastLayer != null) {
				switch(history.objType) {
				case PENDANT:
					pendantManager.subPendant(history.oid, (PendantManager.Pendant)(history.lastObject));
					break;
				case MAGICWAND:
					magicwandManager.subMagicwand(history.oid, (MagicwandManager.Magicwand)(history.lastObject));
					break;
				}
			}
		}

		time--;

		return true;
	}
	//重做操作
	public boolean redo() {
		if(time >= historyList.size())
			return false;
		
		History history = historyList.get(time);
		
		if(history.lid >= 0) {
			if(history.currLayer != null)
				layerManager.subLayer(history.lid, history.currLayer);
			else {
				if(history.currLevel >= 0)
					layerManager.setLayerLevel(history.lid, history.currLevel);
				else
					layerManager.removeLayer(history.lid);
			}
						
		}
		
		if(history.oid >=0) {
			if(history.currLayer != null) {
				switch(history.objType) {
				case PENDANT:
					pendantManager.subPendant(history.oid, (PendantManager.Pendant)(history.currObject));
					break;
				case MAGICWAND:
					magicwandManager.subMagicwand(history.oid, (MagicwandManager.Magicwand)(history.currObject));
					break;
				}
			}
		}
		
		time++;
		return true;
	}
	
	//清空历史
	public void clear() {
		historyList.clear();
		time = 0;
	}
}
