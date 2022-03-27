package com.denchic45.widget.transition;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.transition.Transition;
import androidx.transition.TransitionValues;

import org.jetbrains.annotations.NotNull;

public class Elevation extends Transition {

    public static final String ELEVATION = "ELEVATION";

    @Override
    public void captureStartValues(@NonNull TransitionValues transitionValues) {
        captureValues(transitionValues);
    }

    @Override
    public void captureEndValues(@NonNull TransitionValues transitionValues) {
        captureValues(transitionValues);
    }

    private void captureValues(@NotNull TransitionValues transitionValues) {
        View view = transitionValues.view;
        transitionValues.values.put(ELEVATION, view.getElevation());
    }

    @Nullable
    @Override
    public Animator createAnimator(@NonNull ViewGroup sceneRoot,
                                   @Nullable TransitionValues startValues,
                                   @Nullable TransitionValues endValues) {

        float startRotate = (float) startValues.values.get(ELEVATION);
        float endRotate = (float) endValues.values.get(ELEVATION);
        return ObjectAnimator.ofFloat(endValues.view, "elevation", startRotate, endRotate);
    }
}
