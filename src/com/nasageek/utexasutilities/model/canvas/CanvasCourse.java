
package com.nasageek.utexasutilities.model.canvas;

import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;

import com.nasageek.utexasutilities.model.Course;

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
    private Section[] sections;

    public CanvasCourse(Parcel in) {
        super.name = in.readString();
        super.id = in.readString();
        super.course_code = in.readString();
        super.start_at = in.readString();
        super.type = in.readString();
        super.term_name = in.readString();
        term = in.readParcelable(Term.class.getClassLoader());
        sections = in.createTypedArray(Section.CREATOR);
    }

    public CanvasCourse() {
        type = "canvas";
    }

    @Override
    public String getCourseCode() {
        return super.getCourseCode();
    }

    public String getUnique() {
        if (sections == null || sections.length < 1) {
            return null;
        }

        return sections[0].getName();
    }

    @Override
    public String getTermName() {
        return term.getName();
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
        dest.writeString(super.start_at);
        dest.writeString(super.type);
        dest.writeString(super.term_name);
        dest.writeParcelable(term, 0);
        dest.writeTypedArray(sections, 0);
    }
}
