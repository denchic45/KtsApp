package com.denchic45.kts.ui.updateView

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.denchic45.kts.databinding.SnackbarUpdateBinding

class SnackbarUpdateView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    enum class UpdateState { REMIND, LOADING, INSTALL }

    private var _binding: SnackbarUpdateBinding? = null
    val binding: SnackbarUpdateBinding
        get() = _binding!!


    var onLaterClickListener: ()->Unit = {}
    var onDownloadClickListener: ()->Unit = {}
    var onInstallClickListener: ()->Unit = {}

    fun showState(state: UpdateState) {
        binding.vf.displayedChild = state.ordinal
    }

    fun updateLoadingProgress(progress: Int, progressInfo: String) {
        binding.progressDownload.setProgress(progress, true)
        binding.tvProgress.text = progressInfo
    }

    init {
        _binding = SnackbarUpdateBinding.inflate(LayoutInflater.from(context), this, true)

        with(binding) {
            btnUpdate.setOnClickListener { onDownloadClickListener() }
            btnLater.setOnClickListener { onLaterClickListener() }
            // TODO кнопка установки еще нужна
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        _binding = null
    }
}