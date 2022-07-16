package com.denchic45.kts.ui.subjectEditor

import android.graphics.Color
import android.graphics.drawable.PictureDrawable
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.denchic45.kts.SvgColorListener
import com.denchic45.kts.R
import com.denchic45.kts.databinding.DialogSubjectEditorBinding
import com.denchic45.kts.glideSvg.GlideApp
import com.denchic45.kts.rx.EditTextTransformer
import com.denchic45.kts.ui.base.BaseDialogFragment
import com.denchic45.kts.ui.adapter.ColorPickerAdapter
import com.denchic45.kts.ui.iconPicker.IconPickerDialog
import com.denchic45.kts.util.ViewUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jakewharton.rxbinding4.widget.textChanges

class SubjectEditorDialog : BaseDialogFragment<SubjectEditorViewModel, DialogSubjectEditorBinding>(
    R.layout.dialog_subject_editor
) {

    override val binding: DialogSubjectEditorBinding by viewBinding(DialogSubjectEditorBinding::bind)
    override val viewModel: SubjectEditorViewModel by viewModels { viewModelFactory }
    private lateinit var adapter: ColorPickerAdapter

    override fun onBuildDialog(dialog: MaterialAlertDialogBuilder, savedInstanceState: Bundle?) {
        dialog.setTitle("Редактор предметов")
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

        with(binding) {
            viewModel.showColors.observe(
                viewLifecycleOwner
            ) { listWithCurrentPair ->
                adapter = ColorPickerAdapter()
                binding.rvColorPicker.adapter = adapter
                adapter.list = listWithCurrentPair.first
                adapter.setItemClickListener { position: Int ->
                    viewModel.onColorSelect(position)
                    adapter.notifyItemRangeChanged(0, adapter.itemCount)
                }
                viewModel.currentSelectedColorPosition.observe(viewLifecycleOwner) { current: Int ->
                    adapter.setCurrent(current)
                }
            }
            viewModel.deleteBtnVisibility.observe(viewLifecycleOwner) { visibility: Boolean ->
                alertDialog.getButton(
                    AlertDialog.BUTTON_NEUTRAL
                ).visibility = if (visibility) View.VISIBLE else View.GONE
            }

            viewModel.title.observe(
                viewLifecycleOwner
            ) { title: String -> dialog!!.setTitle(title) }
            viewModel.openIconPicker.observe(viewLifecycleOwner) {
                IconPickerDialog().show(
                    requireActivity().supportFragmentManager, null
                )
            }
            viewModel.enablePositiveBtn.observe(viewLifecycleOwner) { enabled: Boolean ->
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = enabled
            }
            viewModel.nameField.observe(
                viewLifecycleOwner
            ) { name: String ->
                if (!etSubjectName.text.contentEquals(name))
                    etSubjectName.setText(name)
            }
            viewModel.icon.observe(viewLifecycleOwner) { iconUrl: String? ->
                GlideApp.with(
                    requireActivity()
                )
                    .`as`(PictureDrawable::class.java)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .listener(
                        SvgColorListener(
                            ivSubjectIc,
                            viewModel.colorIcon.value!!,
                            requireContext()
                        )
                    )
                    .load(iconUrl)
                    .into(ivSubjectIc)
            }
            viewModel.colorIcon.observe(viewLifecycleOwner) { color: Int ->
                ivSubjectIc.post { ViewUtils.paintImageView(ivSubjectIc, color, requireContext()) }
            }

            etSubjectName.textChanges()
                .skipInitialValue()
                .compose(EditTextTransformer())
                .subscribe { name: String -> viewModel.onNameType(name) }

            ivSubjectIc.setOnClickListener { viewModel.onIconClick() }
        }
    }

    companion object {
        const val SUBJECT_ID = "SUBJECT_ID"
        fun newInstance(subjectId: String?): SubjectEditorDialog {
            val fragment = SubjectEditorDialog()
            val args = Bundle()
            args.putString(SUBJECT_ID, subjectId)
            fragment.arguments = args
            return fragment
        }
    }
}