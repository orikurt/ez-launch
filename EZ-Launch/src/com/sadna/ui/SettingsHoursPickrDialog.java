package com.sadna.ui;

import java.util.concurrent.TimeUnit;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.view.View.OnTouchListener;
import com.sadna.data.DataManager;
import com.sadna.ui.RangeSeekBar.OnRangeSeekBarChangeListener;
import com.sadna.widgets.application.R;

public class SettingsHoursPickrDialog  extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {

		final DataManager dm = new DataManager(this.getBaseContext());

		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings_time_pickr);
		this.setTitle(R.string.hours_title);
		final TextView leftText = (TextView) findViewById(R.id.clockLeft);
		final TextView rightText = (TextView) findViewById(R.id.clockRight);


		final RangeSeekBar<Integer> seekBar = new RangeSeekBar<Integer>(0, (24*6)-1, this);

		seekBar.setOnRangeSeekBarChangeListener(new OnRangeSeekBarChangeListener<Integer>() {
			@Override
			public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Integer minValue, Integer maxValue) {
				// handle changed range values
				leftText.setText(formatTime(minValue*10));
				rightText.setText(formatTime(maxValue*10));
			}
		});


		seekBar.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				leftText.setText(formatTime(seekBar.getSelectedMinValue()*10));
				rightText.setText(formatTime(seekBar.getSelectedMaxValue()*10));
				return false;
			}
		});
		// add RangeSeekBar to layout
		ViewGroup PrefLayout = (ViewGroup) findViewById(R.id.rangeSeekContainer);
		PrefLayout.addView(seekBar);

		int[] hours = dm.getWorkingHours();
		if (hours.length == 2) {

			seekBar.setSelectedMinValue(hours[0]/10);
			seekBar.setSelectedMaxValue(hours[1]/10);
			leftText.setText(formatTime(hours[0]));
			rightText.setText(formatTime(hours[1]));
		}
		final Button btn = (Button) findViewById(R.id.buttonSave);
		btn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {  
				Integer selectedMinValue = seekBar.getSelectedMinValue() * 10;
				Integer selectedMaxValue = seekBar.getSelectedMaxValue() * 10;
				dm.setWorkingHours( selectedMinValue, selectedMaxValue);
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

	@SuppressLint("DefaultLocale")
	private String formatTime(int d) {
		int hours  = (int) TimeUnit.MINUTES.toHours(d);
		int minutes = d - (hours * 60); 

		return String.format("%02d:%02d", hours,minutes);

	}
}