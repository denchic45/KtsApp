package com.denchic45.kts.ui.base.chooser

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.denchic45.kts.R
import com.denchic45.kts.data.model.DomainModel
import com.denchic45.kts.databinding.FragmentChooserBinding
import com.denchic45.kts.ui.BaseFragment
import com.denchic45.widget.extendedAdapter.DelegationAdapterExtended
import com.example.appbarcontroller.appbarcontroller.AppBarController
import com.example.searchbar.SearchBar
import kotlinx.coroutines.flow.collect

abstract class ChooserFragment<T : DomainModel> :
    BaseFragment<ChooserViewModel<T>, FragmentChooserBinding>() {

    override val viewModel: ChooserViewModel<T> by viewModels { viewModelFactory }
    override val binding: FragmentChooserBinding by viewBinding(FragmentChooserBinding::bind)

    abstract val adapter: DelegationAdapterExtended

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
            lifecycleScope.launchWhenStarted {
                viewModel.items.collect {
                    adapter.submit(it)
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