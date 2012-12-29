package com.android.data;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.sadna.interfaces.IDataManager;
import com.sadna.interfaces.ISnapshotInfo;
import com.sadna.interfaces.IWidgetItemInfo;

public class DataManager extends SQLiteOpenHelper implements IDataManager {

	public static final String DATE_FORMAT = "YYYY-MM-DD HH:MM:SS";

	// All Static variables
	// Database Name
	private static final String DATABASE_NAME = "EZ_Launch_DB";

	// Database Version
	private static final int DATABASE_VERSION = 1;

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
	private static final String COLUMN_WIDGET_SCORE = "widgetScore";


	// Widget info to Snapshot table
	private static final String TABLE_WIDGET_TO_SNAPSHOT = "widgetToSnapshotTable";

	// Widget info to Snapshot columns 
	private static final String KEY_WIDGET_REF = "packageNameREF";
	private static final String KEY_SNAPSHOT_REF = "snapshotNameREF";
	
	//Date Formater
	


	public DataManager(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);

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
				COLUMN_WIDGET_SCORE+ " REAL NOT NULL DEFAULT '0'" + ");";
		db.execSQL(CREATE_WIDGET_INFO_TABLE);

		String CREATE_WIDGET_TO_SNAPSHOT_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_WIDGET_TO_SNAPSHOT + "("
				+ KEY_WIDGET_REF + " TEXT (35) NOT NULL," + 
				KEY_SNAPSHOT_REF + " TEXT (35) NOT NULL, " + 
				"PRIMARY KEY (" + KEY_WIDGET_REF + ", "  + KEY_SNAPSHOT_REF +  " ), "
				+ "FOREIGN KEY(" + KEY_WIDGET_REF + ") REFERENCES " + TABLE_WIDGET_INFO + "(" + KEY_WIDGET_NAME +") ON UPDATE CASCADE ON DELETE CASCADE,"
				+ "FOREIGN KEY(" + KEY_SNAPSHOT_REF + ") REFERENCES " + TABLE_SNAPSHOT_INFO + "(" + KEY_SNAPSHOT_NAME +") ON UPDATE CASCADE ON DELETE CASCADE" +");";
		db.execSQL(CREATE_WIDGET_TO_SNAPSHOT_TABLE);

	}

	// Upgrading database
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
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
		String insertSnapShotQuery = getInsertOrReplaceQuery(TABLE_SNAPSHOT_INFO, 
				new String[] {KEY_SNAPSHOT_NAME,COLUMN_SNAPSHOT_LAST_DATE}, 
				new String[] {snap.getSnapshot().getSnapshotName(), snap.getSnapshot().getLastEditedFormated()});

		db.execSQL(insertSnapShotQuery);

		// Update relations - the current Implementation is not perfect as it sends many queries to the DB instead of 1 query
		for (IWidgetItemInfo widg : snap) {
			String insertSnapToWidgetQuery = getInsertOrReplaceQuery(TABLE_WIDGET_TO_SNAPSHOT,
					new String[]{KEY_WIDGET_REF,KEY_SNAPSHOT_REF}, 
					new String[]{widg.getPackageName(),snap.getSnapshot().getSnapshotName()});
			db.execSQL(insertSnapToWidgetQuery);
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
		String insertWidgetQuery = getInsertOrReplaceQuery(TABLE_WIDGET_INFO, 
				new String[]{KEY_WIDGET_NAME,COLUMN_WIDGET_LABEL,COLUMN_WIDGET_SCORE}, 
				new String[]{widg.getPackageName(),widg.getLabel(),Double.toString(widg.getScore())});
		db.execSQL(insertWidgetQuery);
		return true;
	}
	//field 
	private String getInsertOrReplaceQuery(String table,String fields[], String values[]){
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
				Date lastEdited;
				try {
					lastEdited = df.parse(c.getString(c.getColumnIndex(COLUMN_SNAPSHOT_LAST_DATE)));
				} catch (ParseException e) {
					lastEdited = new Date();
				}
				SnapshotInfo si = new SnapshotInfo(snapshotName,lastEdited);
				WidgetItemInfo wi = new WidgetItemInfo(packageName,label,score);
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

	private String getBaseSelectWithJoin() {
		return "SELECT " + KEY_WIDGET_NAME + " , " + COLUMN_WIDGET_LABEL + " , "  + COLUMN_WIDGET_SCORE + " , "  + KEY_SNAPSHOT_NAME + " , "  + COLUMN_SNAPSHOT_LAST_DATE + " "  +
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
		
		String loadQuery = getBaseSelectWithJoin() + " AND " + KEY_SNAPSHOT_NAME + " = " + snapName + ";";
		SQLiteDatabase db = getReadableDatabase();
		Cursor c = db.rawQuery(loadQuery, null);
		List<Snapshot> retval = extractSnapshotFormDB(c);
		return (retval != null) ? retval.get(0) : null;
	}


	@Override
	public boolean setSelectedSnapshot(Snapshot snap) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public Snapshot getSelectedSnapshot() {
		// TODO Auto-generated method stub
		return null;
	}



}
