package com.sadna.UI;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.NumberPicker;

import com.sadna.data.DataManager;
import com.sadna.widgets.application.R;

public class SettingsNumPickrDialog extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {

		final DataManager dm = new DataManager(this.getBaseContext());

		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings_num_pickr);
		this.setTitle(R.string.appsPerScreen);
		
		// numberPicker
		final NumberPicker numberPK = (NumberPicker) findViewById(R.id.numberPicker1);
		numberPK.setMaxValue(50);
		numberPK.setMinValue(1);
		numberPK.setWrapSelectorWheel(true);
		numberPK.setValue(dm.getApplicationLimit());

		//Hide keyboard
		InputMethodManager imm = (InputMethodManager)getSystemService(
				Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(numberPK.getWindowToken(), 0);

		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		numberPK.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

		// button
		final Button btn = (Button) findViewById(R.id.button1);
		// Perform action on click
		btn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {  

				dm.setApplicationLimit(numberPK.getValue());
				finish();
			}
		});
		
		final Button btnCancel = (Button) findViewById(R.id.button2);
		// Perform action on click
		btnCancel.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {  

				dm.setApplicationLimit(dm.getApplicationLimit());
				finish();
			}
		});

	}
}
