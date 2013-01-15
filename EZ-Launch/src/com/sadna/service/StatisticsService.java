package com.sadna.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

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
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;


import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class StatisticsService extends Service{
	String LOG_TAG = "StatisticsService";
	public static final String NEW_SNAPSHOT = "com.sadna.widgets.application.newSnapshot";
	public static final String SNAPSHOT_UPDATE = "com.sadna.widgets.application.SNAPSHOT_UPDATE";
	public static final String SERVICE_UPDATE = "com.sadna.widgets.application.SERVICE_UPDATE";
	public static final String RESERVED_SNAPSHOT = "Default Snapshot";
	private static final String SERVICE_NOTIFIER_LAUNCH = "sadna.service_notifier_launch";
	private static final String SERVICE_ALARM_LOCK = "sadna.service_alarm_lock";
	private static final String SERVICE_ALARM_UNLOCK = "sadna.service_alarm_unlock";
	private static final int MAX_TASKS = 25;
	private static final long UPDATE_DELAY = 7500;

	Snapshot		currSnapshot;
	IDataManager	dataManager;

	SystemIntentsReceiver systemIntentsReceiver;

	PackageManager	packageManager;
	ActivityManager	activityManager;

	// Intent related globals
	private Date lastUnlock;
	private boolean screenLocked;
	private String lastUsed;
	private boolean lastRunning;
	private HandlerThread d;
	private Handler h;
	Object syncObj = new Object();

	Runnable timerRunnable = new Runnable(){
		public void run(){
			//Log.d(LOG_TAG, "timerRunnable started!");
			synchronized (syncObj) {
				h.removeCallbacksAndMessages(null);
				scoreUpdate();
				if (!screenLocked){
					h.postDelayed(timerRunnable, UPDATE_DELAY);
				}
			}
		}
	};
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		// Notify the user about Creating.
		super.onCreate();
		d = new HandlerThread("Looper");
		d.start();
		h = new Handler(d.getLooper());
		h.postDelayed(timerRunnable, UPDATE_DELAY);
		Log.d(LOG_TAG, "Created");
	}

	@Override
	public void onDestroy() {
		if (h != null)
			h.removeCallbacksAndMessages(null);
		if (d != null)
			d.quit();
		// Notify the user about destroying.
		Toast.makeText(this, R.string.statistics_service_stopped, Toast.LENGTH_SHORT).show();
		Log.d(LOG_TAG, "Destroyed");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// Notify the user about Starting.
		//Toast.makeText(this, R.string.statistics_service_started, Toast.LENGTH_SHORT).show();
		Log.d(LOG_TAG, "Started");

		// Initialize all private fields
		initFields();

		// Register to ACTION_SCREEN_OFF;
		if (systemIntentsReceiver == null)
			systemIntentsReceiver = new SystemIntentsReceiver();
		registerReceiver(systemIntentsReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
		registerReceiver(systemIntentsReceiver, new IntentFilter(Intent.ACTION_SCREEN_ON));
		registerReceiver(systemIntentsReceiver, new IntentFilter(SERVICE_NOTIFIER_LAUNCH));
		registerReceiver(systemIntentsReceiver, new IntentFilter(SERVICE_UPDATE));

		return START_STICKY;
	}

	public void initFields() {

		dataManager = new DataManager(this.getApplicationContext());

		activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		packageManager = getPackageManager();

		screenLocked = false;
		lastUnlock = new Date();
		lastUsed = null;
		lastRunning = false;

		// Get current snapshot

		currSnapshot = dataManager.getSelectedSnapshot();

	}

	public void notifyWidget() {
		Log.d(LOG_TAG, "notifyWidget");

		// Save snapshot to DB
		//currSnapshot.normalizeScores();
		//Collections.sort(currSnapshot);
		dataManager.saveSnapshot(currSnapshot);
		dataManager.setSelectedSnapshot(currSnapshot);
		
		// Send update intent
		Intent updateWidget = new Intent(SNAPSHOT_UPDATE);
		sendBroadcast(updateWidget);
	}
	
	private void scoreUpdate(){
		List<RecentTaskInfo> recentTasksInfo = activityManager.getRecentTasks(MAX_TASKS, ActivityManager.RECENT_IGNORE_UNAVAILABLE);
		
		if (recentTasksInfo == null) return;
		
		RecentTaskInfo recentTask = recentTasksInfo.get(0);
		if (recentTask == null) return;
		
		// get package name
		String name;
		
		Intent intent = new Intent(recentTask.baseIntent);
		if (recentTask.origActivity != null){
			intent.setComponent(recentTask.origActivity);
		}
		name = packageManager.resolveActivity(intent, 0).activityInfo.packageName;
		if (name.equals("com.sec.android.app.launcher")){
			return;
		}
//			recentTask = recentTasksInfo.get(1);
//			if (recentTask == null) return;
//			intent = new Intent(recentTask.baseIntent);
//			if (recentTask.origActivity != null){
//				intent.setComponent(recentTask.origActivity);
//			}
//			name = packageManager.resolveActivity(intent, 0).activityInfo.packageName;
//		}
		
		// check if app is running
		boolean isRunning = false;
		ComponentName comp;
		String runningName;
		List< RunningTaskInfo > runningTasksInfo = activityManager.getRunningTasks(MAX_TASKS);
		if (runningTasksInfo == null) return;
		RunningTaskInfo runningTask = runningTasksInfo.get(0);
		if (runningTask == null) return;
		comp = runningTask.baseActivity;
		runningName = comp.getPackageName();
		if (runningName.equals(name))
			isRunning = true;

		
		if (lastUsed != null){
			if (lastUsed.equals(name)){
				if (!lastRunning && isRunning)
					increaseScore(name, 1.0);
			} else{
				lastRunning = isRunning;
				lastUsed = name;
				increaseScore(name, 1.0);
			
			}
		} else{
			lastUsed = name;
			lastRunning = isRunning;
			increaseScore(name, 1.0);
		}
	}
	
	public void increaseScore(String name, double score){
		IWidgetItemInfo itemInfo = currSnapshot.getItemByName(name);
		if (itemInfo == null){
			ApplicationInfo appInfo;
			try {
				appInfo = packageManager.getApplicationInfo(name, PackageManager.GET_META_DATA);
			} catch (NameNotFoundException e) {
				return;
			}
			String label = packageManager.getApplicationLabel(appInfo).toString();
			itemInfo = new WidgetItemInfo(name, label);
			Log.d(LOG_TAG, "Added " + name);
		}
		itemInfo.setScore(itemInfo.getScore() + score);
		itemInfo.setLastUse(new Date());
		Log.d(LOG_TAG, name + " score increased by " + Double.toString(score));
	}

	public class SystemIntentsReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {

			if (intent.getAction().equals(Intent.ACTION_MAIN) && intent.hasCategory(Intent.CATEGORY_HOME)){
				Log.d(LOG_TAG, "Home button pressed; notifying widget");
				notifyWidget();
			}
			
			if (intent.getAction().equals(SERVICE_NOTIFIER_LAUNCH)){
				String pkgName = intent.getStringExtra("name");
				if (pkgName != null){ 
					IWidgetItemInfo item = currSnapshot.getItemByName(pkgName);
					item.setScore(item.getScore()+0.25);
					item.setLastUse(new Date());
					Log.d(LOG_TAG, pkgName + " new score:" + Double.toString(item.getScore()));
				}
			}
			if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
				lastUnlock = new Date();
				screenLocked = false;
				h.removeCallbacksAndMessages(null);
				h.postDelayed(timerRunnable, UPDATE_DELAY);
			}

			if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
//				Date now = new Date();
//				if ((now.getTime() - lastUnlock.getTime()) > /*1000*/0){
//					//updateWithRecentTasks();
//					updateWithRunningTasks();
//					notifyWidget();
//				}
				screenLocked = true;
				h.removeCallbacksAndMessages(null);
				notifyWidget();
			}
			
			if (intent.getAction().equals(SERVICE_UPDATE)){
				updateReservedSnapshot();
				notifyWidget();
			}
		}
	}

	public void updateReservedSnapshot() {

		Snapshot newCurrSnapshot = dataManager.getSelectedSnapshot();
		if (newCurrSnapshot == null) {
			return;
		}

		String newName = newCurrSnapshot.getSnapshotInfo().getSnapshotName();
		if ((newName != null) && (!newName.equalsIgnoreCase(RESERVED_SNAPSHOT))) {

			// User changed the current snapshot
			newCurrSnapshot.getSnapshotInfo().setSnapshotName(RESERVED_SNAPSHOT);
			currSnapshot = newCurrSnapshot;
		}
	}
}