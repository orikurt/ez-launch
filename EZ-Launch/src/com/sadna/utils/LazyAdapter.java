package com.sadna.utils;

import java.util.ArrayList;

import com.sadna.interfaces.IWidgetItemInfo;
import com.sadna.widgets.application.R;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;

public class LazyAdapter extends BaseAdapter {

	private Activity activity;
	private ArrayList<IWidgetItemInfo> data;
	private static LayoutInflater inflater=null;


	public LazyAdapter(Activity a, ArrayList<IWidgetItemInfo> d) {
		activity = a;
		data=d;
		inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

	}

	public int getCount() {
		return data.size();
	}

	public Object getItem(int position) {
		return position;
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View vi=convertView;
		
		if(convertView==null)
			vi = inflater.inflate(R.layout.list_row, null);

		final Context mContext = vi.getContext();
		TextView title = (TextView)vi.findViewById(R.id.APPtitle); // title

		//TextView status = (TextView)vi.findViewById(R.id.AppStatus); // duration
		ImageView thumb_image=(ImageView)vi.findViewById(R.id.APP_list_image); // thumb image
		//Switch onOff = (Switch)vi.findViewById(R.id.switch1);
		ImageButton leftBtn = (ImageButton)vi.findViewById(R.id.arrowLeft);
		ImageButton rightBtn = (ImageButton)vi.findViewById(R.id.arrowRight);
		final ViewFlipper vf = (ViewFlipper)vi.findViewById(R.id.viewFlipper1);
		
	

	
		leftBtn.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {

				vf.setInAnimation(AnimationUtils.loadAnimation(mContext,
						R.anim.right_in));
				vf.setOutAnimation(AnimationUtils.loadAnimation(mContext,
						R.anim.right_out));
				vf.showPrevious();
			}
		});

		rightBtn.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				
				vf.setInAnimation(AnimationUtils.loadAnimation(mContext,
						R.anim.left_in));
				vf.setOutAnimation(AnimationUtils.loadAnimation(mContext,
						R.anim.left_out));
				vf.showNext();
			}
		});
		
		IWidgetItemInfo wi = data.get(position);

		// Setting all values in listview
		title.setText(wi.getLabel());
		//artist.setText(song.get(SettingsListActivity.KEY_ARTIST));
		//status.setText(wi.getItemState().toString());
		thumb_image.setImageBitmap(wi.getBitmap(mContext));
		return vi;
	}


}