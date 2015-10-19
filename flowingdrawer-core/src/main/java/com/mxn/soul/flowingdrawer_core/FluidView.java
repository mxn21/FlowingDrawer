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

/**
 * Created by cys on 15/10/8.
 */
public class FluidView extends View {

    private Paint mPaint;
//    private int mMaxArcWidth;
    private Status mStatus = Status.NONE;
    private AnimationListener mAnimationListener;
    private Path mPath = new Path();
    private int currentPointX = 0;
    private int currentPointY = 0;
    private int topY;
    private int bottomY;
    private int downspeed = 1;
    private boolean isupping = false;

    private int autoUppingX;

    private int mArcWidth;
    boolean showContent = true;


    public enum Status {
        NONE,
        STATUS_SMOOTH_UP,
        STATUS_UP,
        STATUS_DOWN,
    }

    public FluidView(Context context) {
        super(context);
        init();
    }

    public FluidView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public FluidView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(getResources().getColor(android.R.color.white));
//        mMaxArcWidth = getResources().getDimensionPixelSize(R.dimen.arc_max_width);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public FluidView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBG(canvas);
    }

    public void show(float x, float y, Status status) {



//        if (isupping && mStatus == Status.STATUS_SMOOTH_UP) {
//            isupping = false ;
//            final int temp = currentPointX ;
//            final int w = getWidth() ;
//            int length = (int) (1.5 * currentPointX * getHeight() / getWidth());
//            ValueAnimator valueAnimator = ValueAnimator.ofInt(length, 2 * length );
//            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//                @Override
//                public void onAnimationUpdate(ValueAnimator animation) {
//                     mArcWidth = (int) animation.getAnimatedValue();
////                    autoUppingX =  ;
////                    currentPointX = (25*autoUppingX *w + 5*temp*w - 20
////                            *autoUppingX * temp )/
////                            (20*w -12*temp) ;
////                    if (autoUppingX == getWidth()) {
////                        duang();
////                    }
//                    invalidate();
//                }
//            });
//            valueAnimator.setDuration(400);
//            valueAnimator.setInterpolator(new AccelerateInterpolator());
//            valueAnimator.start();
//            return;
//        }

        if (mStatus == Status.STATUS_SMOOTH_UP)
            return;
        mStatus = status;
        if (mStatus == Status.STATUS_UP) {
            currentPointX = (int) x;
            currentPointY = (int) y;
//            if (mAnimationListener != null) {
//                mAnimationListener.onStart();
//                this.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        mAnimationListener.onContentShow();
//                    }
//                }, 600);
//            }
        } else if (mStatus == Status.STATUS_DOWN) {
            downspeed = (int) Math.abs(x - currentPointX);
            currentPointX = (int) x;
            currentPointY = (int) y;

        }
        invalidate();
//        Log.e("====", mStatus + "===show") ;
    }

    public void downing() {
        final int w = getWidth();
        ValueAnimator valueAnimator = ValueAnimator.ofInt(w + 100, w - 50);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
//                mArcWidth = (int) animation.getAnimatedValue();
//                invalidate();
                currentPointX = (int) animation.getAnimatedValue();
                float fraction = animation.getAnimatedFraction();
                autoUppingX = (int) (w - 50 * fraction);
                invalidate();
            }
        });
        valueAnimator.addListener(new FlowingAnimationListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (mAnimationListener != null) {
                    mAnimationListener.onEnd(currentPointY);
                    isupping = false;
                    showContent = true;
                }
            }
        });
        valueAnimator.setDuration(500);
        valueAnimator.setInterpolator(new OvershootInterpolator(4f));
        valueAnimator.start();
    }


    private void drawBG(Canvas canvas) {
//        Log.e("====", mStatus + "") ;

        mPath.reset();
        switch (mStatus) {
            case NONE:
//                currentPointX = getWidth() - mMaxArcWidth;
                break;
            case STATUS_SMOOTH_UP:

                mPath.moveTo(autoUppingX, 0);

                mPath.quadTo(currentPointX, currentPointY, autoUppingX, getHeight());

                mPath.lineTo(0, getHeight());
                mPath.lineTo(0, 0);
                mPath.lineTo(autoUppingX, 0);
                canvas.drawPath(mPath, mPaint);
//                topY =  currentPointY - mArcWidth  ;
//                bottomY = currentPointY + mArcWidth ;
//
//                mPath.moveTo(0, topY);
//
//                mPath.cubicTo(0, currentPointY / 4 + 3 * topY / 4, currentPointX, 3 * currentPointY / 4
//                                + topY / 4,
//                        currentPointX
//                        , currentPointY);
//                mPath.cubicTo(currentPointX, 5 * currentPointY / 4 - topY / 4, 0,
//                        7 * currentPointY / 4 - 3 * topY / 4, 0, bottomY);
//
//                mPath.lineTo(0, topY);
//                canvas.drawPath(mPath, mPaint);


                break;
            case STATUS_UP:
                if (isupping) return;
//                if (currentPointX > getWidth() / 2) {
//                    isupping = true;
//                    mStatus = Status.STATUS_SMOOTH_UP;
//                    invalidate();
//                    return;
//                }
                if (currentPointY - getHeight() / 2 >= 0) {
                    topY = (int) (currentPointY - 1.5 * currentPointX * getHeight() / getWidth()) - currentPointY / 2 +
                            getHeight() / 4;
                    bottomY = (int) (currentPointY + 1.5 * currentPointX * getHeight() / getWidth());
                } else {
                    topY = (int) (currentPointY - 1.5 * currentPointX * getHeight() / getWidth());
                    bottomY = (int) (currentPointY + 1.5 * currentPointX * getHeight() / getWidth()) -
                            currentPointY / 2 + getHeight() / 4;
                }

//                mPath.moveTo(0, topY);
//
//                mPath.cubicTo(0, currentPointY / 4 + 3 * topY / 4, currentPointX, 3 * currentPointY / 4
//                                + topY / 4,
//                        currentPointX
//                        , currentPointY);
//                mPath.cubicTo(currentPointX, 5 * currentPointY / 4 - topY / 4, 0,
//                        7 * currentPointY / 4 - 3 * topY / 4, 0, bottomY);
//
//                mPath.lineTo(0, topY);


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

//                mPath.moveTo(0, topY);
//
//                mPath.cubicTo(0, currentPointY / 4 + 3 * topY / 4, currentPointX, 3 * currentPointY / 4
//                                + topY / 4,
//                        currentPointX
//                        , currentPointY);
//                mPath.cubicTo(currentPointX, 5 * currentPointY / 4 - topY / 4, 0,
//                        7 * currentPointY / 4 - 3 * topY / 4, 0, bottomY);
//
//                mPath.lineTo(0, topY);
//
//                getWidth()-currentPointX
//                canvas.drawPath(mPath, mPaint);
//TODO
//                Log.e("===DOWN", topY + "--"+(getWidth()-currentPointX)+"---"+currentPointX);
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

//        mPath.moveTo(currentPointX, 0);
//        mPath.quadTo(currentPointX + mArcWidth, getHeight() / 2, currentPointX, getHeight());
//        mPath.lineTo(0, getHeight());
//        mPath.lineTo(0, 0);
//        mPath.lineTo(currentPointX, 0);

//        mPath.moveTo(0, 0);
//        mPath.quadTo(currentPointX , currentPointY, 0, getHeight());
//        mPath.quadTo(currentPointX/2,  currentPointY,
//                currentPointX, currentPointY);
//
//        mPath.quadTo(currentPointX/2,  currentPointY,
//                0, getHeight());

//
//        mPath.moveTo(0, topY);
//
//        mPath.cubicTo(0, currentPointY / 4 + 3 * topY / 4, currentPointX, 3 * currentPointY / 4
//                        + topY / 4,
//                currentPointX
//                , currentPointY);
//        mPath.cubicTo(currentPointX, 5 * currentPointY / 4 - topY / 4, 0,
//                7 * currentPointY / 4 - 3 * topY / 4, 0, bottomY);
//
//        mPath.lineTo(0, topY);
//        canvas.drawPath(mPath, mPaint);
    }


    public AnimationListener getAnimationListener() {
        return mAnimationListener;
    }

    public void setAnimationListener(AnimationListener animationListener) {
        mAnimationListener = animationListener;
    }

    public interface AnimationListener {

        void onStart();

        void onEnd(int y);

        void onContentShow(int y);

        void onReSet() ;


    }

    public boolean isStartAuto(float x) {
        return x >= getWidth() / 2;
    }


    public void autoUpping(float x) {
        mStatus = Status.STATUS_SMOOTH_UP;
        isupping = true;

//        final int temp = x;
        final int w = getWidth();
//        ValueAnimator valueAnimator = ValueAnimator.ofInt(temp / 2, w);
//        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//            @Override
//            public void onAnimationUpdate(ValueAnimator animation) {
//                autoUppingX = (int) animation.getAnimatedValue();
//                float fraction = animation.getAnimatedFraction();
//                if (fraction < 0.8) {
//                    currentPointX = (int) (temp + 0.8 * fraction * (w - temp));
//                } else {
//                    if(showContent) {
//                        showContent = false;
//                        mAnimationListener.onContentShow(currentPointY );
//                    }
//                    currentPointX = (int) (1.8 * fraction * w - 0.8 * w - 1.8 * fraction * temp + 500 *
//                            fraction - 400 + 1.8 * temp);
//                }
//                invalidate();
//            }
//        });
//        valueAnimator.addListener(new SimpleAnimationListener() {
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                showContent = true;
//                downing();
//            }
//        });
//        valueAnimator.setDuration(400);
//        valueAnimator.setInterpolator(new DecelerateInterpolator());
//        valueAnimator.start();
//        autoUppingX = (2*x*w - halfWidth * w - halfWidth * x)/(2*w - 2*halfWidth);
//        currentPointX = w+100;

//        autoUppingX = (2*x*w - halfWidth * w - halfWidth * x/(2*w - 2*halfWidth);
        double per = (2 * x - w) / w;

        autoUppingX = (int) (0.25 * w * per + 0.75 * w);
        currentPointX = (int) (100 * per + w);

        if(per <= 0.8){
//            currentPointX =  w;
        }else  {
//            currentPointX = (int) (100 * per + w);
            if (showContent) {
                showContent = false;
                mAnimationListener.onContentShow(currentPointY);
            }
        }
        invalidate();

        if (per == 1)
            downing();
    }


    public boolean isupping() {
        return isupping;
    }
    public void resetContent() {
        showContent = true ;
        isupping  = false ;
        mAnimationListener.onReSet();
    }

    public void resetStatus(){
        mStatus = Status.NONE ;
        isupping = false ;
    }

    public Status getStatus(){
        return mStatus  ;
    }
}
