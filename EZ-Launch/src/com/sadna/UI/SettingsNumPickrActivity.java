package com.sadna.UI;

import com.sadna.widgets.application.R;

import android.app.Activity;
import android.os.Bundle;
import android.widget.NumberPicker;

public class SettingsNumPickrActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings_num_pickr);
		
		// numberPicker
		final NumberPicker numberPK = (NumberPicker) findViewById(R.id.numberPicker);
		numberPK.setMaxValue(100);
		numberPK.setMinValue(0);
		numberPK.setWrapSelectorWheel(false);
		
	}

}
