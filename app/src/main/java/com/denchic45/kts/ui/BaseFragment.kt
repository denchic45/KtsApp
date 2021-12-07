package com.denchic45.kts.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.viewbinding.ViewBinding
import com.denchic45.kts.di.viewmodel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject


interface HasViewModel<VM : ViewModel> {
    var viewModelFactory: ViewModelFactory<VM>

     val viewModel: VM
}

abstract class BaseFragment<VM : ViewModel, VB : ViewBinding>(layoutId:Int = 0) :
      Fragment(layoutId),HasViewModel<VM> {

    @Inject
    override lateinit var viewModelFactory: ViewModelFactory<VM>

    abstract val binding: VB

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }
}
