package com.denchic45.kts.ui

import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.denchic45.kts.R
import com.denchic45.kts.di.viewmodel.ViewModelFactory
import com.denchic45.kts.ui.base.BaseViewModel
import com.denchic45.kts.ui.confirm.ConfirmDialog
import com.denchic45.kts.utils.collectWhenResumed
import com.denchic45.kts.utils.collectWhenStarted
import com.denchic45.kts.utils.setActivityTitle
import com.denchic45.kts.utils.toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.android.support.AndroidSupportInjection
import kotlinx.coroutines.flow.collect
import javax.inject.Inject


interface HasViewModel<VM : BaseViewModel> {

    var viewModelFactory: ViewModelFactory<VM>

    val viewModel: VM
}

abstract class BaseFragment<VM : BaseViewModel, VB : ViewBinding>(layoutId: Int) :
    Fragment(layoutId), HasViewModel<VM> {

    @Inject
    override lateinit var viewModelFactory: ViewModelFactory<VM>

    abstract val binding: VB

   open val navController: NavController by lazy { findNavController() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launchWhenStarted {
            viewModel.toast.collect(this@BaseFragment::toast)
        }
        collectOnShowToolbarTitle()
            viewModel.finish.collectWhenStarted(lifecycleScope) {
                findNavController().navigateUp()
            }

        viewModel.toast.collectWhenStarted(lifecycleScope, this::toast)

        viewModel.toastRes.collectWhenStarted(lifecycleScope, this::toast)

        viewModel.snackBar.collectWhenStarted(lifecycleScope) {
            Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
        }

        viewModel.snackBarRes.collectWhenStarted(lifecycleScope) {
            Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
        }

        viewModel.dialog.collectWhenStarted(lifecycleScope) { (title, message) ->
            MaterialAlertDialogBuilder(requireContext(), R.style.MaterialAlertDialog_Rounded)
                .setTitle(title)
                .setMessage(message)
                .create()
                .show()
        }

        viewModel.dialogRes.collectWhenStarted(lifecycleScope) { (titleRes, messageRes) ->
            MaterialAlertDialogBuilder(requireContext(), R.style.MaterialAlertDialog_Rounded)
                .setTitle(titleRes)
                .setMessage(messageRes)
                .create()
                .show()
        }

        viewModel.openConfirmation.collectWhenStarted(lifecycleScope) { (title, message) ->
                findNavController().navigate(
                    R.id.action_global_confirmDialog,
                    bundleOf(
                        ConfirmDialog.TITLE to title,
                        ConfirmDialog.MESSAGE to message
                    )
                )
            }
    }

    open fun collectOnShowToolbarTitle() {
                viewModel.showToolbarTitle.collectWhenResumed(lifecycleScope) {
                    setActivityTitle(it)
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
