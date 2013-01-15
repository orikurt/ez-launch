package com.sadna.UI;

import com.sadna.data.DataManager;
import com.sadna.widgets.application.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;

public class SettingsNumPickrActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings_num_pickr);
		
		// numberPicker
		final NumberPicker numberPK1 = (NumberPicker) findViewById(R.id.numberPicker);
		numberPK1.setMaxValue(100);
		numberPK1.setMinValue(0);
		numberPK1.setWrapSelectorWheel(false);
		
		
        // button
        final Button btn1 = (Button) findViewById(R.id.button);
        // Perform action on click
        btn1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {  
            	
            	DataManager dm = new DataManager(v.getContext());  
            	dm.setApplicationLimit(numberPK1.getValue());
            	finish();
            }
        });
		
	}
}
