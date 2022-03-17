package com.denchic45.kts.ui.adminPanel.timetableEditor.eventEditor.lessonEditor

import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.graphics.drawable.PictureDrawable
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import by.kirich1409.viewbindingdelegate.viewBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.denchic45.kts.SvgColorListener
import com.denchic45.kts.R
import com.denchic45.kts.data.model.domain.User
import com.denchic45.kts.databinding.FragmentLessonEditorBinding
import com.denchic45.kts.glideSvg.GlideApp
import com.denchic45.kts.ui.BaseFragment
import com.denchic45.kts.utils.Dimensions
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable

class LessonEditorFragment :
    BaseFragment<LessonEditorViewModel, FragmentLessonEditorBinding>(R.layout.fragment_lesson_editor) {
    override val binding: FragmentLessonEditorBinding by viewBinding(FragmentLessonEditorBinding::bind)
    override val viewModel: LessonEditorViewModel by viewModels { viewModelFactory }

    override val navController: NavController by lazy {
        Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            viewModel.subjectField.observe(viewLifecycleOwner) { (_, name, iconUrl, colorName) ->
                val colorId =
                    resources.getIdentifier(colorName, "color", requireActivity().packageName)
                GlideApp.with(requireActivity())
                    .`as`(PictureDrawable::class.java)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .listener(
                        SvgColorListener(
                            ivSubjectIc,
                            colorId,
                            requireContext()
                        )
                    )
                    .load(iconUrl)
                    .into(ivSubjectIc)
                tvSubjectName.text = name
            }

            viewModel.showErrorField.observe(viewLifecycleOwner) { (first, second) ->
                val rlView = view.findViewById<ViewGroup>(first)
                if (second) {
                    if (rlView.findViewWithTag<View?>(first) == null) {
                        rlView.addView(createErrorImageView(first))
                    }
                } else {
                    val ivError = rlView.findViewWithTag<ImageView>(first)
                    if (ivError != null) {
                        rlView.removeView(ivError)
                    }
                }
            }

            viewModel.teachersField.observe(viewLifecycleOwner) { teachers: List<User> ->
                cpTeachers.removeViews(0, cpTeachers.childCount - 1)
                for (i in teachers.indices) {
                    val chip = createChip(teachers[i])
                    chip.setOnCloseIconClickListener { v: View? ->
                        viewModel.onRemoveTeacherItemClick(
                            i
                        )
                    }
                    cpTeachers.addView(chip, cpTeachers.childCount - 1)
                }
            }
            viewModel.openChoiceOfGroupSubject.observe(
                viewLifecycleOwner
            ) {
                navController.navigate(R.id.action_lessonEditorFragment_to_choiceOfGroupSubjectFragment) }

            viewModel.openChoiceOfTeacher.observe(
                viewLifecycleOwner
            ) { navController.navigate(R.id.action_lessonEditorFragment_to_teacherChooserFragment) }

            rlSubject.setOnClickListener { viewModel.onSubjectClick() }
            cpAddTeacher.setOnClickListener { viewModel.onAddTeacherItemClick() }
        }
    }

    private fun createChip(teacher: User): Chip {
        val chip = Chip(requireContext())
        chip.setChipDrawable(
            ChipDrawable.createFromAttributes(
                requireContext(),
                null,
                0,
                R.style.Widget_MaterialComponents_Chip_Entry
            )
        )
        chip.text = teacher.fullName
        chip.chipBackgroundColor =
            ColorStateList.valueOf(ContextCompat.getColor(requireContext(), android.R.color.white))
        chip.chipStrokeColor =
            ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.white_gray))
        chip.chipStrokeWidth = Dimensions.dpToPx(1, requireContext()).toFloat()
        //        chip.setHeight(dpToPx(44, requireContext()));
//        chip.setChipIconSize(dpToPx(44, requireContext()));
        chip.ellipsize = TextUtils.TruncateAt.END
        Glide.with(requireActivity())
            .load(teacher.photoUrl)
            .circleCrop()
            .listener(object : RequestListener<Drawable?> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any,
                    target: Target<Drawable?>,
                    isFirstResource: Boolean
                ): Boolean {
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any,
                    target: Target<Drawable?>,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    chip.chipIcon = resource
                    return false
                }
            }).preload()
        return chip
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