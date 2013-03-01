package com.nasageek.utexasutilities;

import java.io.Serializable;

public class Pair<F,S> implements Serializable{

	static final long serialVersionUID = 1L;
	
	public final F first;
	public final S second;
	
	public Pair(F first, S second) 
	{
		this.first = first;
		this.second = second;	
	}
	
} 
