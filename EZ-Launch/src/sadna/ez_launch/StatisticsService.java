package sadna.ez_launch;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sadna.ez_launch.MainActivity.StatisticsUpdateReceiver;
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
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class StatisticsService extends Service{

	ArrayList<Shortcut> mList;
	List<Score> scoreList;
	
	public static StatisticsService sInstance;
	SystemIntentsReceiver systemIntentsReceiver;
	
	private int MAX_TASKS = 10;
	public static String UPDATE_INTENT = "sadna.ez_launch.UPDATE_INTENT";

	public static StatisticsService getInstance() {
		return sInstance;
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		//code to execute when the service is first created
		sInstance = this;
		
		initData();
		
		// Register to StatisticsService.UPDATE_INTENT;
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
		sInstance = this;
		//code to execute when the service is starting up
	}

	public void initData() {
		mList= new ArrayList<Shortcut>();
		scoreList = new ArrayList<Score>();
	}

	public void sendStatistics() {		
		Intent i = new Intent(UPDATE_INTENT);
		sendBroadcast(i);
	}

//	private StringBuilder GetRunningActivityManager() {
//		ActivityManager actvityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
//
//		// get the info from the currently running task
//		List< RunningTaskInfo > tasksInfo = actvityManager.getRunningTasks(MAX_TASKS);
//
//		final StringBuilder logs = new StringBuilder();
//		String separator = System.getProperty("line.separator"); 
//
//		for (RunningTaskInfo taskInfo : tasksInfo) {
//
//			ComponentName componentInfo = taskInfo.baseActivity;
//			componentInfo.getPackageName();
//
//			logs.append(componentInfo);
//			logs.append(separator);
//		}
//		return logs;
//	}

	private void GetRecentTasks() {
	ActivityManager actvityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);

	// get the info from the currently running task
	List< RecentTaskInfo > tasksInfo = actvityManager.getRecentTasks(MAX_TASKS, ActivityManager.RECENT_IGNORE_UNAVAILABLE);
	PackageManager pk = getPackageManager();

	for (RecentTaskInfo taskInfo : tasksInfo) {
		Shortcut a;
		try {
			a = new Shortcut(pk.getApplicationIcon(taskInfo.origActivity.getPackageName()),
					taskInfo.origActivity.getPackageName());
			mList.add(a);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
	
	private void GetInstalledApplicationsList()
	{
		Context context = getApplicationContext();

		final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		final List<ResolveInfo> pkgAppsList = context.getPackageManager().queryIntentActivities( mainIntent, 0);
		
		for (ResolveInfo resolveInfo : pkgAppsList) {

			Shortcut a = new Shortcut((resolveInfo.activityInfo.loadIcon(getPackageManager())),
					resolveInfo.activityInfo.packageName);
			mList.add(a);
		}
	}
	
	public class SystemIntentsReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			
			if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
				//GetInstalledApplicationsList();
				GetRecentTasks();
				sendStatistics();
			}
		}
	}
}