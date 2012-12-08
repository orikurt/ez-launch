package sadna.ez_launch;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

public class Shortcut {
	public Drawable Icon;
	public String label;
	public String URI;
	
	public Shortcut(Drawable icon,String label, String uri)
	{
		this.Icon = icon;
		this.label = label;
		this.URI = uri;
	}

	public Shortcut() {
		// TODO Auto-generated constructor stub
	}

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
}
