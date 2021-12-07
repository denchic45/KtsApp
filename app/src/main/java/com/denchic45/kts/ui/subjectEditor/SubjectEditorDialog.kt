package com.denchic45.kts.ui.subjectEditor

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.PictureDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.denchic45.SvgColorListener
import com.denchic45.kts.R
import com.denchic45.kts.data.model.domain.ListItem
import com.denchic45.kts.databinding.FragmentSubjectEditorBinding
import com.denchic45.kts.glideSvg.GlideApp
import com.denchic45.kts.rx.EditTextTransformer
import com.denchic45.kts.ui.BaseDialogFragment
import com.denchic45.kts.ui.adapter.ColorPickerAdapter
import com.denchic45.kts.ui.confirm.ConfirmDialog
import com.denchic45.kts.ui.iconPicker.IconPickerDialog
import com.denchic45.kts.utils.ViewUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jakewharton.rxbinding4.widget.textChanges
import dagger.android.support.AndroidSupportInjection

class SubjectEditorDialog :
    BaseDialogFragment<SubjectEditorViewModel, FragmentSubjectEditorBinding>() {

    override val binding: FragmentSubjectEditorBinding by viewBinding(FragmentSubjectEditorBinding::bind)
    override val viewModel: SubjectEditorViewModel by viewModels { viewModelFactory }
    private lateinit var alertDialog: AlertDialog
    private lateinit var adapter: ColorPickerAdapter
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog =
            MaterialAlertDialogBuilder(requireActivity(), R.style.MaterialAlertDialog_Rounded)
                .setView(binding.root)
                .setTitle("Редактор предметов")
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
            btnDelete.setOnClickListener { v: View? -> viewModel.onDeleteClick() }
            btnPositive.setOnClickListener { v: View? -> viewModel.onPositiveClick() }
        }
        return alertDialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            viewModel.showColors.observe(
                viewLifecycleOwner,
                { listWithCurrentPair: Pair<List<ListItem>, Int> ->
                    adapter = ColorPickerAdapter()
                    binding.rvColorPicker.adapter = adapter
                    adapter.list = listWithCurrentPair.first
                    adapter.setItemClickListener { position: Int ->
                        viewModel.onColorSelect(position)
                        adapter.notifyDataSetChanged()
                    }
                    viewModel.currentSelectedColor.observe(viewLifecycleOwner, { current: Int ->
                        adapter.setCurrent(current)
                    })
                })
            viewModel.deleteBtnVisibility.observe(viewLifecycleOwner, { visibility: Boolean ->
                alertDialog.getButton(
                    AlertDialog.BUTTON_NEUTRAL
                ).visibility = if (visibility) View.VISIBLE else View.GONE
            })
            viewModel.openConfirmation.observe(
                viewLifecycleOwner,
                { stringStringPair: Pair<String, String> ->
                    ConfirmDialog.newInstance(stringStringPair.first, stringStringPair.second).show(
                        childFragmentManager, null
                    )
                })
            viewModel.title.observe(
                viewLifecycleOwner,
                { title: String -> dialog!!.setTitle(title) })
            viewModel.openIconPicker.observe(viewLifecycleOwner, {
                IconPickerDialog().show(
                    requireActivity().supportFragmentManager, null
                )
            })
            viewModel.enablePositiveBtn.observe(viewLifecycleOwner, { enabled: Boolean ->
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = enabled
            })
            viewModel.nameField.observe(
                viewLifecycleOwner,
                { name: String -> etSubjectName.setText(name) })
            viewModel.icon.observe(viewLifecycleOwner, { iconUrl: String? ->
                GlideApp.with(
                    requireActivity()
                )
                    .`as`(PictureDrawable::class.java)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .listener(SvgColorListener(ivSubjectIc, viewModel.colorIcon.value!!, activity))
                    .load(iconUrl)
                    .into(ivSubjectIc)
            })
            viewModel.colorIcon.observe(viewLifecycleOwner, { color: Int ->
                ivSubjectIc.post { ViewUtils.paintImageView(ivSubjectIc, color, context) }
            })
            viewModel.showMessage.observe(viewLifecycleOwner, { message: String ->
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            })
            viewModel.finish.observe(viewLifecycleOwner, { dismiss() })
            etSubjectName.textChanges()
                .compose(EditTextTransformer())
                .subscribe { name: String -> viewModel.onNameType(name) }
            ivSubjectIc.setOnClickListener { viewModel.onIconClick() }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        AndroidSupportInjection.inject(this)
    }

    companion object {
        const val SUBJECT_UUID = "SUBJECT_UUID"
        fun newInstance(subjectUuid: String?): SubjectEditorDialog {
            val fragment = SubjectEditorDialog()
            val args = Bundle()
            args.putString(SUBJECT_UUID, subjectUuid)
            fragment.arguments = args
            return fragment
        }
    }
}