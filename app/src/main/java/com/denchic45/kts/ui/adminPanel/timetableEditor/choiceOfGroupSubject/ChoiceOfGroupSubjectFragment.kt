package com.denchic45.kts.ui.adminPanel.timetableEditor.choiceOfGroupSubject

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.denchic45.kts.R
import com.denchic45.kts.data.Resource
import com.denchic45.kts.data.model.domain.Subject
import com.denchic45.kts.di.viewmodel.ViewModelFactory
import com.denchic45.kts.ui.adapter.SubjectAdapter
import com.denchic45.kts.ui.iconPicker.IconPickerDialog
import com.denchic45.kts.utils.NetworkException
import com.denchic45.kts.utils.setActivityTitle
import com.denchic45.widget.ListStateLayout
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class ChoiceOfGroupSubjectFragment : Fragment() {
    @Inject
    lateinit var viewModelFactory: ViewModelFactory<ChoiceOfGroupSubjectViewModel>
    private val viewModel: ChoiceOfGroupSubjectViewModel by viewModels { viewModelFactory }
    private var rv: RecyclerView? = null
    private var listStateLayout: ListStateLayout? = null
    private var adapter: SubjectAdapter? = null
    private var navController: NavController? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_choice_of_group_subject, container, false)
        rv = root.findViewById(R.id.rv_subjects)
        listStateLayout = root.findViewById(R.id.listStateLayout)
        return root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
        inflater.inflate(R.menu.options_choice_of_group, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        viewModel.onOptionsItemSelected(item.itemId)
        return super.onOptionsItemSelected(item)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController(view)
        adapter = SubjectAdapter { position: Int -> viewModel.onSubjectClick(position) }
        rv!!.adapter = adapter
        rv!!.layoutManager = LinearLayoutManager(context)
        rv!!.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        viewModel.title.observe(viewLifecycleOwner, this::setActivityTitle)
        viewModel.openIconPicker.observe(viewLifecycleOwner) {
            IconPickerDialog().show(
                requireActivity().supportFragmentManager, null
            )
        }
        viewModel.openChoiceOfSubject.observe(
            viewLifecycleOwner
        ) { navController!!.navigate(R.id.action_choiceOfGroupSubjectFragment_to_choiceOfSubjectFragment) }
        viewModel.updateIconEventSubject.observe(
            viewLifecycleOwner
        ) { adapter!!.notifyItemChanged(0, SubjectAdapter.PAYLOAD.UPDATE_ICON) }
        viewModel.finish.observe(
            viewLifecycleOwner
        ) { navController!!.popBackStack() }
        viewModel.showSubjectsOfGroup.observe(
            viewLifecycleOwner
        ) { resource: Resource<List<Subject>> ->
            if (resource is Resource.Success) {
                listStateLayout!!.showList()
                adapter!!.submitList(resource.data)
            } else if (resource is Resource.Error) {
                if (resource.error is NetworkException) {
                    listStateLayout!!.showView(ListStateLayout.NETWORK_VIEW)
                }
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        AndroidSupportInjection.inject(this)
    }
}