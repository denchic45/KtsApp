package com.denchic45.kts.ui.adminPanel.timetableEditor

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.viewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.denchic45.kts.R
import com.denchic45.kts.databinding.FragmentTimetableEditorBinding
import com.denchic45.kts.ui.BaseFragment
import com.denchic45.kts.ui.adminPanel.timetableEditor.finder.TimetableFinderFragment
import com.denchic45.kts.ui.adminPanel.timetableEditor.loader.TimetableLoaderFragment

class TimetableEditorFragment :
    BaseFragment<TimetableEditorViewModel, FragmentTimetableEditorBinding>(R.layout.fragment_timetable_editor) {

    override val binding: FragmentTimetableEditorBinding by viewBinding(
        FragmentTimetableEditorBinding::bind
    )
    override val viewModel: TimetableEditorViewModel by viewModels { viewModelFactory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = TimetableEditorPageAdapter(childFragmentManager)
        with(binding) {
            vp.adapter = adapter
            tl.setupWithViewPager(vp)
            tl.getTabAt(0)!!.setText("Добавить").setIcon(R.drawable.ic_add)
            tl.getTabAt(1)!!.setText("Посмотреть").setIcon(R.drawable.ic_timetable)
        }

    }

    private class TimetableEditorPageAdapter(fm: FragmentManager) :
        FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> TimetableLoaderFragment()
                1 -> TimetableFinderFragment()
                else -> throw RuntimeException("position not is: $position")
            }

        }

        override fun getCount(): Int = 2
    }
}