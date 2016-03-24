package com.ui.trainer.view;

import android.animation.Animator;
import android.animation.TimeInterpolator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;

import com.ui.trainer.DataAnimation;
import com.ui.trainer.DataAnimationListener;
import com.ui.trainer.Point;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LiYanZhao
 * @date 16-3-19 上午10:50
 */
public class BezierView extends View implements DataAnimationListener {
    public static final int TYPE_ONE = 1;
    public static final int TYPE_SECOND = 2;
    public static final int TYPE_THIRD = 3;

    public  List<Point> line = new ArrayList<>();
    private Paint paint = new Paint();
    private Path path = new Path();
    private DataAnimation dataAnimation;
    public static final int ANIMATION_DURATION = 400;
    private int screenHeight;
    float screenWidth = 780f;
    public static  final float RANGE = 200f;

    private int lineColor = Color.parseColor("#E1E8F8");

    private static final float LINE_SMOOTHNESS = 0.16f;

    private float baseLine = 0; //横向基准线

    private DataAnimationListener mDataAnimationListener;


    public BezierView(Context context) {
        super(context);
        init();
    }

    public BezierView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BezierView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public BezierView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public void init(){
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(3);
        paint.setColor(lineColor);

        dataAnimation = new DataAnimation();
        dataAnimation.setDataAnimationListener(this);

        WindowManager wm = (WindowManager) this.getContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);
        screenHeight = metrics.heightPixels;
        screenWidth = metrics.widthPixels;

