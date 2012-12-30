package com.android.data;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

import com.sadna.interfaces.IWidgetItemInfo;

public class WidgetItemInfo implements IWidgetItemInfo{

	//private Drawable image;
	private String packageName;
	private String label;
	private Intent launchIntent;
	private double score;


	public WidgetItemInfo(String name,String label,double score) {
		this(name,null,label,score);
	}

	public WidgetItemInfo(String name, Intent launchIntent,String label) {
		this(name,launchIntent,label,0);
	}
	public WidgetItemInfo(String name, Intent launchIntent,String label,double score) {
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
	public Drawable getImage(Context c) {
		try {
			return c.getPackageManager().getApplicationIcon(packageName);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	@Override
	public Bitmap getBitmap(Context c) {
		// TODO Auto-generated method stub
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
		//ByteArrayOutputStream stream = new ByteArrayOutputStream();
		//bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
		//byte[] byteArray = stream.toByteArray();
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
