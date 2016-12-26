
package com.mxn.soul.flowingdrawer_core;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ValueAnimator;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Scroller;

/**
 * Created by mxn on 2016/10/15.
 * ElasticDrawer
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
    protected boolean mHardwareLayersEnabled = false;

    /**
     * The initial X position of a drag.
     */
    protected float mInitialMotionX;

    /**
     * The initial Y position of a drag.
     */
    protected float mInitialMotionY;

    /**
     * The last X position of a drag.
     */
    protected float mLastMotionX = -1;

    /**
     * The last Y position of a drag.
     */
    protected float mLastMotionY = -1;

    /**
     * Velocity tracker used when animating the drawer open/closed after a drag.
     */
    protected VelocityTracker mVelocityTracker;

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
    private FlowingMenuLayout mMenuView;

    /**
     * The color of the menu.
     */
    protected int mMenuBackground;
    /**
     * Current offset.
     */
    protected float mOffsetPixels;

    /**
     * The default touch bezel size of the drawer in dp.
     */
    private static final int DEFAULT_DRAG_BEZEL_DP = 32;
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
    public static final int STATE_DRAGGING_OPEN = 2;

    /**
     * Indicates that the drawer is currently being dragged by the user.
     */
    public static final int STATE_DRAGGING_CLOSE = 4;

    /**
     * Indicates that the drawer is currently opening.
     */
    public static final int STATE_OPENING = 6;

    /**
     * Indicates that the drawer is currently open.
     */
    public static final int STATE_OPEN = 8;

    /**
     * The current drawer state.
     *
     * @see #STATE_CLOSED
     * @see #STATE_CLOSING
     * @see #STATE_DRAGGING_OPEN
     * @see #STATE_DRAGGING_CLOSE
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

    private float eventY;

    protected boolean isFirstPointUp;

    /**
     * Runnable used when animating the drawer open/closed.
     */
    private final Runnable mDragRunnable = new Runnable() {
        @Override
        public void run() {
            postAnimationInvalidate();
        }
    };

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

    @SuppressLint("NewApi")
    protected void initDrawer(Context context, AttributeSet attrs, int defStyle) {
        setWillNotDraw(false);
        setFocusable(false);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ElasticDrawer);

        mMenuSize = a.getDimensionPixelSize(R.styleable.ElasticDrawer_edMenuSize, dpToPx(240));

        mMenuBackground = a.getColor(R.styleable.ElasticDrawer_edMenuBackground, 0xFFdddddd);

        mTouchBezelSize = a.getDimensionPixelSize(R.styleable.ElasticDrawer_edTouchBezelSize,
                dpToPx(DEFAULT_DRAG_BEZEL_DP));

        mMaxAnimationDuration = a.getInt(R.styleable.ElasticDrawer_edMaxAnimationDuration, DEFAULT_ANIMATION_DURATION);

        final int position = a.getInt(R.styleable.ElasticDrawer_edPosition, 0);
        setPosition(position);
        a.recycle();

        mMenuContainer = new NoClickThroughFrameLayout(context);
        mMenuContainer.setBackgroundColor(getResources().getColor(android.R.color.transparent));

        mContentContainer = new NoClickThroughFrameLayout(context);

        final ViewConfiguration configuration = ViewConfiguration.get(context);
        mTouchSlop = configuration.getScaledTouchSlop();
        mMaxVelocity = configuration.getScaledMaximumFlingVelocity();

        mScroller = new Scroller(context, SMOOTH_INTERPOLATOR);
        mCloseEnough = dpToPx(CLOSE_ENOUGH);

        mContentContainer.setLayerType(View.LAYER_TYPE_NONE, null);
        mContentContainer.setHardwareLayersEnabled(false);
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
         *
         * @return true if view is draggable by delta dx.
         */
        boolean isViewDraggable(View v, int delta, int x, int y);
    }

    class Position {
        // Positions the drawer to the left of the content.
        static final int LEFT = 1;
        // Positions the drawer to the right of the content.
        static final int RIGHT = 2;
        /**
         * Position the drawer at the start edge. This will position the drawer to the {@link #LEFT} with LTR
         * languages and
         * {@link #RIGHT} with RTL languages.
         */
        static final int START = 3;
        /**
         * Position the drawer at the end edge. This will position the drawer to the {@link #RIGHT} with LTR
         * languages and
         * {@link #LEFT} with RTL languages.
         */
        static final int END = 4;
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
            mContentContainer
                    .addView(content, new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        } else {
            throw new IllegalStateException(
                    "content view must be added in xml .");
        }
        View menu = getChildAt(0);
        if (menu != null) {
            removeView(menu);
            mMenuView = (FlowingMenuLayout) menu;
            mMenuView.setBackgroundColor(getResources().getColor(android.R.color.transparent));
            mMenuView.setPaintColor(mMenuBackground);
            mMenuView.setMenuPosition(getPosition());
            mMenuContainer.removeAllViews();
            mMenuContainer.addView(menu, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        } else {
            throw new IllegalStateException(
                    "menu view must be added in xml .");
        }
        addView(mContentContainer, -1, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        addView(mMenuContainer, -1, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
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
    @SuppressWarnings("unused")
    public void openMenu() {
        openMenu(true);
    }

    /**
     * Opens the menu.
     *
     * @param animate Whether open/close should be animated.
     */
    public abstract void openMenu(boolean animate);

    public abstract void openMenu(boolean animate, float y);

    /**
     * Animates the menu closed.
     */
    @SuppressWarnings("unused")
    public void closeMenu() {
        closeMenu(true);
    }

    /**
     * Closes the menu.
     *
     * @param animate Whether open/close should be animated.
     */
    public abstract void closeMenu(boolean animate);

    public abstract void closeMenu(boolean animate, float y);

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
    @SuppressWarnings("unused")
    public void setMenuSize(final int size) {
        mMenuSize = size;
        if (mDrawerState == STATE_OPEN || mDrawerState == STATE_OPENING) {
            setOffsetPixels(mMenuSize, 0, FlowingMenuLayout.TYPE_NONE);
        }
        requestLayout();
        invalidate();
    }

    protected void smoothClose(final int eventY) {
        endDrag();
        setDrawerState(STATE_CLOSING);

        ValueAnimator valueAnimator = ValueAnimator.ofFloat(mOffsetPixels, 0);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                setOffsetPixels((Float) animation.getAnimatedValue(), eventY,
                        FlowingMenuLayout.TYPE_DOWN_SMOOTH);
            }
        });
        valueAnimator.addListener(new FlowingAnimationListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mMenuVisible = false;
                setOffsetPixels(0, 0, FlowingMenuLayout.TYPE_NONE);
                setDrawerState(STATE_CLOSED);
                stopLayerTranslation();
            }
        });
        valueAnimator.setDuration(500);
        valueAnimator.setInterpolator(new DecelerateInterpolator(4f));
        valueAnimator.start();
    }

    /**
     * Moves the drawer to the position passed.
     *
     * @param position The position the content is moved to.
     * @param velocity Optional velocity if called by releasing a drag event.
     * @param animate  Whether the move is animated.
     */
    protected void animateOffsetTo(int position, int velocity, boolean animate, float eventY) {
        endDrag();
        final int startX = (int) mOffsetPixels;
        final int dx = position - startX;
        if (dx == 0 || !animate) {
            setOffsetPixels(position, 0, FlowingMenuLayout.TYPE_NONE);
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
        animateOffsetTo(position, duration, eventY);
    }

    protected void animateOffsetTo(int position, int duration, float eventY) {
        final int startX = (int) mOffsetPixels;
        final int dx = position - startX;
        if (getPosition() == Position.LEFT) {
            if (dx > 0) {
                setDrawerState(STATE_OPENING);
            } else {
                setDrawerState(STATE_CLOSING);
            }
        } else {
            if (dx > 0) {
                setDrawerState(STATE_CLOSING);
            } else {
                setDrawerState(STATE_OPENING);
            }
        }
        mScroller.startScroll(startX, 0, dx, 0, duration);
        this.eventY = eventY;
        startLayerTranslation();
        postAnimationInvalidate();
    }

    /**
     * Sets the number of pixels the content should be offset.
     *
     * @param offsetPixels The number of pixels to offset the content by.
     */
    protected void setOffsetPixels(float offsetPixels, float eventY, int type) {
        final int oldOffset = (int) mOffsetPixels;
        final int newOffset = (int) offsetPixels;

        mOffsetPixels = offsetPixels;
        mMenuView.setClipOffsetPixels(mOffsetPixels, eventY, type);
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

        if (getPosition() != mResolvedPosition) {
            mResolvedPosition = getPosition();
            setOffsetPixels(mOffsetPixels * -1, 0, FlowingMenuLayout.TYPE_NONE);
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
    @SuppressWarnings("unused")
    public void setOnInterceptMoveEventListener(OnInterceptMoveEventListener listener) {
        mOnInterceptMoveEventListener = listener;
    }

    /**
     * Sets the maximum duration of open/close animations.
     *
     * @param duration The maximum duration in milliseconds.
     */
    @SuppressWarnings("unused")
    public void setMaxAnimationDuration(int duration) {
        mMaxAnimationDuration = duration;
    }

    @SuppressWarnings("unused")
    public ViewGroup getMenuContainer() {
        return mMenuContainer;
    }

    /**
     * Returns the ViewGroup used as a parent for the content view.
     *
     * @return The content view's parent.
     */
    @SuppressWarnings("unused")
    public ViewGroup getContentContainer() {
        return mContentContainer;
    }

    /**
     * Get the current state of the drawer.
     *
     * @return The state of the drawer.
     */
    @SuppressWarnings("unused")
    public int getDrawerState() {
        return mDrawerState;
    }

    protected void setDrawerState(int state) {
        if (state != mDrawerState) {
            final int oldState = mDrawerState;
            mDrawerState = state;
            if (mOnDrawerStateChangeListener != null) {
                mOnDrawerStateChangeListener.onDrawerStateChange(oldState, state);
            }
            if (DEBUG) {
                logDrawerState(state);
            }
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

            case STATE_DRAGGING_CLOSE:
                Log.d(TAG, "[DrawerState] STATE_DRAGGING_CLOSE");
                break;
            case STATE_DRAGGING_OPEN:
                Log.d(TAG, "[DrawerState] STATE_DRAGGING_OPEN");
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
            setOffsetPixels(0, 0, FlowingMenuLayout.TYPE_NONE);
        }
        mDrawerState = menuOpen ? STATE_OPEN : STATE_CLOSED;
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState state = new SavedState(superState);

        if (mState == null) {
            mState = new Bundle();
        }
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

        @SuppressLint("ParcelClassLoader")
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

    protected boolean canChildrenScroll(int dx, int x, int y) {
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
     *
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
     * Callback when each frame in the drawer animation should be drawn.
     */
    private void postAnimationInvalidate() {
        if (mScroller.computeScrollOffset()) {
            final int oldX = (int) mOffsetPixels;
            final int x = mScroller.getCurrX();

            if (x != oldX) {
                if (mDrawerState == STATE_OPENING) {
                    setOffsetPixels(x, eventY, FlowingMenuLayout.TYPE_UP_AUTO);
                } else if (mDrawerState == STATE_CLOSING) {
                    setOffsetPixels(x, eventY, FlowingMenuLayout.TYPE_DOWN_AUTO);
                }
            }
            if (x != mScroller.getFinalX()) {
                postOnAnimation(mDragRunnable);
                return;
            }
        }
        if (mDrawerState == STATE_OPENING) {
            completeAnimation();
        } else if (mDrawerState == STATE_CLOSING) {
            mScroller.abortAnimation();
            final int finalX = mScroller.getFinalX();
            mMenuVisible = finalX != 0;
            setOffsetPixels(finalX, 0, FlowingMenuLayout.TYPE_NONE);
            setDrawerState(finalX == 0 ? STATE_CLOSED : STATE_OPEN);
            stopLayerTranslation();
        }

    }

    /**
     * Called when a drawer animation has successfully completed.
     */
    private void completeAnimation() {
        mScroller.abortAnimation();
        final int finalX = mScroller.getFinalX();
        flowDown(finalX);
    }

    private void flowDown(final int finalX) {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mMenuView.setUpDownFraction(animation.getAnimatedFraction());
            }
        });
        valueAnimator.addListener(new FlowingAnimationListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (mDrawerState == STATE_OPENING) {
                    mMenuVisible = finalX != 0;
                    setOffsetPixels(finalX, 0, FlowingMenuLayout.TYPE_NONE);
                    setDrawerState(finalX == 0 ? STATE_CLOSED : STATE_OPEN);
                    stopLayerTranslation();
                }
            }
        });
        valueAnimator.setDuration(300);
        valueAnimator.setInterpolator(new OvershootInterpolator(4f));
        valueAnimator.start();
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
        if (mHardwareLayersEnabled && !mLayerTypeHardware) {
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

    @SuppressWarnings("unused")
    public void setTouchBezelSize(int size) {
        mTouchBezelSize = size;
    }

    @SuppressWarnings("unused")
    public int getTouchBezelSize() {
        return mTouchBezelSize;
    }

    @SuppressWarnings("unused")
    public void setHardwareLayerEnabled(boolean enabled) {
        if (enabled != mHardwareLayersEnabled) {
            mHardwareLayersEnabled = enabled;
            mMenuContainer.setHardwareLayersEnabled(enabled);
            mContentContainer.setHardwareLayersEnabled(enabled);
            stopLayerTranslation();
        }
    }

}
