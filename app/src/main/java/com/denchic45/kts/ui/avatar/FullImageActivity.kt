package com.denchic45.kts.ui.avatar;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.SharedElementCallback;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.transition.ChangeBounds;
import android.transition.ChangeImageTransform;
import android.transition.ChangeTransform;
import android.transition.Fade;
import android.transition.TransitionSet;
import android.util.FloatProperty;
import android.view.View;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.utils.widget.ImageFilterView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.denchic45.kts.R;
import com.github.chrisbanes.photoview.PhotoViewAttacher;
import com.google.android.material.appbar.AppBarLayout;

import java.util.List;

public class FullImageActivity extends AppCompatActivity {

    public static final String IMAGE_URL = "IMAGE_URL";
    private ImageFilterView iv;
    private AppBarLayout appBarLayout;


    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        iv.setRoundPercent(0.001f);
        appBarLayout.setAlpha(1);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportPostponeEnterTransition();
        getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        getWindow().setSharedElementEnterTransition(new DetailsTransition());
        getWindow().setEnterTransition(new Fade());
        getWindow().setExitTransition(new Fade());
        getWindow().setSharedElementReturnTransition(new DetailsTransition());

        setContentView(R.layout.activity_full_image);
        iv = findViewById(R.id.iv_image);
        iv.setImageResource(R.drawable.ic_add);
        appBarLayout = findViewById(R.id.app_bar);
        appBarLayout.setOutlineProvider(null);

        Glide.with(this)
//                .load("https://previews.123rf.com/images/fantrazy/fantrazy1605/fantrazy160500001/58722210-square-grey-font-with-white-inside-path-geometric-typeface-minimal-typewriter-latin-alphabet-letters.jpg")
                .load(getPhotoUrl())
                .dontTransform()
                .addListener(new RequestListener<Drawable>() {

                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        supportStartPostponedEnterTransition();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        supportStartPostponedEnterTransition();
                        return false;
                    }
                })
                .into(iv);

        setEnterSharedElementCallback(new SharedElementCallback() {

            private float from = 1;
            private float to = 0.001f;

            @Override
            public void onSharedElementEnd(List<String> sharedElementNames, List<View> sharedElements, List<View> sharedElementSnapshots) {
                ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(iv, new FloatProperty<ImageFilterView>("roundPercent") {
                    @Override
                    public void setValue(ImageFilterView object, float value) {
                        object.setRoundPercent(value);
                    }

                    @Override
                    public Float get(ImageFilterView object) {
                        return object.getRoundPercent();
                    }
                }, from, to);
                objectAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
                objectAnimator.setDuration(300);
                objectAnimator.start();
                objectAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        appBarLayout.animate().alpha(1).setDuration(100).start();
                    }
                });
                to = 1;
                from = 0.001f;
            }
        });
    }

    protected String getPhotoUrl() {
        return getIntent().getStringExtra(IMAGE_URL);
    }


    @Override
    protected void onResume() {
        super.onResume();
        PhotoViewAttacher photoViewAttacher = new PhotoViewAttacher(iv);
        photoViewAttacher.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        photoViewAttacher.setZoomable(true);
        photoViewAttacher.update();
    }

    public static class DetailsTransition extends TransitionSet {
        public DetailsTransition() {
            setOrdering(ORDERING_TOGETHER);
            setInterpolator(new AccelerateDecelerateInterpolator());

            addTransition(new ChangeBounds())
                    .addTransition(new ChangeTransform())
                    .addTransition(new ChangeImageTransform());
        }
    }
}