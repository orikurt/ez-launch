package com.android.data;

import java.util.List;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

import com.sadna.interfaces.IDataManager;

public class DataManager extends SQLiteOpenHelper implements IDataManager {
	
	// All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;
    
    

	public DataManager(Context context, String name, CursorFactory factory,int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
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

	@Override
	public void onCreate(SQLiteDatabase arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

}
