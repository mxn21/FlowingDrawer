package com.mxn.soul.flowingdrawer_core;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.OvershootInterpolator;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ValueAnimator;



public class FlowingView extends View {

    private Paint mPaint;
    private Status mStatus = Status.NONE;
    private Path mPath = new Path();
    private int currentPointX = 0;
    private int currentPointY = 0;
    private int topY;
    private int bottomY;
    private int downspeed = 1;
    private boolean isupping = false;

    private int autoUppingX;

    private boolean showContent = true;

    private MenuFragment mMenuFragment ;

    private double per = 0 ;

    private int rightMargin ;

    public enum Status {
        NONE,
        STATUS_SMOOTH_UP,
        STATUS_UP,
        STATUS_DOWN,
    }

    public FlowingView(Context context) {
        super(context);
        init();
    }

    public FlowingView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public FlowingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(getResources().getColor(R.color.paint_color));
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public FlowingView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBG(canvas);
    }

    public void show(float x, float y, Status status) {
        if (mStatus == Status.STATUS_SMOOTH_UP)
            return;
        mStatus = status;
        if (mStatus == Status.STATUS_UP) {
            currentPointX = (int) x;
            currentPointY = (int) y;
        } else if (mStatus == Status.STATUS_DOWN) {
            downspeed = (int) Math.abs(x - currentPointX);
            currentPointX = (int) x;
            currentPointY = (int) y;
        }
        invalidate();
    }

    public void downing() {
        final int w = getWidth();
        ValueAnimator valueAnimator = ValueAnimator.ofInt(w + 100, w - rightMargin);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                currentPointX = (int) animation.getAnimatedValue();
                float fraction = animation.getAnimatedFraction();
                autoUppingX = (int) (w - rightMargin * fraction);
                invalidate();
            }
        });
        valueAnimator.addListener(new FlowingAnimationListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                    isupping = false;
                    showContent = true;
            }
        });
        valueAnimator.setDuration(500);
        valueAnimator.setInterpolator(new OvershootInterpolator(4f));
        valueAnimator.start();
    }

    private void drawBG(Canvas canvas) {
        mPath.reset();
        switch (mStatus) {
            case NONE:
                break;
            case STATUS_SMOOTH_UP:
                mPath.moveTo(autoUppingX, 0);
                mPath.quadTo(currentPointX, currentPointY, autoUppingX, getHeight());
                mPath.lineTo(0, getHeight());
                mPath.lineTo(0, 0);
                mPath.lineTo(autoUppingX, 0);
                canvas.drawPath(mPath, mPaint);
                break;
            case STATUS_UP:
                if (isupping) return;
                if (currentPointY - getHeight() / 2 >= 0) {
                    topY = (int) (currentPointY - 1.5 * currentPointX * getHeight() / getWidth()) - currentPointY / 2 +
                            getHeight() / 4;
                    bottomY = (int) (currentPointY + 1.5 * currentPointX * getHeight() / getWidth());
                } else {
                    topY = (int) (currentPointY - 1.5 * currentPointX * getHeight() / getWidth());
                    bottomY = (int) (currentPointY + 1.5 * currentPointX * getHeight() / getWidth()) -
                            currentPointY / 2 + getHeight() / 4;
                }
                mPath.moveTo(getWidth() - currentPointX, topY);
                mPath.cubicTo(getWidth() - currentPointX, currentPointY / 4 + 3 * topY / 4, getWidth(), 3 * currentPointY / 4
                                + topY / 4,
                        getWidth()
                        , currentPointY);
                mPath.cubicTo(getWidth(), 5 * currentPointY / 4 - topY / 4, getWidth() - currentPointX,
                        7 * currentPointY / 4 - 3 * topY / 4, getWidth() - currentPointX, bottomY);
                mPath.lineTo(getWidth() - currentPointX, topY);
                canvas.drawPath(mPath, mPaint);
                break;
            case STATUS_DOWN:
                topY = topY - downspeed;
                bottomY = bottomY + downspeed;
                mPath.moveTo(getWidth() - currentPointX, topY);
                mPath.cubicTo(getWidth() - currentPointX, currentPointY / 4 + 3 * topY / 4, getWidth(), 3 * currentPointY / 4
                                + topY / 4,
                        getWidth()
                        , currentPointY);
                mPath.cubicTo(getWidth(), 5 * currentPointY / 4 - topY / 4, getWidth() - currentPointX,
                        7 * currentPointY / 4 - 3 * topY / 4, getWidth() - currentPointX, bottomY);
                mPath.lineTo(getWidth() - currentPointX, topY);
                canvas.drawPath(mPath, mPaint);
                break;
        }

    }


    public boolean isStartAuto(float x) {
        return x >= getWidth() / 2;
    }


    public void autoUpping(float x) {
        mStatus = Status.STATUS_SMOOTH_UP;
        isupping = true;
        final int w = getWidth();
        if(per == 1 && x !=w){
            autoUppingX = w ;
            currentPointX = w + 100;
            invalidate();
            return ;
        }else{
             per = (2 * x - w) / w;
            autoUppingX = (int) (0.25 * w * per + 0.75 * w);
            currentPointX = (int) (100 * per + w);
        }

        if(per > 0.8){
            if (showContent) {
                showContent = false;
                mMenuFragment.show(currentPointY) ;
            }
        }
        invalidate();
        if (per == 1){
            downing();
        }

    }

    public boolean isupping() {
        return isupping;
    }
    public void resetContent() {
        showContent = true ;
        isupping  = false ;
        mMenuFragment.hideView();
    }

    public void resetStatus(){
        per = 0 ;
        mStatus = Status.NONE ;
        isupping = false ;
    }


    public void setMenuFragment(MenuFragment mMenuFragment ){
        this.mMenuFragment = mMenuFragment ;
    }

    public void setRightMargin(int rightMargin){
        this.rightMargin = rightMargin ;
    }

}
