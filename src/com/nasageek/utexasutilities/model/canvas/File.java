package com.nasageek.utexasutilities.model.canvas;

import java.io.Serializable;
import java.util.ArrayList;

public class File implements Serializable {

    private static final long serialVersionUID = 1L;
    public int size;
	String content_type; //can be null
	public String url;
	String id;
	public String display_name;
	
	public static class List extends ArrayList<File> {}
}
