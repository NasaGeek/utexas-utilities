package com.nasageek.utexasutilities.model.canvas;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class Section implements Parcelable, Serializable {

    private static final long serialVersionUID = 1L;

    public static Parcelable.Creator<Section> CREATOR = new Parcelable.Creator<Section>() {

        @Override
        public Section createFromParcel(Parcel source) {
            return new Section(source);
        }

        @Override
        public Section[] newArray(int size) {
            return new Section[size];
        }
    };

    private int id;
    private String name;

    public Section(Parcel in) {
        id = in.readInt();
        name = in.readString();
    }

    public String getName() {
        // section name looks like "C S350C(53072)", extract unique number out of parentheses
        int left = name.lastIndexOf('(');
        int right = name.lastIndexOf(')');

        if (left == -1 || right == -1) {
            return null;
        }

        return name.substring(left + 1, right);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
    }
}
