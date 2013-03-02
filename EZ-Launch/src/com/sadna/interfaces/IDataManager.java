package com.sadna.interfaces;

import java.util.List;

import com.sadna.data.Snapshot;

public interface IDataManager {
	
	public boolean saveAllSnapshots(List<Snapshot> lst);
	public boolean saveSnapshot(Snapshot snap);
	
	public List<Snapshot> loadAllSnapshots();
	public Snapshot loadSnapshot(String snapName);
	
	public boolean deleteSnapshot(String snapName);
	public boolean deleteWidgetItemInfo(String widgPack);
	
	public boolean setSelectedSnapshot(Snapshot snap);
	public Snapshot getSelectedSnapshot();
	
	public int getApplicationLimit();
	public boolean setApplicationLimit(int limit);
	
	public void validateIntegrity();
	
	public String getDefaultLauncher();
	public void setDefaultLauncher(String pack);
	
	public Snapshot getSelectedSnapshotFiltered(int id);
	public Snapshot getSelectedSnapshotFiltered(Snapshot s,int id);
	public List<Snapshot> loadSnapshots(List<String> snapshotsNameList);
	
	
	public void setProfolingState(boolean state);
	public boolean getProfolingState();
	
	public void setWorkingDays(int[] workingDays);
	public int[] getWorkingDays();
	
	
	public void setWorkingHours(int startMinutes,int endMinutes);
	public int[] getWorkingHours();
	
	public double getAvaregeScore();
	
}