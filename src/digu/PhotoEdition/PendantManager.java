package digu.PhotoEdition;

import java.util.HashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Paint.Style;

public class PendantManager {
	private static final float maxScale = 8.0f;
	public class Pendant {		
		private final Bitmap bmp;
		
		private float x;
		private float y;
		private float width;
		private float height;		
		private float rotate;
		
		private final float minWidth;
		private final float maxWidth;
		
		//构造函数
		public Pendant(Bitmap bmp, float adjust) {
			this.bmp = bmp;
			
			x = 0;
			y = 0;	
			
			minWidth = bmp.getWidth() * adjust / maxScale;
			maxWidth = bmp.getWidth() * adjust;
			
			width = (minWidth + maxWidth) / 2;
			height = bmp.getHeight() * (width / bmp.getWidth());
			rotate = 0.0f;
		}
		//复制构造函数
 		public Pendant(Pendant pendant) {
 			bmp = pendant.bmp;
 			
 			x = pendant.x;
 			y = pendant.y;	
 			width = pendant.width;
 			height = pendant.height;
 			rotate = pendant.rotate;
 			
 			minWidth = pendant.minWidth;
 			maxWidth = pendant.maxWidth;
 		}
 		//克隆函数
 		public Pendant clone() {
 			Pendant pendant = new Pendant(this);
 			return pendant;
 		}
		//平移挂件
		public void translate(float dx, float dy) {
			x += dx;
			y += dy;
		}
		//缩放挂件
		public void scale(float dscale) {
			float mscale;
			if(dscale > 1)
				mscale = Math.min(width * dscale, maxWidth) / width;
			else
				mscale = Math.max(width * dscale, minWidth) / width;
			x -= width * (mscale - 1) / 2;
			y -= height * (mscale - 1) / 2;
			width *= mscale;
			height *= mscale;
		}
		//旋转挂件
		public void rotate(float drotate) {
			rotate = (rotate + drotate + 360) % 360;
		}
		
		//获取挂件图
		public Bitmap getBmp() {
			return bmp;
		}
		//获取挂件矩阵
		public Matrix getMatrix(float scalewidth, float scaleheight) {
			if(bmp == null)
				return null;
			
			Matrix matrix = new Matrix();
			matrix.postScale(width * scalewidth / bmp.getWidth(), height * scaleheight / bmp.getHeight());
			matrix.postRotate(rotate, width * scalewidth / 2, height * scaleheight / 2);
			matrix.postTranslate(x * scalewidth, y * scaleheight);
			
			return matrix;
		}
		//获取挂件边框
		public RectF getRect() {
			double cosr = Math.abs(Math.cos(rotate * Math.PI / 180));
			double sinr = Math.abs(Math.sin(rotate * Math.PI / 180));
			
			float rWidth = (float) (width * cosr + height * sinr);
			float rHeight = (float) (height * cosr + width * sinr);
			float rX = x + width / 2 - rWidth / 2;
			float rY = y + height / 2 - rHeight / 2;
			
			return new RectF(rX, rY, rX + rWidth, rY + rHeight);
		}
		//获取坐标
		public float getX() {
			return x;
		}
		public float getY() {
			return y;
		}
		//获取宽高
		public float getWidth() {
			return width;
		}
		public float getHeight() {
			return height;
		}
		//获取最小宽和最大宽
		public float getMaxWidth() {
			return maxWidth;
		}
		public float getMinWidth() {
			return minWidth;
		}
		//获取旋转角度
		public float getRotate() {
			return rotate;
		}

	}
		
	private Context context;
	private HashMap<String, String> pendantMap;
	private HashMap<String, String> exPendantMap;
	private HashMap<Long, Pendant> pendants;
	private Pendant currPendant;
	private BitmapManager bitmapManager;
	private int viewWidth;
	private int viewHeight;
	private float adjust;
	private long pidNumber;

	//挂件初始化位置位移量
	private static final Point[] dPos;
	static {
		dPos = new Point[4];
		dPos[0] = new Point(50,0);
		dPos[1] = new Point(0,-50);
		dPos[2] = new Point(-50,0);
		dPos[3] = new Point(0,50);
	}
	private int dType;
		
	//构造函数（资源路径输入），载入可选挂件及初始化大小
	@SuppressWarnings("unchecked")
	public PendantManager(Context context, HashMap<String, String> idpendant, float adjust, 
			int viewwidth, int viewheight) {
		this.context = context;
		if(idpendant != null)
			pendantMap = (HashMap<String, String>) idpendant.clone();
		else
			pendantMap = new HashMap<String, String>();
		exPendantMap = new HashMap<String, String>();
		pendants = new HashMap<Long, Pendant>();
		currPendant = null;
		bitmapManager = new BitmapManager();
		viewWidth = viewwidth;
		viewHeight = viewheight;
		this.adjust = adjust;
		pidNumber = 0;		

		dType = 0;
	}

	//添加动态挂件
	public void addExPendants(HashMap<String, String> idpendant) {
		if(idpendant != null)
			exPendantMap.putAll(idpendant);
	}
	
