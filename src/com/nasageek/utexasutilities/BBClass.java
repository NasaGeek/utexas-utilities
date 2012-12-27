package com.nasageek.utexasutilities;

import android.util.Log;

public class BBClass {

	private String name;
	private String bbid;
	private String fullcourseid;
	private String 	semester;
	private String unique;
	private String courseid;
	
	private boolean courseIdAvailable, fullCourseIdTooShort;

	public BBClass(String name, String bbid, String fullcourseid)
	{
		//name is now blank, and the Course ID is mysteriously absent :( - all as of 8/29/2012 
		//oh ho ho! It was fixed, how wonderful. Ignore commented stuff below
		//checks to see if it is what is now a legitimate courseid
		//year (2 digits) followed by semester (in caps) followed by name followed by unique (digits in parentheses)
	//	if(courseid.matches("^\\d\\d[A-Z]{1,2} .*?(\\d+?)$"))
	//		Log.d("BBClass check", "Class format is good");

		
		
		if(!fullcourseid.matches("^\\d{4}_[a-z]+?_\\d{5}_[A-Za-z]+?_\\w+$"))
			Log.d("BBClass check", "Class Course ID malformed: " + fullcourseid);
		if(!name.matches("^\\d{2}[A-Z]{1,2} .*?\\(\\d+?\\)$"))
			Log.d("BBClass check", "Class Name malformed: " + name);
		
		//filter out the year and semester at the beginning and the unique at the end
		//year/semester should never fail.  If no space, indexOf returns -1 and you get the whole string
		this.name = (name.contains("(") && name.charAt(0) != '(' && name.indexOf(" ")+1 <= name.indexOf("(")-1) 
																? name.substring(name.indexOf(" ")+1,name.indexOf("(")-1)
																: name.substring(name.indexOf(" ")+1);
		
		this.bbid = bbid;
		this.fullcourseid = fullcourseid;
		//some courseid's are malformed (ex. 00002), can't pull semester out of that unfortunately
		try
		{
			//pulls the first section and second section of courseid, capitalizes the first letter of the semester
			this.semester = fullcourseid.split("_")[0]+" "+(fullcourseid.split("_")[1].charAt(0)+"").toUpperCase()+fullcourseid.split("_")[1].substring(1);	
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			this.semester = "Unknown";
		}
		
		if(fullcourseid.split("_").length>=3)	
		{	
			fullCourseIdTooShort = false;
			this.unique = fullcourseid.split("_")[2];
			//assumes Course ID is directly after unique_ and is at the end of the string
			//will fail if unique start is less than 6 characters from the end of the string.
			try
			{
				courseid = fullcourseid.substring(fullcourseid.indexOf(unique)+6).replaceAll("_"," ");
				courseIdAvailable = true;
			}
			catch(Exception ex)
			{
				courseIdAvailable = false;
			}
		}
		else
			fullCourseIdTooShort = true;
		
		
	}
	public boolean isFullCourseIdTooShort()
	{
		return fullCourseIdTooShort;
	}
	public boolean isCourseIdAvailable()
	{
		return courseIdAvailable;
	}
	public String getCourseId()
	{
		return courseid;
	}
	public String getUnique()
	{
		return unique;
	}
	public String getName() 
	{
		return name;
	}
	public void setName(String name)
	{
		this.name = name;
	}
	public String getBbid()
	{
		return bbid;
	}
	public void setBbid(String bbid) 
	{
		this.bbid = bbid;
	}
	public String getFullCourseid() 
	{
		return fullcourseid;
	}
	public void setFullCourseid(String fullcourseid) 
	{
		this.fullcourseid = fullcourseid;
	}
	public String getSemester() 
	{
		return semester;
	}
	public void setSemester(String semester)
	{
		this.semester = semester;
	}
}
