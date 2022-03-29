package com.denchic45.kts.ui

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavArgs
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.denchic45.kts.R
import com.denchic45.kts.di.viewmodel.ViewModelFactory
import com.denchic45.kts.ui.base.BaseViewModel
import com.denchic45.kts.ui.confirm.ConfirmDialog
import com.denchic45.kts.utils.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.android.support.AndroidSupportInjection
import kotlinx.coroutines.flow.debounce
import javax.inject.Inject


interface HasViewModel<VM : BaseViewModel> {

    var viewModelFactory: ViewModelFactory<VM>

    val viewModel: VM
}

interface HasNavArgs<T : NavArgs> {
    val navArgs: T
}

abstract class BaseFragment<VM : BaseViewModel, VB : ViewBinding>(
    layoutId: Int,
    private val menuResId: Int = 0
) : Fragment(layoutId), HasViewModel<VM> {

    @Inject
    override lateinit var viewModelFactory: ViewModelFactory<VM>

    abstract val binding: VB

    open val navController: NavController by lazy { findNavController() }

    protected lateinit var menu: Menu
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (menuResId != 0)
            setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.toast.collectWhenStarted(lifecycleScope, this@BaseFragment::toast)

        collectOnShowToolbarTitle()
        collectOnOptionVisibility()

        viewModel.navigate.collectWhenStarted(viewLifecycleOwner.lifecycleScope) { command ->
            when (command) {
                is NavigationCommand.To -> navController.navigate(command.directions)
                NavigationCommand.Back -> navController.popBackStack()
                is NavigationCommand.BackTo ->
                    navController.popBackStack(command.destinationId, false)
                NavigationCommand.ToRoot ->
                    navController.popBackStack(navController.graph.startDestinationId, false)
            }
        }

        viewModel.finish.collectWhenStarted(viewLifecycleOwner.lifecycleScope) {
            findNavController().navigateUp()
        }

        viewModel.toast.collectWhenStarted(viewLifecycleOwner.lifecycleScope, this::toast)

        viewModel.toastRes.collectWhenStarted(viewLifecycleOwner.lifecycleScope, this::toast)

        viewModel.snackBar.collectWhenStarted(viewLifecycleOwner.lifecycleScope) { (message, action) ->
            val snackbar = Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            action?.let { snackbar.setAction(action) { viewModel.onSnackbarActionClick(message) } }
            snackbar.show()
        }

        viewModel.snackBarRes.collectWhenStarted(viewLifecycleOwner.lifecycleScope) { (messageRes, action) ->
            val snackbar = Snackbar.make(binding.root, messageRes, Snackbar.LENGTH_LONG)
            action?.let {
                snackbar.setAction(action) {
                    viewModel.onSnackbarActionClick(requireContext().strings(messageRes))
                }
            }
            snackbar.show()
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
            navController.navigate(
                R.id.action_global_confirmDialog,
                bundleOf(
                    ConfirmDialog.TITLE to title,
                    ConfirmDialog.MESSAGE to message
                )
            )
        }
    }

    open fun collectOnOptionVisibility() {
        viewModel.optionVisibility
            .debounce(500)
            .collectWhenStarted(lifecycleScope) { (itemId, visible) ->
                menu.findItem(itemId).isVisible = visible
            }
    }

    open fun collectOnShowToolbarTitle() {
        viewModel.showToolbarTitle.collectWhenResumed(lifecycleScope) {
            setActivityTitle(it)
        }
    }


    final override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        this.menu = menu
        inflater.inflate(menuResId, menu)
        viewModel.onCreateOptions()
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        this.menu = menu
        return super.onPrepareOptionsMenu(menu)
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

sealed class NavigationCommand {
    data class To(val directions: NavDirections) : NavigationCommand()
    object Back : NavigationCommand()
    data class BackTo(val destinationId: Int) : NavigationCommand()
    object ToRoot : NavigationCommand()
}
