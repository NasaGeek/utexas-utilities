
package com.nasageek.utexasutilities.model;

public class BuildingPlacemark extends Placemark {

    public double getLatitude() {
        return Double.valueOf(coordinates.split(",")[1]);
    }

    public double getLongitude() {
        return Double.valueOf(coordinates.split(",")[0]);
    }
}
