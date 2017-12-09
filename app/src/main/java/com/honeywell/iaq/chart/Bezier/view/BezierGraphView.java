package com.honeywell.iaq.chart.Bezier.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.honeywell.iaq.R;
import com.honeywell.iaq.utils.DensityUtil;

/**
 * Created by zhujunyu on 2017/4/8.
 */

public class BezierGraphView extends View {
    private static final String TAG = BezierGraphView.class.getSimpleName();
    private static final int POINT_CIRCLE_RADIUS = 5;
    private static final int Y_AXIS_MAX_VALUE = 120;
    private static final int Y_AXIS_START_VALUE = 40;
    private static final int X_AXIS_POINT_COUNT = 12;
    private static final int HORIZONTAL_DIVIDER_LINE_NUM = 5;
    private static final int TOUCH_MIS = 50;
    public static final int ANIMATION_DURATION = 2 * 1000;
    public static final float ANIMATION_END_VALUE = 1.0f;

    private Path mCurvePath;
    private Paint mCurvePaint;
    private Paint mHorizontalLinePaint;
    private Paint mIndicatePointPaint;
    private Paint mTextPaint;

    private int[] mDataValue;

    private float mViewWidth;
    private float mViewHeight;
    private float mContentHeight;
    private float mContentWidth;

    private float mPaddingStart;
    private float mPaddingEnd;
    private float mPaddingTop;
    private float mPaddingBottom;

    //标示线的纵向距离
    private float mHorizontalLineDividerHeight;
    //Y轴上能表达的值，Y轴最大值 减去 Y轴起始值
    private float mAxisYContentValue;
    //Y轴上的比例，Y轴的高度 除以 Y周能表达的值
    private float mAxisYValueScale;

    private float[] mTimePointsAxisX;
    private float[] mDataPointAxisX;
    private float mTextSize;
    private float mTopTextSize;
    private float mTopTextPaddingBottom;

    private Paint mTopTextPaint;
    private float mGraphAxisYStartPosition;
    private int mTouchPoint = -1;
    private PathMeasure mPathMeasure;
    private Path mDstAnimationPath;
    private ValueAnimator mValueAnimator;
    private int mDrawPointNum;
    private float mPerPoint;
    private DelayRunnable mDelayRunnable;

    public BezierGraphView(Context context) {
        super(context);
        init(context);
    }

    public BezierGraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public BezierGraphView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void setData(final int[] data) {
        removeCallbacks(mDelayRunnable);
        reset();
        if (mDelayRunnable == null) {
            mDelayRunnable = new DelayRunnable();
        }
        mDelayRunnable.setData(data);
        postDelayed(mDelayRunnable, 150);
    }

    private class DelayRunnable implements Runnable {
        private int[] data;

        public void setData(int[] data) {
            this.data = data;
        }

        @Override
        public void run() {
            setDataDelay(data);
        }
    }

    private void reset() {
        if (mValueAnimator.isRunning()) {
            mValueAnimator.cancel();
        }
        mDrawPointNum = 0;
        mDstAnimationPath.reset();
        mCurvePath.reset();
        mTouchPoint = -1;
        invalidate();
    }

    private void setDataDelay(int[] data) {
        if (data != null && data.length > 0) {
            if (data.length >= X_AXIS_POINT_COUNT) {
                System.arraycopy(data, 0, mDataValue, 0, X_AXIS_POINT_COUNT);
            } else {
                System.arraycopy(data, 0, mDataValue, 0, data.length);
                for (int i = data.length; i < X_AXIS_POINT_COUNT; i++) {
                    mDataValue[i] = 0;
                }
            }

            reset();
            //多条小贝塞尔曲线组成一条大曲线，每一条贝塞尔曲线都是三次贝塞尔曲线，两个控制点
            float lastPointX;
            float lastPointY;
            for (int i = 0; i < X_AXIS_POINT_COUNT; i++) {
                if (mDataValue[i] != 0) {
                    lastPointX = mDataPointAxisX[i];
                    lastPointY = mGraphAxisYStartPosition - (mDataValue[i] - Y_AXIS_START_VALUE) * mAxisYValueScale;

                    for (int j = i; j < X_AXIS_POINT_COUNT; j++) {
                        if (mDataValue[j] != 0) {
                            float endPointX = mDataPointAxisX[j];
                            float endPointY = mGraphAxisYStartPosition - (mDataValue[j] - Y_AXIS_START_VALUE) * mAxisYValueScale;
                            mCurvePath.setLastPoint(lastPointX, lastPointY);
                            mCurvePath.cubicTo((endPointX + lastPointX) / 2, lastPointY, (lastPointX + endPointX) / 2, endPointY, endPointX, endPointY);

                            lastPointX = endPointX;
                            lastPointY = endPointY;
                        }
                    }
                    break;
                }
//                float startPointX = mDataPointAxisX[i - 1];
//                float startPointY = mGraphAxisYStartPosition - (mDataValue[i - 1] - Y_AXIS_START_VALUE) * mAxisYValueScale;
//                float endPointX = mDataPointAxisX[i];
//                float endPointY = mGraphAxisYStartPosition - (mDataValue[i] - Y_AXIS_START_VALUE) * mAxisYValueScale;
//                //使用setLastPoint才能使用PathMeasure获得准确长度，做出动画效果
//                mCurvePath.setLastPoint(startPointX, startPointY);
//                mCurvePath.cubicTo((endPointX + startPointX) / 2, startPointY, (startPointX + endPointX) / 2, endPointY, endPointX, endPointY);
            }

            mPathMeasure.setPath(mCurvePath, false);
            mValueAnimator.start();
        } else {
            Log.e(TAG, "data can not be null!");
        }
    }

