
package com.nasageek.utexasutilities.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;

public class UTClass implements Parcelable, Serializable {

    private static final long serialVersionUID = 1L;

    private String courseId, unique, name, semId, color;

    private ArrayList<Classtime> classtimes;

    public static Parcelable.Creator<UTClass> CREATOR = new Parcelable.Creator<UTClass>() {

        @Override
        public UTClass createFromParcel(Parcel source) {
            return new UTClass(source);
        }

        @Override
        public UTClass[] newArray(int size) {
            return new UTClass[size];
        }

    };

    private UTClass(Parcel in) {
        unique = in.readString();
        courseId = in.readString();
        name = in.readString();

        classtimes = new ArrayList<>();
        in.readTypedList(classtimes, Classtime.CREATOR);
        semId = in.readString();
        color = in.readString();
    }

    public UTClass(String unique, String courseId, String name, String[] buildingIds,
            String[] buildingRooms, String[] days, String[] times, String semId, String color) {
        this.unique = unique;
        this.courseId = courseId;
        this.name = name.replace("&amp;", "&");
        this.semId = semId;
        this.color = color;

        if (buildingIds.length != buildingRooms.length) {
            Log.e("UTClass creation", "building/room size inconsistency: b" + buildingIds.length
                    + " r" + buildingRooms.length);
        }

        ArrayList<Building> buildings = new ArrayList<>();
        for (int i = 0; i < buildingIds.length; i++) {
            // If there are fewer rooms than buildings, just don't give rooms to later buildings.
            // I suspect a blank room indicates an error/oversight on the class listing, so
            // hopefully it's not an issue that will occur frequently
            if (i < buildingRooms.length) {
                buildings.add(new Building(buildingIds[i], buildingRooms[i]));
            } else {
                buildings.add(new Building(buildingIds[i], ""));
            }
        }

        /* Class Listing page leaves out building info if there are multiple sections in the same
           location at different times. See Chris Roberts Fall 2014 Class Listing. Here we check
           to see if location info was absent from a row.
        */
        if (buildings.size() < days.length) {
            buildings.add(new Building(buildings.get(0)));
        }

        classtimes = new ArrayList<>();
        if (!(days.length == times.length && days.length == buildings.size() && buildings.size() == times.length)) {
            Log.e("UTClass creation", "building/day/time size inconsistency: b" + buildings.size()
                    + " d" + days.length + " t" + times.length);
        }
        for (int i = 0; i < days.length && i < times.length && i < buildings.size(); i++) {
            String[] dayArray = days[i].split("");

            for (int k = 1; k < dayArray.length; k++) {
                classtimes.add(new Classtime(dayArray[k], times[i], buildings.get(i), this.color,
                        this.courseId, this.name, this.unique));
            }
        }
    }

    @Override
    public String toString() {
        String out = courseId + " in ";
        for (int i = 0; i < classtimes.size(); i++) {
            Classtime classtime = classtimes.get(i);

            out += classtime.getBuilding().getId() + " in room "
                    + classtime.getBuilding().getRoom() + " at "
                    + classtime.getStartTime() + "-" + classtime.getEndTime()
                    + " on " + classtime.getDay();
            if (i != classtimes.size() - 1) {
                out += " and in ";
            }
        }
        return out;
    }

    public ArrayList<Classtime> getClassTimes() {
        return classtimes;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return courseId;
    }

    public String getUnique() {
        return unique;
    }

    public String getSemId() {
        return semId;
    }

    public String getColor() {
        return color;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(unique);
        out.writeString(courseId);
        out.writeString(name);
        out.writeTypedList(classtimes);
        out.writeString(semId);
        out.writeString(color);
    }
}
