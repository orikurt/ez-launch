package com.sadna.interfaces;

import android.content.Intent;
import android.graphics.drawable.Drawable;

public interface IWidgetItemInfo {
	
	public Drawable getImage();
	public void setImage(Drawable image);
	
	public String getPackageName();
	public void setPackageName(String packageName);
	
	public String getLabel();
	public void setLabel(String label);
	
	public Intent getLaunchIntent();
	public void setLaunchIntent(Intent launchIntent);
	
	public double getScore();
	public void setScore(double newScore);
}

