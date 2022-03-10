package com.denchic45.kts.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.denchic45.kts.R
import com.denchic45.kts.di.viewmodel.ViewModelFactory
import com.denchic45.kts.ui.base.BaseViewModel
import com.denchic45.kts.ui.confirm.ConfirmDialog
import com.denchic45.kts.utils.collectWhenStarted
import com.denchic45.kts.utils.toast
import com.google.android.material.snackbar.Snackbar
import dagger.android.AndroidInjection
import javax.inject.Inject

abstract class BaseActivity<VM : BaseViewModel, VB : ViewBinding>(
    layoutId: Int = 0
) : AppCompatActivity(layoutId) {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory<VM>

    abstract val viewModel: VM

    abstract val binding: VB

    open val navController: NavController by lazy { findNavController(R.id.nav_host_fragment) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidInjection.inject(this)

        viewModel.finish.collectWhenStarted(lifecycleScope) {
            finish()
        }

        viewModel.toast.collectWhenStarted(lifecycleScope, this::toast)

        viewModel.toastRes.collectWhenStarted(lifecycleScope, this::toast)

        viewModel.snackBar.collectWhenStarted(lifecycleScope) {
            Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
        }

        viewModel.snackBarRes.collectWhenStarted(lifecycleScope) {
            Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
        }

        viewModel.openConfirmation.collectWhenStarted(lifecycleScope) { (title, message) ->
            navController.navigate(
                R.id.action_global_confirmDialog,
                bundleOf(
                    ConfirmDialog.TITLE to title,
                    ConfirmDialog.MESSAGE to message
                )
            )
        }
    }
}