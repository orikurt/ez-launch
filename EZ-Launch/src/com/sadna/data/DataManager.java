package com.sadna.data;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;

import com.sadna.enums.ItemState;
import com.sadna.interfaces.IDataManager;
import com.sadna.interfaces.ISnapshotInfo;
import com.sadna.interfaces.IWidgetItemInfo;
import com.sadna.service.StatisticsService;

public class DataManager extends SQLiteOpenHelper implements IDataManager {

	private static final String SETTINGS_WORKING_HOURS = "SettingsWorkingHours";

	private static final String SETTINGS_WORKING_DAYS = "SettingsWorkingDays";

	private static final String SETTINGS_PROFILING_STATE = "SettingsProfilingState";

	private static final String DEFAULT_LAUNCHER_FALLBACK = "com.sec.android.app.launcher";

	private static final String BAD_LAUNCHER = "BadDefaultSnapshot";

	private static final String SETTINGS_DEFAULT_LAUNCHER = "SettingsDefaultLauncher";

	private static final int APPLICATION_LIMIT_DEFUALT = 16;

	private static final String APPLICATION_SHARED_PREFRENCES = "ApplicationSharedPrefrences";

	//private static final String BAD_SNAPSHOT = "Bad_SnapshotXXXERROR";

	public static final String DATE_FORMAT = "yyMMddHHmmssZ";

	// All Static variables
	// Database Name
	private static final String DATABASE_NAME = "EZ_Launch_DB";

	// Database Version
	private static final int DATABASE_VERSION = 14;

	// Snapshot info table name
	private static final String TABLE_SNAPSHOT_INFO = "snapshotInfoTable";

	// Snapshot info table columns
	private static final String KEY_SNAPSHOT_NAME = "snapshotName";
	private static final String COLUMN_SNAPSHOT_LAST_DATE = "lastEdited";

	// Widget info table name
	private static final String TABLE_WIDGET_INFO = "widgetInfoTable";

	// Widget info table columns
	private static final String KEY_WIDGET_NAME = "packageName";
	private static final String COLUMN_WIDGET_LABEL = "widgetInfo";
	private static final String COLUMN_WIDGET_LAST_DATE = "widgetLastUsed";


	// Widget info to Snapshot table
	private static final String TABLE_WIDGET_TO_SNAPSHOT = "widgetToSnapshotTable";

	// Widget info to Snapshot columns 
	private static final String KEY_WIDGET_REF = "packageNameREF";
	private static final String KEY_SNAPSHOT_REF = "snapshotNameREF";
	private static final String COLUMN_WIDGET_SCORE = "widgetScore";
	private static final String COLUMN_WIDGET_STATE = "widgetState";

	// Shared Preferences
	private static SharedPreferences sharedPreferences;
	//private static final String SELECTED_SNAPSHOT = "SelectedSnapshot";
	private static final String SELECTED_APPLICATION_LIMIT = "SelectedApplicationLimit";

	private static Snapshot selectedSnapshot = null;

	private Context mContext;

	private ApplicationListCache appListCache = null;

	private static Object snapshotIteratorSyncObject = new Object();

	public static Date lastWidgerUpdate;

	public DataManager(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		mContext = context;
		sharedPreferences = context.getSharedPreferences(APPLICATION_SHARED_PREFRENCES, Context.MODE_PRIVATE);
	}
	
