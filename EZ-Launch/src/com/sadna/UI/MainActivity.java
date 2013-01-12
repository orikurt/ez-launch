package com.sadna.UI;

import com.sadna.widgets.application.Preferences;
import com.sadna.widgets.application.R;

import android.app.Activity;
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
