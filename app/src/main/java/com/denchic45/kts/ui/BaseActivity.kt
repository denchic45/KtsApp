package com.denchic45.kts.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.viewbinding.ViewBinding
import com.denchic45.kts.di.viewmodel.ViewModelFactory
import dagger.android.AndroidInjection
import javax.inject.Inject

abstract class BaseActivity<VM : ViewModel, VB : ViewBinding>(
    layoutId: Int = 0
) : AppCompatActivity(layoutId) {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory<VM>

    abstract val viewModel: VM

    abstract val binding: VB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidInjection.inject(this)
    }
}