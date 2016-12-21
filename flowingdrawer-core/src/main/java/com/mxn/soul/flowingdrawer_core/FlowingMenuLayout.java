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
    public final static int TYPE_UP1 = 3;
    public final static int TYPE_UP2 = 4;
    public final static int TYPE_UP3 = 5;
    public final static int TYPE_UP4 = 6;

    private int currentType = TYPE_NONE;
    private float eventY = 0;
    private int topControlY;
    private int bottomControlY;
    private int topY;
    private int bottomY;

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

    @Override
    protected void dispatchDraw(Canvas canvas) {

        int width = getWidth();
        int height = getHeight();
        switch (currentType) {
            case TYPE_NONE:
                // x == 0 或全部
                break;
            case TYPE_UP_MANUAL:
                double verticalOffsetRatio = Math.abs((double) (2 * eventY - height) / height);
                double ratio1 = verticalOffsetRatio * 3 + 1;
                double ratio2 = verticalOffsetRatio * 5 + 1;
                int currentPointX = (int) mClipOffsetPixels;
                if (eventY - height / 2 >= 0) {
                    bottomY = (int) (eventY + 0.7 * height / (ratio1 + 1) + currentPointX * 6 / (ratio2 + 1));
                    topY = (int) (eventY - 0.7 * height / (1 + 1 / ratio1) - currentPointX * 6 / (1 / ratio2 + 1));
                    topControlY = (int) (-bottomY / 4 + 5 * eventY / 4);
                    bottomControlY = (int) (bottomY / 4 + 3 * eventY / 4);
                } else {
                    bottomY = (int) (eventY + 0.7 * height / (1 / ratio1 + 1) + currentPointX * 6 / (1 / ratio2 + 1));
                    topY = (int) (eventY - 0.7 * height / (1 + ratio1) - currentPointX * 6 / (ratio2 + 1));
                    topControlY = (int) (topY / 4 + 3 * eventY / 4);
                    bottomControlY = (int) (-topY / 4 + 5 * eventY / 4);
                }

                mClipPath.reset();
                mClipPath.moveTo(width - mClipOffsetPixels, topY);
                mClipPath.cubicTo(width - mClipOffsetPixels, topControlY, width,
                        topControlY, width, eventY);
                mClipPath.cubicTo(width, bottomControlY, width - mClipOffsetPixels,
                        bottomControlY, width - mClipOffsetPixels, bottomY);
                mClipPath.lineTo(width - mClipOffsetPixels, topY);
                canvas.save();
                canvas.drawPath(mClipPath, mPaint);
                canvas.clipPath(mClipPath, Region.Op.REPLACE);
                canvas.setDrawFilter(pfd);
                super.dispatchDraw(canvas);
                canvas.restore();
                break;
            case TYPE_UP_AUTO: {
                currentPointX = (int) mClipOffsetPixels;
                mClipPath.reset();
                if (eventY - getHeight() / 2 >= 0) {
                    topY = (int) (eventY - 1.5 * currentPointX * height / width -
                                          eventY / 2 + height / 4);
                    bottomY = (int) (eventY + 1.5 * currentPointX * getHeight() / getWidth());
                } else {
                    topY = (int) (eventY - 1.5 * currentPointX * getHeight() / getWidth());
                    bottomY = (int) (eventY + 1.5 * currentPointX * getHeight() / getWidth() -
                                             eventY / 2 + getHeight() / 4);
                }
                mClipPath.moveTo(getWidth() - currentPointX, topY);
                mClipPath.cubicTo(getWidth() - currentPointX, eventY / 4 + 3 * topY / 4, getWidth(), 3 *
                                eventY / 4
                                + topY / 4,
                        getWidth()
                        , eventY);
                mClipPath.cubicTo(getWidth(), 5 * eventY / 4 - topY / 4, getWidth() - currentPointX,
                        7 * eventY / 4 - 3 * topY / 4, getWidth() - currentPointX, bottomY);
                mClipPath.lineTo(getWidth() - currentPointX, topY);
                canvas.save();
                canvas.clipPath(mClipPath);
                super.dispatchDraw(canvas);
                canvas.restore();
                break;
            }
        }
    }

    //        @Override
    //        protected void onDraw(Canvas canvas) {
    //            Rect newRect = canvas.getClipBounds();
    //            newRect.inset (2000, 2000)  ;//make the rect larger
    //            canvas.clipRect (newRect, Region.Op.REPLACE);
    //            super.onDraw(canvas);
    //        }
}
