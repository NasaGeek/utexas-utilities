package com.nasageek.utexasutilities;

import java.io.Serializable;

import android.util.Pair;

public class MyPair<F, S> extends Pair<F, S> implements Serializable {
    
	private static final long serialVersionUID = 1L;

	public MyPair(F f, S s) {
    	super(f, s);
    }
}