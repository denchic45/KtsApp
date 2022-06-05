package com.denchic45.kts.ui.iconPicker

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.GridView
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.denchic45.kts.R
import com.denchic45.kts.databinding.FragmentIconPickerBinding
import com.denchic45.kts.ui.base.BaseDialogFragment
import com.denchic45.kts.ui.adapter.IconPickerAdapter
import com.denchic45.kts.utils.collectWhenStarted
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class IconPickerDialog :
    BaseDialogFragment<IconPickerViewModel, FragmentIconPickerBinding>(R.layout.fragment_icon_picker) {
    override val viewModel: IconPickerViewModel by viewModels { viewModelFactory }
    override val binding: FragmentIconPickerBinding by viewBinding(FragmentIconPickerBinding::bind)
    private var gvIconPicker: GridView? = null
    private var adapter: IconPickerAdapter? = null

//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        return inflater.inflate(R.layout.fragment_icon_picker, container, false)
//    }

    override fun onBuildDialog(dialog: MaterialAlertDialogBuilder, savedInstanceState: Bundle?) {
        dialog.setTitle("Выбрать иконку предмета")
            .setNegativeButton("Отмена", null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        gvIconPicker = binding.root.findViewById(R.id.gv_icon_picker)
        adapter = IconPickerAdapter(requireActivity())
        gvIconPicker!!.adapter = adapter
        viewModel.showIcons.collectWhenStarted(
            lifecycleScope
        ) { list -> adapter!!.addAll(list) }
        gvIconPicker!!.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position: Int, _ ->
                viewModel.onIconItemClick(position)
            }
    }
}