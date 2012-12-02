package sadna.ez_launch;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
//import com.kasperholtze.watchwidget.R;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.widget.RemoteViews;

public class NoamWidgetProvider extends AppWidgetProvider {
	

	 @Override
	    public void onUpdate( Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds )
	    {
	        RemoteViews remoteViews;
	        ComponentName watchWidget;
	        DateFormat format = SimpleDateFormat.getTimeInstance( SimpleDateFormat.MEDIUM, Locale.getDefault() );

	        remoteViews = new RemoteViews( context.getPackageName(), R.layout.main );
	        watchWidget = new ComponentName( context, NoamWidgetProvider.class );
	        remoteViews.setTextViewText( R.id.widget_textview, "Time = " + format.format( new Date()));
	        appWidgetManager.updateAppWidget( watchWidget, remoteViews );
	    }
}
