package com.honeywell.iaq.widget;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewConfigurationCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

import com.honeywell.lib.widgets.directionalviewpager.DirectionalViewPager;
import com.honeywell.net.utils.Logger;

/**
 * Created by zhujunyu on 2017/4/5.
 */

public class CustomViewPager extends DirectionalViewPager {
    private String TAG = "CustomViewPager";
    final private int mTouchSlop;
    private OnPageChangeListener mScrollListener;
    private float mScrollPositionOffset;
    private float mLastMotionY;
    private int mActivePointerId;
    private final static int INVALID_POINTER_ID = -1;
    private MyDirectListener myDirectListener;

    public CustomViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        setStaticTransformationsEnabled(true);
        final ViewConfiguration configuration = ViewConfiguration.get(context);
        mTouchSlop = ViewConfigurationCompat
                .getScaledPagingTouchSlop(configuration);
        super.setOnPageChangeListener(new MyOnPageChangeListener());
    }

    public void setMyDirectListener(MyDirectListener myDirectListener) {
        this.myDirectListener = myDirectListener;
    }


    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        final int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                mLastMotionY = ev.getY();
                mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                break;
            }
            case MotionEventCompat.ACTION_POINTER_DOWN: {
                final int index = MotionEventCompat.getActionIndex(ev);
                final float y = MotionEventCompat.getY(ev, index);
                mLastMotionY = y;
                mActivePointerId = MotionEventCompat.getPointerId(ev, index);
                break;
            }
            case MotionEvent.ACTION_MOVE: {

                break;
            }
            case MotionEvent.ACTION_UP:
                if (mActivePointerId != INVALID_POINTER_ID) {
                    // Scroll to follow the motion event
                    final int activePointerIndex = MotionEventCompat
                            .findPointerIndex(ev, mActivePointerId);
                    float y ;
                    try {
                        y = MotionEventCompat.getY(ev, activePointerIndex);
                    }catch (Exception e){
                        return super.onTouchEvent(ev);
                    }
//                    final float y = MotionEventCompat.getY(ev, activePointerIndex);
                    final float deltaY = mLastMotionY - y;
                    final float oldScrolly = getScrollY();
                    final int height = getHeight();
                    final int heightWithMargin = height + getPageMargin();
                    final int lastItemIndex = getAdapter().getCount() - 1;
                    final int currentItemIndex = getCurrentItem();
                    final float topBound = Math.max(0, (currentItemIndex - 1)
                            * heightWithMargin);
                    final float bottomBound = Math.min(currentItemIndex + 1,
                            lastItemIndex) * heightWithMargin;
                    final float scrollY = oldScrolly + deltaY;
                    if (mScrollPositionOffset == 0) {
                        if (scrollY < topBound) {
                            if (topBound == 0) {
                                final float over = deltaY + mTouchSlop;
                                // System.out.println("---左边第一页-->>");
                                myDirectListener.getsliderLister(0);
                            }
                        } else if (scrollY > bottomBound) {
                            if (bottomBound == lastItemIndex * heightWithMargin) {
                                // System.out.println("---右边最后一页-->>");
                                myDirectListener.getsliderLister(1);
                            }
                        }
                    } else {
                        mLastMotionY = y;
                    }
                } else {
                }
                break;
            case MotionEvent.ACTION_CANCEL: {
                mActivePointerId = INVALID_POINTER_ID;
                break;
            }
            case MotionEvent.ACTION_POINTER_UP: {
                final int pointerIndex = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                final int pointerId = MotionEventCompat.getPointerId(ev,
                        pointerIndex);
                if (pointerId == mActivePointerId) {
                    // This was our active pointer going up. Choose a new
                    // active pointer and adjust accordingly.
                    final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    mLastMotionY = ev.getX(newPointerIndex);
                    mActivePointerId = MotionEventCompat.getPointerId(ev,
                            newPointerIndex);
                }
                break;
            }
        }

        return super.onTouchEvent(ev);
    }


    private class MyOnPageChangeListener implements OnPageChangeListener {

        @Override
        public void onPageScrolled(int position, float positionOffset,
                                   int positionOffsetPixels) {
            if (mScrollListener != null) {
                mScrollListener.onPageScrolled(position, positionOffset,
                        positionOffsetPixels);
            }
            mScrollPositionOffset = positionOffset;
        }

        @Override
        public void onPageSelected(int position) {

            if (mScrollListener != null) {
                mScrollListener.onPageSelected(position);
            }
        }

        @Override
        public void onPageScrollStateChanged(final int state) {

            if (mScrollListener != null) {
                mScrollListener.onPageScrollStateChanged(state);
            }
            if (state == SCROLL_STATE_IDLE) {
                mScrollPositionOffset = 0;
            }
        }
    }

    public interface MyDirectListener {
        public void getsliderLister(int direct);
    }
}
