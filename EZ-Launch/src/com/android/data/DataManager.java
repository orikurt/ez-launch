package com.android.data;

import java.util.List;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.sadna.interfaces.IDataManager;

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
	private static final String KEY_SNAPSHOT_LASR_DATE = "lastEdited";


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
				KEY_SNAPSHOT_LASR_DATE + " TEXT DEFAULT CURRENT_TIMESTAMP " + ")";
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
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public boolean saveSnapshot(Snapshot snap) {
		// TODO Auto-generated method stub
		return false;
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
