package com.sadna.UI;

import com.sadna.data.DataManager;
import com.sadna.data.Snapshot;
import com.sadna.utils.LazyAdapter;
import com.sadna.widgets.application.R;
import com.sadna.widgets.application.R.layout;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class SettingsListActivity extends PreferenceActivity{

	public ListView list;
    public LazyAdapter adapter;
	private DataManager dm;
	//private ArrayList<IWidgetItemInfo> appList;
	private Snapshot snap;
    

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.configuration_list);
		this.setTitle(R.string.fixAppsOnScreen);

		dm = new DataManager(getApplicationContext());
		//appList = new ArrayList<IWidgetItemInfo>();

		
		//String xml = parser.getXmlFromUrl(URL); // getting XML from URL
		//Document doc = parser.getDomElement(xml); // getting DOM element
		
		//NodeList nl = doc.getElementsByTagName(KEY_SONG);
		// looping through all song nodes <song>
		snap = dm.getSelectedSnapshot();
//		for (IWidgetItemInfo wi : snap) {
//			
//
//			// adding HashList to ArrayList
//			appList.add(wi);
//		}

		

		list=(ListView)findViewById(android.R.id.list);
		//list=(ListView)findViewById(R.id.list);
		
		// Getting adapter by passing xml data ArrayList
        adapter=new LazyAdapter(this, snap);        
        list.setAdapter(adapter);
        

        // Click event for single list row
        list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
							

			}
		});		
	}	

	@Override
	public void onPause(){
		super.onPause();
		dm.setSelectedSnapshot(snap);
		dm.saveSnapshot(snap);
	}
}
