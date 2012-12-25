package com.sadna.service;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class serviceActivity extends Activity {
    
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = new Intent("com.sadna.service.StatisticsService");  
	    startService(intent);
	    finish();
	}
	
}