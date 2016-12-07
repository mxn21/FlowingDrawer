/*
 * Copyright (C) 2016 Baidu, Inc. All Rights Reserved.
 */
package com.mxn.soul.flowingdrawer_core;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.Scroller;

/**
 * Created by mxn on 2016/10/15.
 */
public abstract class ElasticDrawer extends ViewGroup {

    /**
     * Tag used when logging.
     */
    private static final String TAG = "DraggableDrawer";

    /**
     * Indicates whether debug code should be enabled.
     */
    private static final boolean DEBUG = false;
    /**
     * The size of the menu (width or height depending on the gravity).
     */
    protected int mMenuSize;
    /**
     * Defines whether the drop shadow is enabled.
     */
    protected boolean mShadowEnabled;
    /**
     * Drawable used as content drop shadow onto the menu.
     */
    protected Drawable mDropShadowDrawable;
    /**
     * The color of the drop shadow.
     */
    protected int mShadowColor;


    private boolean mCustomShadowEnabled;
    /**
     * The size of the content drop shadow.
     */
    protected int mShadowSize;
    /**
     * The touch bezel size of the drawer in px.
     */
    protected int mTouchBezelSize;

    /**
     * Whether an overlay should be drawn as the drawer is opened and closed.
     */
    protected boolean mDrawOverlay;

    /**
     * Interpolator used when animating the drawer open/closed.
     */
    protected static final Interpolator SMOOTH_INTERPOLATOR = new SmoothInterpolator();

    /**
     * Slop before starting a drag.
     */
    protected int mTouchSlop;
    /**
     * Maximum velocity allowed when animating the drawer open/closed.
     */
    protected int mMaxVelocity;
    /**
     * Scroller used when animating the drawer open/closed.
     */
    private Scroller mScroller;
    /**
     * Scroller used for the peek drawer animation.
     */
    protected Scroller mPeekScroller;
    /**
     * Interpolator used for peeking at the drawer.
     */
    private static final Interpolator PEEK_INTERPOLATOR = new PeekInterpolator();
    /**
     * Distance in px from closed position from where the drawer is considered closed with regards to touch events.
     */
    protected int mCloseEnough;

    /**
     * The position of the drawer.
     */
    private int mPosition;
    /**
     * Touch mode for the Drawer.
     * Possible values are {@link #TOUCH_MODE_NONE}, {@link #TOUCH_MODE_BEZEL} or {@link #TOUCH_MODE_FULLSCREEN}
     * Default: {@link #TOUCH_MODE_BEZEL}
     */
    protected int mTouchMode = TOUCH_MODE_BEZEL;
    /**
     * The touch area size of the drawer in px.
     */
    protected int mTouchSize;

    /**
     * The parent of the menu view.
     */
    protected BuildLayerFrameLayout mMenuContainer;

    /**
     * The parent of the content view.
     */
    protected BuildLayerFrameLayout mContentContainer;
    /**
     * The custom menu view set by the user.
     */
    private View mMenuView;
    /**
     * Current offset.
     */
    protected float mOffsetPixels;


    /**
     * The default drop shadow size in dp.
     */
    private static final int DEFAULT_DROP_SHADOW_DP = 6;
    /**
     * The default touch bezel size of the drawer in dp.
     */
    private static final int DEFAULT_DRAG_BEZEL_DP = 24;
    /**
     * Distance in dp from closed position from where the drawer is considered closed with regards to touch events.
     */
    private static final int CLOSE_ENOUGH = 3;
    /**
     * Disallow opening the drawer by dragging the screen.
     */
    public static final int TOUCH_MODE_NONE = 0;
    /**
     * Allow opening drawer only by dragging on the edge of the screen.
     */
    public static final int TOUCH_MODE_BEZEL = 1;

    /**
     * Allow opening drawer by dragging anywhere on the screen.
     */
    public static final int TOUCH_MODE_FULLSCREEN = 2;


    public ElasticDrawer(Context context) {
        super(context);
    }