	//新增挂件
	public long addPendant(String id) {
		String pendantpath;
		Bitmap pendantbmp;

		if(pendantMap.containsKey(id)) {
			pendantpath = pendantMap.get(id);
			pendantbmp = bitmapManager.getBitmapFromAssets(context, pendantpath);
		}
		else if(exPendantMap.containsKey(id)){
			pendantpath = exPendantMap.get(id);
			pendantbmp = bitmapManager.getBitmap(pendantpath);
		}
		else {
			pendantpath = null;
			pendantbmp = null;
		}
		
		if(pendantbmp == null)
			return -1;
		
		Pendant pendant = new Pendant(pendantbmp, adjust);
		float x = (viewWidth - pendant.getWidth()) / 2;
		float y = (viewHeight - pendant.getHeight()) / 2;
		pendant.translate(x, y);
		
		pendants.put(pidNumber, pendant);
		setFocus(pidNumber);
		pidNumber++;
		
		return (pidNumber - 1);
	}
	//新增当前挂件
	public long addCurrPendant() {
		if(currPendant == null)
			return -1;
		
		Pendant pendant = new Pendant(currPendant);
		float x = (viewWidth - pendant.getWidth()) / 2 + dPos[dType].x;
		float y = (viewHeight - pendant.getHeight()) / 2 + dPos[dType].y;
		dType = (dType + 1) % 4;
		pendant.translate(x - pendant.getX(), y - pendant.getY());
		
		pendants.put(pidNumber, pendant);
		setFocus(pidNumber);
		pidNumber++;
		
		return (pidNumber - 1);
	}
	//替换挂件
	public void subPendant(long pid, Pendant pendant) {
		pendants.remove(pid);
		pendants.put(pid, pendant);
	}
	//失去焦点
	public void lostFocus() {
		currPendant = null;
	}
	//获取焦点
	public boolean setFocus(long pid) {
		Pendant pendant = pendants.get(pid);
		 if(pendant == null)
			 return false;
		currPendant = pendant;
		return true;
	}
	//缩放当前挂件
	public void scalePendant(float scale) {
		if(currPendant == null)
			return;
		
		float min = currPendant.getMinWidth();
		float max = currPendant.getMaxWidth();
		float now = currPendant.getWidth();
		float dscale = ((max - min) * scale / 100 + min) / now;
		currPendant.scale(dscale);
	}
	//旋转当前挂件
	public void rotatePendant(float scale) {
		if(currPendant == null)
			return;
		
		float now = currPendant.getRotate();
		float drotate = (360 * scale / 100 + 180) % 360 - now;
		currPendant.rotate(drotate);
	}
	//获取当前挂件缩放倍数（对应控制条）
	public float getScale() {
		if(currPendant == null)
			return -1.0f;
		
		float min = currPendant.getMinWidth();
		float max = currPendant.getMaxWidth();
		float now = currPendant.getWidth();
		return (now - min) * 100 / (max - min);
	}
	//获取当前挂件旋转角度（对应控制条）
	public float getRotate() {
		if(currPendant == null)
			return -1.0f;
		
		float now = currPendant.getRotate();
		return ((now + 180) % 360) * 100 / 360;
	}
	//获取挂件
	public Pendant getPendant(long pid) {
		return pendants.get(pid);
	}
	//获取当前挂件
	public Pendant getCurrPendant() {
		return currPendant;
	}
	//获取当前挂件的图片
	public Bitmap getCurrPendantBmp() {
		if(currPendant == null)
			return null;
		
		return currPendant.getBmp();
	}
	//画单个挂件
	public void drawPendant(Bitmap bmp, long pid) {
		if(bmp == null)
			return;
		Pendant pendant = pendants.get(pid);
		if(pendant == null)
			return;
		
		float scalewidth = (float)bmp.getWidth() / viewWidth;
		float scaleheight = (float)bmp.getHeight() / viewHeight;
		
		Canvas canvas = new Canvas(bmp);
		canvas.drawBitmap(pendant.getBmp(), 
				pendant.getMatrix(scalewidth, scaleheight), null);
	}
	//画当前挂件
	public void drawCurrPendant(Canvas canvas, PhotoEditionView2.DrawMode mode) {
		if(canvas == null || currPendant == null)
			return;
		
		Paint paint = new Paint();
		
		//画挂件
		if(mode != PhotoEditionView2.DrawMode.SELECTED)
			paint.setAlpha(150);
		canvas.drawBitmap(currPendant.getBmp(), 
				currPendant.getMatrix(1.0f, 1.0f), paint);
		//画边框
		switch(mode) {
		case SELECTED:
		case CHANGED:
			paint.setColor(Color.MAGENTA);
			paint.setStyle(Style.STROKE);
			break;
		case DELETED:
			paint.setColor(Color.argb(100, 255, 0, 0));
			break;
		}
		canvas.drawRoundRect(currPendant.getRect(), 
				5.0f, 5.0f, paint);
	}
	
	//清空已有挂件
	public void clear() {
		lostFocus();
		pendants.clear();
		pidNumber = 0;
	}
	//重设视图宽高，清空已有挂件
	public void resetView(int viewwidth, int viewheight) {
		viewWidth = viewwidth;
		viewHeight = viewheight;
	}	
	//回收位图内存
	public void recycleAll() {
		clear();
		bitmapManager.recycleAll();
	}
}
