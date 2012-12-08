package sadna.ez_launch;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class StatisticsUpdateReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(StatisticsService.UPDATE_INTENT)) {

			Bundle extras = intent.getExtras();
			if (extras != null)
			{
				//final TextView textViewToChange = (TextView) findViewById(R.id.textView1);
				//textViewToChange.setMovementMethod(new ScrollingMovementMethod());
				int data = extras.getInt("data");
				String log = String.format("Data is %d", data);
				//textViewToChange.setText(log);
			}
		}
	}
}