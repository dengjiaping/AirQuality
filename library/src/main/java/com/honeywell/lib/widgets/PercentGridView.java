package com.honeywell.lib.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;
import android.widget.ListView;

import com.honeywell.lib.utils.LogUtil;


public class PercentGridView extends GridView {

    private static final String TAG = PercentGridView.class.getSimpleName();


    public PercentGridView(Context context) {
        super(context);
    }

    public PercentGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PercentGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if (widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY) {
            setMeasuredDimension(widthSize, heightSize);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

}
