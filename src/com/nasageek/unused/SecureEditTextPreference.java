package com.nasageek.unused;

import com.nasageek.utexasutilities.SecurePreferences;

import android.content.Context;
import android.preference.EditTextPreference;
import android.util.AttributeSet;

public class SecureEditTextPreference extends EditTextPreference {

	SecurePreferences sp;
	String mText;
	
	public SecureEditTextPreference(Context con)
	{
		super(con);
		sp = new SecurePreferences(con,"com.nasageek.utexasutilities", "lalalakeyhere", false);
	}
	public SecureEditTextPreference(Context con, AttributeSet as)
	{
		super(con,as);
		sp = new SecurePreferences(con,"com.nasageek.utexasutilities", "lalalakeyhere", false);
	}
	public SecureEditTextPreference(Context con, AttributeSet as, int defStyle)
	{
		super(con,as,defStyle);
		sp = new SecurePreferences(con,"com.nasageek.utexasutilities", "lalalakeyhere", false);
	}
	@Override
	public void setText(String text)
	{
		boolean wasBlocking = shouldDisableDependents();
		
	//	mText = sp.encryptString(text);
		persistString(mText);
		
		boolean isBlocking = shouldDisableDependents();
		if(isBlocking != wasBlocking)
		{
			notifyDependencyChange(isBlocking);	
		}
	}
	@Override
	public String getText()
	{
		return null;//sp.decryptString(mText);
	}
	@Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        if(mText == null)
        	setText(restoreValue ? getPersistedString(mText) : (String) defaultValue);
    //    else
    //    	setText(restoreValue ? getPersistedString(sp.decryptString(mText)) : (String) defaultValue);
    }
	
}
