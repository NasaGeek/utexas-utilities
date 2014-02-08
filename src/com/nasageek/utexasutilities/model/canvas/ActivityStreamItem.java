
package com.nasageek.utexasutilities.model.canvas;

import java.io.Serializable;
import java.util.ArrayList;

public class ActivityStreamItem implements Serializable {

    private static final long serialVersionUID = 1L;
    private String id;
    public String title;
    public String message; // can be null
    public String type;
    private String created_at;
    private String course_id;
    private String html_url;
    private boolean read_state;

    // Message
    private String notification_category;

    // Submission

    public static class List extends ArrayList<ActivityStreamItem> {

        private static final long serialVersionUID = 1L;
    }

    @Override
    public String toString() {
        return title + message + created_at + course_id;
    }

}
