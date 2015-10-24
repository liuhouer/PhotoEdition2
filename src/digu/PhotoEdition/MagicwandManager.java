package digu.PhotoEdition;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Bitmap.Config;
import android.graphics.Paint.Style;

public class MagicwandManager {
	//魔术棒类
	public class Magicwand {
		private final ArrayList<Bitmap> gadgets;
		private final ArrayList<PointF> points;
		private final ArrayList<Float> scales;
		private boolean noGap;
		//等价构图
		private Bitmap bmp;
		//绘图外框（非判断边框），与points一致
		private RectF wholeRect;
		//平移量
		private float x;
		private float y;
		
		//构造函数
		@SuppressWarnings("unchecked")
		public Magicwand(ArrayList<Bitmap> gadgets) {
			this.gadgets = (ArrayList<Bitmap>) gadgets.clone();
			points = new ArrayList<PointF>();
			scales = new ArrayList<Float>();
			noGap = true;
			
			bmp = null;
			wholeRect = new RectF();
			x = 0;
			y = 0;
		}
		//复制构造函数
		@SuppressWarnings("unchecked")
		public Magicwand(Magicwand magicwand) {
			gadgets = (ArrayList<Bitmap>) magicwand.gadgets.clone();
			points = new ArrayList<PointF>();
			scales = new ArrayList<Float>();
			for(int i = 0; i < magicwand.points.size(); i++) {
				PointF point = magicwand.points.get(i);
				points.add(new PointF(point.x, point.y));
				scales.add(magicwand.scales.get(i).floatValue());
			}//for i
			noGap = magicwand.noGap;
			if(magicwand.bmp != null)
				bmp = Bitmap.createBitmap(magicwand.bmp);
			wholeRect = new RectF(magicwand.wholeRect);
			x = magicwand.x;
			y = magicwand.y;
			
		}
		//克隆函数
		public Magicwand clone() {
			Magicwand magicwand = new Magicwand(this);
			return magicwand;
		}
		//添加落点
		public void addPoint(PointF point, float scale) {
			points.add(point);
			scales.add(scale);
			noGap = false;
			Bitmap gadget = gadgets.get((points.size() - 1) % gadgets.size());
			float x1 = point.x - gadget.getWidth() * scale / 2;
			float y1 = point.y - gadget.getHeight() * scale / 2;
			float x2 = point.x + gadget.getWidth() * scale / 2;
			float y2 = point.y + gadget.getHeight() * scale / 2;

			if(points.size() == 1) {//第一个点
				wholeRect.left = x1;
				wholeRect.top = y1;
				wholeRect.right = x2;
				wholeRect.bottom = y2;
			}
			else {
				if(x1 < wholeRect.left)
					wholeRect.left = x1;
				if(y1 < wholeRect.top)
					wholeRect.top = y1;
				if(x2 > wholeRect.right)
					wholeRect.right = x2;
				if(y2 > wholeRect.bottom)
					wholeRect.bottom = y2;
			}
			redrawBmp(1.0f, 1.0f);
		}
		//平移魔术棒
		public void translate(float dx, float dy) {
			x += dx;
			y += dy;
		}
		//获取所有机件
		public ArrayList<Bitmap> getGadgets() {
			return gadgets;
		}
		
		//获取落点总数
		public int getSum() {
			return points.size();
		}
		//获取落点
		public PointF getPoint(int n) {
			if(n >= 0 && n < points.size())
				return points.get(n);
			else
				return null;
		}
		//获取缩放比例
		public float getScale(int n) {
			if(n >= 0 && n < scales.size())
				return scales.get(n);
			else
				return -1.0f;
		}
		
