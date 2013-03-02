package com.sadna.data;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.Drawable;
import android.media.audiofx.Equalizer;
import android.os.Parcel;
import android.os.Parcelable;

import com.sadna.enums.ItemState;
import com.sadna.interfaces.IWidgetItemInfo;

public class WidgetItemInfo implements IWidgetItemInfo{

	public static final String BLACK_LIST_APP_INTENT = "BlackListAppIntent";

	public static final String LAUNCH_INTENT = "com.sadna.data.LAUNCH_INTENT";
	
	private String packageName;
	private String label;
	private double score;

	private ItemState itemState;

	private Date lastUsed;

	public WidgetItemInfo(String name, String label) {
		this(name,label,1);
	}
	
	public WidgetItemInfo(String name, String label,double score,ItemState itemState,Date lastUsed) {
		this.packageName = name;
		this.label = label;
		this.score = score;
		this.itemState = itemState;
		this.lastUsed = lastUsed;
	}
	
	public WidgetItemInfo(String name, String label,double score) {
		this(name,label,score,ItemState.AUTO,new Date());
	}
	
	public WidgetItemInfo(Parcel in) {
		packageName = in.readString();
		label = in.readString();
		score = in.readDouble();
	}

	@Override
	public Drawable getImage(Context c) {
		try {
			return c.getPackageManager().getApplicationIcon(packageName);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	@Override
	public Bitmap getBitmap(Context c) {
		return getImage(getImage(c));
	}	

	private static Bitmap getImage(Drawable icon)
	{
		if (icon == null) {
			return null;
		}
		Bitmap bmp = Bitmap.createBitmap(icon.getIntrinsicWidth(), icon.getIntrinsicHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(bmp); 
		icon.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
		icon.draw(canvas);
		return bmp;
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
	public Intent getLaunchIntent(Context c) {
		Intent ret = c.getPackageManager().getLaunchIntentForPackage(getPackageName());
		if (ret == null) {
			Intent intent = new Intent();
			intent.addCategory(BLACK_LIST_APP_INTENT);
			intent.setPackage(getPackageName());
			return intent;
		}
		return ret;
	}
	
	@Override
	/**
	 * We are trying a new method
	 * we normalize in here 
	 * */
	public double getScore() {
		long diff = (new Date().getTime()) - getLastUse().getTime();
		long days = TimeUnit.MILLISECONDS.toDays(diff);
		return score * Math.pow((99.0/100.0), days);
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
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(packageName);
		dest.writeString(label);
		dest.writeDouble(score);

	}

	@Override
	public int compareTo(IWidgetItemInfo another) {
		if (getScore() - another.getScore() < 0)
			return 1;
		if (getScore() - another.getScore() > 0)
			return -1;
		
		return 0;
	}
	@Override
	public void setItemState(ItemState iState) {
		this.itemState = iState;
		
	}
	@Override
	public ItemState getItemState() {
		return itemState;
	}

	@Override
	public Date getLastUse() {
		return this.lastUsed;
	}

	@Override
	public void setLastUse(Date lastUse) {
		this.lastUsed = lastUse;
	}

	@Override
	public String getLastUsedFormated() {
		DateFormat df = new SimpleDateFormat(DataManager.DATE_FORMAT,Locale.getDefault());
		return df.format(getLastUse());
	}
	
	@Override 
	public boolean equals(Object o){
		try {
			return ((IWidgetItemInfo)o).getPackageName().equalsIgnoreCase(getPackageName());
		} catch (ClassCastException e) {
			return false;
		}
	}
	@Override
    public int hashCode() {
        return getPackageName().hashCode();
    }
}