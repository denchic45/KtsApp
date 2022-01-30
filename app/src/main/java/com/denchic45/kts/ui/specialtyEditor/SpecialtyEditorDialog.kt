package com.denchic45.kts.ui.specialtyEditor

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import com.denchic45.kts.R
import com.denchic45.kts.databinding.FragmentSpecialtyEditorBinding
import com.denchic45.kts.rx.EditTextTransformer
import com.denchic45.kts.ui.BaseDialogFragment
import com.denchic45.kts.ui.confirm.ConfirmDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import by.kirich1409.viewbindingdelegate.viewBinding
import com.jakewharton.rxbinding4.widget.textChanges
import dagger.android.support.AndroidSupportInjection

class SpecialtyEditorDialog :
    BaseDialogFragment<SpecialtyEditorViewModel, FragmentSpecialtyEditorBinding>() {

    override val binding: FragmentSpecialtyEditorBinding by viewBinding(FragmentSpecialtyEditorBinding::bind)
    override val viewModel: SpecialtyEditorViewModel by viewModels { viewModelFactory }
    private lateinit var alertDialog: AlertDialog
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog =
            MaterialAlertDialogBuilder(requireActivity(), R.style.MaterialAlertDialog_Rounded)
                .setView(R.layout.fragment_specialty_editor)
                .setTitle("Редактор специальности")
                .setPositiveButton("Ок", null)
                .setNegativeButton("Отмена", null)
                .setNeutralButton("Удалить", null)
        alertDialog = dialog.create()
        alertDialog.window!!.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        alertDialog.setOnShowListener {
            val btnDelete = alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL)
            val btnPositive = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
            btnDelete.setTextColor(Color.RED)
            btnDelete.setOnClickListener { viewModel.onDeleteClick() }
            btnPositive.setOnClickListener { viewModel.onPositiveClick() }
        }
        return alertDialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.enablePositiveBtn.observe(viewLifecycleOwner) { enabled: Boolean? ->
            alertDialog.getButton(
                AlertDialog.BUTTON_POSITIVE
            ).isEnabled = enabled!!
        }
        with(binding) {
            viewModel.nameField.observe(
                viewLifecycleOwner
            ) { name: String? -> etName.setText(name) }
            etName.textChanges()
                .compose(EditTextTransformer())
                .subscribe { name: String? ->
                    viewModel.onNameType(
                        name!!
                    )
                }
        }
        viewModel.deleteBtnVisibility.observe(viewLifecycleOwner) { visibility: Boolean ->
            alertDialog.getButton(
                AlertDialog.BUTTON_NEUTRAL
            ).visibility = if (visibility) View.VISIBLE else View.GONE
        }
        viewModel.title.observe(
            viewLifecycleOwner
        ) { name: String? -> alertDialog.setTitle(name) }
        viewModel.showMessageRes.observe(viewLifecycleOwner) { resId: Int? ->
            Toast.makeText(
                context, getString(
                    resId!!
                ), Toast.LENGTH_SHORT
            ).show()
        }
        viewModel.finish.observe(viewLifecycleOwner) { dismiss() }
        viewModel.openConfirmation.observe(
            viewLifecycleOwner
        ) { titleWithSubtitlePair: Pair<String, String> ->
            ConfirmDialog.newInstance(titleWithSubtitlePair.first, titleWithSubtitlePair.second)
                .show(childFragmentManager, null)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        AndroidSupportInjection.inject(this)
    }

    companion object {
        const val SPECIALTY_ID = "SPECIALTY_UUID"
        fun newInstance(specialtyUuid: String?): SpecialtyEditorDialog {
            val fragment = SpecialtyEditorDialog()
            val args = Bundle()
            args.putString(SPECIALTY_ID, specialtyUuid)
            fragment.arguments = args
            return fragment
        }
    }
}