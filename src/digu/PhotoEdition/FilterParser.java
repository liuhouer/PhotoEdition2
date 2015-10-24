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

public class FilterParser {
	//记录滤镜信息
	public class FilterInfo {
		public float[] filterMatrix;
		public int[] holoColorArray;
		public float[] holoPosArray;
		public String holoModeName;
		public String overlayPath;
		public String overlayModeName;
	}
	
	HashMap<String, FilterInfo> idFilterInfo;
	LinkedHashMap<String, String> idIcon;
	HashMap<String, String> idName;
	
	//构造函数（绝对路径输入）
	public FilterParser(String filterspath) {		
		try {
			InputStream is = new FileInputStream(filterspath + "/filters.json");

			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));					
				StringBuilder sb = new StringBuilder();
				String line = null;
				while ((line = reader.readLine()) != null) {
					sb.append(line + " ");
				}					
				String json = sb.toString();
		        
				JSONArray objArray = new JSONObject(json).getJSONArray("filters");
				idFilterInfo = new HashMap<String, FilterInfo>();
				idIcon = new LinkedHashMap<String, String>();
				idName = new HashMap<String, String>();
				
				for(int i = 0; i < objArray.length(); i++) {
					JSONObject obj = objArray.getJSONObject(i);
					
					String id = obj.getString("id");
					String name = obj.getString("name");
					String iconpath = filterspath + "/" + id + ".filter/icon.png";
					
					FilterInfo filterinfo = new FilterInfo();					
					if(!obj.isNull("filter")) {
						JSONArray objArray1 = obj.getJSONArray("filter");
						filterinfo.filterMatrix = new float[objArray1.length()];
						for(int j = 0; j < objArray1.length(); j++)
							filterinfo.filterMatrix[j] = (float)objArray1.getDouble(j);

					}
					
					if(!obj.isNull("holoColor")) {
						JSONArray objArray2 = obj.getJSONArray("holoColor");
						filterinfo.holoColorArray = new int[objArray2.length()];
						for(int j = 0; j < objArray2.length(); j++) {
							filterinfo.holoColorArray[j] = Long.decode(objArray2.getString(j)).intValue();
						}
					}
					
					if(!obj.isNull("holoPos")) {
						JSONArray objArray3 = obj.getJSONArray("holoPos");
						filterinfo.holoPosArray = new float[objArray3.length()];
						for(int j = 0; j < objArray3.length(); j++)
							filterinfo.holoPosArray[j] = (float)objArray3.getDouble(j);
					}
					
					if(!obj.isNull("holoMode")) {
						filterinfo.holoModeName = obj.getString("holoMode");
					}
					
					if(!obj.isNull("overlay")&& obj.getBoolean("overlay")) {
						filterinfo.overlayPath = filterspath + "/" + id + ".filter/overlay.png";
					}
					
					if(!obj.isNull("overlayMode")) {
						filterinfo.overlayModeName = obj.getString("overlayMode");
					}

					idFilterInfo.put(id, filterinfo);
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
	public FilterParser(Context context, String filterspath) {		
		try {
			InputStream is = context.getAssets().open(filterspath + "/filters.json");

			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));					
				StringBuilder sb = new StringBuilder();
				String line = null;
				while ((line = reader.readLine()) != null) {
					sb.append(line + " ");
				}					
				String json = sb.toString();
		        
				JSONArray objArray = new JSONObject(json).getJSONArray("filters");
				idFilterInfo = new HashMap<String, FilterInfo>();
				idIcon = new LinkedHashMap<String, String>();
				idName = new HashMap<String, String>();
				
				for(int i = 0; i < objArray.length(); i++) {
					JSONObject obj = objArray.getJSONObject(i);
					
					String id = obj.getString("id");
					String name = obj.getString("name");
					String iconpath = filterspath + "/" + id + ".filter/icon.png";
					
					FilterInfo filterinfo;
					if(!obj.isNull("origin") && obj.getBoolean("origin"))
						filterinfo = null;
					else {
						filterinfo = new FilterInfo();					
						if(!obj.isNull("filter")) {
							JSONArray objArray1 = obj.getJSONArray("filter");
							filterinfo.filterMatrix = new float[objArray1.length()];
							for(int j = 0; j < objArray1.length(); j++)
								filterinfo.filterMatrix[j] = (float)objArray1.getDouble(j);
	
						}
						
						if(!obj.isNull("holoColor")) {
							JSONArray objArray2 = obj.getJSONArray("holoColor");
							filterinfo.holoColorArray = new int[objArray2.length()];
							for(int j = 0; j < objArray2.length(); j++) {
								filterinfo.holoColorArray[j] = Long.decode(objArray2.getString(j)).intValue();
							}
						}
						
						if(!obj.isNull("holoPos")) {
							JSONArray objArray3 = obj.getJSONArray("holoPos");
							filterinfo.holoPosArray = new float[objArray3.length()];
							for(int j = 0; j < objArray3.length(); j++)
								filterinfo.holoPosArray[j] = (float)objArray3.getDouble(j);
						}
						
						if(!obj.isNull("holoMode")) {
							filterinfo.holoModeName = obj.getString("holoMode");
						}
						
						if(!obj.isNull("overlay")&& obj.getBoolean("overlay")) {
							filterinfo.overlayPath = filterspath + "/" + id + ".filter/overlay.png";
						}
						
						if(!obj.isNull("overlayMode")) {
							filterinfo.overlayModeName = obj.getString("overlayMode");
						}
					}

					idFilterInfo.put(id, filterinfo);
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

	//获取id与滤镜信息的映射
	public HashMap<String, FilterInfo> getIdFilterInfo() {
		return idFilterInfo;
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
