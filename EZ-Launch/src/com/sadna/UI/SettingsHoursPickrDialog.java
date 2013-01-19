package com.sadna.UI;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TimePicker;

import com.sadna.data.DataManager;
import com.sadna.widgets.application.R;

public class SettingsHoursPickrDialog  extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {

		final DataManager dm = new DataManager(this.getBaseContext());

		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings_time_pickr);

		// TimePicker
		final TimePicker timePK = (TimePicker) findViewById(R.id.timePicker1);

		// button
		final Button btn = (Button) findViewById(R.id.button);
		// Perform action on click
		btn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {  

				finish();
			}
		});
		
		final Button btnCancel = (Button) findViewById(R.id.buttonCancel);
		// Perform action on click
		btnCancel.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {  

				finish();
			}
		});

	}
}
