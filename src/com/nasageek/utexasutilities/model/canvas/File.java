package com.nasageek.utexasutilities.model.canvas;

import java.util.ArrayList;

public class File {

	public int size;
	String content_type; //can be null
	public String url;
	String id;
	public String display_name;
	
	public static class List extends ArrayList<File> {}
}
