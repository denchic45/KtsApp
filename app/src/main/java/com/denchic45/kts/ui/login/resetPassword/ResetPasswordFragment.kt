package com.denchic45.kts.ui.login.resetPassword

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.denchic45.kts.R
import com.denchic45.kts.databinding.FragmentResetPasswordBinding
import com.denchic45.kts.ui.BaseFragment
import com.google.android.material.textfield.TextInputLayout

class ResetPasswordFragment : BaseFragment<ResetPasswordViewModel, FragmentResetPasswordBinding>() {
    private var btnSend: Button? = null
    private var etEmail: EditText? = null
    private var tilEmail: TextInputLayout? = null

    override val viewModel: ResetPasswordViewModel by viewModels { viewModelFactory }
    override val binding: FragmentResetPasswordBinding by viewBinding(FragmentResetPasswordBinding::bind)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_reset_password, container, false)
        btnSend = root.findViewById(R.id.btn_send)
        etEmail = root.findViewById(R.id.et_email)
        tilEmail = root.findViewById(R.id.til_email)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btnSend!!.setOnClickListener { v: View? ->
            viewModel.onSendClick(
                etEmail!!.text.toString()
            )
        }
        viewModel.showErrorFieldEmail.observe(viewLifecycleOwner, { error: Boolean ->
            if (error) {
                tilEmail!!.error = "Некоректная почта"
            } else {
                tilEmail!!.error = null
            }
        })
        viewModel.showMessage.observe(viewLifecycleOwner, { s: String? ->
            Toast.makeText(
                activity, s, Toast.LENGTH_LONG
            ).show()
        })
        viewModel.finish.observe(
            viewLifecycleOwner,
            { unused: Void? -> findNavController(view).popBackStack() })
    }
}