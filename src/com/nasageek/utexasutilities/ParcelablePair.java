package com.nasageek.utexasutilities;

import java.io.Serializable;

import com.nasageek.utexasutilities.model.Building;

import android.os.Parcel;
import android.os.Parcelable;

public class ParcelablePair<F,S> implements Serializable, Parcelable {

	static final long serialVersionUID = 1L;
	
	public final F first;
	public final S second;
	
	public static Parcelable.Creator<ParcelablePair> CREATOR = new Parcelable.Creator<ParcelablePair>(){

		@Override
		public ParcelablePair createFromParcel(Parcel source) {
			return new ParcelablePair(source);
		}

		@Override
		public ParcelablePair[] newArray(int size) {
			return new ParcelablePair[size];
		}
		
	};
	
	public ParcelablePair(F first, S second) {
		this.first = first;
		this.second = second;	
	}

	@SuppressWarnings("unchecked")
	public ParcelablePair(Parcel source) {
		
		try {
			first = (F) source.readValue(Class.forName(source.readString()).getClassLoader());
			second = (S) source.readValue(Class.forName(source.readString()).getClassLoader());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new IllegalStateException("Could not find class when unparcelling.");
		}
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(first.getClass().getName());
		dest.writeValue(first);
		dest.writeString(second.getClass().getName());
		dest.writeValue(second);
	}
	
} 
