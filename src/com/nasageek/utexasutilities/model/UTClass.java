
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

        classtimes = new ArrayList<Classtime>();
        in.readTypedList(classtimes, Classtime.CREATOR);
        semId = in.readString();
        color = in.readString();
    }

    public UTClass(String unique, String courseId, String name, String[] buildingIds,
            String[] buildingRooms, String[] days, String[] times, String semId, String color) {
        this.unique = unique;
        this.courseId = courseId;
        this.name = name;

        ArrayList<Building> buildings = new ArrayList<Building>();
        for (int i = 0; i < buildingIds.length; i++) {
            buildings.add(new Building(buildingIds[i], buildingRooms[i]));
        }

        classtimes = new ArrayList<Classtime>();
        if (!(days.length == times.length && days.length == buildings.size() && buildings.size() == times.length)) {
            Log.d("UTClass creation", "building/day/time size inconsistency: b" + buildings.size()
                    + " d" + days.length + " t" + times.length);
        }
        for (int i = 0; i < days.length && i < times.length && i < buildings.size(); i++) {
            String[] dayArray = days[i].split("");

            for (int k = 1; k < dayArray.length; k++) {
                classtimes.add(new Classtime(dayArray[k], times[i], buildings.get(i), color,
                        courseId, name, unique));
                // Log.d("DAYTIME", days[k]+" "+t[i]);
            }

        }
        // Log.d("BLENGTH", b.length+ " "+br.length);

        this.semId = semId;
        this.color = color;
    }

    @Override
    public String toString() {
        String out = courseId + " in ";
        for (int i = 0; i < classtimes.size(); i++) {
            out += classtimes.get(i).getBuilding().getId() + " in room "
                    + classtimes.get(i).getBuilding().getRoom() + " at "
                    + classtimes.get(i).getStartTime() + "-" + classtimes.get(i).getEndTime()
                    + " on " + classtimes.get(i).getDay();
            if (i == classtimes.size() - 1) {
                continue;
            } else {
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
