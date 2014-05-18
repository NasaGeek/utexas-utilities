package com.mapsaurus.paneslayout;

import android.content.Context;
import android.support.v4.widget.SlidingPaneLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class MySlidingPaneLayout extends SlidingPaneLayout {

	private boolean isShowingWebView = false;
	
	public MySlidingPaneLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	@Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
		if (event.getX() > getWidth() / 9 && isShowingWebView && !isOpen() ) {
             return false;
		}
		return super.onInterceptTouchEvent(event);
    }
	
	public void setIsShowingWebView(boolean show) {
		isShowingWebView = show;
	}

}
