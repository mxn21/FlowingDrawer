package com.mxn.soul.flowingdrawer_core;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ValueAnimator;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.OvershootInterpolator;

public class FlowingView2 extends View {

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

    private MenuFragment mMenuFragment;

    private double per = 0;

    private int rightMargin;

    public static int currentStatus = -1;
    public static final int STATUS_OPEN_MANUAL = 0;
    public static final int STATUS_OPEN_AUTO = 1;

    private boolean isAutoOpenFinish = true;
    private int topControlY;
    private int bottomControlY;
    private int controlAutoX;
    private float fraction1;

    public enum Status {
        NONE,
        STATUS_SMOOTH_UP,
        STATUS_UP,
        STATUS_DOWN,

    }

    public FlowingView2(Context context) {
        super(context);
        init();
    }

    public FlowingView2(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public FlowingView2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(getResources().getColor(R.color.paint_color));

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public FlowingView2(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBG(canvas);
    }

    public void show(float x, float y, Status status) {
        if (mStatus == Status.STATUS_SMOOTH_UP) {
            return;
        }
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
        //        switch (mStatus) {
        //            case NONE:
        //                break;
        //            case STATUS_SMOOTH_UP:
        //                mPath.moveTo(autoUppingX, 0);
        //                mPath.quadTo(currentPointX, currentPointY, autoUppingX, getHeight());
        //                mPath.lineTo(0, getHeight());
        //                mPath.lineTo(0, 0);
        //                mPath.lineTo(autoUppingX, 0);
        //                canvas.drawPath(mPath, mPaint);
        //                break;
        //            case STATUS_UP:
        //                if (isupping) return;
        //                if (currentPointY - getHeight() / 2 >= 0) {
        //                    topY = (int) (currentPointY - 1.5 * currentPointX * getHeight() / getWidth()) -
        // currentPointY / 2 +
        //                            getHeight() / 4;
        //                    bottomY = (int) (currentPointY + 1.5 * currentPointX * getHeight() / getWidth());
        //                } else {
        //                    topY = (int) (currentPointY - 1.5 * currentPointX * getHeight() / getWidth());
        //                    bottomY = (int) (currentPointY + 1.5 * currentPointX * getHeight() / getWidth()) -
        //                            currentPointY / 2 + getHeight() / 4;
        //                }
        //                mPath.moveTo(getWidth() - currentPointX, topY);
        //                mPath.cubicTo(getWidth() - currentPointX, currentPointY / 4 + 3 * topY / 4, getWidth(), 3 *
        // currentPointY / 4
        //                                + topY / 4,
        //                        getWidth()
        //                        , currentPointY);
        //                mPath.cubicTo(getWidth(), 5 * currentPointY / 4 - topY / 4, getWidth() - currentPointX,
        //                        7 * currentPointY / 4 - 3 * topY / 4, getWidth() - currentPointX, bottomY);
        //                mPath.lineTo(getWidth() - currentPointX, topY);
        //                canvas.drawPath(mPath, mPaint);
        //                break;
        //            case STATUS_DOWN:
        //                topY = topY - downspeed;
        //                bottomY = bottomY + downspeed;
        //                mPath.moveTo(getWidth() - currentPointX, topY);
        //                mPath.cubicTo(getWidth() - currentPointX, currentPointY / 4 + 3 * topY / 4, getWidth(), 3 *
        // currentPointY / 4
        //                                + topY / 4,
        //                        getWidth()
        //                        , currentPointY);
        //                mPath.cubicTo(getWidth(), 5 * currentPointY / 4 - topY / 4, getWidth() - currentPointX,
        //                        7 * currentPointY / 4 - 3 * topY / 4, getWidth() - currentPointX, bottomY);
        //                mPath.lineTo(getWidth() - currentPointX, topY);
        //                canvas.drawPath(mPath, mPaint);
        //                break;
        //        }

        int width = getWidth();
        int height = getHeight();
        switch (currentStatus) {
            case STATUS_OPEN_MANUAL:
                /**
                 verticalOffsetRatio = -1 when currentPointY = 0 ;
                 verticalOffsetRatio = 0 when currentPointY = 0.5 * height ;
                 verticalOffsetRatio = 1 when currentPointY = height ;
                 bottomY,topY由两部分组成
                 第一部分是初始位置，由ratio1和currentPointY决定
                 第二部分由currentPointX移动位置决定
                 两部分系数分别是ratio1，ratio2
                 ratio1，ratio2表示 (currentPointY - topY)/ (bottomY - currentPointY)
                 第一部分bottomY - topY的初始值为height/2 倍
                 第二部分bottomY - topY变化的总长为currentPointX变化总长的6倍
                 */
                double verticalOffsetRatio = Math.abs((double) (2 * currentPointY - height) / height);
                double ratio1 = verticalOffsetRatio * 3 + 1;
                double ratio2 = verticalOffsetRatio * 5 + 1;
                if (currentPointY - getHeight() / 2 >= 0) {
                    bottomY = (int) (currentPointY + 0.7 * height / (ratio1 + 1) + currentPointX * 6 / (ratio2 + 1));
                    topY = (int) (currentPointY - 0.7 * height / (1 + 1 / ratio1) - currentPointX * 6 / (1 / ratio2 +
                            1));
                } else {
                    bottomY = (int) (currentPointY + 0.7 * height / (1 / ratio1 + 1) + currentPointX * 6 / (1 /
                            ratio2 + 1));
                    topY = (int) (currentPointY - 0.7 * height / (1 + ratio1) - currentPointX * 6 / (ratio2 + 1));
                }

                if (currentPointY - getHeight() / 2 >= 0) {
                    topControlY = -bottomY / 4 + 5 * currentPointY / 4;
                    bottomControlY = bottomY / 4 + 3 * currentPointY / 4;
                } else {
                    topControlY = topY / 4 + 3 * currentPointY / 4;
                    bottomControlY = -topY / 4 + 5 * currentPointY / 4;
                }

                mPath.moveTo(getWidth() - currentPointX, topY);
                mPath.cubicTo(getWidth() - currentPointX, topControlY, getWidth(),
                        topControlY, getWidth(), currentPointY);
                mPath.cubicTo(getWidth(), bottomControlY, getWidth() - currentPointX,
                        bottomControlY, getWidth() - currentPointX, bottomY);
                mPath.lineTo(getWidth() - currentPointX, topY);
                canvas.drawPath(mPath, mPaint);
                Log.e("====",verticalOffsetRatio +"===" +topY + "====="+bottomY) ;
                break;
            case STATUS_OPEN_AUTO:
                Log.e("====", topY + "");

                int x = (int) (getWidth() + fraction1 * 100);
                mPath.moveTo(getWidth() - currentPointX, topY);
                mPath.cubicTo(controlAutoX, topControlY, getWidth(),
                        topControlY, getWidth(), currentPointY);
                mPath.cubicTo(getWidth(), bottomControlY, controlAutoX,
                        bottomControlY, getWidth() - currentPointX, bottomY);
                mPath.lineTo(getWidth() - currentPointX, topY);
                canvas.drawPath(mPath, mPaint);
                break;
            default:
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
        if (per == 1 && x != w) {
            autoUppingX = w;
            currentPointX = w + 100;
            invalidate();
            return;
        } else {
            per = (2 * x - w) / w;
            autoUppingX = (int) (0.25 * w * per + 0.75 * w);
            currentPointX = (int) (100 * per + w);
        }

        if (per > 0.8) {
            if (showContent) {
                showContent = false;
                mMenuFragment.show(currentPointY);
            }
        }
        invalidate();
        if (per == 1) {
            downing();
        }

    }

    public boolean isupping() {
        return isupping;
    }

    public void resetContent() {
        showContent = true;
        isupping = false;
        mMenuFragment.hideView();
    }

    public void resetStatus() {
        per = 0;
        mStatus = Status.NONE;
        isupping = false;
    }

    public void setMenuFragment(MenuFragment mMenuFragment) {
        this.mMenuFragment = mMenuFragment;
    }

    public void setRightMargin(int rightMargin) {
        this.rightMargin = rightMargin;
    }

    public void show(float x, float y, int status) {
        currentStatus = status;
        switch (currentStatus) {
            case STATUS_OPEN_MANUAL:
                currentPointX = (int) x;
                currentPointY = (int) y;
                invalidate();
                break;
            case STATUS_OPEN_AUTO:
                autoOpen(x);
                break;
            default:
                break;
        }

    }

    private void autoOpen(float x) {
        final int w = getWidth();
        per = (2 * x - w) / w;

        ValueAnimator valueAnimator = ValueAnimator.ofInt(getWidth() - currentPointX, w);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                controlAutoX = (int) animation.getAnimatedValue();
                fraction1 = animation.getAnimatedFraction();
                invalidate();
            }
        });
        valueAnimator.addListener(new FlowingAnimationListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
            }
        });
        valueAnimator.setDuration(100);
        valueAnimator.setInterpolator(new AccelerateInterpolator(4f));
        valueAnimator.start();
    }

    public int getCurrentStatus() {
        return currentStatus;
    }

}

