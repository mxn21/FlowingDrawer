/*
 * Copyright (C) 2016 Baidu, Inc. All Rights Reserved.
 */
package com.mxn.soul.flowingdrawer_core;


import static com.mxn.soul.flowingdrawer_core.FlowingDrawer.USE_TRANSLATIONS;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.Interpolator;
import android.widget.Scroller;

/**
 * Created by mxn on 2016/10/15.
 *
 */
public abstract class ElasticDrawer extends ViewGroup {

    /**
     * Tag used when logging.
     */
    private static final String TAG = "ElasticDrawer";

    /**
     * Indicates whether debug code should be enabled.
     */
    private static final boolean DEBUG = false;
    /**
     * The time between each frame when animating the drawer.
     */
    protected static final int ANIMATION_DELAY = 1000 / 60;


    /**
     * Indicates whether the menu is currently visible.
     */
    protected boolean mMenuVisible;
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
    /**
     * Defines whether the drop shadow is enabled.
     */
    protected boolean mDropShadowEnabled;

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
     * Indicates whether the current layer type is {@link android.view.View#LAYER_TYPE_HARDWARE}.
     */
    protected boolean mLayerTypeHardware;
    /**
     * Indicates whether to use {@link View#LAYER_TYPE_HARDWARE} when animating the drawer.
     */
    protected boolean mHardwareLayersEnabled = true;

    /**
     * Default delay between each subsequent animation, after {@link #peekDrawer()} has been called.
     */
    protected long mPeekDelay;

    /**
     * Scroller used for the peek drawer animation.
     */
    protected Scroller mPeekScroller;
    /**
     * Velocity tracker used when animating the drawer open/closed after a drag.
     */
    protected VelocityTracker mVelocityTracker;
    /**
     * Interpolator used for peeking at the drawer.
     */
    private static final Interpolator PEEK_INTERPOLATOR = new PeekInterpolator();

    /**
     * Indicates whether the menu should be offset when dragging the drawer.
     */
    protected boolean mOffsetMenu = true;

    /**
     * Distance in px from closed position from where the drawer is considered closed with regards to touch events.
     */
    protected int mCloseEnough;

    /**
     * The position of the drawer.
     */
    private int mPosition;

    private int mResolvedPosition;
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
     * Whether an overlay should be drawn as the drawer is opened and closed.
     */
    protected boolean mDrawOverlay;


    protected final Rect mDropShadowRect = new Rect();

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

    /**
     * Listener used to dispatch state change events.
     */
    private OnDrawerStateChangeListener mOnDrawerStateChangeListener;
    /**
     * Callback that lets the listener override intercepting of touch events.
     */
    protected OnInterceptMoveEventListener mOnInterceptMoveEventListener;

    /**
     * The maximum animation duration.
     */
    private static final int DEFAULT_ANIMATION_DURATION = 600;
    /**
     * The maximum duration of open/close animations.
     */
    protected int mMaxAnimationDuration = DEFAULT_ANIMATION_DURATION;
    /**
     * Indicates that the drawer is currently closed.
     */
    public static final int STATE_CLOSED = 0;

    /**
     * Indicates that the drawer is currently closing.
     */
    public static final int STATE_CLOSING = 1;

    /**
     * Indicates that the drawer is currently being dragged by the user.
     */
    public static final int STATE_DRAGGING = 2;

    /**
     * Indicates that the drawer is currently opening.
     */
    public static final int STATE_OPENING = 4;

    /**
     * Indicates that the drawer is currently open.
     */
    public static final int STATE_OPEN = 8;

    /**
     * The current drawer state.
     *
     * @see #STATE_CLOSED
     * @see #STATE_CLOSING
     * @see #STATE_DRAGGING
     * @see #STATE_OPENING
     * @see #STATE_OPEN
     */
    protected int mDrawerState = STATE_CLOSED;

    /**
     * Bundle used to hold the drawers state.
     */
    protected Bundle mState;

    /**
     * Key used when saving menu visibility state.
     */
    private static final String STATE_MENU_VISIBLE = "ElasticDrawer.menuVisible";
    /**
     * Indicates whether the drawer is currently being dragged.
     */
    protected boolean mIsDragging;
    /**
     * The current pointer id.
     */
    protected int mActivePointerId = INVALID_POINTER;

    public static final int INVALID_POINTER = -1;

    /**
     * Default delay from {@link #peekDrawer()} is called until first animation is run.
     */
    private static final long DEFAULT_PEEK_START_DELAY = 5000;
    /**
     * Default delay between each subsequent animation, after {@link #peekDrawer()} has been called.
     */
    private static final long DEFAULT_PEEK_DELAY = 10000;

    /**
     * Runnable used for first call to {@link #startPeek()} after {@link #peekDrawer()}  has been called.
     */
    private Runnable mPeekStartRunnable;

    /**
     * Runnable used when the peek animation is running.
     */
    protected final Runnable mPeekRunnable = new Runnable() {
        @Override
        public void run() {
            peekDrawerInvalidate();
        }
    };
    /**
     * Runnable used when animating the drawer open/closed.
     */
    private final Runnable mDragRunnable = new Runnable() {
        @Override
        public void run() {
            postAnimationInvalidate();
        }
    };

    protected boolean mIsPeeking;



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

        mMaxAnimationDuration = a.getInt(R.styleable.ElasticDrawer_edMaxAnimationDuration, DEFAULT_ANIMATION_DURATION);

        mDrawOverlay = a.getBoolean(R.styleable.ElasticDrawer_edDrawOverlay, true);

        final int position = a.getInt(R.styleable.ElasticDrawer_edPosition, 0);
        setPosition(position);

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

    protected boolean isViewDescendant(View v) {
        ViewParent parent = v.getParent();
        while (parent != null) {
            if (parent == this) {
                return true;
            }
            parent = parent.getParent();
        }
        return false;
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

    class Position {
        // Positions the drawer to the left of the content.
        static final int LEFT = 1 ;
        // Positions the drawer to the right of the content.
        static final int RIGHT = 2 ;
        /**
         * Position the drawer at the start edge. This will position the drawer to the {@link #LEFT} with LTR languages and
         * {@link #RIGHT} with RTL languages.
         */
        static final int START = 3 ;
        /**
         * Position the drawer at the end edge. This will position the drawer to the {@link #RIGHT} with LTR languages and
         * {@link #LEFT} with RTL languages.
         */
        static final int END = 4 ;
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
                    "content view must be added in xml .");
        }
        View menu = getChildAt(1);
        if (menu != null) {
            removeView(menu);
            mMenuView = menu;
            mMenuContainer.removeAllViews();
            mMenuContainer.addView(menu, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        } else {
            throw new IllegalStateException(
                    "menu view must be added in xml .");
        }
    }

    /**
     * Called when the number of pixels the content should be offset by has changed.
     *
     * @param offsetPixels The number of pixels to offset the content by.
     */
    protected abstract void onOffsetPixelsChanged(int offsetPixels);
    /**
     * Toggles the menu open and close with animation.
     */
    public void toggleMenu() {
        toggleMenu(true);
    }

    /**
     * Toggles the menu open and close.
     *
     * @param animate Whether open/close should be animated.
     */
    public void toggleMenu(boolean animate) {
        if (mDrawerState == STATE_OPEN || mDrawerState == STATE_OPENING) {
            closeMenu(animate);
        } else if (mDrawerState == STATE_CLOSED || mDrawerState == STATE_CLOSING) {
            openMenu(animate);
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
     * Indicates whether the menu is currently visible.
     *
     * @return True if the menu is open, false otherwise.
     */
    public boolean isMenuVisible() {
        return mMenuVisible;
    }


    /**
     * Set the size of the menu drawer when open.
     *
     * @param size The size of the menu.
     */
    public void setMenuSize(final int size) {
        mMenuSize = size;
        if (mDrawerState == STATE_OPEN || mDrawerState == STATE_OPENING) {
            setOffsetPixels(mMenuSize);
        }
        requestLayout();
        invalidate();
    }

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

    protected void animateOffsetTo(int position, int duration) {
        final int startX = (int) mOffsetPixels;
        final int dx = position - startX;

        if (dx > 0) {
            setDrawerState(STATE_OPENING);
            mScroller.startScroll(startX, 0, dx, 0, duration);
        } else {
            setDrawerState(STATE_CLOSING);
            mScroller.startScroll(startX, 0, dx, 0, duration);
        }
        startLayerTranslation();
        postAnimationInvalidate();
    }

    /**
     * Sets the number of pixels the content should be offset.
     *
     * @param offsetPixels The number of pixels to offset the content by.
     */
    protected void setOffsetPixels(float offsetPixels) {
        final int oldOffset = (int) mOffsetPixels;
        final int newOffset = (int) offsetPixels;

        mOffsetPixels = offsetPixels;

        if (newOffset != oldOffset) {
            onOffsetPixelsChanged(newOffset);
            mMenuVisible = newOffset != 0;

            // Notify any attached listeners of the current open ratio
            final float openRatio = ((float) Math.abs(newOffset)) / mMenuSize;
            dispatchOnDrawerSlide(openRatio, newOffset);
        }
    }
    @Override
    public void onRtlPropertiesChanged(int layoutDirection) {
        super.onRtlPropertiesChanged(layoutDirection);

        if (!mCustomShadowEnabled) setDropShadowColor(mShadowColor);

        if (getPosition() != mResolvedPosition) {
            mResolvedPosition = getPosition();
            setOffsetPixels(mOffsetPixels * -1);
        }

        requestLayout();
        invalidate();
    }

    private void setPosition(int position) {
        mPosition = position;
        mResolvedPosition = getPosition();
    }

    protected int getPosition() {
        final int layoutDirection = ViewHelper.getLayoutDirection(this);

        switch (mPosition) {
            case Position.START:
                if (layoutDirection == LAYOUT_DIRECTION_RTL) {
                    return Position.RIGHT;
                } else {
                    return Position.LEFT;
                }

            case Position.END:
                if (layoutDirection == LAYOUT_DIRECTION_RTL) {
                    return Position.LEFT;
                } else {
                    return Position.RIGHT;
                }
        }

        return mPosition;
    }

    /**
     * Register a callback to be invoked when the drawer state changes.
     *
     * @param listener The callback that will run.
     */
    public void setOnDrawerStateChangeListener(OnDrawerStateChangeListener listener) {
        mOnDrawerStateChangeListener = listener;
    }
    /**
     * Register a callback that will be invoked when the drawer is about to intercept touch events.
     *
     * @param listener The callback that will be invoked.
     */
    public void setOnInterceptMoveEventListener(OnInterceptMoveEventListener listener) {
        mOnInterceptMoveEventListener = listener;
    }


    /**
     * Animates the drawer slightly open until the user opens the drawer.
     */
    public void peekDrawer() {
        peekDrawer(DEFAULT_PEEK_START_DELAY, DEFAULT_PEEK_DELAY);
    }

    /**
     * Animates the drawer slightly open. If delay is larger than 0, this happens until the user opens the drawer.
     *
     * @param delay The delay (in milliseconds) between each run of the animation. If 0, this animation is only run
     *              once.
     */
    public void peekDrawer(long delay) {
        peekDrawer(DEFAULT_PEEK_START_DELAY, delay);
    }

    /**
     * Animates the drawer slightly open. If delay is larger than 0, this happens until the user opens the drawer.
     *
     * @param startDelay The delay (in milliseconds) until the animation is first run.
     * @param delay      The delay (in milliseconds) between each run of the animation. If 0, this animation is only run
     *                   once.
     */
    public void peekDrawer(final long startDelay, final long delay) {
        if (startDelay < 0) {
            throw new IllegalArgumentException("startDelay must be zero or larger.");
        }
        if (delay < 0) {
            throw new IllegalArgumentException("delay must be zero or larger");
        }
        removeCallbacks(mPeekRunnable);
        removeCallbacks(mPeekStartRunnable);
        mPeekDelay = delay;
        mPeekStartRunnable = new Runnable() {
            @Override
            public void run() {
                startPeek();
            }
        };
        postDelayed(mPeekStartRunnable, startDelay);
    }

    /**
     * Returns the ViewGroup used as a parent for the menu view.
     *
     * @return The menu view's parent.
     */
    /**
     * Sets the maximum duration of open/close animations.
     *
     * @param duration The maximum duration in milliseconds.
     */
    public void setMaxAnimationDuration(int duration) {
        mMaxAnimationDuration = duration;
    }

    /**
     * Sets whether an overlay should be drawn when sliding the drawer.
     *
     * @param drawOverlay Whether an overlay should be drawn when sliding the drawer.
     */
    public void setDrawOverlay(boolean drawOverlay) {
        mDrawOverlay = drawOverlay;
    }

    /**
     * Gets whether an overlay is drawn when sliding the drawer.
     *
     * @return Whether an overlay is drawn when sliding the drawer.
     */
    public boolean getDrawOverlay() {
        return mDrawOverlay;
    }

    public ViewGroup getMenuContainer() {
        return mMenuContainer;
    }



    /**
     * Returns the ViewGroup used as a parent for the content view.
     *
     * @return The content view's parent.
     */
    public ViewGroup getContentContainer() {
        return mContentContainer;
    }

    /**
     * Get the current state of the drawer.
     *
     * @return The state of the drawer.
     */
    public int getDrawerState() {
        return mDrawerState;
    }

    /**
     * Enables or disables offsetting the menu when dragging the drawer.
     *
     * @param offsetMenu True to offset the menu, false otherwise.
     */
    public void setOffsetMenuEnabled(boolean offsetMenu) {
        if (offsetMenu != mOffsetMenu) {
            mOffsetMenu = offsetMenu;
            requestLayout();
            invalidate();
        }
    }

    /**
     * Indicates whether the menu is being offset when dragging the drawer.
     *
     * @return True if the menu is being offset, false otherwise.
     */
    public boolean getOffsetMenuEnabled() {
        return mOffsetMenu;
    }



    protected void setDrawerState(int state) {
        if (state != mDrawerState) {
            final int oldState = mDrawerState;
            mDrawerState = state;
            if (mOnDrawerStateChangeListener != null) mOnDrawerStateChangeListener.onDrawerStateChange(oldState, state);
            if (DEBUG) logDrawerState(state);
        }
    }

    protected void logDrawerState(int state) {
        switch (state) {
            case STATE_CLOSED:
                Log.d(TAG, "[DrawerState] STATE_CLOSED");
                break;

            case STATE_CLOSING:
                Log.d(TAG, "[DrawerState] STATE_CLOSING");
                break;

            case STATE_DRAGGING:
                Log.d(TAG, "[DrawerState] STATE_DRAGGING");
                break;

            case STATE_OPENING:
                Log.d(TAG, "[DrawerState] STATE_OPENING");
                break;

            case STATE_OPEN:
                Log.d(TAG, "[DrawerState] STATE_OPEN");
                break;
            default:
                Log.d(TAG, "[DrawerState] Unknown: " + state);
        }
    }

    /**
     * Sets the drawer touch mode. Possible values are {@link #TOUCH_MODE_NONE}, {@link #TOUCH_MODE_BEZEL} or
     * {@link #TOUCH_MODE_FULLSCREEN}.
     *
     * @param mode The touch mode.
     */
    public void setTouchMode(int mode) {
        if (mTouchMode != mode) {
            mTouchMode = mode;
            updateTouchAreaSize();
        }
    }

    public int getTouchMode() {
        return mTouchMode;
    }


    @Override
    public void postOnAnimation(Runnable action) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            super.postOnAnimation(action);
        } else {
            postDelayed(action, ANIMATION_DELAY);
        }
    }

    protected void dispatchOnDrawerSlide(float openRatio, int offsetPixels) {
        if (mOnDrawerStateChangeListener != null) {
            mOnDrawerStateChangeListener.onDrawerSlide(openRatio, offsetPixels);
        }
    }


    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        final int offsetPixels = (int) mOffsetPixels;

        if (mDrawOverlay && offsetPixels != 0) {
            drawOverlay(canvas);
        }
        if (mDropShadowEnabled && offsetPixels != 0 ) {
            drawDropShadow(canvas);
        }
    }

