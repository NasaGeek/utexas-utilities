
package com.nasageek.utexasutilities.model.canvas;

import java.io.Serializable;

public class ModuleItem implements Serializable {

    private static final long serialVersionUID = 1L;

    public String id;
    public int indent;
    public int position;
    public String title;
    public String type;
    public String module_id;
    public String html_url;
    public String content_id;
    public String url;

    public ContentDetails content_details;

    class ContentDetails implements Serializable {

        private static final long serialVersionUID = 1L;

        public double points_possible; // WOW WORTHLESS, GIMME POINTS
    }
}
