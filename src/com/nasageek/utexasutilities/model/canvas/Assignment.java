
package com.nasageek.utexasutilities.model.canvas;

import java.io.Serializable;
import java.util.ArrayList;

public class Assignment implements Serializable {

    private static final long serialVersionUID = 1L;
    private String id;
    public String name;
    private String description;
    private String due_at; // possibly null
    private String course_id;
    private String html_url;
    public Boolean muted;
    public int points_possible;
    private String grading_type;

    public Submission submission; // doesn't look this can be null, but all of
                                  // its members can be

    public static class List extends ArrayList<Assignment> {

        private static final long serialVersionUID = 1L;
    }

    public class Submission {
        public String score;

        public SubmissionComments submission_comments;
    }

    public class SubmissionComments {
        public String comment;
    }
}
