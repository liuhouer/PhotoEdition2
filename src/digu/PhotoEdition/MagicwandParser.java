package digu.PhotoEdition;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

public class MagicwandParser {
	
	HashMap<String, ArrayList<String>> idGadgets;
	LinkedHashMap<String, String> idIcon;
	
	//构造函数（资源路径输入）
	public MagicwandParser(Context context, String magicwandsPath) {
		try {
			InputStream is = context.getAssets().open(magicwandsPath + "/magicwands.json");

			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));					
				StringBuilder sb = new StringBuilder();
				String line = null;
				while ((line = reader.readLine()) != null) {
					sb.append(line + " ");
				}					
				String json = sb.toString();
		        
				JSONArray objArray = new JSONArray(json);
				idGadgets = new HashMap<String, ArrayList<String>>();
				idIcon = new LinkedHashMap<String, String>();
				
				for(int i = 0; i < objArray.length(); i++) {
					JSONObject obj = objArray.getJSONObject(i);
					
					String id = obj.getString("id");
					String iconpath = magicwandsPath + "/" + id + ".magicwand/icon.png";
					int multi;
					if(!obj.isNull("multi"))
						multi = obj.getInt("multi");
					else
						multi = 1;
					ArrayList<String> gadgets = new ArrayList<String>();
					for(int j = 0; j < multi; j++) {
						gadgets.add(magicwandsPath + "/" + id + ".magicwand/gadget" + j + ".png");
					}//for j
					
					idGadgets.put(id, gadgets);
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
	//获取id与机件路径列表的映射
	public HashMap<String, ArrayList<String>> getIdGadget() {
		return idGadgets;
	}
	//获取id与图标路径的映射
	public LinkedHashMap<String, String> getIdIcon() {
		return idIcon;
	}
}
