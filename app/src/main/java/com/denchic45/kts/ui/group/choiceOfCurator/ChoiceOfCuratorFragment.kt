package com.denchic45.kts.ui.group.choiceOfCurator

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.denchic45.kts.R
import com.denchic45.kts.data.model.DomainModel
import com.denchic45.kts.data.model.domain.User
import com.denchic45.kts.di.viewmodel.ViewModelFactory
import com.denchic45.kts.ui.adapter.UserAdapter
import com.denchic45.widget.ListStateLayout
import com.example.appbarcontroller.appbarcontroller.AppBarController
import com.example.searchbar.SearchBar
import dagger.android.support.AndroidSupportInjection
import java.util.*
import javax.inject.Inject

class ChoiceOfCuratorFragment : Fragment() {
    @Inject
    lateinit var viewModelFactory: ViewModelFactory<ChoiceOfCuratorViewModel>
    private val viewModel: ChoiceOfCuratorViewModel by viewModels { viewModelFactory }
    private var adapter: UserAdapter? = null
    private var navController: NavController? = null
    private var appBarController: AppBarController? = null
    private var searchBar: SearchBar? = null
    private var listStateLayout: ListStateLayout? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_choice_of_curator, container, false)
        adapter = UserAdapter({ position -> viewModel.onTeacherClick(position) })
        val rv: RecyclerView = root.findViewById(R.id.recyclerview_teachers)
        rv.layoutManager = LinearLayoutManager(activity)
        rv.adapter = adapter
        listStateLayout = root.findViewById(R.id.listStateLayout)
        appBarController = AppBarController.findController(requireActivity())
        appBarController!!.toolbar.visibility = View.GONE
        searchBar = SearchBar(requireActivity())
        appBarController!!.addView(searchBar)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController(view)
        viewModel.showFoundTeachers.observe(viewLifecycleOwner) { users: List<User> ->
            adapter!!.submitList(ArrayList<DomainModel>(users))
        }

        viewModel.showErrorNetworkState.observe(
            viewLifecycleOwner
        ) { show: Boolean -> if (show) listStateLayout!!.showView(ListStateLayout.NETWORK_VIEW) else listStateLayout!!.showList() }
        searchBar!!.setOnQueryTextListener(object : SearchBar.OnQueryTextListener() {
            override fun onQueryTextChange(name: String) {
                viewModel.onTeacherNameType(name)
            }
        })
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        AndroidSupportInjection.inject(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        appBarController!!.removeView(searchBar!!)
        appBarController!!.toolbar.visibility = View.VISIBLE
    }
}