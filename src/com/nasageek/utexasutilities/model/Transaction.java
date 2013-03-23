package com.nasageek.utexasutilities.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Transaction implements Parcelable {

	private String cost, reason, date;
	
	public static Parcelable.Creator<Transaction> CREATOR = new Parcelable.Creator<Transaction>() {

		@Override
		public Transaction createFromParcel(Parcel source) {
			return new Transaction(source);
		}

		@Override
		public Transaction[] newArray(int size) {
			return new Transaction[size];
		}
		
	};
	
	public Transaction(Parcel in) {
		reason = in.readString();
		cost = in.readString();
		date = in.readString();
	}
	
	public Transaction(String reason, String cost, String date) {
		this.reason = reason;
		this.cost = cost;
		this.date = date;
	}
	public String getReason() {
		return this.reason;
	}
	public String getCost() {
		return this.cost;
	}
	public String getDate() {
		return this.date;
	}
	@Override
	public int describeContents() {
		return 0;
	}
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(reason);
		dest.writeString(cost);
		dest.writeString(date);
	}
}
