package com.nasageek.utexasutilities.model.canvas;

import java.util.ArrayList;

public class File {

	int size;
	String content_type;
	String url;
	String id;
	String display_name;	
	
	public static class List extends ArrayList<File> {}
}