    protected abstract void drawOverlay(Canvas canvas);

    private void drawDropShadow(Canvas canvas) {
        // Can't pass the position to the constructor, so wait with loading the drawable until the drop shadow is
        // actually drawn.
        if (mDropShadowDrawable == null) {
            setDropShadowColor(mShadowColor);
        }
        updateDropShadowRect();
        mDropShadowDrawable.setBounds(mDropShadowRect);
        mDropShadowDrawable.draw(canvas);
    }

    /**
     * Sets the drawable of the drop shadow.
     *
     * @param drawable The drawable of the drop shadow.
     */
    public void setDropShadow(Drawable drawable) {
        mDropShadowDrawable = drawable;
        mCustomShadowEnabled = drawable != null;
        invalidate();
    }
    /**
     * Sets the color of the drop shadow.
     *
     * @param color The color of the drop shadow.
     */
    public void setDropShadowColor(int color) {
        GradientDrawable.Orientation orientation = getDropShadowOrientation();

        final int endColor = color & 0x00FFFFFF;
        mDropShadowDrawable = new GradientDrawable(orientation,
                new int[] {
                        color,
                        endColor,
                });
        invalidate();
    }
    /**
     * Defines whether the drop shadow is enabled.
     *
     * @param enabled Whether the drop shadow is enabled.
     */
    public void setDropShadowEnabled(boolean enabled) {
        mDropShadowEnabled = enabled;
        invalidate();
    }

