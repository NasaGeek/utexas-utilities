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
	
	public ParcelablePair(F first, S second) 
	{
		this.first = first;
		this.second = second;	
	}

	public ParcelablePair(Parcel source) {
		
		first = (F) source.readValue(null);
		second = (S) source.readValue(null);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeValue(first);
		dest.writeValue(second);
	}
	
} 
