package com.denchic45.kts.utils;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListAdapter;

import androidx.core.content.ContextCompat;

import org.jetbrains.annotations.NotNull;

public class ViewUtils {

    public static ViewGroup getParent(@NotNull View view) {
        return (ViewGroup) view.getParent();
    }

    public static void removeView(View view) {
        ViewGroup parent = getParent(view);
        if (parent != null) {
            parent.removeView(view);
        }
    }

    public static void setClickable(View view, boolean clickable) {
        if (view != null) {
            if (view instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) view;
                for (int i = 0; i < viewGroup.getChildCount(); i++) {
                    setClickable(viewGroup.getChildAt(i), clickable);
                }
            }
            view.setClickable(clickable);
        }
    }

    public static void replaceView(View currentView, View newView) {
        ViewGroup parent = getParent(currentView);
        if (parent == null) {
            return;
        }
        final int index = parent.indexOfChild(currentView);
        removeView(currentView);
        removeView(newView);
        parent.addView(newView, index);
    }

    public static int measureAdapter(@NotNull ListAdapter listAdapter, Context context) {
        ViewGroup mMeasureParent = new FrameLayout(context);
        int longestWidth = listAdapter.getView(0, null, mMeasureParent).getMeasuredWidth();
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, mMeasureParent);
            listItem.measure(0, 0);
            int width = listItem.getMeasuredWidth();
            if (width > longestWidth) {
                longestWidth = width;
            }
        }
        return longestWidth;
    }

    public static void paintImageView(@NotNull ImageView iv, int color, Context context) {
        Paint paint = new Paint();
        paint.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(context, color), PorterDuff.Mode.SRC_ATOP));
        iv.setLayerPaint(paint);
    }
}