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
    
    
	
	
	
	
	
    

	public DataManager(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		// TODO Auto-generated constructor stub
	}


	
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		
	}

    
	// Upgrading database
	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
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
