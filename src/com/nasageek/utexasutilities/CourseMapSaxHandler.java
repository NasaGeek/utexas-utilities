
package com.nasageek.utexasutilities;

import com.nasageek.utexasutilities.fragments.BlackboardFragment;
import com.nasageek.utexasutilities.model.CourseMapItem;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.Stack;

public class CourseMapSaxHandler extends DefaultHandler {

    // ===========================================================
    // Fields
    // ===========================================================

    private boolean in_maptag = false;
    private boolean in_childrentag = false;

    private int folderDepth;
    private Stack<ArrayList> recLists;
    private ArrayList top;
    private ArrayList content;

    public CourseMapSaxHandler() {
        super();

    }

    public ArrayList getParsedData() {
        return this.top;
    }

    @Override
    public void startDocument() throws SAXException {
        this.top = new ArrayList();
        this.content = top;
        this.recLists = new Stack<ArrayList>();
        this.folderDepth = 0;
    }

    @Override
    public void endDocument() throws SAXException {
        // Nothing to do
    }

    /**
     * Gets be called on opening tags like: <tag> Can provide attribute(s), when
     * xml was like: <tag attribute="attributeValue">
     */
    @Override
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts)
            throws SAXException {
        if (localName.equals("map-item")) {
            this.in_maptag = true;
            // maybe I should just check for a non-null viewurl, I suppose this
            // works for the time being, though
            // TODO: actually include SUBHEADERs as non-clickable list items
            // instead of ignoring them
            if (atts.getValue("linktype") != null && !atts.getValue("linktype").equals("DIVIDER")
                    && !atts.getValue("linktype").equals("SUBHEADER")) {
                String host = "";
                Boolean blackboardItem = false;
                if (!(atts.getValue("viewurl").contains("http://") || atts.getValue("viewurl")
                        .contains("https://"))) {
                    host = BlackboardFragment.BLACKBOARD_DOMAIN;
                }
                content.add(new MyPair(new CourseMapItem(atts.getValue("name"), host
                        + atts.getValue("viewurl").replace("&amp;", "&"), atts
                        .getValue("contentid"), atts.getValue("linktype")), new ArrayList()));
            }
            folderDepth++;

        } else if (localName.equals("children")) {
            this.in_childrentag = true;
            recLists.push(content);
            content = (ArrayList) ((MyPair) (content.get(content.size() - 1))).second;
        }
    }

    /**
     * Gets be called on closing tags like: </tag>
     */
    @Override
    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
        if (localName.equals("map-item")) {
            folderDepth--;
            if (folderDepth == 0) {
                this.in_maptag = false;
            }
        } else if (localName.equals("children")) {
            this.in_childrentag = false;
            content = recLists.pop();
        }
    }

    /**
     * Gets be called on the following structure: <tag>characters</tag>
     */
    @Override
    public void characters(char ch[], int start, int length) {

    }
}
