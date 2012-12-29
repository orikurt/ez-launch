package com.sadna.widgets.application;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.xmlpull.v1.XmlPullParser;


import com.sadna.interfaces.IDataManager;
import com.android.data.Snapshot;
//import com.sadna.widgets.application.ConfigurationActiviyOriginal.HelpButtonClick;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
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
	private MultiSelectListPreference FixPreference;
    private Preference HelpPref;
    private Preference AboutPref; 
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
				Intent resultIntent = new Intent();
                resultIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
                setResult(RESULT_OK, resultIntent);
				
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
		prepareAboutBtn();
		prepareFixPref();
	}

	@SuppressLint("NewApi")
	private void prepareFixPref() {
		
		FixPreference = (MultiSelectListPreference)findPreference(Preferences.FIX);
		FixPreference.setKey(String.format(Preferences.FIX, widgetID));
//		Context c = getApplicationContext();
		
		
		PackageManager pm = getPackageManager();
		List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
		int i = 0;
		int numApps = packages.size();
		CharSequence[] Appnames = new CharSequence[numApps];
		CharSequence[] Values = new CharSequence[numApps]; 
		for (ApplicationInfo applicationInfo : packages) 
		{
			Appnames[i] = applicationInfo.loadLabel(pm);
			Values[i] = Appnames[i];
			i++;
		}
		
		//CharSequence[] Appnames = new CharSequence[] {"1", "2", "3"};
		//CharSequence[] Values = new CharSequence[] {"1", "2", "3"};
		FixPreference.setDefaultValue(Values);
		FixPreference.setEntries(Appnames);
		FixPreference.setEntryValues(Values);
		
		/*
		FixPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
	        public boolean onPreferenceChange(Preference preference, Object o) {

	            HashSet hashSet = (HashSet) o;
	            Iterator stringIterator = hashSet.iterator();
	            boolean[] states = {false, false, false};
	            String prefString;

	            while (stringIterator.hasNext()) {

	                prefString = (String) stringIterator.next();

	                if (prefString == null)
	                    continue;

	                if (prefString.compareTo("1") == 0)
	                    states[0] = true;
	                else if (prefString.compareTo("2") == 0)
	                    states[1] = true;
	                else if (prefString.compareTo("3") == 0)
	                    states[2] = true;

	            }

	            PreferenceManager
	                    .getDefaultSharedPreferences(getApplicationContext())
	                    .edit()
	                    .putBoolean("1", states[0])
	                    .putBoolean("2", states[1])
	                    .putBoolean("3", states[2])
	                    .commit();

	            return true;
	        }
	    });

		*/
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


	private void prepareAboutBtn() {
		// TODO Auto-generated method stub
		AboutPref = findPreference("ABOUT");
		//AboutPref.setKey(String.format(Preferences.ABOUT, widgetID));
		AboutPref.setOnPreferenceClickListener(new HelpPreferenceClickListener(this, true));
	}

	
	private void prepareHelpBtn() {
				
		HelpPref = findPreference(Preferences.HELP);
		HelpPref.setKey(String.format(Preferences.HELP, widgetID));
		
		HelpPref.setOnPreferenceClickListener(new HelpPreferenceClickListener(this));
				
	}
	
	public class HelpPreferenceClickListener implements OnPreferenceClickListener {
		
		private final Context fContext;
		private Boolean fAbout = false;
		
		public HelpPreferenceClickListener(Context context) 
		{
			fContext = context;
			
	        /*
	        setDialogLayoutResource(R.layout.numberpicker_dialog);
	        setDialogIcon(null);
	        */
	    }

		public HelpPreferenceClickListener(Context context, Boolean about) 
		{
			fContext = context;
			fAbout  = about;
	        /*
	        setDialogLayoutResource(R.layout.numberpicker_dialog);
	        setDialogIcon(null);
	        */
	    }
		
		@Override
		public boolean onPreferenceClick(Preference preference) {
			AlertDialog alertDialog;
			alertDialog = new AlertDialog.Builder(fContext).create();
			if (!fAbout)
			{
			alertDialog.setTitle(fContext.getString(R.string.help));
			alertDialog.setMessage(fContext.getString(R.string.helptext));
			}
			else
			{
				alertDialog.setTitle(fContext.getString(R.string.about));
				alertDialog.setMessage(fContext.getString(R.string.abouttext));
					
			}
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
	

	
