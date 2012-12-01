package sadna.ez_launch;

import java.util.ArrayList;
import java.util.List;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.RemoteViews;

public class EZLaunchWidgetProvider extends AppWidgetProvider {
	@Override
	public void onUpdate( Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds )
	{
		
		 // update each of the app widgets with the remote adapter
	    for (int i = 0; i < appWidgetIds.length; ++i) {
	        
	        // Set up the intent that starts the StackViewService, which will
	        // provide the views for this collection.
	        Intent intent = new Intent(context, EZLaunchWidgetService.class);
	        // Add the app widget ID to the intent extras.
	        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
	        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
	        // Instantiate the RemoteViews object for the App Widget layout.
	        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.ezlaunch_app_widget_layout);
	        // Set up the RemoteViews object to use a RemoteViews adapter. 
	        // This adapter connects
	        // to a RemoteViewsService  through the specified intent.
	        // This is how you populate the data.
	        rv.setRemoteAdapter(appWidgetIds[i], R.id.gridview, intent);
	        //rv.setRemoteAdapter(appWidgetIds[i], intent);
	        
	        // The empty view is displayed when the collection has no items. 
	        // It should be in the same layout used to instantiate the RemoteViews
	        // object above.
	        //rv.setEmptyView(R.id.stack_view, R.id.empty_view);

	        //
	        // Do additional processing specific to this app widget...
	        //
	        
	        appWidgetManager.updateAppWidget(appWidgetIds[i], rv);   
	    }
	    super.onUpdate(context, appWidgetManager, appWidgetIds);
		
		//GetInstalledApplicationsList(context);

	}

/*
	private void GetInstalledApplicationsList(final Context context)
	{

		
		final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		final List<ResolveInfo> pkgAppsList = context.getPackageManager().queryIntentActivities( mainIntent, 0);
		
		LayoutInflater li = (LayoutInflater)context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );

		final List<Shortcut> mList= new ArrayList<Shortcut>();
		for (ResolveInfo resolveInfo : pkgAppsList) {

			Shortcut a = new Shortcut((resolveInfo.activityInfo.loadIcon(context.getPackageManager())),
					resolveInfo.activityInfo.packageName);
			mList.add(a);

		}

	
		

		//setContentView(R.layout.secondscreen);

		GridView gridview = (GridView)findViewById(R.id.gridview);
		gridview.setAdapter(new ImageAdapter(context,mList,li));

		gridview.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {


				String ToLaunch = mList.get(position).getLabel();
				//Intent intent = new Intent(Intent.ACTION_MAIN);
				Intent LaunchIntent = context.getPackageManager().getLaunchIntentForPackage(ToLaunch);
				startActivity(LaunchIntent);
				//intent.setComponent(new ComponentName("com.android.calculator2", "com.android.calculator2.Calculator"));

				//startActivity(intent);
				//Toast.makeText(MainActivity.this, "" + position, Toast.LENGTH_SHORT).show();
			}


		});



	}

*/

}
