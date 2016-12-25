package com.mxn.soul.flowingdrawer_core;

import android.annotation.SuppressLint;
import android.os.Build;
import android.view.View;

/**
 * Created by mxn on 2016/12/9.
 * ViewHelper
 */

final class ViewHelper {

    private ViewHelper() {
    }

    @SuppressLint("NewApi")
    static int getLeft(View v) {
        if (FlowingDrawer.USE_TRANSLATIONS) {
            return (int) (v.getLeft() + v.getTranslationX());
        }

        return v.getLeft();
    }

    @SuppressLint("NewApi")
    static int getTop(View v) {
        if (FlowingDrawer.USE_TRANSLATIONS) {
            return (int) (v.getTop() + v.getTranslationY());
        }

        return v.getTop();
    }

    @SuppressLint("NewApi")
    static int getRight(View v) {
        if (FlowingDrawer.USE_TRANSLATIONS) {
            return (int) (v.getRight() + v.getTranslationX());
        }

        return v.getRight();
    }

    @SuppressLint("NewApi")
    static int getLayoutDirection(View v) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return v.getLayoutDirection();
        }

        return View.LAYOUT_DIRECTION_LTR;
    }
}