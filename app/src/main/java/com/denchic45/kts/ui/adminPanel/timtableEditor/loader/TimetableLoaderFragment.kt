package com.denchic45.kts.ui.adminPanel.timtableEditor.loader

import android.Manifest
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Parcel
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.adapter.FragmentViewHolder
import by.kirich1409.viewbindingdelegate.viewBinding
import com.denchic45.kts.R
import com.denchic45.kts.data.model.DomainModel
import com.denchic45.kts.data.model.domain.ListItem
import com.denchic45.kts.databinding.FragmentTimetableLoaderBinding
import com.denchic45.kts.ui.BaseFragment
import com.denchic45.kts.ui.adapter.*
import com.denchic45.kts.ui.adapter.EventAdapter.*
import com.denchic45.kts.ui.adminPanel.timtableEditor.eventEditor.EventEditorActivity
import com.denchic45.kts.ui.adminPanel.timtableEditor.loader.lessonsOfDay.LessonsOfDayFragment
import com.denchic45.kts.utils.FilePicker
import com.denchic45.kts.utils.path
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
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
    private lateinit var groupLessonsAdapter: GroupLessonsAdapter2
    private lateinit var itemAdapter: ItemAdapter
    private val viewBinding: FragmentTimetableLoaderBinding by viewBinding(
        FragmentTimetableLoaderBinding::bind
    )

    private lateinit var filePicker: FilePicker

    private var navController: NavController? = null

    override fun onResume() {
        super.onResume()
        setHasOptionsMenu(true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        filePicker = FilePicker(requireActivity() as AppCompatActivity, this, { result ->
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
        groupLessonsAdapter = GroupLessonsAdapter2(childFragmentManager, lifecycle)
        with(viewBinding) {
            groupLessonsAdapter = GroupLessonsAdapter2(childFragmentManager, lifecycle)
            groupLessonsAdapter.setOnEditEventItemClickListener(this@TimetableLoaderFragment)
            groupLessonsAdapter.setOnCreateLessonClickListener(this@TimetableLoaderFragment)
            groupLessonsAdapter.setOnItemMoveListener(this@TimetableLoaderFragment)
            vpTimetablePreview.adapter = groupLessonsAdapter
            TabLayoutMediator(
                tlTimetableGroup,
                vpTimetablePreview
            ) { tab: TabLayout.Tab, position: Int ->
                tab.text = viewModel.showTimetable.value!!.first[position]
            }.attach()
            tlTimetableGroup.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    viewModel.onGroupSelect(tab.position)
                }

                override fun onTabUnselected(tab: TabLayout.Tab) {}
                override fun onTabReselected(tab: TabLayout.Tab) {}
            })
            itemAdapter = ItemAdapter(R.layout.item_icon_content_2)
            rvPreference.adapter = itemAdapter
            itemAdapter.itemClickListener =
                OnItemClickListener { position: Int -> viewModel.onPreferenceItemClick(position) }
            itemAdapter.itemCheckListener =
                OnItemCheckListener { position: Int, isChecked: Boolean ->
                    viewModel.onPreferenceItemCheck(
                        position,
                        isChecked
                    )
                }
            rvPreference.layoutManager = LinearLayoutManager(activity)
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
                picker.addOnPositiveButtonClickListener { selection: Long? ->
                    viewModel.onFirstDateOfNewTimetableSelect(
                        selection
                    )
                }
            }

            viewModel.showTimetable.observe(
                viewLifecycleOwner,
                { lessonsWithGroupNamePair ->
                    for (i in lessonsWithGroupNamePair.first.indices) {
                        tlTimetableGroup.addTab(tlTimetableGroup.newTab())
                    }
                    groupLessonsAdapter.list = lessonsWithGroupNamePair.second
                    groupLessonsAdapter.notifyItemRangeInserted(
                        0,
                        lessonsWithGroupNamePair.second.size
                    )
                })
            viewModel.showPublishingTimetable.observe(viewLifecycleOwner, {
                itemAdapter.notifyItemChanged(0, ItemAdapter.PAYLOAD.SHOW_LOADING)
                itemAdapter.notifyItemChanged(0, ItemAdapter.PAYLOAD.CHANGE_TITLE)
            })
            viewModel.enableEditMode.observe(viewLifecycleOwner, { enable: Boolean ->
                vpTimetablePreview.post {
                    groupLessonsAdapter.notifyItemRangeChanged(
                        0, groupLessonsAdapter.itemCount,
                        if (enable) EventAdapter.PAYLOAD.ENABLE_EDIT_MODE else EventAdapter.PAYLOAD.DISABLE_EDIT_MODE
                    )
                }
            })

            viewModel.showPage.observe(
                viewLifecycleOwner,
                { position: Int? -> vsTimetableLoader.displayedChild = position!! })


            vpTimetablePreview.adapter = groupLessonsAdapter
            TabLayoutMediator(
                tlTimetableGroup,
                vpTimetablePreview
            ) { tab: TabLayout.Tab, position: Int ->
                tab.text = viewModel.showTimetable.value!!.first[position]
            }.attach()
            tlTimetableGroup.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    viewModel.onGroupSelect(tab.position)
                }

                override fun onTabUnselected(tab: TabLayout.Tab) {}
                override fun onTabReselected(tab: TabLayout.Tab) {}
            })
            itemAdapter = ItemAdapter(R.layout.item_icon_content_2)
            rvPreference.adapter = itemAdapter
            itemAdapter.itemClickListener =
                OnItemClickListener { position: Int -> viewModel.onPreferenceItemClick(position) }
            itemAdapter.itemCheckListener =
                OnItemCheckListener { position: Int, isChecked: Boolean ->
                    viewModel.onPreferenceItemCheck(
                        position,
                        isChecked
                    )
                }
            rvPreference.layoutManager = LinearLayoutManager(activity)
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
                picker.addOnPositiveButtonClickListener { selection: Long? ->
                    viewModel.onFirstDateOfNewTimetableSelect(
                        selection
                    )
                }
            }
            btnAddGroup.setOnClickListener { v: View? -> viewModel.onAddGroupClick() }
        }
        val navHostFragment = requireActivity().supportFragmentManager.primaryNavigationFragment
        navController = findNavController(navHostFragment!!.requireView())
        viewModel.openFilePicker.observe(viewLifecycleOwner, {

            filePicker.selectFiles()
        })
        viewModel.allowEditTimetable.observe(
            viewLifecycleOwner,
            { itemAdapter.notifyItemChanged(0, ItemAdapter.PAYLOAD.SHOW_IMAGE) })

        viewModel.showDone.observe(viewLifecycleOwner, {
            itemAdapter.notifyItemRangeRemoved(0, 2)
            itemAdapter.notifyItemChanged(1)
        })
        viewModel.showMessageRes.observe(viewLifecycleOwner, { resId: Int? ->
            Toast.makeText(
                context, getString(
                    resId!!
                ), Toast.LENGTH_SHORT
            ).show()
        })
        viewModel.showErrorDialog.observe(viewLifecycleOwner, { s: String ->
            MaterialAlertDialogBuilder(requireContext(), R.style.MaterialAlertDialog_Rounded)
                .setTitle("Произошла ошибка")
                .setMessage(s)
                .setPositiveButton("ОК") { dialog: DialogInterface, which: Int -> }.show()
        })

        viewModel.showPreferenceList.observe(
            viewLifecycleOwner,
            { listItems: List<ListItem?> -> itemAdapter.submitList(listItems) })
        viewModel.updateLessonsOfGroup.observe(
            viewLifecycleOwner,
            { integerListPair: Pair<Int, MutableList<DomainModel>> ->
                groupLessonsAdapter.updateList(
                    integerListPair.first,
                    integerListPair.second
                )
            })
        viewModel.openLessonEditor.observe(viewLifecycleOwner, {
            startActivity(Intent(activity, EventEditorActivity::class.java))
        })
        viewModel.openChoiceOfGroup.observe(
            viewLifecycleOwner,
            { navController!!.navigate(R.id.action_global_choiceOfGroupFragment) })
        viewModel.finish.observe(
            viewLifecycleOwner,
            { navController!!.popBackStack() })
        viewModel.showAddedGroup.observe(viewLifecycleOwner, {
            groupLessonsAdapter.notifyItemInserted(
                groupLessonsAdapter.itemCount
            )
        })

    }

    internal class GroupLessonsAdapter2(
        private val fragmentManager: FragmentManager,
        lifecycle: Lifecycle
    ) : FragmentStateAdapter(
        fragmentManager, lifecycle
    ) {
        var list: MutableList<MutableList<DomainModel>> = ArrayList()
        private var onItemMoveListener: OnItemMoveListener? = null
        private var onCreateLessonClickListener: OnCreateLessonClickListener =
            OnCreateLessonClickListener { }
        private var onLessonItemClickListener: OnLessonItemClickListener =
            object : OnLessonItemClickListener() {}
        private var onEditEventItemClickListener: OnEditEventItemClickListener =
            OnEditEventItemClickListener { }

        fun setOnEditEventItemClickListener(onEditEventItemClickListener: OnEditEventItemClickListener) {
            this.onEditEventItemClickListener = onEditEventItemClickListener
        }

        fun setOnCreateLessonClickListener(onCreateLessonClickListener: OnCreateLessonClickListener) {
            this.onCreateLessonClickListener = onCreateLessonClickListener
        }

        fun setOnLessonItemClickListener(onLessonItemClickListener: OnLessonItemClickListener) {
            this.onLessonItemClickListener = onLessonItemClickListener
        }

        fun setOnItemMoveListener(onItemMoveListener: OnItemMoveListener?) {
            this.onItemMoveListener = onItemMoveListener
        }

        override fun onBindViewHolder(
            holder: FragmentViewHolder,
            position: Int,
            payloads: List<Any>
        ) {
            if (payloads.isEmpty()) {
                onBindViewHolder(holder, position)
            } else {
                val tag = "f" + holder.itemId
                val fragment = fragmentManager.findFragmentByTag(tag) as LessonsOfDayFragment
                for (payload in payloads) {
                    when {
                        payload === EventAdapter.PAYLOAD.ENABLE_EDIT_MODE -> {
                            fragment.enableEditMode()
                        }
                        payload === EventAdapter.PAYLOAD.DISABLE_EDIT_MODE -> {
                            fragment.disableEditMode()
                        }
                        payload === PAYLOAD.UPDATE_LESSONS -> {
                            fragment.updateList(list[position])
                        }
                    }
                }
            }
        }

        override fun createFragment(position: Int): Fragment {
            val fragment = LessonsOfDayFragment()

            fragment.setOnEditEventItemClickListener(onEditEventItemClickListener)
            fragment.setOnCreateLessonClickListener(onCreateLessonClickListener)
            fragment.setOnLessonItemClickListener(onLessonItemClickListener)

            fragment.setList(this.list[position])
            fragment.setOnItemMoveListener(onItemMoveListener)
            return fragment
        }

        override fun getItemCount(): Int = list.size


        fun updateList(position: Int, list: MutableList<DomainModel>) {
            this.list[position] = list
            notifyItemChanged(position, PAYLOAD.UPDATE_LESSONS)
        }

        internal enum class PAYLOAD { UPDATE_LESSONS }
    }

    companion object {
        const val PICK_FILE_RESULT_CODE = 1
    }

    override fun onLessonEditClick(position: Int) {
        viewModel.onLessonItemEditClick(
            position,
            binding.tlTimetableGroup.selectedTabPosition
        )
    }

    override fun onLessonCreateClick(position: Int) {
        viewModel.onCreateLessonItemClick(
            position
        )
    }

    override fun onMove(oldPosition: Int, targetPosition: Int) {
        viewModel.onLessonItemMove(
            binding.vpTimetablePreview.currentItem, oldPosition, targetPosition
        )
    }

}