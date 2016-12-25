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
        return (int) (v.getLeft() + v.getTranslationX());
    }

    @SuppressLint("NewApi")
    static int getTop(View v) {
        return (int) (v.getTop() + v.getTranslationY());
    }

    @SuppressLint("NewApi")
    static int getRight(View v) {
        return (int) (v.getRight() + v.getTranslationX());
    }

    @SuppressLint("NewApi")
    static int getLayoutDirection(View v) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return v.getLayoutDirection();
        }

        return View.LAYOUT_DIRECTION_LTR;
    }
}