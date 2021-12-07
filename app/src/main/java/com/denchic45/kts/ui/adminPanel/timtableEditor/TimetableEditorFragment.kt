package com.denchic45.kts.ui.adminPanel.timtableEditor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.viewModels
import androidx.viewpager.widget.ViewPager
import by.kirich1409.viewbindingdelegate.viewBinding
import com.denchic45.kts.R
import com.denchic45.kts.databinding.FragmentTimetableEditorBinding
import com.denchic45.kts.ui.BaseFragment
import com.denchic45.kts.ui.adminPanel.timtableEditor.finder.TimetableFinderFragment
import com.denchic45.kts.ui.adminPanel.timtableEditor.loader.TimetableLoaderFragment
import com.google.android.material.tabs.TabLayout

class TimetableEditorFragment() :
    BaseFragment<TimetableEditorViewModel, FragmentTimetableEditorBinding>() {

    override val binding: FragmentTimetableEditorBinding by viewBinding(FragmentTimetableEditorBinding::bind)
    override val viewModel: TimetableEditorViewModel by viewModels { viewModelFactory }
    private var vp: ViewPager? = null
    private var tl: TabLayout? = null
    private var adapter: TimetableEditorPageAdapter? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = LayoutInflater.from(activity)
            .inflate(R.layout.fragment_timetable_editor, container, false)
        vp = root.findViewById(R.id.vp_timetable_editor)
        tl = root.findViewById(R.id.tl_timetable_editor)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = TimetableEditorPageAdapter(childFragmentManager)
        vp!!.adapter = adapter
        tl!!.setupWithViewPager(vp)
        tl!!.getTabAt(0)!!.setText("Добавить").setIcon(R.drawable.ic_add)
        tl!!.getTabAt(1)!!.setText("Посмотреть").setIcon(R.drawable.ic_timetable)
    }

    private class TimetableEditorPageAdapter(fm: FragmentManager) :
        FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        override fun getItem(position: Int): Fragment {
            when (position) {
                0 -> return TimetableLoaderFragment()
                1 -> return TimetableFinderFragment()
            }
            throw RuntimeException("position not is: $position")
        }

        override fun getCount(): Int {
            return 2
        }
    }
}