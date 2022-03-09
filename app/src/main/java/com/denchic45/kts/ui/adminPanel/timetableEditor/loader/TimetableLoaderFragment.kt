package com.denchic45.kts.ui.adminPanel.timetableEditor.loader

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Parcel
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.adapter.FragmentViewHolder
import by.kirich1409.viewbindingdelegate.viewBinding
import com.denchic45.kts.R
import com.denchic45.kts.data.model.DomainModel
import com.denchic45.kts.databinding.FragmentTimetableLoaderBinding
import com.denchic45.kts.ui.BaseFragment
import com.denchic45.kts.ui.adapter.EventAdapter
import com.denchic45.kts.ui.adapter.EventAdapter.*
import com.denchic45.kts.ui.adapter.OnItemMoveListener
import com.denchic45.kts.ui.adapter.PreferenceSwitchAdapterDelegate
import com.denchic45.kts.ui.adapter.preferenceAdapter
import com.denchic45.kts.ui.adminPanel.timetableEditor.eventEditor.EventEditorActivity
import com.denchic45.kts.ui.adminPanel.timetableEditor.loader.lessonsOfDay.EventsFragment
import com.denchic45.kts.utils.FilePicker
import com.denchic45.kts.utils.path
import com.denchic45.kts.utils.toast
import com.denchic45.widget.extendedAdapter.DelegationAdapterExtended
import com.denchic45.widget.extendedAdapter.extension.check
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.flow.collect
import java.io.File
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import java.util.*