	public static void SortSnapshot(Snapshot snapshot){
		synchronized (snapshotIteratorSyncObject) {
			Collections.sort(snapshot);
		}
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String CREATE_SNAPSHOT_INFO_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_SNAPSHOT_INFO + "("
				+ KEY_SNAPSHOT_NAME + " TEXT PRIMARY KEY," + 
				COLUMN_SNAPSHOT_LAST_DATE + " TEXT DEFAULT CURRENT_TIMESTAMP " + ");";
		db.execSQL(CREATE_SNAPSHOT_INFO_TABLE);

		String CREATE_WIDGET_INFO_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_WIDGET_INFO + "("
				+ KEY_WIDGET_NAME + " TEXT PRIMARY KEY," + 
				COLUMN_WIDGET_LABEL + " TEXT," + 
				COLUMN_WIDGET_LAST_DATE + " TEXT DEFAULT CURRENT_TIMESTAMP "+ ");";
		db.execSQL(CREATE_WIDGET_INFO_TABLE);

		String CREATE_WIDGET_TO_SNAPSHOT_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_WIDGET_TO_SNAPSHOT + "("
				+ KEY_WIDGET_REF + " TEXT (35) NOT NULL," + 
				KEY_SNAPSHOT_REF + " TEXT (35) NOT NULL, " + 
				COLUMN_WIDGET_SCORE+ " REAL NOT NULL DEFAULT '1'," +
				COLUMN_WIDGET_STATE + " TEXT (10),"+
				"PRIMARY KEY (" + KEY_WIDGET_REF + ", "  + KEY_SNAPSHOT_REF +  " ), "
				+ "FOREIGN KEY(" + KEY_WIDGET_REF + ") REFERENCES " + TABLE_WIDGET_INFO + "(" + KEY_WIDGET_NAME +") ON UPDATE CASCADE ON DELETE CASCADE,"
				+ "FOREIGN KEY(" + KEY_SNAPSHOT_REF + ") REFERENCES " + TABLE_SNAPSHOT_INFO + "(" + KEY_SNAPSHOT_NAME +") ON UPDATE CASCADE ON DELETE CASCADE" +");";
		db.execSQL(CREATE_WIDGET_TO_SNAPSHOT_TABLE);
	}

