package com.honeywell.iaq.chart.Bezier.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.honeywell.iaq.R;
import com.honeywell.iaq.chart.Bezier.model.Point;
import com.honeywell.net.utils.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Qian Jin on 2/16/17.
 */

public class LineChartView extends View {

    private static String TAG = "LineChartView";
    public static final int CHART_TYPE_PM25 = 1;
    public static final int CHART_TYPE_TEMPERATURE = 2;
    public static final int CHART_TYPE_HUMIDITY = 3;
    public static final int CHART_TYPE_HCHO = 4;
    public static final int CHART_TYPE_CO2 = 5;
    public static final int CHART_TYPE_TVOC = 6;

    public static  int[] PM25_Y_VALUES = {0, 100, 150};
    public static  int[] TEMPERATURE_Y_VALUES = {0, 20, 30};
    public static  int[] HUMIDITY_Y_VALUES = {25, 50, 75};
    public static  int[] HOHC_Y_VALUES = {25, 50, 75};
    public static  int[] CO2_Y_VALUES = {25, 50, 75};
    public static  int[] TVOC_Y_VALUES = {25, 50, 75};

    private Context mContext;
    private int mChartType;

    //画线的画笔
    private Paint mLinePaint;
    //画虚线线的画笔
    private Paint mDottedLinePaint;
    //写字的画笔
    private Paint mTextPaint;
    //底部坐标写字的画笔
    private Paint mTextPaintBottom;
    //开始X坐标
    private int startX;
    //开始Y坐标
    private int startY;
    //结束X坐标
    private int stopX;
    //结束Y坐标
    private int stopY;
    //测量值 宽度
    private int measuredWidth;
    //测量值 高度
    private int measuredHeight;
    //每条柱状图的宽度
    private int barWidth;
    //设置最大值，用于计算比例
    private float mYMax;
    //每条柱状图的名字
    private ArrayList<String> mXValue;
    //设置每条柱状图的目标值，除以max即为比例
    private List<Float> mYValue;
    //设置一共有几条柱状图
    private int mTotalXNum;
    //设置每条柱状图的当前比例
    private Float[] currentBarProgress;
    //每条竖线之间的间距
    private int deltaX;
    //每条柱状图之间的间距
    private int deltaY;
    //底部的短bar的宽度
    private int mBottonBarWidth = 5;
    //纵坐标的文字与 横线之间的间隙
    private int space = 15;
    /**
     * 最大值得比例。如果是mg，默认为1，如果是g,该值为1000
     */
    private int mMaxScale = 1;

    private Paint mIndoorLinePaint;

    private List<Point> indoorPoints;

    private GestureDetector gestureDetector;

    private OnScrollCallback mScrollCallback;

    private OnTouchCallback mOnTouchCallback;

    //alarm y坐标
    private int mXOffset = 0;

    public interface OnScrollCallback {
        void onScroll(int index);
    }

    public interface OnTouchCallback {
        void onTouch();
    }

    private int[] indoorGradientColor;

    public void setOnScrollCallback(OnScrollCallback onScrollCallback) {
        this.mScrollCallback = onScrollCallback;
    }

    public void setOnTouchCallback(OnTouchCallback onTouchCallback) {
        this.mOnTouchCallback = onTouchCallback;
    }

    public LineChartView(Context context) {
        super(context);

        mContext = context;
    }

