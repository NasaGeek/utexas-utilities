
package com.nasageek.utexasutilities.model;

import java.io.Serializable;

import android.os.Parcel;
import android.os.Parcelable;

public class Classtime implements Parcelable, Serializable {
    private static final long serialVersionUID = 1L;
    private char day;
    private String starttime, endtime, courseId, name;
    private Building buil;
    private String unique;
    private String color;

    public static Parcelable.Creator<Classtime> CREATOR = new Parcelable.Creator<Classtime>() {

        @Override
        public Classtime createFromParcel(Parcel source) {
            return new Classtime(source);
        }

        @Override
        public Classtime[] newArray(int size) {
            return new Classtime[size];
        }

    };

    private Classtime(Parcel in) {
        day = (char) in.readInt();
        starttime = in.readString();
        endtime = in.readString();
        buil = in.readParcelable(Building.class.getClassLoader());
        color = in.readString();
        courseId = in.readString();
        name = in.readString();
        unique = in.readString();
    }

    public Classtime(String day, String time, Building building, String color, String courseId,
            String name, String unique) {
        this.day = day.charAt(0);
        starttime = time.split("-")[0];
        endtime = time.split("-")[1];

        if ((endtime.charAt(endtime.length() - 1) == 'P')
                && (Integer.parseInt(endtime.split(":")[0]) > Integer
                        .parseInt(starttime.split(":")[0]) || starttime.split(":")[0].equals("12"))
                && !(endtime.split(":")[0].equals("12"))) {
            starttime = starttime + "P";
        }
        this.buil = building;
        this.color = color;
        this.courseId = courseId;
        this.name = name;
        this.unique = unique;

    }

    // TODO: old leftovers, do I need this?
    /*
     * public Classtime(char d,String s, String e, String bid) { starttime = s;
     * endtime = e; day = d; buil=new Building(bid,"0"); }
     */
    public char getDay() {
        return day;
    }

    public String getStartTime() {
        return starttime;
    }

    public String getEndTime() {
        return endtime;
    }

    public Building getBuilding() {
        return buil;
    }

    public String getColor() {
        return color;
    }

    public String getCourseId() {
        return courseId;
    }

    public String getName() {
        return name;
    }

    public String getUnique() {
        return unique;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(day); // THIS IS A CHAR
        dest.writeString(starttime);
        dest.writeString(endtime);
        dest.writeParcelable(buil, 0);
        dest.writeString(color);
        dest.writeString(courseId);
        dest.writeString(name);
        dest.writeString(unique);
    }
}
