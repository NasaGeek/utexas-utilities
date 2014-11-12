package com.nasageek.utexasutilities;

import com.nasageek.utexasutilities.model.Placemark;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayDeque;
import java.util.Deque;

public class PlacemarkSaxHandler<E extends Placemark> extends DefaultHandler {

    protected boolean in_coordinatestag = false;
    protected boolean in_nametag = false;

    protected StringBuffer buffer;
    protected Deque<E> placemarks;
    protected E currentPlacemark;

    public PlacemarkSaxHandler() {
        super();
    }

    public Deque<E> getParsedData() {
        return this.placemarks;
    }

    @Override
    public void startDocument() throws SAXException {
        this.placemarks = new ArrayDeque<>();
    }

    @Override
    public void endDocument() throws SAXException {
        // Nothing to do
    }
}
