package com.denchic45.kts.ui.login.loginByPhoneNum

import android.content.Context
import android.os.Bundle
import android.telephony.PhoneNumberFormattingTextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.denchic45.kts.R
import com.denchic45.kts.ui.login.LoginViewModel
import com.google.android.material.textfield.TextInputLayout

class LoginByPhoneNumFragment : Fragment() {
    private lateinit var etNum: EditText
    private lateinit var tnlNum: TextInputLayout
    private lateinit var btnGetCode: Button
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_login_by_phone_num, container, false)
        etNum = root.findViewById(R.id.editText_num)
        tnlNum = root.findViewById(R.id.til_num)
        btnGetCode = root.findViewById(R.id.btn_next)
        etNum.addTextChangedListener(PhoneNumberFormattingTextWatcher())
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val loginViewModel = ViewModelProvider(requireActivity())[LoginViewModel::class.java]
        loginViewModel.errorNum.observe(viewLifecycleOwner) { s: String? -> tnlNum.error = s }
        btnGetCode.setOnClickListener {
            val phoneNum = etNum.text.toString()
            loginViewModel.onGetCodeClick(phoneNum)
            val imm =
                requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(etNum.applicationWindowToken, 0)
        }
    }
}