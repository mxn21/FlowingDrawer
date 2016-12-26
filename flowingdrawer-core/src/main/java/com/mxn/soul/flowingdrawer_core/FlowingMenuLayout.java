package com.mxn.soul.flowingdrawer_core;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Region;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

/**
 * Created by mxn on 2016/12/13.
 * menu layout
 */

@SuppressWarnings("FieldCanBeLocal")
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
    private int position;

    public FlowingMenuLayout(Context context) {
        this(context, null);
    }

    public FlowingMenuLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FlowingMenuLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mClipPath = new Path();
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            setLayerType(View.LAYER_TYPE_SOFTWARE, mPaint);
        }
    }

    public void setPaintColor(int color) {
        mPaint.setColor(color);
    }

    public void setMenuPosition(int position) {
        this.position = position;
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
        if (position == ElasticDrawer.Position.LEFT) {
            drawLeftMenu();
        } else {
            drawRightMenu();
        }
        canvas.save();
        canvas.drawPath(mClipPath, mPaint);
        canvas.clipPath(mClipPath, Region.Op.REPLACE);
        super.dispatchDraw(canvas);
        canvas.restore();
    }

    private void drawLeftMenu() {
        switch (currentType) {
            case TYPE_NONE:
                /**
                 * 空状态
                 * mClipOffsetPixels =0 or mClipOffsetPixels = width
                 */
                mClipPath.moveTo(0, 0);
                mClipPath.lineTo(width, 0);
                mClipPath.lineTo(width, height);
                mClipPath.lineTo(0, height);
                mClipPath.lineTo(0, 0);
                break;
            case TYPE_UP_MANUAL:
                /**
                 * 手动打开状态
                 verticalOffsetRatio = 0 when currentPointY = 0.5 * height ;
                 verticalOffsetRatio = 1 when currentPointY = height or currentPointY = 0;
                 bottomY,topY由两部分组成
                 第一部分是初始位置，由ratio1和currentPointY决定
                 第二部分由currentPointX移动位置决定
                 两部分系数分别是ratio1，ratio2
                 ratio1，ratio2表示 (currentPointY - topY)/ (bottomY - currentPointY)
                 第一部分bottomY - topY的初始值为height的0.7 倍
                 第二部分bottomY - topY变化的总长为currentPointX变化总长的6倍
                 */
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
                /**
                 * 自动打开状态
                 fraction变化范围是0-1
                 0-0.5时fractionCenter变化慢（根号函数）,fractionEdge变化快（指数函数）
                 0.5-1时fractionCenter变化快（指数函数）,fractionEdge变化慢（根号函数）
                 centerXOffset初始值width / 2, 变化到width + 150
                 edgeXOffset初始值width * 0.75 ,变化到width + 100
                 */
                fraction = (mClipOffsetPixels - width / 2) / (width / 2);
                if (fraction <= 0.5) {
                    fractionCenter = (float) (2 * Math.pow(fraction, 2));
                    fractionEdge = (float) ((1 / Math.sqrt(2)) * Math.sqrt(fraction));
                } else {
                    fractionCenter =
                            (float) (1 / (2 - Math.sqrt(2)) * Math.sqrt(fraction) + 1 - 1 / (2 - Math.sqrt(2)));
                    fractionEdge = (float) (2 * Math.pow(fraction, 2) / 3 + (float) 1 / 3);
                }
                centerXOffset = (int) (width / 2 + fractionCenter * (width / 2 + 150));
                edgeXOffset = (int) (width * 0.75 + fractionEdge * (width / 4 + 100));
                mClipPath.moveTo(width - mClipOffsetPixels, 0);
                mClipPath.lineTo(edgeXOffset, 0);
                mClipPath.quadTo(centerXOffset, eventY, edgeXOffset, height);
                mClipPath.lineTo(width - mClipOffsetPixels, height);
                mClipPath.lineTo(width - mClipOffsetPixels, 0);
                break;
            case TYPE_UP_DOWN:
                /**
                 * 打开后回弹状态
                 centerXOffset初始值width + 150,变化到width
                 edgeXOffset初始值width + 100 ,变化到width
                 */
                centerXOffset = (int) (width + 150 - 150 * fractionUpDown);
                edgeXOffset = (int) (width + 100 - 100 * fractionUpDown);
                mClipPath.moveTo(width - mClipOffsetPixels, 0);
                mClipPath.lineTo(edgeXOffset, 0);
                mClipPath.quadTo(centerXOffset, eventY, edgeXOffset, height);
                mClipPath.lineTo(width - mClipOffsetPixels, height);
                mClipPath.lineTo(width - mClipOffsetPixels, 0);
                break;
            case TYPE_DOWN_AUTO:
                /**
                 * 自动关闭状态
                 edgeXOffset值width
                 centerXOffset 比edgeXOffset多移动0.5 * width
                 */
                fractionCenterDown = 1 - mClipOffsetPixels / width;
                centerXOffset = (int) (width - 0.5 * width * fractionCenterDown);
                mClipPath.moveTo(width - mClipOffsetPixels, 0);
                mClipPath.lineTo(width, 0);
                mClipPath.quadTo(centerXOffset, eventY, width, height);
                mClipPath.lineTo(width - mClipOffsetPixels, height);
                mClipPath.lineTo(width - mClipOffsetPixels, 0);
                break;
            case TYPE_DOWN_MANUAL:
                /**
                 * 手动关闭状态
                 edgeXOffset值width
                 centerXOffset 比edgeXOffset多移动0.5 * width
                 */
                fractionCenterDown = 1 - mClipOffsetPixels / width;
                centerXOffset = (int) (width - 0.5 * width * fractionCenterDown);
                mClipPath.moveTo(width - mClipOffsetPixels, 0);
                mClipPath.lineTo(width, 0);
                mClipPath.quadTo(centerXOffset, eventY, width, height);
                mClipPath.lineTo(width - mClipOffsetPixels, height);
                mClipPath.lineTo(width - mClipOffsetPixels, 0);
                break;
            case TYPE_DOWN_SMOOTH:
                /**
                 * 手动打开不到一半,松手后恢复到初始状态
                 每次绘制两边纵坐标增加10
                 */
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
                break;
            default:
                break;
        }
    }

    private void drawRightMenu() {
        switch (currentType) {
            case TYPE_NONE:
                /**
                 * 空状态
                 * mClipOffsetPixels =0 or mClipOffsetPixels = width
                 */
                mClipPath.moveTo(width, 0);
                mClipPath.lineTo(0, 0);
                mClipPath.lineTo(0, height);
                mClipPath.lineTo(width, height);
                mClipPath.lineTo(width, 0);
                break;
            case TYPE_UP_MANUAL:
                /**
                 * 手动打开状态
                 verticalOffsetRatio = 0 when currentPointY = 0.5 * height ;
                 verticalOffsetRatio = 1 when currentPointY = height or currentPointY = 0;
                 bottomY,topY由两部分组成
                 第一部分是初始位置，由ratio1和currentPointY决定
                 第二部分由currentPointX移动位置决定
                 两部分系数分别是ratio1，ratio2
                 ratio1，ratio2表示 (currentPointY - topY)/ (bottomY - currentPointY)
                 第一部分bottomY - topY的初始值为height的0.7 倍
                 第二部分bottomY - topY变化的总长为currentPointX变化总长的6倍
                 */
                verticalOffsetRatio = Math.abs((double) (2 * eventY - height) / height);
                ratio1 = verticalOffsetRatio * 3 + 1;
                ratio2 = verticalOffsetRatio * 5 + 1;
                if (eventY - height / 2 >= 0) {
                    bottomY = (int) (eventY + 0.7 * height / (ratio1 + 1) - mClipOffsetPixels * 6 / (ratio2 + 1));
                    topY = (int) (eventY - 0.7 * height / (1 + 1 / ratio1) + mClipOffsetPixels * 6 / (1 / ratio2 + 1));
                    topControlY = (int) (-bottomY / 4 + 5 * eventY / 4);
                    bottomControlY = (int) (bottomY / 4 + 3 * eventY / 4);
                } else {
                    bottomY =
                            (int) (eventY + 0.7 * height / (1 / ratio1 + 1) - mClipOffsetPixels * 6 / (1 / ratio2 +
                                                                                                               1));
                    topY = (int) (eventY - 0.7 * height / (1 + ratio1) + mClipOffsetPixels * 6 / (ratio2 + 1));
                    topControlY = (int) (topY / 4 + 3 * eventY / 4);
                    bottomControlY = (int) (-topY / 4 + 5 * eventY / 4);
                }
                mClipPath.moveTo(-mClipOffsetPixels, topY);
                mClipPath.cubicTo(-mClipOffsetPixels, topControlY, 0,
                        topControlY, 0, eventY);
                mClipPath.cubicTo(0, bottomControlY, -mClipOffsetPixels,
                        bottomControlY, -mClipOffsetPixels, bottomY);
                mClipPath.lineTo(-mClipOffsetPixels, topY);
                break;
            case TYPE_UP_AUTO:
                /**
                 * 自动打开状态
                 fraction变化范围是0-1
                 0-0.5时fractionCenter变化慢（根号函数）,fractionEdge变化快（指数函数）
                 0.5-1时fractionCenter变化快（指数函数）,fractionEdge变化慢（根号函数）
                 centerXOffset初始值width / 2, 变化到width + 150
                 edgeXOffset初始值width * 0.75 ,变化到width + 100
                 */
                fraction = (-mClipOffsetPixels - width / 2) / (width / 2);
                if (fraction <= 0.5) {
                    fractionCenter = (float) (2 * Math.pow(fraction, 2));
                    fractionEdge = (float) ((1 / Math.sqrt(2)) * Math.sqrt(fraction));
                } else {
                    fractionCenter =
                            (float) (1 / (2 - Math.sqrt(2)) * Math.sqrt(fraction) + 1 - 1 / (2 - Math.sqrt(2)));
                    fractionEdge = (float) (2 * Math.pow(fraction, 2) / 3 + (float) 1 / 3);
                }
                centerXOffset = (int) (width / 2 + fractionCenter * (width / 2 + 150));
                edgeXOffset = (int) (width * 0.75 + fractionEdge * (width / 4 + 100));
                mClipPath.moveTo(-mClipOffsetPixels, 0);
                mClipPath.lineTo(width - edgeXOffset, 0);
                mClipPath.quadTo(width - centerXOffset, eventY, width - edgeXOffset, height);
                mClipPath.lineTo(-mClipOffsetPixels, height);
                mClipPath.lineTo(-mClipOffsetPixels, 0);
                break;
            case TYPE_UP_DOWN:
                /**
                 * 打开后回弹状态
                 centerXOffset初始值width + 150,变化到width
                 edgeXOffset初始值width + 100 ,变化到width
                 */
                centerXOffset = (int) (width + 150 - 150 * fractionUpDown);
                edgeXOffset = (int) (width + 100 - 100 * fractionUpDown);
                mClipPath.moveTo(-mClipOffsetPixels, 0);
                mClipPath.lineTo(width - edgeXOffset, 0);
                mClipPath.quadTo(width - centerXOffset, eventY, width - edgeXOffset, height);
                mClipPath.lineTo(-mClipOffsetPixels, height);
                mClipPath.lineTo(-mClipOffsetPixels, 0);
                break;
            case TYPE_DOWN_AUTO:
                /**
                 * 自动关闭状态
                 edgeXOffset值width
                 centerXOffset 比edgeXOffset多移动0.5 * width
                 */
                fractionCenterDown = 1 + mClipOffsetPixels / width;
                centerXOffset = (int) (width - 0.5 * width * fractionCenterDown);
                mClipPath.moveTo(-mClipOffsetPixels, 0);
                mClipPath.lineTo(0, 0);
                mClipPath.quadTo(width - centerXOffset, eventY, 0, height);
                mClipPath.lineTo(-mClipOffsetPixels, height);
                mClipPath.lineTo(-mClipOffsetPixels, 0);
                break;
            case TYPE_DOWN_MANUAL:
                /**
                 * 手动关闭状态
                 edgeXOffset值width
                 centerXOffset 比edgeXOffset多移动0.5 * width
                 */
                fractionCenterDown = 1 + mClipOffsetPixels / width;
                centerXOffset = (int) (width - 0.5 * width * fractionCenterDown);
                mClipPath.moveTo(-mClipOffsetPixels, 0);
                mClipPath.lineTo(0, 0);
                mClipPath.quadTo(width - centerXOffset, eventY, 0, height);
                mClipPath.lineTo(-mClipOffsetPixels, height);
                mClipPath.lineTo(-mClipOffsetPixels, 0);
                break;
            case TYPE_DOWN_SMOOTH:
                /**
                 * 手动打开不到一半,松手后恢复到初始状态
                 每次绘制两边纵坐标增加10
                 */
                bottomY = bottomY + 10;
                topY = topY - 10;
                if (eventY - height / 2 >= 0) {
                    topControlY = (int) (-bottomY / 4 + 5 * eventY / 4);
                    bottomControlY = (int) (bottomY / 4 + 3 * eventY / 4);
                } else {
                    topControlY = (int) (topY / 4 + 3 * eventY / 4);
                    bottomControlY = (int) (-topY / 4 + 5 * eventY / 4);
                }
                mClipPath.moveTo(-mClipOffsetPixels, topY);
                mClipPath.cubicTo(-mClipOffsetPixels, topControlY, 0,
                        topControlY, 0, eventY);
                mClipPath.cubicTo(0, bottomControlY, -mClipOffsetPixels,
                        bottomControlY, -mClipOffsetPixels, bottomY);
                mClipPath.lineTo(-mClipOffsetPixels, topY);
                break;
            default:
                break;
        }
    }

}