    public void setCurveColor(int colorId) {
        mCurvePaint.setColor(getResources().getColor(colorId));
        invalidate();
    }

    private void init(Context context) {
        mPaddingTop = DensityUtil.dip2px(context, 5);
        mPaddingStart = DensityUtil.dip2px(context, 12);
        mPaddingEnd = DensityUtil.dip2px(context, 12);
        mPaddingBottom = DensityUtil.dip2px(context, 20);

        //TODO 基础心率先不显示，等显示再设置回来，不然view的高度太多空白
        mTopTextSize = DensityUtil.sp2px(context, 0);
        mTopTextPaddingBottom = DensityUtil.dip2px(context, 12);
        mTextSize = DensityUtil.sp2px(context, 13);

        mDataValue = new int[X_AXIS_POINT_COUNT];
        mAxisYContentValue = Y_AXIS_MAX_VALUE - Y_AXIS_START_VALUE;

        mTimePointsAxisX = new float[X_AXIS_POINT_COUNT];
        mDataPointAxisX = new float[X_AXIS_POINT_COUNT];

        mCurvePath = new Path();
        mDstAnimationPath = new Path();
        mPathMeasure = new PathMeasure();
        mPerPoint = ANIMATION_END_VALUE / X_AXIS_POINT_COUNT;
        mValueAnimator = ValueAnimator.ofFloat(0f, ANIMATION_END_VALUE);
        mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float length = mPathMeasure.getLength();
                float factor = (float) animation.getAnimatedValue();
                float currentPathLength = length * factor;
                mDrawPointNum = (int) (factor / mPerPoint);
                mPathMeasure.getSegment(0, currentPathLength, mDstAnimationPath, true);
                invalidate();
            }
        });
        mValueAnimator.setDuration(ANIMATION_DURATION);

        mHorizontalLinePaint = new Paint();
        mHorizontalLinePaint.setAntiAlias(true);
        mHorizontalLinePaint.setStyle(Paint.Style.STROKE);
        mHorizontalLinePaint.setStrokeWidth(3);
        mHorizontalLinePaint.setColor(Color.WHITE);

        mIndicatePointPaint = new Paint();
        mIndicatePointPaint.setAntiAlias(true);
        mIndicatePointPaint.setStyle(Paint.Style.FILL);
        mIndicatePointPaint.setStrokeWidth(5);
        mIndicatePointPaint.setColor(Color.WHITE);

//        LinearGradient lg = new LinearGradient(0, 0, DensityUtil.dip2px(getContext(), 720), 0,
//                getResources().getColor(R.color.step_bar_start_color),
//                getResources().getColor(R.color.step_bar_end_color),
//                Shader.TileMode.CLAMP);

        mCurvePaint = new Paint();
        mCurvePaint.setAntiAlias(true);
        mCurvePaint.setStyle(Paint.Style.STROKE);
        mCurvePaint.setStrokeWidth(5);