    protected abstract void updateDropShadowRect() ;

    protected abstract GradientDrawable.Orientation getDropShadowOrientation() ;

    /**
     * Saves the state of the drawer.
     *
     * @return Returns a Parcelable containing the drawer state.
     */
    public final Parcelable saveState() {
        if (mState == null) mState = new Bundle();
        saveState(mState);
        return mState;
    }

    void saveState(Bundle state) {
        final boolean menuVisible = mDrawerState == STATE_OPEN || mDrawerState == STATE_OPENING;
        state.putBoolean(STATE_MENU_VISIBLE, menuVisible);
    }

    /**
     * Restores the state of the drawer.
     *
     * @param in A parcelable containing the drawer state.
     */
    public void restoreState(Parcelable in) {
        mState = (Bundle) in;
        final boolean menuOpen = mState.getBoolean(STATE_MENU_VISIBLE);
        if (menuOpen) {
            openMenu(false);
        } else {
            setOffsetPixels(0);
        }
        mDrawerState = menuOpen ? STATE_OPEN : STATE_CLOSED;
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState state = new SavedState(superState);

        if (mState == null) mState = new Bundle();
        saveState(mState);

        state.mState = mState;
        return state;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());

        restoreState(savedState.mState);
    }

    static class SavedState extends BaseSavedState {

        Bundle mState;

        SavedState(Parcelable superState) {
            super(superState);
        }

        SavedState(Parcel in) {
            super(in);
            mState = in.readBundle();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeBundle(mState);
        }

        @SuppressWarnings("UnusedDeclaration")
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    protected float getXVelocity(VelocityTracker velocityTracker) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            return velocityTracker.getXVelocity(mActivePointerId);
        }

        return velocityTracker.getXVelocity();
    }

    protected float getYVelocity(VelocityTracker velocityTracker) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            return velocityTracker.getYVelocity(mActivePointerId);
        }

        return velocityTracker.getYVelocity();
    }

    protected boolean canChildrenScroll(int dx, int dy, int x, int y) {
        boolean canScroll = false;

        switch (getPosition()) {
            case Position.LEFT:
            case Position.RIGHT:
                if (!mMenuVisible) {
                    canScroll = canChildScrollHorizontally(mContentContainer, false, dx,
                            x - ViewHelper.getLeft(mContentContainer), y - ViewHelper.getTop(mContentContainer));
                } else {
                    canScroll = canChildScrollHorizontally(mMenuContainer, false, dx,
                            x - ViewHelper.getLeft(mMenuContainer), y - ViewHelper.getTop(mContentContainer));
                }
                break;
        }

        return canScroll;
    }

    /**
     * Tests scrollability within child views of v given a delta of dx.
     *
     * @param v      View to test for horizontal scrollability
     * @param checkV Whether the view should be checked for draggability
     * @param dx     Delta scrolled in pixels
     * @param x      X coordinate of the active touch point
     * @param y      Y coordinate of the active touch point
     * @return true if child views of v can be scrolled by delta of dx.
     */
    protected boolean canChildScrollHorizontally(View v, boolean checkV, int dx, int x, int y) {
        if (v instanceof ViewGroup) {
            final ViewGroup group = (ViewGroup) v;

            final int count = group.getChildCount();
            // Count backwards - let topmost views consume scroll distance first.
            for (int i = count - 1; i >= 0; i--) {
                final View child = group.getChildAt(i);

                final int childLeft = child.getLeft() + supportGetTranslationX(child);
                final int childRight = child.getRight() + supportGetTranslationX(child);
                final int childTop = child.getTop() + supportGetTranslationY(child);
                final int childBottom = child.getBottom() + supportGetTranslationY(child);

                if (x >= childLeft && x < childRight && y >= childTop && y < childBottom
                        && canChildScrollHorizontally(child, true, dx, x - childLeft, y - childTop)) {
                    return true;
                }
            }
        }

        return checkV && mOnInterceptMoveEventListener.isViewDraggable(v, dx, x, y);
    }

    /**
     * Tests scrollability within child views of v given a delta of dx.
     *
     * @param v      View to test for horizontal scrollability
     * @param checkV Whether the view should be checked for draggability
     * @param dx     Delta scrolled in pixels
     * @param x      X coordinate of the active touch point
     * @param y      Y coordinate of the active touch point
     * @return true if child views of v can be scrolled by delta of dx.
     */
    protected boolean canChildScrollVertically(View v, boolean checkV, int dx, int x, int y) {
        if (v instanceof ViewGroup) {
            final ViewGroup group = (ViewGroup) v;

            final int count = group.getChildCount();
            // Count backwards - let topmost views consume scroll distance first.
            for (int i = count - 1; i >= 0; i--) {
                final View child = group.getChildAt(i);

                final int childLeft = child.getLeft() + supportGetTranslationX(child);
                final int childRight = child.getRight() + supportGetTranslationX(child);
                final int childTop = child.getTop() + supportGetTranslationY(child);
                final int childBottom = child.getBottom() + supportGetTranslationY(child);

                if (x >= childLeft && x < childRight && y >= childTop && y < childBottom
                        && canChildScrollVertically(child, true, dx, x - childLeft, y - childTop)) {
                    return true;
                }
            }
        }

        return checkV && mOnInterceptMoveEventListener.isViewDraggable(v, dx, x, y);
    }

    private int supportGetTranslationY(View v) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return (int) v.getTranslationY();
        }

        return 0;
    }

    private int supportGetTranslationX(View v) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return (int) v.getTranslationX();
        }

        return 0;
    }

    protected boolean isCloseEnough() {
        return Math.abs(mOffsetPixels) <= mCloseEnough;
    }

    /**
     * Stops ongoing peek drawer animation.
     */
    protected void endPeek() {
        removeCallbacks(mPeekStartRunnable);
        removeCallbacks(mPeekRunnable);
        stopLayerTranslation();
        mIsPeeking = false;
    }

    /**
     * Called when the peek drawer animation has successfully completed.
     */
    private void completePeek() {
        mPeekScroller.abortAnimation();
        setOffsetPixels(0);
        setDrawerState(STATE_CLOSED);
        stopLayerTranslation();
        mIsPeeking = false;
    }

    protected abstract void initPeekScroller();

    /**
     * Callback when each frame in the peek drawer animation should be drawn.
     */
    private void peekDrawerInvalidate() {
        if (mPeekScroller.computeScrollOffset()) {
            final int oldX = (int) mOffsetPixels;
            final int x = mPeekScroller.getCurrX();
            if (x != oldX) setOffsetPixels(x);

            if (!mPeekScroller.isFinished()) {
                postOnAnimation(mPeekRunnable);
                return;

            } else if (mPeekDelay > 0) {
                mPeekStartRunnable = new Runnable() {
                    @Override
                    public void run() {
                        startPeek();
                    }
                };
                postDelayed(mPeekStartRunnable, mPeekDelay);
            }
        }

        completePeek();
    }

    /**
     * Starts peek drawer animation.
     */
    protected void startPeek() {
        mIsPeeking = true;
        initPeekScroller();
        startLayerTranslation();
        peekDrawerInvalidate();
    }

    /**
     * Callback when each frame in the drawer animation should be drawn.
     */
    private void postAnimationInvalidate() {
        if (mScroller.computeScrollOffset()) {
            final int oldX = (int) mOffsetPixels;
            final int x = mScroller.getCurrX();

            if (x != oldX) setOffsetPixels(x);
            if (x != mScroller.getFinalX()) {
                postOnAnimation(mDragRunnable);
                return;
            }
        }
        completeAnimation();
    }

    /**
     * Called when a drawer animation has successfully completed.
     */
    private void completeAnimation() {
        mScroller.abortAnimation();
        final int finalX = mScroller.getFinalX();
        setOffsetPixels(finalX);
        setDrawerState(finalX == 0 ? STATE_CLOSED : STATE_OPEN);
        stopLayerTranslation();
    }

    protected void cancelContentTouch() {
        final long now = SystemClock.uptimeMillis();
        final MotionEvent cancelEvent = MotionEvent.obtain(now, now, MotionEvent.ACTION_CANCEL, 0.0f, 0.0f, 0);
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            getChildAt(i).dispatchTouchEvent(cancelEvent);
        }
        mContentContainer.dispatchTouchEvent(cancelEvent);
        cancelEvent.recycle();
    }

    /**
     * Called when a drag has been ended.
     */
    protected void endDrag() {
        mIsDragging = false;
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    /**
     * Stops ongoing animation of the drawer.
     */
    protected void stopAnimation() {
        removeCallbacks(mDragRunnable);
        mScroller.abortAnimation();
        stopLayerTranslation();
    }

    /**
     * If possible, set the layer type to {@link android.view.View#LAYER_TYPE_HARDWARE}.
     */
    @SuppressLint("NewApi")
    protected void startLayerTranslation() {
        if (USE_TRANSLATIONS && mHardwareLayersEnabled && !mLayerTypeHardware) {
            mLayerTypeHardware = true;
            mContentContainer.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            mMenuContainer.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }
    }

    /**
     * If the current layer type is {@link android.view.View#LAYER_TYPE_HARDWARE}, this will set it to
     * {@link View#LAYER_TYPE_NONE}.
     */
    @SuppressLint("NewApi")
    protected void stopLayerTranslation() {
        if (mLayerTypeHardware) {
            mLayerTypeHardware = false;
            mContentContainer.setLayerType(View.LAYER_TYPE_NONE, null);
            mMenuContainer.setLayerType(View.LAYER_TYPE_NONE, null);
        }
    }
    public void setTouchBezelSize(int size) {
        mTouchBezelSize = size;
    }

    public int getTouchBezelSize() {
        return mTouchBezelSize;
    }


    public void setHardwareLayerEnabled(boolean enabled) {
        if (enabled != mHardwareLayersEnabled) {
            mHardwareLayersEnabled = enabled;
            mMenuContainer.setHardwareLayersEnabled(enabled);
            mContentContainer.setHardwareLayersEnabled(enabled);
            stopLayerTranslation();
        }
    }

}
