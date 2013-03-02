
package com.sadna.widgets.application;

import java.util.Date;

import android.annotation.SuppressLint;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.sadna.data.DataManager;
import com.sadna.data.Snapshot;
import com.sadna.data.WidgetItemInfo;
import com.sadna.service.StatisticsService;


public abstract class ContactWidget extends AppWidgetProvider {
	private static final int THRESHOLD = 4;

	public interface WidgetImplementation {
		public void setWidget(ContactWidget widget);
		public void onUpdate(Context context, int appWidgetId, Snapshot snap);
		public boolean onReceive(Context context, Intent intent);
	}

	// Tag for logging
	private static final String TAG = "sadna.ContactWidget";

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
		if (appWidgetIds == null) {
			appWidgetIds = Preferences.getAllWidgetIds(context);
		}

		if (appWidgetIds.length == 0) {
			Log.d(TAG, "appWidgetIds is empty");
			return;
		}

		final int N = appWidgetIds.length;
		for (int i = 0; i < N; i++) {
			// Construct views
			int appWidgetId = appWidgetIds[i];
			mImpl.onUpdate(context, appWidgetId, snap);  
		}
	}

	public abstract int getWidth();

	@SuppressLint("NewApi")
	@Override
	public void onReceive(Context context, Intent intent) {
		final String action = intent.getAction();


		if (AppWidgetManager.ACTION_APPWIDGET_DELETED.equals(action)) {
			final int appWidgetId = intent.getExtras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
					AppWidgetManager.INVALID_APPWIDGET_ID);
			if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
				Log.d(TAG, "onReceive-calling delete");
				this.onDeleted(context, new int[] { appWidgetId });
			}
		}
		else if (StatisticsService.SNAPSHOT_UPDATE.equals(action)) {
			// Generate appWidgetManager
			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
			ComponentName thisWidget = new ComponentName(context, getClass());
			int [] widgetIDs = appWidgetManager.getAppWidgetIds(thisWidget);
			if ((DataManager.lastWidgerUpdate == null) || (DataManager.getSeconds(DataManager.lastWidgerUpdate) > THRESHOLD)) {

				DataManager.lastWidgerUpdate = new Date();
				for (int id : widgetIDs) {
					if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB){

					}else
					{
						appWidgetManager.notifyAppWidgetViewDataChanged(id, R.id.my_gridview);
					}
				}
			}
		}
		else if (AppWidgetManager.ACTION_APPWIDGET_ENABLED.equals(action)) {
			// Start StatisticsService
			Intent serviceIntent = new Intent("com.sadna.service.StatisticsService");  
			context.startService(serviceIntent);
		}
		else if (!mImpl.onReceive(context, intent)) {
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


	public void onClick(Context context, int appWidgetId, Rect targetRect, Intent LaunchIntent) {

		if (LaunchIntent.getCategories().contains(WidgetItemInfo.BLACK_LIST_APP_INTENT)) {
			addToBlackList(context, LaunchIntent);
			return;
		}
		try {
			context.startActivity(LaunchIntent);
			Intent serviceIntent = new Intent(StatisticsService.SERVICE_NOTIFIER_LAUNCH);
			serviceIntent.putExtra("name", LaunchIntent.getPackage());
			context.sendBroadcast(serviceIntent);
		}catch (ActivityNotFoundException e) {
			addToBlackList(context, LaunchIntent);

		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void addToBlackList(Context context, Intent LaunchIntent) {
		Intent serviceIntent = new Intent(StatisticsService.SERVICE_NOTIFIER_BLACK_LIST);
		serviceIntent.putExtra("name", LaunchIntent.getPackage());
		context.sendBroadcast(serviceIntent);
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