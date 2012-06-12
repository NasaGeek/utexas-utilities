package com.nasageek.utexasutilities;

public class BBClass {

	private String name;
	private String bbid;
	private String courseid;
	private String semester;

	public BBClass(String name, String bbid, String courseid)
	{
		this.name = name.substring(name.indexOf(" ")+1);
		this.bbid = bbid;
		this.courseid = courseid;
		this.semester = courseid.split("_")[0]+" "+(courseid.split("_")[1].charAt(0)+"").toUpperCase()+courseid.split("_")[1].substring(1);
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
