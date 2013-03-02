package com.sadna.interfaces;

import java.util.Date;

import android.os.Parcelable;

public interface ISnapshotInfo extends Parcelable{
	public String getSnapshotName();
	public Date getLastEdited();
	public String getLastEditedFormated();
	public void setSnapshotName(String snapshotName);
	public void setLastEdited(Date lastEdited);
	@Override
	public boolean equals(Object o);
	@Override
	public int hashCode();

}