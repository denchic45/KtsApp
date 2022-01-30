package com.denchic45.kts.ui.adminPanel.timetableEditor.choiceOfSubject

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.denchic45.kts.R
import com.denchic45.kts.data.Resource
import com.denchic45.kts.data.model.DomainModel
import com.denchic45.kts.databinding.FragmentChoiceOfSubjectBinding
import com.denchic45.kts.ui.BaseFragment
import com.denchic45.kts.ui.adapter.SubjectAdapter
import com.denchic45.kts.utils.NetworkException
import com.denchic45.widget.ListStateLayout
import com.example.appbarcontroller.appbarcontroller.AppBarController
import com.example.searchbar.SearchBar
import java.util.*

class ChoiceOfSubjectFragment :
    BaseFragment<ChoiceOfSubjectViewModel, FragmentChoiceOfSubjectBinding>() {
    private var appBarController: AppBarController? = null
    private var searchBar: SearchBar? = null
    private var navController: NavController? = null
    private lateinit var listStateLayout: ListStateLayout
    override val binding: FragmentChoiceOfSubjectBinding by viewBinding(
        FragmentChoiceOfSubjectBinding::bind
    )
    override val viewModel: ChoiceOfSubjectViewModel by viewModels { viewModelFactory }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_choice_of_subject, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController(view)
        appBarController = AppBarController.findController(requireActivity())
        appBarController!!.toolbar.visibility = View.GONE
        searchBar = SearchBar(requireActivity())
        appBarController!!.addView(searchBar)
        val rv: RecyclerView = view.findViewById(R.id.rv)
        listStateLayout = view.findViewById(R.id.listStateLayout)
        val adapter = SubjectAdapter { position: Int -> viewModel.onSubjectClick(position) }
        rv.adapter = adapter
        searchBar!!.setOnQueryTextListener(object : SearchBar.OnQueryTextListener() {
            override fun onQueryTextChange(name: String) {
                viewModel.onSubjectNameType(name)
            }
        })
        viewModel.showFoundSubjects.observe(
            viewLifecycleOwner
        ) { resource ->
            if (resource is Resource.Success) {
                listStateLayout.showList()
                adapter.submitList(ArrayList<DomainModel?>(resource.data))
            } else if (resource is Resource.Error) {
                if (resource.error is NetworkException) {
                    listStateLayout.showView(ListStateLayout.NETWORK_VIEW)
                }
            }
        }
        viewModel.finish.observe(viewLifecycleOwner) { navController!!.popBackStack() }
    }

    override fun onDestroy() {
        super.onDestroy()
        appBarController!!.removeView(searchBar!!)
        appBarController!!.toolbar.visibility = View.VISIBLE
    }
}