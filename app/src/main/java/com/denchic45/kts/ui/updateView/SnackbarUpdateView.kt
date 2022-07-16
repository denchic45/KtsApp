package com.denchic45.kts.ui.updateView

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.denchic45.kts.databinding.SnackbarUpdateBinding
import com.denchic45.kts.util.animateHeight


class SnackbarUpdateView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    enum class UpdateState { REMIND, LOADING, INSTALL }

    private var _binding: SnackbarUpdateBinding? = null
    val binding: SnackbarUpdateBinding
        get() = _binding!!


    var onLaterClickListener: () -> Unit = {}
    var onDownloadClickListener: () -> Unit = {}
    var onInstallClickListener: () -> Unit = {}

    fun showState(state: UpdateState) {
        with(binding.vf) {
            val snackbarOnShowed = binding.vf.height != 0
            if (state.ordinal != displayedChild) {
                displayedChild = state.ordinal
                if (snackbarOnShowed)
                    animateHeight()
            }
        }
    }

    fun updateLoadingProgress(progress: Long, progressInfo: String) {
        binding.progressDownload.setProgress(progress.toInt(), true)
        binding.tvProgress.text = progressInfo
    }


    init {
        _binding = SnackbarUpdateBinding.inflate(LayoutInflater.from(context), this, true)

        with(binding) {
            btnUpdate.setOnClickListener { onDownloadClickListener() }
            btnLater.setOnClickListener { onLaterClickListener() }
            btnInstall.setOnClickListener { onInstallClickListener() }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        _binding = null
    }

    fun indeterminate(value: Boolean) {
       binding.progressDownload.isIndeterminate = value
    }
}

fun View.getActivity(): Activity? {
    var context = context
    while (context is ContextWrapper) {
        if (context is Activity) {
            return context
        }
        context = context.baseContext
    }
    return null
}