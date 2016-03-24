package com.ui.trainer.view;

import android.animation.Animator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;

import com.ui.trainer.DataAnimation;
import com.ui.trainer.DataAnimationListener;
import com.ui.trainer.R;

/**
 * @author LiYanZhao
 * @date 16-3-21 下午4:02
 */
public class FloatingActionButton extends View implements DataAnimationListener {
    private Paint bgPaint;
    private Paint contentPaint;
    int centerX = 0;
    int centerY = 0;
    int width = 0;
    int height = 0;

    private final int DURATION = 600;


    public static final int CIRCLE_TO_ROUND_RECT = 0; //圆形状态切换为圆角矩形
    public static final int ROUND_RECT_TO_CIRCLE = 1; //圆角矩形切换为圆形

    private float originRadius; //原始半径
    private float radius; //当前半径
    private float roundRectWidth = 0; //长度（不含弧度半径）
    private float diffRadius; //弧度半径差值
    private float diffWidth; //长度差值
    private float offsetX = Float.NaN;  //在圆角矩形切换圆形状态时X坐标偏移量
    private int marginRight = 0;

    private float originArrowHeight = Float.NaN; //原箭头高度
    private float arrowHeight = Float.NaN; //现箭头高度
    private float diffArrowHeight = Float.NaN; //箭头高度差
    private float arrowDegree = 45f;
    private float originDegree = 45f; //原角度
    private float diffDegree = 135f; //原角度

//    private float originUpheight = Float.NaN; //箭头 转 X 上部分原高度
//    private float originDownheight = Float.NaN; //箭头 转 X 下部分原高度
//    private float originOffsetCenter = Float.NaN;//原中心偏移量

    private float diffUpHeight = Float.NaN; // 箭头 转 X 上部分高度差
    private float diffDownHeight = Float.NaN; // 箭头 转 X 下部分高度差
    private float diffOffsetCenter = Float.NaN; // 箭头 转 X 中心偏移量差

    private float addHalfHeight = Float.NaN; // 加号半边长度

    private float arrowUpHeight = Float.NaN; // 箭头上部分长度
    private float arrowDownHeight = Float.NaN; //箭头下部分长度
    private float offsetCenter = Float.NaN; //中心偏移量

    private String text = "";
    private float textWidth;
    private float textSize = dip2px(16); //默认字体大小

    private final float ARROW_RADIUS = dip2px(2);
    private final float ARROW_WIDTH = dip2px(2.5f);



    private int type = 0;

    private DataAnimation mDataAnimation;
    private int screenWidth = 0;

    public FloatingActionButton(Context context) {
        super(context);
        init(null);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public FloatingActionButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    public FloatingActionButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    public FloatingActionButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(width, height);
    }

    @SuppressWarnings("ResourceType")
    public void init(AttributeSet attrs){
        bgPaint = new Paint();
        bgPaint.setColor(getResources().getColor(R.color.jordy_blue));
        bgPaint.setAntiAlias(true);

        contentPaint = new Paint();
        contentPaint.setColor(Color.WHITE);
        contentPaint.setAntiAlias(true);

        radius = originRadius = getResources().getDimensionPixelSize(R.dimen.design_fab_size_normal) / 2;
        diffRadius = originRadius / 4;
        diffWidth = 5 * originRadius;

        mDataAnimation = new DataAnimation();
        mDataAnimation.setDataAnimationListener(this);

        height = width = (int) (radius * 2);

        arrowHeight = originArrowHeight = radius / 3 * 2; //箭头初始长度
        if(Float.isNaN(arrowUpHeight)){
            arrowUpHeight = arrowHeight / 3 * 2;
            arrowDownHeight = arrowHeight / 3;
            offsetCenter = arrowHeight / 3 - ARROW_WIDTH / 2;
        }
        float minArrowHeight = originArrowHeight / 2; //箭头最小长度
        diffArrowHeight = originArrowHeight - minArrowHeight;

        addHalfHeight = radius / 2;

        marginRight = this.getContext().getResources().getDimensionPixelOffset(R.dimen.floating_margin_right);
        if(attrs != null){
            int[] attrsId = new int[]{android.R.attr.layout_marginRight,android.R.attr.text,android.R.attr.textSize};
            TypedArray ta = this.getContext().obtainStyledAttributes(attrs,attrsId);
            marginRight = ta.getDimensionPixelOffset(0,marginRight);
            textSize = ta.getDimensionPixelSize(2, (int) textSize);
            text = ta.getString(1);
            ta.recycle();
        }

        WindowManager wm = (WindowManager) this.getContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);
        screenWidth = metrics.widthPixels;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        centerX = w /2;
        centerY = h /2;

