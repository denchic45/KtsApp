package com.denchic45.kts.ui.specialtyEditor

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.denchic45.kts.R
import com.denchic45.kts.databinding.FragmentSpecialtyEditorBinding
import com.denchic45.kts.rx.EditTextTransformer
import com.denchic45.kts.ui.base.BaseDialogFragment
import com.denchic45.kts.util.collectWhenStarted
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jakewharton.rxbinding4.widget.textChanges

class SpecialtyEditorDialog :
    BaseDialogFragment<SpecialtyEditorViewModel, FragmentSpecialtyEditorBinding>(R.layout.fragment_specialty_editor) {

    override val binding: FragmentSpecialtyEditorBinding by viewBinding(
        FragmentSpecialtyEditorBinding::bind
    )
    override val viewModel: SpecialtyEditorViewModel by viewModels { viewModelFactory }

    override fun onBuildDialog(dialog: MaterialAlertDialogBuilder, savedInstanceState: Bundle?) {
        dialog.setTitle("Редактор специальности")
            .setPositiveButton("Ок", null)
            .setNegativeButton("Отмена", null)
            .setNeutralButton("Удалить", null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        alertDialog.setOnShowListener {
            val btnDelete = alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL)
            val btnPositive = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
            btnDelete.setTextColor(Color.RED)
            btnDelete.setOnClickListener { viewModel.onDeleteClick() }
            btnPositive.setOnClickListener { viewModel.onPositiveClick() }
        }

        viewModel.enablePositiveBtn.observe(viewLifecycleOwner) { enabled: Boolean? ->
            alertDialog.getButton(
                AlertDialog.BUTTON_POSITIVE
            ).isEnabled = enabled!!
        }
        with(binding) {
            etName.textChanges()
                .compose(EditTextTransformer())
                .subscribe(viewModel::onNameType)

            viewModel.nameField.collectWhenStarted(
                lifecycleScope
            ) { name -> if (!etName.text.contentEquals(name)) etName.setText(name) }
        }
        viewModel.deleteBtnVisibility.observe(viewLifecycleOwner) { visibility: Boolean ->
            alertDialog.getButton(
                AlertDialog.BUTTON_NEUTRAL
            ).visibility = if (visibility) View.VISIBLE else View.GONE
        }
        viewModel.title.observe(
            viewLifecycleOwner
        ) { name: String -> alertDialog.setTitle(name) }

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