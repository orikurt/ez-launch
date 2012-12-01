package sadna.ez_launch;

import android.graphics.drawable.Drawable;

public class Shortcut {

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
}
