package digu.PhotoEdition;

import java.util.HashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.Bitmap.Config;

@SuppressWarnings("serial")
public class FilterManager {
	//模式名字与模式的映射
	private static final HashMap<String, PorterDuff.Mode> modeMap = 
		new HashMap<String, PorterDuff.Mode>() {{
		put("darken", PorterDuff.Mode.DARKEN);
		put("lighten", PorterDuff.Mode.LIGHTEN);
		put("multiply", PorterDuff.Mode.MULTIPLY);
		put("screen", PorterDuff.Mode.SCREEN);
		}};
	//滤镜类
	public class Filter {
		private ColorMatrixColorFilter colorFilter;
		private int[] holoColor;
		private float[] holoPos;
		private PorterDuffXfermode holoMode;
		private String overlayPath;		
		private PorterDuffXfermode overlayMode;
		
		public Filter(FilterParser.FilterInfo filterinfo) {
			
			if(filterinfo.filterMatrix != null) {
				ColorMatrix cm = new ColorMatrix(filterinfo.filterMatrix);
				colorFilter = new ColorMatrixColorFilter(cm);
			}
			
			if(filterinfo.holoColorArray != null) {
				holoColor = filterinfo.holoColorArray.clone();
			}
			
			if(filterinfo.holoPosArray != null) {
				holoPos = filterinfo.holoPosArray.clone();
			}
			
			if(filterinfo.holoModeName != null) {
				holoMode = new PorterDuffXfermode(modeMap.get(filterinfo.holoModeName));
			}
			
			if(filterinfo.overlayPath != null) {
				overlayPath = filterinfo.overlayPath;
			}
			
			if(filterinfo.overlayModeName != null) {
				overlayMode = new PorterDuffXfermode(modeMap.get(filterinfo.overlayModeName));
			}
		}
		
		public ColorMatrixColorFilter getColorFilter() {
			return colorFilter;
		}
		public int[] getHoloColor() {
			return holoColor;
		}
		public float[] getHoloPos() {
			return holoPos;
		}
		public PorterDuffXfermode getHoloMode() {
			return holoMode;
		}
		public String getOverlayPath() {
			return overlayPath;
		}
		public PorterDuffXfermode getOverlayMode() {
			return overlayMode;
		}
	}
	
	private Context context;
	private HashMap<String, Filter> filterMap;
	private HashMap<String, Filter> exFilterMap;
	private Filter currFilter;
	private Bitmap currOverlay;
	private BitmapManager bitmapManager;
		
	//构造函数，从滤镜信息构造滤镜结构
	public FilterManager(Context context, HashMap<String, FilterParser.FilterInfo> idfilterinfo) {
		this.context = context;
		filterMap = new HashMap<String, Filter>();
		for(String id : idfilterinfo.keySet()) {
			if(idfilterinfo.get(id) != null) {
				Filter filter = new Filter(idfilterinfo.get(id));
				filterMap.put(id, filter);
			}
		}//for id
		exFilterMap = new HashMap<String, Filter>();
		currFilter = null;
		currOverlay = null;
		bitmapManager = new BitmapManager();	
	}
	//设置滤镜
	public void setFilter(String id) {
		if(filterMap.containsKey(id)) {
			currFilter = filterMap.get(id);
			if(currFilter != null && currFilter.overlayPath != null)
				currOverlay = bitmapManager.getBitmapFromAssets(context, currFilter.overlayPath);
			else
				currOverlay = null;
		}
		else if(exFilterMap.containsKey(id)){
			currFilter = exFilterMap.get(id);
			if(currFilter != null && currFilter.overlayPath != null)
				currOverlay = bitmapManager.getBitmap(currFilter.overlayPath);
			else
				currOverlay = null;
		}
		else {
			currFilter = null;
			currOverlay = null;
		}
	}
	//获取加载滤镜后的图片
	public Bitmap getFilterBmp(Bitmap bmp) {
		if(bmp == null)
			return null;
		
		int width = bmp.getWidth();
		int height = bmp.getHeight();

		Canvas canvas = new Canvas(bmp); 
        Paint paint = new Paint();

		if(currFilter != null) {
			//颜色过滤器
			if(currFilter.getColorFilter() != null) {
		        paint.setColorFilter(currFilter.getColorFilter());
		        Bitmap bmp1 = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		        Canvas canvas1 = new Canvas(bmp1);
		        canvas1.drawBitmap(bmp, 0, 0, paint);
		        bmp.recycle();
		        bmp = bmp1;
		        canvas = canvas1;
			}
			//光晕
			if(currFilter.getHoloColor() != null) {
				paint.reset();
				if(currFilter.getHoloMode() != null) {
					paint.setXfermode(currFilter.getHoloMode());
				}
				RadialGradient rg = new RadialGradient(
						(float)width / 2, (float)height / 2, (float)height, 
						currFilter.getHoloColor(), currFilter.getHoloPos(), Shader.TileMode.CLAMP);
				paint.setShader(rg);
				canvas.drawRect(0.0f, 0.0f, width, height, paint);
			}
			//覆盖层
			if(currOverlay != null) {
		       	paint.reset();
		        if(currFilter.getOverlayMode() != null) {
		        	paint.setXfermode(currFilter.getOverlayMode());
		        }
		        Matrix matrix = new Matrix();
		        matrix.postScale((float)width / currOverlay.getWidth(), 
		        		(float)height / currOverlay.getHeight());
		        canvas.drawBitmap(currOverlay, matrix, paint);
			}
		}
		
		return bmp;

	}
	
	//回收位图内存
	public void recycleAll() {
		currOverlay = null;
		bitmapManager.recycleAll();	
	}

}
