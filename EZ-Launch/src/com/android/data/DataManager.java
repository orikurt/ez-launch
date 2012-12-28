package com.android.data;

import java.util.List;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.sadna.interfaces.IDataManager;
import com.sadna.interfaces.IWidgetItemInfo;

public class DataManager extends SQLiteOpenHelper implements IDataManager {

	// All Static variables
	// Database Name
	private static final String DATABASE_NAME = "EZ_Launch_DB";

	// Database Version
	private static final int DATABASE_VERSION = 1;

	// Snapshot info table name
	private static final String TABLE_SNAPSHOT_INFO = "snapshotInfo";

	// Snapshot info table columns
	private static final String KEY_SNAPSHOT_NAME = "snapshotName";
	private static final String COLUMN_SNAPSHOT_LAST_DATE = "lastEdited";


	// Widget info table name
	private static final String TABLE_WIDGET_INFO = "widgetInfo";

	// Widget info table columns
	private static final String KEY_WIDGET_NAME = "packageName";
	private static final String COLUMN_WIDGET_LABEL = "widgetInfo";
	private static final String COLUMN_WIDGET_SCORE = "widgetInfo";


	// Widget info to Snapshot table
	private static final String TABLE_WIDGET_TO_SNAPSHOT = "widgetToSnapshot";

	// Widget info to Snapshot columns 
	private static final String KEY_WIDGET_REF = "packageNameREF";
	private static final String KEY_SNAPSHOT_REF = "snapshotNameREF";


	public DataManager(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);

	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String CREATE_SNAPSHOT_INFO_TABLE = "CREATE TABLE IF NOT EXISTS" + TABLE_SNAPSHOT_INFO + "("
				+ KEY_SNAPSHOT_NAME + " TEXT PRIMARY KEY," + 
				COLUMN_SNAPSHOT_LAST_DATE + " TEXT DEFAULT CURRENT_TIMESTAMP " + ")";
		db.execSQL(CREATE_SNAPSHOT_INFO_TABLE);
		
		String CREATE_WIDGET_INFO_TABLE = "CREATE TABLE IF NOT EXISTS" + TABLE_WIDGET_INFO + "("
				+ KEY_WIDGET_NAME + " TEXT PRIMARY KEY," + 
				COLUMN_WIDGET_LABEL + " TEXT," + 
				COLUMN_WIDGET_SCORE+ " REAL NOT NULL DEFAULT '0'" + ")";
		db.execSQL(CREATE_WIDGET_INFO_TABLE);
		
		String CREATE_WIDGET_TO_SNAPSHOT_TABLE = "CREATE TABLE IF NOT EXISTS" + TABLE_WIDGET_INFO + "("
				+ KEY_WIDGET_REF + " TEXT," + 
				KEY_SNAPSHOT_REF + " TEXT," + 
				"PRIMARY KEY (" + KEY_WIDGET_REF + ", " + KEY_WIDGET_REF + KEY_SNAPSHOT_REF +  " ), "
				+ "FOREIGN KEY(" + KEY_WIDGET_REF + ") REFERENCES " + TABLE_WIDGET_INFO + "(" + KEY_WIDGET_NAME +") ON UPDATE CASCADE ON UPDATE CASCADE,"
				+ "FOREIGN KEY(" + KEY_SNAPSHOT_REF + ") REFERENCES " + TABLE_SNAPSHOT_INFO + "(" + KEY_SNAPSHOT_NAME +") ON UPDATE CASCADE ON UPDATE CASCADE" +")";
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
		for (IWidgetItemInfo iWidgetItemInfo : snap) {
			saveWidgetInfo(iWidgetItemInfo);
		}
		String insertSnapShotQuery = getInsertOrReplaceQuery(TABLE_SNAPSHOT_INFO, 
				new String[] {KEY_SNAPSHOT_NAME,COLUMN_SNAPSHOT_LAST_DATE}, 
				new String[] {snap.getSnapshot().getSnapshotName(), snap.getSnapshot().getLastEdited().toString()});
		
		getWritableDatabase().execSQL(insertSnapShotQuery);
		
		// Update relations - the current Implementation is not perfect as it sends many queries to the DB instead of 1 query
		for (IWidgetItemInfo widg : snap) {
			String insertSnapToWidgetQuery = getInsertOrReplaceQuery(TABLE_WIDGET_TO_SNAPSHOT,
					new String[]{KEY_WIDGET_REF,KEY_SNAPSHOT_REF}, 
					new String[]{widg.getPackageName(),snap.getSnapshot().getSnapshotName()});
			getWritableDatabase().execSQL(insertSnapToWidgetQuery);
		}
		
		/*
		INSERT OR REPLACE INTO Employee (id,role,name) 
		  VALUES (  1, 
		            'code monkey',
		            (select name from Employee where id = 1)
		          );*/
		return true;
	}
	
	private boolean saveWidgetInfo(IWidgetItemInfo widg){
		String insertWidgetQuery = getInsertOrReplaceQuery(TABLE_WIDGET_INFO, 
				new String[]{KEY_WIDGET_NAME,COLUMN_WIDGET_LABEL,COLUMN_WIDGET_SCORE}, 
				new String[]{widg.getPackageName(),widg.getLabel(),Double.toString(widg.getScore())});
		

		getWritableDatabase().execSQL(insertWidgetQuery);

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
			res.append(values[i] + ",");	
		}
		res.append(values[values.length-1]);
		res.append(");");
		return res.toString();
	}

	@Override
	public List<Snapshot> loadAllSnapshots() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Snapshot loadSnapshot(String snapName) {
		// TODO Auto-generated method stub
		return null;
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
