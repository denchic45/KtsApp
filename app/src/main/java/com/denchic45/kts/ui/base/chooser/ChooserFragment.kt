package com.denchic45.kts.ui.base.chooser

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.denchic45.kts.R
import com.denchic45.kts.data.Resource
import com.denchic45.kts.domain.DomainModel
import com.denchic45.kts.databinding.FragmentChooserBinding
import com.denchic45.kts.ui.base.BaseFragment
import com.denchic45.kts.util.NetworkException
import com.denchic45.kts.util.collectWhenStarted
import com.denchic45.sample.SearchBar
import com.denchic45.widget.ListStateLayout
import com.denchic45.widget.extendedAdapter.AdapterDelegate
import com.denchic45.widget.extendedAdapter.DelegationAdapterExtended
import com.denchic45.widget.extendedAdapter.adapter
import com.example.appbarcontroller.appbarcontroller.AppBarController

abstract class ChooserFragment<VM : ChooserViewModel<out DomainModel>> :
    BaseFragment<VM, FragmentChooserBinding>(R.layout.fragment_chooser) {

    override val binding: FragmentChooserBinding by viewBinding(FragmentChooserBinding::bind)
    abstract fun adapterDelegates(): List<AdapterDelegate>

    private val adapter: DelegationAdapterExtended = adapter {
        delegates(adapterDelegates())
        onClick { viewModel.onItemClick(it) }
    }

    private lateinit var mainToolbar: Toolbar
    private lateinit var searchBar: SearchBar

    private val appBarController: AppBarController by lazy {
        AppBarController.findController(requireActivity())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            appBarController.apply {
                mainToolbar = toolbar
                removeView(toolbar)
                searchBar = addView(R.layout.searchbar_chooser) as SearchBar
            }

            rvItems.adapter = adapter

            searchBar.setOnQueryTextListener(object : SearchBar.OnQueryTextListener() {
                override fun onQueryTextChange(newText: String) {
                    super.onQueryTextChange(newText)
                    viewModel.onNameType(newText)
                }

                override fun onQueryTextSubmit(query: String) {
                    super.onQueryTextSubmit(query)
                    viewModel.onNameType(query)
                }
            })

            viewModel.items.collectWhenStarted(lifecycleScope) { resource ->
                when (resource) {
                    is Resource.Loading -> {}
                    is Resource.Success -> {
                        lsl.showList()
                        adapter.submit(resource.data)
                    }
                    is Resource.Error -> {
                        when (resource.error) {
                            is NetworkException -> {
                                lsl.showView(ListStateLayout.NETWORK_VIEW)
                            }
                        }
                    }
                    is Resource.Next -> {}
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        appBarController.removeView(searchBar)
        appBarController.addView(mainToolbar)
        appBarController.setExpanded(true, false)
    }
}