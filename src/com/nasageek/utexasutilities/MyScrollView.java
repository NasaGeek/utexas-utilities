
package com.nasageek.utexasutilities;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ScrollView;

public class MyScrollView extends ScrollView {

    public MyScrollView(Context con) {
        super(con, null);
    }

    public MyScrollView(Context con, AttributeSet attrs) {
        super(con, attrs);
    }

    public MyScrollView(Context con, AttributeSet attrs, int defStyle) {
        super(con, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    }

    /**
     * Check if this view can be scrolled vertically in a certain direction.
     * 
     * @return true if this view can be scrolled, false otherwise.
     */
    public boolean canScrollVertically() {
        final int offset = computeVerticalScrollOffset();
        final int range = computeVerticalScrollRange() - computeVerticalScrollExtent();
        if (range == 0) {
            return false;
        }
        return offset > 0 || offset < range - 1;
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
