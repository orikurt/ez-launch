package com.sadna.service;

import java.util.ArrayList;
import java.util.List;

import com.sadna.interfaces.IDataManager;
import com.sadna.interfaces.ISnapshotInfo;
import com.sadna.interfaces.IWidgetItemInfo;
import com.sadna.interfaces.Snapshot;

import android.app.ActivityManager;
import android.app.ActivityManager.RecentTaskInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Service;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import android.os.IBinder;

public class StatisticsService extends Service{

	Snapshot currSnapshot;
	IDataManager dataManager;

	SystemIntentsReceiver systemIntentsReceiver;
	
	PackageManager packageManager;
	ActivityManager activityManager;

	private int MAX_TASKS = 10;
	public static String UPDATE_INTENT = "com.sadna.intents.UPDATE_INTENT";

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		//code to execute when the service is first created
		initFields();

		// Register to ACTION_SCREEN_OFF;
		if (systemIntentsReceiver == null)
			systemIntentsReceiver = new SystemIntentsReceiver();
		registerReceiver(systemIntentsReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
	}

	@Override
	public void onDestroy() {
		//code to execute when the service is shutting down
	}

	@Override
	public void onStart(Intent intent, int startId) {
		//code to execute when the service is starting up
	}

	public void initFields() {

		/*dataManager = new DataManager();*/
		
		ISnapshotInfo snapshotInfo = null /*TODO: new  SnapshotInfo()*/;
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
			//TODO: fill itemInfo parameters
			IWidgetItemInfo itemInfo/* = new WidgetItemInfo()*/ = null;
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
			i--;
		}
		
		currSnapshot.normalizeScores();
	}

	public void notifyWidget() {
		// Send intent
		Intent updateIntent = new Intent(UPDATE_INTENT);
		sendBroadcast(updateIntent);
	}
	
	public class SystemIntentsReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {

			if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
				updateWithRecentTasks();
				notifyWidget();
			}
		}
	}
}