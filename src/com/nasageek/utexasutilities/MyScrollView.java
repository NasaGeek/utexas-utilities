package com.nasageek.utexasutilities;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ScrollView;



public class MyScrollView extends ScrollView {

	public MyScrollView(Context con)
	{
		super(con,null);
	}
	public MyScrollView(Context con, AttributeSet attrs)
	{
		super(con, attrs);
	}
	public MyScrollView(Context con, AttributeSet attrs, int defStyle)
	{
		super(con, attrs, defStyle);
	}
	
	public boolean canScroll() {
        View child = getChildAt(0);
        if (child != null) {
            int childHeight = child.getMeasuredHeight();
            return getMeasuredHeight() < childHeight + getPaddingTop() + getPaddingBottom();
        }
        return false;
    }
}
