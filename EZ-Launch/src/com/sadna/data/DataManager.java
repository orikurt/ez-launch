package com.sadna.data;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.sadna.enums.ItemState;
import com.sadna.interfaces.IDataManager;
import com.sadna.interfaces.ISnapshotInfo;
import com.sadna.interfaces.IWidgetItemInfo;
import com.sadna.service.StatisticsService;

public class DataManager extends SQLiteOpenHelper implements IDataManager {

	private static final int APPLICATION_LIMIT = 15;

	private static final String APPLICATION_SHARED_PREFRENCES = "ApplicationSharedPrefrences";

	private static final String BAD_SNAPSHOT = "Bad_SnapshotXXXERROR";

	public static final String DATE_FORMAT = "YYYY-MM-DD HH:MM:SS".toLowerCase(Locale.getDefault());

	// All Static variables
	// Database Name
	private static final String DATABASE_NAME = "EZ_Launch_DB";

	// Database Version
	private static final int DATABASE_VERSION = 12;

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
	private static final String SELECTED_SNAPSHOT = "SelectedSnapshot";

	private static Snapshot selectedSnapshot = null;

	private Context mContext;

	private ApplicationListCahce appListCache = null;
	
	public DataManager(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		mContext = context;
		sharedPreferences = context.getSharedPreferences(APPLICATION_SHARED_PREFRENCES, Context.MODE_PRIVATE);

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

		for (IWidgetItemInfo iWidgetItemInfo : snap) {
			saveWidgetInfo(iWidgetItemInfo,db);
		}
//		String insertSnapShotQuery = getInsertOrReplaceQuery(TABLE_SNAPSHOT_INFO, 
//				new String[] {KEY_SNAPSHOT_NAME,COLUMN_SNAPSHOT_LAST_DATE}, 
//				new String[] {snap.getSnapshotInfo().getSnapshotName(),snap.getSnapshotInfo().getLastEditedFormated()});
		ContentValues iSSQCV = new ContentValues(2);
		iSSQCV.put(KEY_SNAPSHOT_NAME, snap.getSnapshotInfo().getSnapshotName());
		iSSQCV.put(COLUMN_SNAPSHOT_LAST_DATE, snap.getSnapshotInfo().getLastEditedFormated());
		db.insertWithOnConflict(TABLE_SNAPSHOT_INFO, null, iSSQCV, SQLiteDatabase.CONFLICT_REPLACE);
		//db.execSQL(insertSnapShotQuery);

		// Update relations - the current Implementation is not perfect as it sends many queries to the DB instead of 1 query
		for (IWidgetItemInfo widg : snap) {
//			String insertSnapToWidgetQuery = getInsertOrReplaceQuery(TABLE_WIDGET_TO_SNAPSHOT,
//					new String[]{KEY_WIDGET_REF,KEY_SNAPSHOT_REF}, 
//					new String[]{widg.getPackageName(),snap.getSnapshotInfo().getSnapshotName()});
			ContentValues iSTWQ = new ContentValues(4);
			iSTWQ.put(KEY_WIDGET_REF, widg.getPackageName());
			iSTWQ.put(KEY_SNAPSHOT_REF, snap.getSnapshotInfo().getSnapshotName());
			iSTWQ.put(COLUMN_WIDGET_SCORE, widg.getScore());
			iSTWQ.put(COLUMN_WIDGET_STATE, widg.getItemState().getStatusCode());
			db.insertWithOnConflict(TABLE_WIDGET_TO_SNAPSHOT, null, iSTWQ, SQLiteDatabase.CONFLICT_REPLACE);
			//db.execSQL(insertSnapToWidgetQuery);
		}

		db.setTransactionSuccessful();
		db.endTransaction();
		db.close();

		/*
		INSERT OR REPLACE INTO Employee (id,role,name) 
		  VALUES (  1, 
		            'code monkey',
		            (select name from Employee where id = 1)
		          );*/
		return true;
	}

