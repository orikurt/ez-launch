package com.sadna.data;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Parcel;

import com.sadna.enums.ItemState;
import com.sadna.interfaces.IWidgetItemInfo;
import com.sadna.ui.SettingsActivity;
import com.sadna.widgets.application.R;

public class ConfigurationItemInfo implements IWidgetItemInfo{

	public static final String COM_SADNA_WIDGETS_APPLICATION_CONFIGURATION = "com.sadna.widgets.application.configuration";

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		}

	@Override
	public Drawable getImage(Context c) {
		// TODO Auto-generated method stub
		return c.getResources().getDrawable(R.drawable.icon);
	}

	@Override
	public Bitmap getBitmap(Context c) {
		Drawable image = getImage(c);
		if (image == null) {
			return null;
		}
		Bitmap bmp = Bitmap.createBitmap(image.getIntrinsicWidth(), image.getIntrinsicHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(bmp); 
		image.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
		image.draw(canvas);
		//ByteArrayOutputStream stream = new ByteArrayOutputStream();
		//bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
		//byte[] byteArray = stream.toByteArray();
		return bmp;
	}

	@Override
	public String getPackageName() {
		return COM_SADNA_WIDGETS_APPLICATION_CONFIGURATION;
	}

	@Override
	public void setPackageName(String packageName) {
		
		
	}

	@Override
	public String getLabel() {
		return "Settings";
	}

	@Override
	public void setLabel(String label) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Intent getLaunchIntent(Context c) {
		// TODO Auto-generated method stub
		return new Intent(c, SettingsActivity.class);
	}



	@Override
	public double getScore() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setScore(double newScore) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int compareTo(IWidgetItemInfo another) {
		return -1;
	}

	@Override
	public void setItemState(ItemState iState) {
		
	}

	@Override
	public ItemState getItemState() {
		return ItemState.MUST;
	}

	@Override
	public Date getLastUse() {
		return new Date();
	}

	@Override
	public void setLastUse(Date lastUse) {
		
	}

	@Override
	public String getLastUsedFormated() {
		DateFormat df = new SimpleDateFormat(DataManager.DATE_FORMAT,Locale.getDefault());
		// TODO Auto-generated method stub
		return df.format(getLastUse());
	}

}
