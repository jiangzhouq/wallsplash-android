package com.iyun.unsplash.activities;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

/**
 * Created by qjizho on 16/2/3.
 */
public class NointerRelativeLayout extends RelativeLayout {
    public NointerRelativeLayout(Context context) {
        super(context);
        requestDisallowInterceptTouchEvent(false);
    }

    public NointerRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NointerRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return false;
    }
}