//        mCurvePaint.setShader(lg);
        mCurvePaint.setColor(Color.parseColor("#FDD119"));
        mCurvePaint.setPathEffect(new CornerPathEffect(0xFF));

        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setColor(Color.WHITE);

        mTopTextPaint = new Paint();
        mTopTextPaint.setAntiAlias(true);
        mTopTextPaint.setTextSize(mTopTextSize);
        mTopTextPaint.setColor(getResources().getColor(R.color.white));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mViewWidth = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        mViewHeight = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);

        mContentHeight = mViewHeight - mPaddingTop - mPaddingBottom;
        mContentWidth = mViewWidth - mPaddingStart - mPaddingEnd;
        float graphHeight = mContentHeight - mTopTextSize - mTextSize * 2 - mTopTextPaddingBottom;

        mGraphAxisYStartPosition = mViewHeight - mPaddingBottom - mTextSize;

        //X轴每个点隔开的宽度
        float axisXDividerWidth = mContentWidth / (float) X_AXIS_POINT_COUNT;
        //让曲线的每个点，在X轴每个时间点的中间，表达平均值的意思
        float dataPointOffSet = axisXDividerWidth / 2.0f + mTextSize / 2;
        //Y轴内容的比例是Y轴的高度除以Y轴表达的值，得到Y轴的每一点的值对应的view高度
        mAxisYValueScale = graphHeight / mAxisYContentValue;

        mHorizontalLineDividerHeight = graphHeight / (HORIZONTAL_DIVIDER_LINE_NUM - 1);

        for (int i = 0; i < X_AXIS_POINT_COUNT; i++) {
            mTimePointsAxisX[i] = mPaddingStart + axisXDividerWidth * i;
            mDataPointAxisX[i] = mTimePointsAxisX[i] + dataPointOffSet;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
//        canvas.drawText(mContext.getString(R.string.resting_heart_rate) + mBaseHeartRate, mPaddingStart - mTextSize / 3, mPaddingTop + mTopTextSize, mTopTextPaint);

        //画横向指示线
        mTextPaint.setAlpha(0x7F);
        for (int i = 0; i < HORIZONTAL_DIVIDER_LINE_NUM; i++) {
            float y = mViewHeight - mPaddingBottom - mTextSize - mHorizontalLineDividerHeight * i;

            //画横向指示线的每根线上面的指示数值
            if (i != 0) {
                String text = String.valueOf((int) (mAxisYContentValue / (HORIZONTAL_DIVIDER_LINE_NUM - 1) * i + Y_AXIS_START_VALUE));
                canvas.drawText(text, mPaddingStart + mTextSize / 2, y - mTextSize / 4, mTextPaint);
                mHorizontalLinePaint.setAlpha(0x33);
            } else {
                canvas.drawText(String.valueOf((int) Y_AXIS_START_VALUE), mPaddingStart + mTextSize / 2, y - mTextSize / 4, mTextPaint);
                mHorizontalLinePaint.setAlpha(0x4D);
            }

            canvas.drawLine(mPaddingStart, y, mViewWidth - mPaddingEnd, y, mHorizontalLinePaint);
        }

        //画底部的时间指示点
        mTextPaint.setAlpha(0xFF);
        for (int i = 0; i < X_AXIS_POINT_COUNT; i++) {
            canvas.drawText(String.valueOf(i * 2),
                    mPaddingStart + mContentWidth / X_AXIS_POINT_COUNT * i + mTextSize / 2,
                    mViewHeight - mPaddingBottom, mTextPaint);
        }

        //根据ValueAnimation中获得的增长的值，获取对应长度的路径，造成动画效果
        canvas.drawPath(mDstAnimationPath, mCurvePaint);

        //画每个数据点
        mIndicatePointPaint.setAlpha(0xFF);
        for (int j = 0; j < mDataValue.length; j++) {

            //在动画到达的地方才画出点
            if (j < mDrawPointNum && mDataValue[j] != 0) {
                float startPointX = mDataPointAxisX[j];
                float startPointY = mGraphAxisYStartPosition - (mDataValue[j] - Y_AXIS_START_VALUE) * mAxisYValueScale;
                canvas.drawCircle(startPointX, startPointY, POINT_CIRCLE_RADIUS, mIndicatePointPaint);
            }
        }

        if (mTouchPoint != -1) {
            float dataPointY = mGraphAxisYStartPosition - (mDataValue[mTouchPoint] - Y_AXIS_START_VALUE) * mAxisYValueScale;

            mTextPaint.setAlpha(0xFF);
            canvas.drawText(String.valueOf(mDataValue[mTouchPoint]), mDataPointAxisX[mTouchPoint], dataPointY - mTextSize, mTextPaint);

            mIndicatePointPaint.setAlpha(0x33);
            canvas.drawCircle(mDataPointAxisX[mTouchPoint], dataPointY, POINT_CIRCLE_RADIUS * 5, mIndicatePointPaint);
            mTouchPoint = -1;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();

        if (!mValueAnimator.isRunning() && event.getAction() == MotionEvent.ACTION_DOWN) {

            for (int i = 0; i < X_AXIS_POINT_COUNT; i++) {
                float dataPointY = mGraphAxisYStartPosition - (mDataValue[i] - Y_AXIS_START_VALUE) * mAxisYValueScale;
                if (mDataValue[i] != 0 && touchY >= dataPointY - TOUCH_MIS && touchY <= dataPointY + TOUCH_MIS) {
                    if (touchX >= mDataPointAxisX[i] - TOUCH_MIS && touchX <= mDataPointAxisX[i] + TOUCH_MIS) {
                        mTouchPoint = i;
                        invalidate();
                        break;
                    }
                }
            }

        }
        return super.onTouchEvent(event);
    }
}