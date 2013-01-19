package com.sadna.UI;

import android.content.Context;

public class RangeSeekBardDouble extends RangeSeekBar<Double>{

	public RangeSeekBardDouble(Double absoluteMinValue,
			Double absoluteMaxValue, Context context)
			throws IllegalArgumentException {
		super(absoluteMinValue, absoluteMaxValue, context);
		// TODO Auto-generated constructor stub
	}
	
	public RangeSeekBardDouble(Context context)
	{
		this(0.0,23.5,context);
	}

}
