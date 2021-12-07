package com.denchic45.kts.ui.login.auth

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.denchic45.kts.R
import com.denchic45.kts.databinding.FragmentAuthBinding
import com.denchic45.kts.di.viewmodel.ViewModelFactory
import com.denchic45.kts.ui.BaseFragment
import com.denchic45.kts.ui.login.LoginViewModel
import javax.inject.Inject

class AuthFragment : BaseFragment<LoginViewModel, FragmentAuthBinding>(R.layout.fragment_auth) {
    override val binding: FragmentAuthBinding by viewBinding(FragmentAuthBinding::bind)

    override val viewModel: LoginViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            btnSms.setOnClickListener { viewModel.onSmsClick() }
            btnMail.setOnClickListener { viewModel.onEmailClick() }
        }
    }

    companion object {
        fun newInstance(): AuthFragment {
            return AuthFragment()
        }
    }
}