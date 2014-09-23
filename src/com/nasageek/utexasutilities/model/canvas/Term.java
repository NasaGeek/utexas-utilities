
package com.nasageek.utexasutilities.model.canvas;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class Term implements Parcelable, Serializable {

    private static final long serialVersionUID = 1L;
    private String id;
    private String name;
    private static final String INTERNAL_TERM_NAME_REGEX = "(Spring|Fall|Summer) \\d{4}";

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
        if (name.matches(INTERNAL_TERM_NAME_REGEX)) {
            String[] splitname = name.split(" ");
            return splitname[1] + " " + splitname[0];
        }
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
