package com.sadna.UI;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import java.util.List;


import com.sadna.data.DataManager;
import com.sadna.data.Snapshot;

import com.sadna.interfaces.IDataManager;
import com.sadna.service.StatisticsService;
import com.sadna.widgets.application.Preferences;
import com.sadna.widgets.application.R;
import com.sadna.widgets.application.R.string;
import com.sadna.widgets.application.R.xml;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.SwitchPreference;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

@SuppressLint("NewApi")
public class SettingsActivity extends PreferenceActivity {

	private List<Snapshot> snapShots;
	public IDataManager DM;

	//private ListPreference loadSnapshot; 
	//private ListPreference deleteSnapshot;
	private SwitchPreference ProfilingSwitch;
	private MultiSelectListPreference ProfilingPref;
	private Preference FixPreference;
	//private Preference NumberPreference;
	private Preference HelpPref;
	private Preference AboutPref;
	//private Preference SaveSnapshotPref;
	private int widgetID;
	
	private final String LOG_TAG = "SettingsActivity";
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Build GUI from resource
		addPreferencesFromResource(R.xml.preferences);
		
		// Add SettingsList button
		ListView v = getListView();
		Button goButton = new Button(this);
		goButton.setText("Go!");
		goButton.setOnClickListener(new OnClickListener() {
			public void onClick(final View Arg) {				
			    finish();
			}
		});
		v.addFooterView(goButton);
		
		
		// Prepare
		DM = new DataManager(this);
		snapShots = DM.loadAllSnapshots();
		
