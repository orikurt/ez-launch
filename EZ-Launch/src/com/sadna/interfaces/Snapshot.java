package com.sadna.interfaces;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public abstract class Snapshot implements List<IWidgetItemInfo>{

	private ISnapshotInfo snapInfo;
	private List<IWidgetItemInfo> collection;
	
	
	public ISnapshotInfo getSnapshot(){
		return snapInfo;
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
		return containsAll(arg0);
	}

	@Override
	public IWidgetItemInfo get(int location) {
		return get(location);
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
	

}
