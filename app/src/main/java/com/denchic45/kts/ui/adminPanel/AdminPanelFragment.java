package com.denchic45.kts.ui.adminPanel;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.denchic45.kts.R;
import com.denchic45.kts.ui.adapter.ItemAdapter;
import com.denchic45.kts.ui.creater.CreatorDialog;
import com.example.appbarcontroller.appbarcontroller.AppBarController;

import org.jetbrains.annotations.NotNull;

import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class AdminPanelFragment extends Fragment {

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private NavController navController;
    private RecyclerView rv;

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_panel, container, false);

        rv = view.findViewById(R.id.recyclerview_preference);
        rv.setLayoutManager(new LinearLayoutManager(getActivity()));
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        AdminPanelViewModel viewModel = new ViewModelProvider(requireActivity()).get(AdminPanelViewModel.class);
        navController = Navigation.findNavController(view);
        ItemAdapter adapter = new ItemAdapter();
        adapter.submitList(viewModel.getItemList());
        adapter.setItemClickListener(viewModel::onItemClick);
        rv.setAdapter(adapter);

        viewModel.openTimetableEditor.observe(getViewLifecycleOwner(), (Observer<Object>) o ->
                navController.navigate(R.id.action_menu_admin_panel_to_timetableEditorFragment));

        viewModel.openUserFinder.observe(getViewLifecycleOwner(), (Observer<Object>) o ->
                navController.navigate(R.id.action_menu_admin_panel_to_finderFragment2));

        viewModel.openCreator.observe(getViewLifecycleOwner(), (Observer<Object>) o -> {
            CreatorDialog creatorDialog = new CreatorDialog();
            creatorDialog.show(getChildFragmentManager(), null);
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        AppBarController appBarController = AppBarController.findController(requireActivity());
        appBarController.setExpandableIfViewCanScroll(rv, getViewLifecycleOwner());
        appBarController.setLiftOnScroll(true);
        ((AppCompatActivity) requireActivity()).getSupportActionBar().setTitle(null);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        rv.setAdapter(null);
        rv = null;
        compositeDisposable.clear();
    }
}