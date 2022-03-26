package com.denchic45.kts.ui.adminPanel.timetableEditor.eventEditor

import android.content.DialogInterface
import android.os.Bundle
import android.os.Parcel
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.denchic45.kts.R
import com.denchic45.kts.databinding.FragmentEventEditorBinding
import com.denchic45.kts.rx.EditTextTransformer
import com.denchic45.kts.ui.BaseFragment
import com.denchic45.kts.utils.Dimensions
import com.example.appbarcontroller.appbarcontroller.AppBarController
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jakewharton.rxbinding4.widget.textChanges
import java.util.*


class EventEditorFragment : BaseFragment<EventEditorViewModel, FragmentEventEditorBinding>(
    R.layout.fragment_event_editor,
    R.menu.options_lesson_editor
) {
    private lateinit var toolbarEventEditor: View
    private lateinit var tvTitleBar: TextView
    private var childNavController: NavController? = null
    private var controller: AppBarController? = null
    override val binding: FragmentEventEditorBinding by viewBinding(FragmentEventEditorBinding::bind)
    override val viewModel: EventEditorViewModel by activityViewModels { viewModelFactory }
    private var navHostFragment: Fragment? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbarEventEditor = LayoutInflater.from(context)
            .inflate(
                R.layout.toolbar_event_editor,
                requireActivity().findViewById(R.id.toolbar),
                false
            )
        tvTitleBar = toolbarEventEditor.findViewById(R.id.tv_toolbar)

        with(binding) {
            viewModel.dateField.observe(
                viewLifecycleOwner
            ) { text -> tvDate.text = text }

            viewModel.orderField.observe(
                viewLifecycleOwner
            ) { text1 ->
                tvOrder.text = text1
            }
            viewModel.roomField.observe(
                viewLifecycleOwner
            ) { text -> if (etRoom.text.toString() != text) etRoom.setText(text) }
            viewModel.showErrorField.observe(viewLifecycleOwner) { (first, second) ->
                val rlView = view.findViewById<ViewGroup>(first)
                if (second) {
                    if (rlView.findViewWithTag<View>(first) == null) {
                        rlView.addView(createErrorImageView(first))
                    }
                } else {
                    val ivError = rlView.findViewWithTag<ImageView>(first)
                    if (ivError != null) {
                        rlView.removeView(ivError)
                    }
                }
            }

            fab.setOnClickListener { viewModel.onFabClick() }
            toolbarEventEditor.setOnClickListener { viewModel.onToolbarClick() }

            rlDate.setOnClickListener { viewModel.onDateClick() }

            etRoom.textChanges()
                .compose(EditTextTransformer())
                .subscribe(viewModel::onRoomType)

            tvOrder.textChanges()
                .compose(EditTextTransformer())
                .filter(String::isNotEmpty)
                .map(String::toInt)
                .subscribe(viewModel::onOrderType)
        }

        viewModel.openDatePicker.observe(viewLifecycleOwner) {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setCalendarConstraints(
                    CalendarConstraints.Builder()
                        .setValidator(object : CalendarConstraints.DateValidator {
                            override fun isValid(date: Long): Boolean {
                                val cal = Calendar.getInstance()
                                cal.timeInMillis = date
                                val dayOfWeek = cal[Calendar.DAY_OF_WEEK]
                                return dayOfWeek != Calendar.SUNDAY
                            }

                            override fun describeContents(): Int {
                                return 0
                            }

                            override fun writeToParcel(dest: Parcel, flags: Int) {}
                        }).build()
                )
                .build()
            datePicker.addOnPositiveButtonClickListener { selection: Long? ->
                viewModel.onDateSelected(
                    selection!!
                )
            }
            datePicker.show(childFragmentManager, null)
        }

        viewModel.showListOfEventTypes.observe(
            viewLifecycleOwner
        ) { (first, second) ->
            MaterialAlertDialogBuilder(
                requireContext(), R.style.MaterialAlertDialog_Rounded
            )
                .setSingleChoiceItems(first, second) { dialog: DialogInterface, which: Int ->
                    dialog.dismiss()
                    viewModel.onEventTypeSelect(which)
                }
                .setTitle("Выберите тип события")
                .create().show()
        }
        viewModel.showDetailEditor.observe(
            viewLifecycleOwner
        ) { fragmentId: Int -> setStartFragment(fragmentId) }
    }

    override fun onStart() {
        super.onStart()
        controller = AppBarController.findController(requireActivity())
        (activity as AppCompatActivity?)!!.supportActionBar?.title = null
        controller!!.toolbar.addView(toolbarEventEditor)

        navHostFragment = childFragmentManager.findFragmentById(R.id.nav_host_fragment)
        childNavController = findNavController(navHostFragment!!.requireView())
    }

    private fun setStartFragment(fragmentId: Int) {
        if (fragmentId == 0) {
            navHostFragment!!.requireView().visibility = View.GONE
            return
        }
        navHostFragment!!.requireView().visibility = View.VISIBLE
        val navInflater = childNavController!!.navInflater
        val graph = navInflater.inflate(R.navigation.navigation_event_detail_editor)
        graph.setStartDestination(fragmentId)
        childNavController!!.graph = graph
    }

    override fun onStop() {
        super.onStop()
        controller!!.toolbar.removeView(toolbarEventEditor)
    }

    private fun createErrorImageView(viewId: Int): ImageView {
        val ivError = ImageView(context)
        ivError.tag = viewId
        ivError.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_error))
        val imageSize = Dimensions.dpToPx(24, requireContext())
        val layoutParams = RelativeLayout.LayoutParams(imageSize, imageSize)
        layoutParams.marginEnd = Dimensions.dpToPx(16, requireContext())
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END, RelativeLayout.TRUE)
        layoutParams.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE)
        ivError.layoutParams = layoutParams
        return ivError
    }
}