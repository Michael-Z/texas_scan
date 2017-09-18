package com.ruilonglai.texas_scan.view;


import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;

import com.nineoldandroids.animation.ValueAnimator;

public class RippleDrawable extends Drawable implements Animatable {

    //需要的画笔
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    //画笔的颜色
    private int bg_color = Color.RED;
    //目标view的宽度
    private int mViewWidth;
    //目标view的高度
    private int mViewHeight;
    //扩散的距离
    private int mFullSpace;
    //控制的动画
    private ValueAnimator mValueAnimator;
    //半径
    private int mRadius;

    /**
     * 必须重写画drawable的方法
     * @param canvas
     */
    @Override
    public void draw(Canvas canvas) {
        mPaint.setColor(bg_color);
        mPaint.setAlpha(100);
        canvas.drawCircle(mViewWidth / 2, mViewHeight / 2, mRadius, mPaint);
    }

    /**
     * 设置透明度
     */
    @Override
    public void setAlpha(int alpha) {

    }
    /**
     * 设置颜色过滤
     */
    @Override
    public void setColorFilter(ColorFilter colorFilter) {

    }

    /**
     * 设置精度
     */
    @Override
    public int getOpacity() {
        return PixelFormat.UNKNOWN;
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        mViewWidth = Math.abs(bounds.width());
        mViewHeight = Math.abs(bounds.height());
        mFullSpace = (int) Math.sqrt((mViewWidth * mViewWidth + mViewHeight * mViewHeight));

        mValueAnimator = ValueAnimator.ofInt(0, mFullSpace);
        mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                //获取不断改变的半径并重绘drawable
                mRadius = (int) animation.getAnimatedValue();
                //重绘,会再次调用draw
                invalidateSelf();
            }
        });
        //设置动画持续的时间
        mValueAnimator.setDuration(1000);
        start();
    }

    /**
     * 动画开始
     */
    @Override
    public void start() {
        if (mValueAnimator != null) {
            mValueAnimator.start();
        }
    }

    /**
     * 动画结束
     */
    @Override
    public void stop() {
        if (mValueAnimator != null) {
            mValueAnimator.end();
        }
    }

    /**
     * 动画是否运行中
     */
    @Override
    public boolean isRunning() {
        return mValueAnimator != null && mValueAnimator.isRunning();
    }

}
