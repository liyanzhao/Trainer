package com.ui.trainer;

import android.animation.Animator;

/**
 * @author LiYanZhao
 * @date 16-3-19 下午5:43
 */
public interface DataAnimationListener {

    public void onAnimationUpdate(float scale);

    public void onAnimationFinish();

    void onAnimationStart(Animator animation);
}
