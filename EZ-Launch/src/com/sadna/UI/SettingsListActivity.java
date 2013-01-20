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
import android.widget.Button;
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
	
		snap = dm.getSelectedSnapshot();

		

		list=(ListView)findViewById(android.R.id.list);
	
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
        
        
		final Button btn = (Button) findViewById(R.id.cancel_button);
		btn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {  
				finish();
			}
		});
		
		// ok_button
		final Button btnCancel = (Button) findViewById(R.id.ok_button);
		btnCancel.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {  

				//dm.setApplicationLimit(numberPK.getValue());
				dm.saveSnapshot(snap);
				finish();
			}
		});

	}	
}
