package com.denchic45.kts.ui.adminPanel.timetableEditor.eventEditor

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.denchic45.kts.R
import com.denchic45.kts.databinding.FragmentEventEditorBinding
import com.denchic45.kts.rx.EditTextTransformer
import com.denchic45.kts.ui.base.BaseFragment
import com.denchic45.kts.util.Dimensions
import com.denchic45.kts.util.collectWhenResumed
import com.example.appbarcontroller.appbarcontroller.AppBarController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jakewharton.rxbinding4.widget.textChanges


class EventEditorFragment : BaseFragment<EventEditorViewModel, FragmentEventEditorBinding>(
    R.layout.fragment_event_editor,
    R.menu.options_lesson_editor
) {
    private var childNavController: NavController? = null
    private var controller: AppBarController? = null
    override val binding: FragmentEventEditorBinding by viewBinding(FragmentEventEditorBinding::bind)
    override val viewModel: EventEditorViewModel by activityViewModels { viewModelFactory }
    private var navHostFragment: Fragment? = null

    private val toolbar by lazy { requireActivity().findViewById<Toolbar>(R.id.toolbar) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

            toolbar.findViewById<LinearLayout>(R.id.toolbar_event_editor)
                .setOnClickListener { viewModel.onToolbarClick() }

            etRoom.textChanges()
                .compose(EditTextTransformer())
                .subscribe(viewModel::onRoomType)

            tvOrder.textChanges()
                .compose(EditTextTransformer())
                .filter(String::isNotEmpty)
                .map(String::toInt)
                .subscribe(viewModel::onOrderType)
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

    override fun collectOnShowToolbarTitle() {
        viewModel.showToolbarTitle.collectWhenResumed(lifecycleScope) {
            toolbar.findViewById<TextView>(R.id.tv_toolbar).text = it
        }
    }

    override fun onStart() {
        super.onStart()
        controller = AppBarController.findController(requireActivity())

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