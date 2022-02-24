package com.denchic45.kts.ui

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.denchic45.kts.di.viewmodel.ViewModelFactory
import com.denchic45.kts.ui.base.BaseViewModel
import com.denchic45.kts.utils.toast
import dagger.android.support.AndroidSupportInjection
import kotlinx.coroutines.flow.collect
import javax.inject.Inject


interface HasViewModel<VM : BaseViewModel> {

    var viewModelFactory: ViewModelFactory<VM>

    val viewModel: VM
}

abstract class BaseFragment<VM : BaseViewModel, VB : ViewBinding>(layoutId: Int = 0) :
    Fragment(layoutId), HasViewModel<VM> {

    @Inject
    override lateinit var viewModelFactory: ViewModelFactory<VM>

    abstract val binding: VB

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launchWhenStarted {
            viewModel.showToast.collect(this@BaseFragment::toast)
        }
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }
}
