package com.honeywell.lib.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.honeywell.lib.R;

/**
 * Created by milton on 16/7/12.
 */
public class RectangleRelativeLayout extends RelativeLayout {
    private boolean mBasedOnWidth = true;
    private float mPercent = 1.0f;

    public RectangleRelativeLayout(Context context) {
        super(context);
    }

    public RectangleRelativeLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RectangleRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RectangleView);
        mBasedOnWidth = a.getBoolean(R.styleable.RectangleView_rv_based_on_width, true);
        mPercent = a.getFloat(R.styleable.RectangleView_rv_percent, 1.0f);
        a.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(getDefaultSize(0, widthMeasureSpec), getDefaultSize(0, heightMeasureSpec));
        if (mBasedOnWidth) {
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(getMeasuredWidth(), MeasureSpec.EXACTLY);
            heightMeasureSpec = MeasureSpec.makeMeasureSpec((int) (getMeasuredWidth() * mPercent), MeasureSpec.EXACTLY);
        } else {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(getMeasuredHeight(), MeasureSpec.EXACTLY);
            widthMeasureSpec = MeasureSpec.makeMeasureSpec((int) (getMeasuredHeight() * mPercent), MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}