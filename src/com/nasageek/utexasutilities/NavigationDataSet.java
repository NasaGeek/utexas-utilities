
package com.nasageek.utexasutilities;

import java.util.ArrayList;
import java.util.Iterator;

import com.nasageek.utexasutilities.model.Placemark;

public class NavigationDataSet<E extends Placemark> implements Iterable<E> {

    private ArrayList<E> placemarks = new ArrayList<E>();
    private E currentPlacemark;
    private E routePlacemark;

    @Override
    public String toString() {
        String s = "";
        for (Iterator<E> iter = placemarks.iterator(); iter.hasNext();) {
            E p = iter.next();
            s += p.getTitle() + "\t" + p.getDescription() + "\n";
        }
        return s;
    }

    public void addCurrentPlacemark() {
        placemarks.add(currentPlacemark);
    }

    public ArrayList<E> getPlacemarks() {
        return placemarks;
    }

    public void setPlacemarks(ArrayList<E> placemarks) {
        this.placemarks = placemarks;
    }

    public E getCurrentPlacemark() {
        return currentPlacemark;
    }

    public void setCurrentPlacemark(E currentPlacemark) {
        this.currentPlacemark = currentPlacemark;
    }

    public E getRoutePlacemark() {
        return routePlacemark;
    }

    public void setRoutePlacemark(E routePlacemark) {
        this.routePlacemark = routePlacemark;
    }

    @Override
    public Iterator<E> iterator() {
        return placemarks.iterator();
    }
}
