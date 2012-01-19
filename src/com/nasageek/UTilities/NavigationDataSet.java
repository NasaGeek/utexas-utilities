package com.nasageek.UTilities;

import java.util.ArrayList;
import java.util.Iterator;


public class NavigationDataSet implements Iterable{ 

private ArrayList<Placemark> placemarks = new ArrayList<Placemark>();
private Placemark currentPlacemark;
private Placemark routePlacemark;

public String toString() {
    String s= "";
    for (Iterator<Placemark> iter=placemarks.iterator();iter.hasNext();) {
        Placemark p = (Placemark)iter.next();
        s += p.getTitle() + "\t" + p.getDescription() + "\n";
    }
    return s;
}

public void addCurrentPlacemark() {
    placemarks.add(currentPlacemark);
}

public ArrayList<Placemark> getPlacemarks() {
    return placemarks;
}

public void setPlacemarks(ArrayList<Placemark> placemarks) {
    this.placemarks = placemarks;
}

public Placemark getCurrentPlacemark() {
    return currentPlacemark;
}

public void setCurrentPlacemark(Placemark currentPlacemark) {
    this.currentPlacemark = currentPlacemark;
}

public Placemark getRoutePlacemark() {
    return routePlacemark;
}

public void setRoutePlacemark(Placemark routePlacemark) {
    this.routePlacemark = routePlacemark;
}

public Iterator iterator() {
	// TODO Auto-generated method stub
	return placemarks.iterator();
}
}