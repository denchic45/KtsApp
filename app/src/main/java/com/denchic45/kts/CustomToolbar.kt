package com.denchic45.kts;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Display;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;

import org.jetbrains.annotations.NotNull;


public class CustomToolbar extends Toolbar {

    int sizeText = (int) (getResources().getDimension(R.dimen.large_text) / getResources().getDisplayMetrics().density);
    private TextView textView;
    private int screenWidth;
    private boolean centerTitle = false;

    public CustomToolbar(Context context) {
        super(context);
        init();
    }

    public CustomToolbar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomToolbar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setBackgroundColor(Color.TRANSPARENT);
        screenWidth = getScreenSize().x;
        textView = new TextView(getContext());
        textView.setTextSize(sizeText);
        Typeface type = ResourcesCompat.getFont(getContext(), R.font.gilroy_medium);
        textView.setTypeface(type);
        textView.setSingleLine(true);
        textView.setEllipsize(TextUtils.TruncateAt.END);
        addView(textView);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (centerTitle) {
            int[] location = new int[2];
            textView.getLocationOnScreen(location);
            textView.setTranslationX(textView.getTranslationX() + (-location[0] + screenWidth / 2 - textView.getWidth() / 2));
        } else textView.setTranslationX(0);
    }

    public int pxToDp(float dpValue) {
        float dp = getResources().getDisplayMetrics().density;
        return (int) (dpValue * dp); // margin in pixels
    }


    @Override
    public void setTitle(CharSequence title) {
        textView.setText(title);
        requestLayout();
    }

    @Override
    public void setTitle(int titleRes) {
        textView.setText(titleRes);
        requestLayout();
    }

    public void setTitleCentered(boolean centered) {
        centerTitle = centered;
        requestLayout();
    }

    @NotNull
    private Point getScreenSize() {
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point screenSize = new Point();
        display.getSize(screenSize);

        return screenSize;
    }
}