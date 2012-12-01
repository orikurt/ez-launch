package sadna.ez_launch;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

public class StatisticsService extends Service{

	int data;

	private int MAX_TASKS = 10;
	public static String UPDATE_INTENT = "sadna.ez_launch.UPDATE_INTENT";


	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		//code to execute when the service is first created
		data = 0;
		Intent i = new Intent(UPDATE_INTENT);
		i.putExtra("data", data);
		sendBroadcast(i);
	}

	@Override
	public void onDestroy() {
		//code to execute when the service is shutting down
	}

	@Override
	public void onStart(Intent intent, int startId) {
		//code to execute when the service is starting up
	}

	public void getStatistics() {
		Intent i = new Intent(UPDATE_INTENT);
		i.putExtra("data", data);
		sendBroadcast(i);
	}

	private StringBuilder GetRunningActivityManager() {
		ActivityManager actvityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);

		// get the info from the currently running task
		List< RunningTaskInfo > tasksInfo = actvityManager.getRunningTasks(MAX_TASKS);

		final StringBuilder logs = new StringBuilder();
		String separator = System.getProperty("line.separator"); 

		for (RunningTaskInfo taskInfo : tasksInfo) {

			ComponentName componentInfo = taskInfo.baseActivity;
			componentInfo.getPackageName();

			logs.append(componentInfo);
			logs.append(separator);
		}
		return logs;
	}

	private void bla()
	{
		try
		{
			Process mLogcatProc = null;
			BufferedReader reader = null;
			mLogcatProc = Runtime.getRuntime().exec("logcat -d");

			reader = new BufferedReader(new InputStreamReader(mLogcatProc.getInputStream()));

			String line;
			final StringBuilder log = new StringBuilder();
			String separator = System.getProperty("line.separator"); 

			while ((line = reader.readLine()) != null)
			{
				if (line.contains("ActivityManager") && line.contains("LAUNCHER"))
				{
					Pattern pattern = Pattern.compile("(pkg|cmp)=(.*?)/");
					Matcher matcher = pattern.matcher(line);
					if (matcher.find())
					{
						log.append(matcher.group(2));
						log.append(separator);
					}
				}
			}

			/*final TextView textViewToChange = (TextView) findViewById(R.id.textView1);
   textViewToChange.setMovementMethod(new ScrollingMovementMethod());
   textViewToChange.setText(log.toString());*/


		}
		catch (Exception e) 
		{
			Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}


}