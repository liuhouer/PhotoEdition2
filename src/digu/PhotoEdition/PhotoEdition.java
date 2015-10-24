package digu.PhotoEdition;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.ViewFlipper;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class PhotoEdition extends Activity {
    /** Called when the activity is first created. */	

	private int saveSize;
	private float adjust;
	private int viewWidth;
	private int viewHeight;
	private int frameSize;
	private Bitmap originalPhoto;
	
	private ViewFlipper viewflipper;
	
	private Animation pushupinanim;
	private Animation pushdownoutanim;
	private Animation popinanim;
	private Animation popoutanim;
	private Animation pushleftinanim;
	private Animation pushleftoutanim;
	private Animation pushrightinanim;
	private Animation pushrightoutanim;
	
	//第一视图元素
	private static enum Surface1 {NONE, FILTER, FRAME, HELP1}
	private Surface1 surface1;
	
	private int icon4Size;
	private PhotoEditionView1 pev1;
	private GridView  filtergv;
	private GridView  framegv;
	private ImageView help1iv;
	
	private ImageButton filterbtn;
	private ImageButton framebtn;
	private ImageButton help1btn;
	private Button back1btn;
	private Button next1btn;
	
	private IconAdapter filterAdapter;
	private IconAdapter frameAdapter;
	
	//第二视图元素
	private static enum Surface2 {NONE, DECORATE, HELP2}
	private Surface2 surface2;
	
	private int icon5Size;
	private PhotoEditionView2 pev2;
	private LinearLayout decoratell;
	private GridView  pendantgv;
	private GridView  magicwandgv;
	private LinearLayout pendantbar;
	private LinearLayout magicwandbar;
	private ImageView help2iv;
	
	private ImageButton decoratebtn;
	private Button pendantbtn;
	private Button magicwandbtn;
	private ImageButton undobtn;
	private ImageButton clearbtn;
	private ImageButton redobtn;
	private ImageButton help2btn;
	private Button back2btn;
	private Button next2btn;
	
	private IconAdapter pendantAdapter;
	private IconAdapter magicwandAdapter;
	
	private HashMap<Button, View> tabMap;
	//挂件控制条
	private static enum PendantControl{SCALE, ROTATE}
	private PendantControl pendantControl;
	private boolean pendantControlOpen;
	private ImageView pendantiv;
	private ImageView pendantdispiv1;
	private SeekBar pendantseekbar;
	private ImageView pendantdispiv2;
	private ImageButton pendantswitchbtn;
	//魔术棒控制条
	private static enum MagicwandControl{START, STOP}
	private MagicwandControl magicwandControl;
	private boolean magicwandControlOpen;
	private ImageView magicwandiv;
	private SeekBar magicwandseekbar;
	private ImageButton magicwandswitchbtn;
	private ImageButton magicwanddeletebtn;
			
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
        		WindowManager.LayoutParams.FLAG_FULLSCREEN);//全屏
        setContentView(R.layout.photo_edition);

        saveSize = 640;
        DisplayMetrics metrics = new DisplayMetrics();
    	getWindowManager().getDefaultDisplay().getMetrics(metrics);
    	adjust = (float)metrics.widthPixels / 320;
    	initOriginalPhotoTest("demo_photo_w.jpg", metrics);

        pushupinanim = AnimationUtils.loadAnimation(this, R.anim.push_up_in);
    	pushdownoutanim = AnimationUtils.loadAnimation(this, R.anim.push_down_out);
    	popinanim = AnimationUtils.loadAnimation(this, R.anim.pop_in);
    	popoutanim = AnimationUtils.loadAnimation(this, R.anim.pop_out);
    	pushleftinanim = AnimationUtils.loadAnimation(this, R.anim.push_left_in);
    	pushleftoutanim = AnimationUtils.loadAnimation(this, R.anim.push_left_out);
    	pushrightinanim = AnimationUtils.loadAnimation(this, R.anim.push_right_in);
    	pushrightoutanim = AnimationUtils.loadAnimation(this, R.anim.push_right_out);
        
        viewflipper = (ViewFlipper) this.findViewById(R.id.viewflipper);
        
        initiateView1();
        initiateView2();
        
    }
    
    //初始化原图（绝对路径输入），确定显示大小和显示宽高
    private void initOriginalPhoto(String photopath, DisplayMetrics metrics) {
    	Bitmap photo = BitmapFactory.decodeFile(photopath);
		
		if(photo == null) {
			viewWidth = 0;
			viewHeight = 0;
			return;
		}
		
		if(originalPhoto != null && !originalPhoto.isRecycled())
			originalPhoto.recycle();
		originalPhoto = photo;
		
		float scale = Math.min((float)metrics.widthPixels / originalPhoto.getWidth(), 
				(float)(metrics.heightPixels - 100 * adjust) / originalPhoto.getHeight());		
		viewWidth = (int) (originalPhoto.getWidth() * scale);
		viewHeight = (int) (originalPhoto.getHeight() * scale);
		frameSize = (int) Math.min(metrics.widthPixels, metrics.heightPixels - 100 * adjust);
		Log.i("view",viewWidth + "," + viewHeight);
    }
    
    //初始化原图（资源路径输入），确定显示大小和显示宽高
    private void initOriginalPhotoTest(String photopath, DisplayMetrics metrics) {
    	Bitmap photo = null;
		try {
			InputStream is = this.getAssets().open(photopath);
			byte[] buffer = new byte[is.available()];
			while (is.read(buffer) != -1);	
			photo = (Bitmap)BitmapFactory.decodeByteArray(buffer, 0, buffer.length);
			is.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(photo == null) {
			viewWidth = 0;
			viewHeight = 0;
			return;
		}
		
		if(originalPhoto != null && !originalPhoto.isRecycled())
			originalPhoto.recycle();
		originalPhoto = photo;
		
		float scale = Math.min((float)metrics.widthPixels / originalPhoto.getWidth(), 
				(float)(metrics.heightPixels - 100 * adjust) / originalPhoto.getHeight());		
		viewWidth = (int) (originalPhoto.getWidth() * scale);
		viewHeight = (int) (originalPhoto.getHeight() * scale);
		frameSize = (int) Math.min(metrics.widthPixels, metrics.heightPixels - 100 * adjust);
		Log.i("view",viewWidth + "," + viewHeight);
    }
    //初始化第一视图
    private void initiateView1() {
    	//图标大小（已自适应）
        icon4Size = (int)(58 * adjust);
        
        FilterParser filterparser = new FilterParser(this, "filter");
        FrameParser frameparser = new FrameParser(this, "frame");

        pev1 = (PhotoEditionView1) this.findViewById(R.id.pev1);
        pev1.initiate(viewWidth, viewHeight, frameSize, 
        		filterparser.getIdFilterInfo(), frameparser.getIdFrame());
        pev1.loadOriginalPhoto(originalPhoto);
        
        filterAdapter = new IconAdapter(this, filterparser.getIdIcon(), filterparser.getIdName(), icon4Size);
        frameAdapter = new IconAdapter(this, frameparser.getIdIcon(), frameparser.getIdName(), icon4Size);
        
        filtergv = (GridView) this.findViewById(R.id.filtergv);
        filtergv.setAdapter(filterAdapter);
        filtergv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position,
					long id) {
				// TODO Auto-generated method stub
				filterAdapter.setSelected(position);
				pev1.setFilter(filterAdapter.getIconId(position));
				pev1.update();
				setSurface1(Surface1.NONE);
			}});
        
        framegv = (GridView) this.findViewById(R.id.framegv);
        framegv.setAdapter(frameAdapter);
        framegv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position,
					long id) {
				// TODO Auto-generated method stub
				frameAdapter.setSelected(position);
				pev1.setFrame(frameAdapter.getIconId(position));
				pev1.update();
				setSurface1(Surface1.NONE);
			}});
        
        filterbtn = (ImageButton) this.findViewById(R.id.filterbtn);
        filterbtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(surface1 == Surface1.FILTER)
					setSurface1(Surface1.NONE);
				else
					setSurface1(Surface1.FILTER);
			}});
        
        framebtn = (ImageButton) this.findViewById(R.id.framebtn);
        framebtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(surface1 == Surface1.FRAME)
					setSurface1(Surface1.NONE);
				else
					setSurface1(Surface1.FRAME);
			}});
        
        help1iv = (ImageView) this.findViewById(R.id.help1iv);
        
        help1btn = (ImageButton) this.findViewById(R.id.help1btn);
        help1btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(surface1 == Surface1.HELP1)
					setSurface1(Surface1.NONE);
				else
					setSurface1(Surface1.HELP1);
			}});
        
        back1btn = (Button) this.findViewById(R.id.back1btn);
    	back1btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
			}});
    	
        next1btn = (Button) this.findViewById(R.id.next1btn);
        next1btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//切换到第二视图
				pev2.loadOriginalPhoto(pev1.getPhoto(saveSize), pev1.hasFrame());
				viewflipper.setInAnimation(pushleftinanim);
				viewflipper.setOutAnimation(pushleftoutanim);
				viewflipper.showNext();
				setSurface1(Surface1.NONE);
			}});
                
    	filterAdapter.setSelected(0);
    	frameAdapter.setSelected(0);
    	surface1 = Surface1.NONE;
    }
    //初始化第二视图
    private void initiateView2() {
    	//图标大小（已自适应）
        icon5Size = (int)(38 * adjust);
        
    	PendantParser pendantparser = new PendantParser(this, "pendant");
    	MagicwandParser magicwandparser = new MagicwandParser(this, "magicwand");
    	
    	pev2 = (PhotoEditionView2) this.findViewById(R.id.pev2);
    	pev2.initiate(viewWidth, viewHeight, frameSize, adjust, 
    			pendantparser.getIdPendant(), magicwandparser.getIdGadget());
    	
    	pendantAdapter = new IconAdapter(this, pendantparser.getIdIcon(), icon5Size);
    	magicwandAdapter = new IconAdapter(this, magicwandparser.getIdIcon(), icon5Size);
    	
    	decoratell = (LinearLayout) this.findViewById(R.id.decoratell);
    	pendantgv = (GridView) this.findViewById(R.id.pendantgv);
    	pendantgv.setAdapter(pendantAdapter);
    	pendantgv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position,
					long id) {
				// TODO Auto-generated method stub
				pendantAdapter.setSelected(position);
				String iconid = pendantAdapter.getIconId(position);
				pev2.addPendant(iconid);
				setSurface2(Surface2.NONE);
			}});
    	pendantbar = (LinearLayout) this.findViewById(R.id.pendantbar);
    	
    	magicwandgv = (GridView) this.findViewById(R.id.magicwandgv);
    	magicwandgv.setAdapter(magicwandAdapter);
    	magicwandgv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position,
					long id) {
				// TODO Auto-generated method stub
				magicwandAdapter.setSelected(position);
				String iconid = magicwandAdapter.getIconId(position);
				pev2.addMagicwand(iconid);
				setMagicwandControl(MagicwandControl.STOP);
				setSurface2(Surface2.NONE);
			}});
    	magicwandbar = (LinearLayout) this.findViewById(R.id.magicwandbar);
    	
    	
    	decoratebtn = (ImageButton) this.findViewById(R.id.decoratebtn);
    	decoratebtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(surface2 == Surface2.DECORATE)
					setSurface2(Surface2.NONE);
				else
					setSurface2(Surface2.DECORATE);
			}});    	
    	pendantbtn = (Button) this.findViewById(R.id.pendantbtn);
    	pendantbtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				setTab(pendantbtn);
			}});
    	
    	magicwandbtn = (Button) this.findViewById(R.id.magicwandbtn);
    	magicwandbtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				setTab(magicwandbtn);
			}});
    	
    	undobtn = (ImageButton) this.findViewById(R.id.undobtn);
    	undobtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				pev2.undo();
			}});
    	clearbtn = (ImageButton) this.findViewById(R.id.clearbtn);
    	clearbtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				new AlertDialog.Builder(PhotoEdition.this)
				.setTitle("清空装饰")
				.setMessage("此操作将不可撤销，你确定清空所有装饰吗？")
				.setPositiveButton("清空", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						pev2.clearAll();
					}})
				.setNegativeButton("取消", null)
				.show();
				
			}});
    	redobtn = (ImageButton) this.findViewById(R.id.redobtn);
    	redobtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				pev2.redo();
			}});
    	
    	help2iv = (ImageView) this.findViewById(R.id.help2iv);
        
        help2btn = (ImageButton) this.findViewById(R.id.help2btn);
        help2btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				pev2.lostLayer();
				if(surface2 == Surface2.HELP2)
					setSurface2(Surface2.NONE);
				else
					setSurface2(Surface2.HELP2);
			}});
        
    	back2btn = (Button) this.findViewById(R.id.back2btn);
    	back2btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//切换回第一视图
				viewflipper.setInAnimation(pushrightinanim);
				viewflipper.setOutAnimation(pushrightoutanim);
				viewflipper.showPrevious();
				setSurface2(Surface2.NONE);
				
				pev2.lostLayer();
			}});
    	
    	next2btn = (Button) this.findViewById(R.id.next2btn);
    	next2btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String filepath = savePhoto();
				if(filepath != null)
					Toast.makeText(PhotoEdition.this, "图片保存在" + filepath, Toast.LENGTH_SHORT).show();
				else
					Toast.makeText(PhotoEdition.this, "没有装载SD卡或文件无法读写，无法保存...", Toast.LENGTH_SHORT).show();
			}});
    	
    	//挂件控制条，缩放和旋转挂件
    	pendantiv = (ImageView) this.findViewById(R.id.pendantiv);
    	pendantiv.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				pev2.addCurrPendant();
			}});
    	pendantseekbar = (SeekBar) this.findViewById(R.id.pendantseekbar);
    	pendantseekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// TODO Auto-generated method stub	
				switch(pendantControl) {
				case SCALE:
					pev2.scalePendant(progress);
					break;
					
				case ROTATE:
					pev2.rotatePendant(progress);
					break;
				}
					
			}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				pev2.recordOldState();
			}
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				pev2.updateLayer();
			}});
    	pendantdispiv1 = (ImageView) this.findViewById(R.id.pendantdispiv1);
    	pendantdispiv2 = (ImageView) this.findViewById(R.id.pendantdispiv2);
    	pendantswitchbtn = (ImageButton) this.findViewById(R.id.pendantswitchbtn);
    	pendantswitchbtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				switch(pendantControl) {	
				case SCALE:
					setPendantControl(PendantControl.ROTATE);
					break;					
				case ROTATE:
					setPendantControl(PendantControl.SCALE);
					break;					
				}
				updatePendantSeekBar();
			}});
    	//魔术棒控制条，开始、结束和删除按钮
    	magicwandiv = (ImageView) this.findViewById(R.id.magicwandiv);
    	magicwandseekbar = (SeekBar) this.findViewById(R.id.magicwandseekbar);
    	magicwandseekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// TODO Auto-generated method stub
				pev2.setMagicwandScale(progress);
			}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub				
			}
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub				
			}});
    	magicwandswitchbtn = (ImageButton) this.findViewById(R.id.magicwandswitchbtn);
    	magicwandswitchbtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				switch(magicwandControl) {
				case START:
					pev2.addCurrMagicwand();
					setMagicwandControl(MagicwandControl.STOP);
					break;
				case STOP:
					pev2.endMagicwand();
					setMagicwandControl(MagicwandControl.START);
					break;
				}
			}});
    	magicwanddeletebtn = (ImageButton) this.findViewById(R.id.magicwanddeletebtn);
    	magicwanddeletebtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				pev2.deleteLayer();
			}});
    	
    	tabMap = new HashMap<Button, View>();
    	tabMap.put(pendantbtn, pendantgv);
    	tabMap.put(magicwandbtn, magicwandgv);
    	setTab(pendantbtn);
    	
    	pendantAdapter.setSelected(-1);
    	magicwandAdapter.setSelected(-1);
    	surface2 = Surface2.NONE;
    	
    	pendantControlOpen = false;
    	setPendantControl(PendantControl.SCALE);
    	magicwandControlOpen = false;
    	setMagicwandControl(MagicwandControl.STOP);
    	pev2.setParentHandler(new PhotoEditionHandler(Looper.myLooper()));
    }
    
    //设置面板1
    private void setSurface1(Surface1 newsurface) {
    	switch(surface1) {
    	case NONE:
    		break;
    	case FILTER:
			filterbtn.setSelected(false);
			filtergv.startAnimation(pushdownoutanim);
			filtergv.setVisibility(View.GONE);
    		break;
    	case FRAME:
    		framebtn.setSelected(false);
			framegv.startAnimation(pushdownoutanim);
			framegv.setVisibility(View.GONE);
    		break;
    	case HELP1:
    		help1btn.setSelected(false);
    		help1iv.startAnimation(popoutanim);
    		help1iv.setVisibility(View.GONE);
    		break;
    	}
    	
    	switch(newsurface) {
    	case NONE:
    		break;
    	case FILTER:
    		filterbtn.setSelected(true);
			filtergv.setVisibility(View.VISIBLE);
			filtergv.startAnimation(pushupinanim);
    		break;
    	case FRAME:
    		framebtn.setSelected(true);
			framegv.setVisibility(View.VISIBLE);
			framegv.startAnimation(pushupinanim);
    		break;
    	case HELP1:
			help1btn.setSelected(true);
			help1iv.setVisibility(View.VISIBLE);
			help1iv.startAnimation(popinanim);
    		break;	
    	}
    	
    	surface1 = newsurface;
    }
    //设置面板2
    private void setSurface2(Surface2 newsurface) {
    	switch(surface2) {
    	case NONE:
    		break;
    	case DECORATE:
    		decoratebtn.setSelected(false);
    		decoratell.startAnimation(pushdownoutanim);
    		decoratell.setVisibility(View.GONE);
			break;
    	case HELP2:
    		help2btn.setSelected(false);
    		help2iv.startAnimation(popoutanim);
    		help2iv.setVisibility(View.GONE);
    		break;
    	}
    	
    	switch(newsurface) {
    	case NONE:
    		break;
    	case DECORATE:
    		decoratebtn.setSelected(true);
    		decoratell.setVisibility(View.VISIBLE);
    		decoratell.startAnimation(pushupinanim);
    		break;
    	case HELP2:
			help2btn.setSelected(true);
			help2iv.setVisibility(View.VISIBLE);
			help2iv.startAnimation(popinanim);
    		break;	
    	}
    	
    	surface2 = newsurface;
    }
    
    //设置tab
    private void setTab(Button tabbtn) {
    	for(Button btn : tabMap.keySet()) {
    		if(btn.isSelected()) {
    			btn.setSelected(false);
    			tabMap.get(btn).setVisibility(View.GONE);
    		}
    	}//for btn
    	
    	tabbtn.setSelected(true);
    	tabMap.get(tabbtn).setVisibility(View.VISIBLE);
    }
    
    //设置挂件控制条状态
    private void setPendantControl(PendantControl pendantcontrol) {
    	switch(pendantcontrol) {
    	case SCALE:
    		pendantdispiv1.setImageDrawable(getResources().getDrawable(R.drawable.small_img));
    		pendantdispiv2.setImageDrawable(getResources().getDrawable(R.drawable.big_img));
    		pendantswitchbtn.setImageDrawable(getResources().getDrawable(R.drawable.scalebtn));
    		break;   		
    	case ROTATE:
    		pendantdispiv1.setImageDrawable(getResources().getDrawable(R.drawable.anticlockwise_img));
    		pendantdispiv2.setImageDrawable(getResources().getDrawable(R.drawable.clockwise_img));
    		pendantswitchbtn.setImageDrawable(getResources().getDrawable(R.drawable.rotatebtn));
    		break;
    	}
    	pendantControl = pendantcontrol;
    }
    
    //设置魔术棒控制条状态
    private void setMagicwandControl(MagicwandControl magicwandcontrol) {
    	switch(magicwandcontrol) {
    	case START:
    		magicwandswitchbtn.setImageDrawable(getResources().getDrawable(R.drawable.startbtn));
    		break;
    	case STOP:
    		magicwandswitchbtn.setImageDrawable(getResources().getDrawable(R.drawable.stopbtn));
    		break;
    	}
    	magicwandControl = magicwandcontrol;
    }
 
    //保存图片
    private String savePhoto() {
		if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
			return null;

		Bitmap savedbmp = pev2.getPhoto(saveSize);
    	String dirpath = Environment.getExternalStorageDirectory().toString() + "/digu-PhotoEdition2";
    	File saveddir = new File(dirpath);
    	if(!saveddir.exists())
    		saveddir.mkdirs();
    	String filepath = dirpath + "/saved_photo.jpg";
		File savedfile = new File(filepath);
		
		try {
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(savedfile));
			savedbmp.compress(Bitmap.CompressFormat.JPEG, 80, bos);
			bos.flush();
			bos.close();
		}
		catch(Exception e) {
			e.getMessage();
			return null;
		}
		
		if(savedbmp != null && !savedbmp.isRecycled()) {
			savedbmp.recycle();
			savedbmp = null;
		}
		
		return filepath;
	}
    
    //开启挂件控制条
    private void openPendantControl() {
    	if(!pendantControlOpen) {
    		pendantbar.setVisibility(View.VISIBLE);
    		pendantbar.startAnimation(pushupinanim);
    		pendantControlOpen = true;
    	}

    	//更新当前挂件图片
    	pendantiv.setImageBitmap(pev2.getCurrPendantBmp());
    }
    //更新挂件控制条
    private void updatePendantSeekBar() {
    	int scale = -1;
    	switch(pendantControl) {	
    	case SCALE:
    		scale = pev2.getPendantScale();
    		break;
    		
    	case ROTATE:
    		scale = pev2.getPendantRotate();
    		break;   		
    	}
    	if(scale >= 0)
    		pendantseekbar.setProgress(scale);
    }
    //关闭挂件控制条
    private void closePendantControl() {
    	if(pendantControlOpen) {
    		pendantbar.startAnimation(pushdownoutanim);
    		pendantbar.setVisibility(View.GONE);
    		pendantControlOpen = false;
    	}
    }
    
    //开启魔术棒控制条
    private void openMagicwandControl() {
    	if(!magicwandControlOpen) {
    		magicwandbar.setVisibility(View.VISIBLE);
    		magicwandbar.startAnimation(pushupinanim);
    		magicwandControlOpen = true;
    	}

    	//更新当前挂件图片
    	magicwandiv.setImageBitmap(pev2.getCurrMagicwandBmp());
    }
    //更新魔术棒控制条
    private void updateMagicwandSeekBar() {
    	int scale = pev2.getMagicwandScale();
    	if(scale >= 0)
    		magicwandseekbar.setProgress(scale);
    }
    //关闭魔术棒控制条
    private void closeMagicwandControl() {
    	if(magicwandControlOpen) {
    		magicwandbar.startAnimation(pushdownoutanim);
    		magicwandbar.setVisibility(View.GONE);
    		setMagicwandControl(MagicwandControl.START);
    		magicwandControlOpen = false;
    	}
    }
    
    public static final int OPEN_PENDANT_CONTROL = 0;
    public static final int UPDATE_PENDANT_CONTROL = 1;
    public static final int CLOSE_PENDANT_CONTROL = 2;
    public static final int OPEN_MAGICWAND_CONTROL = 3;
    public static final int CLOSE_MAGICWAND_CONTROL = 4;
    //处理消息句柄
    private class PhotoEditionHandler extends Handler {
    	
    	public PhotoEditionHandler(Looper looper) {
    		super(looper);
    	}
    	
    	@Override
    	public void handleMessage(Message msg) {
    		switch(msg.what) {
    		case OPEN_PENDANT_CONTROL:
    			closeMagicwandControl();
    			openPendantControl();
    			updatePendantSeekBar();
    			break;   			
    		case UPDATE_PENDANT_CONTROL:
    			updatePendantSeekBar();
				break;
    		case CLOSE_PENDANT_CONTROL:
    			closePendantControl();
    			break;
    		case OPEN_MAGICWAND_CONTROL:
    			closePendantControl();
    			openMagicwandControl();
    			//updateMagicwandSeekBar();
    			break;	
    		case CLOSE_MAGICWAND_CONTROL:
    			closeMagicwandControl();
    			break;	
    		}
    	}
    }

 	//回收内存
	public void recycleAll() {
		if(originalPhoto != null && !originalPhoto.isRecycled())
			originalPhoto.recycle();
		originalPhoto = null;
		pev1.recycleAll();
		pev2.recycleAll();
		filterAdapter.recycleAll();
		frameAdapter.recycleAll();
		pendantAdapter.recycleAll();
		magicwandAdapter.recycleAll();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		recycleAll();
		Log.i("recycleAll", "done.");
	}

}