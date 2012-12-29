package com.sadna.widgets.application;

import com.android.data.Snapshot;
import com.sadna.android.content.LauncherIntent;
import com.sadna.interfaces.IWidgetItemInfo;
import com.sadna.service.StatisticsService;



import android.annotation.SuppressLint;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

@SuppressLint("NewApi")
public class ContactWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ContactRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}

class ContactRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    public static final String TAG = "boombuler.ContactRemoteViewsFactory";
    private Context mContext;
    private int mAppWidgetId;
	private Snapshot mData = null;
	//private int mDefWidth;
	//private Bitmap mFallbackImage;

    public ContactRemoteViewsFactory(Context context, Intent intent) {
		Log.d(TAG, "ContactRemoteViewsFactory created");
        mContext = context;
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
        mData = intent.getParcelableExtra(StatisticsService.NEW_SNAPSHOT);
        //mDefWidth = intent.getIntExtra(ImplHC.EXTRA_DEFAULT_WIDTH, 1);
    }
    @Override
    public void onCreate() {    
    	
    }

    @Override
    public void onDestroy() {/*
		if (mData != null) {
			for (ContactData data : mData) {
				if (data.Photo != mFallbackImage) 
					data.Photo.recycle();
			}
			mData = null;
		}
		if (mFallbackImage != null) {
			mFallbackImage.recycle();
			mFallbackImage = null;
		}*/
    }

    public int getCount() {
    	if (mData == null){
    		return 0;
    	}
		return mData.size();
    }

    
    @Override
    public RemoteViews getViewAt(int position) {
		Log.d(TAG, "get item at position: "+ position);
        // position will always range from 0 to getCount() - 1.
		IWidgetItemInfo item = mData.get(position);
		
		boolean isICS = Preferences.getBGImage(mContext, mAppWidgetId) == Preferences.BG_ICS;
		int textVisibility = Preferences.getShowName(mContext, mAppWidgetId) ? View.VISIBLE : View.GONE;		
		int itemresid = R.layout.gridviewitem_hc;
		if (isICS) {
			itemresid = R.layout.gridviewitem_ics;
		} else {
			if (textVisibility == View.VISIBLE) {
				switch(Preferences.getTextAlign(mContext, mAppWidgetId)) {
					case Preferences.ALIGN_RIGHT:
						itemresid = R.layout.gridviewitem_txt_right; break;
					case Preferences.ALIGN_LEFT:
						itemresid = R.layout.gridviewitem_txt_left; break;
				}
			}
		}
		// We construct a remote views item based on our widget item xml file, and set the
        // text based on the position.
        RemoteViews rv = new RemoteViews(mContext.getPackageName(), itemresid);
		
		if (textVisibility == View.VISIBLE) {
			rv.setTextViewText(R.id.displayname, item.getLabel());			
		} else {
			rv.setViewVisibility(R.id.displayname, textVisibility);
			if (isICS) 
				rv.setViewVisibility(R.id.label_overlay, textVisibility);
		}
		
		rv.setImageViewBitmap(R.id.photo, item.getBitmap(mContext));
		
        // Next, we set a fill-intent which will be used to fill-in the pending intent template
        // which is set on the collection view in StackWidgetProvider.
        Bundle extras = new Bundle();
        extras.putString(LauncherIntent.Extra.Scroll.EXTRA_ITEM_POS, item.getPackageName());
        Intent fillInIntent = new Intent();
        fillInIntent.putExtras(extras);
        rv.setOnClickFillInIntent(R.id.displayname, fillInIntent);
		rv.setOnClickFillInIntent(R.id.photo, fillInIntent);

        // Return the remote views object.
        return rv;
    }

    public RemoteViews getLoadingView() {
        // You can create a custom loading view (for instance when getViewAt() is slow.) If you
        // return null here, you will get the default loading view.
        return null;
    }

    public int getViewTypeCount() {
        return 1;
    }

    public long getItemId(int position) {
        return position;
    }

    public boolean hasStableIds() {
        return false;
    }

    @Override 
    public void onDataSetChanged() {
		Log.d(TAG, "Start Query!");
		
		//onDestroy();
		
		/*
		Uri dataUri = DataProvider.CONTENT_URI_MESSAGES.buildUpon().appendEncodedPath(Integer.toString(mAppWidgetId)).build();
	
		DataProvider prov = new DataProvider();
		Cursor cursor = prov.query(dataUri, DataProvider.PROJECTION_APPWIDGETS, null, null, null);

		Log.d(TAG, "Found: "+cursor.getCount());
		final boolean isICS = Preferences.getBGImage(mContext, mAppWidgetId) == Preferences.BG_ICS;
		boolean autosizeImages = !isICS;
		if(autosizeImages && Preferences.getShowName(mContext, mAppWidgetId)) {
			autosizeImages = false;
			if (Preferences.getTextAlign(mContext, mAppWidgetId) == Preferences.ALIGN_CENTER)
				autosizeImages = true;
		}
		Options options = new Options();
        options.inPreferredConfig = Config.ARGB_8888;
        
        mFallbackImage = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.no_image, options);
		if (autosizeImages) {
			final int width = ContactWidget.calcWidthPixel(mContext, mAppWidgetId, mDefWidth);
			mFallbackImage = ThumbnailUtils.extractThumbnail(mFallbackImage, width, width, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
		}
		else if (isICS) {
			final int width = ContactWidget.getICSWidth(mContext);
			mFallbackImage = ThumbnailUtils.extractThumbnail(mFallbackImage, width, width, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
		}
        
        LinkedList<ContactData> contacts = new LinkedList<ContactData>();
        if (cursor.moveToFirst()) {
        	while(!cursor.isAfterLast()) {
        		ContactData item = new ContactData();
        		contacts.add(item);
		
        		item.Name = cursor.getString(DataProvider.DataProviderColumns.name.ordinal());
        		item.URI = cursor.getString(DataProvider.DataProviderColumns.contacturi.ordinal());
		
				byte[] blob = cursor.getBlob(DataProvider.DataProviderColumns.photo.ordinal());

				if (blob != null && blob.length > 0) {		
					item.Photo = BitmapFactory.decodeByteArray(blob, 0, blob.length, options);
				}
				if (item.Photo == null) {
					item.Photo = mFallbackImage;
				} else if (autosizeImages || isICS) {
					final int width = isICS ? ContactWidget.getICSWidth(mContext) : 
						ContactWidget.calcWidthPixel(mContext, mAppWidgetId, mDefWidth);
					item.Photo = ThumbnailUtils.extractThumbnail(item.Photo, width, width, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
				}
				cursor.moveToNext();
        	}
        	mData = contacts; 
        }
        cursor.close();
        */
    }
}