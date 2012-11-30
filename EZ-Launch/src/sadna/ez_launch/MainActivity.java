package sadna.ez_launch;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.os.DeadObjectException;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RecentTaskInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.pm.PackageManager;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		updateTaskList();
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
			sb.append("application"+item.id+"' and the PID is '"+ item.origActivity+ "  | "  + " | "  + "\n");
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

		//


		//    	IActivityManager myActivityManager = ActivityManagerNative.getDefault();
		//    	/* Will hold all the task"".toString()"" entries */
		//    	ArrayList<String> listEntries = new ArrayList<String>();
		//    	try {
		//    		int showLimit = 1;
		//    		/* Get all Tasks available (with limit set). */
		//    		List<IActivityManager.TaskInfo> allTasks = myActivityManager
		//    				.getTasks(showLimit, 0, null);
		//    		int i = 1;
		//    		/* Loop through all tasks returned. */
		//    		for (IActivityManager.TaskInfo aTask : allTasks) {
		//    			listEntries
		//    			.add("" + (i++) + ": "
		//    					+ aTask.baseActivity.getClassName() + " ID="
		//    					+ aTask.id);
		//    		}
		//    	} catch (DeadObjectException e) {
		//    		Log.e("TaskManager", e.getMessage(), e);
		//    	}
		//    	/* Display out listEntries */
		//    	setListAdapter(new ArrayAdapter<String>(this,
		//    			android.R.layout.simple_list_item_1_small, listEntries));
	}

}