		//判断是否无隙
		public boolean isNoGap() {
			return noGap;
		}
		//清空间隙
		public void setNoGap() {
			noGap = true;
		}
		//获取间隙距离
		public PointF getGap(float scale) {
			Bitmap bmp1 = gadgets.get((points.size() - 1) % gadgets.size());
			Bitmap bmp2 = gadgets.get(points.size() % gadgets.size());
			float lastscale = scales.get(scales.size() - 1);
			float x = (bmp1.getWidth() * lastscale + bmp2.getWidth() * scale) / 2;
			float y = (bmp1.getHeight() * lastscale + bmp2.getHeight() * scale) / 2;
			return (new PointF(x, y));
		}
		//获取选区区域群
		public ArrayList<RectF> getRects() {
			ArrayList<RectF> rects = new ArrayList<RectF>();
			for(int i = 0; i < points.size(); i++) {
				Bitmap gadget = gadgets.get(i % gadgets.size());
				float scale = scales.get(i);
				PointF point = points.get(i);
				
				RectF rect = new RectF(point.x - gadget.getWidth() * scale / 2 + x, 
						point.y - gadget.getHeight() * scale / 2 + y, 
						point.x + gadget.getWidth() * scale / 2 + x, 
						point.y + gadget.getHeight() * scale / 2 + y);
				rects.add(rect);
			}//for i
			
			return rects;
		}
		//获取绘图外框
		public RectF getWholeRect() {
			return (new RectF(wholeRect.left + x, wholeRect.top + y, 
					wholeRect.right + x, wholeRect.bottom + y));
		}
		//获取魔术棒图
		public Bitmap getBmp() {
			return bmp;
		}
		//重绘魔术棒图
		public Bitmap redrawBmp(float scalewidth, float scaleheight) {
			if(bmp != null && !bmp.isRecycled())
				bmp.recycle();
			bmp = Bitmap.createBitmap((int) ((wholeRect.right - wholeRect.left + 2) * scalewidth), 
					(int) ((wholeRect.bottom - wholeRect.top + 2) * scaleheight), Config.ARGB_8888);
			Canvas canvas = new Canvas(bmp);
			Matrix matrix = new Matrix();
			for(int i = 0; i < points.size(); i++) {
				Bitmap gadget = gadgets.get(i % gadgets.size());
				float scale = scales.get(i);
				PointF point = points.get(i);
				
				matrix.reset();
				matrix.postScale(scale * scalewidth, scale * scaleheight);
				matrix.postTranslate((point.x - gadget.getWidth() * scale / 2 - wholeRect.left) * scalewidth, 
						(point.y - gadget.getHeight() * scale / 2 - wholeRect.top) * scaleheight);
				canvas.drawBitmap(gadget, matrix, null);
			}//for i
			
			return bmp;
		}
		//获取平移量
		public PointF getOffset(float scalewidth, float scaleheight) {
			return (new PointF((wholeRect.left + x) * scalewidth, (wholeRect.top + y) * scaleheight));
		}
		//获取缩略图
		public Bitmap getSmallBmp() {
			return gadgets.get(0);
		}
		//清空魔术棒图片
		public void clearBmp() {
			if(bmp != null && !bmp.isRecycled())
				bmp.recycle();
			bmp = null;
		}
	}//end class MagicWand
	private Context context;
	private HashMap<String, ArrayList<String>> gadgetsMap;
	private HashMap<Long, Magicwand> magicwands;
	private Magicwand currMagicwand;
	private boolean isDrawing;
	private BitmapManager bitmapManager;
	private float currScale;	
	private int viewWidth;
	private int viewHeight;
	private float adjust;
	private long midNumber;
	
