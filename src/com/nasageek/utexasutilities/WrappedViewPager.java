package com.nasageek.utexasutilities;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.ViewGroup;

public class WrappedViewPager extends ViewPager
{

	public WrappedViewPager(Context context) 
	{
		super(context);
	}
	public WrappedViewPager(Context context, AttributeSet aSet)
	{
		super(context, aSet);
	}
	
	@Override
	public void onMeasure(int heightMeasureSpec, int widthMeasureSpec)
	{
		super.onMeasure(heightMeasureSpec, widthMeasureSpec);

		if(getChildCount() != 0 && getChildAt(0) instanceof ViewGroup)
			setMeasuredDimension(getMeasuredWidth(), ((ViewGroup)getChildAt(0)).getChildAt(0).getMeasuredHeight());
	}
	
}
