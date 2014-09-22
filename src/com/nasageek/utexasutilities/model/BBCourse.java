
package com.nasageek.utexasutilities.model;

import java.io.Serializable;
import java.util.Locale;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class BBCourse extends Course implements Parcelable, Serializable {

    private static final long serialVersionUID = 1L;
    private String fullcourseid;
    private String unique;
    private String fullName;

    private boolean courseIdAvailable, fullCourseIdTooShort;

    public static Parcelable.Creator<BBCourse> CREATOR = new Parcelable.Creator<BBCourse>() {

        @Override
        public BBCourse createFromParcel(Parcel source) {
            return new BBCourse(source);
        }

        @Override
        public BBCourse[] newArray(int size) {
            return new BBCourse[size];
        }
    };

    public BBCourse(Parcel in) {
        super.name = in.readString();
        super.id = in.readString();
        fullcourseid = in.readString();
        super.term_name = in.readString();
        unique = in.readString();
        super.course_code = in.readString();
        super.type = in.readString();
        boolean[] temp = new boolean[2];
        in.readBooleanArray(temp);
        courseIdAvailable = temp[0];
        fullCourseIdTooShort = temp[1];
    }

    // TODO: move auto-formatting into a separate method?
    public BBCourse(String name, String id, String fullcourseid) {

        if (!fullcourseid.matches("^\\d{4}_[a-z]+?_\\d{5}_[A-Za-z]+?_\\w+$")) {
            Log.d("BBClass check", "Course ID malformed: " + fullcourseid);
        }
        if (!name.matches("^\\d{2}[A-Z]{1,2} .*?\\(\\d+?\\)$")) {
            Log.d("BBClass check", "Course Name malformed: " + name);
        }

        // names don't always have spaces, be careful!
        if (name.contains(" ")) {
            // TODO: might try this with a split[0] rather than the substring
            // TODO: this might not trim off Summer semester identifier, check
            // Alex's tablet
            // If we've got a date in the front (probably) chop it off
            if (name.substring(0, name.indexOf(" ")).matches("^\\d{2}[A-Z]{1,2}$")) {
                this.name = name.substring(name.indexOf(" ") + 1);
            } else {
                this.name = name;
            }
        } else {
            this.name = name;
        }

        // remove anything in parentheses, it's usually all superfluous
        this.name = this.name.replaceAll("\\(.*?\\)", "");
        // because the stuff in parentheses is generally at the beginning
        // or end, it leaves leading/trailing whitespace when removed
        this.name = this.name.trim();

        // the course ID seems to relatively consistently be the last 2 tokens
        // in the full ID, maybe just pull those out
        super.id = id;
        this.fullName = name;
        this.fullcourseid = fullcourseid;
        // some courseid's are malformed (ex. 00002), can't pull semester out of
        // that unfortunately
        try {
            // pulls the first section and second section of fullcourseid,
            // capitalizes the first letter of the semester
            super.term_name = fullcourseid.split("_")[0] + " "
                    + (fullcourseid.split("_")[1].charAt(0) + "").toUpperCase(Locale.US)
                    + fullcourseid.split("_")[1].substring(1);
        } catch (Exception ex) {
            ex.printStackTrace();
            super.term_name = "Unknown";
        }

        if (fullcourseid.split("_").length >= 3) {
            fullCourseIdTooShort = false;
            this.unique = fullcourseid.split("_")[2];
            // assumes Course ID is directly after unique_ and is at the end of
            // the string
            // will fail if unique start is less than 6 characters from the end
            // of the string.
            try {
                // TODO: check validity of this with regex after merge
                super.course_code = fullcourseid.substring(fullcourseid.indexOf(unique) + 6)
                        .replaceAll("_", " ");
                courseIdAvailable = true;
            } catch (Exception ex) {
                courseIdAvailable = false;
            }
        } else {
            fullCourseIdTooShort = true;
        }

        super.type = "blackboard";
    }

    public boolean isFullCourseIdTooShort() {
        return fullCourseIdTooShort;
    }

    public boolean isCourseIdAvailable() {
        return courseIdAvailable;
    }

    public String getUnique() {
        return unique;
    }

    public String getFullCourseid() {
        return fullcourseid;
    }

    public String getFullName() {
        return fullName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeString(super.name);
        dest.writeString(super.id);
        dest.writeString(fullcourseid);
        dest.writeString(super.term_name);
        dest.writeString(unique);
        dest.writeString(super.course_code);
        dest.writeString(super.type);

        dest.writeBooleanArray(new boolean[] {
                courseIdAvailable, fullCourseIdTooShort
        });

    }
}
