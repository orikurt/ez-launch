package com.sadna.widgets.application;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.sadna.data.DataManager;
import com.sadna.data.Snapshot;
import com.sadna.data.WidgetItemInfo;
import com.sadna.enums.ItemState;
import com.sadna.interfaces.IDataManager;
import com.sadna.interfaces.IWidgetItemInfo;
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
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputBinding;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

@SuppressLint("NewApi")
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
				
				// Start service
				Intent intent = new Intent("com.sadna.service.StatisticsService");  
			    startService(intent);
			    
			    // Start Widget
				Intent resultIntent = new Intent();
				resultIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
				setResult(RESULT_OK, resultIntent);
				Log.d(LOG_TAG, "widgetID=" + widgetID);
				Intent updateWidget = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
				int[] ids = new int[] { widgetID };
				updateWidget.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
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
		
		CharSequence[] Appnames;
		CharSequence[] Values;

		Snapshot Snapy = DM.getSelectedSnapshot();
		if (Snapy != null)
		{
		int i = 0;
		int numApps = Snapy.size();
		Appnames = new CharSequence[numApps];
		Values = new CharSequence[numApps]; 
		for (IWidgetItemInfo wInfo : Snapy) 
		{
			Appnames[i] = wInfo.getLabel();
			Values[i] = wInfo.getPackageName();
			i++;
		}
		}
		else
		{
			Appnames = new CharSequence[]{};
			Values = new CharSequence[]{};
		}

		FixPreference.setDefaultValue(Values);
		FixPreference.setEntries(Appnames);
		FixPreference.setEntryValues(Values);
		FixPreference.setOnPreferenceChangeListener(new OnFixPreferenceChangeListener());

	}

	private void prepareLoadScreenshotPref() {
		loadSnapshot = (ListPreference)findPreference(Preferences.LOAD_SNAPSHOT);
		loadSnapshot.setKey(String.format(Preferences.LOAD_SNAPSHOT, widgetID));
		setListPreferenceData(loadSnapshot);
		loadSnapshot.setOnPreferenceClickListener(new onLoadPreferenceClickListener());
	}


	private void setListPreferenceData(ListPreference loadSnapshot2) {
		//Here you put the names of the snapshot
		SnapShots = DM.loadAllSnapshots();
		int SnapShotsLength = (SnapShots != null) ? SnapShots.size() : 0;

		//Create the snapshot value arrays and fill them with data
		CharSequence[] Titles= new CharSequence[SnapShotsLength];
		CharSequence[] Values= new CharSequence[SnapShotsLength];

		for (int i = 0; i < SnapShotsLength; i++) {
			Values[i] = SnapShots.get(i).getSnapshotInfo().getSnapshotName();
			Titles[i] = SnapShots.get(i).getSnapshotInfo().getSnapshotName();
		}

		loadSnapshot.setEntries(Titles);
		loadSnapshot.setEntryValues(Values);

	}

	private void prepareAboutBtn() {
		AboutPref = findPreference("ABOUT");
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
		}

		public HelpPreferenceClickListener(Context context, Boolean about) 
		{
			fContext = context;
			fAbout  = about;
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
		
		private boolean saveSnapshotProcess(String value) 
		{	
			Snapshot snap = DM.getSelectedSnapshot();
			String oldName = snap.getSnapshotInfo().getSnapshotName();
			snap.getSnapshotInfo().setSnapshotName(value);
			if (SnapShots.contains(snap))
			{
				snap.getSnapshotInfo().setSnapshotName(oldName);
				return false;
			}
			DM.saveSnapshot(snap);
			return true;
		}
		
		@Override
		public boolean onPreferenceClick(Preference preference) {
			
			Context Cntxt = this.fContext;
			AlertDialog.Builder alert = new AlertDialog.Builder(Cntxt);

			alert.setTitle("Save Snapshot");
			alert.setMessage("Enter name to save:");

			
			// Set an EditText view to get user input 
			final EditText input = new EditText(Cntxt);
			alert.setView(input);

			alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
			  final String value = input.getText().toString();
			  saveSnapshotProcess(value);
				}
			});

			alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				  public void onClick(DialogInterface dialog, int whichButton) {
				  }
				});

			alert.show();
			return false;
		}
	}
	
	public class onLoadPreferenceClickListener implements OnPreferenceClickListener {

		@Override
		public boolean onPreferenceClick(Preference preference) {
			// TODO Auto-generated method stub
			setListPreferenceData(loadSnapshot);
			return false;
		}
	}
	
	public class OnFixPreferenceChangeListener implements OnPreferenceChangeListener{

		Set<String> packNames;
		@SuppressLint("NewApi")
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			
			packNames = FixPreference.getValues();
			Snapshot Snapy = DM.getSelectedSnapshot();
			 
			for (IWidgetItemInfo wInfo : Snapy) 
			{
				if (packNames.contains(wInfo.getPackageName()))
				{
					wInfo.setItemState(ItemState.MUST);
				}
			}

			return false;
		}
		
	}
}