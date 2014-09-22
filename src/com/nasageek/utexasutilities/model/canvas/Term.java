
package com.nasageek.utexasutilities.model.canvas;

import java.io.Serializable;

import android.os.Parcel;
import android.os.Parcelable;

public class Term implements Parcelable, Serializable {

    private static final long serialVersionUID = 1L;
    private String id;
    private String name;

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
    }

    public String getName() {
        return name;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
    }
}
