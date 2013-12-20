package com.nasageek.utexasutilities.model.canvas;

import java.util.ArrayList;

public class Assignment {

	
	private String id;
	public String name;
	private String description;
	private String due_at; //possibly null
	private String course_id;
	private String html_url;
	private Boolean muted;
	public int points_possible;
	private String grading_type;
	
	public Submission submission;
	
	
	public static class List extends ArrayList<Assignment> {}
	
	public class Submission {
		public String score;
		
		public SubmissionComments submission_comments;
	}
	
	public class SubmissionComments {
		public String comment;
	}
}