	// Upgrading database
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_SNAPSHOT_INFO);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_WIDGET_INFO);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_WIDGET_TO_SNAPSHOT);
		// Create tables again
		onCreate(db);
	}

	@Override
	public boolean saveAllSnapshots(List<Snapshot> lst) {
		// I know this is not ideal but it will have to do for now - the list size shouldn't exceed 6
		for (Snapshot snapshot : lst) {
			saveSnapshot(snapshot);
		}
		return true;
	}

	@Override
	public boolean saveSnapshot(Snapshot snap) {
		SQLiteDatabase db = getWritableDatabase();
		db.beginTransaction();
		synchronized (snapshotIteratorSyncObject){
			for (IWidgetItemInfo iWidgetItemInfo : snap) {
				saveWidgetInfo(iWidgetItemInfo,db);
			}
			ContentValues iSSQCV = new ContentValues(2);
			iSSQCV.put(KEY_SNAPSHOT_NAME, snap.getSnapshotInfo().getSnapshotName());
			iSSQCV.put(COLUMN_SNAPSHOT_LAST_DATE, snap.getSnapshotInfo().getLastEditedFormated());
			db.insertWithOnConflict(TABLE_SNAPSHOT_INFO, null, iSSQCV, SQLiteDatabase.CONFLICT_REPLACE);

			// Update relations - the current Implementation is not perfect as it sends many queries to the DB instead of 1 query
			for (IWidgetItemInfo widg : snap) {
				ContentValues iSTWQ = new ContentValues(4);
				iSTWQ.put(KEY_WIDGET_REF, widg.getPackageName());
				iSTWQ.put(KEY_SNAPSHOT_REF, snap.getSnapshotInfo().getSnapshotName());
				iSTWQ.put(COLUMN_WIDGET_SCORE, widg.getScore());
				iSTWQ.put(COLUMN_WIDGET_STATE, widg.getItemState().getStatusCode());
				db.insertWithOnConflict(TABLE_WIDGET_TO_SNAPSHOT, null, iSTWQ, SQLiteDatabase.CONFLICT_REPLACE);
			}
		}
		db.setTransactionSuccessful();
		db.endTransaction();
		db.close();

		return true;
	}

	private boolean saveWidgetInfo(IWidgetItemInfo widg, SQLiteDatabase db){
		ContentValues cv = new ContentValues(3);
		cv.put(KEY_WIDGET_NAME, widg.getPackageName());
		cv.put(COLUMN_WIDGET_LABEL, widg.getLabel());
		cv.put(COLUMN_WIDGET_LAST_DATE, widg.getLastUsedFormated());

		db.insertWithOnConflict(TABLE_WIDGET_INFO, null, cv,SQLiteDatabase.CONFLICT_REPLACE); 
		return true;
	}

	@Override
	public boolean deleteSnapshot(String snapName) {

		SQLiteDatabase db = getWritableDatabase();
		db.beginTransaction();

		//First we have to remove all dependencies
		String whereClause = KEY_SNAPSHOT_REF + " = ?";
		String[] whereArgs = {snapName};

		db.delete(TABLE_WIDGET_TO_SNAPSHOT, whereClause, whereArgs);


		// now Delete form Snapshot Tabel
		whereClause = KEY_SNAPSHOT_NAME + " = ?";
		db.delete(TABLE_SNAPSHOT_INFO, whereClause, whereArgs);
		db.setTransactionSuccessful();
		db.endTransaction();
		db.close();

		return true;
	}

	@Override
	public boolean deleteWidgetItemInfo(String widgPack) {
		SQLiteDatabase db = getWritableDatabase();
		db.beginTransaction();

		//First we have to remove all dependencies
		String whereClause = KEY_WIDGET_REF + " = ?";
		String[] whereArgs = {widgPack};
		db.delete(TABLE_WIDGET_TO_SNAPSHOT, whereClause, whereArgs);

		// now Delete form Snapshot Table
		whereClause = KEY_WIDGET_NAME + " = ?";
		db.delete(TABLE_WIDGET_INFO, whereClause, whereArgs);
		db.setTransactionSuccessful();
		db.endTransaction();
		db.close();

		//delete from loaded snapshot
		Snapshot snapRef = getSelectedSnapshot();
		IWidgetItemInfo item = snapRef.getItemByName(widgPack);
		if (item != null) {
			snapRef.remove(item);
		}

		return true;
	}

	@Override
	public List<Snapshot> loadAllSnapshots() {
		SQLiteDatabase db = getReadableDatabase();
		/**
		 * 	SELECT packageName, widgetInfo, widgetScore, snapshotName, lastEdited
			FROM widgetInfoTable, snapshotInfoTable, widgetToSnapshotTable
			WHERE packageName = packageNameREF AND snapshotName = snapshotNameREF Order by snapshotName;
		 **/ 
		String loadAllQuery = getBaseSelectWithJoin() + " ORDER BY " + KEY_SNAPSHOT_NAME + ";";

		Cursor c = db.rawQuery(loadAllQuery, null);
		List<Snapshot> retVal = extractSnapshotFormDB(c);
		db.close();
		return retVal;
	}

	private List<Snapshot> extractSnapshotFormDB(Cursor c) {
		if (c == null) {
			return null;
		}
		DateFormat df = new SimpleDateFormat(DataManager.DATE_FORMAT,Locale.getDefault());
		Map<ISnapshotInfo, List<IWidgetItemInfo>> hs = new HashMap<ISnapshotInfo, List<IWidgetItemInfo>>();

		if (c.getCount() > 0) 
		{               
			c.moveToFirst();
			do {
				// get the data from the object
				String packageName = 	c.getString(c.getColumnIndex(KEY_WIDGET_NAME));
				String label =			c.getString(c.getColumnIndex(COLUMN_WIDGET_LABEL));
				double score = 			c.getDouble(c.getColumnIndex(COLUMN_WIDGET_SCORE));
				String snapshotName = 	c.getString(c.getColumnIndex(KEY_SNAPSHOT_NAME));
				ItemState itemState = 	ItemState.parse(c.getString(c.getColumnIndex(COLUMN_WIDGET_STATE)));
				Date lastUsed;
				Date lastEdited;
				try {
					lastEdited = df.parse(c.getString(c.getColumnIndex(COLUMN_SNAPSHOT_LAST_DATE)));
					lastUsed = df.parse(c.getString(c.getColumnIndex(COLUMN_WIDGET_LAST_DATE)));
				} catch (ParseException e) {
					lastEdited = new Date();
					lastUsed = new Date();
				}
				ISnapshotInfo si = new SnapshotInfo(snapshotName,lastEdited);
				IWidgetItemInfo wi = iWidgetItemInfoFactory(packageName, label, score,itemState,lastUsed);
				if (hs.containsKey(si)) {
					List<IWidgetItemInfo> widgList = hs.get(si);
					widgList.add(wi);
				}else{
					List<IWidgetItemInfo> widgList = new ArrayList<IWidgetItemInfo>();
					widgList.add(wi);
					hs.put(si, widgList);
				}

			} while (c.moveToNext());
			c.close();

			List<Snapshot> retList = new ArrayList<Snapshot>();
			for (ISnapshotInfo si : hs.keySet()) {
				retList.add(new Snapshot(si,hs.get(si)));
			}
			return retList;
		}
		return null;
	}

	public static IWidgetItemInfo iWidgetItemInfoFactory(String packageName, String label, double score, ItemState itemState,Date lastUsed) {
		if (packageName.equalsIgnoreCase(ConfigurationItemInfo.COM_SADNA_WIDGETS_APPLICATION_CONFIGURATION)) {
			return new ConfigurationItemInfo();
		}
		return new WidgetItemInfo(packageName,label,score,itemState,lastUsed);
	}

	private String getBaseSelectWithJoin() {
		return "SELECT " + KEY_WIDGET_NAME + " , " + COLUMN_WIDGET_LABEL + " , "  + COLUMN_WIDGET_SCORE +" , " + COLUMN_WIDGET_STATE + " , " + COLUMN_WIDGET_LAST_DATE + " , "  + KEY_SNAPSHOT_NAME + " , "  + COLUMN_SNAPSHOT_LAST_DATE + " "  +
				"FROM " + TABLE_WIDGET_INFO + " , " + TABLE_SNAPSHOT_INFO + " , "  + TABLE_WIDGET_TO_SNAPSHOT + " " +
				"WHERE " + KEY_WIDGET_NAME + " = " + KEY_WIDGET_REF + " AND " + KEY_SNAPSHOT_NAME + " = " + KEY_SNAPSHOT_REF;
	}

	@Override
	public List<Snapshot> loadSnapshots(List<String> snapshotsNameList) {
		SQLiteDatabase db = getReadableDatabase();
		/**
		 * 	SELECT packageName, widgetInfo, widgetScore, snapshotName, lastEdited
			FROM widgetInfoTable, snapshotInfoTable, widgetToSnapshotTable
			WHERE packageName = packageNameREF AND snapshotName = snapshotNameREF AND snapshotName in('Snapshot 0' , 'Snapshot 1');
		 * **/
		String inQuery = " AND " + KEY_SNAPSHOT_NAME + " in(";
		for (int i = 0; i < snapshotsNameList.size() -1; i++) {
			inQuery += "\"" + snapshotsNameList.get(i) + "\", ";
		}
		inQuery += "\"" + snapshotsNameList.get(snapshotsNameList.size() - 1) + "\" );";
		String loadSomeQuery = getBaseSelectWithJoin() + inQuery;
		Cursor c  = db.rawQuery(loadSomeQuery, null);
		List<Snapshot> retVal = extractSnapshotFormDB(c);
		c.close();
		db.close();
		return retVal;
	}

	@Override
	public Snapshot loadSnapshot(String snapName) {
		/**
		 * 	SELECT packageName, widgetInfo, widgetScore, snapshotName, lastEdited
			FROM widgetInfoTable, snapshotInfoTable, widgetToSnapshotTable
			WHERE packageName = packageNameREF AND snapshotName = snapshotNameREF AND snapshotName = 'Snapshot 0';
		 ***/
		String loadQuery = getBaseSelectWithJoin() + " AND " + KEY_SNAPSHOT_NAME + " = " + "\"" + snapName + "\"" + ";";
		SQLiteDatabase db = getReadableDatabase();
		Cursor c = db.rawQuery(loadQuery, null);
		List<Snapshot> retval = extractSnapshotFormDB(c);
		c.close();
		db.close();
		return (retval != null) ? retval.get(0) : null;
	}

	@Override
	public boolean setSelectedSnapshot(Snapshot snap) {
		if (snap == null) {
			throw new NullPointerException();
		}
		selectedSnapshot = snap;
		selectedSnapshot.getSnapshotInfo().setSnapshotName(getProperSnapshotName());
		return true;
	}

	private String getProperSnapshotName(){
		if (!getProfolingState()) {
			return String.format(StatisticsService.RESERVED_SNAPSHOT, "Profiling Off");
		}
		Calendar cal = Calendar.getInstance();
		int today = cal.get(Calendar.DAY_OF_WEEK);
		
		if (isWorkingDay(today)) {
			int[] a = getWorkingHours();
			if (isNowBetweenDateTime(dateFromHourMin(a[0]), dateFromHourMin(a[1]))) {
				return String.format(StatisticsService.RESERVED_SNAPSHOT, "Working day Work hours");	
			}
			return String.format(StatisticsService.RESERVED_SNAPSHOT, "Working day off work");
		}
		return String.format(StatisticsService.RESERVED_SNAPSHOT, "Weekend");
	}

	@Override
	public Snapshot getSelectedSnapshot() {
		if (selectedSnapshot != null) {
			String properSnapshotName = getProperSnapshotName();
			if (selectedSnapshot.getSnapshotInfo().getSnapshotName().equals(properSnapshotName)) {
				return selectedSnapshot;	
			}
			saveSnapshot(selectedSnapshot);
			Snapshot selectedSnapshotTemp = loadSnapshot(properSnapshotName);
			if (selectedSnapshotTemp != null) {
				selectedSnapshot = selectedSnapshotTemp;
			}else{
				selectedSnapshot.getSnapshotInfo().setSnapshotName(properSnapshotName);
			}
			
			
		}
			selectedSnapshot = loadSnapshot(getProperSnapshotName()); 
			if (selectedSnapshot == null) {
				// the Db has a corrupted snapshot ! generating a new one
				selectedSnapshot = generateValidSnapshot();
				return selectedSnapshot;
				
			}
			return selectedSnapshot;		
	}

	private Snapshot generateValidSnapshot() {
		Date currDate = new Date();
		ISnapshotInfo snapshotInfo = new SnapshotInfo(getProperSnapshotName(), currDate);
		Snapshot currSnapshot = new Snapshot(snapshotInfo, getInstalledAppsInfo());
		synchronized (snapshotIteratorSyncObject) {
			currSnapshot.removeDuplicateEntries();
		}
		this.saveSnapshot(currSnapshot);
		this.setSelectedSnapshot(currSnapshot);
		return currSnapshot;
	}

	private List<IWidgetItemInfo> getInstalledAppsInfo() {
		List<IWidgetItemInfo> result = new ArrayList<IWidgetItemInfo>();

		Context context = this.mContext;

		final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		final List<ResolveInfo> pkgAppsList = context.getPackageManager().queryIntentActivities(mainIntent, 0);

		for (ResolveInfo resolveInfo : pkgAppsList) {
			String itemLabel = resolveInfo.loadLabel(mContext.getPackageManager()).toString();
			String itemPkgName = resolveInfo.activityInfo.packageName;
			if (itemPkgName.equals(this.getDefaultLauncher()))
				continue;
			IWidgetItemInfo itemInfo = new WidgetItemInfo(itemPkgName, itemLabel);
			result.add(itemInfo);
		}

		return result;
	}

	@Override
	public Snapshot getSelectedSnapshotFiltered(Snapshot s,int id) {
		if (s == null) {
			return null;
		}
		if (appListCache == null) {
			appListCache = new ApplicationListCache();
		}
		if (!appListCache.isValid()) {
			appListCache.setSnap(generateList(s));
		}
		return appListCache.getSplitlist(id);
	}

	private Snapshot generateList(Snapshot s) {
		Snapshot filtered = new Snapshot(new SnapshotInfo("FilteredSelectedSnapshot", new Date()), new ArrayList<IWidgetItemInfo>());
		Snapshot must = new Snapshot(new SnapshotInfo("FilteredSelectedSnapshot", new Date()), new ArrayList<IWidgetItemInfo>());

		boolean shouldValidate = false;
		IWidgetItemInfo itemCopy;

		synchronized (snapshotIteratorSyncObject){
			for (IWidgetItemInfo item: s){
				itemCopy = iWidgetItemInfoFactory(item.getPackageName(), item.getLabel(), item.getScore(),item.getItemState(),item.getLastUse());
				if (item.getBitmap(mContext) == null) {
					shouldValidate = true;
					continue;
				}
				if (item.getPackageName().equals(getDefaultLauncher())) {
					continue;
				}
				if (item.getItemState() == ItemState.MUST){
					must.add(itemCopy);
				}else if (item.getItemState() == ItemState.AUTO) {
					filtered.add(itemCopy);
				}
			}

		}

		Collections.sort(filtered);
		Collections.sort(must);

		must.addAll(filtered);
		
		if (shouldValidate) {
			validateIntegrity();
		}
		return must;
	}

	public Snapshot getSelectedSnapshotFiltered(int id){
		return getSelectedSnapshotFiltered(getSelectedSnapshot(),id);
	}

	class ApplicationListCache{
		private static final int TRESHOLD = 12;
		Date date;
		private Snapshot snap;

		boolean isValid(){
			if (snap == null) {
				return false;
			}

			return (getSeconds(date)< TRESHOLD);
		}

		public Snapshot getSplitlist(int id) {
			if (snap.size() == 0) {
				return new Snapshot(snap.getSnapshotInfo() ,new ArrayList<IWidgetItemInfo>());
			}
			int start = Math.min(id * getApplicationLimit(),snap.size() - 1);
			int end = Math.min(((id + 1) * getApplicationLimit()),snap.size());
			if (id < 0 ) {
				start = 0;
				end = Math.min(getApplicationLimit(),snap.size());
			}
			if (start < end) {
				return new Snapshot(snap.getSnapshotInfo() ,snap.subList(start, end));	
			}
			return new Snapshot(snap.getSnapshotInfo() ,new ArrayList<IWidgetItemInfo>());
			
		}

		public Snapshot getSnap() {
			return snap;
		}

		public void setSnap(Snapshot snap) {
			date = new Date();
			this.snap = snap;
		}
	}

	public void validateIntegrity(){
		Snapshot snap = getSelectedSnapshot().getCopy();
		Snapshot snapMain = getSelectedSnapshot();
		Date currDate = new Date();
		ISnapshotInfo snapshotInfo = new SnapshotInfo(StatisticsService.RESERVED_SNAPSHOT, currDate);
		Snapshot tempSnapshot = new Snapshot(snapshotInfo, getInstalledAppsInfo());
		synchronized (snapshotIteratorSyncObject) {
			snap.removeAll(tempSnapshot);
			for (IWidgetItemInfo iWidgetItemInfo : snap) {
				deleteWidgetItemInfo(iWidgetItemInfo.getPackageName());
				snapMain.remove(iWidgetItemInfo);
			}
			setSelectedSnapshot(snapMain);
		}
	}

	@Override
	public int getApplicationLimit() {
		int limit = sharedPreferences.getInt(SELECTED_APPLICATION_LIMIT, 0);
		if (limit == 0) {
			setApplicationLimit(APPLICATION_LIMIT_DEFUALT);
			return APPLICATION_LIMIT_DEFUALT;
		} else {
			return limit;
		}		
	}

	@Override
	public boolean setApplicationLimit(int limit) {
		if (limit < 0) {
			return false;
		}
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putInt(SELECTED_APPLICATION_LIMIT, limit);
		editor.commit();
		return true;

	}

	@Override
	public String getDefaultLauncher() {
		String selected = sharedPreferences.getString(SETTINGS_DEFAULT_LAUNCHER,BAD_LAUNCHER);
		if (selected.toString().equals(BAD_LAUNCHER)) {
			return DEFAULT_LAUNCHER_FALLBACK;
		} 
		return selected;
	}

	@Override
	public void setDefaultLauncher(String pack) {
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString(SETTINGS_DEFAULT_LAUNCHER, pack);
		editor.commit();
	}

	@Override
	public void setProfolingState(boolean state) {
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putBoolean(SETTINGS_PROFILING_STATE, state);
		editor.commit();
	}

	@Override
	public boolean getProfolingState() {
		return sharedPreferences.getBoolean(SETTINGS_PROFILING_STATE,false);
	}

	@SuppressLint("NewApi")
	@Override
	public void setWorkingDays(int[] workingDays) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
			return;
		if (workingDays == null) {
			return;
		}
		Set<String> valList = new TreeSet<String>();
		for (int day : workingDays) {
			valList.add(Integer.toString(day));
		}
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putStringSet(SETTINGS_WORKING_DAYS, valList);
		editor.commit();
	}

	@SuppressLint("NewApi")
	@Override
	public int[] getWorkingDays() {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
			return new int[]{Calendar.MONDAY ,Calendar.TUESDAY ,Calendar.WEDNESDAY ,Calendar.THURSDAY, Calendar.FRIDAY};
		Set<String> valList  = sharedPreferences.getStringSet(SETTINGS_WORKING_DAYS, null);
		if (valList == null) {
			Calendar cal = Calendar.getInstance();
			switch (cal.getFirstDayOfWeek()) {
			case Calendar.SATURDAY:
				return new int[]{Calendar.SATURDAY ,Calendar.SUNDAY ,Calendar.MONDAY ,Calendar.TUESDAY ,Calendar.WEDNESDAY};
			case Calendar.SUNDAY:
				return new int[]{Calendar.SUNDAY ,Calendar.MONDAY ,Calendar.TUESDAY ,Calendar.WEDNESDAY ,Calendar.THURSDAY};
			case Calendar.MONDAY:
				return new int[]{Calendar.MONDAY ,Calendar.TUESDAY ,Calendar.WEDNESDAY ,Calendar.THURSDAY, Calendar.FRIDAY};
			default:
				return new int[]{Calendar.MONDAY ,Calendar.TUESDAY ,Calendar.WEDNESDAY ,Calendar.THURSDAY, Calendar.FRIDAY};
			}
		}
		int[] retArr = new int[valList.size()];
		int i=0;
		for (String day : valList) {
			retArr[i] = Integer.parseInt(day);
			i++;
		}
		return retArr;
	}

	@SuppressLint("NewApi")
	@Override
	public void setWorkingHours(int startMinutes,int endMinutes) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
			return;
		// TODO Auto-generated method stub
		Set<String> valList = new TreeSet<String>();

		valList.add(Integer.toString(startMinutes));
		valList.add(Integer.toString(endMinutes));

		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putStringSet(SETTINGS_WORKING_HOURS, valList);
		editor.commit();
	}

	@SuppressLint("NewApi")
	@Override
	public int[] getWorkingHours() {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
			return new int[]{(8*60),(18*60)};
		Set<String> valList  = sharedPreferences.getStringSet(SETTINGS_WORKING_HOURS, null);
		if ((valList == null) || (valList.size() != 2)) {
			return new int[]{(8*60),(18*60)};
		}
		int[] retArr = new int[2];
		List<Integer> l = new ArrayList<Integer>();
		for (String time : valList) {
			l.add(Integer.parseInt(time));
		}
		Collections.sort(l);
		for (int i = 0; i < retArr.length; i++) {
			retArr[i] = l.get(i);
		}
		return retArr;
	}

	private Date dateFromHourMin(int m)
	{
		final GregorianCalendar gc = new GregorianCalendar();
		int hours  = (int) TimeUnit.MINUTES.toHours(m);
		int minutes = m - (hours * 60); 
		
		gc.set(Calendar.HOUR_OF_DAY, hours);
		gc.set(Calendar.MINUTE, minutes);
		gc.set(Calendar.SECOND, 0);
		gc.set(Calendar.MILLISECOND, 0);
		return gc.getTime();
	}
	
	boolean isWorkingDay(int day){
		int[] days = getWorkingDays();
		for (int i : days) {
			if (i == day) {
				return true;
			}
		}
		return false;
	}
	
	boolean isNowBetweenDateTime(final Date s, final Date e)
	{
	    final Date now = new Date();
	    return now.after(s) && now.before(e);
	}

	@Override
	public double getAvaregeScore() {
		Snapshot temp = getSelectedSnapshotFiltered(0);
		if (temp.size() == 0) {
			return 1;
		}
		double sum = 0;
		synchronized (snapshotIteratorSyncObject){
			for (IWidgetItemInfo iWidgetItemInfo : temp) {
				sum += iWidgetItemInfo.getScore();
			}
		}
		return sum/temp.size();
	}

	public static long getSeconds(Date lastUpdate) {
		long diff = (new Date()).getTime() - lastUpdate.getTime();
		return TimeUnit.MILLISECONDS.toSeconds(diff);
	}
}