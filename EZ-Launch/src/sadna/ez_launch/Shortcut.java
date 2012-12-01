package sadna.ez_launch;

import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

public class Shortcut implements Parcelable {

	public Shortcut(Drawable icon,String label)
	{
		setIcon(icon);
		setLabel(label);
	}
	
	private Drawable Icon;

	Drawable getIcon() {
		return Icon;
	}

	void setIcon(Drawable icon) {
		Icon = icon;
	}
	
	String getLabel() {
		return label;
	}

	void setLabel(String label) {
		this.label = label;
	}

	private String label;

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}
}
