package com.nasageek.UTilities;

import java.util.ArrayList;

import android.util.Log;

public class UTClass {
	
	private String courseid, unique, name, professor;

	private ArrayList<building> buildings;
	private ArrayList<classtime> classtimes;
	
	
	public UTClass(String u, String ci, String n, String[] b, String[] br, String[] d, String[] t)
	{
		classtimes = new ArrayList<classtime>();
		buildings = new ArrayList<building>();
		//Log.d("BLENGTH", b.length+ " "+br.length);
		courseid = ci;
		name = n;
		unique = u;
		for(int i = 0; i<b.length; i++)
		{
			buildings.add(new building(b[i], br[i]));
		}
		
		for(int i = 0; i<d.length; i++)
		{
			
			String[] days = d[i].split("");
			
			for(int k = 1; k<days.length; k++)
			{
				classtimes.add(new classtime(days[k],t[i],buildings.get(i),unique));
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
}

class classtime
{
	char day;
	String starttime, endtime;
	building buil;
	String unique;
	public classtime(String d, String t, building b, String u)
	{
		unique = u;
		starttime = t.split("-")[0];
		
		endtime = t.split("-")[1];
	
		if(endtime.charAt(endtime.length()-1)=='P' && 
				(Integer.parseInt(endtime.split(":")[0])>Integer.parseInt(starttime.split(":")[0]) || starttime.split(":")[0].equals("12")) && 
				!(endtime.split(":")[0].equals("12")))
			{starttime = starttime+"P";}
			
		day = d.charAt(0); buil = b;
	}
	public classtime(String u,char d,String s, String e, String bid)
	{
		unique = u;
		starttime = s;
		endtime = e;
		day = d;
		buil=new building(bid,"0");	
	}
	public char getDay()
	{
		return day;
	}
	public String getUnique()
	{
		return unique;
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