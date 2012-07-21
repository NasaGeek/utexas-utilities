package com.nasageek.utexasutilities;

import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.os.Build;


public class Utility {
	@TargetApi(9)
	public static void commit(SharedPreferences.Editor editor)
	{
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD)
			editor.apply();
		else
			editor.commit();
	}
}
