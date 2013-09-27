package com.nasageek.utexasutilities.model.canvas;

public class Term {

	private int id;
	private String name;
	private String start_at;
	private String end_at;
	
	public String getName() {
		String[] splitname = name.split(" ");
		return splitname[1] + " " + splitname[0];
	}
}
