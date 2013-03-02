package com.sadna.utils;
import android.widget.BaseAdapter;
import android.widget.Filter;

public abstract class LazyAdapterBase extends BaseAdapter {
	 public abstract Filter getFilter();
}

