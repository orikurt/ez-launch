package com.sadna.UI;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.sadna.UI.RangeSeekBar.OnRangeSeekBarChangeListener;
import com.sadna.data.DataManager;
import com.sadna.widgets.application.R;

public class SettingsHoursPickrDialog  extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {

		final DataManager dm = new DataManager(this.getBaseContext());

		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings_time_pickr);
		final TextView leftText = (TextView) findViewById(R.id.clockLeft);
		final TextView rightText = (TextView) findViewById(R.id.clockRight);
		RangeSeekBar<Double> seekBar = new RangeSeekBar<Double>(0.0, 23.5, this);
		seekBar.setOnRangeSeekBarChangeListener(new OnRangeSeekBarChangeListener<Double>() {
		        @Override
		        public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Double minValue, Double maxValue) {
		                // handle changed range values
		                Log.d("LOG", "User selected new range values: MIN=" + minValue + ", MAX=" + maxValue);
		                leftText.setText(minValue.toString());
		                rightText.setText(minValue.toString());
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
