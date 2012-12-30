package com.sadna.data;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.os.Parcel;
import android.os.Parcelable;

import com.sadna.interfaces.ISnapshotInfo;

public class SnapshotInfo implements ISnapshotInfo {
	private String snapshotName;
	private Date lastEdited;

	public SnapshotInfo(String name, Date lastEdit) {
		this.snapshotName = name;
		this.lastEdited = lastEdit;
	}
	public SnapshotInfo(Parcel in) {
		snapshotName = in.readString();
		lastEdited = (Date) in.readSerializable();
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

	@Override
	public String getLastEditedFormated() {
		DateFormat df = new SimpleDateFormat(DataManager.DATE_FORMAT,Locale.getDefault());
		// TODO Auto-generated method stub
		return df.format(lastEdited);
	}
	@Override 
	public boolean equals(Object o){
		try {
			return ((SnapshotInfo)o).getSnapshotName().equalsIgnoreCase(snapshotName);
		} catch (ClassCastException e) {
			return false;
		}
	}
	@Override
    public int hashCode() {
        return getSnapshotName().hashCode();
    }




	
	public static final Parcelable.Creator<SnapshotInfo> CREATOR
	= new Parcelable.Creator<SnapshotInfo>() {
		public SnapshotInfo createFromParcel(Parcel in) {
			return new SnapshotInfo(in);
		}

		public SnapshotInfo[] newArray(int size) {
			return new SnapshotInfo[size];
		}
	};

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(snapshotName);
		dest.writeSerializable(lastEdited);
	}
}
