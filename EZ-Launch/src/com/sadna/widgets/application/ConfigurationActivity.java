package com.sadna.widgets.application;

import java.util.ArrayList;
import java.util.List;
import com.sadna.interfaces.IDataManager;
import com.sadna.interfaces.IWidgetItemInfo;
import com.android.data.DataManager;
import com.android.data.Snapshot;
import com.android.data.WidgetItemInfo;
//import com.sadna.widgets.application.ConfigurationActiviyOriginal.HelpButtonClick;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.Log;
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
	private Preference SaveSnapshotPref;
	private int widgetID;
	
private String LOG_TAG = "ConfigurationActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Build GUI from resource
		addPreferencesFromResource(R.xml.preferences);
		ListView v = getListView();
		Button startButton = new Button(this);
		startButton.setText("Start");
		startButton.setOnClickListener(new OnClickListener() {
			public void onClick(final View Arg) {
				Intent resultIntent = new Intent();
				resultIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
				setResult(RESULT_OK, resultIntent);
				Log.d(LOG_TAG, "widgetID=" + widgetID);
				Intent updateWidget = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
				int[] ids = new int[] { widgetID };
				
				updateWidget.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);

				// Generate very first snapshot
				/*Date currDate = new Date();
				ISnapshotInfo snapshotInfo = new SnapshotInfo(currDate.toString(), currDate);
				Snapshot currSnapshot = new Snapshot(snapshotInfo, getInstalledAppsInfo());

				updateWidget.putExtra(StatisticsService.NEW_SNAPSHOT, currSnapshot);*/
				sendBroadcast(updateWidget);

				finish();
			}
		});
		v.addFooterView(startButton);


		// Get the starting Intent
		Intent launchIntent = getIntent();
		Bundle extras = launchIntent.getExtras();
		if (extras != null) {
			widgetID = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
			   // Cancel by default
            Intent cancelResultValue = new Intent();
            cancelResultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
            setResult(RESULT_CANCELED, cancelResultValue);
		}
		else
		{
			finish();
		}

		DM = new DataManager(this);
		SnapShots = DM.loadAllSnapshots();
		
		prepareLoadScreenshotPref();
		prepareSaveSnapshotPref();
		prepareHelpBtn();
		prepareAboutBtn();
		prepareFixPref();
	}

	private List<IWidgetItemInfo> getInstalledAppsInfo() {
		List<IWidgetItemInfo> result = new ArrayList<IWidgetItemInfo>();

		Context context = getApplicationContext();

		final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		final List<ResolveInfo> pkgAppsList = context.getPackageManager().queryIntentActivities(mainIntent, 0);
		PackageManager packageManager = getPackageManager();
		for (ResolveInfo resolveInfo : pkgAppsList) {

			String itemLabel = resolveInfo.loadLabel(packageManager).toString();
			String itemPkgName = resolveInfo.resolvePackageName;
			IWidgetItemInfo itemInfo = new WidgetItemInfo(itemPkgName, itemLabel);
			result.add(itemInfo);
		}

		return result;
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

		int SnapShotsLength = (SnapShots != null) ? SnapShots.size() : 0;

		//Create the snapshot value arrays and fill them with data
		CharSequence[] Titles= new CharSequence[SnapShotsLength];
		CharSequence[] Values= new CharSequence[SnapShotsLength];

		for (int i = 0; i < SnapShotsLength; i++) {
			Values[i] = SnapShots.get(i).getSnapshotInfo().getSnapshotName();
			Titles[i] = SnapShots.get(i).getSnapshotInfo().getSnapshotName();
		}


		//CharSequence[] Titles = new CharSequence[] {"1", "2", "3"};
		//CharSequence[] Values = new CharSequence[] {"1", "2", "3"};

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
	

	private void prepareSaveSnapshotPref() {

		SaveSnapshotPref = findPreference(Preferences.SAVE);
		SaveSnapshotPref.setKey(String.format(Preferences.SAVE, widgetID));
		SaveSnapshotPref.setOnPreferenceClickListener(new SavePreferenceClickListener(this));
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
	
	
	public class SavePreferenceClickListener implements OnPreferenceClickListener {

		private final Context fContext;

		public SavePreferenceClickListener(Context context) 
		{
			fContext = context;
		}
		@Override
		public boolean onPreferenceClick(Preference preference) {
			AlertDialog alertDialog;
			alertDialog = new AlertDialog.Builder(fContext).create();
			alertDialog.setTitle(fContext.getString(R.string.save));
			alertDialog.setMessage(fContext.getString(R.string.saveAsk));
			alertDialog.setButton(fContext.getString(R.string.okbtn), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});
			alertDialog.setButton2(fContext.getString(R.string.cancelbtn), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});
			alertDialog.show();
			return false;
		}
	}
}



