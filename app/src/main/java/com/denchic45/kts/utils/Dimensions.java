package com.denchic45.kts.utils;

import android.content.Context;
import android.util.DisplayMetrics;

import org.jetbrains.annotations.NotNull;

public final class Dimensions {

    public static int dpToPx(int dp, @NotNull Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }
}
