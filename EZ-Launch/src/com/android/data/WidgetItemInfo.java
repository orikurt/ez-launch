package com.android.data;

import android.content.Intent;
import android.graphics.drawable.Drawable;

import com.sadna.interfaces.IWidgetItemInfo;

public class WidgetItemInfo implements IWidgetItemInfo{

	
	private Drawable image;
	private String packageName;
	private String label;
	private Intent launchIntent;
	private double score;
	
	
	
	public WidgetItemInfo(Drawable image, String name, Intent launchIntent,
			String label) {
		this.image = image;
		this.packageName = name;
		this.launchIntent = launchIntent;
		this.label = label;
		this.score = 0;
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
	
	
	


	
	
	
}
