package com.mxn.soul.flowingdrawer_core;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.nineoldandroids.view.ViewHelper;


public class LeftDrawerLayout extends ViewGroup {

    private static final int MIN_DRAWER_MARGIN = 0; // dp
    /**
     * Minimum velocity that will be detected as a fling
     */
    private static final int MIN_FLING_VELOCITY = 400; // dips per second

    /**
     * drawer离父容器右边的最小外边距
     */
    private int mMinDrawerMargin;

    private View mLeftMenuView;
    private ViewGroup mContentView;

    private ViewDragHelper mHelper;
    /**
     * drawer显示出来的占自身的百分比
     */
    private float mLeftMenuOnScrren;

    private FlowingView mFlowingView;

    private float pointY;
    private float rightX;
    //返回动画是否结束
    private boolean releasing = false;

    private ImageView mBg;

    private int rightMargin;


    public void setFluidView(FlowingView mFlowingView) {
        this.mFlowingView = mFlowingView;
        mFlowingView.setRightMargin(rightMargin);
        ViewCompat.setLayerType(this, ViewCompat.LAYER_TYPE_NONE, null);
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            ViewCompat.setLayerType(getChildAt(i), ViewCompat.LAYER_TYPE_NONE,
                    null);
        }
    }

    public void setMenuFragment(MenuFragment mMenuFragment) {
        mFlowingView.setMenuFragment(mMenuFragment);
    }

    public LeftDrawerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        //setup drawer's minMargin
        final float density = getResources().getDisplayMetrics().density;
        final float minVel = MIN_FLING_VELOCITY * density;
        mMinDrawerMargin = (int) (MIN_DRAWER_MARGIN * density + 0.5f);

        mHelper = ViewDragHelper.create(this, 1.0f, new ViewDragHelper.Callback() {
            @Override
            public int clampViewPositionHorizontal(View child, int left, int dx) {
                return Math.max(-child.getWidth(), Math.min(left, 0));
            }

            @Override
            public boolean tryCaptureView(View child, int pointerId) {
                return child == mLeftMenuView;
            }

            @Override
            public void onEdgeDragStarted(int edgeFlags, int pointerId) {
                mHelper.captureChildView(mLeftMenuView, pointerId);
            }


            @Override
            public void onViewReleased(View releasedChild, float xvel, float yvel) {
                final int childWidth = releasedChild.getWidth();
                float offset = (childWidth + releasedChild.getLeft()) * 1.0f / childWidth;
                boolean openMark = xvel > 0 || xvel == 0 && offset > 0.5f;
                mHelper.settleCapturedViewAt(openMark ? 0 : -childWidth, releasedChild.getTop());
                if (!openMark) {
                    releasing = true;
                    mFlowingView.resetContent();
                }
                invalidate();
            }

            @Override
            public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
                final int childWidth = changedView.getWidth();
                float offset = (float) (childWidth + left) / childWidth;
                mLeftMenuOnScrren = offset;
                showShadow(offset);
                changedView.setVisibility(offset == 0 ? View.INVISIBLE : View.VISIBLE);
                rightX = left + childWidth;
                if (mFlowingView.isStartAuto(rightX)) {
                    mFlowingView.autoUpping(rightX);
                    if (rightX == 0) {
                        mFlowingView.resetStatus();
                        releasing = false;
                    }
                    return;
                }

                if (mFlowingView.isupping()) {
                    if (rightX == 0) {
                        mFlowingView.resetStatus();
                        releasing = false;
                    }
                    return;
                }
                if (!releasing) {
                    mFlowingView.show(rightX, pointY, FlowingView.Status.STATUS_UP);
                } else {
                    mFlowingView.show(rightX, pointY, FlowingView.Status.STATUS_DOWN);
                    if (rightX == 0) {
                        mFlowingView.resetStatus();
                        releasing = false;
                    }
                }
                invalidate();
            }

            @Override
            public int getViewHorizontalDragRange(View child) {
                return mLeftMenuView == child ? child.getWidth() : 0;
            }
        });
        //设置edge_left track
        mHelper.setEdgeTrackingEnabled(ViewDragHelper.EDGE_LEFT);
        //设置minVelocity
        mHelper.setMinVelocity(minVel);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        setMeasuredDimension(widthSize, heightSize);

        View leftMenuView = getChildAt(1);
        MarginLayoutParams lp = (MarginLayoutParams)
                leftMenuView.getLayoutParams();
        final int drawerWidthSpec = getChildMeasureSpec(widthMeasureSpec,
                mMinDrawerMargin + lp.leftMargin + lp.rightMargin,
                lp.width);
        final int drawerHeightSpec = getChildMeasureSpec(heightMeasureSpec,
                lp.topMargin + lp.bottomMargin,
                lp.height);
        leftMenuView.measure(drawerWidthSpec, drawerHeightSpec);


        View contentView = getChildAt(0);
        lp = (MarginLayoutParams) contentView.getLayoutParams();
        final int contentWidthSpec = MeasureSpec.makeMeasureSpec(
                widthSize - lp.leftMargin - lp.rightMargin, MeasureSpec.EXACTLY);
        final int contentHeightSpec = MeasureSpec.makeMeasureSpec(
                heightSize - lp.topMargin - lp.bottomMargin, MeasureSpec.EXACTLY);
        contentView.measure(contentWidthSpec, contentHeightSpec);

        mLeftMenuView = leftMenuView;
        mContentView = (ViewGroup) contentView;

        ViewGroup mLeftView = (ViewGroup) mLeftMenuView;
        View fragmentLayout = mLeftView.getChildAt(1);
        MarginLayoutParams mlp = (MarginLayoutParams) fragmentLayout.getLayoutParams();
        rightMargin = mlp.rightMargin;
        if (mFlowingView != null)
            mFlowingView.setRightMargin(rightMargin);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        View menuView = mLeftMenuView;
        View contentView = mContentView;

        MarginLayoutParams lp = (MarginLayoutParams) contentView.getLayoutParams();
        contentView.layout(lp.leftMargin, lp.topMargin,
                lp.leftMargin + contentView.getMeasuredWidth(),
                lp.topMargin + contentView.getMeasuredHeight());

        lp = (MarginLayoutParams) menuView.getLayoutParams();

        final int menuWidth = menuView.getMeasuredWidth();
        int childLeft = -menuWidth + (int) (menuWidth * mLeftMenuOnScrren);
        menuView.layout(childLeft, lp.topMargin, childLeft + menuWidth,
                lp.topMargin + menuView.getMeasuredHeight());
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mHelper.shouldInterceptTouchEvent(ev);
    }


    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        mHelper.processTouchEvent(event);
        pointY = event.getY();
        return true;
    }


    @Override
    public void computeScroll() {
        if (mHelper.continueSettling(true)) {
            invalidate();
        }
    }

    @Override
    protected void dispatchDraw(@NonNull Canvas canvas) {
        PaintFlagsDrawFilter pfd = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint
                .FILTER_BITMAP_FLAG);
        canvas.setDrawFilter(pfd);
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child == null)
                return;
        }
        super.dispatchDraw(canvas);
    }

    public boolean isShownMenu() {
        return mLeftMenuOnScrren > 0.5;
    }


    public void toggle() {
        if (isShownMenu()) {
            closeDrawer();
        } else {
            openDrawer();
        }
    }

    public void closeDrawer() {
        View menuView = mLeftMenuView;
        mLeftMenuOnScrren = 0.f;
        pointY = getHeight() / 2;
        releasing = true;
        mFlowingView.resetContent();
        mHelper.smoothSlideViewTo(menuView, -menuView.getWidth(), menuView.getTop());
        postInvalidate();
    }

    public void openDrawer() {
        View menuView = mLeftMenuView;
        mLeftMenuOnScrren = 1.0f;
        pointY = getHeight() / 2;
        mHelper.smoothSlideViewTo(menuView, 0, menuView.getTop());
        postInvalidate();
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new MarginLayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    protected LayoutParams generateLayoutParams(LayoutParams p) {
        return new MarginLayoutParams(p);
    }


    protected void showShadow(float per) {
        if (mBg == null) {
            mBg = new ImageView(mContentView.getContext());
            mBg.setBackgroundColor(Color.argb(150, 20, 20, 20));
            ViewGroup.LayoutParams lp =
                    new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            mContentView.addView(mBg, lp);
        }
        ViewHelper.setAlpha(mBg, per);
        mBg.setClickable(per > 0);
    }

}
