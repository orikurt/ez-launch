package com.sadna.UI;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

public class MainActivity extends Activity {
	
	@Override
	protected void onStart() {
		super.onStart();
		startActivity(new Intent(this.getBaseContext(), SettingsActivity.class));
		finish();
	}

}
