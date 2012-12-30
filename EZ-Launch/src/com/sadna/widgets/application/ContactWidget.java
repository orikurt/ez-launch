
package com.sadna.widgets.application;

import java.util.Set;

import com.sadna.data.Snapshot;
import com.sadna.service.StatisticsService;


import android.annotation.SuppressLint;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract.QuickContact;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.WindowManager;


public abstract class ContactWidget extends AppWidgetProvider {

	public interface WidgetImplementation {
		public void setWidget(ContactWidget widget);
		public void onUpdate(Context context, int appWidgetId, Snapshot snap);
		public boolean onReceive(Context context, Intent intent);
	}


	// Tag for logging
	private static final String TAG = "sadna.ContactWidget";
	
	private static final String SERVICE_NOTIFIER_LAUNCH = "sadna.service_notifier_launch";

	private WidgetImplementation mImpl;
	Snapshot snap;

	public ContactWidget() {
		super();
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
			mImpl = new ImplSWA();
		else
			mImpl = new ImplHC();
		mImpl.setWidget(this);

	}


	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		// If no specific widgets requested, collect list of all
		Log.d(TAG, "onUpdate");
		if (appWidgetIds == null) {
			appWidgetIds = Preferences.getAllWidgetIds(context);
		}

		
		
        

		if (appWidgetIds.length == 0) {
			Log.d(TAG, "appWidgetIds is empty");
			return;
		}

		Log.d(TAG, "appWidgetIds[0]=" + appWidgetIds[0]);


		final int N = appWidgetIds.length;
		for (int i = 0; i < N; i++) {
			// Construct views
			int appWidgetId = appWidgetIds[i];
			mImpl.onUpdate(context, appWidgetId, snap);  
		}


	}

	public abstract int getWidth();

	public void logIntent(Intent intent, boolean extended) {
		if (extended)
			Log.d(TAG, "------------Log Intent------------");
		Log.d(TAG, "Action       : " + intent.getAction());
		if (!extended)
			return;
		Log.d(TAG, "Data         : " + intent.getDataString());
		Log.d(TAG, "Component    : " + intent.getComponent().toString());
		Log.d(TAG, "Package      : " + intent.getPackage());
		Log.d(TAG, "Flags        : " + intent.getFlags());
		Log.d(TAG, "Scheme       : " + intent.getScheme());
		Log.d(TAG, "SourceBounds : " + intent.getSourceBounds());
		Log.d(TAG, "Type         : " + intent.getType());
		Bundle extras = intent.getExtras();
		if (extras != null) {
			Log.d(TAG, "--Extras--");

			for(String key : extras.keySet()) {
				Log.d(TAG, key + " --> " + extras.get(key));
			}
			Log.d(TAG, "----------");
		}
		Set<String> cats = intent.getCategories();
		if (cats != null) {
			Log.d(TAG, "--Categories--");
			for(String cat : cats) {
				Log.d(TAG, " --> " + cat);
			}
			Log.d(TAG, "--------------");
		}
		Log.d(TAG, "----------------------------------");
	}

	@SuppressLint("NewApi")
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "Contact Widget - onReceive");
		final String action = intent.getAction();
		logIntent(intent, true);


		if (AppWidgetManager.ACTION_APPWIDGET_DELETED.equals(action)) {
			final int appWidgetId = intent.getExtras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
					AppWidgetManager.INVALID_APPWIDGET_ID);
			if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
				Log.d(TAG, "onReceive-calling delete");
				this.onDeleted(context, new int[] { appWidgetId });
			}
		}

		else if (StatisticsService.SNAPSHOT_UPDATE.equals(action)) {
			Log.d(TAG, "onReceive- calling onupdate");

			// Generate appWidgetManager
			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
			ComponentName thisWidget = new ComponentName(context, getClass());
			int [] widgetIDs = appWidgetManager.getAppWidgetIds(thisWidget);
			/*snap = intent.getParcelableExtra(StatisticsService.NEW_SNAPSHOT);
			onUpdate(context, null, Preferences.getAllWidgetIds(context));*/
			for (int id : widgetIDs) {
				Log.d(TAG, " calling notifyAppWidgetViewDataChanged for id=" + id);
				appWidgetManager.notifyAppWidgetViewDataChanged(id, R.id.my_gridview);
			}
		}

		else if (!mImpl.onReceive(context, intent)) {
			Log.d(TAG, "onReceive- calling onReceive for mImpl");
			super.onReceive(context, intent);
		}
	}

	/**
	 * Will be executed when the widget is removed from the homescreen
	 */
	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		super.onDeleted(context, appWidgetIds);
		// Drop the settings if the widget is deleted
		Preferences.DropSettings(context, appWidgetIds);
	}


	public void onClick(Context context, int appWidgetId, Rect targetRect, Uri uri) {
		Intent LaunchIntent = context.getPackageManager().getLaunchIntentForPackage(uri.toString());
		context.startActivity(LaunchIntent);
		Intent serviceIntent = new Intent(SERVICE_NOTIFIER_LAUNCH);
		serviceIntent.putExtra("name", LaunchIntent.getPackage());
		context.sendBroadcast(serviceIntent);
		/*
		try
		{
			int act = Preferences.getOnClickAction(context, appWidgetId);
            if (act == Preferences.CLICK_QCB) {
			    QuickContact.showQuickContact(context,targetRect ,
					uri, QuickContact.MODE_LARGE, null);
            } else if (act == Preferences.CLICK_SHWCONTACT || act == Preferences.CLICK_SMS) {
            	Intent launch = new Intent(Intent.ACTION_VIEW, uri);
    			launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    			context.startActivity(launch);
            } else if (act == Preferences.CLICK_DIAL) {
            	Intent launch = new Intent(Intent.ACTION_CALL, uri);
    			launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    			context.startActivity(launch);
            }
		}
		catch(ActivityNotFoundException expt)
		{
			Log.d(TAG, "FAILED: " + expt.getMessage());
		}
		 */
	}

	public static int getICSWidth(Context context) {
		return (int)context.getResources().getDimensionPixelSize(R.dimen.widgetColumnWidth);
	}

	public static int calcWidthPixel(Context context, int appWidgetId, int width) {
		DisplayMetrics dm = context.getResources().getDisplayMetrics();
		Display display = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		display.getMetrics(dm);
		boolean horizontal = (display.getOrientation() % 2) == 1;

		int spanx = Preferences.getSpanX(context, appWidgetId, width);
		if (horizontal)
			width = 106 * spanx;
		else
			width = 80 * spanx;
		final int colCount = Preferences.getColumnCount(context, appWidgetId);
		width = width - (colCount * 5) - 5; // grid view spacing...
		width = (int)(((width - 24) / colCount) * dm.density);
		return width;
	}
}