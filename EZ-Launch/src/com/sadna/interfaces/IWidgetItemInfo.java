package com.sadna.interfaces;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;

public interface IWidgetItemInfo extends Parcelable, Comparable<IWidgetItemInfo>{
	
	public Drawable getImage(Context c);
	public Bitmap getBitmap(Context c);
	
	public String getPackageName();
	public void setPackageName(String packageName);
	
	public String getLabel();
	public void setLabel(String label);
	
	public Intent getLaunchIntent();
	public void setLaunchIntent(Intent launchIntent);
	
	public double getScore();
	public void setScore(double newScore);
}

