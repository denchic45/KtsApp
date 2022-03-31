package com.denchic45.kts.ui.avatar

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.app.SharedElementCallback
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.transition.*
import android.util.FloatProperty
import android.view.View
import android.view.Window
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.utils.widget.ImageFilterView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.denchic45.kts.R
import com.github.chrisbanes.photoview.PhotoViewAttacher
import com.google.android.material.appbar.AppBarLayout

open class FullImageActivity : AppCompatActivity() {
    private lateinit var iv: ImageFilterView
    private lateinit var appBarLayout: AppBarLayout
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        iv.roundPercent = 0.001f
        appBarLayout.alpha = 1f
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportPostponeEnterTransition()
        window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
        window.sharedElementEnterTransition = DetailsTransition()
        window.enterTransition = Fade()
        window.exitTransition = Fade()
        window.sharedElementReturnTransition = DetailsTransition()
        setContentView(R.layout.activity_full_image)
        iv = findViewById(R.id.iv_image)
        iv.setImageResource(R.drawable.ic_add)
        appBarLayout = findViewById(R.id.app_bar)
        appBarLayout.outlineProvider = null
        Glide.with(this) //                .load("https://previews.123rf.com/images/fantrazy/fantrazy1605/fantrazy160500001/58722210-square-grey-font-with-white-inside-path-geometric-typeface-minimal-typewriter-latin-alphabet-letters.jpg")
            .load(photoUrl)
            .dontTransform()
            .addListener(object : RequestListener<Drawable?> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any,
                    target: Target<Drawable?>,
                    isFirstResource: Boolean
                ): Boolean {
                    supportStartPostponedEnterTransition()
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any,
                    target: Target<Drawable?>,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    supportStartPostponedEnterTransition()
                    return false
                }
            })
            .into(iv)
        setEnterSharedElementCallback(object : SharedElementCallback() {
            private var from = 1f
            private var to = 0.001f
            override fun onSharedElementEnd(
                sharedElementNames: List<String>,
                sharedElements: List<View>,
                sharedElementSnapshots: List<View>
            ) {
                val objectAnimator = ObjectAnimator.ofFloat(
                    iv,
                    object : FloatProperty<ImageFilterView>("roundPercent") {
                        override fun setValue(`object`: ImageFilterView, value: Float) {
                            `object`.roundPercent = value
                        }

                        override fun get(`object`: ImageFilterView): Float {
                            return `object`.roundPercent
                        }
                    },
                    from,
                    to
                )
                objectAnimator.interpolator = AccelerateDecelerateInterpolator()
                objectAnimator.duration = 300
                objectAnimator.start()
                objectAnimator.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        appBarLayout.animate().alpha(1f).setDuration(100).start()
                    }
                })
                to = 1f
                from = 0.001f
            }
        })
    }

    protected val photoUrl: String?
        get() = intent.getStringExtra(IMAGE_URL)

    override fun onResume() {
        super.onResume()
        val photoViewAttacher = PhotoViewAttacher(iv)
        photoViewAttacher.scaleType = ImageView.ScaleType.CENTER_INSIDE
        photoViewAttacher.isZoomable = true
        photoViewAttacher.update()
    }

    class DetailsTransition : TransitionSet() {
        init {
            ordering = ORDERING_TOGETHER
            interpolator = AccelerateDecelerateInterpolator()
            addTransition(ChangeBounds())
                .addTransition(ChangeTransform())
                .addTransition(ChangeImageTransform())
        }
    }

    companion object {
        const val IMAGE_URL = "IMAGE_URL"
    }
}