
package com.mxn.soul.flowingdrawer_core;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by mxn on 16/7/13.
 */
public class FlowingDrawerLayout extends ViewGroup {

    private static final float TOUCH_SLOP_SENSITIVITY = 1.f;
    private static final int MIN_DRAWER_MARGIN = 0; // dp
    /**
     * Minimum velocity that will be detected as a fling
     */
    private static final int MIN_FLING_VELOCITY = 400; // dips per second
    /**
     * Length of time to delay before peeking the drawer.
     */
    private static final int PEEK_DELAY = 160; // ms

    private View mLeftMenuView;
    private ViewGroup mContentView;

    private final ViewDragHelper mLeftDragger;
    private final ViewDragCallback mLeftCallback;

    private int mMinDrawerMargin;
    private boolean mInLayout;
    private boolean mFirstLayout = true;
    private float mLeftMenuOnScrren;

    private float pointY;
    private float pointX;
    private FlowingView2 mFlowingView2;
    //返回动画是否结束
    private boolean releasing = false;

    private DrawerListener mListener;
    private static final int[] LAYOUT_ATTRS = new int[] {
            android.R.attr.layout_gravity
    };

    public FlowingDrawerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
        final float density = getResources().getDisplayMetrics().density;
        final float minVel = MIN_FLING_VELOCITY * density;
        mMinDrawerMargin = (int) (MIN_DRAWER_MARGIN * density + 0.5f);
        mLeftCallback = new ViewDragCallback(Gravity.LEFT);
        mLeftDragger = ViewDragHelper.create(this, TOUCH_SLOP_SENSITIVITY, mLeftCallback);
        mLeftDragger.setEdgeTrackingEnabled(ViewDragHelper.EDGE_LEFT);
        mLeftDragger.setMinVelocity(minVel);
        mLeftCallback.setDragger(mLeftDragger);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mFirstLayout = true;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mFirstLayout = true;
    }

    public void setMenuFragment(MenuFragment mMenuFragment) {
        mFlowingView2.setMenuFragment(mMenuFragment);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        if (widthMode != MeasureSpec.EXACTLY || heightMode != MeasureSpec.EXACTLY) {
            if (isInEditMode()) {
                // Don't crash the layout editor. Consume all of the space if specified
                // or pick a magic number from thin air otherwise.
                // It will crash on a real device.
                if (widthMode == MeasureSpec.AT_MOST) {
                    widthMode = MeasureSpec.EXACTLY;
                } else if (widthMode == MeasureSpec.UNSPECIFIED) {
                    widthMode = MeasureSpec.EXACTLY;
                    widthSize = 300;
                }
                if (heightMode == MeasureSpec.AT_MOST) {
                    heightMode = MeasureSpec.EXACTLY;
                } else if (heightMode == MeasureSpec.UNSPECIFIED) {
                    heightMode = MeasureSpec.EXACTLY;
                    heightSize = 300;
                }
            } else {
                throw new IllegalArgumentException(
                        "DrawerLayout must be measured with MeasureSpec.EXACTLY.");
            }
        }

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

        //        ViewGroup mLeftView = (ViewGroup) mLeftMenuView;
        //        View fragmentLayout = mLeftView.getChildAt(1);
        //        MarginLayoutParams mlp = (MarginLayoutParams) fragmentLayout.getLayoutParams();
        //        rightMargin = mlp.rightMargin;
        //        if (mFlowingView != null)
        //            mFlowingView.setRightMargin(rightMargin);

    }

    @Override
    protected void onLayout(boolean b, int i, int i1, int i2, int i3) {
        mInLayout = true;
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

        mInLayout = false;
        mFirstLayout = false;
    }

    @Override
    public void requestLayout() {
        if (!mInLayout) {
            super.requestLayout();
        }
    }

    private class ViewDragCallback extends ViewDragHelper.Callback {

        private final int mAbsGravity;
        private ViewDragHelper mDragger;

        public ViewDragCallback(int gravity) {
            mAbsGravity = gravity;
        }

        public void setDragger(ViewDragHelper dragger) {
            mDragger = dragger;
        }

        public void removeCallbacks() {
        }

        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return child == mLeftMenuView;
        }

        @Override
        public void onViewDragStateChanged(int state) {
        }


        // left  -735 --- 0
        // childWidth  735
        // offset  0 -1
        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {

            final int childWidth = changedView.getWidth();
            float offset = (float) (childWidth + left) / childWidth;
//            Log.e("======",left +"====="+childWidth+"===="+offset ) ;
            mLeftMenuOnScrren = offset;
            Log.e("======", "====="+mLeftMenuOnScrren+"====" ) ;
            setDrawerViewOffset(changedView, offset);
            changedView.setVisibility(offset == 0 ? View.INVISIBLE : View.VISIBLE);

            if(!isForbidManual()) {
                mFlowingView2.show(pointX, pointY, FlowingView2.STATUS_OPEN_MANUAL);
            }
//            rightX = left + childWidth;
//            if (mFlowingView.isStartAuto(rightX)) {
//                mFlowingView.autoUpping(rightX);
//                if (rightX == 0) {
//                    mFlowingView.resetStatus();
//                    releasing = false;
//                }
//                return;
//            }
//
//            if (mFlowingView.isupping()) {
//                if (rightX == 0) {
//                    mFlowingView.resetStatus();
//                    releasing = false;
//                }
//                return;
//            }
//            if (!releasing) {
//                mFlowingView.show(rightX, pointY, FlowingView.Status.STATUS_UP);
//            } else {
//                mFlowingView.show(rightX, pointY, FlowingView.Status.STATUS_DOWN);
//                if (rightX == 0) {
//                    mFlowingView.resetStatus();
//                    releasing = false;
//                }
//            }
            invalidate();
        }

        @Override
        public void onViewCaptured(View capturedChild, int activePointerId) {

        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
//            final int childWidth = releasedChild.getWidth();
//            float offset = (childWidth + releasedChild.getLeft()) * 1.0f / childWidth;
//            boolean openMark = xvel > 0 || xvel == 0 && offset > 0.5f;
//            mLeftDragger.settleCapturedViewAt(openMark ? 0 : -childWidth, releasedChild.getTop());
//            if (!openMark) {
//                releasing = true;
//                mFlowingView.resetContent();
//            }
//            invalidate();
        }

        @Override
        public void onEdgeTouched(int edgeFlags, int pointerId) {
        }

        @Override
        public boolean onEdgeLock(int edgeFlags) {
            return false;
        }

        @Override
        public void onEdgeDragStarted(int edgeFlags, int pointerId) {
            mDragger.captureChildView(mLeftMenuView, pointerId);
        }

        @Override
        public int getViewHorizontalDragRange(View child) {
            return mLeftMenuView == child ? child.getWidth() : 0;
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            return Math.max(-child.getWidth(), Math.min(left, 0));
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            return child.getTop();
        }
    }

    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new MarginLayoutParams(p);
    }

    /**
     * 是否禁止手动移动
     */
    public boolean isForbidManual() {
        return mLeftMenuOnScrren >= 0.5;
    }

    public void toggle() {
        if (isForbidManual()) {
            closeDrawer();
        } else {
            openDrawer();
        }
    }

    public void closeDrawer() {
        View menuView = mLeftMenuView;
        mLeftMenuOnScrren = 0.f;
        pointY = getHeight() / 2;
        //        releasing = true;
        //        mFlowingView.resetContent();
        mLeftDragger.smoothSlideViewTo(menuView, -menuView.getWidth(), menuView.getTop());
        postInvalidate();
    }

    public void openDrawer() {
        View menuView = mLeftMenuView;
        mLeftMenuOnScrren = 1.0f;
        pointY = getHeight() / 2;
        mLeftDragger.smoothSlideViewTo(menuView, 0, menuView.getTop());
        postInvalidate();
    }

    @Override
    protected void dispatchDraw(@NonNull Canvas canvas) {
        PaintFlagsDrawFilter pfd = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint
                .FILTER_BITMAP_FLAG);
        canvas.setDrawFilter(pfd);
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child == null) {
                return;
            }
        }
        super.dispatchDraw(canvas);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mLeftDragger.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        if (isForbidManual() ) {
            if(mFlowingView2.getCurrentStatus() == FlowingView2.STATUS_OPEN_MANUAL){
                /**
                 * 手动转自动
                 */
                mFlowingView2.show(pointX, pointY, FlowingView2.STATUS_OPEN_AUTO);
                Log.e("======","=====false");
            }
            return false;
        }
        mLeftDragger.processTouchEvent(event);
        pointY = event.getY();
        pointX = event.getX();
        return true;
    }

    @Override
    public void computeScroll() {
        if (mLeftDragger.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    public void setFluidView(FlowingView2 mFlowingView2) {
        this.mFlowingView2 = mFlowingView2;
        //        mFlowingView.setRightMargin(rightMargin);
        ViewCompat.setLayerType(this, ViewCompat.LAYER_TYPE_NONE, null);
                final int childCount = getChildCount();
                for (int i = 0; i < childCount; i++) {
                    ViewCompat.setLayerType(getChildAt(i), ViewCompat.LAYER_TYPE_NONE,
                            null);
                }
    }


    void setDrawerViewOffset(View drawerView, float slideOffset) {
       //TODO
//        final LayoutParams lp = (LayoutParams) drawerView.getLayoutParams();
//        if (slideOffset == lp.onScreen) {
//            return;
//        }
//
//        lp.onScreen = slideOffset;
        dispatchOnDrawerSlide(drawerView, slideOffset);
    }

    void dispatchOnDrawerSlide(View drawerView, float slideOffset) {
        if (mListener != null) {
            mListener.onDrawerSlide(drawerView, slideOffset);
        }
    }


    /**
     * Listener for monitoring events about drawers.
     */
    public interface DrawerListener {
        /**
         * Called when a drawer's position changes.
         * @param drawerView The child view that was moved
         * @param slideOffset The new offset of this drawer within its range, from 0-1
         */
        public void onDrawerSlide(View drawerView, float slideOffset);

        /**
         * Called when a drawer has settled in a completely open state.
         * The drawer is interactive at this point.
         *
         * @param drawerView Drawer view that is now open
         */
        public void onDrawerOpened(View drawerView);

        /**
         * Called when a drawer has settled in a completely closed state.
         *
         * @param drawerView Drawer view that is now closed
         */
        public void onDrawerClosed(View drawerView);

        /**
         * Called when the drawer motion state changes. The new state will
         * be one of {@link #STATE_IDLE}, {@link #STATE_DRAGGING} or {@link #STATE_SETTLING}.
         *
         * @param newState The new drawer motion state
         */
        public void onDrawerStateChanged(@State int newState);
    }

    @IntDef({STATE_IDLE, STATE_DRAGGING, STATE_SETTLING})
    @Retention(RetentionPolicy.SOURCE)
    private @interface State {}
    /**
     * Indicates that any drawers are in an idle, settled state. No animation is in progress.
     */
    public static final int STATE_IDLE = ViewDragHelper.STATE_IDLE;

    /**
     * Indicates that a drawer is currently being dragged by the user.
     */
    public static final int STATE_DRAGGING = ViewDragHelper.STATE_DRAGGING;

    /**
     * Indicates that a drawer is in the process of settling to a final position.
     */
    public static final int STATE_SETTLING = ViewDragHelper.STATE_SETTLING;


    public static class LayoutParams extends ViewGroup.MarginLayoutParams {

        public int gravity = Gravity.NO_GRAVITY;
        float onScreen;
        boolean isPeeking;
        boolean knownOpen;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);

            final TypedArray a = c.obtainStyledAttributes(attrs, LAYOUT_ATTRS);
            this.gravity = a.getInt(0, Gravity.NO_GRAVITY);
            a.recycle();
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(int width, int height, int gravity) {
            this(width, height);
            this.gravity = gravity;
        }

        public LayoutParams(LayoutParams source) {
            super(source);
            this.gravity = source.gravity;
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }

        public LayoutParams(ViewGroup.MarginLayoutParams source) {
            super(source);
        }
    }
}
