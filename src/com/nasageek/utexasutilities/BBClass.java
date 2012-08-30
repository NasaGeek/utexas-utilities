package com.nasageek.utexasutilities;

import android.util.Log;

public class BBClass {

	private String name;
	private String bbid;
	private String courseid;
	private String semester;

	public BBClass(String name, String bbid, String courseid)
	{
		//name is now blank, and the Course ID is mysteriously absent :( - all as of 8/29/2012 
		//oh ho ho! It was fixed, how wonderful. Ignore commented stuff below
		//checks to see if it is what is now a legitimate courseid
		//year (2 digits) followed by semester (in caps) followed by name followed by unique (digits in parentheses)
	//	if(courseid.matches("^\\d\\d[A-Z]{1,2} .*?(\\d+?)$"))
	//		Log.d("BBClass check", "Class format is good");

		
		
		if(!courseid.matches("^\\d{4}_[a-z]+?_\\d{5}_[A-Za-z]+?_\\w+$"))
			Log.d("BBClass check", "Class Course ID malformed");
		if(!name.matches("^\\d{2}[A-Z]{1,2} .*?\\(\\d+?\\)$"))
			Log.d("BBClass check", "Class Name malformed");
		//this should never fail.  If no space, indexOf returns -1 and you get the whole string
		this.name = name.substring(name.indexOf(" ")+1);
		this.bbid = bbid;
		this.courseid = courseid;
		//some courseid's are malformed (ex. 00002), can't pull semester out of that unfortunately
		try
		{
			//pulls the first section and second section of courseid, capitalizes the first letter of the semester
			this.semester = courseid.split("_")[0]+" "+(courseid.split("_")[1].charAt(0)+"").toUpperCase()+courseid.split("_")[1].substring(1);	
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			this.semester = "Unknown";
		}
		
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getBbid() {
		return bbid;
	}

	public void setBbid(String bbid) {
		this.bbid = bbid;
	}

	public String getCourseid() {
		return courseid;
	}

	public void setCourseid(String courseid) {
		this.courseid = courseid;
	}

	public String getSemester() {
		return semester;
	}

	public void setSemester(String semester) {
		this.semester = semester;
	}
}
