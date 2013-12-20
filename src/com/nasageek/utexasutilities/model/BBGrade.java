package com.nasageek.utexasutilities.model;

public class BBGrade {
	String name, grade, pointsPossible, comment;
	
	public BBGrade(String name, String grade, String pointsPossible, String comment) {
		this.name = name;
		this.grade = grade;
		this.pointsPossible = pointsPossible;
		this.comment = comment;
	}
	public String getName() {
		return name;
	}
	public String getComment() {
		return comment;
	}
	public Number getNumGrade() {
		if(!grade.equals("-")) {
			String temp = grade.replaceAll("[^\\d\\.]*", "");
			if(temp.equals("")) {
				return -2;
			}
			double d = Double.parseDouble(temp);
			if(d == Math.floor(d)) {
				return (int)d;
			}
			else
				return d;
		}	
		else
			return -1;
	}
	public String getGrade() {
		return grade;
	}
	public String getPointsPossible() {
		return pointsPossible;
	}
	public Number getNumPointsPossible() {
		String temp = pointsPossible.replaceAll("[^\\d\\.]*", "");
		double d = Double.parseDouble(temp);
		if(d == Math.floor(d)) {
			return (int)d;
		}
		else
			return d;
	}

}