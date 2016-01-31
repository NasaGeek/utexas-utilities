
package com.nasageek.utexasutilities.model;

public class Placemark {

    String title;
    String description;
    Double latitude;
    Double longitude;

    public Placemark() {}

    public Placemark(String title, String description, Double latitude, Double longitude) {
        this.title = title;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Placemark(String title, String description) {
        this(title, description, 0.0, 0.0);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setLatitude(Double latitude) { this.latitude  = latitude; }

    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    @Override
    public String toString() {
        return title + " : " + description + " (" + longitude + ", " + latitude + ")";
    }
}
