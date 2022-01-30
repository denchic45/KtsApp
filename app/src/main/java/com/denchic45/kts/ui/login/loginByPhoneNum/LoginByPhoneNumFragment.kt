package com.denchic45.kts.ui.login.loginByPhoneNum;

import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.denchic45.kts.R;
import com.denchic45.kts.ui.login.LoginViewModel;
import com.denchic45.kts.ui.login.loginByEmail.LoginByEmailFragment;
import com.google.android.material.textfield.TextInputLayout;

import org.jetbrains.annotations.NotNull;

public class LoginByPhoneNumFragment extends Fragment {

    private EditText etNum;
    private TextInputLayout tnlNum;
    private Button btnGetCode;




    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_login_by_phone_num, container, false);
        etNum = root.findViewById(R.id.editText_num);
        tnlNum = root.findViewById(R.id.textinput_num);
        btnGetCode = root.findViewById(R.id.btn_next);

        etNum.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        LoginViewModel loginViewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);
        loginViewModel.errorNum.observe(getViewLifecycleOwner(), s -> tnlNum.setError(s));

        btnGetCode.setOnClickListener(view1 -> {
            String phoneNum = etNum.getText().toString();
            loginViewModel.onGetCodeClick(phoneNum);
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(etNum.getApplicationWindowToken(), 0);
        });
    }
}