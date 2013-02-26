package com.nasageek.utexasutilities.model;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.util.TimeFormatException;

public class FeedItem
{
	/*public enum SourceType {
		
	}*/
	
	private BBClass clz;
	private String bbid, type, message, contentid, name, sourcetype;
	private Date date;
	
	public FeedItem(String type, String message, String contentid, BBClass clz, String sourcetype, String date, SimpleDateFormat formatter)
	{
		this.type = type;
		this.message = message;
		this.contentid = contentid;
		this.sourcetype = sourcetype;
		this.clz = clz;
		try {
			this.date = formatter.parse(date);
		} catch (ParseException e) {
			//TODO
			e.printStackTrace();
		}
	}
	public String getBbId()
	{
		return clz.getBbid();
	}
	public String getCourseId()
	{
		return clz.getCourseId();
	}
	public String getName()
	{
		return clz.getName();
	}
	public Date getDate()
	{
		return date;
	}
	public String getType()
	{
		//fallback for what I presume to be Blackboard's old format
		if(sourcetype == null)
		{
			if("ANNOUNCEMENT".equals(type))
				return "Announcement";
			else
				return "Unknown";
		}
		else if("CO".equals(sourcetype))
			return "Content";
		else if("GB".equals(sourcetype))
			return "Grades";
		else if("CR".equals(sourcetype))
			return "Courses";
		//TODO: fix this, AS should take us to grades I think... notifications are annoying
		else if("AS".equals(sourcetype))
			return "Notification";
		else if("AN".equals(sourcetype))
			return "Announcement";
		else
			return "Unknown";
	}
	public String getMessage()
	{
		return message;
	}
	public String getContentId()
	{
		return contentid;
	}
	public BBClass getBbClass()
	{
		return clz;
	}

}