package sadna.ez_launch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RecentTaskInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.os.IBinder;

public class StatisticsService extends Service{

	ArrayList<Shortcut> mList;
	List<Scoring> scoringList;

	public static StatisticsService sInstance;
	SystemIntentsReceiver systemIntentsReceiver;
	PackageManager pk;

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
		initData();

		// Register to StatisticsService.UPDATE_INTENT;
		if (systemIntentsReceiver == null)
			systemIntentsReceiver = new SystemIntentsReceiver();
		registerReceiver(systemIntentsReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
		//registerReceiver(systemIntentsReceiver, new IntentFilter(Intent.ACTION_USER_FOREGROUND));
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
		sInstance = this;
		mList = new ArrayList<Shortcut>();
		scoringList	= new ArrayList<Scoring>();
		pk = getPackageManager();
		GetInstalledApplicationsList();
	}

	public void sendStatistics() {		
		Intent i = new Intent(UPDATE_INTENT);
		Collections.sort(scoringList, new ScoringComp());
		mList.clear();
		for (Scoring s : scoringList) {

			String name= s.info.loadLabel(getPackageManager()).toString();
			Shortcut a = new Shortcut((s.info.activityInfo.loadIcon(getPackageManager())),
					s.info.activityInfo.packageName,name);
			if (mList.size() < 16)
				mList.add(a);
		}
		sendBroadcast(i);
	}

	private void GetFrontActivity() {
		ActivityManager actvityManager = (ActivityManager) this.getBaseContext().getSystemService(ACTIVITY_SERVICE);

		// get the info from the currently running task
		List< RunningTaskInfo > tasksInfo = actvityManager.getRunningTasks(1);
		Shortcut a;
		try {
			a = new Shortcut(pk.getApplicationIcon(tasksInfo.get(0).baseActivity.getPackageName()),
					tasksInfo.get(0).baseActivity.getPackageName(),"No Name");
			mList.add(a);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void GetRecentTasks() {
		ActivityManager actvityManager = (ActivityManager) this.getBaseContext().getSystemService(ACTIVITY_SERVICE);

		List< RecentTaskInfo > tasksInfo = actvityManager.getRecentTasks(MAX_TASKS, ActivityManager.RECENT_IGNORE_UNAVAILABLE);
		
		float i = tasksInfo.size();
		for (RecentTaskInfo taskInfo : tasksInfo) {
			ResolveInfo resolveInfo = pk.resolveActivity(taskInfo.baseIntent, PackageManager.MATCH_DEFAULT_ONLY);
			String packageName = resolveInfo.activityInfo.packageName;
			
			for (Scoring scoring :scoringList) {
				String scoringPackageName =  scoring.info.activityInfo.packageName;
				if (packageName.equals(scoringPackageName)) {
					scoring.score += i;
					i--;
					break;
				}
			}
		}
	}

	private void GetInstalledApplicationsList()
	{
		mList.clear();
		Context context = getApplicationContext();
		PackageManager pk = context.getPackageManager();

		final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		List<ResolveInfo> pkgAppsList = pk.queryIntentActivities( mainIntent, 0);

		for (ResolveInfo resolveInfo : pkgAppsList) {
			//if (resolveInfo.loadLabel(pk).toString().equalsIgnoreCase("calculator")){
			scoringList.add(new Scoring(0, resolveInfo));
		}
	}

	public class SystemIntentsReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {

			if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
				//GetInstalledApplicationsList();
				//GetFrontActivity();
				GetRecentTasks();
				sendStatistics();
			}

			//			if (intent.getAction().equals(Intent.ACTION_USER_FOREGROUND)) {
			//				GetFrontActivity();
			//				sendStatistics();
			//			}
		}
	}

	public class ScoringComp implements Comparator<Scoring>{

		@Override
		public int compare(Scoring s1, Scoring s2){
			return (s1.score>s2.score ? -1 : (s1.score==s2.score ? 0 : 1));
		}
	}

	public class Scoring{
		float score;
		ResolveInfo info;

		public Scoring(int s, ResolveInfo i){
			this.score = s;
			this.info = i;
		}

	}
}