	private boolean saveWidgetInfo(IWidgetItemInfo widg, SQLiteDatabase db){
//		String insertWidgetQuery = getInsertOrReplaceQuery(TABLE_WIDGET_INFO, 
//				new String[]{KEY_WIDGET_NAME,COLUMN_WIDGET_LABEL,COLUMN_WIDGET_SCORE,COLUMN_WIDGET_STATE,COLUMN_WIDGET_LAST_DATE}, 
//				new String[]{widg.getPackageName(),widg.getLabel(),Double.toString(widg.getScore()),widg.getItemState().getStatusCode(),widg.getLastUsedFormated()});
		ContentValues cv = new ContentValues(3);
		cv.put(KEY_WIDGET_NAME, widg.getPackageName());
		cv.put(COLUMN_WIDGET_LABEL, widg.getLabel());
		cv.put(COLUMN_WIDGET_LAST_DATE, widg.getLastUsedFormated());
		
		
		db.insertWithOnConflict(TABLE_WIDGET_INFO, null, cv,SQLiteDatabase.CONFLICT_REPLACE); 
		//db.execSQL(insertWidgetQuery);
		return true;
	}
	//field 
	private String getInsertOrReplaceQuery2(String table,String fields[], String values[]){
		StringBuilder res = new StringBuilder();
		res.append("INSERT OR REPLACE INTO ");
		res.append(table);
		res.append("( ");
		for (int i = 0; i < fields.length - 1; i++) {
			res.append(fields[i] + ",");	
		}
		res.append(fields[fields.length - 1]);
		res.append(") VALUES( ");
		for (int i = 0; i < values.length - 1; i++) {
			res.append("\"" + values[i]+ "\"" + ",");	
		}
		res.append("\"" + values[values.length-1]+ "\"");
		res.append(");");
		return res.toString();
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
		//Snapshot snapArr[]=new Snapshot[c.getCount()];
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
		// TODO Auto-generated method stub
		
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
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString(SELECTED_SNAPSHOT, snap.getSnapshotInfo().getSnapshotName());
		editor.commit();
		return true;
	}


	@Override
	public Snapshot getSelectedSnapshot() {
		if (selectedSnapshot != null) {
			return selectedSnapshot;
		}
		String selected = sharedPreferences.getString(SELECTED_SNAPSHOT,BAD_SNAPSHOT);
		if (selected.toString().equals(BAD_SNAPSHOT)) {
			// DB is empty - Generate first snapshot
			return generateValidSnapshot();
		} else {
			Snapshot ret = loadSnapshot(selected);
			if (ret == null) {
				// the Db has a corrupted snapshot ! generating a new one
				return generateValidSnapshot();
			}
			return ret;
		}		
	}

	private Snapshot generateValidSnapshot() {
		Date currDate = new Date();
		ISnapshotInfo snapshotInfo = new SnapshotInfo(StatisticsService.RESERVED_SNAPSHOT, currDate);
		Snapshot currSnapshot = new Snapshot(snapshotInfo, getInstalledAppsInfo());
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
			appListCache = new ApplicationListCahce();
		}
		if (!appListCache.isValid()) {
			appListCache.setSnap(generateList(s));
		}
		return appListCache.getSplitlist(id);
	}

	private Snapshot generateList(Snapshot s) {
		Snapshot filtered = new Snapshot(new SnapshotInfo("FilteredSelectedSnapshot", new Date()), new ArrayList<IWidgetItemInfo>());
		Snapshot must = new Snapshot(new SnapshotInfo("FilteredSelectedSnapshot", new Date()), new ArrayList<IWidgetItemInfo>());
		
		IWidgetItemInfo itemCopy;
		
		for (IWidgetItemInfo item: s){
			itemCopy = iWidgetItemInfoFactory(item.getPackageName(), item.getLabel(), item.getScore(),item.getItemState(),item.getLastUse());
			if (item.getItemState() == ItemState.MUST){
				must.add(itemCopy);
			}else if (item.getItemState() == ItemState.AUTO) {
				filtered.add(itemCopy);
			}
		}
		
		
		
		
		//filtered.add(new ConfigurationItemInfo());
		Collections.sort(filtered);
		Collections.sort(must);

		must.addAll(filtered);
		must.add(APPLICATION_LIMIT,new ConfigurationItemInfo());
		return must;
	}
	
	public Snapshot getSelectedSnapshotFiltered(int id){
		return getSelectedSnapshotFiltered(getSelectedSnapshot(),id);
	}
	
	class ApplicationListCahce{
		private static final int TRESHOLD = 7;
		Date date;
		private Snapshot snap;
		
		boolean isValid(){
			if (snap == null) {
				return false;
			}
			long diff = (new Date()).getTime() - date.getTime();
			long seconds = TimeUnit.MILLISECONDS.toSeconds(diff);
			return (seconds < TRESHOLD);
		}

		public Snapshot getSplitlist(int id) {
			return new Snapshot(snap.getSnapshotInfo() ,snap.subList(id * APPLICATION_LIMIT, (id + 1) * APPLICATION_LIMIT));
		}

		public Snapshot getSnap() {
			return snap;
		}

		public void setSnap(Snapshot snap) {
			date = new Date();
			this.snap = snap;
		}
	}


}
