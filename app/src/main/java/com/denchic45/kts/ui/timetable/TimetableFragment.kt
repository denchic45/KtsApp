package com.denchic45.kts.ui.timetable

import android.animation.ValueAnimator
import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.denchic45.EventObserver
import com.denchic45.kts.R
import com.denchic45.kts.data.model.DomainModel
import com.denchic45.kts.data.model.domain.Event
import com.denchic45.kts.databinding.FragmentTimetableBinding
import com.denchic45.kts.di.viewmodel.ViewModelFactory
import com.denchic45.kts.ui.adapter.EventAdapter
import com.denchic45.kts.ui.adapter.EventAdapter.OnLessonItemClickListener
import com.denchic45.kts.ui.main.MainViewModel
import com.denchic45.widget.ListStateLayout
import com.denchic45.widget.calendar.WeekCalendarListener
import com.denchic45.widget.calendar.WeekCalendarListener.OnLoadListener
import com.denchic45.widget.calendar.WeekCalendarView
import com.denchic45.widget.calendar.model.Week
import com.example.appbarcontroller.appbarcontroller.AppBarController
import com.google.android.material.animation.AnimationUtils
import dagger.android.support.AndroidSupportInjection
import java.util.*
import javax.inject.Inject

class TimetableFragment : Fragment(R.layout.fragment_timetable), WeekCalendarListener,
    OnLoadListener {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory<TimetableViewModel>
    private val viewModel: TimetableViewModel by viewModels { viewModelFactory }

    private lateinit var rvLessons: RecyclerView
    private lateinit var wcv: WeekCalendarView
    private var adapter: EventAdapter? = null
    private val mainViewModel: MainViewModel by activityViewModels()
    private val viewBinding by viewBinding(FragmentTimetableBinding::bind)
    private lateinit var appBarController: AppBarController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.options_timtable, menu)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_timetable, container, false)
    }

    override fun onResume() {
        super.onResume()
        viewModel.title.value = viewModel.title.value

        appBarController.setLiftOnScroll(false)
        appBarController.setExpandableIfViewCanScroll(viewBinding.rvLessons, viewLifecycleOwner)
        viewModel.title.observe(
            viewLifecycleOwner,
            { title: String ->
                (requireActivity() as AppCompatActivity).supportActionBar!!.title =
                    title
            })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(viewBinding) {
            this@TimetableFragment.rvLessons = rvLessons
            this@TimetableFragment.wcv = wcv
            val listStateLayout: ListStateLayout = view.findViewById(R.id.listStateLayout)
            listStateLayout.addView(R.layout.state_lessons_day_off, DAY_OFF_VIEW)
            viewModel.initTimetable.observe(viewLifecycleOwner, { groupVisibility: Boolean? ->
                appBarController = AppBarController.findController(
                    requireActivity()
                )

                mainViewModel.selectedDate.observe(viewLifecycleOwner, { selectDate: Date? ->
                    wcv.setSelectDate(
                        selectDate!!
                    )
                })
                viewBinding.wcv.setListener(this@TimetableFragment)
                adapter = EventAdapter(viewModel.lessonTime, groupVisibility!!,

                    onLessonItemClickListener = object : OnLessonItemClickListener() {
                        override fun onHomeworkChecked(checked: Boolean) {
                            viewModel.onTaskChecked(checked)
                        }
                    })
                rvLessons.adapter = adapter
                viewModel.showLessonsOfDay.observe(
                    viewLifecycleOwner,
                    EventObserver { lessons: List<Event> ->
                                adapter!!.submitList(
                                    ArrayList<DomainModel>(lessons),
                                    listStateLayout.getCommitCallback(adapter)
                                )
                        rvLessons.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                                startLiftOnScrollElevationOverlayAnimation(
                                    recyclerView.canScrollVertically(
                                        -1
                                    )
                                )
                            }
                        })
                    })
            })

            viewModel.showListState.observe(
                viewLifecycleOwner,
                EventObserver { viewKey: String? ->
                    adapter!!.submitList(emptyList()) {
                        listStateLayout.showView(viewKey)
                    }
                })
        }


    }

    private fun startLiftOnScrollElevationOverlayAnimation(lifted: Boolean) {
        val appBarElevation = resources.getDimension(R.dimen.design_appbar_elevation)
        val fromElevation: Float = if (lifted) 0F else appBarElevation
        val toElevation: Float = if (lifted) appBarElevation else 0F
        if (lifted && viewBinding.wcv.elevation == appBarElevation
            || !lifted && viewBinding.wcv.elevation == 0f
        ) return
        val elevationOverlayAnimator = ValueAnimator.ofFloat(fromElevation, toElevation)
        elevationOverlayAnimator.duration =
            resources.getInteger(R.integer.app_bar_elevation_anim_duration).toLong()
        elevationOverlayAnimator.interpolator = AnimationUtils.LINEAR_INTERPOLATOR
        elevationOverlayAnimator.addUpdateListener { valueAnimator: ValueAnimator ->
            viewBinding.wcv.elevation = valueAnimator.animatedValue as Float
        }
        elevationOverlayAnimator.start()
    }

    override fun onDaySelect(date: Date) {
        viewModel.onDaySelect(date)
    }

    override fun onWeekSelect(week: Week) {
        viewModel.onWeekSelect(week)
    }

    override fun onWeekLoad(week: Week) {
        viewModel.onWeekLoad(week)
    }

    companion object {
        const val DAY_OFF_VIEW = "DAY_OFF_VIEW"
        const val GROUP_ID = "GROUP_ID"

        @JvmStatic
        fun newInstance(groupId: String): TimetableFragment {
            val fragment = TimetableFragment()
            val args = Bundle()
            args.putString(GROUP_ID, groupId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        AndroidSupportInjection.inject(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        rvLessons.adapter = null
        wcv.removeListeners()
    }
}