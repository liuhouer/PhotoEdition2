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

public class PendantParser {

	HashMap<String, String> idPendant;
	LinkedHashMap<String, String> idIcon;

	//构造函数（绝对路径输入），只解析一个挂件信息
	public PendantParser(String pendantspath, String id) {		
		idPendant = new HashMap<String, String>();
		idIcon = new LinkedHashMap<String, String>();
							
		String iconpath = pendantspath + "/" + id + ".pendant/icon.png";
    	String pendantpath = pendantspath + "/" + id + ".pendant/pendant.png";
	
		idPendant.put(id, pendantpath);
		idIcon.put(id, iconpath);
	
	}
	//构造函数（绝对路径输入）
	public PendantParser(String pendantspath) {
		try {
			InputStream is = new FileInputStream(pendantspath + "/pendants.json");

			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));					
				StringBuilder sb = new StringBuilder();
				String line = null;
				while ((line = reader.readLine()) != null) {
					sb.append(line + " ");
				}					
				String json = sb.toString();
		        
				JSONArray objArray = new JSONArray(json);
				idPendant = new HashMap<String, String>();
				idIcon = new LinkedHashMap<String, String>();
				
				for(int i = 0; i < objArray.length(); i++) {
					JSONObject obj = objArray.getJSONObject(i);
					
					String id = obj.getString("id");					
					String iconpath = pendantspath + "/" + id + ".pendant/icon.png";
		        	String pendantpath = pendantspath + "/" + id + ".pendant/pendant.png";
	        	
		        	idPendant.put(id, pendantpath);
					idIcon.put(id, iconpath);
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
	public PendantParser(Context context, String pendantspath) {		
		try {
			InputStream is = context.getAssets().open(pendantspath + "/pendants.json");

			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));					
				StringBuilder sb = new StringBuilder();
				String line = null;
				while ((line = reader.readLine()) != null) {
					sb.append(line + " ");
				}					
				String json = sb.toString();
		        
				JSONArray objArray = new JSONObject(json).getJSONArray("pendants");
				idPendant = new HashMap<String, String>();
				idIcon = new LinkedHashMap<String, String>();
				
				for(int i = 0; i < objArray.length(); i++) {
					JSONObject obj = objArray.getJSONObject(i);
					
					String id = obj.getString("id");				
					String iconpath = pendantspath + "/" + id + ".pendant/icon.png";
		        	String pendantpath = pendantspath + "/" + id + ".pendant/pendant.png";
	        	
		        	idPendant.put(id, pendantpath);
					idIcon.put(id, iconpath);		        	
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
	
	//获取id与挂件路径的映射
	public HashMap<String, String> getIdPendant() {
		return idPendant;
	}
	//获取id与图标路径的映射
	public LinkedHashMap<String, String> getIdIcon() {
		return idIcon;
	}
}
