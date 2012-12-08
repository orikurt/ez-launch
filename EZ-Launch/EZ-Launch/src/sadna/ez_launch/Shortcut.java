package sadna.ez_launch;

import android.graphics.drawable.Drawable;

public class Shortcut {
	private Drawable Icon;
	private String label;
	private String name;

	public Shortcut(Drawable icon,String label, String name)
	{
		setIcon(icon);
		setLabel(label);
		setName(name);
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


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}
}
