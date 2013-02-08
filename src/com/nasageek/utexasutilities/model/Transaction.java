package com.nasageek.utexasutilities.model;

public class Transaction {

	private String cost, reason, date;
	
	public Transaction(String reason, String cost, String date)
	{
		this.reason = reason;
		this.cost = cost;
		this.date = date;
	}
	public String getReason()
	{
		return this.reason;
	}
	public String getCost()
	{
		return this.cost;
	}
	public String getDate()
	{
		return this.date;
	}
}
