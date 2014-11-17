package com.nasageek.utexasutilities;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.Map;

/**
 * Class to manage currently displayed markers on a GoogleMap
 */
public class MarkerManager<E> {

    GoogleMap mMap;

    // objects that provide the data for the marker
    Map<E, Marker> markerBackingMap;

    public MarkerManager(GoogleMap mMap) {
        this.markerBackingMap = new HashMap<>();
        this.mMap = mMap;
    }

    public Marker placeMarker(E backingData, MarkerOptions markerOpt, boolean duplicatesAllowed) {
        if (!duplicatesAllowed && markerBackingMap.containsKey(backingData)) {
            return markerBackingMap.get(backingData);
        }
        Marker marker = mMap.addMarker(markerOpt);
        markerBackingMap.put(backingData, marker);
        return marker;
    }

    public void clearMarkers() {
        for (Marker m : markerBackingMap.values()) {
            m.remove();
        }
        markerBackingMap.clear();
    }

    public boolean isShowing(E markerBacking) {
        return markerBackingMap.containsKey(markerBacking);
    }
}
