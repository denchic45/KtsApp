package com.example.utils;

import android.content.Context;
import android.util.DisplayMetrics;

import org.jetbrains.annotations.NotNull;

public final class DimensionUtils {

    public static int pxToDp(int px, @NotNull Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public static int dpToPx(int dp, @NotNull Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }
}
