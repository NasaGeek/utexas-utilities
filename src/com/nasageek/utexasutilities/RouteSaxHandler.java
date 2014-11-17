
package com.nasageek.utexasutilities;

import com.nasageek.utexasutilities.model.Placemark;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class RouteSaxHandler extends PlacemarkSaxHandler<Placemark> {

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
                currentPlacemark = new Placemark();
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
                // don't push currentPlacemark here, it's already been pushed in coordinates tag
                break;
            case "name":
                this.in_nametag = false;
                break;
            case "description":
                this.in_descriptiontag = false;
                break;
            // coordinates tag indicates the end of the Placemark
            case "coordinates":
                String[] lngLats = buffer.toString().trim().replaceAll(" ", "").split("\n");
                Placemark basePlacemark = currentPlacemark;

                for (String lngLatStr : lngLats) {
                    if ("".equals(lngLatStr)) {
                        continue;
                    }
                    String[] lngLat = lngLatStr.split(",");
                    try {
                        currentPlacemark.setLongitude(Double.parseDouble(lngLat[0]));
                        currentPlacemark.setLatitude(Double.parseDouble(lngLat[1]));
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        // ensure both lng and lat are set back to default
                        currentPlacemark.setLongitude(0.0);
                        currentPlacemark.setLatitude(0.0);
                    }
                    placemarks.push(currentPlacemark);
                    currentPlacemark =
                            new Placemark(basePlacemark.getTitle(), basePlacemark.getDescription());

                }
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
                currentPlacemark = new Placemark();
            }
            currentPlacemark.setTitle(new String(ch, start, length));
        } else if (this.in_descriptiontag) {
            if (currentPlacemark == null) {
                currentPlacemark = new Placemark();
            }
            currentPlacemark.setDescription(new String(ch, start, length));
        } else if (this.in_coordinatestag) {
            if (currentPlacemark == null) {
                currentPlacemark = new Placemark();
            }
            buffer.append(ch, start, length);
        }
    }
}
