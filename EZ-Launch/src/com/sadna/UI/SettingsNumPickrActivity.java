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
		
		final DataManager dm = new DataManager(this.getBaseContext());
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings_num_pickr);
		
		// numberPicker
		final NumberPicker numberPK1 = (NumberPicker) findViewById(R.id.numberPicker);
		numberPK1.setMaxValue(50);
		numberPK1.setMinValue(1);
		numberPK1.setWrapSelectorWheel(true);
		numberPK1.setValue(dm.getApplicationLimit());
		
		
        // button
        final Button btn1 = (Button) findViewById(R.id.button);
        // Perform action on click
        btn1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {  
            	 
            	dm.setApplicationLimit(numberPK1.getValue());
            	finish();
            }
        });
		
	}
}
