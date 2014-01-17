package com.nasageek.utexasutilities;


import roboguice.util.temp.Ln;

import android.util.Log;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.SpiceService;

public class MySpiceManager extends SpiceManager {

	public MySpiceManager(Class<? extends SpiceService> spiceServiceClass) {
		super(spiceServiceClass);
		Ln.getConfig().setLoggingLevel(Log.ERROR);
	}
}
