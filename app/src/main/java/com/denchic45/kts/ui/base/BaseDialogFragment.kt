package com.denchic45.kts.ui.base

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.denchic45.kts.R
import com.denchic45.kts.di.viewmodel.ViewModelFactory
import com.denchic45.kts.ui.confirm.ConfirmDialog
import com.denchic45.kts.util.collectWhenResumed
import com.denchic45.kts.util.collectWhenStarted
import com.denchic45.kts.util.strings
import com.denchic45.kts.util.toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

abstract class BaseDialogFragment<VM : BaseViewModel, VB : ViewBinding>(
    private val layoutId: Int = 0
) : DialogFragment() {

    @Inject
    open lateinit var viewModelFactory: ViewModelFactory<VM>

    abstract val viewModel: VM

    abstract val binding: VB

    lateinit var alertDialog: AlertDialog

    val navController by lazy { findNavController() }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    val root: View by lazy {
        layoutInflater.inflate(layoutId, null)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialogBuilder =
            MaterialAlertDialogBuilder(requireContext(), R.style.MaterialAlertDialog_Rounded)
        if (layoutId != 0) {
            dialogBuilder.setView(root)
        }
        onBuildDialog(dialogBuilder, savedInstanceState)
        alertDialog = dialogBuilder.create()
        alertDialog.window!!.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        return alertDialog
    }

    abstract fun onBuildDialog(dialog: MaterialAlertDialogBuilder, savedInstanceState: Bundle?)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return if (layoutId != 0) {
            root
        } else {
            super.onCreateView(inflater, container, savedInstanceState)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.navigate.collectWhenResumed(viewLifecycleOwner.lifecycleScope) { command ->
            when (command) {
                is NavigationCommand.To -> navController.navigate(command.directions)
                NavigationCommand.Back -> navController.popBackStack()
                is NavigationCommand.BackTo ->
                    navController.popBackStack(command.destinationId, false)
                NavigationCommand.ToRoot ->
                    navController.popBackStack(navController.graph.startDestinationId, false)
            }
        }

        viewModel.finish.collectWhenStarted(lifecycleScope) { dismiss() }

        viewModel.openConfirmation.collectWhenStarted(lifecycleScope) { (title, message) ->
            findNavController().navigate(
                R.id.action_global_confirmDialog,
                bundleOf(
                    ConfirmDialog.TITLE to title,
                    ConfirmDialog.MESSAGE to message
                )
            )
        }

        viewModel.toast.collectWhenStarted(lifecycleScope, this::toast)

        viewModel.toastRes.collectWhenStarted(lifecycleScope, this::toast)

        viewModel.snackBar.collectWhenStarted(lifecycleScope) { (message, action) ->
            val snackbar = Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            action?.let { snackbar.setAction(action) { viewModel.onSnackbarActionClick(message) } }
            snackbar.show()
        }

        viewModel.snackBarRes.collectWhenStarted(lifecycleScope) { (messageRes, action) ->
            val snackbar = Snackbar.make(binding.root, messageRes, Snackbar.LENGTH_LONG)
            action?.let {
                snackbar.setAction(action) {
                    viewModel.onSnackbarActionClick(requireContext().strings(messageRes))
                }
            }
            snackbar.show()
        }
    }
}