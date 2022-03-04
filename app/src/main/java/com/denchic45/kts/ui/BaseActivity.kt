package com.denchic45.kts.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.denchic45.kts.di.viewmodel.ViewModelFactory
import com.denchic45.kts.ui.base.BaseViewModel
import dagger.android.AndroidInjection
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

abstract class BaseActivity<VM : BaseViewModel, VB : ViewBinding>(
    layoutId: Int = 0
) : AppCompatActivity(layoutId) {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory<VM>

    abstract val viewModel: VM

    abstract val binding: VB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidInjection.inject(this)

        lifecycleScope.launchWhenStarted {
            viewModel.finish.collect {
                finish()
            }
        }
    }
}