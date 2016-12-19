package com.mxn.soul.flowingdrawer_core;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by mxn on 2016/12/18.
 *
 */

public class FlowingView extends View {

    private Paint mPaint;
    private float mClipOffsetPixels = 0;

    public final static int TYPE_NONE = 0 ;
    public final static int TYPE_UP_MANUAL = 1 ;
    public final static int TYPE_UP_AUTO = 2 ;
    public final static int TYPE_UP1 = 3 ;
    public final static int TYPE_UP2 = 4 ;
    public final static int TYPE_UP3 = 5 ;
    public final static int TYPE_UP4 = 6 ;

    private int currentType = TYPE_NONE ;
    private float eventY  = 0 ;
    private int topControlY;
    private int bottomControlY;
    private int topY;
    private int bottomY;
    private Path mPath ;


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
        mPath = new Path();
        mPaint.setAntiAlias(true);
        mPaint.setColor(getResources().getColor(R.color.paint_color));
        ViewCompat.setLayerType(this, ViewCompat.LAYER_TYPE_NONE,
                null);
    }


    @Override
    protected void onDraw(Canvas canvas) {
//        drawBG(canvas);
        super.onDraw(canvas);
        drawBG(canvas);
    }

    private void drawBG(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();
        mPath.reset();
        switch ( currentType) {
            case TYPE_NONE :
                // x == 0 或全部
                break ;
            case TYPE_UP_MANUAL :
                double verticalOffsetRatio = Math.abs((double) (2 * eventY - height) / height);
                double ratio1 = verticalOffsetRatio * 3 + 1;
                double ratio2 = verticalOffsetRatio * 5 + 1;
                int currentPointX = (int) mClipOffsetPixels;
                if (eventY - height / 2 >= 0) {
                    bottomY = (int) (eventY + 0.7 *height/ (ratio1 + 1) + currentPointX * 6 / (ratio2 + 1));
                    topY = (int) (eventY - 0.7 * height / (1 + 1 / ratio1) - currentPointX * 6 / (1 / ratio2 + 1));
                    topControlY = (int) (-bottomY / 4 + 5 * eventY / 4);
                    bottomControlY = (int) (bottomY / 4 + 3 * eventY / 4);
                } else {
                    bottomY = (int) (eventY + 0.7 * height / (1 / ratio1 + 1) + currentPointX * 6 / (1 / ratio2 + 1));
                    topY = (int) (eventY - 0.7 * height / (1 + ratio1) - currentPointX * 6 / (ratio2 + 1));
                    topControlY = (int) (topY / 4 + 3 * eventY / 4);
                    bottomControlY = (int) (-topY / 4 + 5 * eventY / 4);
                }

                mPath.moveTo(width - mClipOffsetPixels, topY);
                mPath.cubicTo(width - mClipOffsetPixels, topControlY, width,
                        topControlY, width, eventY);
                mPath.cubicTo(width, bottomControlY,width - mClipOffsetPixels,
                        bottomControlY, width - mClipOffsetPixels, bottomY);
                mPath.lineTo(width - mClipOffsetPixels, topY);
                canvas.drawPath(mPath, mPaint);

                break ;
            case TYPE_UP_AUTO :

                break ;
        }
    }

    public void setClipOffsetPixels(float clipOffsetPixels, float eventY, int type) {
        mClipOffsetPixels = clipOffsetPixels;
        currentType = type ;
        this.eventY = eventY ;
        invalidate();
    }

}
