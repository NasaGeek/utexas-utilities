
package com.nasageek.utexasutilities.model.canvas;

import android.os.Parcel;
import android.os.Parcelable;

import com.nasageek.utexasutilities.model.Course;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class CanvasCourse extends Course implements Parcelable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public static Parcelable.Creator<CanvasCourse> CREATOR = new Parcelable.Creator<CanvasCourse>() {

        @Override
        public CanvasCourse createFromParcel(Parcel source) {
            return new CanvasCourse(source);
        }

        @Override
        public CanvasCourse[] newArray(int size) {
            return new CanvasCourse[size];
        }
    };

    private Term term;

    public CanvasCourse(Parcel in) {
        super.name = in.readString();
        super.id = in.readString();
        super.course_code = in.readString();
        super.start_at = new Date(in.readLong());
        super.type = in.readString();
        super.term_name = in.readString();
    }

    public CanvasCourse() {
        type = "canvas";
    }

    @Override
    public String getTermName() {
        if (term_name != null) {
            return term_name;
        }
        if (term.getName().matches(TERM_NAME_REGEX)) {
            term_name = term.getName();
        } else if (start_at != null) {
            // course doesn't have a well-formed term name, let's take our best guess
            Calendar start = new GregorianCalendar();
            start.setTime(start_at);
            String termNameGuess = start.get(Calendar.YEAR) + " ";
            int month = start.get(Calendar.MONTH);
            if (month >= Calendar.AUGUST && month <= Calendar.NOVEMBER) {
                termNameGuess += "Fall";
            } else if (month >= Calendar.DECEMBER ||
                    (month >= Calendar.JANUARY && month <= Calendar.APRIL)) {
                termNameGuess += "Spring";
            } else {
                termNameGuess += "Summer";
            }
            term_name = termNameGuess;
        } else {
            term_name = term.getName();
        }
        return term_name;
    }

    public static class List extends ArrayList<CanvasCourse> {

        private static final long serialVersionUID = 1L;
    }

    @Override
    public String toString() {
        return id + " " + name + " " + course_code;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(super.name);
        dest.writeString(super.id);
        dest.writeString(super.course_code);
        dest.writeLong(super.start_at.getTime());
        dest.writeString(super.type);
        dest.writeString(super.term_name);
        dest.writeParcelable(term, 0);
    }
}
