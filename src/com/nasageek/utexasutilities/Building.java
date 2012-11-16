package com.nasageek.utexasutilities;

public class Building
{
	String id;
	String room;
	public Building(String i, String r)
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