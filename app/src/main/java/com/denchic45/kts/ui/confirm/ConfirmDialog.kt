package com.denchic45.kts.ui.confirm

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.denchic45.kts.R
import com.denchic45.kts.di.viewmodel.ViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class ConfirmDialog : DialogFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory<ConfirmViewModel>
    val viewModel: ConfirmViewModel by viewModels { viewModelFactory }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog =
            MaterialAlertDialogBuilder(requireActivity(), R.style.MaterialAlertDialog_Rounded)
                .setTitle(requireArguments().getString(TITLE))
                .setMessage(requireArguments().getString(MESSAGE))
                .setPositiveButton("Да") { dialogInterface: DialogInterface?, i: Int -> viewModel.onPositiveClick() }
                .setNegativeButton("Нет") { dialog1: DialogInterface?, which: Int -> viewModel.onNegativeClick() }
        return dialog.create()
    }

    companion object {
        const val TITLE = "TITLE"
        const val MESSAGE = "SUBTITLE"
        fun newInstance(title: String?, subtitle: String?): ConfirmDialog {
            val fragment = ConfirmDialog()
            val args = Bundle()
            args.putString(TITLE, title)
            args.putString(MESSAGE, subtitle)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        AndroidSupportInjection.inject(this)
    }
}