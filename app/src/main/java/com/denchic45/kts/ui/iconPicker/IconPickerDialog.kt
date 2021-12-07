package com.denchic45.kts.ui.iconPicker

import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.GridView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.denchic45.kts.R
import com.denchic45.kts.databinding.FragmentIconPickerBinding
import com.denchic45.kts.ui.BaseDialogFragment
import com.denchic45.kts.ui.adapter.IconPickerAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.jetbrains.annotations.Contract

class IconPickerDialog : BaseDialogFragment<IconPickerViewModel, FragmentIconPickerBinding>() {
    override val viewModel: IconPickerViewModel by viewModels { viewModelFactory }
    override val binding: FragmentIconPickerBinding by viewBinding(FragmentIconPickerBinding::bind)
    private var gvIconPicker: GridView? = null
    private lateinit var alertDialog: AlertDialog
    private var adapter: IconPickerAdapter? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_icon_picker, container, false)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        gvIconPicker = binding.root.findViewById(R.id.gv_icon_picker)
        val dialog =
            MaterialAlertDialogBuilder(requireActivity(), R.style.MaterialAlertDialog_Rounded)
                .setView(binding.root)
                .setTitle("Выбрать иконку предмета")
                .setNegativeButton("Отмена", null)
        alertDialog = dialog.create()
        alertDialog.window!!.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        return alertDialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = IconPickerAdapter(requireActivity())
        gvIconPicker!!.adapter = adapter
        viewModel.finish.observe(viewLifecycleOwner, { dismiss() })
        viewModel.showIcons.observe(
            viewLifecycleOwner,
            { collection: List<Uri?> -> adapter!!.addAll(collection) })
        gvIconPicker!!.onItemClickListener =
            AdapterView.OnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
                viewModel.onIconItemClick(position)
            }
    }

    companion object {
        @Contract(" -> new")
        fun newInstance(): IconPickerDialog {
            return IconPickerDialog()
        }
    }
}