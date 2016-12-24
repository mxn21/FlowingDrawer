package com.mxn.soul.flowingdrawer_core;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.Region;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * Created by mxn on 2016/12/13.
 */

public class FlowingMenuLayout extends FrameLayout {

    private Path mClipPath;
    private float mClipOffsetPixels = 0;

    public final static int TYPE_NONE = 0;
    public final static int TYPE_UP_MANUAL = 1;
    public final static int TYPE_UP_AUTO = 2;
    public final static int TYPE_UP_DOWN = 3;
    public final static int TYPE_DOWN_AUTO = 4;
    public final static int TYPE_DOWN_MANUAL = 5;
    public final static int TYPE_DOWN_SMOOTH = 6;

    private int currentType = TYPE_NONE;
    private float eventY = 0;
    private int topControlY;
    private int bottomControlY;
    private int topY;
    private int bottomY;
    private int width;
    private int height;
    private double verticalOffsetRatio;
    private double ratio1;
    private double ratio2;
    private float fraction;
    private float fractionUpDown;
    private float fractionEdge;
    private float fractionCenter;
    private float fractionCenterDown;

    private int centerXOffset;
    private int edgeXOffset;

    private Paint mPaint;
    private PaintFlagsDrawFilter pfd;

    public FlowingMenuLayout(Context context) {
        this(context, null);
    }

