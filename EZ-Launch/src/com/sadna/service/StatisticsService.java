package com.sadna.service;

import java.util.Date;
import java.util.List;

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
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.sadna.data.DataManager;
import com.sadna.data.Snapshot;
import com.sadna.data.WidgetItemInfo;
import com.sadna.enums.ItemState;
import com.sadna.interfaces.IDataManager;
import com.sadna.interfaces.IWidgetItemInfo;
import com.sadna.widgets.application.R;

public class StatisticsService extends Service{
	String LOG_TAG = "StatisticsService";
	public static final String NEW_SNAPSHOT = "com.sadna.widgets.application.newSnapshot";
	public static final String SNAPSHOT_UPDATE = "com.sadna.widgets.application.SNAPSHOT_UPDATE";
	public static final String SERVICE_UPDATE = "com.sadna.widgets.application.SERVICE_UPDATE";
	public static final String RESERVED_SNAPSHOT = "Default Snapshot";
	public static final String SERVICE_NOTIFIER_LAUNCH = "sadna.service_notifier_launch";
	//private static final String SERVICE_ALARM_LOCK = "sadna.service_alarm_lock";
	//private static final String SERVICE_ALARM_UNLOCK = "sadna.service_alarm_unlock";
	private static final int MAX_TASKS = 25;
	private static final long UPDATE_DELAY = 7500;
	public static final String SERVICE_NOTIFIER_BLACK_LIST = "sadna.service_notifier_black_list";

	Snapshot		currSnapshot;
	IDataManager	dataManager;

	SystemIntentsReceiver systemIntentsReceiver;

	PackageManager	packageManager;
	ActivityManager	activityManager;
	String defaultLauncher;

	// Intent related globals
	private Date lastUnlock;
	private boolean screenLocked;
	private String lastUsed;
	private boolean lastRunning;
	private HandlerThread d;
	private Handler h;
	private static Object syncObj = new Object();

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
		super.onCreate();
		// Notify the user about Creating.
		initFields();
		d = new HandlerThread("Looper");
		d.start();
		h = new Handler(d.getLooper());
		h.postDelayed(timerRunnable, UPDATE_DELAY);
		Log.d(LOG_TAG, "Created");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		synchronized (syncObj) {
			if (h != null)
				h.removeCallbacksAndMessages(null);
			if (d != null)
				d.quit();
		}
		// Notify the user about destroying.
		Toast.makeText(this, R.string.statistics_service_stopped, Toast.LENGTH_SHORT).show();
		Log.d(LOG_TAG, "Destroyed");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		// Notify the user about Starting.
		//Toast.makeText(this, R.string.statistics_service_started, Toast.LENGTH_SHORT).show();
		Log.d(LOG_TAG, "Started");

		// Register to ACTION_SCREEN_OFF;
		if (systemIntentsReceiver == null)
			systemIntentsReceiver = new SystemIntentsReceiver();
		registerReceiver(systemIntentsReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
		registerReceiver(systemIntentsReceiver, new IntentFilter(Intent.ACTION_SCREEN_ON));
		registerReceiver(systemIntentsReceiver, new IntentFilter(SERVICE_NOTIFIER_LAUNCH));
		registerReceiver(systemIntentsReceiver, new IntentFilter(SERVICE_UPDATE));
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
		filter.addDataScheme("package");
		registerReceiver(systemIntentsReceiver, filter);

		return START_STICKY;
	}

	public void initFields() {

		if (dataManager == null) {
			dataManager = new DataManager(this.getApplicationContext());
		}
		if (activityManager == null) {
			activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);	
		}
		if (packageManager == null) {
			packageManager = getPackageManager();	
		}
		if (defaultLauncher == null) {
			defaultLauncher = resolveDefaultLauncher();
		}
		
		
		dataManager.setDefaultLauncher(defaultLauncher);
		dataManager.validateIntegrity();

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
		if (name.equals(defaultLauncher)){
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
		/*WTF ?! who called this before init ?! 
		 * Pleaes make sure you understand the flow before removing those checks.. */
		if (dataManager == null) {
			dataManager = new DataManager(getApplicationContext());
		}
		if (currSnapshot == null) {
			currSnapshot = dataManager.getSelectedSnapshot();
		}
		if (packageManager == null) {
			packageManager = getPackageManager();
		}
		
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
			currSnapshot.add(itemInfo);
			Log.d(LOG_TAG, "Added " + name);
		}
		itemInfo.setScore(itemInfo.getScore() + score);
		itemInfo.setLastUse(new Date());
		Log.d(LOG_TAG, name + " score increased by " + Double.toString(score));
	}

	public class SystemIntentsReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(SERVICE_UPDATE)){
				Log.d(LOG_TAG, "Got SERVICE_UPDATE");
				updateReservedSnapshot();
				notifyWidget();
			}

			else if (intent.getAction().equals(Intent.ACTION_MAIN) && intent.hasCategory(Intent.CATEGORY_HOME)){
				Log.d(LOG_TAG, "Home button pressed; notifying widget");
				notifyWidget();
			}
			
			else if (intent.getAction().equals(SERVICE_NOTIFIER_LAUNCH)){
				String pkgName = intent.getStringExtra("name");
				if (pkgName != null){ 
					IWidgetItemInfo item = currSnapshot.getItemByName(pkgName);
					item.setScore(item.getScore()+0.25);
					item.setLastUse(new Date());
					Log.d(LOG_TAG, pkgName + " new score:" + Double.toString(item.getScore()));
				}
			}
			else if (intent.getAction().equals(SERVICE_NOTIFIER_BLACK_LIST)){
				String pkgName = intent.getStringExtra("name");
				if (pkgName != null){ 
					IWidgetItemInfo item = currSnapshot.getItemByName(pkgName);
					item.setScore(0);
					item.setItemState(ItemState.NOT_ALLOWED);
					Log.d(LOG_TAG, pkgName + "Added to black List");
				}
			}
			else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
				lastUnlock = new Date();
				
				synchronized (syncObj) {
					screenLocked = false;
					h.removeCallbacksAndMessages(null);
					h.postDelayed(timerRunnable, UPDATE_DELAY);
				}
			}

			else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
//				Date now = new Date();
//				if ((now.getTime() - lastUnlock.getTime()) > /*1000*/0){
//					//updateWithRecentTasks();
//					updateWithRunningTasks();
//					notifyWidget();
//				}
				synchronized (syncObj) {
					screenLocked = true;
					h.removeCallbacksAndMessages(null);
					notifyWidget();
				}
			}
			
			else if (intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED)){
				String name = intent.getDataString();
				dataManager.deleteWidgetItemInfo(name);
				Log.d(LOG_TAG, name + " was removed");
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
	
	public String resolveDefaultLauncher(){
		final Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		final ResolveInfo res = packageManager.resolveActivity(intent, 0);
		if ((res.activityInfo == null) || res.activityInfo.packageName.equals("android")){
			return null;
		} else{
			return res.activityInfo.packageName;
		}
	}
}