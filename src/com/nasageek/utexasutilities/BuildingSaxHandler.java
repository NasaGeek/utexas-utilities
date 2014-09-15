
package com.nasageek.utexasutilities;

import com.nasageek.utexasutilities.model.BuildingPlacemark;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class BuildingSaxHandler extends DefaultHandler {

    private boolean in_coordinatestag = false;
    private boolean in_nametag = false;

    private StringBuffer buffer;

    private NavigationDataSet<BuildingPlacemark> navigationDataSet;

    public BuildingSaxHandler() {
        super();
    }

    public NavigationDataSet<BuildingPlacemark> getParsedData() {
        return this.navigationDataSet;
    }

    @Override
    public void startDocument() throws SAXException {
        this.navigationDataSet = new NavigationDataSet<BuildingPlacemark>();
    }

    @Override
    public void endDocument() throws SAXException {
        // Nothing to do
    }

    /**
     * Called on opening tags like: <tag> Can provide attribute(s), when xml was
     * like: <tag attribute="attributeValue">
     */
    @Override
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts)
            throws SAXException {
        if (localName.equals("Placemark")) {
            navigationDataSet.setCurrentPlacemark(new BuildingPlacemark());
            if (atts.getLength() > 0) {
                navigationDataSet.getCurrentPlacemark().setTitle(atts.getValue(0));
            }
        } else if (localName.equals("name")) {
            this.in_nametag = true;
        } else if (localName.equals("coordinates")) {
            buffer = new StringBuffer();
            this.in_coordinatestag = true;
        }
    }

    /**
     * Called on closing tags like: </tag>
     */
    @Override
    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
        if (localName.equals("Placemark")) {
            navigationDataSet.addCurrentPlacemark();
        } else if (localName.equals("name")) {
            this.in_nametag = false;
        } else if (localName.equals("coordinates")) {
            navigationDataSet.getCurrentPlacemark().setCoordinates(buffer.toString().trim());
            this.in_coordinatestag = false;
        }
    }

    /**
     * Called on the following structure: <tag>characters</tag>
     */
    @Override
    public void characters(char ch[], int start, int length) {
        if (this.in_nametag) {
            if (navigationDataSet.getCurrentPlacemark() == null) {
                navigationDataSet.setCurrentPlacemark(new BuildingPlacemark());
            }
            String description = (navigationDataSet.getCurrentPlacemark().getDescription() == null) ? ""
                    : navigationDataSet.getCurrentPlacemark().getDescription();
            navigationDataSet.getCurrentPlacemark().setDescription(
                    description.concat(new String(ch, start, length)));
        } else if (this.in_coordinatestag) {
            if (navigationDataSet.getCurrentPlacemark() == null) {
                navigationDataSet.setCurrentPlacemark(new BuildingPlacemark());
            }
            buffer.append(ch, start, length);
        }
    }
}
