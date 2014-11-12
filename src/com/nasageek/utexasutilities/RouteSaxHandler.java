
package com.nasageek.utexasutilities;

import com.nasageek.utexasutilities.model.RoutePlacemark;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class RouteSaxHandler extends PlacemarkSaxHandler<RoutePlacemark> {

    private boolean in_descriptiontag = false;

    /**
     * Is called on opening tags like: <tag> Can provide attribute(s), when xml
     * was like: <tag attribute="attributeValue">
     */
    @Override
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts)
            throws SAXException {
        switch (localName) {
            case "Placemark":
                currentPlacemark = new RoutePlacemark();
                if (atts.getLength() > 0) {
                    currentPlacemark.setDescription(atts.getValue(0));
                }
                break;
            case "name":
                this.in_nametag = true;
                break;
            case "description":
                this.in_descriptiontag = true;
                break;
            case "coordinates":
                buffer = new StringBuffer();
                this.in_coordinatestag = true;
                break;
        }
    }

    /**
     * Is called on closing tags like: </tag>
     */
    @Override
    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
        switch (localName) {
            case "Placemark":
                placemarks.push(currentPlacemark);
                break;
            case "name":
                this.in_nametag = false;
                break;
            case "description":
                this.in_descriptiontag = false;
                break;
            case "coordinates":
                currentPlacemark.setCoordinates(buffer.toString().trim());
                this.in_coordinatestag = false;
                break;
        }
    }

    /**
     * Is called on the following structure: <tag>characters</tag>
     */
    @Override
    public void characters(char ch[], int start, int length) {
        if (this.in_nametag) {
            if (currentPlacemark == null) {
                currentPlacemark = new RoutePlacemark();
            }
            currentPlacemark.setTitle(new String(ch, start, length));
        } else if (this.in_descriptiontag) {
            if (currentPlacemark == null) {
                currentPlacemark = new RoutePlacemark();
            }
            currentPlacemark.setDescription(new String(ch, start, length));
        } else if (this.in_coordinatestag) {
            if (currentPlacemark == null) {
                currentPlacemark = new RoutePlacemark();
            }
            buffer.append(ch, start, length);
        }
    }
}
