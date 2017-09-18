package com.ruilonglai.texas_scan.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by wangshuai on 2017/5/16.
 */

public class CardTableView extends View {

    private int width;//一个子项的宽

    public CardTableView(Context context) {
        this(context, null);
        getImageWidth();
    }

    public CardTableView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    public void initImages() {
        for (int i = 1; i <= 13; i++) {
            for (int j = 1; j <= 13; j++) {

            }
        }
    }

    public void changeCard(int card) {

    }

    private void getImageWidth() {
        // 尺子
        DisplayMetrics metrics = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay()
                .getMetrics(metrics);
        // 把宽分成10份，
        width = metrics.widthPixels / 13;
    }
}
