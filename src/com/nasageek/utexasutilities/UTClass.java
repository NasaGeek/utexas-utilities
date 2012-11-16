package com.nasageek.utexasutilities;

import java.util.ArrayList;

import android.util.Log;

public class UTClass {
	
	private String courseid, unique, name, professor, semId, color;

	private ArrayList<Building> buildings;
	private ArrayList<Classtime> classtimes;

	
	
	public UTClass(String u, String ci, String n, String[] b, String[] br, String[] d, String[] t, String semId, String c)
	{
		this.semId = semId;
		classtimes = new ArrayList<Classtime>();
		buildings = new ArrayList<Building>();
		//Log.d("BLENGTH", b.length+ " "+br.length);
		courseid = ci;
		name = n;
		unique = u;
		color = c;
		for(int i = 0; i<b.length; i++)
		{
			buildings.add(new Building(b[i], br[i]));
		}
		
		if(!(d.length == t.length && d.length == buildings.size() && buildings.size() == t.length))
			Log.d("UTClass creation", "building/day/time size inconsistency: b"+buildings.size()+" d"+d.length+" t"+t.length);
		for(int i = 0; i < d.length && i < t.length && i < buildings.size(); i++)
		{
			String[] days = d[i].split("");
			
			for(int k = 1; k<days.length; k++)
			{
				classtimes.add(new Classtime(days[k],t[i],buildings.get(i),c,this));
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
	public ArrayList<Classtime> getClassTimes()
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