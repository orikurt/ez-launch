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
	public static final String RESERVED_SNAPSHOT = "Default Snapshot %s";
	public static final String SERVICE_NOTIFIER_LAUNCH = "sadna.service_notifier_launch";
	private static final int MAX_TASKS = 25;
	private static final long UPDATE_DELAY = 7500;
	public static final String SERVICE_NOTIFIER_BLACK_LIST = "sadna.service_notifier_black_list";
	public static final double RUN_FROM_WIDGET_BONUS = 0.25;
	public static final double NOT_RUN_FROM_LAUNCHER_MODIFIER = 0.5;
	public static final double BASE_SCORE = 1.0;

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
		Log.d(LOG_TAG, "Destroyed");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		Log.d(LOG_TAG, "Started");

		// Register to intents;
		if (systemIntentsReceiver == null)
			systemIntentsReceiver = new SystemIntentsReceiver();
		registerReceiver(systemIntentsReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
		registerReceiver(systemIntentsReceiver, new IntentFilter(Intent.ACTION_SCREEN_ON));
		registerReceiver(systemIntentsReceiver, new IntentFilter(SERVICE_NOTIFIER_LAUNCH));
		registerReceiver(systemIntentsReceiver, new IntentFilter(SERVICE_NOTIFIER_BLACK_LIST));
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
	}

	public void notifyWidget() {
		Log.d(LOG_TAG, "notifyWidget");
		// Send update intent
		Intent updateWidget = new Intent(SNAPSHOT_UPDATE);
		sendBroadcast(updateWidget);
	}
	
	private void scoreUpdate(){
		if (packageManager == null) {
			initFields();
		}
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
			lastUsed = defaultLauncher;
			return;
		}
		
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
		
		double score = BASE_SCORE;
		
		if (lastUsed != null){
			if (!lastUsed.equals(defaultLauncher)){
				score *= NOT_RUN_FROM_LAUNCHER_MODIFIER;
			}
			if (lastUsed.equals(name)){
				if (!lastRunning && isRunning)
					increaseScore(name, score);
			} else{
				lastRunning = isRunning;
				lastUsed = name;
				increaseScore(name, score);
			
			}
		} else{
			lastUsed = name;
			lastRunning = isRunning;
			increaseScore(name, score);
		}
	}
	
	public void increaseScore(String name, double score){
		if (dataManager == null) {
			dataManager = new DataManager(getApplicationContext());
		}
		if (packageManager == null) {
			packageManager = getPackageManager();
		}
		
		IWidgetItemInfo itemInfo = dataManager.getSelectedSnapshot().getItemByName(name);
		if (itemInfo == null){
			ApplicationInfo appInfo;
			try {
				appInfo = packageManager.getApplicationInfo(name, PackageManager.GET_META_DATA);
			} catch (NameNotFoundException e) {
				return;
			}
			String label = packageManager.getApplicationLabel(appInfo).toString();
			itemInfo = new WidgetItemInfo(name, label,dataManager.getAvaregeScore());
			dataManager.getSelectedSnapshot().add(itemInfo);
			Log.d(LOG_TAG, "Added " + name);
		}
		Log.d(LOG_TAG, name + " score:" + Double.toString(itemInfo.getScore()) + " -> "+ Double.toString(itemInfo.getScore() + score));
		itemInfo.setScore(itemInfo.getScore() + score);
		itemInfo.setLastUse(new Date());
	}

	public class SystemIntentsReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(SERVICE_UPDATE)){
				Log.d(LOG_TAG, "Got SERVICE_UPDATE");
				//updateReservedSnapshot();
				notifyWidget();
			}
			
			else if (intent.getAction().equals(SERVICE_NOTIFIER_LAUNCH)){
				String pkgName = intent.getStringExtra("name");
				if (pkgName != null){ 
					increaseScore(pkgName, RUN_FROM_WIDGET_BONUS);
				}
			}
			else if (intent.getAction().equals(SERVICE_NOTIFIER_BLACK_LIST)){
				String pkgName = intent.getStringExtra("name");
				if (pkgName != null){ 
					IWidgetItemInfo item = dataManager.getSelectedSnapshot().getItemByName(pkgName);
					item.setScore(0);
					item.setItemState(ItemState.NOT_ALLOWED);
					Log.d(LOG_TAG, pkgName + " Added to black List");
					Log.d(LOG_TAG, "Got SERVICE_UPDATE");
					notifyWidget();
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
				synchronized (syncObj) {
					screenLocked = true;
					h.removeCallbacksAndMessages(null);
					notifyWidget();
				}
				dataManager.saveSnapshot(dataManager.getSelectedSnapshot());
			}
			
			else if (intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED)){
				String name = intent.getDataString();
				dataManager.deleteWidgetItemInfo(name);
				Log.d(LOG_TAG, name + " was removed");
				notifyWidget();
			}
			

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