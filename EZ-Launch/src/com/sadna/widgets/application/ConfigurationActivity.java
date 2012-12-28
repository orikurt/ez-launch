package com.sadna.widgets.application;

import java.util.List;

import org.xmlpull.v1.XmlPullParser;


import com.sadna.interfaces.IDataManager;
import com.sadna.interfaces.Snapshot;
import com.sadna.widgets.application.ConfigurationActiviyOriginal.HelpButtonClick;


import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.AttributeSet;
import android.util.Xml;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;

public class ConfigurationActivity extends PreferenceActivity {
	
	private List<Snapshot> SnapShots;
	public IDataManager DM;
	
    private ListPreference loadSnapshot; 
    private Preference HelpPref; 
    private int widgetID;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Build GUI from resource
		addPreferencesFromResource(R.xml.preferences);
		ListView v = getListView();
		Button but = new Button(this);
        but.setText("Start");
        but.setOnClickListener(new OnClickListener() {
			public void onClick(final View Arg) {
				Intent resultValue = new Intent();
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
                setResult(RESULT_OK, resultValue);
				
				Intent updateWidget = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
				int[] ids = new int[] { widgetID };
                updateWidget.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
                sendBroadcast(updateWidget);
				
                finish();
			}
		});
		v.addFooterView(but);
        

		// Get the starting Intent
				Intent launchIntent = getIntent();
				Bundle extras = launchIntent.getExtras();
		        if (extras != null) {
		            widgetID = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
		        }
		        else
		        {
		        	finish();
		        }
		
		prepareLoadScreenshotPref();
		prepareHelpBtn();
	}

	private void prepareLoadScreenshotPref() {
		// TODO Auto-generated method stub
		loadSnapshot = (ListPreference)findPreference(Preferences.LOAD_SNAPSHOT);
		loadSnapshot.setKey(String.format(Preferences.LOAD_SNAPSHOT, widgetID));
		
		//Here you put the names of the screenshots
		
				/*
				SnapShots = DM.loadAllSnapshots();
				int SnapShotsLength = SnapShots.size();
				
				//Create the snapshot value arrays and fill them with data
				CharSequence[] Titles= new CharSequence[SnapShotsLength];
				CharSequence[] Values= new CharSequence[SnapShotsLength];
				
				for (int i = 0; i < SnapShotsLength; i++) {
					Values[i] = SnapShots.get(i).getSnapshot().getSnapshotName();
					Titles[i] = Values[i];
				}
				*/
		
		CharSequence[] Titles = new CharSequence[] {"1", "2", "3"};
		CharSequence[] Values = new CharSequence[] {"1", "2", "3"};
		
		loadSnapshot.setEntries(Titles);
		loadSnapshot.setEntryValues(Values);
		
	}
	
	
	private void prepareHelpBtn() {
		
		/*
		Resources resources = this.getResources();
		XmlPullParser parser = resources.getXml(R.xml.preferences);
		AttributeSet attributes = Xml.asAttributeSet(parser);
		HelpPref = new HelpPreference(this, attributes);
		*/
		
		HelpPref = findPreference(Preferences.HELP);
		HelpPref.setKey(String.format(Preferences.HELP, widgetID));
		
		HelpPref.setOnPreferenceClickListener(new HelpPreferenceClickListener(this));
				
	}
	
	public class HelpPreferenceClickListener implements OnPreferenceClickListener {
		
		private final Context fContext;
		
		public HelpPreferenceClickListener(Context context) 
		{
			fContext = context;
			
	        /*
	        setDialogLayoutResource(R.layout.numberpicker_dialog);
	        setDialogIcon(null);
	        */
	    }

		@Override
		public boolean onPreferenceClick(Preference preference) {
			AlertDialog alertDialog;
			alertDialog = new AlertDialog.Builder(fContext).create();
			alertDialog.setTitle(fContext.getString(R.string.help));
			alertDialog.setMessage(fContext.getString(R.string.helptext));
			alertDialog.setButton(fContext.getString(R.string.okbtn), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});
			alertDialog.show();
			return false;
		}
	}
		
		
		
		//HelpPreference.setOnPreferenceClickListener(new HelpButtonClick(this, true));
	}
	

	
