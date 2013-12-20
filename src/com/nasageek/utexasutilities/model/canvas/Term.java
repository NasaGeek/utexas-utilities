package com.nasageek.utexasutilities.model.canvas;

import android.os.Parcel;
import android.os.Parcelable;

public class Term implements Parcelable {

	private String id;
	private String name;
	private String start_at;
	private String end_at;
	
	public static Parcelable.Creator<Term> CREATOR = new Parcelable.Creator<Term>() {

		@Override
		public Term createFromParcel(Parcel source) {
			return new Term(source);
		}

		@Override
		public Term[] newArray(int size) {
			return new Term[size];
		}	
	};
	
	public Term(Parcel in) {
		id = in.readString();
		name = in.readString();
		start_at = in.readString();
		end_at = in.readString();
	}
	
	public String getName() {
		String[] splitname = name.split(" ");
		return splitname[1] + " " + splitname[0];
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(id);
		dest.writeString(name);
		dest.writeString(start_at);
		dest.writeString(end_at);
	}
}
