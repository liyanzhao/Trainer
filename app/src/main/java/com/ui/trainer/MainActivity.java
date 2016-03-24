package com.ui.trainer;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;

import com.ui.trainer.view.BezierView;
import com.ui.trainer.view.FloatingActionButton;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    int index = 1;
    private int screenHeight;
    private int screenWidth;

    private final int BEZIER_ENTER_DURATION = 1300;
    private final int BEZIER_EXIT_DURATION = 800;

    @OnClick({R.id.floating})
    public void onClick(View view) {
        if(index == 1){
            exit();
            index ++;
        }

    }

    @Bind(R.id.bezier)
    BezierView mBezierView;

    @Bind(R.id.floating)
    FloatingActionButton mFloatingActionButton;

    @Bind({R.id.card0, R.id.card1, R.id.card2, R.id.card3})
    List<ImageView> cards;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);
        screenHeight = metrics.heightPixels;
        screenWidth = metrics.widthPixels;

        this.getWindow().getDecorView().getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                enter();

                MainActivity.this.getWindow().getDecorView().getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }


    public void enter(){
        mBezierView.switchLine(BezierView.TYPE_SECOND);
        Animator animator = ObjectAnimator.ofFloat(mBezierView, "baseLine", -BezierView.RANGE, mFloatingActionButton.getY() - mFloatingActionButton.getMeasuredHeight() / 2);
        //animator.setStartDelay(BezierView.ANIMATION_DURATION);
        // animator.setInterpolator(new OvershootInterpolator());
        animator.setDuration(BEZIER_ENTER_DURATION);
        animator.start();

        Animator floatingAnimaor = ObjectAnimator.ofFloat(mFloatingActionButton, "translationY", 300f, 0f);
        floatingAnimaor.setStartDelay(BezierView.ANIMATION_DURATION);
        floatingAnimaor.setDuration(BEZIER_ENTER_DURATION);
        floatingAnimaor.setInterpolator(new OvershootInterpolator());
        floatingAnimaor.start();
        floatingAnimaor.addListener(new MyAnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mFloatingActionButton.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mFloatingActionButton.switchState(FloatingActionButton.CIRCLE_TO_ROUND_RECT);
            }
        });

        cardEnter();
    }

    public void exit(){
        mFloatingActionButton.switchState(FloatingActionButton.ROUND_RECT_TO_CIRCLE,BEZIER_EXIT_DURATION);
        cardExit();

        mBezierView.switchLine(BezierView.TYPE_THIRD);

        Animator animator = ObjectAnimator.ofFloat(mBezierView, "baseLine",mBezierView.getBaseLine(),0f);
        animator.setStartDelay(BezierView.ANIMATION_DURATION);
        // animator.setInterpolator(new OvershootInterpolator());
        animator.setDuration(BEZIER_EXIT_DURATION);
        animator.start();

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mBezierView.switchLine(BezierView.TYPE_ONE);
                    }
                });

            }
        },BEZIER_EXIT_DURATION + BezierView.ANIMATION_DURATION - 110);
    }

    public void cardEnter() {
        int delay = 100;
        for (final ImageView card : cards) {
            Animator animator = ObjectAnimator.ofFloat(card, "translationX", screenWidth - card.getLeft(), 0f);
            animator.setDuration(550);
            animator.setStartDelay(BezierView.ANIMATION_DURATION + delay * cards.indexOf(card));
            animator.start();
            animator.addListener(new MyAnimatorListener(){
                @Override
                public void onAnimationStart(Animator animation) {
                    card.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    public void cardExit() {
        int delay = 50;
        for (final ImageView card : cards) {
            Animator animator = ObjectAnimator.ofFloat(card, "translationY", 0, - (card.getY() + card.getMeasuredHeight()));
            animator.setDuration(300 + 150 * cards.indexOf(card));
            animator.setStartDelay(delay * cards.indexOf(card));
            animator.setInterpolator(new AccelerateInterpolator());
            animator.start();
        }
    }



    public class MyAnimatorListener implements Animator.AnimatorListener{

        @Override
        public void onAnimationStart(Animator animation) {

        }

        @Override
        public void onAnimationEnd(Animator animation) {

        }

        @Override
        public void onAnimationCancel(Animator animation) {

        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }
    }

}
