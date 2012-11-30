package sadna.ez_launch;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    

//    @SuppressWarnings("unchecked")
//    private void updateTaskList() {
//    	/* Grab the Systems IActivityManager. */
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
//    }

}
