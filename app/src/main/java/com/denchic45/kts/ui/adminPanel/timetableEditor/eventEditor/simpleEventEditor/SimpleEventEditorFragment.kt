package com.denchic45.kts.ui.adminPanel.timetableEditor.eventEditor.simpleEventEditor

import android.graphics.drawable.PictureDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.ListPopupWindow
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.denchic45.kts.SvgColorListener
import com.denchic45.kts.R
import com.denchic45.kts.customPopup.ListPopupWindowAdapter
import com.denchic45.kts.data.model.domain.ListItem
import com.denchic45.kts.data.model.domain.SimpleEventDetails
import com.denchic45.kts.databinding.FragmentSimpleEventBinding
import com.denchic45.kts.glideSvg.GlideApp
import com.denchic45.kts.ui.base.BaseFragment
import com.denchic45.kts.util.Dimensions

class SimpleEventEditorFragment :
    BaseFragment<SimpleEventEditorViewModel, FragmentSimpleEventBinding>(R.layout.fragment_simple_event) {
    override val viewModel: SimpleEventEditorViewModel by viewModels { viewModelFactory }
    override val binding: FragmentSimpleEventBinding by viewBinding(FragmentSimpleEventBinding::bind)
    private var popupWindow: ListPopupWindow? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        popupWindow = ListPopupWindow(requireContext())
        with(binding) {
            viewModel.showSelectedEvent.observe(
                viewLifecycleOwner
            ) { eventDetails: SimpleEventDetails ->
                GlideApp.with(requireContext())
                    .`as`(PictureDrawable::class.java)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .listener(
                        SvgColorListener(
                            ivSubjectIc,
                            resources.getIdentifier(
                                eventDetails.color,
                                "color",
                                requireContext().packageName
                            ),
                            requireContext()
                        )
                    )
                    .load(eventDetails.iconUrl)
                    .into(ivSubjectIc)
                tvEventName.text = eventDetails.name
                popupWindow!!.dismiss()
            }
            viewModel.showEvents.observe(viewLifecycleOwner) { eventItems: List<ListItem> ->
                if (popupWindow!!.isShowing) {
                    popupWindow!!.dismiss()
                    return@observe
                }
                popupWindow!!.anchorView = rlEvent
                val adapter = ListPopupWindowAdapter(requireContext(), eventItems)
                popupWindow!!.setAdapter(adapter)
                popupWindow!!.setOnItemClickListener { parent: AdapterView<*>, view1: View, position: Int, id: Long ->
                    viewModel.onEventSelect(
                        position
                    )
                }
                popupWindow!!.show()
            }
            rlEvent.setOnClickListener { viewModel.onEventClick() }
            viewModel.showErrorField.observe(
                viewLifecycleOwner
            ) { idWithEnablePair: Pair<Int, Boolean> ->
                val rlView = view.findViewById<ViewGroup>(idWithEnablePair.first)
                if (idWithEnablePair.second) {
                    if (rlView.findViewWithTag<View>(idWithEnablePair.first) == null) {
                        rlView.addView(createErrorImageView(idWithEnablePair.first))
                    }
                } else {
                    val ivError = rlView.findViewWithTag<ImageView>(idWithEnablePair.first)
                    if (ivError != null) {
                        rlView.removeView(ivError)
                    }
                }
            }
        }
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

    companion object {
        fun newInstance(): SimpleEventEditorFragment {
            return SimpleEventEditorFragment()
        }
    }
}