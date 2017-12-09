package com.honeywell.iaq.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;

import com.honeywell.net.utils.Logger;

/**
 * Created by zhujunyu on 2017/4/5.
 */

public class CustomScrollView extends ScrollView {
    private String TAG = "CustomScrollView";

    public CustomScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomScrollView(Context context) {
        super(context);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {


        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastY = ev.getY();
                break;
//            case MotionEvent.ACTION_MOVE:
//                float y = ev.getY();
//                Logger.e(TAG, "" + Math.abs(mLastY - y));
////                Logger.e(TAG, "" + Math.abs(mLastY - y));
//                if (Math.abs(mLastY - y) > 200) {
//                    return super.onInterceptTouchEvent(ev);
//                }else {
//                    return false;
//                }
        }

        return super.onInterceptTouchEvent(ev);
    }


    private boolean mCanScroll = true;
    private float mLastY;

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        int y = (int) ev.getY();
//        Logger.d(TAG, "action：" + ev.getAction());
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:

                //上下移动距离很小的时候


                int currentY = getScrollY();
                int deltaY = (int) (y - mLastY);
                if (deltaY < 0) {
                    if ((getChildAt(0).getMeasuredHeight() == (currentY + getHeight()))) {//到达底部
                        mCanScroll = false;
                    }

                } else {
                    //上拉
                    if (currentY == 0) {//到达顶部
                        mCanScroll = false;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                mCanScroll = true;
                break;
            case MotionEvent.ACTION_CANCEL:
                mCanScroll = true;
                break;
        }
//        Logger.e(TAG, "mCanScroll:" + mCanScroll);
        if (mCanScroll) {            //通知ViewPager不要干扰自身的操作
            getParent().requestDisallowInterceptTouchEvent(true);
            return super.onTouchEvent(ev);
        } else {
            getParent().requestDisallowInterceptTouchEvent(false);
            return false;
        }
    }

}
