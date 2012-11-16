package com.nasageek.utexasutilities;

public class Classtime
{
	char day;
	String starttime, endtime;
	UTClass utclass;
	Building buil;
	String unique;
	String color;
	
	public Classtime(String d, String t, Building b, String c, UTClass clz)
	{
		color = c;
		utclass = clz;
		starttime = t.split("-")[0];
		endtime = t.split("-")[1];
	
		if(endtime.charAt(endtime.length()-1)=='P' && 
				(Integer.parseInt(endtime.split(":")[0])>Integer.parseInt(starttime.split(":")[0]) || starttime.split(":")[0].equals("12")) && 
				!(endtime.split(":")[0].equals("12")))
			{starttime = starttime+"P";}
			
		day = d.charAt(0); buil = b;
	}
	public Classtime(char d,String s, String e, String bid)
	{
		starttime = s;
		endtime = e;
		day = d;
		buil=new Building(bid,"0");	
	}
	public char getDay()
	{
		return day;
	}
	public String getStartTime()
	{
		return starttime;
	}
	public String getEndTime()
	{
		return endtime;
	}
	public Building getBuilding()
	{
		return buil;
	}
	public String getColor()
	{
		return color;
	}
	public UTClass getUTClass()
	{
		return utclass;
	}
}