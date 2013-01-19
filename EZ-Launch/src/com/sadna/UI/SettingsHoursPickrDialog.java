package com.sadna.UI;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.sadna.UI.RangeSeekBar.OnRangeSeekBarChangeListener;
import com.sadna.data.DataManager;
import com.sadna.widgets.application.R;

public class SettingsHoursPickrDialog  extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {

		final DataManager dm = new DataManager(this.getBaseContext());

		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings_time_pickr);
		
		RangeSeekBar<Integer> seekBar = new RangeSeekBar<Integer>(0, 23, this);
		seekBar.setOnRangeSeekBarChangeListener(new OnRangeSeekBarChangeListener<Integer>() {
		        @Override
		        public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Integer minValue, Integer maxValue) {
		                // handle changed range values
		                Log.i("LOG", "User selected new range values: MIN=" + minValue + ", MAX=" + maxValue);
		        }
		});

		// add RangeSeekBar to layout
		ViewGroup PrefLayout = (ViewGroup) findViewById(R.id.rangeSeekContainer);
		PrefLayout.addView(seekBar);
		
		final Button btn = (Button) findViewById(R.id.buttonSave);
		btn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {  
				// Save to DB
				finish();
			}
		});
		
		final Button btnCancel = (Button) findViewById(R.id.buttonCancel);
		btnCancel.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {  
				finish();
			}
		});

	}
}
