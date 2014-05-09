
package com.nasageek.utexasutilities.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class Building implements Parcelable, Serializable {
    private static final long serialVersionUID = 1L;
    String id;
    String room;

    public static Parcelable.Creator<Building> CREATOR = new Parcelable.Creator<Building>() {

        @Override
        public Building createFromParcel(Parcel source) {
            return new Building(source);
        }

        @Override
        public Building[] newArray(int size) {
            return new Building[size];
        }

    };

    private Building(Parcel in) {
        id = in.readString();
        room = in.readString();
    }

    public Building(String i, String r) {
        id = i;
        room = r;
    }

    public String getRoom() {
        return room;
    }

    public String getId() {
        return id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(room);
    }
}