		/*prepareLoadScreenshotPref();
		prepareSaveSnapshotPref();
		prepareDeleteSnapshotPref();*/
		prepareSwitchPref();
		prepareProfPref();
		prepareFixPref();
		prepareNumPickrPref();
		prepareHelpBtn();
		prepareAboutBtn();
	}
	

	@Override
	public void onPause(){
		super.onPause();
		
		// Send update intent
		Intent updateService = new Intent(com.sadna.service.StatisticsService.SERVICE_UPDATE);
		Log.d(LOG_TAG, "Send SERVICE_UPDATE");
		sendBroadcast(updateService);
	}
	
	/************************* Preparing Functions ****************************/
	private void prepareSwitchPref() {
		ProfilingSwitch = (SwitchPreference) findPreference(Preferences.PROF_ENABLE);
		ProfilingSwitch.setOnPreferenceChangeListener(new onProfSwitchChangeListener());
	}
	
	private void prepareProfPref() {
		ProfilingPref = (MultiSelectListPreference) findPreference(Preferences.PROF);
		Calendar cal = Calendar.getInstance();
		CharSequence[] entries, entryValues;
		
		// Build Preference
		switch (cal.getFirstDayOfWeek()) {
		case Calendar.SATURDAY:
			entries = new CharSequence[]{
					getString(R.string.saturday),
					getString(R.string.sunday),
					getString(R.string.monday),
					getString(R.string.tuesday),
					getString(R.string.wednesday),
					getString(R.string.thursday),
					getString(R.string.friday)
					};
			entryValues = new CharSequence[]{
					String.valueOf(Calendar.SATURDAY),
					String.valueOf(Calendar.SUNDAY),
					String.valueOf(Calendar.MONDAY),
					String.valueOf(Calendar.TUESDAY),
					String.valueOf(Calendar.WEDNESDAY),
					String.valueOf(Calendar.THURSDAY),
					String.valueOf(Calendar.FRIDAY)
					};
			break;
			
		case Calendar.SUNDAY:
			entries = new CharSequence[]{
					getString(R.string.sunday),
					getString(R.string.monday),
					getString(R.string.tuesday),
					getString(R.string.wednesday),
					getString(R.string.thursday),
					getString(R.string.friday),
					getString(R.string.saturday)
					};
			entryValues = new CharSequence[]{
					String.valueOf(Calendar.SUNDAY),
					String.valueOf(Calendar.MONDAY),
					String.valueOf(Calendar.TUESDAY),
					String.valueOf(Calendar.WEDNESDAY),
					String.valueOf(Calendar.THURSDAY),
					String.valueOf(Calendar.FRIDAY),
					String.valueOf(Calendar.SATURDAY)
					};
			break;
			
		default:
			entries = new CharSequence[]{
					getString(R.string.monday),
					getString(R.string.tuesday),
					getString(R.string.wednesday),
					getString(R.string.thursday),
					getString(R.string.friday),
					getString(R.string.saturday),
					getString(R.string.sunday)
					};
			entryValues = new CharSequence[]{
				String.valueOf(Calendar.MONDAY),
				String.valueOf(Calendar.TUESDAY),
				String.valueOf(Calendar.WEDNESDAY),
				String.valueOf(Calendar.THURSDAY),
				String.valueOf(Calendar.FRIDAY),
				String.valueOf(Calendar.SATURDAY),
				String.valueOf(Calendar.SUNDAY)
				};
		}
		ProfilingPref.setEntries(entries);
		ProfilingPref.setEntryValues(entryValues);
		
		// Get working days from DB
		Set<String> values = new HashSet<String>();
		int[] workingDays = DM.getWorkingDays();
		for (int dayID : workingDays) {
			values.add(String.valueOf(dayID));
		}
		ProfilingPref.setValues(values);
		
		// Change listener
		ProfilingPref.setOnPreferenceChangeListener(new onProfDaysChangeListener());
	}

	
	/*private void prepareLoadScreenshotPref() {
		loadSnapshot = (ListPreference)findPreference(Preferences.LOAD_SNAPSHOT);
		loadSnapshot.setKey(String.format(Preferences.LOAD_SNAPSHOT, widgetID));
		setListPreferenceData(loadSnapshot);
		loadSnapshot.setOnPreferenceClickListener(new onLoadPreferenceClickListener(true));
		loadSnapshot.setOnPreferenceChangeListener(new onLoadPreferenceChangeListener(true));
	}

	private void prepareDeleteSnapshotPref() {
		
		deleteSnapshot = (ListPreference)findPreference(Preferences.DELETE);
		deleteSnapshot.setKey(String.format(Preferences.DELETE, widgetID));
		// TODO Auto-generated method stub
		setListPreferenceData(deleteSnapshot);
		deleteSnapshot.setOnPreferenceClickListener(new onLoadPreferenceClickListener(false));
		deleteSnapshot.setOnPreferenceChangeListener(new onLoadPreferenceChangeListener(false));
	}

	private void prepareSaveSnapshotPref() {

		SaveSnapshotPref = findPreference(Preferences.SAVE);
		SaveSnapshotPref.setKey(String.format(Preferences.SAVE, widgetID));
		SaveSnapshotPref.setOnPreferenceClickListener(new SavePreferenceClickListener(this));
	}*/
	
	@SuppressLint("NewApi")
	private void prepareFixPref() {
		FixPreference = findPreference(Preferences.FIX);
		FixPreference.setKey(String.format(Preferences.FIX, widgetID));
		FixPreference.setOnPreferenceClickListener(new onFixPreferenceClickListener());
	}
	
	@SuppressLint("NewApi")
	private void prepareNumPickrPref() {
		FixPreference = findPreference(Preferences.ICONNUMBER);
		FixPreference.setKey(String.format(Preferences.ICONNUMBER, widgetID));
		FixPreference.setOnPreferenceClickListener(new onNumPickrClickListener());
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
	
	private void setListPreferenceData(ListPreference Pref) {
		// TODO Auto-generated method stub
		//Here you put the names of the screenshots
		snapShots = DM.loadAllSnapshots();
		
		if (snapShots == null) {
			// Should consider display a message to the user... "no snapshots available"
			Pref.setEntries(new CharSequence[0]);
			Pref.setEntryValues(new CharSequence[0]);
			return;
		}

		
		//Create the snapshot value arrays and fill them with data

		List<CharSequence> Titles= new ArrayList<CharSequence>();
		
		for (Snapshot snap : snapShots) {
			if (!snap.getSnapshotInfo().getSnapshotName().equals(StatisticsService.RESERVED_SNAPSHOT)) {
				Titles.add(snap.getSnapshotInfo().getSnapshotName());
			}
		}
		Pref.setEntries(Titles.toArray(new CharSequence[Titles.size()]));
		Pref.setEntryValues(Titles.toArray(new CharSequence[Titles.size()]));

	}

	/************************* End of Preparing Functions ****************************/

	/*public class ProfilingPrefClickListener implements OnPreferenceClickListener {

		@Override
		public boolean onPreferenceClick(Preference preference) {
			Intent NumPickrIntent = new Intent(preference.getContext(), SettingsDaysPickrDialog.class);
			startActivity(NumPickrIntent);
			return false;
		}
	}*/
	
	public class onProfSwitchChangeListener implements OnPreferenceChangeListener {

		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			DM.setProfolingState((Boolean) newValue);
			ProfilingPref.setEnabled((Boolean) newValue);
			return true;
		}
	}
	public class onProfDaysChangeListener implements OnPreferenceChangeListener {

		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			
			if (newValue.getClass() != HashSet.class) {
				return false;
			}
			
			int[] temp = new int[((HashSet<String>)newValue).size()];
			int i = 0;
			for (String day : (HashSet<String>)newValue) {
				temp[i] = Integer.parseInt(day);
				i++;
			}
			DM.setWorkingDays(temp);
			return true;
		}

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
	
	public static class SetNumberOf extends DialogFragment {
	    @Override
	    public Dialog onCreateDialog(Bundle savedInstanceState) {
	        // Use the Builder class for convenient dialog construction
	        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	        builder.setMessage("test")
	               .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
	                   public void onClick(DialogInterface dialog, int id) {
	                       // FIRE ZE MISSILES!
	                   }
	               })
	               .setNegativeButton("No", new DialogInterface.OnClickListener() {
	                   public void onClick(DialogInterface dialog, int id) {
	                       // User cancelled the dialog
	                   }
	               });
	        // Create the AlertDialog object and return it
	        return builder.create();
	    }
	}
	
	
	/*public class SavePreferenceClickListener implements OnPreferenceClickListener {

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
			if (snapShots.contains(snap))
			{
				snap.getSnapshotInfo().setSnapshotName(oldName);
				return false;
			}
			DM.saveSnapshot(snap);
			setListPreferenceData(deleteSnapshot);
			setListPreferenceData(loadSnapshot);
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
			AlertDialog dialog = alert.create();
			dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
			dialog.show();
			//alert.show();
			
			return false;
		}
	}
	
	public class onLoadPreferenceClickListener implements OnPreferenceClickListener {
		boolean isLoad;
		public onLoadPreferenceClickListener(boolean bool)
		{
			isLoad = bool;
		}

		@Override
		public boolean onPreferenceClick(Preference preference) {
			if (isLoad)
			{
			setListPreferenceData(loadSnapshot);
			}
			else 
			{
				setListPreferenceData(deleteSnapshot);
			}
			return true;
		}
	}
	
	public class onLoadPreferenceChangeListener implements OnPreferenceChangeListener {

		boolean isLoad;
		
		public onLoadPreferenceChangeListener(boolean bool)
		{
			isLoad = bool;
		}
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			
			String snapName = newValue.toString();
			if (isLoad)
			{
				Snapshot SnapToLoad = DM.loadSnapshot(snapName);
				DM.setSelectedSnapshot(SnapToLoad);
			}
			else
			{
				DM.deleteSnapshot(snapName);
			}
			return false;
		}
	}*/
	
	public class onFixPreferenceClickListener implements OnPreferenceClickListener {

		@Override
		public boolean onPreferenceClick(Preference preference) {
			Intent fixIntent = new Intent(preference.getContext(), SettingsListActivity.class);
			startActivity(fixIntent);
			return false;
		}
	}
	
	public class onNumPickrClickListener implements OnPreferenceClickListener {

		@Override
		public boolean onPreferenceClick(Preference preference) {
			Intent NumPickrIntent = new Intent(preference.getContext(), SettingsNumPickrDialog.class);
			startActivity(NumPickrIntent);
			return false;
		}
	}
}



