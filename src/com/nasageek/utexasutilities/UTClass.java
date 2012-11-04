package com.nasageek.utexasutilities;

import java.util.ArrayList;

import android.util.Log;

public class UTClass {
	
	private String courseid, unique, name, professor, semId, color;

	private ArrayList<building> buildings;
	private ArrayList<classtime> classtimes;

	
	
	public UTClass(String u, String ci, String n, String[] b, String[] br, String[] d, String[] t, String semId, String c)
	{
		this.semId = semId;
		classtimes = new ArrayList<classtime>();
		buildings = new ArrayList<building>();
		//Log.d("BLENGTH", b.length+ " "+br.length);
		courseid = ci;
		name = n;
		unique = u;
		color = c;
		for(int i = 0; i<b.length; i++)
		{
			buildings.add(new building(b[i], br[i]));
		}
		
		if(!(d.length == t.length && d.length == buildings.size() && buildings.size() == t.length))
			Log.d("UTClass creation", "building/day/time size inconsistency: b"+buildings.size()+" d"+d.length+" t"+t.length);
		for(int i = 0; i < d.length && i < t.length && i < buildings.size(); i++)
		{
			String[] days = d[i].split("");
			
			for(int k = 1; k<days.length; k++)
			{
				classtimes.add(new classtime(days[k],t[i],buildings.get(i),c,this));
				//Log.d("DAYTIME", days[k]+" "+t[i]);
			}
			
		}
	}
	@Override
	public String toString()
	{
		String out = courseid +" in ";
		for(int i =0; i<classtimes.size(); i++)
		{
			out += classtimes.get(i).getBuilding().getId()+" in room "+classtimes.get(i).getBuilding().getRoom()+
					" at "+classtimes.get(i).getStartTime()+"-"+classtimes.get(i).getEndTime()+" on "+classtimes.get(i).getDay();
			if(i==classtimes.size()-1)
				continue;
			else out +=" and in ";
		}
		
		return out;
	}	
	public ArrayList<classtime> getClassTimes()
	{
		return classtimes;
	}
	public String getName()
	{
		return name;
	}
	public String getId()
	{
		return courseid;
	}
	public String getUnique()
	{
		return unique;
	}
	public String getSemId()
	{
		return semId;
	}
	public String getColor()
	{
		return color;
	}
}

class classtime
{
	char day;
	String starttime, endtime;
	UTClass utclass;
	building buil;
	String unique;
	String color;
	
	public classtime(String d, String t, building b, String c, UTClass clz)
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
	public classtime(char d,String s, String e, String bid)
	{
		starttime = s;
		endtime = e;
		day = d;
		buil=new building(bid,"0");	
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
	public building getBuilding()
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
class building
{
	String id;
	String room;
	public building(String i, String r)
	{
		//Log.d("BUILDING", "constructed");
		id = i; room = r;
	}
	public String getRoom()
	{
		return room;
	}
	public String getId()
	{
		return id;
	}
	public void setId(String id)
	{
		this.id = id;
	}
}