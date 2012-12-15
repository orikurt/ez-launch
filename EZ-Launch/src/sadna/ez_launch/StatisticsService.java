package sadna.ez_launch;

import java.util.ArrayList;
import java.util.Collections;
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
import android.os.IBinder;

public class StatisticsService extends Service{

	ArrayList<Shortcut> shortcutList;
	List<Score> scoreList;

	public static StatisticsService sInstance;
	SystemIntentsReceiver systemIntentsReceiver;
	PackageManager packageManager;
	ActivityManager activityManager;

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
		packageManager = getPackageManager();
		activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);

		initData();

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
		sInstance = this;
		//code to execute when the service is starting up
	}

	public void initData() {

		shortcutList = new ArrayList<Shortcut>();

		scoreList = new ArrayList<Score>();
		initScoreList();
	}

	private void initScoreList() {
		Context context = getApplicationContext();

		final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		final List<ResolveInfo> pkgAppsList = context.getPackageManager().queryIntentActivities( mainIntent, 0);

		for (ResolveInfo resolveInfo : pkgAppsList) {
			Score score = new Score(resolveInfo.activityInfo.packageName, 0);
			scoreList.add(score);
		}
	}

	public void notifyWidget() {

		// Prepare shortcut list
		shortcutList.clear();
		Collections.sort(scoreList);
		for (Score score : scoreList) {
			try {
				ApplicationInfo appInfo = packageManager.getApplicationInfo(score.name, PackageManager.GET_META_DATA);
				Shortcut shortcut = new Shortcut(appInfo.loadIcon(packageManager), score.name, null);
				shortcutList.add(shortcut);
			}
			catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// Send intent
		Intent i = new Intent(UPDATE_INTENT);
		sendBroadcast(i);
	}

	private void updateWithRunningTasks() {
		ActivityManager actvityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);

		// get the info from the currently running task
		List< RunningTaskInfo > tasksInfo = actvityManager.getRunningTasks(MAX_TASKS);

		for (RunningTaskInfo taskInfo : tasksInfo) {

			ComponentName componentInfo = taskInfo.baseActivity;
			componentInfo.getPackageName();
		}
		normalizeScores();
	}

	private void updateWithRecentTasks() {
		// get the info from the currently running task
		List<RecentTaskInfo> tasksInfo = activityManager.getRecentTasks(MAX_TASKS, ActivityManager.RECENT_IGNORE_UNAVAILABLE);
		int i = scoreList.size() - tasksInfo.size();
		for (RecentTaskInfo taskInfo : tasksInfo) {
			Score score = findScore(taskInfo.origActivity.getPackageName());
			if (score == null)
				continue;
			
			score.adjustScore(i);
			i--;
		}
		
		normalizeScores();
	}

	private Score findScore(String name) {
		for (Score score : scoreList) {
			if (name.compareTo(score.getName()) == 0) {
				return score;
			}
		}
		return null;
	}

	private void normalizeScores() {
		
		// Calculate sum-of-squares
		float sumOfSqaures = 0;
		for (Score score : scoreList) {
			sumOfSqaures += (score.getScore() * score.getScore());
		}
		
		// Divide each score in sumOfSqaures
		for (Score score : scoreList) {
			score.setScore(score.getScore() / sumOfSqaures);
		}
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