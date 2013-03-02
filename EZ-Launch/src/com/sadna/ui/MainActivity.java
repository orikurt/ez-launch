package com.sadna.ui;

import com.sadna.service.StatisticsService;
import com.sadna.widgets.application.Preferences;
import com.sadna.widgets.application.R;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Check if StatisticsService running
	    ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	        if (StatisticsService.class.getName().equals(service.service.getClassName())) {
	            return;
	        }
	    }
		
		// Start Statistics Service
		startService(new Intent(this, StatisticsService.class));
	}
	
	@Override
	protected void onStart() {
		super.onStart();

		// Check for widgets on screen
		Context c = this.getBaseContext();
		int[] widgets = Preferences.getAllWidgetIds(c);
		if (widgets.length != 0) {
			
			// Widget exists - Start settings activity
			startActivity(new Intent(this.getBaseContext(), SettingsActivity.class));
			finish();
		}
		
		// Widget doesn't exist - Tell user to put widgets
		setContentView(R.layout.main_activity);
	}
}