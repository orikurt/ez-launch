package com.sadna.UI;

import com.sadna.data.DataManager;
import com.sadna.widgets.application.R;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;

public class SettingsNumPickrDialog extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		final DataManager dm = new DataManager(this.getBaseContext());
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings_num_pickr);
		
		// numberPicker
		final NumberPicker numberPK = (NumberPicker) findViewById(R.id.numberPicker);
		numberPK.setMaxValue(50);
		numberPK.setMinValue(1);
		numberPK.setWrapSelectorWheel(true);
		numberPK.setValue(dm.getApplicationLimit());
		
		
        // button
        final Button btn = (Button) findViewById(R.id.button);
        // Perform action on click
        btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {  
            	 
            	dm.setApplicationLimit(numberPK.getValue());
            	finish();
            }
        });
		
	}
}