        if(Float.isNaN(offsetX)){
            offsetX = screenWidth - getLeft() - marginRight - originRadius * 2;
        }
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        //绘制背景
        RectF rect = new RectF(centerX - radius - roundRectWidth / 2,centerY - radius,centerX + radius + roundRectWidth / 2,centerY + radius);
        canvas.drawRoundRect(rect,radius,radius,bgPaint);

        drawText(canvas);

        drawIcon(canvas);
    }

    /**
     * 绘制箭头图标及十字图标
     * @param canvas
     */
    public void drawIcon(Canvas canvas){
        float arrowCenterX = centerX + roundRectWidth / 2;
        canvas.rotate(arrowDegree,arrowCenterX,centerY);

//        float arrowUpHeight = arrowHeight / 3 * 2; // 箭头上部分长度
//        float arrowDownHeight = arrowHeight / 3; //箭头下部分长度
//        float offsetCenter = arrowHeight / 3 - ARROW_WIDTH / 2; //中心偏移量

        RectF leftRect = new RectF(arrowCenterX - arrowUpHeight,centerY + offsetCenter - ARROW_WIDTH / 2,arrowCenterX + arrowDownHeight,centerY + offsetCenter + ARROW_WIDTH / 2);
        canvas.drawRoundRect(leftRect,ARROW_RADIUS,ARROW_RADIUS,contentPaint);


        RectF rightRect = new RectF(arrowCenterX + offsetCenter - ARROW_WIDTH / 2,centerY - arrowUpHeight,arrowCenterX + offsetCenter + ARROW_WIDTH / 2,centerY + arrowDownHeight);
        canvas.drawRoundRect(rightRect,ARROW_RADIUS,ARROW_RADIUS,contentPaint);

        canvas.restore();
    }

    /**
     * 绘制文字
     * @param canvas
     */
    public void drawText(Canvas canvas){
        contentPaint.setTextSize(textSize);
        textWidth = contentPaint.measureText(text);
        float textLeft;
        if(type == CIRCLE_TO_ROUND_RECT){
            textLeft = centerX - textWidth / 2 - diffWidth /2 + roundRectWidth /2;
        }else{
            textLeft = centerX - textWidth / 2;
        }
        canvas.drawText(text,textLeft,centerY - ((contentPaint.descent() + contentPaint.ascent()) / 2),contentPaint);
        canvas.save();
    }


    public void switchState(int type){
        switchState(type,DURATION);
    }

    public void switchState(int type,int duration){
        this.type = type;
        mDataAnimation.setInterpolator(new DecelerateInterpolator());
        mDataAnimation.startAnimation(duration);
    }

    @Override
    public void onAnimationUpdate(float scale) {
        if(this.type == CIRCLE_TO_ROUND_RECT){
            radius = originRadius - diffRadius * scale;
            roundRectWidth = diffWidth * scale;

            //计算箭头数据
            arrowHeight = originArrowHeight - scale * diffArrowHeight;
            arrowUpHeight = arrowHeight / 3 * 2;
            arrowDownHeight = arrowHeight / 3;
            offsetCenter = arrowHeight / 3 - ARROW_WIDTH / 2;
        }else{
            textSize = (1 - scale) * textSize;

            radius = originRadius - (1f - scale) * diffRadius;
            roundRectWidth = (1f - scale) * diffWidth;
            setTranslationX(scale * offsetX);

            if(Float.isNaN(diffUpHeight)){
                diffUpHeight = addHalfHeight - arrowUpHeight;
                diffDownHeight = addHalfHeight - arrowDownHeight;
                diffOffsetCenter = offsetCenter;
            }

            arrowUpHeight = addHalfHeight - (1 - scale) * diffUpHeight;
            arrowDownHeight = addHalfHeight - (1 - scale) * diffDownHeight;
            offsetCenter = (1 - scale) * diffOffsetCenter;
            arrowDegree = originDegree + scale * diffDegree;
        }

        width = (int) (radius * 2 + roundRectWidth);
        height = (int) (radius * 2);

        requestLayout(); //强制measure
       // ViewCompat.postInvalidateOnAnimation(this);
    }

    @Override
    public void onAnimationFinish() {

    }

    @Override
    public void onAnimationStart(Animator animation) {

    }

    public float dip2px(float dipValue){
        DisplayMetrics metrics = this.getContext().getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, metrics);
    }
}
