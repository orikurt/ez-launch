package com.sadna.utils;

import java.util.ArrayList;
import java.util.List;

import com.sadna.enums.ItemState;
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
import android.widget.Filter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;

public class LazyAdapterStatistics extends LazyAdapterBase {

	private Activity activity;
	private List<IWidgetItemInfo> data;
	private List<IWidgetItemInfo> originalData;
	private Filter filter = null;
	private static LayoutInflater inflater=null;


	public LazyAdapterStatistics(Activity a, List<IWidgetItemInfo> d) {
		activity = a;
		data=originalData=d;
		inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		filter = new filter_here();
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
			vi = inflater.inflate(R.layout.list_row_statistics, null);

		final Context mContext = vi.getContext();
		TextView title = (TextView)vi.findViewById(R.id.APPtitle); // title

		ImageView thumb_image=(ImageView)vi.findViewById(R.id.APP_list_image); // thumb image


		final TextView scoreText = (TextView)vi.findViewById(R.id.AppScore); // duration
		
		
		final IWidgetItemInfo wi = data.get(position);
		
		
		// Setting all values in listview
		title.setText(wi.getLabel());
		scoreText.setText(String.format(mContext.getString(R.string.score_base), wi.getScore()));


		thumb_image.setImageBitmap(wi.getBitmap(mContext));
		return vi;
	}


	
	
    public Filter getFilter() {
        return filter ;
    }
	
	public class filter_here extends Filter{

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            FilterResults Result = new FilterResults();
            if(constraint.length() == 0 ){
                Result.values = originalData;
                Result.count = originalData.size();
                return Result;
            }

            List<IWidgetItemInfo> filteredData = new ArrayList<IWidgetItemInfo>();
            String filterString = constraint.toString().toLowerCase();
            IWidgetItemInfo filterableString;

            for(int i = 0; i<originalData.size(); i++){
                filterableString = originalData.get(i);
                if(filterableString.getLabel().toLowerCase().contains(filterString)){
                    filteredData.add(filterableString);
                }
            }
            Result.values = filteredData;
            Result.count = filteredData.size();

            return Result;
        }

        @SuppressWarnings("unchecked")
		@Override
        protected void publishResults(CharSequence constraint,FilterResults results) {
        		data = ((List<IWidgetItemInfo>) results.values);	
            notifyDataSetChanged();
        }

    }
}