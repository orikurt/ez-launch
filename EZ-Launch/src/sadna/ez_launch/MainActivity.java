package sadna.ez_launch;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.os.Bundle;
import android.os.DeadObjectException;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RecentTaskInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//updateTaskList();
		GetInstalledApplicationsList();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}



	private void updateTaskList() {
		/* Grab the Systems IActivityManager. */

		PackageManager pm = this.getPackageManager();

		int numberOfTasks = 6;
		ActivityManager m = (ActivityManager)this.getSystemService(ACTIVITY_SERVICE);
		//Get some number of running tasks and grab the first one.  getRunningTasks returns newest to oldest
		RunningTaskInfo task = m.getRunningTasks(numberOfTasks).get(0);
		StringBuilder sb = new StringBuilder(); 
		//ActivityManager.RecentTaskInfo.
		for (RecentTaskInfo item : m.getRecentTasks(numberOfTasks, 0)) {
			sb.append("application"+item.id+"' and the PID is '"+ item.origActivity+ "  | "  + "\n");
		}
		sb.append("\n\n\n--------\n\n\n");
		for (RunningTaskInfo item : m.getRunningTasks(numberOfTasks)) {
			sb.append("application"+item.id+"' and the PID is '"+item.baseActivity.toShortString()+"'" + "\n");
		}
		//Build output
		String output  = "application"+task.id+"' and the PID is '"+task.baseActivity.toShortString()+"'";

		final TextView textViewToChange = (TextView) findViewById(R.id.textView1);
		textViewToChange.setMovementMethod(new ScrollingMovementMethod());
		textViewToChange.setText(sb.toString());

	
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

			final TextView textViewToChange = (TextView) findViewById(R.id.textView1);
			textViewToChange.setMovementMethod(new ScrollingMovementMethod());
			textViewToChange.setText(log.toString());


		}
		catch (Exception e) 
		{
			Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}

	public boolean GetRunningActivityManager() {
		ActivityManager actvityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		List<RecentTaskInfo> procInfos = actvityManager.getRecentTasks(50, ActivityManager.RECENT_WITH_EXCLUDED);

		final StringBuilder procsID = new StringBuilder();
		String separator = System.getProperty("line.separator"); 
		
		for (RecentTaskInfo info : procInfos) {
			procsID.append(info.id);
			procsID.append(separator);
		}
		final TextView textViewToChange = (TextView) findViewById(R.id.textView1);
		textViewToChange.setMovementMethod(new ScrollingMovementMethod());
		textViewToChange.setText(procsID.toString());
		return true;
	}

	private void GetInstalledApplicationsList()
	{
		
		
		Context context = getApplicationContext();
		
		final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		final List<ResolveInfo> pkgAppsList = context.getPackageManager().queryIntentActivities( mainIntent, 0);
		final TextView textViewToChange = (TextView) findViewById(R.id.textView1);
		
		StringBuilder sb = new StringBuilder();
		List<Drawable> mList= new ArrayList<Drawable>();
		for (ResolveInfo resolveInfo : pkgAppsList) {
			mList.add((resolveInfo.activityInfo.loadIcon(getPackageManager())));
		}
		
		
		setContentView(R.layout.secondscreen);

	    GridView gridview = (GridView) findViewById(R.id.gridview);
	    gridview.setAdapter(new ImageAdapter(this,mList));

	    gridview.setOnItemClickListener(new OnItemClickListener() {
	    	@Override
	    	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
	            Toast.makeText(MainActivity.this, "" + position, Toast.LENGTH_SHORT).show();
	        }

			
	    });
	    
	    
		
		textViewToChange.setMovementMethod(new ScrollingMovementMethod());
		textViewToChange.setText(sb.toString());
	}
}
