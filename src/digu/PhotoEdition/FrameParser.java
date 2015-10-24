package digu.PhotoEdition;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

public class FrameParser {

	HashMap<String, String> idFrame;
	LinkedHashMap<String, String> idIcon;
	HashMap<String, String> idName;
	
	//构造函数（绝对路径输入），只解析一个相框信息
	public FrameParser(String framespath, String id) {		
		try {
			InputStream is = new FileInputStream(framespath + "/" + id + ".frame/frame.json");

			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));					
				StringBuilder sb = new StringBuilder();
				String line = null;
				while ((line = reader.readLine()) != null) {
					sb.append(line + " ");
				}					
				String json = sb.toString();
		        
				JSONObject obj = new JSONObject(json);
				idFrame = new HashMap<String, String>();
				idIcon = new LinkedHashMap<String, String>();
				idName = new HashMap<String, String>();
				
				String name = obj.getString("name");
				String iconpath = framespath + "/" + id + ".frame/icon.png";
				String framepath = framespath + "/" + id + ".frame/frame.png";;
        	
				idFrame.put(id, framepath);
				idIcon.put(id, iconpath);
				idName.put(id, name);

			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				try {
					is.close();
				} 
				catch (IOException e) {
					e.printStackTrace();
				}
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
	//构造函数（绝对路径输入）
	public FrameParser(String framespath) {		
		try {
			InputStream is = new FileInputStream(framespath + "/frames.json");

			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));					
				StringBuilder sb = new StringBuilder();
				String line = null;
				while ((line = reader.readLine()) != null) {
					sb.append(line + " ");
				}					
				String json = sb.toString();
		        
				JSONArray objArray = new JSONObject(json).getJSONArray("frames");
				idFrame = new HashMap<String, String>();
				idIcon = new LinkedHashMap<String, String>();
				idName = new HashMap<String, String>();
				
				for(int i = 0; i < objArray.length(); i++) {
					JSONObject obj = objArray.getJSONObject(i);
					
					String id = obj.getString("id");
					String name = obj.getString("name");
					String iconpath = framespath + "/" + id + ".frame/icon.png";
					String framepath = framespath + "/" + id + ".frame/frame.png";
	        	
					idFrame.put(id, framepath);
					idIcon.put(id, iconpath);
					idName.put(id, name);	        	
				}//for i
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				try {
					is.close();
				} 
				catch (IOException e) {
					e.printStackTrace();
				}
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
	//构造函数（资源路径输入）
	public FrameParser(Context context, String framespath) {		
		try {
			InputStream is = context.getAssets().open(framespath + "/frames.json");

			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));					
				StringBuilder sb = new StringBuilder();
				String line = null;
				while ((line = reader.readLine()) != null) {
					sb.append(line + " ");
				}					
				String json = sb.toString();
		        
				JSONArray objArray = new JSONObject(json).getJSONArray("frames");
				idFrame = new HashMap<String, String>();
				idIcon = new LinkedHashMap<String, String>();
				idName = new HashMap<String, String>();
				
				for(int i = 0; i < objArray.length(); i++) {
					JSONObject obj = objArray.getJSONObject(i);
					
					String id = obj.getString("id");
					String name = obj.getString("name");
					String iconpath = framespath + "/" + id + ".frame/icon.png";

					String framepath;
					if(!obj.isNull("origin") && obj.getBoolean("origin"))
						framepath = null;
					else
						framepath = framespath + "/" + id + ".frame/frame.png";
	        	
					idFrame.put(id, framepath);
					idIcon.put(id, iconpath);
					idName.put(id, name);  	
				}//for i
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				try {
					is.close();
				} 
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
	
	//获取id与相框路径的映射
	public HashMap<String, String> getIdFrame() {
		return idFrame;
	}
	//获取id与图标路径的映射
	public LinkedHashMap<String, String> getIdIcon() {
		return idIcon;
	}
	//获取id与名字的映射
	public HashMap<String, String> getIdName() {
		return idName;
	}

}
