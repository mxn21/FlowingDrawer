/*
 * Copyright (C) 2016 Baidu, Inc. All Rights Reserved.
 */
package com.mxn.soul.flowingdrawer_core;

import android.view.animation.Interpolator;

/**
 * Created by baidu on 2016/10/17.
 */
public class SinusoidalInterpolator implements Interpolator {

    @Override
    public float getInterpolation(float input) {
        return (float) (0.5f + 0.5f * Math.sin(input * Math.PI - Math.PI / 2.f));
    }
}

