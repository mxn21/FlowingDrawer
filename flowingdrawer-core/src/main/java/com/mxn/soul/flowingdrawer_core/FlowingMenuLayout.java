package com.mxn.soul.flowingdrawer_core;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

/**
 * Created by mxn on 2016/12/13.
 *
 */

public class FlowingMenuLayout extends FrameLayout {

    private Path mClipPath;
    private float mClipOffsetPixels = 0;

    public FlowingMenuLayout(Context context) {
        this(context, null);
    }

    public FlowingMenuLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FlowingMenuLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mClipPath = new Path();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
    }

    public float getClipOffsetPixels() {
        return mClipOffsetPixels;
    }

    public void setClipOffsetPixels(float clipOffsetPixels) {
        mClipOffsetPixels = clipOffsetPixels;
        invalidate();
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
//        mClipPath.reset();
//        mClipPath.addCircle(mClipCenterX, mClipCenterY, mClipRadius, Path.Direction.CW);
//        canvas.save();
//        canvas.clipPath(mClipPath);
        super.dispatchDraw(canvas);
//        canvas.restore();
//        Log.e("======dispatchDraw", mClipRadius + "");
    }

}
