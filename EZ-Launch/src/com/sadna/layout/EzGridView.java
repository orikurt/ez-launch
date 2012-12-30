package com.sadna.layout;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.GridView;

public class EzGridView extends GridView {

	public EzGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setListener();
	}
	public EzGridView(Context context) {
		super(context);
		setListener();
	}

	public EzGridView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setListener();
	}
	private void setListener() {
		setOnTouchListener(new OnTouchListener(){
			 @Override
			 public boolean onTouch(View v, MotionEvent event) {
				 if(event.getAction() == MotionEvent.ACTION_MOVE){
					 return true;
				 }
				 return false;
			 }

		});
	}
	
	



	

}
