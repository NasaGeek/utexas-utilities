package com.nasageek.utexasutilities.model;

import java.io.Serializable;

public class CourseMapItem implements Serializable
{

	private static final long serialVersionUID = 1L;
	
	private String name,viewUrl,contentId,linkType;
	private boolean blackboardItem;
	
	public CourseMapItem(String name, String viewUrl, String contentId, String linkType)
	{
		this.name = name;
		this.viewUrl = viewUrl;
		this.contentId = contentId;
		this.linkType = linkType;
	}
	public String getName()
	{
		return name;
	}
	public String getViewUrl()
	{
		return viewUrl;
	}
	public String getContentId()
	{
		return contentId;
	}
	public String getLinkType()
	{
		return linkType;
	}
	public boolean isBlackboardItem()
	{
		return blackboardItem;
	}
}