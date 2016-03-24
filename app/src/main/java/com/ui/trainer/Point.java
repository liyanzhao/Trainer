package com.ui.trainer;

/**
 * @author LiYanZhao
 * @date 16-3-19 上午10:51
 */
public class Point {

    private float x;
    private float y;
    private float originX;
    private float originY;
    private float diffX;
    private float diffY;
    private char[] label;

    public Point() {
        set(0, 0);
    }

    public Point(float x, float y) {
        set(x, y);
    }

    public Point(Point Point) {
        set(Point.x, Point.y);
        this.label = Point.label;
    }

    public void update(float scale) {
        x = originX + diffX * scale;
        y = originY + diffY * scale;
    }

    public void finish() {
        set(originX + diffX, originY + diffY);
    }

    public Point set(float x, float y) {
        this.x = x;
        this.y = y;
        this.originX = x;
        this.originY = y;
        this.diffX = 0;
        this.diffY = 0;
        return this;
    }

    public Point setTarget(float targetX, float targetY) {
        set(x, y);
        this.diffX = targetX - originX;
        this.diffY = targetY - originY;
        return this;
    }

    public float getX() {
        return this.x;
    }

    public float getY() {
        return this.y;
    }

}
