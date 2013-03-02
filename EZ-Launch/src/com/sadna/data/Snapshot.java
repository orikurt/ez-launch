package com.sadna.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import android.os.Parcel;
import android.os.Parcelable;

import com.sadna.interfaces.ISnapshotInfo;
import com.sadna.interfaces.IWidgetItemInfo;

public class Snapshot implements List<IWidgetItemInfo>, Parcelable  {

	private ISnapshotInfo snapInfo;
	private List<IWidgetItemInfo> collection;

	public Snapshot(ISnapshotInfo snapshotInfo,List<IWidgetItemInfo> lst){
		snapInfo = snapshotInfo;
		collection = lst;
	}
	public Snapshot getCopy()
	{
		Snapshot snap = new Snapshot(this.getSnapshotInfo(),new ArrayList<IWidgetItemInfo>());
		for (IWidgetItemInfo iWidgetItemInfo : collection) {
			snap.add(iWidgetItemInfo);
		}
		return snap;
	}

	public Snapshot(Parcel in) {
		if (in != null) {
			snapInfo = in.readParcelable(SnapshotInfo.class.getClassLoader());
			collection = new ArrayList<IWidgetItemInfo>();
			in.readList(collection, WidgetItemInfo.class.getClassLoader());
		}
	}
	
	public void removeDuplicateEntries()
	{
		collection = new ArrayList<IWidgetItemInfo>(new HashSet<IWidgetItemInfo>(collection));
	}

	public ISnapshotInfo getSnapshotInfo() {
		return snapInfo;
	}

	public IWidgetItemInfo getItemByName (String name) {
		if (name == null)
			return null;

		for (IWidgetItemInfo itemInfo : collection) {
			if (name.equals(itemInfo.getPackageName())) {
				return itemInfo;
			}
		}
		return null;
	}

	public void normalizeScores() {
		// Calculate root of sum-of-squares
		double rootSumOfSqaures = 0;
		for (IWidgetItemInfo itemInfo : collection) {
			rootSumOfSqaures += (itemInfo.getScore() * itemInfo.getScore());
		}
		rootSumOfSqaures = Math.sqrt(rootSumOfSqaures);
		if (rootSumOfSqaures != 0) {
			// Divide each score in root of sum-of-squares
			for (IWidgetItemInfo itemInfo : collection) {
				itemInfo.setScore(itemInfo.getScore() / rootSumOfSqaures);
			}
		}
	}

	@Override
	public boolean add(IWidgetItemInfo object) {
		return collection.add(object);
	}

	@Override
	public void add(int location, IWidgetItemInfo object) {
		collection.add(object);
	}

	@Override
	public boolean addAll(Collection<? extends IWidgetItemInfo> arg0) {
		return collection.addAll(arg0);
	}

	@Override
	public boolean addAll(int arg0, Collection<? extends IWidgetItemInfo> arg1) {
		return collection.addAll(arg0, arg1);
	}

	@Override
	public void clear() {
		collection.clear();
	}

	@Override
	public boolean contains(Object object) {
		return collection.contains(object);
	}

	@Override
	public boolean containsAll(Collection<?> arg0) {
		return collection.containsAll(arg0);
	}

	@Override
	public IWidgetItemInfo get(int location) {
		return collection.get(location);
	}

	@Override
	public int indexOf(Object object) {
		return collection.indexOf(object);
	}

	@Override
	public boolean isEmpty() {
		return collection.isEmpty();
	}

	@Override
	public Iterator<IWidgetItemInfo> iterator() {
		return collection.iterator();
	}

	@Override
	public int lastIndexOf(Object object) {
		return collection.lastIndexOf(object);
	}

	@Override
	public ListIterator<IWidgetItemInfo> listIterator() {
		return collection.listIterator();
	}

	@Override
	public ListIterator<IWidgetItemInfo> listIterator(int location) {
		return collection.listIterator(location);
	}

	@Override
	public IWidgetItemInfo remove(int location) {
		return collection.remove(location);
	}

	@Override
	public boolean remove(Object object) {
		return collection.remove(object);
	}

	@Override
	public boolean removeAll(Collection<?> arg0) {
		return collection.removeAll(arg0);
	}

	@Override
	public boolean retainAll(Collection<?> arg0) {
		return collection.retainAll(arg0);
	}

	@Override
	public IWidgetItemInfo set(int location, IWidgetItemInfo object) {
		return collection.set(location, object);
	}

	@Override
	public int size() {
		return collection.size();
	}

	@Override
	public List<IWidgetItemInfo> subList(int start, int end) {
		return collection.subList(start, end);
	}

	@Override
	public Object[] toArray() {
		return collection.toArray();
	}

	@Override
	public <T> T[] toArray(T[] array) {
		return collection.toArray(array);
	}

	public static final Parcelable.Creator<Snapshot> CREATOR
	= new Parcelable.Creator<Snapshot>() {
		public Snapshot createFromParcel(Parcel in) {
			return new Snapshot(in);
		}

		public Snapshot[] newArray(int size) {
			return new Snapshot[size];
		}
	};

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeParcelable(snapInfo, 0);
		dest.writeList(collection);
	}

	@Override 
	public String toString(){
		StringBuilder sb =new StringBuilder(); 
		sb.append(snapInfo.getSnapshotName() + "\n");
		sb.append("( ");
		for (IWidgetItemInfo wid : collection) {
			sb.append(wid.getPackageName() + " , ");
		}
		sb.append(" )");
		return sb.toString();
	}
}