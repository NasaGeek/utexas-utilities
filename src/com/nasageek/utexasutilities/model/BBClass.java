
package com.nasageek.utexasutilities.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.io.Serializable;
import java.util.Locale;

public class BBClass implements Parcelable, Serializable {

    private static final long serialVersionUID = 1L;
    private String name;
    private String bbid;
    private String fullcourseid;
    private String semester;
    private String unique;
    private String courseid;
    private String fullName;

    private boolean courseIdAvailable, fullCourseIdTooShort;

    public static Parcelable.Creator<BBClass> CREATOR = new Parcelable.Creator<BBClass>() {

        @Override
        public BBClass createFromParcel(Parcel source) {
            return new BBClass(source);
        }

        @Override
        public BBClass[] newArray(int size) {
            return new BBClass[size];
        }
    };

    public BBClass(Parcel in) {
        name = in.readString();
        bbid = in.readString();
        fullcourseid = in.readString();
        semester = in.readString();
        unique = in.readString();
        courseid = in.readString();
        boolean[] temp = new boolean[2];
        in.readBooleanArray(temp);
        courseIdAvailable = temp[0];
        fullCourseIdTooShort = temp[1];
    }

    // TODO: move auto-formatting into a separate method?
    public BBClass(String name, String bbid, String fullcourseid) {
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
        this.bbid = bbid;
        this.fullName = name;
        this.fullcourseid = fullcourseid;
        // some courseid's are malformed (ex. 00002), can't pull semester out of
        // that unfortunately
        try {
            // pulls the first section and second section of courseid,
            // capitalizes the first letter of the semester
            this.semester = fullcourseid.split("_")[0] + " "
                    + (fullcourseid.split("_")[1].charAt(0) + "").toUpperCase(Locale.US)
                    + fullcourseid.split("_")[1].substring(1);
        } catch (Exception ex) {
            ex.printStackTrace();
            this.semester = "Unknown";
        }

        if (fullcourseid.split("_").length >= 3) {
            fullCourseIdTooShort = false;
            this.unique = fullcourseid.split("_")[2];
            // assumes Course ID is directly after unique_ and is at the end of
            // the string
            // will fail if unique start is less than 6 characters from the end
            // of the string.
            try {
                courseid = fullcourseid.substring(fullcourseid.indexOf(unique) + 6)
                        .replaceAll("_", " ");
                courseIdAvailable = true;
            } catch (Exception ex) {
                courseIdAvailable = false;
            }
        } else {
            fullCourseIdTooShort = true;
        }
    }

    public boolean isFullCourseIdTooShort() {
        return fullCourseIdTooShort;
    }

    public boolean isCourseIdAvailable() {
        return courseIdAvailable;
    }

    public String getCourseId() {
        return courseid;
    }

    public String getUnique() {
        return unique;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBbid() {
        return bbid;
    }

    public void setBbid(String bbid) {
        this.bbid = bbid;
    }

    public String getFullCourseid() {
        return fullcourseid;
    }

    public void setFullCourseid(String fullcourseid) {
        this.fullcourseid = fullcourseid;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
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

        dest.writeString(name);
        dest.writeString(bbid);
        dest.writeString(fullcourseid);
        dest.writeString(semester);
        dest.writeString(unique);
        dest.writeString(courseid);
        dest.writeBooleanArray(new boolean[] {
                courseIdAvailable, fullCourseIdTooShort
        });

    }
}
