package com.honeywell.Zxing.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import com.honeywell.Zxing.camera.CameraManager;
import com.honeywell.iaq.R;


/**
 * Created by Vincent on 7/12/15.
 */
public class ViewWhiteBackGround extends View {
    private int slideTop;
    private int slideBottom;
    private int slideLeft;
    private int slideRight;
    boolean isFirst;
    private final Paint paint;
    private Point mPoint;


    public ViewWhiteBackGround(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        paint.setColor(context.getResources().getColor(R.color.white));
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
            slideBottom = frame.bottom;
            slideLeft = frame.left;
            slideRight = frame.right;
            mPoint = CameraManager.get().getScreenPoint();
        }
        canvas.drawRect(0, 0, slideLeft, mPoint.y, paint);
        canvas.drawRect(slideLeft, 0, slideRight, slideTop, paint);
        canvas.drawRect(slideLeft, slideBottom, slideRight, mPoint.y, paint);
        canvas.drawRect(slideRight, 0, mPoint.x, mPoint.y, paint);
    }
}
