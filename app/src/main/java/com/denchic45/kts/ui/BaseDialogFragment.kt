package com.denchic45.kts.ui

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.denchic45.kts.di.viewmodel.ViewModelFactory
import com.denchic45.kts.ui.base.BaseViewModel
import dagger.android.support.AndroidSupportInjection
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

abstract class BaseDialogFragment<VM : BaseViewModel, VB : ViewBinding>(layoutId: Int = 0) :
    DialogFragment(layoutId) {

    @Inject
    open lateinit var viewModelFactory: ViewModelFactory<VM>

    abstract val viewModel: VM

    abstract val binding: VB

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launchWhenStarted {
            viewModel.finish.collect { dismiss() }
        }
    }
}