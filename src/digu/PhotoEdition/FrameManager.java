package digu.PhotoEdition;

import java.util.HashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;

public class FrameManager {
	private Context context;
	private HashMap<String, String> frameMap;
	private HashMap<String, String> exFrameMap;
	private Bitmap currFrame;
	private BitmapManager bitmapManager;

	public FrameManager(Context context, HashMap<String, String> idframe) {
		this.context = context;
		frameMap = new HashMap<String, String>();
		for(String id : idframe.keySet()) {
			if(idframe.get(id) != null) {			
				String frame = new String(idframe.get(id));
				frameMap.put(id, frame);
			}
		}//for id
		exFrameMap = new HashMap<String, String>();
		currFrame = null;
		bitmapManager = new BitmapManager();	
	}
	
	//添加动态相框
	public void addExFrames(HashMap<String, String> idframe) {
		if(idframe != null)
			exFrameMap.putAll(idframe);
	}
	
	//设置当前选中相框
	public void setFrame(String id) {
		if(frameMap.containsKey(id)) {
			String framepath = frameMap.get(id);
			currFrame = bitmapManager.getBitmapFromAssets(context, framepath);
		}
		else if(exFrameMap.containsKey(id)){
			String framepath = exFrameMap.get(id);
			currFrame = bitmapManager.getBitmap(framepath);
		}
		else
			currFrame = null;
	}
	//判断是否选中相框
	public boolean isFrameEmpty() {
		return (currFrame == null);
	}
	//在图片上画相框
	public void drawFrameBmp(Bitmap bmp) {
		if(bmp == null)
			return;
		
		int width = bmp.getWidth();
		int height = bmp.getHeight();

		Canvas canvas = new Canvas(bmp); 

		if(currFrame != null) {
			Matrix matrix = new Matrix();
			matrix.postScale((float)width / currFrame.getWidth(), 
					(float)height / currFrame.getHeight());
			canvas.drawBitmap(currFrame, matrix, null);
		}

	}
	//在画布上画相框
	public void drawFrameBmp(Canvas canvas, int width, int height) {
		if(canvas == null)
			return;
		
		if(currFrame != null) {
			Matrix matrix = new Matrix();
			matrix.postScale((float)width / currFrame.getWidth(), 
					(float)height / currFrame.getHeight());
			canvas.drawBitmap(currFrame, matrix, null);
		}
	}
	
	//回收位图内存
	public void recycleAll() {
		currFrame = null;
		bitmapManager.recycleAll();	
	}
	
}
