package sadna.ez_launch;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

public class MainActivity extends Activity {

	StatisticsService statisticsService;
	StatisticsUpdateReceiver statisticsUpdateReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Start startService
		startService(new Intent(this, StatisticsService.class));
		
		// Register to StatisticsService.UPDATE_INTENT;
		if (statisticsUpdateReceiver == null) statisticsUpdateReceiver = new StatisticsUpdateReceiver();
		registerReceiver(statisticsUpdateReceiver, new IntentFilter(StatisticsService.UPDATE_INTENT));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	public class StatisticsUpdateReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(StatisticsService.UPDATE_INTENT)) {

				// Get StatisticsService instance
				do {
					statisticsService = StatisticsService.getInstance();
				}
				while (statisticsService == null);

				// Deploy on screen all running applications Shortcuts
				LayoutInflater li = getLayoutInflater();
				setContentView(R.layout.secondscreen);
				GridView gridview = (GridView) findViewById(R.id.gridview);
				gridview.setAdapter(new ImageAdapter(getApplicationContext(), statisticsService.mList, li));
				gridview.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

						String ToLaunch = statisticsService.mList.get(position).getLabel();
						Intent LaunchIntent = getPackageManager().getLaunchIntentForPackage(ToLaunch);
						startActivity(LaunchIntent);
					}
				});
			}
		}
	}
}