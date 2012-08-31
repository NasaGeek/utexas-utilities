package com.nasageek.utexasutilities.libraries;

public class Room {

	public String location;
	public String room = "";
	public String reqFeatures;
	public String seating;
	public String available;
	public String reserveLink;
	public String groupName;

	public String toString()
	{
		return String.format("location: %s room: %s reqFeatures: %s seating: %s available: %s reserveLink: %s", location,room, reqFeatures, seating, available, reserveLink);
	}


}