    public FlowingMenuLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FlowingMenuLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mClipPath = new Path();
        //        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2
        //                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(getResources().getColor(R.color.paint_color2));
        //        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeWidth(5);
        //            setLayerType(View.LAYER_TYPE_SOFTWARE, mPaint);
        ViewCompat.setLayerType(this, ViewCompat.LAYER_TYPE_NONE,
                null);
        pfd = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint
                .FILTER_BITMAP_FLAG);
        //        }

    }

    public float getClipOffsetPixels() {
        return mClipOffsetPixels;
    }

    public void setClipOffsetPixels(float clipOffsetPixels, float eventY, int type) {
        mClipOffsetPixels = clipOffsetPixels;
        currentType = type;
        this.eventY = eventY;
        invalidate();
    }

    public void setUpDownFraction(float fraction) {
        fractionUpDown = fraction;
        currentType = TYPE_UP_DOWN;
        invalidate();
    }


    @Override
    protected void dispatchDraw(Canvas canvas) {

        width = getWidth();
        height = getHeight();
        mClipPath.reset();
        switch (currentType) {
            case TYPE_NONE:
                // x == 0 或全部
                mClipPath.moveTo(0, 0);
                mClipPath.lineTo(width, 0) ;
                mClipPath.lineTo(width, height) ;
                mClipPath.lineTo(0, height) ;
                mClipPath.lineTo(0, 0);
                break;
            case TYPE_UP_MANUAL:
                verticalOffsetRatio = Math.abs((double) (2 * eventY - height) / height);
                ratio1 = verticalOffsetRatio * 3 + 1;
                ratio2 = verticalOffsetRatio * 5 + 1;
                if (eventY - height / 2 >= 0) {
                    bottomY = (int) (eventY + 0.7 * height / (ratio1 + 1) + mClipOffsetPixels * 6 / (ratio2 + 1));
                    topY = (int) (eventY - 0.7 * height / (1 + 1 / ratio1) - mClipOffsetPixels * 6 / (1 / ratio2 + 1));
                    topControlY = (int) (-bottomY / 4 + 5 * eventY / 4);
                    bottomControlY = (int) (bottomY / 4 + 3 * eventY / 4);
                } else {
                    bottomY =
                            (int) (eventY + 0.7 * height / (1 / ratio1 + 1) + mClipOffsetPixels * 6 / (1 / ratio2 + 1));
                    topY = (int) (eventY - 0.7 * height / (1 + ratio1) - mClipOffsetPixels * 6 / (ratio2 + 1));
                    topControlY = (int) (topY / 4 + 3 * eventY / 4);
                    bottomControlY = (int) (-topY / 4 + 5 * eventY / 4);
                }
                mClipPath.moveTo(width - mClipOffsetPixels, topY);
                mClipPath.cubicTo(width - mClipOffsetPixels, topControlY, width,
                        topControlY, width, eventY);
                mClipPath.cubicTo(width, bottomControlY, width - mClipOffsetPixels,
                        bottomControlY, width - mClipOffsetPixels, bottomY);
                mClipPath.lineTo(width - mClipOffsetPixels, topY);
                break;
            case TYPE_UP_AUTO:
                fraction = (mClipOffsetPixels - width / 2) / (width / 2);
                if (fraction <=0.5 ){
                    fractionCenter = (float) (2* Math.pow(fraction, 2));
                    fractionEdge = (float) ((1/Math.sqrt(2)) * Math.sqrt(fraction));
                }else {
                    fractionCenter = (float) (1/(2-Math.sqrt(2)) * Math.sqrt(fraction) + 1- 1/(2-Math.sqrt(2)));
                    fractionEdge = (float) (2*Math.pow(fraction, 2)/3 + (float)1/3);
                }
                centerXOffset = (int) (width / 2 + fractionCenter * (width / 2 + 150));
                edgeXOffset = (int) (width * 0.75 + fractionEdge * (width / 4 + 100));
                mClipPath.moveTo(width - mClipOffsetPixels, 0);
                mClipPath.lineTo(edgeXOffset, 0);
                mClipPath.quadTo(centerXOffset, eventY, edgeXOffset, height);
                mClipPath.lineTo(width - mClipOffsetPixels, height);
                mClipPath.lineTo(width - mClipOffsetPixels, 0);
                break;
            case TYPE_UP_DOWN :
                centerXOffset = (int) (width + 150 - 150 * fractionUpDown);
                edgeXOffset = (int) (width + 100 - 100 * fractionUpDown);
                mClipPath.moveTo(width - mClipOffsetPixels, 0);
                mClipPath.lineTo(edgeXOffset, 0);
                mClipPath.quadTo(centerXOffset, eventY, edgeXOffset, height);
                mClipPath.lineTo(width - mClipOffsetPixels, height);
                mClipPath.lineTo(width - mClipOffsetPixels, 0);
                break ;
            case TYPE_DOWN_AUTO :
                fractionCenterDown = 1- mClipOffsetPixels/width ;
                centerXOffset = (int) (width - 0.5 * width * fractionCenterDown);
                mClipPath.moveTo(width - mClipOffsetPixels, 0);
                mClipPath.lineTo(width, 0);
                mClipPath.quadTo(centerXOffset, eventY, width, height);
                mClipPath.lineTo(width - mClipOffsetPixels, height);
                mClipPath.lineTo(width - mClipOffsetPixels, 0);
                break ;
            case TYPE_DOWN_MANUAL :
                fractionCenterDown = 1- mClipOffsetPixels/width ;
                centerXOffset = (int) (width - 0.5 * width * fractionCenterDown);
                mClipPath.moveTo(width - mClipOffsetPixels, 0);
                mClipPath.lineTo(width, 0);
                mClipPath.quadTo(centerXOffset, eventY, width, height);
                mClipPath.lineTo(width - mClipOffsetPixels, height);
                mClipPath.lineTo(width - mClipOffsetPixels, 0);
                break ;
            case TYPE_DOWN_SMOOTH :
                bottomY = bottomY + 10;
                topY = topY - 10;
                if (eventY - height / 2 >= 0) {
                    topControlY = (int) (-bottomY / 4 + 5 * eventY / 4);
                    bottomControlY = (int) (bottomY / 4 + 3 * eventY / 4);
                } else {
                    topControlY = (int) (topY / 4 + 3 * eventY / 4);
                    bottomControlY = (int) (-topY / 4 + 5 * eventY / 4);
                }

                mClipPath.moveTo(width - mClipOffsetPixels, topY);
                mClipPath.cubicTo(width - mClipOffsetPixels, topControlY, width,
                        topControlY, width, eventY);
                mClipPath.cubicTo(width, bottomControlY, width - mClipOffsetPixels,
                        bottomControlY, width - mClipOffsetPixels, bottomY);
                mClipPath.lineTo(width - mClipOffsetPixels, topY);

                break ;
        }

        canvas.save();
        canvas.drawPath(mClipPath, mPaint);
        canvas.clipPath(mClipPath, Region.Op.REPLACE);
        super.dispatchDraw(canvas);
        canvas.restore();
    }

    //        @Override
    //        protected void onDraw(Canvas canvas) {
    //            Rect newRect = canvas.getClipBounds();
    //            newRect.inset (2000, 2000)  ;//make the rect larger
    //            canvas.clipRect (newRect, Region.Op.REPLACE);
    //            super.onDraw(canvas);
    //        }

}
