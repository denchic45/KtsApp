package com.denchic45.kts.ui.login.loginByEmail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.denchic45.kts.R;
import com.denchic45.kts.ui.login.LoginViewModel;
import com.google.android.material.textfield.TextInputLayout;

import org.jetbrains.annotations.NotNull;

public class LoginByEmailFragment extends Fragment {

    private EditText etMail, etPassword;
    private TextInputLayout tilMail, tilPassword;
    private Button btnNext;
    private TextView tvForgotPassword;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_login_by_email, container, false);
        etMail = root.findViewById(R.id.et_email);
        etPassword = root.findViewById(R.id.et_password);
        tilMail = root.findViewById(R.id.til_email);
        tilPassword = root.findViewById(R.id.til_password);
        btnNext = root.findViewById(R.id.btn_next);
        tvForgotPassword = root.findViewById(R.id.tv_forgot_password);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        LoginViewModel loginViewModel = new ViewModelProvider(getActivity()).get(LoginViewModel.class);
        btnNext.setOnClickListener(v -> loginViewModel.onNextMailClick(etMail.getText().toString(), etPassword.getText().toString()));

        loginViewModel.showMailError.observe(getViewLifecycleOwner(), tilMail::setError);

        loginViewModel.showPasswordError.observe(getViewLifecycleOwner(), tilPassword::setError);

        loginViewModel.openResetPassword.observe(getViewLifecycleOwner(), unused ->
                Navigation.findNavController(view).navigate(R.id.action_loginByEmailFragment_to_resetPasswordFragment));

        tvForgotPassword.setOnClickListener(v -> loginViewModel.onForgotPasswordClick());
    }
}