    public ElasticDrawer(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.elasticDrawerStyle);
    }

    public ElasticDrawer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initDrawer(context, attrs, defStyle);
    }

    protected void initDrawer(Context context, AttributeSet attrs, int defStyle) {
        setWillNotDraw(false);
        setFocusable(false);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ElasticDrawer, R.attr.elasticDrawerStyle,
                R.style.MenuDrawerStyle);

        final Drawable menuBackground = a.getDrawable(R.styleable.ElasticDrawer_edMenuBackground);

        mMenuSize = a.getDimensionPixelSize(R.styleable.ElasticDrawer_edMenuSize, dpToPx(240));

        mShadowEnabled = a.getBoolean(R.styleable.ElasticDrawer_edShadowEnabled, true);

        mDropShadowDrawable = a.getDrawable(R.styleable.ElasticDrawer_edShadowDrawable);

        if (mDropShadowDrawable == null) {
            mShadowColor = a.getColor(R.styleable.ElasticDrawer_edShadowColor, 0xFF000000);
        } else {
            mCustomShadowEnabled = true;
        }

        mShadowSize = a.getDimensionPixelSize(R.styleable.ElasticDrawer_edShadowSize,
                dpToPx(DEFAULT_DROP_SHADOW_DP));

        mTouchBezelSize = a.getDimensionPixelSize(R.styleable.ElasticDrawer_edTouchBezelSize,
                dpToPx(DEFAULT_DRAG_BEZEL_DP));

        mDrawOverlay = a.getBoolean(R.styleable.ElasticDrawer_edDrawOverlay, true);

        a.recycle();


        mMenuContainer = new NoClickThroughFrameLayout(context);
        mMenuContainer.setId(R.id.md__menu);
        mMenuContainer.setBackgroundDrawable(menuBackground);

        mContentContainer = new NoClickThroughFrameLayout(context);
        mContentContainer.setId(R.id.md__content);

        final ViewConfiguration configuration = ViewConfiguration.get(context);
        mTouchSlop = configuration.getScaledTouchSlop();
        mMaxVelocity = configuration.getScaledMaximumFlingVelocity();

        mScroller = new Scroller(context, SMOOTH_INTERPOLATOR);
        mPeekScroller = new Scroller(context, PEEK_INTERPOLATOR);

        mCloseEnough = dpToPx(CLOSE_ENOUGH);
    }


    protected int dpToPx(int dp) {
        return (int) (getResources().getDisplayMetrics().density * dp + 0.5f);
    }

    /**
     * Callback interface for changing state of the drawer.
     */
    public interface OnDrawerStateChangeListener {

        /**
         * Called when the drawer state changes.
         *
         * @param oldState The old drawer state.
         * @param newState The new drawer state.
         */
        void onDrawerStateChange(int oldState, int newState);

        /**
         * Called when the drawer slides.
         *
         * @param openRatio    Ratio for how open the menu is.
         * @param offsetPixels Current offset of the menu in pixels.
         */
        void onDrawerSlide(float openRatio, int offsetPixels);
    }

    /**
     * Callback that is invoked when the drawer is in the process of deciding whether it should intercept the touch
     * event. This lets the listener decide if the pointer is on a view that would disallow dragging of the drawer.
     * This is only called when the touch mode is {@link #TOUCH_MODE_FULLSCREEN}.
     */
    public interface OnInterceptMoveEventListener {

        /**
         * Called for each child the pointer i on when the drawer is deciding whether to intercept the touch event.
         *
         * @param v     View to test for draggability
         * @param delta Delta drag in pixels
         * @param x     X coordinate of the active touch point
         * @param y     Y coordinate of the active touch point
         * @return true if view is draggable by delta dx.
         */
        boolean isViewDraggable(View v, int delta, int x, int y);
    }


    protected int getPosition() {
        return mPosition;
    }


    class Position {
        public static final int LEFT = 0 ;
        public static final int RIGHT = 1 ;
    }

    protected void updateTouchAreaSize() {
        if (mTouchMode == TOUCH_MODE_BEZEL) {
            mTouchSize = mTouchBezelSize;
        } else if (mTouchMode == TOUCH_MODE_FULLSCREEN) {
            mTouchSize = getMeasuredWidth();
        } else {
            mTouchSize = 0;
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        if (getChildCount() != 2) {
            throw new IllegalStateException(
                    "child count isn't equal to 2 , content and Menu view must be added in xml .");
        }
        View content = getChildAt(0);
        if (content != null) {
            removeView(content);
            mContentContainer.removeAllViews();
            mContentContainer.addView(content, new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        } else {
            throw new IllegalStateException(
                    "content and Menu view must be added in xml .");
        }
        View menu = getChildAt(1);
        if (menu != null) {
            removeView(menu);
            mMenuView = menu;
            mMenuContainer.removeAllViews();
            mMenuContainer.addView(menu, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        } else {
            throw new IllegalStateException(
                    "content and Menu view must be added in xml .");
        }
    }


    /**
     * Animates the menu open.
     */
    public void openMenu() {
        openMenu(true);
    }

    /**
     * Opens the menu.
     *
     * @param animate Whether open/close should be animated.
     */
    public abstract void openMenu(boolean animate);

    /**
     * Animates the menu closed.
     */
    public void closeMenu() {
        closeMenu(true);
    }

    /**
     * Closes the menu.
     *
     * @param animate Whether open/close should be animated.
     */
    public abstract void closeMenu(boolean animate);


    /**
     * Moves the drawer to the position passed.
     *
     * @param position The position the content is moved to.
     * @param velocity Optional velocity if called by releasing a drag event.
     * @param animate  Whether the move is animated.
     */
    protected void animateOffsetTo(int position, int velocity, boolean animate) {
        endDrag();
        endPeek();

        final int startX = (int) mOffsetPixels;
        final int dx = position - startX;
        if (dx == 0 || !animate) {
            setOffsetPixels(position);
            setDrawerState(position == 0 ? STATE_CLOSED : STATE_OPEN);
            stopLayerTranslation();
            return;
        }

        int duration;

        velocity = Math.abs(velocity);
        if (velocity > 0) {
            duration = 4 * Math.round(1000.f * Math.abs((float) dx / velocity));
        } else {
            duration = (int) (600.f * Math.abs((float) dx / mMenuSize));
        }

        duration = Math.min(duration, mMaxAnimationDuration);
        animateOffsetTo(position, duration);
    }

}
