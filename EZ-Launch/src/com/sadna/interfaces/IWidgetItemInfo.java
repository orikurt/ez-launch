package com.sadna.interfaces;

import android.content.Intent;
import android.graphics.drawable.Drawable;

public interface IWidgetItemInfo {
	
	public Drawable getImage();
	
	public String getPackageName();
	
	public String getLabel();
	
	public Intent getLaunchIntent();
	
	public double getScore();
	
	public void setScore(double newScore);
}

