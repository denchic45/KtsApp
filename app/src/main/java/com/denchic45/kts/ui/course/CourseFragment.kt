package com.denchic45.kts.ui.course

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.viewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.denchic45.kts.R
import com.denchic45.kts.data.model.domain.CourseSection
import com.denchic45.kts.data.model.domain.Task
import com.denchic45.kts.databinding.FragmentCourseBinding
import com.denchic45.kts.ui.BaseFragment
import com.denchic45.kts.ui.adapter.CourseSectionAdapterDelegate
import com.denchic45.kts.ui.adapter.TaskAdapterDelegate
import com.denchic45.widget.extendedAdapter.adapter
import com.example.appbarcontroller.appbarcontroller.AppBarController
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.*

class CourseFragment :
    BaseFragment<CourseViewModel, FragmentCourseBinding>(R.layout.fragment_course) {

    override val binding: FragmentCourseBinding by viewBinding(FragmentCourseBinding::bind)
    override val viewModel: CourseViewModel by viewModels { viewModelFactory }
    var collapsingToolbarLayout: CollapsingToolbarLayout? = null
    lateinit var fab : FloatingActionButton

    private var mainToolbar: Toolbar? = null

    companion object {
        const val COURSE_UUID = "CourseFragment COURSE_UUID"
    }

    private lateinit var appBarController: AppBarController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fab = requireActivity().findViewById(R.id.fab_main)
        appBarController = AppBarController.findController(requireActivity())
        appBarController.apply {
            mainToolbar = toolbar
            removeView(toolbar) //todo сохранять классический тулбар в переменную и возвращать при выходе
            collapsingToolbarLayout =
                addView(R.layout.toolbar_course) as CollapsingToolbarLayout
            collapsingToolbarLayout!!.findViewById<Toolbar>(R.id.toolbar).apply {
                setNavigationIcon(R.drawable.ic_arrow_back)
                inflateMenu(R.menu.options_course)
                setNavigationOnClickListener {
                    requireActivity().onBackPressed()
                }
            }

//            (activity as AppCompatActivity).setSupportActionBar(courseCollapsingToolbarLayout!!.findViewById(R.id.toolbar) as Toolbar)
            setLiftOnScroll(true)
//            (activity as AppCompatActivity).supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            val adapter = adapter {
                delegates(TaskAdapterDelegate(), CourseSectionAdapterDelegate())
            }
            rvCourseItems.adapter = adapter
            viewModel.showContents.observe(viewLifecycleOwner) {
                adapter.submit(it)
            }
            viewModel.courseName.observe(viewLifecycleOwner) {
                collapsingToolbarLayout!!.title = it
            }
            viewModel.fabVisibility.observe(viewLifecycleOwner) {

                if (it) fab.show() else fab.hide()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        fab.hide()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        appBarController.removeView(collapsingToolbarLayout!!)
        appBarController.addView(mainToolbar)
//        (activity as AppCompatActivity).setSupportActionBar(mainToolbar!!)
    }
}