package com.honeywell.iaq.chart.model;

/**
 * Created by Qian Jin on 2/16/17.
 */

public class Point {

    private float mX;
    private float mY;

    public Point() {
    }

    public Point(float x, float y) {
        mX = x;
        mY = y;
    }

    public float getX() {
        return mX;
    }

    public float getY() {
        return mY;
    }

    public void setX(float x) {
        mX = x;
    }

    public void setY(float y) {
        mY = y;
    }

    public void clearPoint(){

    }
}
