package com.android.data;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

import com.sadna.interfaces.IWidgetItemInfo;

public class WidgetItemInfo implements IWidgetItemInfo{

	private Drawable image;
	private String packageName;
	private String label;
	private Intent launchIntent;
	private double score;


	public WidgetItemInfo(String name,String label,double score) {
		this(null,name,null,label,score);
	}

	public WidgetItemInfo(Drawable image, String name, Intent launchIntent,String label) {
		this(image,name,launchIntent,label,0);
	}
	public WidgetItemInfo(Drawable image, String name, Intent launchIntent,String label,double score) {
		this.image = image;
		this.packageName = name;
		this.launchIntent = launchIntent;
		this.label = label;
		this.score = score;
	}
	public WidgetItemInfo(Parcel in) {
		packageName = in.readString();
		label = in.readString();
		launchIntent = in.readParcelable(Intent.class.getClassLoader());
		score = in.readDouble();
	}

	@Override
	public Drawable getImage() {
		return image;
	}
	@Override
	public void setImage(Drawable image) {
		this.image = image;
	}
	@Override
	public String getPackageName() {
		return packageName;
	}
	@Override
	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}
	@Override
	public String getLabel() {
		return label;
	}
	@Override
	public void setLabel(String label) {
		this.label = label;
	}
	@Override
	public Intent getLaunchIntent() {
		return launchIntent;
	}
	@Override
	public void setLaunchIntent(Intent launchIntent) {
		this.launchIntent = launchIntent;
	}
	@Override
	public double getScore() {
		return score;
	}
	@Override
	public void setScore(double score) {
		this.score = score;
	}
	
	public static final Parcelable.Creator<WidgetItemInfo> CREATOR
	= new Parcelable.Creator<WidgetItemInfo>() {
		public WidgetItemInfo createFromParcel(Parcel in) {
			return new WidgetItemInfo(in);
		}

		public WidgetItemInfo[] newArray(int size) {
			return new WidgetItemInfo[size];
		}
	};
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		/*dest.writeParcelable(image, flags);*/
		dest.writeString(packageName);
		dest.writeString(label);
		dest.writeParcelable(launchIntent, flags);
		dest.writeDouble(score);

	}	

}