class TimetableLoaderFragment :
    BaseFragment<TimetableLoaderViewModel, FragmentTimetableLoaderBinding>(R.layout.fragment_timetable_loader),
    OnEditEventItemClickListener,
    OnCreateLessonClickListener,
    OnItemMoveListener {
    override val binding: FragmentTimetableLoaderBinding by viewBinding(
        FragmentTimetableLoaderBinding::bind
    )
    override val viewModel: TimetableLoaderViewModel by viewModels { viewModelFactory }
    private lateinit var groupTimetablesAdapter: GroupTimetablesAdapter
    private lateinit var preferenceAdapter: DelegationAdapterExtended
    private val viewBinding: FragmentTimetableLoaderBinding by viewBinding(
        FragmentTimetableLoaderBinding::bind
    )

    private lateinit var filePicker: FilePicker

    override fun onResume() {
        super.onResume()
        setHasOptionsMenu(true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        filePicker = FilePicker(this, { result ->
            with(result) {
                if (resultCode == Activity.RESULT_OK && data!!.data != null) {
                    viewModel.onSelectedFile(File(requireContext().path(data!!.data!!)))
                }
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(viewBinding) {
            groupTimetablesAdapter = GroupTimetablesAdapter(
                childFragmentManager,
                lifecycle,
                this@TimetableLoaderFragment,
                this@TimetableLoaderFragment,
                this@TimetableLoaderFragment
            )
            vpTimetablePreview.adapter = groupTimetablesAdapter
            TabLayoutMediator(
                tlTimetableGroup,
                vpTimetablePreview
            ) { tab: TabLayout.Tab, position: Int ->
                tab.text = viewModel.groupNames[position]
            }.attach()
            tlTimetableGroup.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    viewModel.onGroupTimetableSelect(tab.position)
                }

                override fun onTabUnselected(tab: TabLayout.Tab) {}
                override fun onTabReselected(tab: TabLayout.Tab) {}
            })
            preferenceAdapter = preferenceAdapter {
                onClick(viewModel::onPreferenceItemClick)

                extensions {
                    check<PreferenceSwitchAdapterDelegate.PreferenceSwitchItemHolder>(
                        view = { it.binding.sw },
                        onCheck = { position, checked ->
                            viewModel.onPreferenceItemCheck(
                                position,
                                checked
                            )
                        }
                    )
                }
            }

            rvPreference.adapter = preferenceAdapter

            btnTimetableLoad.setOnClickListener {
                viewModel.onLoadTimetableDocClick()
            }

            btnCreateEmpty.setOnClickListener {
                val picker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Выберите первый день необходимой недели")
                    .setCalendarConstraints(
                        CalendarConstraints.Builder()
                            .setValidator(object : CalendarConstraints.DateValidator {
                                override fun isValid(date: Long): Boolean {
                                    val cal = Calendar.getInstance()
                                    cal.timeInMillis = date
                                    val dayOfWeek = cal[Calendar.DAY_OF_WEEK]
                                    return dayOfWeek == Calendar.MONDAY
                                }

                                override fun describeContents(): Int {
                                    return 0
                                }

                                override fun writeToParcel(dest: Parcel, flags: Int) {}
                            }).build()
                    )
                    .setSelection(
                        Date.from(
                            LocalDate.now()
                                .with(TemporalAdjusters.next(DayOfWeek.MONDAY))
                                .atStartOfDay(ZoneId.systemDefault()).toInstant()
                        ).time
                    ).build()
                picker.show(childFragmentManager, null)
                picker.addOnPositiveButtonClickListener { selection ->
                    viewModel.onFirstDateOfNewTimetableSelect(selection)
                }
            }

            lifecycleScope.launchWhenStarted {
                viewModel.timetables.collect {
                    groupTimetablesAdapter.updateTimetable(it)
                }
            }

            lifecycleScope.launchWhenStarted {
                viewModel.tabs.collect { groupNames ->
                    toast("tabs ${groupNames.size}")
                    TabLayoutMediator(
                        tlTimetableGroup,
                        vpTimetablePreview
                    ) { tab: TabLayout.Tab, position: Int ->
                        tab.text = groupNames[position]
                    }.attach()

                    tlTimetableGroup.removeAllTabs()
                    for (i in groupNames) {
                        tlTimetableGroup.addTab(tlTimetableGroup.newTab())
                    }
                }

            }

            viewModel.enableEditMode.observe(viewLifecycleOwner) { enable: Boolean ->
                vpTimetablePreview.post {
                    groupTimetablesAdapter.editMode = enable
                }
            }

            viewModel.showPage.observe(
                viewLifecycleOwner
            ) { position -> vsTimetableLoader.displayedChild = position }


            vpTimetablePreview.adapter = groupTimetablesAdapter
            vpTimetablePreview.offscreenPageLimit = 5

            tlTimetableGroup.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    viewModel.onGroupTimetableSelect(tab.position)
                }

                override fun onTabUnselected(tab: TabLayout.Tab) {}
                override fun onTabReselected(tab: TabLayout.Tab) {}
            })


            btnAddGroup.setOnClickListener { viewModel.onAddGroupClick() }
        }

        viewModel.openFilePicker.observe(viewLifecycleOwner) {
            filePicker.selectFiles()
        }

        viewModel.showErrorDialog.observe(viewLifecycleOwner) { s: String ->
            MaterialAlertDialogBuilder(requireContext(), R.style.MaterialAlertDialog_Rounded)
                .setTitle("Произошла ошибка")
                .setMessage(s)
                .setPositiveButton("ОК") { _: DialogInterface, _: Int -> }.show()
        }


        viewModel.updateEventsOfGroup.observe(
            viewLifecycleOwner
        ) { integerListPair: Pair<Int, List<List<DomainModel>>> ->
            groupTimetablesAdapter.updateTimetable(
                integerListPair.first,
                integerListPair.second
            )
        }


        lifecycleScope.launchWhenStarted {
            viewModel.preferences.collect {
                Log.d("lol", "onViewCreated submit: $it")
                preferenceAdapter.submit(it)
            }
        }


        viewModel.openEventEditor.observe(viewLifecycleOwner) {
            startActivity(Intent(requireActivity(), EventEditorActivity::class.java))
        }
        viewModel.openChoiceOfGroup.observe(
            viewLifecycleOwner
        ) { navController.navigate(R.id.action_global_choiceOfGroupFragment) }

        viewModel.addGroup.observe(viewLifecycleOwner) {
            groupTimetablesAdapter.notifyItemInserted(
                groupTimetablesAdapter.itemCount
            )
        }

    }

    internal class GroupTimetablesAdapter(
        private val fragmentManager: FragmentManager,
        lifecycle: Lifecycle,
        private var onEditEventItemClickListener: OnEditEventItemClickListener =
            OnEditEventItemClickListener { _, _ -> },
        private var onItemMoveListener: OnItemMoveListener,
        private var onCreateLessonClickListener: OnCreateLessonClickListener =
            OnCreateLessonClickListener { },
        private var onLessonItemClickListener: OnLessonItemClickListener =
            object : OnLessonItemClickListener() {}
    ) : FragmentStateAdapter(
        fragmentManager, lifecycle
    ) {

        var editMode: Boolean = false
            set(value) {
                field = value
                notifyItemRangeChanged(
                    0, itemCount,
                    if (value) EventAdapter.PAYLOAD.ENABLE_EDIT_MODE else EventAdapter.PAYLOAD.DISABLE_EDIT_MODE
                )
            }


        var list: MutableList<List<List<DomainModel>>> = ArrayList()

        override fun onBindViewHolder(
            holder: FragmentViewHolder,
            position: Int,
            payloads: List<Any>
        ) {
            if (payloads.isEmpty()) {
                onBindViewHolder(holder, position)
            } else {
                val tag = "f" + holder.itemId
                val fragment = fragmentManager.findFragmentByTag(tag) as EventsFragment
                for (payload in payloads) {
                    when {
                        payload === EventAdapter.PAYLOAD.ENABLE_EDIT_MODE -> {
                            fragment.setEditMode(true)
                        }
                        payload === EventAdapter.PAYLOAD.DISABLE_EDIT_MODE -> {
                            fragment.setEditMode(false)
                        }
                        payload === PAYLOAD.UPDATE_EVENTS -> {
                            fragment.submitList(list[position])
                        }
                    }
                }
            }
        }


        override fun createFragment(position: Int): Fragment {
            val fragment = EventsFragment()

            fragment.setOnEditEventItemClickListener(onEditEventItemClickListener)
            fragment.setOnCreateLessonClickListener(onCreateLessonClickListener)
            fragment.setOnLessonItemClickListener(onLessonItemClickListener)

            fragment.submitList(this.list[position])
            fragment.setOnItemMoveListener(onItemMoveListener)
            return fragment
        }

        override fun getItemCount(): Int = list.size

        fun updateTimetable(position: Int, list: List<List<DomainModel>>) {
            this.list[position] = list
            notifyItemChanged(position, PAYLOAD.UPDATE_EVENTS)
        }

        fun updateTimetable(list: List<List<List<DomainModel>>>) {
            this.list.clear()
            this.list.addAll(list)
            notifyItemRangeChanged(0, list.size, PAYLOAD.UPDATE_EVENTS)
        }

        internal enum class PAYLOAD { UPDATE_EVENTS }
    }

    companion object {
        const val PICK_FILE_RESULT_CODE = 1
    }

    override fun onLessonEditClick(position: Int, dayOfWeek: Int) {
        viewModel.onLessonItemEditClick(position - 1, dayOfWeek)
    }

    override fun onLessonCreateClick(dayOfWeek: Int) {
        viewModel.onCreateLessonItemClick(dayOfWeek)
    }

    override fun onMove(oldPosition: Int, targetPosition: Int, dayOfWeek: Int) {
        viewModel.onLessonItemMove(
            oldPosition - 1, targetPosition - 1, dayOfWeek
        )
    }

}