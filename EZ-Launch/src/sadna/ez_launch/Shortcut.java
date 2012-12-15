package sadna.ez_launch;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

public class Shortcut {
	public Bitmap Photo;
	public String label;
	public String URI;
	
	public Shortcut(Bitmap icon,String label, String uri)
	{
		this.Photo = icon;
		this.label = label;
		this.URI = uri;
	}

	public Shortcut() {
		// TODO Auto-generated constructor stub
	}

	public Shortcut(Drawable applicationIcon, String packageName, Object uri2) {
		// TODO Auto-generated constructor stub
	}

	Bitmap getPhoto() {
		return Photo;
	}

	void setIcon(Bitmap icon) {
		Photo = icon;
	}

	String getLabel() {
		return label;
	}

	void setLabel(String label) {
		this.label = label;
	}
}
