package digu.PhotoEdition;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;

public class PhotoEditionView2 extends ImageView {

	private Bitmap originalPhoto;
	private int width;
	private int height;
	private int frameSize;
	private float adjust;
	private boolean isFramed;
	private PendantManager pendantManager;
	private MagicwandManager magicwandManager;
	private Bitmap bgPhoto;
	
	private Handler parentHandler;
	private LayerManager layerManager;
	private HistoryManager historyManager;
	private Object lastObject;
	
	//构造函数
	public PhotoEditionView2(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	public PhotoEditionView2(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	public PhotoEditionView2(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}
	//初始化参数
	public void initiate(int width, int height, int framesize, float adjust, 
			HashMap<String, String> idpendant, HashMap<String, ArrayList<String>> idgadgets) {
		//图片显示宽高
		this.width = width;
		this.height = height;
		//相框显示大小
		frameSize = framesize;
		//像素比例调整
		this.adjust = adjust;
		
		initLayerManager();
		initPendantManager(idpendant);
		initMagicwandManager(idgadgets);
		initHistoryManager();
	}
	//载入原图
	public void loadOriginalPhoto(Bitmap photo, boolean hasframe) {
		if(photo == null)
			return;
		
		if(originalPhoto != null && !originalPhoto.isRecycled())
			originalPhoto.recycle();
		originalPhoto = photo;
		
		if(pendantManager != null) {
			if(isFramed && !hasframe) {//正方形变长方形区域
				pendantManager.resetView(width, height);
				magicwandManager.resetView(width, height);
				clearAll();
			}
			else if(!isFramed && hasframe) {//长方形变正方形区域
				pendantManager.resetView(frameSize, frameSize);
				magicwandManager.resetView(frameSize, frameSize);
				clearAll();
			}
		}
		isFramed = hasframe;
		updateBG();
	}
	
	//初始化图层管理器
	private void initLayerManager() {
		layerManager = new LayerManager();
		Drawable deleteDrawable = getContext().getResources().getDrawable(R.drawable.delete_btn);
		deleteBmp = ((BitmapDrawable)deleteDrawable).getBitmap();
		int deleteSize = 30;
		float scale = (float)deleteSize / Math.max(deleteBmp.getWidth(), deleteBmp.getHeight());
		deleteWidth = (int) (deleteBmp.getWidth() * scale * adjust);
		deleteHeight = (int) (deleteBmp.getHeight() * scale * adjust);
	}
	//初始化挂件管理器
	private void initPendantManager(HashMap<String, String> idpendant) {
		pendantManager = new PendantManager(getContext(), idpendant, adjust, width, height);
	}
	//初始化魔术棒管理器
	private void initMagicwandManager(HashMap<String, ArrayList<String>> idgadgets) {
		magicwandManager = new MagicwandManager(getContext(), idgadgets, adjust, width, height);
	}
	//初始化历史管理器
	private void initHistoryManager() {
		historyManager = new HistoryManager(layerManager, pendantManager, magicwandManager);
		lastObject = null;
	}
	
	//丢失图层焦点
	public void lostLayer() {
		if(editLid != -1) {
			if(layerType == LayerType.PENDANT) {
				pendantManager.lostFocus();
				closePendantControl();
			}
			else if(layerType == LayerType.MAGICWAND) {
				magicwandManager.lostFocus();
				closeMagicwandControl();
			}
		}
		editLid = -1;
		layerType = LayerType.NONE;
		drawMode = DrawMode.SELECTED;
		touchState = TouchState.EDIT;
		
		updateBG();
	}
	//获取图层焦点
	public void setLayer(long lid, TouchState touchstate) {
		if(lid == -1)
			return;

		LayerManager.Layer layer = layerManager.getLayer(lid);
		String type = layer.getType();
		long id = layer.getOid();
		if(type.intern() == "PENDANT") {
			if(!pendantManager.setFocus(id))
				return;			
			openPendantControl();
			layerType = LayerType.PENDANT;
		}
		else if(type.intern() == "MAGICWAND") {
			if(!magicwandManager.setFocus(id))
				return;
			openMagicwandControl();
			layerType = LayerType.MAGICWAND;
		}

		editLid = lid;
		//改变层的顺序，记录历史
		int lastlevel = layerManager.getLevel(editLid);
		int currlevel = layerManager.setLayerTop(editLid);
		historyManager.recordChangeLevel(editLid, lastlevel, currlevel);
		
		drawMode = DrawMode.SELECTED;
		touchState = touchstate;
		
		updateBG();
	}
	//删除图层
	public void deleteLayer() {
		if(editLid == -1)
			return;
		
		if(layerType == LayerType.PENDANT) {
			//pendantManager.deletePendant(editLayer.getId());
			closePendantControl();
		}
		else if(layerType == LayerType.MAGICWAND) {
			//magicwandManager.deleteMagicwand(editLayer.getId());
			closeMagicwandControl();
		}
		//移除层，记录历史
		int lastlevel = layerManager.removeLayer(editLid);
		historyManager.recordChangeLevel(editLid, lastlevel, -1);
		
		editLid = -1;
		layerType = LayerType.NONE;
		drawMode = DrawMode.SELECTED;
		touchState = TouchState.EDIT;
		
		this.postInvalidate();
	}
	//更新图层选取区域
	public void updateLayer() {
		//更新图层，记录历史
		LayerManager.Layer lastlayer = layerManager.getLayer(editLid).clone();
		Object currobject = null;
		
		if(layerType == LayerType.PENDANT) {
			PendantManager.Pendant pendant = pendantManager.getCurrPendant();
			if(pendant == null)
				return;
			
			
			ArrayList<RectF> rects = new ArrayList<RectF>();
			rects.add(pendant.getRect());
			layerManager.updateLayer(editLid, rects, pendant.getRect());
			openPendantControl();
			
			currobject = pendantManager.getCurrPendant().clone();
		}
		else if(layerType == LayerType.MAGICWAND) {
			MagicwandManager.Magicwand magicwand = magicwandManager.getCurrMagicwand();
			if(magicwand == null)
				return;
			
			layerManager.updateLayer(editLid, magicwand.getRects(), magicwand.getWholeRect());
			openMagicwandControl();
			
			currobject = magicwandManager.getCurrMagicwand().clone();
		}
		LayerManager.Layer currlayer = layerManager.getLayer(editLid).clone();
		historyManager.recordUpdate(editLid, lastlayer, currlayer, lastObject, currobject);
		Log.i("history", "update layer");
	}
	
	//记录历史旧状态
	public void recordOldState() {
		if(editLid < 0)
			return;
		
		if(layerType == LayerType.PENDANT) {
			PendantManager.Pendant pendant = pendantManager.getCurrPendant();
			lastObject = pendant.clone();
		}
		else if(layerType == LayerType.MAGICWAND) {
			MagicwandManager.Magicwand magicwand = magicwandManager.getCurrMagicwand();
			lastObject = magicwand.clone();
		}
		Log.i("history", "record old state");
	}
	//历史恢复操作
	public void undo() {
		historyManager.undo();
		lostLayer();
	}
	//历史重做操作
	public void redo() {
		historyManager.redo();
		lostLayer();
	}
	
	//新增挂件
	public void addPendant(String id) {
		long pid = pendantManager.addPendant(id);
		if(pid < 0)
			return;
		PendantManager.Pendant pendant = pendantManager.getPendant(pid);
		RectF rect = pendant.getRect();
		if(rect == null)
			return;

		if(editLid != -1 && layerType == LayerType.MAGICWAND)
			lostLayer();
		
		ArrayList<RectF> rects = new ArrayList<RectF>();
		rects.add(rect);
		long lid = layerManager.addLayer("PENDANT", pid, rects, rect);

		setLayer(lid, TouchState.EDIT);
	}
	//新增当前挂件
	public void addCurrPendant() {
		long pid = pendantManager.addCurrPendant();
		if(pid < 0)
			return;
		PendantManager.Pendant pendant = pendantManager.getPendant(pid);
		RectF rect = pendant.getRect();
		if(rect == null)
			return;

		ArrayList<RectF> rects = new ArrayList<RectF>();
		rects.add(rect);
		long lid = layerManager.addLayer("PENDANT", pid, rects, rect);

		setLayer(lid, TouchState.EDIT);
	}
	//缩放挂件，按钮用
	public void scalePendant(int scale) {
		pendantManager.scalePendant((float)scale);
		this.postInvalidate();
	}
	//旋转挂件，按钮用
	public void rotatePendant(int scale) {
		pendantManager.rotatePendant((float)scale);
		this.postInvalidate();
	}
	//获取挂件缩放倍数
	public int getPendantScale() {
		return (int) pendantManager.getScale();
	}
	//获取挂件旋转角度
	public int getPendantRotate() {
		return (int) pendantManager.getRotate();
	}
	//获取当前挂件的图片
	public Bitmap getCurrPendantBmp() {
		return pendantManager.getCurrPendantBmp();
	}
	
	//新增魔术棒
	public void addMagicwand(String id) {
		long mid = magicwandManager.addMagicwand(id);
		if(mid < 0)
			return;

		if(editLid != -1 && layerType == LayerType.PENDANT)
			lostLayer();
		
		MagicwandManager.Magicwand magicwand = magicwandManager.getCurrMagicwand();
		long lid = layerManager.addLayer("MAGICWAND", mid, magicwand.getRects(), magicwand.getWholeRect());

		setLayer(lid, TouchState.MAGICWAND_DRAW);
	}
	//新增当前魔术棒
	public void addCurrMagicwand() {
		long mid = magicwandManager.addCurrMagicwand();
		if(mid < 0)
			return;
		
		MagicwandManager.Magicwand magicwand = magicwandManager.getCurrMagicwand();
		long lid = layerManager.addLayer("MAGICWAND", mid, magicwand.getRects(), magicwand.getWholeRect());

		setLayer(lid, TouchState.MAGICWAND_DRAW);
	}
	//结束魔术棒
	public void endMagicwand() {
		magicwandManager.endMagicwand();
		drawMode = DrawMode.SELECTED;
		touchState = TouchState.EDIT;
		
		this.postInvalidate();
	}
	//设置魔术棒缩放比例
	public void setMagicwandScale(int scale) {
		magicwandManager.setScale((float)scale);
	}
	//获取当前魔术棒缩放比例
	public int getMagicwandScale() {
		return (int) magicwandManager.getScale();
	}
	//获取当前魔术棒的图片
	public Bitmap getCurrMagicwandBmp() {
		return magicwandManager.getCurrMagicwandBmp();
	}
	
	//设置父亲同步句柄
	public void setParentHandler(Handler handler) {
		parentHandler = handler;
	}

	public static enum DrawMode {SELECTED, CHANGED, DELETED}
	private DrawMode drawMode;
	//重绘函数
	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		if(editLid != -1) {
			if(layerType == LayerType.PENDANT) {
				pendantManager.drawCurrPendant(canvas, drawMode);
			}
			else if(layerType == LayerType.MAGICWAND) {
				magicwandManager.drawCurrMagicwand(canvas, drawMode);
			}
			
			//画垃圾箱
			if(drawMode != PhotoEditionView2.DrawMode.SELECTED) {
				final Paint paint = new Paint();
				paint.setAlpha(200);
				if(isFramed)
					canvas.drawBitmap(deleteBmp, 
							new Rect(0, 0, deleteBmp.getWidth(), deleteBmp.getHeight()), 
							new Rect(frameSize - deleteWidth, frameSize - deleteHeight, frameSize, frameSize), 
							paint);
				else
					canvas.drawBitmap(deleteBmp, 
							new Rect(0, 0, deleteBmp.getWidth(), deleteBmp.getHeight()), 
							new Rect(width - deleteWidth, height - deleteHeight, width, height), 
							paint);
			}
		}
	}
	//更新背景图片
	public void updateBG() {
		Bitmap mphoto;
		Matrix matrix = new Matrix();
		if(isFramed) {
			mphoto = Bitmap.createBitmap(frameSize, frameSize, Config.ARGB_8888);			
			matrix.postScale((float)frameSize / originalPhoto.getWidth(), 
					(float)frameSize / originalPhoto.getHeight());
		}
		else {
			mphoto = Bitmap.createBitmap(width, height, Config.ARGB_8888);
			matrix.postScale((float)width / originalPhoto.getWidth(), 
					(float)height / originalPhoto.getHeight());
		}		
		Canvas canvas = new Canvas(mphoto);
		canvas.drawBitmap(originalPhoto, matrix, null);

		//画装饰
		if(layerManager != null) {
			int sum = layerManager.getSum();
			if(editLid != -1)
				sum--;
			for(int i = 0; i < sum; i++) {
				long lid = layerManager.getLid(i);
				LayerManager.Layer layer = layerManager.getLayer(lid);
				if(layer.getType().intern() == "PENDANT") {
					pendantManager.drawPendant(mphoto, layer.getOid());
				}
				else if(layer.getType().intern() == "MAGICWAND") {
					magicwandManager.drawMagicwand(mphoto, layer.getOid());
				}
			}//for i
		}
		if(bgPhoto != null && !bgPhoto.isRecycled())
			bgPhoto.recycle();
		bgPhoto = mphoto;
		
		this.setImageBitmap(bgPhoto);
	}
	//获取编辑后的图片
	public Bitmap getPhoto(int size) {
		Bitmap mphoto;
		Matrix matrix = new Matrix();
		if(isFramed) {
			mphoto = Bitmap.createBitmap(size, size, Config.ARGB_8888);			
			matrix.postScale((float)size / originalPhoto.getWidth(), 
					(float)size / originalPhoto.getHeight());
		}
		else {
			float scale = (float)size / Math.max(width, height);
			mphoto = Bitmap.createBitmap((int) (width * scale), (int) (height * scale), Config.ARGB_8888);
			matrix.postScale(width * scale / originalPhoto.getWidth(), 
					height * scale / originalPhoto.getHeight());
		}		
		Canvas canvas = new Canvas(mphoto);
		canvas.drawBitmap(originalPhoto, matrix, null);

		//画装饰
		if(layerManager != null) {
			int sum = layerManager.getSum();
			for(int i = 0; i < sum; i++) {
				long lid = layerManager.getLid(i);
				LayerManager.Layer layer = layerManager.getLayer(lid);
				if(layer.getType().intern() == "PENDANT") {
					pendantManager.drawPendant(mphoto, layer.getOid());
				}
				else if(layer.getType().intern() == "MAGICWAND") {
					magicwandManager.drawMagicwand(mphoto, layer.getOid());
				}
			}//for i
		}
		
		return mphoto;
	}

	private static enum TouchState {MAGICWAND_DRAW, EDIT}
	private TouchState touchState = TouchState.EDIT;
	//重写触笔函数
	@Override
    public boolean onTouchEvent(MotionEvent event) {
		switch(touchState) {
		case MAGICWAND_DRAW:
			magicwandDraw(event);
			break;
		case EDIT:
			Integer version = new Integer(android.os.Build.VERSION.SDK);
			if(version.intValue() >= 7)
				edit21(event);
			else
				edit16(event);
			break;
		}
		
		return true;
    }
	//画魔术棒状态触笔响应函数
	private void magicwandDraw(MotionEvent event) {
		float x = event.getX();
        float y = event.getY();
        boolean isrecorded = false;
        switch(event.getAction()) {
        case MotionEvent.ACTION_DOWN: 
        	magicwandManager.continueMagicwand();
        	isrecorded = magicwandManager.recordPoint(new PointF(x, y));
        	break;
        	
        case MotionEvent.ACTION_MOVE:
        	isrecorded = magicwandManager.recordPoint(new PointF(x, y));
            break;
            
        case MotionEvent.ACTION_UP:
        	MagicwandManager.Magicwand magicwand = magicwandManager.getCurrMagicwand();
        	layerManager.updateLayer(editLid, magicwand.getRects(), magicwand.getWholeRect());
        	break;
        }

        if(isrecorded) {
        	this.postInvalidate();
        }

	}

	private static enum Mode {NONE, SINGLE, MULTI}
	private Mode mode = Mode.NONE; 
	private long editLid = -1;
	private static enum LayerType {NONE, PENDANT, MAGICWAND}
	private LayerType layerType = LayerType.NONE;
	private float dX;
	private float dY;
	private float distance;
	private float angle;
	private Bitmap deleteBmp;
	private int deleteWidth;
	private int deleteHeight;
	//2.1以上版本，编辑状态触笔响应函数
	private void edit21(MotionEvent event) {
		float x = event.getX();
        float y = event.getY();
        float x1 = event.getX(0);
        float y1 = event.getY(0);
        float x2 = event.getX(1);
        float y2 = event.getY(1);

        switch(event.getAction() & MotionEvent.ACTION_MASK) {
        case MotionEvent.ACTION_DOWN:
        	if(mode == Mode.NONE) {
	        	long lid = layerManager.getTouchedLid(x, y);
	        	if(lid != -1 || editLid != -1) {//有图层处于选中状态或被选中
	        		mode = Mode.SINGLE;

	        		if(lid != -1 && editLid != lid) {//获取另外一个图层
	        			lostLayer();
	        			setLayer(lid, TouchState.EDIT);
	        		}

	        		dX = x;
	        		dY = y;
	        		recordOldState();
	        	}
        	}
        	break;
        case MotionEvent.ACTION_MOVE:
        	switch(mode) {
        	case SINGLE:
        		if(layerType == LayerType.PENDANT) {
        			PendantManager.Pendant pendant = pendantManager.getCurrPendant();
        			pendant.translate(x - dX, y - dY);
        			
        			if(drawMode == DrawMode.SELECTED)
        				closePendantControl();
        		}
        		else if(layerType == LayerType.MAGICWAND) {
        			MagicwandManager.Magicwand magicwand = magicwandManager.getCurrMagicwand();
        			magicwand.translate(x - dX, y - dY);

        			if(drawMode == DrawMode.SELECTED)
        				closeMagicwandControl();
        		}
        		//检查删除位置
        		if((isFramed && x >= frameSize - deleteWidth && x <= frameSize && 
						y >= frameSize - deleteHeight && y <= frameSize) || 
					(!isFramed && x >= width - deleteWidth && x <= width && 
						y >= height - deleteHeight && y <= height))
        			drawMode = DrawMode.DELETED;
	        	else
	        		drawMode = DrawMode.CHANGED;
        		
        		dX = x;
    			dY = y;

        		this.postInvalidate();
        		break;
        	case MULTI:
        		float d = (float) Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));			
    			float ua = (float) (Math.atan((y2 - y1) / (x2 - x1)) * 180 / Math.PI);
    			float a = ua - angle;
    			if(a > 90)
    				a -= 180;
    			else if(a < -90)
    				a += 180;

    			if(layerType == LayerType.PENDANT) {
    				PendantManager.Pendant pendant = pendantManager.getCurrPendant();
    				pendant.scale(d / distance);
    				pendant.rotate(a);

        			if(drawMode == DrawMode.SELECTED)
        				closePendantControl();
        		}
    			
        		distance = d;       		       		
        		angle = ua;
        		drawMode = DrawMode.CHANGED;
        		this.postInvalidate();
        		break;
        	}
        	break;
        case MotionEvent.ACTION_UP:
        	if(mode == Mode.SINGLE) {
        		if(drawMode != DrawMode.SELECTED)
            		updateLayer();
        		if((isFramed && drawMode != DrawMode.SELECTED && 
	        			x >= frameSize - deleteWidth && x <= frameSize && 
						y >= frameSize - deleteHeight && y <= frameSize) || 
					(!isFramed && drawMode != DrawMode.SELECTED && 
	        			x >= width - deleteWidth && x <= width && 
						y >= height - deleteHeight && y <= height)) {
        			deleteLayer();
        		}
	        	
	        	mode = Mode.NONE;
	        	drawMode = DrawMode.SELECTED;
	        	this.postInvalidate();
        	}
        	break;
        case MotionEvent.ACTION_POINTER_DOWN:
        	if(mode == Mode.SINGLE) {
        		distance = (float) Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
	    		angle = (float) (Math.atan((y2 - y1) / (x2 - x1)) * 180 / Math.PI);
	    		mode = Mode.MULTI;
        	}
        	break;
        case MotionEvent.ACTION_POINTER_UP:
        	if(mode == Mode.MULTI) {
        		if(drawMode != DrawMode.SELECTED)
        			updateLayer();
        		mode = Mode.NONE;
        		drawMode = DrawMode.SELECTED;
        		this.postInvalidate();
        	}
        	break;
        }
	}
	//1.6版本，编辑状态触笔响应函数
	private void edit16(MotionEvent event) {
		float x = event.getX();
        float y = event.getY();

        switch(event.getAction()) {
        case MotionEvent.ACTION_DOWN:
        	if(mode == Mode.NONE) {
	        	long lid = layerManager.getTouchedLid(x, y);
	        	if(lid != -1 || editLid != -1) {//有图层处于选中状态或被选中
	        		mode = Mode.SINGLE;

	        		if(lid != -1 && editLid != lid) {//获取另外一个图层
	        			lostLayer();
	        			setLayer(lid, TouchState.EDIT);
	        		}

	        		dX = x;
	        		dY = y;
	        	}
        	}
        	break;
        case MotionEvent.ACTION_MOVE:
        	if(mode == Mode.SINGLE) {
        		if(layerType == LayerType.PENDANT) {
        			PendantManager.Pendant pendant = pendantManager.getCurrPendant();
        			pendant.translate(x - dX, y - dY);
        			
        			if(drawMode == DrawMode.SELECTED)
        				closePendantControl();
        		}
        		else if(layerType == LayerType.MAGICWAND) {
        			MagicwandManager.Magicwand magicwand = magicwandManager.getCurrMagicwand();
        			magicwand.translate(x - dX, y - dY);

        			if(drawMode == DrawMode.SELECTED)
        				closeMagicwandControl();
        		}
        		//检查删除位置
        		if((isFramed && x >= frameSize - deleteWidth && x <= frameSize && 
						y >= frameSize - deleteHeight && y <= frameSize) || 
					(!isFramed && x >= width - deleteWidth && x <= width && 
						y >= height - deleteHeight && y <= height))
        			drawMode = DrawMode.DELETED;
	        	else
	        		drawMode = DrawMode.CHANGED;
        		
        		dX = x;
    			dY = y;

        		this.postInvalidate();
        		break;
        	}
        	break;
        case MotionEvent.ACTION_UP:
        	if(mode == Mode.SINGLE) {
        		if((isFramed && drawMode != DrawMode.SELECTED && 
	        			x >= frameSize - deleteWidth && x <= frameSize && 
						y >= frameSize - deleteHeight && y <= frameSize) || 
					(!isFramed && drawMode != DrawMode.SELECTED && 
	        			x >= width - deleteWidth && x <= width && 
						y >= height - deleteHeight && y <= height)) {
        			deleteLayer();
        		}
        		else
        			updateLayer();
	        	
	        	mode = Mode.NONE;
	        	drawMode = DrawMode.SELECTED;
	        	this.postInvalidate();
        	}
        	break;
        }
	}
	
	//开启挂件控制条
	private void openPendantControl() {
		if(parentHandler == null)
			return;
		
		Message msg = new Message();
		msg.what = PhotoEdition.OPEN_PENDANT_CONTROL;
		parentHandler.sendMessage(msg);
	}
	//关闭挂件控制条
	private void closePendantControl() {
		if(parentHandler == null)
			return;
		
		Message msg = new Message();
		msg.what = PhotoEdition.CLOSE_PENDANT_CONTROL;
		parentHandler.sendMessage(msg);
	}
	//开启魔术棒控制条
	private void openMagicwandControl() {
		if(parentHandler == null)
			return;
		
		Message msg = new Message();
		msg.what = PhotoEdition.OPEN_MAGICWAND_CONTROL;
		parentHandler.sendMessage(msg);
	}
	//关闭魔术棒控制条
	private void closeMagicwandControl() {
		if(parentHandler == null)
			return;
		
		Message msg = new Message();
		msg.what = PhotoEdition.CLOSE_MAGICWAND_CONTROL;
		parentHandler.sendMessage(msg);
	}

	//清空屏幕
	public void clearAll() {
		lostLayer();
		layerManager.clear();
		pendantManager.clear();
		magicwandManager.clear();
		historyManager.clear();
		
		editLid = -1;
		layerType = LayerType.NONE;
		drawMode = DrawMode.SELECTED;
		touchState = TouchState.EDIT;
		
		updateBG();
	}
	//回收位图内存
	public void recycleAll() {
		if(originalPhoto != null && !originalPhoto.isRecycled())
			originalPhoto.recycle();
		originalPhoto = null;
		if(bgPhoto != null && !bgPhoto.isRecycled())
			bgPhoto.recycle();
		bgPhoto = null;
		pendantManager.recycleAll();
	}
	
}
