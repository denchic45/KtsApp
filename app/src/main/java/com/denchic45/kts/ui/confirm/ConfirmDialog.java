package com.denchic45.kts.ui.confirm;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.denchic45.kts.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.jetbrains.annotations.NotNull;

public class ConfirmDialog extends DialogFragment {

    private static final String TITLE = "TITLE";
    private static final String MESSAGE = "SUBTITLE";
    private ConfirmViewModel viewModel;

    @NotNull
    public static ConfirmDialog newInstance(String title, String subtitle) {
        ConfirmDialog fragment = new ConfirmDialog();
        Bundle args = new Bundle();
        args.putString(TITLE, title);
        args.putString(MESSAGE, subtitle);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(requireActivity(), R.style.MaterialAlertDialog_Rounded)
                .setTitle(getArguments().getString(TITLE))
                .setMessage(getArguments().getString(MESSAGE))
                .setPositiveButton("Да", (dialogInterface, i) -> viewModel.onPositiveClick())
                .setNegativeButton("Нет", (dialog1, which) -> viewModel.onNegativeClick());
        return dialog.create();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(ConfirmViewModel.class);
//        getDialog().setCanceledOnTouchOutside(false);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
    }
}