        line = getLevelLine();
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        drawBesizer(canvas);
    }

    public void drawBesizer(Canvas canvas){
        float prePreviousPointX = Float.NaN;
        float prePreviousPointY = Float.NaN;
        float previousPointX = Float.NaN;
        float previousPointY = Float.NaN;
        float currentPointX = Float.NaN;
        float currentPointY = Float.NaN;
        float nextPointX = Float.NaN;
        float nextPointY = Float.NaN;

        for(int index = 0; index < line.size(); index++){
            if(Float.isNaN(currentPointX)){
                Point point = line.get(index);
                currentPointX = point.getX();
                currentPointY = point.getY() + baseLine;
            }

            if(Float.isNaN(previousPointX)){
                if(index > 0){
                    Point point = line.get(index - 1);
                    previousPointX = point.getX();
                    previousPointY = point.getY() + baseLine;
                }else{
                    previousPointX = currentPointX;
                    previousPointY = currentPointY;
                }
            }

            if(Float.isNaN(prePreviousPointX)){
                if(index > 1){
                    Point point = line.get(index - 2);
                    prePreviousPointX = point.getX();
                    prePreviousPointY = point.getY() + baseLine;
                }else{
                    prePreviousPointX = previousPointX;
                    prePreviousPointY = previousPointY;
                }
            }

            if(index < line.size() - 1){
                Point point = line.get(index + 1);
                nextPointX = point.getX();
                nextPointY = point.getY() + baseLine;
            }else{
                nextPointX = currentPointX;
                nextPointY = currentPointY;
            }

            if(index == 0){
                path.moveTo(currentPointX,currentPointY);
            }else{
                final float firstDiffX = (currentPointX - prePreviousPointX);
                final float firstDiffY = (currentPointY - prePreviousPointY);
                final float secondDiffX = (nextPointX - previousPointX);
                final float secondDiffY = (nextPointY - previousPointY);
                final float firstControlPointX = previousPointX + (LINE_SMOOTHNESS * firstDiffX);
                final float firstControlPointY = previousPointY + (LINE_SMOOTHNESS * firstDiffY);
                final float secondControlPointX = currentPointX - (LINE_SMOOTHNESS * secondDiffX);
                final float secondControlPointY = currentPointY - (LINE_SMOOTHNESS * secondDiffY);
                path.cubicTo(firstControlPointX, firstControlPointY, secondControlPointX, secondControlPointY,
                        currentPointX, currentPointY);
            }

            prePreviousPointX = previousPointX;
            prePreviousPointY = previousPointY;
            previousPointX = currentPointX;
            previousPointY = currentPointY;
            currentPointX = nextPointX;
            currentPointY = nextPointY;
        }
        canvas.drawPath(path, paint);

        path.lineTo(line.get(line.size() - 1).getX(), screenHeight);
        path.lineTo(line.get(0).getX(), screenHeight);
        path.close();
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        //paint.setAlpha(64);
        canvas.drawPath(path, paint);
        paint.setStyle(Paint.Style.STROKE);

        path.reset();
    }


    public void switchLine(int type){
        switchLine(type,ANIMATION_DURATION);
    }

    /**
     * 切换曲线
     */
    public void switchLine(int type,int duration){
        List<Point> newLine;
        switch (type){
            case TYPE_SECOND:
                newLine = getBottomLine();
                break;
            case TYPE_THIRD:
                newLine = getUpLine();
                break;
            default:
                newLine = getLevelLine();
                break;
        }

        if(newLine.size() != line.size()){
            line = newLine;
            ViewCompat.postInvalidateOnAnimation(this);
            return;
        }

        for(int i = 0; i < newLine.size(); i++){
            Point point = newLine.get(i);
            line.get(i).setTarget(point.getX(),point.getY());
        }
        dataAnimation.startAnimation(duration);
    }

    public void setInterpolator(TimeInterpolator interpolator){
        dataAnimation.setInterpolator(interpolator);
    }

    public void setStartDelay(long startDelay){
        dataAnimation.setStartDelay(startDelay);
    }

    private List<Point> getBottomLine(){
        List<Point> line = new ArrayList<>();
        line.add(new Point(0.0f,RANGE / 5));
        line.add(new Point(screenWidth /4,0f));
        line.add(new Point(screenWidth /2,RANGE / 2));
        line.add(new Point(screenWidth /4*3,RANGE));
        line.add(new Point(screenWidth,RANGE / 5 * 4 ));
        return line;
    }

    private List<Point> getUpLine(){
        List<Point> line = new ArrayList<>();
        line.add(new Point(0.0f,0f));
        line.add(new Point(screenWidth /4,RANGE));
        line.add(new Point(screenWidth /8 * 5,RANGE/3*2));
        line.add(new Point(screenWidth /10*8.5f,RANGE / 10 * 9));
        line.add(new Point(screenWidth,RANGE / 5 * 3));
        return line;
    }

    private List<Point> getLevelLine(){
        List<Point> line = new ArrayList<>();
        line.add(new Point(0.0f,0f));
        line.add(new Point(screenWidth /4,0f));
        line.add(new Point(screenWidth /8 * 5,0f));
        line.add(new Point(screenWidth /10*8.5f,0f));
        line.add(new Point(screenWidth,0f));
        return line;
    }


    @Override
    public void onAnimationUpdate(float scale) {
        for(Point point : line){
            point.update(scale);
        }

        ViewCompat.postInvalidateOnAnimation(this);
    }

    @Override
    public void onAnimationFinish() {
        for(Point point : line){
            point.finish();
        }
        ViewCompat.postInvalidateOnAnimation(this);
        if(mDataAnimationListener != null){
            mDataAnimationListener.onAnimationFinish();
        }
    }

    @Override
    public void onAnimationStart(Animator animation) {
        if(mDataAnimationListener != null){
            mDataAnimationListener.onAnimationStart(animation);
        }
    }

    public float getBaseLine() {
        return baseLine;
    }

    public void setBaseLine(float baseLine) {
        this.baseLine = baseLine;
        ViewCompat.postInvalidateOnAnimation(this);
    }

    public DataAnimationListener getDataAnimationListener() {
        return mDataAnimationListener;
    }

    public void setDataAnimationListener(DataAnimationListener mDataAnimationListener) {
        this.mDataAnimationListener = mDataAnimationListener;
    }
}
