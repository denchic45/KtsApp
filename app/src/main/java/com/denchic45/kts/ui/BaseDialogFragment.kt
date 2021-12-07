package com.denchic45.kts.ui

import android.content.Context
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModel
import androidx.viewbinding.ViewBinding
import com.denchic45.kts.di.viewmodel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

abstract class BaseDialogFragment<VM : ViewModel, VB : ViewBinding>(layoutId: Int = 0) :
    DialogFragment(layoutId) {

    @Inject
    open lateinit var viewModelFactory: ViewModelFactory<VM>

    abstract val viewModel: VM

    abstract val binding: VB


    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }
}