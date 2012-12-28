package com.sadna.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.android.data.DataManager;
import com.android.data.Snapshot;
import com.android.data.SnapshotInfo;
import com.android.data.WidgetItemInfo;
import com.sadna.interfaces.IDataManager;
import com.sadna.interfaces.ISnapshotInfo;
import com.sadna.interfaces.IWidgetItemInfo;
import com.sadna.widgets.application.R;

import android.app.ActivityManager;
import android.app.ActivityManager.RecentTaskInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Service;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;

import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class StatisticsService extends Service{
	String LOG_TAG = "StatisticsService";
	
	Snapshot currSnapshot;
	IDataManager dataManager;
	
	SystemIntentsReceiver systemIntentsReceiver;
	
	PackageManager packageManager;
	ActivityManager activityManager;
	
	// Intent related globals
	private Date lastUnlock;

	private int MAX_TASKS = 10;
	public static String UPDATE_INTENT = "com.sadna.intents.UPDATE_INTENT";

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		// Notify the user about Creating.
        Toast.makeText(this, R.string.statistics_service_created, Toast.LENGTH_SHORT).show();
		Log.d(LOG_TAG, "Created");
	}

	@Override
	public void onDestroy() {
        // Notify the user about destroying.
        Toast.makeText(this, R.string.statistics_service_stopped, Toast.LENGTH_SHORT).show();
        Log.d(LOG_TAG, "Destroyed");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
        // Notify the user about Starting.
        Toast.makeText(this, R.string.statistics_service_started, Toast.LENGTH_SHORT).show();
        Log.d(LOG_TAG, "Started");
        
		// Initialize all private fields
		initFields();

		// Register to ACTION_SCREEN_OFF;
		if (systemIntentsReceiver == null)
			systemIntentsReceiver = new SystemIntentsReceiver();
		registerReceiver(systemIntentsReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
		registerReceiver(systemIntentsReceiver, new IntentFilter(Intent.ACTION_SCREEN_ON));
		registerReceiver(systemIntentsReceiver, new IntentFilter(Intent.ACTION_PACKAGE_ADDED));
		registerReceiver(systemIntentsReceiver, new IntentFilter(Intent.ACTION_PACKAGE_REMOVED));
        
        return START_STICKY;
	}

	public void initFields() {


		dataManager = new DataManager(this.getApplicationContext());
		
		
		//dataManager = new DataManager(null, LOG_TAG, null, MAX_TASKS);
		if (dataManager == null) {
			//Error
		}
		lastUnlock = new Date();
		Date current = new Date();
		ISnapshotInfo snapshotInfo = new SnapshotInfo("temp", current);

		currSnapshot = new Snapshot(snapshotInfo, getInstalledAppsInfo());
		if (currSnapshot == null) {
			//Error
		}
		
		packageManager = getPackageManager();
		activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
	}

	private List<IWidgetItemInfo> getInstalledAppsInfo() {
		List<IWidgetItemInfo> result = new ArrayList<IWidgetItemInfo>();
		
		Context context = getApplicationContext();

		final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		final List<ResolveInfo> pkgAppsList = context.getPackageManager().queryIntentActivities(mainIntent, 0);

		for (ResolveInfo resolveInfo : pkgAppsList) {

			Drawable itemIcon = resolveInfo.loadIcon(packageManager);
			String itemLabel = resolveInfo.loadLabel(packageManager).toString();
			String itemPkgName = resolveInfo.resolvePackageName;
			Intent itemIntent = packageManager.getLaunchIntentForPackage(itemPkgName);
			IWidgetItemInfo itemInfo = new WidgetItemInfo(itemIcon, itemPkgName, itemIntent, itemLabel);
			result.add(itemInfo);
		}
		
		return result;
	}

	private void updateWithRunningTasks() {
		ActivityManager actvityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);

		// get the info from the currently running task
		List< RunningTaskInfo > tasksInfo = actvityManager.getRunningTasks(MAX_TASKS);

		for (RunningTaskInfo taskInfo : tasksInfo) {

			ComponentName componentInfo = taskInfo.baseActivity;
			componentInfo.getPackageName();
		}
		currSnapshot.normalizeScores();
	}

	private void updateWithRecentTasks() {
		// get the info from the currently running task
		List<RecentTaskInfo> tasksInfo = activityManager.getRecentTasks(MAX_TASKS, ActivityManager.RECENT_IGNORE_UNAVAILABLE);
		double i = currSnapshot.size() - tasksInfo.size();
		
		for (RecentTaskInfo taskInfo : tasksInfo) {
			IWidgetItemInfo itemInfo = currSnapshot.getItemByName(taskInfo.origActivity.getPackageName());
			if (itemInfo == null)
				continue;
			
			itemInfo.setScore(itemInfo.getScore()+i);
			i/=10;
		}
		
		currSnapshot.normalizeScores();
	}

	public void notifyWidget() {
		// Send update intent
		Intent updateIntent = new Intent(UPDATE_INTENT);
		sendBroadcast(updateIntent);
	}
	
	public class SystemIntentsReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			PackageManager pm = getPackageManager();

			if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
				lastUnlock = new Date();
			}
			
			if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
				Date now = new Date();
				if ((now.getTime() - lastUnlock.getTime()) > 10000){
					updateWithRecentTasks();
					notifyWidget();
				}
			}
			if (intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED)) {
				int pkgId = Integer.parseInt(Intent.EXTRA_UID);
				String name = pm.getNameForUid(pkgId);
				ApplicationInfo info;
				Drawable image;
				try {
					image = pm.getApplicationIcon(name);
					info = pm.getApplicationInfo(name, PackageManager.GET_META_DATA);
				} catch (NameNotFoundException e) {
					// TODO Auto-generated catch block
					// e.printStackTrace();
					return;
				}
				
				String label = pm.getApplicationLabel(info).toString();
				Intent launchIntent = pm.getLaunchIntentForPackage(name);
				IWidgetItemInfo newItem = new WidgetItemInfo(image, name, launchIntent, label);
				currSnapshot.add(newItem);
			}
			if (intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED)){
				notifyWidget();
			}
		}
	}
}