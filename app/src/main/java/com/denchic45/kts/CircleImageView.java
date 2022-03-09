//package com.denchic45.kts;
//
//import android.content.Context;
//import android.util.AttributeSet;
//import android.view.ViewOutlineProvider;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.appcompat.widget.AppCompatImageView;
//
//import org.jetbrains.annotations.NotNull;
//
//public final class CircleImageView extends AppCompatImageView {
//    public CircleImageView(@NotNull Context context) {
//        super(context);
//        init();
//    }
//
//    public CircleImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
//        super(context, attrs);
//        init();
//    }
//
//    public CircleImageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
//        super(context, attrs, defStyleAttr);
//        init();
//    }
//
//    private void init() {
//        setOutlineProvider(ViewOutlineProvider.BACKGROUND);
//        setBackgroundResource(R.drawable.shape_circle);
//        setClipToOutline(true);
//        setScaleType(ScaleType.CENTER_CROP);
//    }
//
//}