package com.denchic45.kts.ui.adminPanel.timetableEditor.eventEditor.lessonEditor

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.graphics.drawable.PictureDrawable
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.denchic45.EventObserver
import com.denchic45.SvgColorListener
import com.denchic45.kts.R
import com.denchic45.kts.data.model.domain.User
import com.denchic45.kts.databinding.FragmentLessonEditorBinding
import com.denchic45.kts.glideSvg.GlideApp
import com.denchic45.kts.ui.BaseFragment
import com.denchic45.kts.utils.Dimensions
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar
import dagger.android.support.AndroidSupportInjection

class LessonEditorFragment : BaseFragment<LessonEditorViewModel, FragmentLessonEditorBinding>() {
    override val binding: FragmentLessonEditorBinding by viewBinding(FragmentLessonEditorBinding::bind)
    override val viewModel: LessonEditorViewModel by viewModels { viewModelFactory }
    private var tvSubjectName: TextView? = null
    private var cpAddTeacher: Chip? = null
    private var ivSubjectIc: ImageView? = null
    private var rlSubject: RelativeLayout? = null
    private var cpTeachers: ChipGroup? = null
    private var navController: NavController? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_lesson_editor, container, false)
        cpAddTeacher = root.findViewById(R.id.cp_add_teacher)
        rlSubject = root.findViewById(R.id.rl_subject)
        ivSubjectIc = root.findViewById(R.id.iv_subject_ic)
        tvSubjectName = root.findViewById(R.id.tv_subject_name)
        cpTeachers = root.findViewById(R.id.cp_teachers)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.subjectField.observe(viewLifecycleOwner) { (_, name, iconUrl, colorName) ->
            val colorId = resources.getIdentifier(colorName, "color", requireActivity().packageName)
            GlideApp.with(requireActivity())
                .`as`(PictureDrawable::class.java)
                .transition(DrawableTransitionOptions.withCrossFade())
                .listener(SvgColorListener(ivSubjectIc, colorId, activity))
                .load(iconUrl)
                .into(ivSubjectIc!!)
            tvSubjectName!!.text = name
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
            cpTeachers!!.removeViews(0, cpTeachers!!.childCount - 1)
            for (i in teachers.indices) {
                val chip = createChip(teachers[i])
                chip.setOnCloseIconClickListener { v: View? ->
                    viewModel.onRemoveTeacherItemClick(
                        i
                    )
                }
                cpTeachers!!.addView(chip, cpTeachers!!.childCount - 1)
            }
        }
        viewModel.openChoiceOfGroupSubject.observe(
            viewLifecycleOwner
        ) { navController!!.navigate(R.id.action_lessonEditorFragment_to_choiceOfGroupSubjectFragment) }
        viewModel.openChoiceOfTeacher.observe(
            viewLifecycleOwner
        ) { navController!!.navigate(R.id.action_lessonEditorFragment_to_choiceOfCuratorFragment) }
        viewModel.showMessage.observe(
            viewLifecycleOwner,
            EventObserver { message: String? ->
                Snackbar.make(
                    view,
                    message!!,
                    Snackbar.LENGTH_SHORT
                ).show()
            })

//        adapter = new UserAdapter(viewModel::onTeacherItemClick);
//        nsvTeachers.setAdapter(adapter);
        rlSubject!!.setOnClickListener { v: View? -> viewModel.onSubjectClick() }
        cpAddTeacher!!.setOnClickListener { v: View? -> viewModel.onAddTeacherItemClick() }
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

    override fun onStart() {
        super.onStart()
        navController = findNavController(requireActivity(), R.id.nav_host_fragment)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        AndroidSupportInjection.inject(this)
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