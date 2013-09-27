package com.nasageek.utexasutilities;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Stupid wrapper for stupid Parcelable class. This is all idiotic.
 * @author Chris
 *
 * @param <T>
 */

public class SectionedParcelableList<T extends Parcelable> implements Parcelable {

	private List<ParcelablePair<String, List<T>>> list;
	private Class<T> clz;
	
	public static Parcelable.Creator<SectionedParcelableList> CREATOR = new Parcelable.Creator<SectionedParcelableList>() {

		@Override
		public SectionedParcelableList createFromParcel(Parcel source) {
			return new SectionedParcelableList(source);
		}

		@Override
		public SectionedParcelableList[] newArray(int size) {
			return new SectionedParcelableList[size];
		}		
	};
	
	public SectionedParcelableList(Parcel source) {
		int size = source.readInt();
		this.list = new ArrayList<ParcelablePair<String, List<T>>>(size);
		try {
			this.clz = (Class<T>) Class.forName(source.readString());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		for(int i = 0; i < size; i++) {
			String category = source.readString();
			ArrayList<T> subList = new ArrayList<T>();
			source.readList(subList, clz.getClassLoader());
			list.add(new ParcelablePair<String, List<T>>(category, subList));
		}
	}
	
	public SectionedParcelableList(List<ParcelablePair<String, List<T>>> list, Class<T> clz) {
		this.list = list;
		this.clz = clz;
	}

	public List<ParcelablePair<String, List<T>>> getList() {
		return this.list;
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(list.size());
		dest.writeString(clz.getName());
		for(ParcelablePair<String, List<T>> pair : list) {
			dest.writeString(pair.first);
			dest.writeList(pair.second);
		}
		
	}
}
