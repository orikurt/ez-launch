package com.sadna.widgets.application;

import java.util.List;
import java.util.Set;

import com.sadna.data.DataManager;
import com.sadna.data.Snapshot;
import com.sadna.enums.ItemState;
import com.sadna.interfaces.IDataManager;
import com.sadna.interfaces.IWidgetItemInfo;
import com.sadna.widgets.application.ConfigurationActivity.OnFixPreferenceChangeListener;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

@SuppressLint("NewApi")
public class SettingsActivity extends PreferenceActivity {

	private List<Snapshot> SnapShots;
	public IDataManager DM;

	private ListPreference loadSnapshot; 
	private MultiSelectListPreference FixPreference;
	private Preference HelpPref;
	private Preference AboutPref;
	private Preference SaveSnapshotPref;
	private int widgetID;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Build GUI from resource
		addPreferencesFromResource(R.xml.preferences);
		
		// Add SettingsList button
		ListView v = getListView();
		Button settingsListButton = new Button(this);
		settingsListButton.setText("Save");
		settingsListButton.setOnClickListener(new OnClickListener() {
			public void onClick(final View Arg) {
			    
			    // Start SettingsList
				/*Intent launch = new Intent(getApplicationContext(), SettingsListActivity.class);
				startActivity(launch);*/
				finish();
			}
		});
		v.addFooterView(settingsListButton);
		
		
		// Prepare
		DM = new DataManager(this);
		SnapShots = DM.loadAllSnapshots();
		
		prepareLoadScreenshotPref();
		prepareSaveSnapshotPref();
		prepareHelpBtn();
		prepareAboutBtn();
		prepareFixPref();
	}

	@SuppressLint("NewApi")
	private void prepareFixPref() {

		FixPreference = (MultiSelectListPreference)findPreference(Preferences.FIX);
		FixPreference.setKey(String.format(Preferences.FIX, widgetID));
		
		CharSequence[] Appnames;
		CharSequence[] Values;
		//		Context c = getApplicationContext();

		//PackageManager pm = getPackageManager();
		//List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
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
		//CharSequence[] Appnames = new CharSequence[] {"1", "2", "3"};
		//CharSequence[] Values = new CharSequence[] {"1", "2", "3"};
		FixPreference.setDefaultValue(Values);
		FixPreference.setEntries(Appnames);
		FixPreference.setEntryValues(Values);
		FixPreference.setOnPreferenceChangeListener(new OnFixPreferenceChangeListener());
	}

	private void prepareLoadScreenshotPref() {
		// TODO Auto-generated method stub
		loadSnapshot = (ListPreference)findPreference(Preferences.LOAD_SNAPSHOT);
		loadSnapshot.setKey(String.format(Preferences.LOAD_SNAPSHOT, widgetID));
		setListPreferenceData(loadSnapshot);
		loadSnapshot.setOnPreferenceClickListener(new onLoadPreferenceClickListener());
	}


	private void setListPreferenceData(ListPreference loadSnapshot2) {
		// TODO Auto-generated method stub
		//Here you put the names of the screenshots
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
			  // Do something with value!

			alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				  public void onClick(DialogInterface dialog, int whichButton) {
				    // Canceled.
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
			
			//CharSequence[] Appnames = new CharSequence[numApps];
			//CharSequence[] Values = new CharSequence[numApps]; 
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



