package com.android.data;

import java.util.Date;

import com.sadna.interfaces.ISnapshotInfo;

public class SnapshotInfo implements ISnapshotInfo{
	private String snapshotName;
	private Date lastEdited;
	
	public SnapshotInfo(String name, Date lastEdit) {
		this.snapshotName = name;
		this.lastEdited = lastEdit;
	}
	@Override
	public String getSnapshotName() {
		return snapshotName;
	}
	@Override
	public void setSnapshotName(String snapshotName) {
		this.snapshotName = snapshotName;
	}
	@Override
	public Date getLastEdited() {
		return lastEdited;
	}
	@Override
	public void setLastEdited(Date lastEdited) {
		this.lastEdited = lastEdited;
	}
	
	


}
