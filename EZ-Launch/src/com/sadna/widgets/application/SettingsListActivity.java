package com.sadna.widgets.application;

import java.util.ArrayList;
import java.util.HashMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.sadna.data.DataManager;
import com.sadna.data.WidgetItemInfo;
import com.sadna.interfaces.IWidgetItemInfo;
import com.sadna.utils.LazyAdapter;





import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class SettingsListActivity extends PreferenceActivity{

	
	public static final String KEY_SONG = "song"; // parent node
	public static final String KEY_ID = "id";
	public static final String KEY_TITLE = "title";
	public static final String KEY_ARTIST = "artist";
	public static final String KEY_DURATION = "duration";
	public static final String KEY_THUMB_URL = "thumb_url";
	

	public ListView list;
    public LazyAdapter adapter;
    

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		

		DataManager dm = new DataManager(getApplicationContext());
		ArrayList<IWidgetItemInfo> appList = new ArrayList<IWidgetItemInfo>();

		
		//String xml = parser.getXmlFromUrl(URL); // getting XML from URL
		//Document doc = parser.getDomElement(xml); // getting DOM element
		
		//NodeList nl = doc.getElementsByTagName(KEY_SONG);
		// looping through all song nodes <song>
		for (IWidgetItemInfo wi : dm.getSelectedSnapshot()) {
			

			// adding HashList to ArrayList
			appList.add(wi);
		}

		

		list=(ListView)findViewById(R.id.list);
		
		// Getting adapter by passing xml data ArrayList
        adapter=new LazyAdapter(this, appList);        
        list.setAdapter(adapter);
        

        // Click event for single list row
        list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
							

			}
		});		
	}	

}
