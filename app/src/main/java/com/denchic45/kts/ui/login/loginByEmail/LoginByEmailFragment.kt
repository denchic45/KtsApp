package com.denchic45.kts.ui.login.loginByEmail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation.findNavController
import com.denchic45.kts.R
import com.denchic45.kts.ui.login.LoginViewModel
import com.google.android.material.textfield.TextInputLayout

class LoginByEmailFragment : Fragment() {
    private lateinit var etMail: EditText
    private lateinit var etPassword: EditText
    private lateinit var tilMail: TextInputLayout
    private lateinit var tilPassword: TextInputLayout
    private lateinit var btnNext: Button
    private lateinit var tvForgotPassword: TextView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_login_by_email, container, false)
        etMail = root.findViewById(R.id.et_email)
        etPassword = root.findViewById(R.id.et_password)
        tilMail = root.findViewById(R.id.til_email)
        tilPassword = root.findViewById(R.id.til_password)
        btnNext = root.findViewById(R.id.btn_next)
        tvForgotPassword = root.findViewById(R.id.tv_forgot_password)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val loginViewModel = ViewModelProvider(requireActivity())[LoginViewModel::class.java]
        btnNext.setOnClickListener {
            loginViewModel.onNextMailClick(
                etMail.text.toString(), etPassword.text.toString()
            )
        }
        loginViewModel.showMailError.observe(
            viewLifecycleOwner
        ) { errorText: String? -> tilMail.error = errorText }
        loginViewModel.showPasswordError.observe(
            viewLifecycleOwner
        ) { errorText: String? -> tilPassword.error = errorText }
        loginViewModel.openResetPassword.observe(viewLifecycleOwner) {
            findNavController(
                view
            ).navigate(R.id.action_loginByEmailFragment_to_resetPasswordFragment)
        }
        tvForgotPassword.setOnClickListener { loginViewModel.onForgotPasswordClick() }
    }
}