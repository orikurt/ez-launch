package com.sadna.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.sadna.data.ConfigurationItemInfo;
import com.sadna.data.DataManager;
import com.sadna.data.Snapshot;
import com.sadna.data.SnapshotInfo;
import com.sadna.data.WidgetItemInfo;
import com.sadna.interfaces.IDataManager;
import com.sadna.interfaces.ISnapshotInfo;
import com.sadna.interfaces.IWidgetItemInfo;
import com.sadna.widgets.application.R;

import android.annotation.SuppressLint;
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


import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class StatisticsService extends Service{
	String LOG_TAG = "StatisticsService";
	public static final String NEW_SNAPSHOT = "com.sadna.widgets.application.newSnapshot";
	public static final String SNAPSHOT_UPDATE = "com.sadna.widgets.application.SNAPSHOT_UPDATE";
	public static final String RESERVED_SNAPSHOT = "Default Snapshot";
	private static final String SERVICE_NOTIFIER_LAUNCH = "sadna.service_notifier_launch";
	private static final int MAX_TASKS = 25;

	Snapshot		currSnapshot;
	IDataManager	dataManager;

	SystemIntentsReceiver systemIntentsReceiver;

	PackageManager	packageManager;
	ActivityManager	activityManager;

	// Intent related globals
	private Date lastUnlock;

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
		registerReceiver(systemIntentsReceiver, new IntentFilter(SERVICE_NOTIFIER_LAUNCH));
		IntentFilter launchFilter = new IntentFilter();
		launchFilter.addCategory(Intent.CATEGORY_LAUNCHER);
		launchFilter.addAction(Intent.ACTION_MAIN);
		IntentFilter homeFilter = new IntentFilter();
		homeFilter.addCategory(Intent.CATEGORY_HOME);
		homeFilter.addAction(Intent.ACTION_MAIN);
		
		registerReceiver(systemIntentsReceiver, launchFilter);
		registerReceiver(systemIntentsReceiver, homeFilter);

		return START_STICKY;
	}

	public void initFields() {

		dataManager = new DataManager(this.getApplicationContext());

		activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		packageManager = getPackageManager();

		lastUnlock = new Date();

		// Get current snapshot
		if (dataManager.getSelectedSnapshot() == null) {
			// DB is empty - Generate first snapshot
			Date currDate = new Date();
			ISnapshotInfo snapshotInfo = new SnapshotInfo(currDate.toString(), currDate);
			currSnapshot = new Snapshot(snapshotInfo, getInstalledAppsInfo());
			// Adding the config option
			currSnapshot.add(new ConfigurationItemInfo());
			dataManager.saveSnapshot(currSnapshot);
			dataManager.setSelectedSnapshot(currSnapshot);
		}
		else {
			// DB isn't empty
			currSnapshot = dataManager.getSelectedSnapshot();
		}
	}

	public void notifyWidget() {
		Log.d(LOG_TAG, "notifyWidget");

		// Save snapshot to DB
		//currSnapshot.normalizeScores();
		Collections.sort(currSnapshot);
		dataManager.saveSnapshot(currSnapshot);
		dataManager.setSelectedSnapshot(currSnapshot);
		
		// Send update intent
		Intent updateWidget = new Intent(SNAPSHOT_UPDATE);
		sendBroadcast(updateWidget);
	}

	private List<IWidgetItemInfo> getInstalledAppsInfo() {
		List<IWidgetItemInfo> result = new ArrayList<IWidgetItemInfo>();

		Context context = getApplicationContext();

		final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		final List<ResolveInfo> pkgAppsList = context.getPackageManager().queryIntentActivities(mainIntent, 0);

		for (ResolveInfo resolveInfo : pkgAppsList) {
			String itemLabel = resolveInfo.loadLabel(packageManager).toString();
			String itemPkgName = resolveInfo.activityInfo.packageName;
			IWidgetItemInfo itemInfo = new WidgetItemInfo(itemPkgName, itemLabel);
			result.add(itemInfo);
		}

		return result;
	}

	private void updateWithRunningTasks() {
		// get the info from the currently running task
		List< RunningTaskInfo > tasksInfo = activityManager.getRunningTasks(MAX_TASKS);
		double i = currSnapshot.size() - tasksInfo.size();
		for (RunningTaskInfo taskInfo : tasksInfo) {
			ComponentName componentInfo = taskInfo.baseActivity;
			String pkgName = componentInfo.getPackageName();
			IWidgetItemInfo itemInfo = currSnapshot.getItemByName(pkgName);
			if (itemInfo != null) {
				itemInfo.setScore( Double.isNaN(itemInfo.getScore()) ? i : itemInfo.getScore()+i);
				Log.d(LOG_TAG, pkgName + " new score:" + Double.toString(itemInfo.getScore()));
			}

			i--;
		}
		currSnapshot.normalizeScores();
	}

	@SuppressLint("NewApi")
	private void updateWithRecentTasks() {
		// get the info from the currently running task
		List<RecentTaskInfo> tasksInfo = activityManager.getRecentTasks(MAX_TASKS, ActivityManager.RECENT_IGNORE_UNAVAILABLE);
		double i = currSnapshot.size() - tasksInfo.size();

		for (RecentTaskInfo taskInfo : tasksInfo) {

			IWidgetItemInfo itemInfo = null;

			// Trying desperately to find package name
			String pkgName = null;
			ComponentName compName = null;

			// First shot
			pkgName = taskInfo.baseIntent.getPackage();
			if (pkgName != null) {
				Log.d(LOG_TAG, "pkgName is not null!");
				itemInfo = currSnapshot.getItemByName(pkgName);
			}
			// Second shot
			else if ((compName = taskInfo.origActivity) != null) {
				Log.d(LOG_TAG, "compName is not null!");
				pkgName = compName.getPackageName();
				if (pkgName != null) {
					itemInfo = currSnapshot.getItemByName(pkgName);
				}
			}

			if (itemInfo == null)
				// We don't Have package name
				continue;

			itemInfo.setScore(itemInfo.getScore()+i);
			i--;
		}
		currSnapshot.normalizeScores();
	}

	public class SystemIntentsReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {

			updateReservedSnapshot();
			
			if (intent.getAction().equals(Intent.ACTION_MAIN) && intent.hasCategory(Intent.CATEGORY_HOME)){
				Log.d(LOG_TAG, "Home button pressed; notifying widget");
				notifyWidget();
			}
			
			if (intent.getAction().equals(SERVICE_NOTIFIER_LAUNCH)){
				String pkgName = intent.getStringExtra("name");
				if (pkgName != null){ 
					IWidgetItemInfo item = currSnapshot.getItemByName(pkgName);
					item.setScore(item.getScore()+1.0);
					Log.d(LOG_TAG, pkgName + " new score:" + Double.toString(item.getScore()));
				}
			}
			if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
				lastUnlock = new Date();
			}

			if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
				Date now = new Date();
				if ((now.getTime() - lastUnlock.getTime()) > /*1000*/0){
					//updateWithRecentTasks();
					updateWithRunningTasks();
					notifyWidget();
				}
			}

			if (intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED)) {
				int pkgId = Integer.parseInt(Intent.EXTRA_UID);
				String name = packageManager.getNameForUid(pkgId);
				ApplicationInfo info;

				try {

					info = packageManager.getApplicationInfo(name, PackageManager.GET_META_DATA);
				} catch (NameNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return;
				}

				String label = packageManager.getApplicationLabel(info).toString();
				IWidgetItemInfo newItem = new WidgetItemInfo(name, label);
				currSnapshot.add(newItem);
				Collections.sort(currSnapshot);
			}

			if (intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED)){
				notifyWidget();
			}
		}
	}

	public void updateReservedSnapshot() {

		Snapshot newCurrSnapshot = dataManager.getSelectedSnapshot();
		if (newCurrSnapshot == null) {
			notifyWidget();
			return;
		}

		String newName = newCurrSnapshot.getSnapshotInfo().getSnapshotName();
		if ((newName != null) && (!newName.equalsIgnoreCase(RESERVED_SNAPSHOT))) {

			// User changed the current snapshot
			newCurrSnapshot.getSnapshotInfo().setSnapshotName(RESERVED_SNAPSHOT);
			currSnapshot = newCurrSnapshot;
			notifyWidget();
		}
	}
}