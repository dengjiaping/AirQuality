/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.honeywell.Zxing.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import com.google.zxing.ResultPoint;
import com.honeywell.Zxing.camera.CameraManager;
import com.honeywell.iaq.R;

/**
 * This view is overlaid on top of the camera preview. It adds the viewfinder rectangle and partial
 * transparency outside it, as well as the laser scanner animation and result points.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class ViewfinderView extends View {

    private static final long ANIMATION_DELAY = 1L;
    private int ScreenRate;
    private final Paint outSidePaint;
    private final Paint cornerPaint;
    private final Paint insidePaint;
    private final Paint linePaint;
    private static float density;
    private static final int CORNER_WIDTH = 5;
    private int slideTop;
    private boolean isFromTop = true;
    boolean isFirst;
    private static final int SPEEN_DISTANCE = 12;
    private static final int MIDDLE_LINE_WIDTH = 5;
    private static final int TEXT_PADDING_TOP = 30;
    private static final int TEXT_SIZE = 13;
    private static final int MIDDLE_LINE_PADDING = 5;


    public ViewfinderView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Initialize these once for performance rather than calling them every time in onDraw().
        outSidePaint = new Paint();
        cornerPaint = new Paint();
        insidePaint = new Paint();
        linePaint = new Paint();
        linePaint.setColor(getResources().getColor(R.color.pale_green));
        linePaint.setStrokeWidth((float) 3.0);
        outSidePaint.setColor(getResources().getColor(R.color.transparent));
        cornerPaint.setColor(getResources().getColor(R.color.white));

        density = context.getResources().getDisplayMetrics().density;
        ScreenRate = (int) (23 * density);
    }

    @Override
    public void onDraw(Canvas canvas) {
        Rect frame = CameraManager.get().getFramingRect();
        if (frame == null) {
            return;
        }
        if (!isFirst) {
            isFirst = true;
            slideTop = frame.top;
        }
        int width = canvas.getWidth();
        int height = canvas.getHeight();

//     Draw the exterior (i.e. outside the framing rect) darkened

        canvas.drawRect(0, 0, width, frame.top, outSidePaint);
        canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, outSidePaint);
        canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1, outSidePaint);
        canvas.drawRect(0, frame.bottom + 1, width, height, outSidePaint);


//        draw inside frame
        Shader mShader = new LinearGradient(frame.left, frame.top, frame.left, frame.bottom, new int[]{getResources().getColor(R.color.transparent), getResources().getColor(R.color.transparent_20)}, null, Shader.TileMode.REPEAT);
//新建一个线性渐变，前两个参数是渐变开始的点坐标，第三四个参数是渐变结束的点的坐标。连接这2个点就拉出一条渐变线了，玩过PS的都懂。然后那个数组是渐变的颜色。下一个参数是渐变颜色的分布，如果为空，每个颜色就是均匀分布的。最后是模式，这里设置的是循环渐变
        insidePaint.setShader(mShader);
        canvas.drawRect(frame.left, frame.top, frame.right, frame.bottom, insidePaint);
//        draw four corner
        canvas.drawRect(frame.left, frame.top, frame.left + ScreenRate, frame.top + CORNER_WIDTH, cornerPaint);
        canvas.drawRect(frame.left, frame.top, frame.left + CORNER_WIDTH, frame.top + ScreenRate, cornerPaint);
        canvas.drawRect(frame.right - ScreenRate, frame.top, frame.right, frame.top + CORNER_WIDTH, cornerPaint);
        canvas.drawRect(frame.right - CORNER_WIDTH, frame.top, frame.right, frame.top + ScreenRate, cornerPaint);
        canvas.drawRect(frame.left, frame.bottom - CORNER_WIDTH, frame.left + ScreenRate, frame.bottom, cornerPaint);
        canvas.drawRect(frame.left, frame.bottom - ScreenRate, frame.left + CORNER_WIDTH, frame.bottom, cornerPaint);
        canvas.drawRect(frame.right - ScreenRate, frame.bottom - CORNER_WIDTH, frame.right, frame.bottom, cornerPaint);
        canvas.drawRect(frame.right - CORNER_WIDTH, frame.bottom - ScreenRate, frame.right, frame.bottom, cornerPaint);

        //draw textview
        cornerPaint.setTextSize(TEXT_SIZE * density);
        cornerPaint.setTextAlign(Paint.Align.CENTER);

//        canvas.drawText(getResources().getString(R.string.scan), frame.centerX(), (float) (frame.bottom + (float) TEXT_PADDING_TOP * density), cornerPaint);

        //draw green line
        canvas.drawRect(frame.left + MIDDLE_LINE_PADDING, slideTop - MIDDLE_LINE_WIDTH / 2, frame.right - MIDDLE_LINE_PADDING, slideTop + MIDDLE_LINE_WIDTH / 2, linePaint);
        if (slideTop >= frame.bottom) {
            isFromTop = false;
        } else if (slideTop <= frame.top) {
            isFromTop = true;
        }
        if (isFromTop) {
            slideTop += SPEEN_DISTANCE;
        } else {
            slideTop -= SPEEN_DISTANCE;
        }

        // Request another update at the animation interval, but only repaint the laser line,
        // not the entire viewfinder mask.
        postInvalidateDelayed(ANIMATION_DELAY, frame.left, frame.top, frame.right, frame.bottom);
    }

    public void drawViewfinder() {
        invalidate();
    }

    public void addPossibleResultPoint(ResultPoint point) {
    }

}
