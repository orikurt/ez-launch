package com.sadna.widgets.application;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.sadna.android.content.LauncherIntent;
import com.sadna.data.ConfigurationItemInfo;
import com.sadna.data.DataManager;
import com.sadna.data.Snapshot;
import com.sadna.data.SnapshotInfo;
import com.sadna.data.WidgetItemInfo;
import com.sadna.interfaces.IDataManager;
import com.sadna.interfaces.ISnapshotInfo;
import com.sadna.interfaces.IWidgetItemInfo;
import com.sadna.service.StatisticsService;



import android.annotation.SuppressLint;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.BitmapFactory;
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
	public static final String TAG = "sadna.ContactRemoteViewsFactory";
	private Context mContext;
	private int mAppWidgetId;
	private int[] mAppWidgetIds;
	private Snapshot mData = null;
	private DataManager dm;

	public ContactRemoteViewsFactory(Context context, Intent intent) {
		Log.d(TAG, "ContactRemoteViewsFactory created");
		mContext = context;
		mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
				AppWidgetManager.INVALID_APPWIDGET_ID);
		mAppWidgetIds = Preferences.getAllWidgetIds(context);
		if (dm == null) {
			dm = new DataManager(mContext);
		}
		int id = getSnapshotID();
		mData = dm.getSelectedSnapshotFiltered(id);
	}
	@Override
	public void onCreate() {
	}

	@Override
	public void onDestroy() {
	}

	private int getSnapshotID() {
		mAppWidgetIds = Preferences.getAllWidgetIds(mContext);
		if (mAppWidgetIds == null) {
			Log.d(TAG, "mAppWidgetIds is null");
			return -1;
		}
		for (int i=0; i<mAppWidgetIds.length; i++) {
			if (mAppWidgetIds[i] == mAppWidgetId)
				return i;
		}
		return -1;
	}

	public int getCount() {
		if (mData == null){
			return 0;
		}
		return mData.size();
	}

	@Override
	public RemoteViews getViewAt(int position) {
		// position will always range from 0 to getCount() - 1.
		IWidgetItemInfo item = mData.get(position);

		int itemresid = R.layout.gridviewitem_ics;
		// We construct a remote views item based on our widget item xml file, and set the
		// text based on the position.
		RemoteViews rv = new RemoteViews(mContext.getPackageName(), itemresid);

			rv.setTextViewText(R.id.displayname, item.getLabel());			

		rv.setImageViewBitmap(R.id.photo, item.getBitmap(mContext.getApplicationContext()));

		// Next, we set a fill-intent which will be used to fill-in the pending intent template
		// which is set on the collection view in StackWidgetProvider.
		Bundle extras = new Bundle();
		extras.putParcelable(com.sadna.data.WidgetItemInfo.LAUNCH_INTENT, item.getLaunchIntent(mContext.getApplicationContext()));
		Intent fillInIntent = new Intent();
		fillInIntent.putExtras(extras);
		rv.setOnClickFillInIntent(R.id.displayname, fillInIntent);
		rv.setOnClickFillInIntent(R.id.photo, fillInIntent);

		// Return the remote views object.
		return rv;
	}

	public RemoteViews getLoadingView() {
		if ((mData == null) || (mData.size() < 1)) {
			return null;
		}
		IWidgetItemInfo item = mData.get(0);
		int itemresid = R.layout.gridviewitem_ics;
		RemoteViews rv = new RemoteViews(mContext.getPackageName(), itemresid);
		rv.setTextViewText(R.id.displayname, "Loading");			
		rv.setImageViewBitmap(R.id.photo,BitmapFactory.decodeResource(mContext.getResources(), R.drawable.loading));
		return rv;
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

	private List<IWidgetItemInfo> getInstalledAppsInfo() {
		List<IWidgetItemInfo> result = new ArrayList<IWidgetItemInfo>();

		final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		final List<ResolveInfo> pkgAppsList = mContext.getPackageManager().queryIntentActivities(mainIntent, 0);
		PackageManager packageManager =  mContext.getPackageManager();
		for (ResolveInfo resolveInfo : pkgAppsList) {

			String itemLabel = resolveInfo.loadLabel(packageManager).toString();
			String itemPkgName = resolveInfo.activityInfo.packageName;
			IWidgetItemInfo itemInfo = new WidgetItemInfo(itemPkgName, itemLabel);
			result.add(itemInfo);
		}

		return result;
	}
	@Override 
	public void onDataSetChanged() {
		Log.d(TAG, "Start Query!");

		if (dm == null) {
			dm = new DataManager(mContext);
		}

		int id = getSnapshotID();
		mData = dm.getSelectedSnapshotFiltered(id);
		Log.d(TAG, "id="+id);
		if (mData == null) {
			Date currDate = new Date();
			ISnapshotInfo snapshotInfo = new SnapshotInfo(currDate.toString(), currDate);
			mData = dm.getSelectedSnapshotFiltered(new Snapshot(snapshotInfo, getInstalledAppsInfo()), id);
		}
	}
}