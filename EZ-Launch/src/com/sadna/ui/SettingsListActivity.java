package com.sadna.ui;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.sadna.data.DataManager;
import com.sadna.data.Snapshot;
import com.sadna.utils.LazyAdapter;
import com.sadna.utils.LazyAdapterBase;
import com.sadna.utils.LazyAdapterStatistics;
import com.sadna.widgets.application.R;

public class SettingsListActivity extends PreferenceActivity{

	public ListView list;
    public LazyAdapterBase adapter;
	private DataManager dm;
	private Snapshot snap;
	private EditText filterText;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.configuration_list);
		this.setTitle(R.string.fixAppsOnScreen);

		dm = new DataManager(getApplicationContext());
		
	    boolean isFixed = getIntent().getBooleanExtra(SettingsActivity.IS_FIXED, true);
		
	    snap = dm.getSelectedSnapshot();
		DataManager.SortSnapshot(snap);
		dm.validateIntegrity();
		dm.saveSnapshot(snap);

		list=(ListView)findViewById(android.R.id.list);
	
		// Getting adapter by passing xml data ArrayList
		if (isFixed) {
			adapter=new LazyAdapter(this, snap);	
		}else{
			adapter=new LazyAdapterStatistics(this, snap);	
		}
                
        list.setAdapter(adapter);
        filterText = (EditText) findViewById(R.building_list.search_box);
        filterText.addTextChangedListener(filterTextWatcher);

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
				dm.setSelectedSnapshot(dm.loadSnapshot(snap.getSnapshotInfo().getSnapshotName()));
				finish();
			}
		});
		
		// ok_button
		final Button btnCancel = (Button) findViewById(R.id.ok_button);
		btnCancel.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				dm.saveSnapshot(snap);
				finish();
			}
		});
	}
	private TextWatcher filterTextWatcher = new TextWatcher() {

	    public void afterTextChanged(Editable s) {
	    }

	    public void beforeTextChanged(CharSequence s, int start, int count,
	            int after) {
	    }

	    public void onTextChanged(CharSequence s, int start, int before,
	            int count) {
	        adapter.getFilter().filter(s);
	    }

	};
}