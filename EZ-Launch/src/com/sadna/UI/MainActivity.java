package com.sadna.UI;

import com.sadna.service.StatisticsService;
import com.sadna.widgets.application.Preferences;
import com.sadna.widgets.application.R;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class MainActivity extends Activity {

	int[] widgets;
	Context c;

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
	    Log.d("MOR", "Start Statistics Service");
		startService(new Intent(this, StatisticsService.class));
	}
	
	@Override
	protected void onStart() {
		super.onStart();

		c = this.getBaseContext();
		widgets = Preferences.getAllWidgetIds(c);
		if (widgets.length != 0) {
			startActivity(new Intent(this.getBaseContext(), SettingsActivity.class));
			finish();
		}
		setContentView(R.layout.main);
	}

}
