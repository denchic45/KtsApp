package com.denchic45.kts.ui

import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.denchic45.kts.R
import com.denchic45.kts.di.viewmodel.ViewModelFactory
import com.denchic45.kts.ui.base.BaseViewModel
import com.denchic45.kts.ui.confirm.ConfirmDialog
import com.denchic45.kts.utils.setActivityTitle
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

   open val navController: NavController by lazy { findNavController() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launchWhenStarted {
            viewModel.showToast.collect(this@BaseFragment::toast)
        }
        collectOnShowToolbarTitle()
        lifecycleScope.launchWhenStarted {
            viewModel.finish.collect {
                findNavController().navigateUp()
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.openConfirmation.collect { (title, message) ->
                findNavController().navigate(
                    R.id.action_global_confirmDialog,
                    bundleOf(
                        ConfirmDialog.TITLE to title,
                        ConfirmDialog.MESSAGE to message
                    )
                )
            }
        }
    }

    open fun collectOnShowToolbarTitle() {
        lifecycleScope.launchWhenResumed {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.showToolbarTitle.collect {
                    setActivityTitle(it)
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        viewModel.onOptionClick(item.itemId)
        return true
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }
}
