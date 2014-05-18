
package com.nasageek.utexasutilities;

import com.nasageek.utexasutilities.model.RoutePlacemark;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class NavigationSaxHandler extends DefaultHandler {

    // ===========================================================
    // Fields
    // ===========================================================

    private boolean in_nametag = false;
    private boolean in_descriptiontag = false;
    private boolean in_coordinatestag = false;

    private StringBuffer buffer;

    private NavigationDataSet<RoutePlacemark> navigationDataSet;

    public NavigationSaxHandler() {
        super();
    }

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    public NavigationDataSet<RoutePlacemark> getParsedData() {
        return this.navigationDataSet;
    }

    // ===========================================================
    // Methods
    // ===========================================================
    @Override
    public void startDocument() throws SAXException {
        this.navigationDataSet = new NavigationDataSet<RoutePlacemark>();
    }

    @Override
    public void endDocument() throws SAXException {
        // Nothing to do
    }

    /**
     * Is called on opening tags like: <tag> Can provide attribute(s), when xml
     * was like: <tag attribute="attributeValue">
     */
    @Override
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts)
            throws SAXException {
        if (localName.equals("Placemark")) {
            navigationDataSet.setCurrentPlacemark(new RoutePlacemark());
            if (atts.getLength() > 0) {
                navigationDataSet.getCurrentPlacemark().setDescription(atts.getValue(0));
            }
        } else if (localName.equals("name")) {
            this.in_nametag = true;
        } else if (localName.equals("description")) {
            this.in_descriptiontag = true;
        } else if (localName.equals("coordinates")) {
            buffer = new StringBuffer();
            this.in_coordinatestag = true;
        }
    }

    /**
     * Is called on closing tags like: </tag>
     */
    @Override
    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
        if (localName.equals("Placemark")) {
            navigationDataSet.addCurrentPlacemark();
        } else if (localName.equals("name")) {
            this.in_nametag = false;
        } else if (localName.equals("description")) {
            this.in_descriptiontag = false;
        } else if (localName.equals("coordinates")) {
            navigationDataSet.getCurrentPlacemark().setCoordinates(buffer.toString().trim());
            this.in_coordinatestag = false;
        }
    }

    /**
     * Is called on the following structure: <tag>characters</tag>
     */
    @Override
    public void characters(char ch[], int start, int length) {
        if (this.in_nametag) {
            if (navigationDataSet.getCurrentPlacemark() == null) {
                navigationDataSet.setCurrentPlacemark(new RoutePlacemark());
            }
            navigationDataSet.getCurrentPlacemark().setTitle(new String(ch, start, length));
        } else if (this.in_descriptiontag) {
            if (navigationDataSet.getCurrentPlacemark() == null) {
                navigationDataSet.setCurrentPlacemark(new RoutePlacemark());
            }
            navigationDataSet.getCurrentPlacemark().setDescription(new String(ch, start, length));
        } else if (this.in_coordinatestag) {
            if (navigationDataSet.getCurrentPlacemark() == null) {
                navigationDataSet.setCurrentPlacemark(new RoutePlacemark());
            }
            buffer.append(ch, start, length);
        }
    }
}
