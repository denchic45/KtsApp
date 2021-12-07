package com.denchic45.kts.ui.base.choiceFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.Navigation.findNavController
import com.denchic45.kts.R
import com.denchic45.kts.data.model.DomainModel
import com.denchic45.kts.ui.adapter.BaseViewHolder
import com.denchic45.kts.ui.adapter.CustomAdapter
import com.denchic45.kts.ui.adapter.OnItemClickListener
import com.denchic45.kts.ui.base.listFragment.ListFragment
import com.example.appbarcontroller.appbarcontroller.AppBarController
import com.example.searchbar.SearchBar

open class ChoiceFragment<T : DomainModel, VH : BaseViewHolder<T, *>> :
    ListFragment<CustomAdapter<T, VH>>() {
    private var appBarController: AppBarController? = null
    private var navController: NavController? = null
    private var searchBar: SearchBar? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.fragment_choice_of_curator, container, false)
        appBarController = AppBarController.findController(requireActivity())
        appBarController!!.toolbar.visibility = View.GONE
        searchBar = SearchBar(requireActivity())
        appBarController!!.addView(searchBar)
        return root
    }

    fun showItems(list: List<T>) {
        adapter!!.submitList(list)
    }

    fun setOnQueryTextListener(onQueryTextListener: SearchBar.OnQueryTextListener) {
        searchBar!!.setOnQueryTextListener(onQueryTextListener)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController(view)
    }
}