package com.sadna.interfaces;

import java.util.Date;

public interface ISnapshotInfo {
	public String getSnapshotName();
	public Date getLastEdited();
	public String getLastEditedFormated();
	public void setSnapshotName(String snapshotName);
	public void setLastEdited(Date lastEdited);

}
