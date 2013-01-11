package com.sadna.utils;

import java.util.ArrayList;
import com.sadna.interfaces.IWidgetItemInfo;
import com.sadna.widgets.application.R;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

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

           TextView title = (TextView)vi.findViewById(R.id.APPtitle); // title
           
           TextView status = (TextView)vi.findViewById(R.id.AppStatus); // duration
           ImageView thumb_image=(ImageView)vi.findViewById(R.id.APP_list_image); // thumb image
           
        
        IWidgetItemInfo wi = data.get(position);
        
        // Setting all values in listview
        title.setText(wi.getLabel());
        //artist.setText(song.get(SettingsListActivity.KEY_ARTIST));
        status.setText(wi.getItemState().toString());
        thumb_image.setImageBitmap(wi.getBitmap(vi.getContext()));
        return vi;
    }
}