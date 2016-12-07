/*
 * Copyright (C) 2016 Baidu, Inc. All Rights Reserved.
 */
package com.mxn.soul.flowingdrawer_core;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;

/**
 * Created by baidu on 2016/10/17.
 */
public class FlowingDrawer extends ElasticDrawer {

    private int mPeekSize;

    static final boolean USE_TRANSLATIONS = Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;

    public FlowingDrawer(Context context) {
        super(context);
    }

    public FlowingDrawer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FlowingDrawer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void initDrawer(Context context, AttributeSet attrs, int defStyle) {
        super.initDrawer(context, attrs, defStyle);

        super.addView(mContentContainer, -1, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            mContentContainer.setLayerType(View.LAYER_TYPE_NONE, null);
        }
        mContentContainer.setHardwareLayersEnabled(false);

        super.addView(mMenuContainer, -1, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        mPeekSize = dpToPx(20);
    }

    @Override
    public void openMenu(boolean animate) {
        int animateTo = 0;
        switch (getPosition()) {
            case Position.LEFT:
                animateTo = mMenuSize;
                break;
            case Position.RIGHT:
                animateTo = -mMenuSize;
                break;
        }
        animateOffsetTo(animateTo, 0, animate);
    }

    @Override
    public void closeMenu(boolean animate) {

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        if (widthMode == MeasureSpec.UNSPECIFIED || heightMode == MeasureSpec.UNSPECIFIED) {
            throw new IllegalStateException("Must measure with an exact size");
        }

        final int width = MeasureSpec.getSize(widthMeasureSpec);
        final int height = MeasureSpec.getSize(heightMeasureSpec);

        if (mOffsetPixels == -1) openMenu(false);

        int menuWidthMeasureSpec;
        int menuHeightMeasureSpec;
        menuWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec, 0, mMenuSize);
        menuHeightMeasureSpec = getChildMeasureSpec(widthMeasureSpec, 0, height);
        mMenuContainer.measure(menuWidthMeasureSpec, menuHeightMeasureSpec);

        final int contentWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec, 0, width);
        final int contentHeightMeasureSpec = getChildMeasureSpec(widthMeasureSpec, 0, height);
        mContentContainer.measure(contentWidthMeasureSpec, contentHeightMeasureSpec);

        setMeasuredDimension(width, height);

        updateTouchAreaSize();
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int width = r - l;
        final int height = b - t;

        mContentContainer.layout(0, 0, width, height);

        if (USE_TRANSLATIONS) {
            switch (getPosition()) {
                case Position.LEFT:
                    mMenuContainer.layout(0, 0, mMenuSize, height);
                    break;

                case Position.RIGHT:
                    mMenuContainer.layout(width - mMenuSize, 0, width, height);
                    break;
            }

        } else {
            final int offsetPixels = (int) mOffsetPixels;
            final int menuSize = mMenuSize;

            switch (getPosition()) {
                case Position.LEFT:
                    mMenuContainer.layout(-menuSize + offsetPixels, 0, offsetPixels, height);
                    break;
                case Position.RIGHT:
                    mMenuContainer.layout(width + offsetPixels, 0, width + menuSize + offsetPixels, height);
                    break;
            }
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (!mMenuVisible && !mIsDragging && mTouchMode == TOUCH_MODE_NONE) {
            return false;
        }
        final int action = ev.getAction() & MotionEvent.ACTION_MASK;

        if (mVelocityTracker == null) mVelocityTracker = VelocityTracker.obtain();
        mVelocityTracker.addMovement(ev);

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                mLastMotionX = mInitialMotionX = ev.getX();
                mLastMotionY = mInitialMotionY = ev.getY();
                final boolean allowDrag = onDownAllowDrag((int) mLastMotionX, (int) mLastMotionY);

                mActivePointerId = ev.getPointerId(0);

                if (allowDrag) {
                    stopAnimation();
                    endPeek();

                    if (!mMenuVisible && mLastMotionX <= mPeekSize) {
                        postDelayed(mRevealRunnable, 160);
                    }

                    startLayerTranslation();
                }
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                final int pointerIndex = ev.findPointerIndex(mActivePointerId);
                if (pointerIndex == -1) {
                    mIsDragging = false;
                    mActivePointerId = INVALID_POINTER;
                    endDrag();
                    closeMenu(true);
                    return false;
                }

                if (!mIsDragging) {
                    final float x = ev.getX(pointerIndex);
                    final float dx = x - mLastMotionX;
                    final float y = ev.getY(pointerIndex);
                    final float dy = y - mLastMotionY;

                    if (checkTouchSlop(dx, dy)) {
                        final boolean allowDrag = onMoveAllowDrag((int) x, (int) y, dx, dy);

                        if (allowDrag) {
                            endPeek();
                            stopAnimation();
                            setDrawerState(STATE_DRAGGING);
                            mIsDragging = true;
                            mLastMotionX = x;
                            mLastMotionY = y;
                        } else {
                            mInitialMotionX = x;
                            mInitialMotionY = y;
                        }
                    }
                }

                if (mIsDragging) {
                    startLayerTranslation();

                    final float x = ev.getX(pointerIndex);
                    final float dx = x - mLastMotionX;
                    final float y = ev.getY(pointerIndex);
                    final float dy = y - mLastMotionY;

                    mLastMotionX = x;
                    mLastMotionY = y;
                    onMoveEvent(dx, dy);
                }
                break;
            }

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                removeCallbacks(mRevealRunnable);
                int index = ev.findPointerIndex(mActivePointerId);
                index = index == -1 ? 0 : index;
                final int x = (int) ev.getX(index);
                final int y = (int) ev.getY(index);
                onUpEvent(x, y);
                mActivePointerId = INVALID_POINTER;
                mIsDragging = false;
                break;
            }

            case MotionEvent.ACTION_POINTER_DOWN:
                final int index = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK)
                        >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                mLastMotionX = ev.getX(index);
                mLastMotionY = ev.getY(index);
                mActivePointerId = ev.getPointerId(index);
                break;

            case MotionEvent.ACTION_POINTER_UP:
                onPointerUp(ev);
                mLastMotionX = ev.getX(ev.findPointerIndex(mActivePointerId));
                mLastMotionY = ev.getY(ev.findPointerIndex(mActivePointerId));
                break;
        }

        return true;
    }


}
