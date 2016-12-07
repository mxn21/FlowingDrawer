/*
 * Copyright (C) 2016 Baidu, Inc. All Rights Reserved.
 */
package com.mxn.soul.flowingdrawer_core;

import android.view.animation.Interpolator;

/**
 * Created by baidu on 2016/10/17.
 */
public class SmoothInterpolator implements Interpolator {

    @Override
    public float getInterpolation(float t) {
        t -= 1.0f;
        return t * t * t * t * t + 1.0f;
    }
}
