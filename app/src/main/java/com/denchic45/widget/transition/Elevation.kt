package com.denchic45.widget.transition

import android.animation.Animator
import android.animation.ObjectAnimator
import android.view.ViewGroup
import androidx.transition.Transition
import androidx.transition.TransitionValues

class Elevation : Transition() {
    override fun captureStartValues(transitionValues: TransitionValues) {
        captureValues(transitionValues)
    }

    override fun captureEndValues(transitionValues: TransitionValues) {
        captureValues(transitionValues)
    }

    private fun captureValues(transitionValues: TransitionValues) {
        val view = transitionValues.view
        transitionValues.values[ELEVATION] = view.elevation
    }

    override fun createAnimator(
        sceneRoot: ViewGroup,
        startValues: TransitionValues?,
        endValues: TransitionValues?
    ): Animator? {
        val startRotate = startValues!!.values[ELEVATION] as Float
        val endRotate = endValues!!.values[ELEVATION] as Float
        return ObjectAnimator.ofFloat(endValues.view, "elevation", startRotate, endRotate)
    }

    companion object {
        const val ELEVATION = "ELEVATION"
    }
}