	//构造函数
	@SuppressWarnings("unchecked")
	public MagicwandManager(Context context, HashMap<String, ArrayList<String>> idgadgets, float adjust, 
			int viewwidth, int viewheight) {
		this.context = context;
		gadgetsMap = (HashMap<String, ArrayList<String>>) idgadgets.clone();
		magicwands = new HashMap<Long, Magicwand>();
		currMagicwand = null;
		isDrawing = false;
		bitmapManager = new BitmapManager(100, 100);
		currScale = 0.55f;
		viewWidth = viewwidth;
		viewHeight = viewheight;
		this.adjust = adjust;
		midNumber = 0;
	}
	//新增魔术棒
	public long addMagicwand(String id) {
		ArrayList<String> gadgetpaths = gadgetsMap.get(id);
		if(gadgetpaths == null)
			return -1;
		
		ArrayList<Bitmap> gadgets = new ArrayList<Bitmap>();		
		for(String gadgetpath : gadgetpaths) {
			Bitmap bmp = bitmapManager.getBitmapFromAssets(context, gadgetpath);
			if(bmp != null)
				gadgets.add(bmp);
		}//for gadgetpath
		if(gadgets.isEmpty())
			return -1;
		
		beginMagicwand();
		currMagicwand = new Magicwand(gadgets);
		magicwands.put(midNumber, currMagicwand);
		midNumber++;
		return (midNumber - 1);
	}
	//新增当前魔术棒
	@SuppressWarnings("unchecked")
	public long addCurrMagicwand() {
		ArrayList<Bitmap> gadgets = (ArrayList<Bitmap>) currMagicwand.getGadgets().clone();
		beginMagicwand();
		currMagicwand = new Magicwand(gadgets);	
		magicwands.put(midNumber, currMagicwand);
		midNumber++;
		return (midNumber - 1);
	}
	//继续操作魔术棒
	public void continueMagicwand() {
		if(currMagicwand != null)
			currMagicwand.setNoGap();
	}
	//记录落点
	public boolean recordPoint(PointF point) {
		if(currMagicwand == null)
			return false;
		
		if(currMagicwand.isNoGap()) {
			currMagicwand.addPoint(point, currScale);
			return true;
		}

		PointF lastpoint = currMagicwand.getPoint(currMagicwand.getSum() - 1);
		PointF gap = currMagicwand.getGap(currScale);
		if(Math.abs(point.x - lastpoint.x) > gap.x || 
				Math.abs(point.y - lastpoint.y) > gap.y) {
			currMagicwand.addPoint(point, currScale);
			return true;
		}
		
		return false;
	}
	//结束写魔术棒
	public void endMagicwand() {
		if(isDrawing) {
			currMagicwand.redrawBmp(1.0f, 1.0f);
			isDrawing = false;
		}
	}
	//开始写魔术棒
	private void beginMagicwand() {
		endMagicwand();
		isDrawing = true;
	}
	//替换魔术棒
	public void subMagicwand(long mid, Magicwand magicwand) {
		magicwands.remove(mid);
		magicwands.put(mid, magicwand);
	}
	//设置当前缩放比例
	public void setScale(float scale) {
		currScale = (scale * 0.9f + 10.0f) * adjust / 100;
	}
	//获取当前魔术棒缩放比例（对应控制条）
	public float getScale() {
		if(currMagicwand == null)
			return -1.0f;
		
		return ((currMagicwand.getScale(currMagicwand.getSum() - 1) * 100 / adjust - 10.0f) / 0.9f);
	}
	//获取魔术棒
	public Magicwand getMagicwand(long mid) {
		return magicwands.get(mid);
	}
	//获取当前魔术棒
	public Magicwand getCurrMagicwand() {
		return currMagicwand;
	}
	//失去焦点
	public void lostFocus() {
		endMagicwand();
		currMagicwand = null;
	}
	//获取焦点
	public boolean setFocus(long mid) {
		Magicwand magicwand = magicwands.get(mid);
		if(magicwand == null)
			return false;
		currMagicwand = magicwand;
		return true;
	}
	//画单个魔术棒
	public void drawMagicwand(Bitmap bmp, long mid) {
		if(bmp == null)
			return;
		Magicwand magicwand = magicwands.get(mid);
		if(magicwand == null)
			return;
		
		float scalewidth = (float)bmp.getWidth() / viewWidth;
		float scaleheight = (float)bmp.getHeight() / viewHeight;
		
		Canvas canvas = new Canvas(bmp);
		Bitmap mwbmp = magicwand.redrawBmp(scalewidth, scaleheight);
		PointF offset = magicwand.getOffset(scalewidth, scaleheight);
		canvas.drawBitmap(mwbmp, offset.x, offset.y, null);
	}
	//画当前魔术棒
	public void drawCurrMagicwand(Canvas canvas, PhotoEditionView2.DrawMode mode) {
		if(canvas == null || currMagicwand == null)
			return;
		Bitmap mwbmp = currMagicwand.getBmp();
		if(mwbmp == null)
			return;
		
		PointF offset = currMagicwand.getOffset(1.0f, 1.0f);
		Paint paint = new Paint();
		if(mode != PhotoEditionView2.DrawMode.SELECTED)
			paint.setAlpha(150);
		canvas.drawBitmap(mwbmp, offset.x, offset.y, paint);
		//画边框
		if(!isDrawing) {
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
			canvas.drawRoundRect(currMagicwand.getWholeRect(), 
					5.0f, 5.0f, paint);
		}
	}
	//获取当前魔术棒的图片
	public Bitmap getCurrMagicwandBmp() {
		return currMagicwand.getSmallBmp();
	}
	//清空已有魔术棒
	public void clear() {
		lostFocus();
		for(Magicwand magicwand : magicwands.values()) {
			magicwand.clearBmp();
		}//for magicwand
		magicwands.clear();
		midNumber = 0;
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
