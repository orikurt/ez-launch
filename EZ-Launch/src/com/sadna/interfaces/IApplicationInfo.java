package com.sadna.interfaces;

import android.content.Intent;
import android.graphics.drawable.Drawable;

public interface IApplicationInfo {
	
	public Drawable getImage();
	
	public String getPackageName();
	
	public String getLabel();
	
	public Intent getLaunchInten();
}

