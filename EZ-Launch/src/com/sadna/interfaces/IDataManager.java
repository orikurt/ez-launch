package com.sadna.interfaces;

import java.util.List;

public interface IDataManager {
	
	public boolean saveAllSnapshots(List<Snapshot> lst);
	public boolean saveSnapshot(Snapshot snap);
	
	public List<Snapshot> loadAllSnapshots(List<Snapshot> lst);
	public Snapshot loadSnapshot(Snapshot snap);

}
