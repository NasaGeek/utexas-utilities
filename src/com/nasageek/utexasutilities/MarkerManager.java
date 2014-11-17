package com.nasageek.utexasutilities;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashSet;
import java.util.Set;

/**
 * Class to manage currently displayed markers on a GoogleMap
 */
public class MarkerManager<E> {

    GoogleMap mMap;

    // objects that provide the data for the marker
    Set<E> markerBackingSet;

    // markers that are being displayed
   Set<Marker> markerSet;

    public MarkerManager(GoogleMap mMap) {
        markerBackingSet = new HashSet<>();
        markerSet = new HashSet<>();
        this.mMap = mMap;
    }

    public Marker placeMarker(E backingData, MarkerOptions markerOpt) {
        Marker marker = mMap.addMarker(markerOpt);
        markerSet.add(marker);
        markerBackingSet.add(backingData);
        return marker;
    }

    public void clearMarkers() {
        for (Marker m : markerSet) {
            m.remove();
        }
        markerSet.clear();
        markerBackingSet.clear();
    }

    public boolean isShowing(E markerBacking) {
        return markerBackingSet.contains(markerBacking);
    }
}
