package com.denchic45.widget.transition;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.transition.Transition;
import androidx.transition.TransitionValues;

public class Rotation extends Transition {

    public static final String ROTATION = "ROTATION";

    @Override
    public void captureStartValues(@NonNull TransitionValues transitionValues) {
        captureValues(transitionValues);
    }

    @Override
    public void captureEndValues(@NonNull TransitionValues transitionValues) {
        captureValues(transitionValues);
    }

    private void captureValues(TransitionValues transitionValues) {
        View view = transitionValues.view;
        transitionValues.values.put(ROTATION, view.getRotation());
    }

    @Nullable
    @Override
    public Animator createAnimator(@NonNull ViewGroup sceneRoot,
                                   @Nullable TransitionValues startValues,
                                   @Nullable TransitionValues endValues) {

        float startRotate = (float) startValues.values.get(ROTATION);
        float endRotate = (float) endValues.values.get(ROTATION);
        return ObjectAnimator.ofFloat(endValues.view, View.ROTATION, startRotate, endRotate);
    }
}
