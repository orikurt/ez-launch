package sadna.ez_launch;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.database.DataSetObserver;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;

public class ImageAdapter extends BaseAdapter {
    private Context mContext;
    private LayoutInflater li;
    private List<Shortcut> mList;
    //private List<Integer> mList;
    public ImageAdapter(Context c,List<Shortcut> mList2, LayoutInflater li) {
        mContext = c;
        mList = mList2;
        this.li = li;
    }

    public int getCount() {
        return mList.size();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        
    	View v = convertView;
    	ImageView imageView;
    	TextView textView;
        if (v == null) {  // if it's not recycled, initialize some attributes

//        	LayoutInflater inflater = (LayoutInflater)mContext.getSystemService
//        		      (Context.LAYOUT_INFLATER_SERVICE);
        	
        	//LayoutInflater inflater = ((Activity)mContext).getLayoutInflater();
            v = (LinearLayout)li.inflate(R.layout.grid_item, null,true);// View.inflate(mContext, position, parent);// View.inflate(R.layout.grid_item, null);
            v.setLayoutParams(new GridView.LayoutParams(85, 85));
            //imageView = new ImageView(mContext);
            imageView = (ImageView)v.findViewById(R.id.grid_item_image);
            //imageView.setLayoutParams(new GridView.LayoutParams(85, 85));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            //imageView.setPadding(8, 8, 8, 8);
            
            textView = (TextView) v.findViewById(R.id.app_label);
            
            
            
        } else {
        	imageView = (ImageView) v.findViewById(R.id.grid_item_image);
        	textView = (TextView) v.findViewById(R.id.app_label);
        }
        

        imageView.setImageDrawable(mList.get(position).getIcon());
        textView.setText(mList.get(position).getName());
        return v;
    }

    // references to our images
//    private Integer[] mThumbIds = {
//            R.drawable.sample_2, R.drawable.sample_3,
//            R.drawable.sample_4, R.drawable.sample_5,
//            R.drawable.sample_6, R.drawable.sample_7,
//            R.drawable.sample_0, R.drawable.sample_1,
//            R.drawable.sample_2, R.drawable.sample_3,
//            R.drawable.sample_4, R.drawable.sample_5,
//            R.drawable.sample_6, R.drawable.sample_7,
//            R.drawable.sample_0, R.drawable.sample_1,
//            R.drawable.sample_2, R.drawable.sample_3,
//            R.drawable.sample_4, R.drawable.sample_5,
//            R.drawable.sample_6, R.drawable.sample_7
//    };
}