package com.sadna.interfaces;

import java.util.List;

import com.sadna.data.Snapshot;

public interface IDataManager {
	
	public boolean saveAllSnapshots(List<Snapshot> lst);
	public boolean saveSnapshot(Snapshot snap);
	
	public List<Snapshot> loadAllSnapshots();
	public Snapshot loadSnapshot(String snapName);
	
	public boolean setSelectedSnapshot(Snapshot snap);
	public Snapshot getSelectedSnapshot();
	public Snapshot getSelectedSnapshotFiltered();
	public List<Snapshot> loadSnapshots(List<String> snapshotsNameList);
	

}