    public LineChartView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;
    }

    public LineChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mContext = context;
    }

    public int getChartType() {
        return mChartType;
    }

    public void setChartType(int chartType) {
        this.mChartType = chartType;

        initPaints(mContext);
    }

    public void setPm25YValues(int[] values){
        PM25_Y_VALUES = values;
    }

    public int[] getPm25YValues(){
        return PM25_Y_VALUES;
    }

    public void setTemYValues(int[] values){
        TEMPERATURE_Y_VALUES = values;
    }

    public int[] getTemYValues(){
        return TEMPERATURE_Y_VALUES;
    }

    public void setHumYValues(int[] values){
        HUMIDITY_Y_VALUES = values;
    }

    public int[] getHumYValues(){
        return HUMIDITY_Y_VALUES;
    }

    public void setHOHCYValues(int[] values){
        HOHC_Y_VALUES = values;
    }

    public int[] getHOHCYValues(){
        return HOHC_Y_VALUES;
    }

    public void setCo2YValues(int[] values){
        CO2_Y_VALUES = values;
    }

    public int[] getCo2YValues(){
        return CO2_Y_VALUES;
    }

    public void setTVOCYValues(int[] values){
        TVOC_Y_VALUES = values;
    }

    public int[] getTVOCYValues(){
        return TVOC_Y_VALUES;
    }


    /**
     * 测量方法，主要考虑宽和高设置为wrap_content的时候，我们的view的宽高设置为多少
     *
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
        if (heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(widthSpecSize, heightSpecSize);
        }

    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        //获得测量后的宽度
        measuredWidth = getMeasuredWidth();
        this.barWidth = measuredWidth * 6 / 300;
        //获得测量后的高度
        measuredHeight = getMeasuredHeight();
        //计算结束X的值(右边空余30)
        stopX = measuredWidth;
        //计算结束Y的值 预留空间写文字
        stopY = measuredHeight - 50;

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        indoorPoints = new ArrayList<Point>();

        //绘制横坐标并添加点数据
        drawHistogram(canvas);

        //绘制Indoor曲线图
        drawBeziergrama(canvas, indoorPoints, mIndoorLinePaint, indoorGradientColor);

        //绘制纵坐标刻度和坐标
        drawYAxis(canvas);

    }

    private void drawBeziergrama(Canvas canvas, List<Point> points, Paint paint, int[] gradientColor) {
        Path path = null;
        Point lastPoint = new Point();
        Point startPoint = null;

        for (int i = 0; i < points.size(); i++) {
            Point currentPoint = points.get(i);
            if (currentPoint.getY() != stopY) {
                if (path == null) {
                    path = new Path();
                    path.moveTo(currentPoint.getX(), currentPoint.getY());

                    startPoint = new Point();
                    startPoint.setX(currentPoint.getX());
                    startPoint.setY(currentPoint.getY());
                } else {
                    path.lineTo(currentPoint.getX() + 35, currentPoint.getY());
                }
            } else {
                if (path != null) {
                    drawIndoorPath(canvas, path, lastPoint, startPoint, paint, gradientColor);
                    startPoint = null;
                }
                path = null;
            }
            lastPoint = currentPoint;
        }
//        最右边点画阴影
        if (path != null) {
            drawIndoorPath(canvas, path, lastPoint, startPoint, paint, gradientColor);
        }

//        if (points == null || points.size() == 0)
//            return;
//
//        List<Float> points2 = new ArrayList<Float>();
//
//        for (Point point : points) {
//            points2.add(point.getX());
//            points2.add(point.getY());
//        }
//
//        // 贝塞尔曲线
//        Path p = new Path();
//        Point p1 = new Point();
//        Point p2 = new Point();
//        Point p3 = new Point();
//        float xp = points2.get(0);
//        float yp = points2.get(1);
//        // 设置第一个点开始
//        p.moveTo(xp, yp);
//        int length = points2.size();
//        // 设置第一个控制点33%的距离
//        float mFirstMultiplier = 0.3f;
//        // 设置第二个控制点为66%的距离
//        float mSecondMultiplier = 1 - mFirstMultiplier;
//
//        for (int b = 0; b < length; b += 2) {
//            int nextIndex = b + 2 < length ? b + 2 : b;
//            int nextNextIndex = b + 4 < length ? b + 4 : nextIndex;
//            // 设置第一个控制点
//            calc(points2, p1, b, nextIndex, mSecondMultiplier);
//            // 设置第二个控制点
//            p2.setX(points2.get(nextIndex));
//            p2.setY(points2.get(nextIndex + 1));
//            // 设置第二个控制点
//            calc(points2, p3, nextIndex, nextNextIndex, mFirstMultiplier);
//            // 最后一个点就是赛贝尔曲线上的点
//            p.cubicTo(p1.getX(), p1.getY(), p2.getX(), p2.getY(), p3.getX(), p3.getY());
//            // 画点
//        }
//        PathMeasure mPathMeasure;
//        mPathMeasure = new PathMeasure(p, false);
//        // 设置为线
//        reSetPointWithPath(mPathMeasure, points2);
//        for (int k = 0; k < points2.size()-1; k +=2) {
////            canvas.drawCircle(points.get(k), points.get(k+1), 5, paint);
//        }
//        canvas.drawPath(p, paint);

//        invalidate();
    }

    //画indoor 折线
    private void drawIndoorPath(Canvas canvas, Path path, Point lastPoint, Point startPoint, Paint paint, int[] gradientColor) {
        canvas.drawPath(path, paint);

        canvas.save();
        Path shadowPath = new Path();
        shadowPath.addPath(path);
        shadowPath.lineTo(lastPoint.getX(), stopY);
        shadowPath.lineTo(startPoint.getX(), stopY);
        shadowPath.lineTo(startPoint.getX(), startPoint.getY());
        drawGradient(shadowPath, canvas, gradientColor);
        canvas.restore();
    }

    /**
     * 画柱状图
     */
    private void drawHistogram(Canvas canvas) {
        float left, deltaLeft = 0;

        deltaY = (stopX - startX) / (mTotalXNum + 1);

        for (int i = 0; i < mTotalXNum; i++) {
            //添加50的偏移量，为了和横线左边保持一定的距离
            left = startX + deltaY + i * (deltaY + barWidth);
            if (i == 0)
                deltaLeft = left;

            //按比例算出
            float indoorTop = (mYValue.get(i) / (mYMax * mMaxScale)) * (stopY - startY);
            float right = startX + deltaY + i * (deltaY + barWidth) + barWidth;

            // 画横轴数值
            if (i < 30) {
                float left2 = (right + left - mBottonBarWidth) / 2 - deltaLeft + 25;
                canvas.drawText(mXValue.get(i), left2, stopY + 50, mTextPaintBottom);
            }
            mXOffset++;

            // 添加点
            Point point = new Point();
            point.setX(left - deltaLeft + barWidth / 2);
            point.setY((stopY - indoorTop*4/5));
            Logger.e(TAG,"stopY:"+stopX+"indoorTop"+indoorTop);
            indoorPoints.add(point);
        }
    }

    private void drawGradient(Path path, Canvas canvas, int[] gradientColor) {
        //  使用实例如下:
        Paint paint = new Paint();
        LinearGradient lg = new LinearGradient(startX, startY, startX, stopY, gradientColor, null, Shader.TileMode.MIRROR);
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setShader(lg);
        canvas.drawPath(path, paint);

    }

    private void drawYAxis(Canvas canvas) {
        int start = 100;

        int bottomTextLeft = start - getTextWidth(mTextPaint, "0") / 2 - space;

        switch (getChartType()) {
            case CHART_TYPE_PM25:
//                canvas.drawText("" + PM25_Y_VALUES[0], bottomTextLeft,
//                        getBaseLine(mTextPaint, stopY - (stopY - startY) / 5), mTextPaint);
                canvas.drawText("" + PM25_Y_VALUES[1], bottomTextLeft,
                        getBaseLine(mTextPaint, stopY - (stopY - startY) / 5 * 2), mTextPaint);
                canvas.drawText("" + PM25_Y_VALUES[2], bottomTextLeft,
                        getBaseLine(mTextPaint, stopY - (stopY - startY) / 5 * 4), mTextPaint);
//                canvas.drawText("" + PM25_Y_VALUES[3], bottomTextLeft,
//                        getBaseLine(mTextPaint, stopY - (stopY - startY) / 5 * 4), mTextPaint);
                break;

            case CHART_TYPE_TEMPERATURE:
//                canvas.drawText("" + TEMPERATURE_Y_VALUES[0], bottomTextLeft,
//                        getBaseLine(mTextPaint, stopY - (stopY - startY) / 5), mTextPaint);
                canvas.drawText("" + TEMPERATURE_Y_VALUES[1], bottomTextLeft,
                        getBaseLine(mTextPaint, stopY - (stopY - startY) / 5 * 2), mTextPaint);
//                canvas.drawText("" + TEMPERATURE_Y_VALUES[2], bottomTextLeft,
//                        getBaseLine(mTextPaint, stopY - (stopY - startY) / 5 * 3), mTextPaint);
                canvas.drawText("" + TEMPERATURE_Y_VALUES[2], bottomTextLeft,
                        getBaseLine(mTextPaint, stopY - (stopY - startY) / 5 * 4), mTextPaint);
                break;

            case CHART_TYPE_HUMIDITY:
//                canvas.drawText("" + HUMIDITY_Y_VALUES[0], bottomTextLeft,
//                        getBaseLine(mTextPaint, stopY - (stopY - startY) / 5), mTextPaint);
                canvas.drawText("" + HUMIDITY_Y_VALUES[1], bottomTextLeft,
                        getBaseLine(mTextPaint, stopY - (stopY - startY) / 5 * 2), mTextPaint);
//                canvas.drawText("" + HUMIDITY_Y_VALUES[2], bottomTextLeft,
//                        getBaseLine(mTextPaint, stopY - (stopY - startY) / 5 * 3), mTextPaint);
                canvas.drawText("" + HUMIDITY_Y_VALUES[2], bottomTextLeft,
                        getBaseLine(mTextPaint, stopY - (stopY - startY) / 5 * 4), mTextPaint);
                break;
            case CHART_TYPE_HCHO:
//                canvas.drawText("" + HUMIDITY_Y_VALUES[0], bottomTextLeft,
//                        getBaseLine(mTextPaint, stopY - (stopY - startY) / 5), mTextPaint);
                canvas.drawText("" + HOHC_Y_VALUES[1], bottomTextLeft,
                        getBaseLine(mTextPaint, stopY - (stopY - startY) / 5 * 2), mTextPaint);
//                canvas.drawText("" + HUMIDITY_Y_VALUES[2], bottomTextLeft,
//                        getBaseLine(mTextPaint, stopY - (stopY - startY) / 5 * 3), mTextPaint);
                canvas.drawText("" + HOHC_Y_VALUES[2], bottomTextLeft,
                        getBaseLine(mTextPaint, stopY - (stopY - startY) / 5 * 4), mTextPaint);
                break;
            case CHART_TYPE_CO2:
//                canvas.drawText("" + HUMIDITY_Y_VALUES[0], bottomTextLeft,
//                        getBaseLine(mTextPaint, stopY - (stopY - startY) / 5), mTextPaint);
                canvas.drawText("" + CO2_Y_VALUES[1], bottomTextLeft,
                        getBaseLine(mTextPaint, stopY - (stopY - startY) / 5 * 2), mTextPaint);
//                canvas.drawText("" + HUMIDITY_Y_VALUES[2], bottomTextLeft,
//                        getBaseLine(mTextPaint, stopY - (stopY - startY) / 5 * 3), mTextPaint);
                canvas.drawText("" + CO2_Y_VALUES[2], bottomTextLeft,
                        getBaseLine(mTextPaint, stopY - (stopY - startY) / 5 * 4), mTextPaint);
                break;
            case CHART_TYPE_TVOC:
//                canvas.drawText("" + HUMIDITY_Y_VALUES[0], bottomTextLeft,
//                        getBaseLine(mTextPaint, stopY - (stopY - startY) / 5), mTextPaint);
                canvas.drawText("" + TVOC_Y_VALUES[1], bottomTextLeft,
                        getBaseLine(mTextPaint, stopY - (stopY - startY) / 5 * 2), mTextPaint);
//                canvas.drawText("" + HUMIDITY_Y_VALUES[2], bottomTextLeft,
//                        getBaseLine(mTextPaint, stopY - (stopY - startY) / 5 * 3), mTextPaint);
                canvas.drawText("" + TVOC_Y_VALUES[2], bottomTextLeft,
                        getBaseLine(mTextPaint, stopY - (stopY - startY) / 5 * 4), mTextPaint);
                break;


            default:
                break;
        }

        //绘制纵坐标轴
        Path path = new Path();
        path.moveTo(start + 20, getBaseLine(mTextPaint, stopY));
        path.lineTo(start + 20, getBaseLine(mTextPaint, -20));
        canvas.drawPath(path, mDottedLinePaint);
        canvas.save();
        canvas.restore();
    }

    private void initPaints(Context context) {
        gestureDetector = new GestureDetector(context, new MyGestureListener(), null);
        //解决长按屏幕之后无法拖动的现象
        gestureDetector.setIsLongpressEnabled(false);
        this.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        //初始化线的画笔
        mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinePaint.setStyle(Paint.Style.FILL);
        mLinePaint.setColor(0xffcdcdcd);
        mLinePaint.setStrokeWidth(2);

        mIndoorLinePaint = new Paint();
        mIndoorLinePaint.setStyle(Paint.Style.STROKE);
        mIndoorLinePaint.setStrokeWidth(15);
        //抗锯齿
        mIndoorLinePaint.setAntiAlias(true);
        mIndoorLinePaint.setFilterBitmap(true);

        mDottedLinePaint = new Paint();
        mDottedLinePaint.reset();
        mDottedLinePaint.setStyle(Paint.Style.STROKE);
        mDottedLinePaint.setColor(Color.LTGRAY);
        mDottedLinePaint.setAntiAlias(true);
        mDottedLinePaint.setStrokeWidth(5);
        mDottedLinePaint.setPathEffect(new DashPathEffect(new float[]{5, 5}, 0));

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextSize(40);
        mTextPaint.setStrokeWidth(1);
        mTextPaint.setColor(context.getResources().getColor(R.color.toolbar_title_text_color));
        mTextPaint.setTextAlign(Paint.Align.CENTER);

        mTextPaintBottom = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaintBottom.setTextSize(40);
        mTextPaintBottom.setStrokeWidth(1);
        mTextPaintBottom.setColor(context.getResources().getColor(R.color.toolbar_title_text_color));
        mTextPaintBottom.setTextAlign(Paint.Align.CENTER);

        switch (getChartType()) {
            case CHART_TYPE_PM25:
                mIndoorLinePaint.setColor(context.getResources().getColor(R.color.chart_green_color));
                mDottedLinePaint.setColor(context.getResources().getColor(R.color.chart_green_color));
                indoorGradientColor = new int[]{context.getResources().getColor(R.color.chart_green_color),
                        context.getResources().getColor(R.color.chart_green_color_transparent)};
                break;

            case CHART_TYPE_TEMPERATURE:
                mIndoorLinePaint.setColor(context.getResources().getColor(R.color.chart_yellow_color));
                mDottedLinePaint.setColor(context.getResources().getColor(R.color.chart_yellow_color));
                indoorGradientColor = new int[]{context.getResources().getColor(R.color.chart_yellow_color),
                        context.getResources().getColor(R.color.chart_yellow_color_transparent)};
                break;

            case CHART_TYPE_HUMIDITY:
                mIndoorLinePaint.setColor(context.getResources().getColor(R.color.chart_blue_color));
                mDottedLinePaint.setColor(context.getResources().getColor(R.color.chart_blue_color));
                indoorGradientColor = new int[]{context.getResources().getColor(R.color.chart_blue_color),
                        context.getResources().getColor(R.color.chart_blue_color_transparent)};
                break;
            case CHART_TYPE_HCHO:
                mIndoorLinePaint.setColor(context.getResources().getColor(R.color.chart_blue_color));
                mDottedLinePaint.setColor(context.getResources().getColor(R.color.chart_blue_color));
                indoorGradientColor = new int[]{context.getResources().getColor(R.color.chart_blue_color),
                        context.getResources().getColor(R.color.chart_blue_color_transparent)};
                break;
            case CHART_TYPE_CO2:
                mIndoorLinePaint.setColor(context.getResources().getColor(R.color.chart_blue_color));
                mDottedLinePaint.setColor(context.getResources().getColor(R.color.chart_blue_color));
                indoorGradientColor = new int[]{context.getResources().getColor(R.color.chart_blue_color),
                        context.getResources().getColor(R.color.chart_blue_color_transparent)};
                break;
            case CHART_TYPE_TVOC:
                mIndoorLinePaint.setColor(context.getResources().getColor(R.color.chart_blue_color));
                mDottedLinePaint.setColor(context.getResources().getColor(R.color.chart_blue_color));
                indoorGradientColor = new int[]{context.getResources().getColor(R.color.chart_blue_color),
                        context.getResources().getColor(R.color.chart_blue_color_transparent)};
                break;

            default:
                break;
        }

        //设开始X坐标 默认可以显示4位数的长度
//        startX = getTextWidth(mTextPaint, "1000") + space;
        startX = 0;
        //设开始Y坐标为20
        startY = 20;
    }


    /**
     * 获取文字的宽度
     */
    public int getTextWidth(Paint paint, String text) {
        float textLength = paint.measureText(text);
        return (int) textLength;
    }

    /**
     * 获取文字的底部线
     */
    public float getBaseLine(Paint textPaint, int targetY) {
        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
        float textBaseY = targetY + (fontMetrics.descent - fontMetrics.ascent) / 2 - fontMetrics.descent;
        return textBaseY;
    }

    /**
     * 设置最大值
     *
     * @param max
     */
    public void setYMax(float max) {
        this.mYMax = max;
//        startX = getTextWidth(mTextPaint, "" + (int) this.mYMax) + space;
//        this.invalidate();
    }

    /**
     * 设置一共有几个柱状图
     *
     * @param totalNum
     */
    public void setTotalXNum(int totalNum) {
        this.mTotalXNum = totalNum;
        currentBarProgress = new Float[totalNum];
        for (int i = 0; i < totalNum; i++) {
            currentBarProgress[i] = 0.0f;
        }

        this.invalidate();
    }

    //时间跟着移动偏移量
    public void setXOffset(int index) {
        mXOffset = index;
    }

    /**
     * 分别设置每个柱状图的目标值
     *
     * @param respTarget
     */
    public void setYAxisValue(List<Float> respTarget) {
        this.mYValue = respTarget;
    }

    /**
     * 分别设置每个柱状图的名字
     *
     * @param respName
     */
    public void setXAxisValue(ArrayList<String> respName) {
        this.mXValue = respName;
    }

    /**
     * 设置最大值得单位比例，如果是mg就为1，g就为1000
     *
     * @param mMaxScale
     */
    public void setMaxScale(int mMaxScale) {
        this.mMaxScale = mMaxScale;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //手势监听的返回值为false,由于我们在重写手势监听的listener的都返回为false,在该方法
        //最后返回true,是因为我们已经把事件的处理交给手势监听了，如果返回false，则view自己还得处理一次
        //当值为false，有些滚动，快速滑动的事件就会检测不到了
        if(mOnTouchCallback==null){
            return true;
        }
        mOnTouchCallback.onTouch();
        boolean resume = gestureDetector.onTouchEvent(event);
        return true;
    }

    class MyGestureListener implements GestureDetector.OnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            return false;
        }

        @Override
        public void onShowPress(MotionEvent e) {
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            int index = moveIndex((int) distanceX);
            mScrollCallback.onScroll(index);
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }

    }

    private int moveIndex(int distance) {
        int width = stopX - startX;
        int index = distance * 30 / width;
        return index;
    }


    /**
     * 计算控制点
     * @param points
     * @param result
     * @param index1
     * @param index2
     * @param multiplier
     */
    private void calc(List<Float> points, Point result, int index1, int index2, final float multiplier) {
        float p1x = points.get(index1);
        float p1y = points.get(index1 + 1);
        float p2x = points.get(index2);
        float p2y = points.get(index2 + 1);

        float diffX = p2x - p1x;
        float diffY = p2y - p1y;
        result.setX(p1x + (diffX * multiplier));
        result.setY(p1y + (diffY * multiplier));
    }

    /**
     * 重新设置点的位置，为曲线上的位置
     * @param mPathMeasure
     * @param pointsList
     */
    public void reSetPointWithPath(PathMeasure mPathMeasure, List<Float> pointsList){
        int length = (int) mPathMeasure.getLength();
        int pointsLength = pointsList.size();
        float[] coords = new float[2];
        for (int b = 0; b < length; b++) {
            mPathMeasure.getPosTan(b, coords, null);
            double prevDiff = Double.MAX_VALUE;
            boolean ok = true;
            for (int j = 0; j < pointsLength && ok; j += 2) {
                double diff = Math.abs(pointsList.get(j) - coords[0]);
                if (diff < 1) {
                    pointsList.set(j + 1, coords[1]);
                    prevDiff = diff;
                }
                ok = prevDiff > diff;
            }
        }
    }


}
