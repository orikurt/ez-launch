package com.sadna.ui;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.SwitchPreference;
import android.util.Log;

import com.sadna.data.DataManager;
import com.sadna.data.Snapshot;
import com.sadna.interfaces.IDataManager;
import com.sadna.service.StatisticsService;
import com.sadna.widgets.application.Preferences;
import com.sadna.widgets.application.R;

public class SettingsActivity extends PreferenceActivity {

	public static final String IS_FIXED = "isFixed";
	private List<Snapshot> snapShots;
	public IDataManager DM;

	private SwitchPreference ProfilingSwitch;
	private MultiSelectListPreference ProfilingDays;
	private Preference ProfilingHours;
	private Preference FixPreference;
	private Preference NumPicker;
	private Preference Statistics;
	private Preference HelpPref;
	private Preference AboutPref;
	private int widgetID;
	
	private final String LOG_TAG = "SettingsActivity";
	
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Build GUI from resource
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB){
			return;
		}
		addPreferencesFromResource(R.xml.preferences);
		
		// Prepare
		DM = new DataManager(this);
		snapShots = DM.loadAllSnapshots();

		prepareSwitchPref();
		prepareProfDays();
		prepareProfHours();
		prepareFixPref();
		prepareNumPickrPref();
		prepareStatitics();
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
	@SuppressWarnings("deprecation")
	private void prepareSwitchPref() {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH){
		}else{
			ProfilingSwitch = (SwitchPreference) findPreference(Preferences.PROF_ENABLE);
			ProfilingSwitch.setChecked(DM.getProfolingState());
			ProfilingSwitch.setOnPreferenceChangeListener(new onProfSwitchChangeListener());
		}
	}
	
	@SuppressWarnings("deprecation")
	private void prepareProfDays() {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB){
			return;
		}
		ProfilingDays = (MultiSelectListPreference) findPreference(Preferences.PROF_DAYS);
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
		ProfilingDays.setEntries(entries);
		ProfilingDays.setEntryValues(entryValues);
		
		// Get working days from DB
		Set<String> values = new HashSet<String>();
		int[] workingDays = DM.getWorkingDays();
		for (int dayID : workingDays) {
			values.add(String.valueOf(dayID));
		}
		ProfilingDays.setValues(values);
		
		// Change listener
		ProfilingDays.setOnPreferenceChangeListener(new onProfDaysChangeListener());
	}

	@SuppressWarnings("deprecation")
	private void prepareProfHours() {
		ProfilingHours = findPreference(Preferences.PROF_HOURS);
		ProfilingHours.setOnPreferenceClickListener(new onHoursPickrClickListener());
	}

	@SuppressWarnings("deprecation")
	private void prepareFixPref() {
		FixPreference = findPreference(Preferences.FIX);
		FixPreference.setKey(String.format(Preferences.FIX, widgetID));
		FixPreference.setOnPreferenceClickListener(new onFixPreferenceClickListener());
	}
	@SuppressWarnings("deprecation")
	private void prepareStatitics() {
		Statistics = findPreference(Preferences.STATISTICS);
		Statistics.setKey(String.format(Preferences.STATISTICS, widgetID));
		Statistics.setOnPreferenceClickListener(new onStatisticsPreferenceClickListener());
	}
	
	@SuppressWarnings("deprecation")
	private void prepareNumPickrPref() {
		NumPicker = findPreference(Preferences.ICONNUMBER);
		NumPicker.setKey(String.format(Preferences.ICONNUMBER, widgetID));
		NumPicker.setOnPreferenceClickListener(new onNumPickrClickListener());
	}
	
	@SuppressWarnings("deprecation")
	private void prepareAboutBtn() {
		AboutPref = findPreference("ABOUT");
		AboutPref.setOnPreferenceClickListener(new HelpPreferenceClickListener(this, true));
	}
	
	@SuppressWarnings("deprecation")
	private void prepareHelpBtn() {

		HelpPref = findPreference(Preferences.HELP);
		HelpPref.setKey(String.format(Preferences.HELP, widgetID));
		HelpPref.setOnPreferenceClickListener(new HelpPreferenceClickListener(this));
	}
	
	@SuppressWarnings("unused")
	private void setListPreferenceData(ListPreference Pref) {
		//Here you put the names of the snaphots
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
	
	@SuppressLint("NewApi")
	public class onProfSwitchChangeListener implements OnPreferenceChangeListener {

		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			DM.setProfolingState((Boolean) newValue);
			ProfilingDays.setEnabled((Boolean) newValue);
			ProfilingHours.setEnabled((Boolean) newValue);
			return true;
		}
	}
	
	public class onHoursPickrClickListener implements OnPreferenceClickListener {

		@Override
		public boolean onPreferenceClick(Preference preference) {
			Intent HoursPickrIntent = new Intent(preference.getContext(), SettingsHoursPickrDialog.class);
			startActivity(HoursPickrIntent);
			return false;
		}
	}
	
	public class onProfDaysChangeListener implements OnPreferenceChangeListener {

		@SuppressWarnings("unchecked")
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

		@SuppressWarnings("deprecation")
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
	
	public class onFixPreferenceClickListener implements OnPreferenceClickListener {

		@Override
		public boolean onPreferenceClick(Preference preference) {
			Intent fixIntent = new Intent(preference.getContext(), SettingsListActivity.class);
			fixIntent.putExtra(IS_FIXED, true);
			startActivity(fixIntent);
			return false;
		}
	}
	
	public class onStatisticsPreferenceClickListener implements OnPreferenceClickListener {

		

		@Override
		public boolean onPreferenceClick(Preference preference) {
			Intent statisticsIntent = new Intent(preference.getContext(), SettingsListActivity.class);
			statisticsIntent.putExtra(IS_FIXED, false);
			startActivity(statisticsIntent);
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