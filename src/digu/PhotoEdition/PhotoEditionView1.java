package digu.PhotoEdition;

import java.util.HashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Bitmap.Config;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

public class PhotoEditionView1 extends ImageView {
	
	private Bitmap originalPhoto;
	private int width;
	private int height;
	private int frameSize;
	private float framedWidth;
	private float framedHeight;
	private float moveX;
	private float moveY;
	private boolean isFramed;
	private FilterManager filterManager;
	private FrameManager frameManager;
	private Bitmap currPhoto;
	
	//拖曳照片用变量	
	private float dX;
	private float dY;
	private boolean isTouched = false;

	//构造函数
	public PhotoEditionView1(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	public PhotoEditionView1(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	public PhotoEditionView1(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}
	//初始化参数
	public void initiate(int width, int height, int framesize, 
			HashMap<String, FilterParser.FilterInfo> idfilterinfo, HashMap<String, String> idframe) {
		//图片显示宽高
		this.width = width;
		this.height = height;
		//相框显示大小
		frameSize = framesize;
		
		initFilterManager(idfilterinfo);
		initFrameManager(idframe);
	}
	//载入原图
	public void loadOriginalPhoto(Bitmap photo) {
		if(photo == null)
			return;
		
		if(originalPhoto != null && !originalPhoto.isRecycled())
			originalPhoto.recycle();
		originalPhoto = photo;
		
		float scale = (float)frameSize / Math.min(photo.getWidth(), photo.getHeight());
		framedWidth = photo.getWidth() * scale;
		framedHeight = photo.getHeight() * scale;
		moveX = (framedWidth - frameSize) / 2;
		moveY = (framedHeight - frameSize) / 2;
		isFramed = false;
		update();
	}
	//初始化滤镜
	private void initFilterManager(HashMap<String, FilterParser.FilterInfo> idfilterinfo) {
		filterManager = new FilterManager(getContext(), idfilterinfo);
	}
	//初始化相框
	private void initFrameManager(HashMap<String, String> idframe) {
		frameManager = new FrameManager(getContext(), idframe);
	}
	
	//设置滤镜
	public void setFilter(String id) {
		filterManager.setFilter(id);
	}
	//设置相框
	public void setFrame(String id) {
		frameManager.setFrame(id);
		isFramed = (!frameManager.isFrameEmpty());
	}
	
	//重绘函数
	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if(isFramed && isTouched) {
			Matrix matrix = new Matrix();
			float scale = (float)frameSize / Math.min(originalPhoto.getWidth(), originalPhoto.getHeight());
			matrix.postScale(scale, scale);
			matrix.postTranslate(-moveX, -moveY);
			canvas.drawBitmap(originalPhoto, matrix, null);
			
			frameManager.drawFrameBmp(canvas, frameSize, frameSize);
		}
	}
	//更新图片
	public void update() {
		Bitmap mphoto;
		Matrix matrix = new Matrix();
		if(isFramed) {
			mphoto = Bitmap.createBitmap(frameSize, frameSize, Config.ARGB_8888);
			matrix.postScale((float)framedWidth / originalPhoto.getWidth(), 
					(float)framedHeight / originalPhoto.getHeight());
			matrix.postTranslate(-moveX, -moveY);
		}
		else {
			mphoto = Bitmap.createBitmap(width, height, Config.ARGB_8888);						
			matrix.postScale((float)width / originalPhoto.getWidth(), 
					(float)height / originalPhoto.getHeight());			
		}
		Canvas canvas = new Canvas(mphoto);
		canvas.drawBitmap(originalPhoto, matrix, null);
		
		//画滤镜
		if(filterManager != null)
			mphoto = filterManager.getFilterBmp(mphoto);
		
		//画相框
		if(frameManager != null)
			frameManager.drawFrameBmp(mphoto);
		
		if(currPhoto != null && !currPhoto.isRecycled())
			currPhoto.recycle();
		currPhoto = mphoto;
		
		this.setImageBitmap(currPhoto);
	}
	
	//获取编辑后的相片
	public Bitmap getPhoto(int size) {
		if(originalPhoto == null)
			return null;
			
		Bitmap mphoto;
		Matrix matrix = new Matrix();
		if(isFramed) {
			mphoto = Bitmap.createBitmap(size, size, Config.ARGB_8888);
			float scale = (float)size / Math.min(originalPhoto.getWidth(), originalPhoto.getHeight());
			matrix.postScale(scale, scale);
			matrix.postTranslate(-moveX * size / frameSize, -moveY * size / frameSize);
		}
		else {
			float scale = (float)size / Math.max(originalPhoto.getWidth(), originalPhoto.getHeight());
			mphoto = Bitmap.createBitmap((int) (originalPhoto.getWidth() * scale), 
					(int) (originalPhoto.getHeight() * scale), Config.ARGB_8888);						
			matrix.postScale(scale, scale);				
		}
		Canvas canvas = new Canvas(mphoto);
		canvas.drawBitmap(originalPhoto, matrix, null);
		
		//画滤镜
		if(filterManager != null)
			mphoto = filterManager.getFilterBmp(mphoto);
		
		//画相框
		if(frameManager != null)
			frameManager.drawFrameBmp(mphoto);
		
		return mphoto;
	}
	//判断是否有相框
	public boolean hasFrame() {
		return isFramed;
	}
	
	//重写触笔函数
	@Override
    public boolean onTouchEvent(MotionEvent event) {
		if(isFramed) {
			float x = event.getX();
	        float y = event.getY();

	        switch(event.getAction()) {
	        case MotionEvent.ACTION_DOWN:
	        	isTouched = true;
	        	break;
	        	
	        case MotionEvent.ACTION_MOVE:
	        	moveX -= x - dX;
	        	if(moveX < 0)
	        		moveX = 0;
	        	else if (moveX > framedWidth - frameSize)
	        		moveX = framedWidth - frameSize;

	        	moveY -= y - dY;
	        	if(moveY < 0)
	        		moveY = 0;
	        	else if(moveY > framedHeight - frameSize)
	        		moveY = framedHeight - frameSize;
	        	
	        	this.postInvalidate();
	            break;
	            
	        case MotionEvent.ACTION_UP:
	        	isTouched = false;
	        	update();
	        	break;
	        	
	        }
	        dX = x;
            dY = y;
		}
		return true;
    }

	//回收位图内存
	public void recycleAll() {
		if(originalPhoto != null && !originalPhoto.isRecycled())
			originalPhoto.recycle();
		originalPhoto = null;
		if(currPhoto != null && !currPhoto.isRecycled())
			currPhoto.recycle();
		currPhoto = null;
		filterManager.recycleAll();
		frameManager.recycleAll();
	}
}
