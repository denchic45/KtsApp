package com.denchic45.kts.ui.timetable

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.denchic45.kts.R
import com.denchic45.kts.data.model.DomainModel
import com.denchic45.kts.databinding.FragmentTimetableBinding
import com.denchic45.kts.ui.BaseFragment
import com.denchic45.kts.ui.adapter.EventAdapter
import com.denchic45.kts.ui.adapter.EventAdapter.OnLessonItemClickListener
import com.denchic45.widget.ListStateLayout
import com.denchic45.widget.calendar.WeekCalendarListener
import com.denchic45.widget.calendar.WeekCalendarListener.OnLoadListener
import com.denchic45.widget.calendar.model.WeekItem
import com.example.appbarcontroller.appbarcontroller.AppBarController
import kotlinx.coroutines.flow.collect
import java.time.LocalDate
import kotlin.properties.Delegates

class TimetableFragment :
    BaseFragment<TimetableViewModel, FragmentTimetableBinding>(R.layout.fragment_timetable),
    WeekCalendarListener,
    OnLoadListener {

    override val binding: FragmentTimetableBinding by viewBinding(FragmentTimetableBinding::bind)
    override val viewModel: TimetableViewModel by viewModels { viewModelFactory }
    private lateinit var appBarController: AppBarController

    private var adapter: EventAdapter by Delegates.notNull()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.options_timtable, menu)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(this.binding) {
            val listStateLayout: ListStateLayout = view.findViewById(R.id.listStateLayout)
            listStateLayout.addView(R.layout.state_lessons_day_off, DAY_OFF_VIEW)

            appBarController = AppBarController.findController(requireActivity())

            binding.wcv.weekCalendarListener = this@TimetableFragment

            lifecycleScope.launchWhenStarted {
                viewModel.initTimetable.collect { groupVisibility: Boolean ->
                    adapter = EventAdapter(viewModel.lessonTime, groupVisibility,
                        onLessonItemClickListener = object : OnLessonItemClickListener() {
                        })
                    rvLessons.adapter = adapter

                    rvLessons.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                        override fun onScrolled(
                            recyclerView: RecyclerView,
                            dx: Int,
                            dy: Int
                        ) {
                            startLiftOnScrollElevationOverlayAnimation(
                                recyclerView.canScrollVertically(
                                    -1
                                )
                            )
                        }
                    })

                    appBarController.setLiftOnScroll(false)
                    appBarController.setExpandableIfViewCanScroll(
                        binding.rvLessons,
                        viewLifecycleOwner
                    )

//                    viewModel.showLessonsOfDay.observe(
//                        viewLifecycleOwner,
//                        EventObserver { lessons ->
//                            adapter.submitList(
//                                ArrayList<DomainModel>(lessons),
//                                listStateLayout.getCommitCallback(adapter)
//                            )
//                            rvLessons.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//                                override fun onScrolled(
//                                    recyclerView: RecyclerView,
//                                    dx: Int,
//                                    dy: Int
//                                ) {
//                                    startLiftOnScrollElevationOverlayAnimation(
//                                        recyclerView.canScrollVertically(
//                                            -1
//                                        )
//                                    )
//                                }
//                            })
//                        })

//                    viewModel.showListState.observe(
//                        viewLifecycleOwner,
//                        EventObserver { t ->
//                            adapter.submitList(emptyList()) {
//                                listStateLayout.showView(t)
//                            }
//                        })


                }
            }

            lifecycleScope.launchWhenStarted {
                viewModel.events.collect {
                    when (it) {
                        is TimetableViewModel.EventsState.Events -> {
                            adapter.submitList(
                                ArrayList<DomainModel>(it.events),
                                listStateLayout.getCommitCallback(adapter)
                            )
                        }
                        is TimetableViewModel.EventsState.DayOff -> {
                            adapter.submitList(emptyList()) {
                                listStateLayout.showView(DAY_OFF_VIEW)
                            }
                        }
                    }
                }
            }


            lifecycleScope.launchWhenStarted {
                viewModel.selectedDate.collect { selectDate ->
                    wcv.selectDate = selectDate
                }
            }
        }
    }


    private fun startLiftOnScrollElevationOverlayAnimation(lifted: Boolean) {
        val appBarElevation = 4F
        val fromElevation: Float = if (lifted) 0F else appBarElevation
        val toElevation: Float = if (lifted) appBarElevation else 0F
        if (lifted && this.binding.wcv.elevation == appBarElevation
            || !lifted && this.binding.wcv.elevation == 0F
        ) return
        val elevationOverlayAnimator = ValueAnimator.ofFloat(fromElevation, toElevation)
        elevationOverlayAnimator.duration = 150
        elevationOverlayAnimator.interpolator = LinearInterpolator()
        elevationOverlayAnimator.addUpdateListener { valueAnimator: ValueAnimator ->
            this.binding.wcv.elevation = valueAnimator.animatedValue as Float
        }
        elevationOverlayAnimator.start()
    }

    override fun onDaySelect(date: LocalDate) {
        viewModel.onDaySelect(date)
    }

    override fun onWeekSelect(weekItem: WeekItem) {
        viewModel.onWeekSelect(weekItem)
    }

    override fun onWeekLoad(weekItem: WeekItem) {
        viewModel.onWeekLoad(weekItem)
    }

    companion object {
        const val DAY_OFF_VIEW = "DAY_OFF_VIEW"
        const val GROUP_ID = "TimetableFragment GROUP_ID"

        fun newInstance(groupId: String): TimetableFragment {
            val fragment = TimetableFragment()
            val args = Bundle()
            args.putString(GROUP_ID, groupId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.wcv.removeListeners()
    }
}