package com.nasageek.utexasutilities.libraries;

//Container for books checked out (retrieved from account page)

public class cBook {
	
	String title = "";
	String barcode = "";
	String status = "";
	String callNumber = "";
	boolean renew = false;
	String renewValue = ""; //value in page for checkbox to renew book
	
	public String toString()
	{
		return String.format("title: %s\n barcode: %s\n status: %s\n callNumber: %s\n renew: %s\n", title, barcode, status, callNumber, ""+renew);
	}

}
