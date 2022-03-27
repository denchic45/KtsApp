package com.denchic45.widget.transition

import android.animation.Animator
import android.animation.ObjectAnimator
import android.view.View
import android.view.ViewGroup
import androidx.transition.Transition
import androidx.transition.TransitionValues

class Rotation : Transition() {
    override fun captureStartValues(transitionValues: TransitionValues) {
        captureValues(transitionValues)
    }

    override fun captureEndValues(transitionValues: TransitionValues) {
        captureValues(transitionValues)
    }

    private fun captureValues(transitionValues: TransitionValues) {
        val view = transitionValues.view
        transitionValues.values[ROTATION] = view.rotation
    }

    override fun createAnimator(
        sceneRoot: ViewGroup,
        startValues: TransitionValues?,
        endValues: TransitionValues?
    ): Animator? {
        val startRotate = startValues!!.values[ROTATION] as Float
        val endRotate = endValues!!.values[ROTATION] as Float
        return ObjectAnimator.ofFloat(endValues.view, View.ROTATION, startRotate, endRotate)
    }

    companion object {
        const val ROTATION = "ROTATION"
    }
}