package com.denchic45.kts.ui.login.verifyPhoneNum

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.denchic45.kts.R
import com.denchic45.kts.databinding.FragmentVerifyPhoneNumberBinding
import com.denchic45.kts.ui.BaseFragment
import com.denchic45.kts.ui.login.LoginViewModel
import com.denchic45.kts.utils.closeKeyboard
import com.denchic45.kts.utils.collectWhenStarted
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import com.jakewharton.rxbinding4.widget.textChanges
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers

class VerifyPhoneNumFragment :
    BaseFragment<VerifyPhoneNumViewModel, FragmentVerifyPhoneNumberBinding>(R.layout.fragment_verify_phone_number) {
    override val binding: FragmentVerifyPhoneNumberBinding by viewBinding(
        FragmentVerifyPhoneNumberBinding::bind
    )
    override val viewModel: VerifyPhoneNumViewModel by viewModels { viewModelFactory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            val btnResendCode = view.findViewById<Button>(R.id.btn_resend_code)
            val loginViewModel = ViewModelProvider(requireActivity())[LoginViewModel::class.java]
            loginViewModel.verifyUser.observe(viewLifecycleOwner) { phoneNum: String ->
                viewModel.onVerifyClick(phoneNum)
            }
            viewModel.authSuccessful.collectWhenStarted(lifecycleScope) { code: String ->
                fillFieldsWithCode(code)
                loginViewModel.onSuccessfulLogin()
            }
            viewModel.showProgressTimeOut.observe(viewLifecycleOwner) { progress: Int ->
                pbCountDown.setProgress(
                    progress, true
                )
            }
            viewModel.enableResendCode.observe(
                viewLifecycleOwner
            ) { enabled: Boolean -> btnResendCode.isEnabled = enabled }
            etCode1.textChanges()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { s: CharSequence ->
                    viewModel.onCharTyped(typedCode)
                    if (s.toString().isNotEmpty()) etCode2.requestFocus()
                }
            etCode2.textChanges()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { s: CharSequence ->
                    viewModel.onCharTyped(typedCode)
                    if (s.toString().isNotEmpty()) etCode3.requestFocus()
                }
            etCode3.textChanges()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map { obj: CharSequence -> obj.toString() }
                .subscribe { s: String ->
                    viewModel.onCharTyped(s)
                    if (s.isNotEmpty()) etCode4.requestFocus()
                }
            etCode4.textChanges()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { s: CharSequence ->
                    viewModel.onCharTyped(typedCode)
                    if (s.toString().isNotEmpty()) etCode5.requestFocus()
                }
            etCode5.textChanges()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { s: CharSequence ->
                    viewModel.onCharTyped(typedCode)
                    if (s.toString().isNotEmpty()) etCode6.requestFocus()
                }
            etCode6.textChanges()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { s: CharSequence ->
                    viewModel.onCharTyped(typedCode)
                    if (s.toString().isNotEmpty()) {
                        etCode6.closeKeyboard()
                    }
                }
            btnAuth.setOnClickListener {
                viewModel.tryAuthWithPhoneNumByCode(
                    typedCode
                )
            }
            btnResendCode.setOnClickListener { viewModel.onResendCodeClick() }
            viewModel.errorToManyRequest.observe(
                viewLifecycleOwner
            ) { showErrorManyRequest() }
            viewModel.errorInvalidRequest.observe(
                viewLifecycleOwner
            ) { showErrorInvalidCode() }
            viewModel.btnAuthVisibility.observe(viewLifecycleOwner) { visible: Boolean ->
                Log.d("lol", "onCreateView: $visible")
                if (visible) {
                    btnAuth.visibility = View.VISIBLE
                    btnAuth.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setInterpolator(FastOutSlowInInterpolator())
                        .setDuration(300)
                        .setListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator) {
                                super.onAnimationEnd(animation)
                                btnAuth.scaleX = 1f
                                btnAuth.scaleY = 1f
                            }
                        })
                    Log.d("lol", "anim: ")
                } else {
                    btnAuth.animate()
                        .scaleX(0f)
                        .scaleY(0f)
                        .setInterpolator(FastOutSlowInInterpolator())
                        .setDuration(300)
                        .setListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator) {
                                super.onAnimationEnd(animation)
                                btnAuth.visibility = View.INVISIBLE
                            }
                        })
                }
            }
        }
    }

    private fun showErrorManyRequest() {
        val builder = MaterialAlertDialogBuilder(
            requireActivity(), R.style.MaterialAlertDialog_Rounded
        )
            .setTitle("Ошибка")
            .setMessage("Превышен лимит попыток входа. Повторите позже")
            .setPositiveButton("Ок") { dialogInterface: DialogInterface, _: Int -> dialogInterface.dismiss() }
        builder.create().show()
    }

    private fun showErrorInvalidCode() {
        with(binding) {
            val textInputLayoutList: List<TextInputLayout> =
                listOf(tilCode2, tilCode2, tilCode3, tilCode4, tilCode5, tilCode6)
            for (i in 0..5) {
                textInputLayoutList[i].error = " "
            }
            Toast.makeText(activity, "Неправильный код!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fillFieldsWithCode(code: String?) {
        with(binding) {
            if (code == null) {
                return
            }
            val editTextList = listOf(etCode1, etCode2, etCode3, etCode4, etCode5, etCode6)
            for (i in code.indices) {
                editTextList[i].setText(code[i].toString())
            }
        }
    }

    private val typedCode: String
        get() = with(binding) {
            etCode1.text.toString() +
                    etCode2.text.toString() +
                    etCode3.text.toString() +
                    etCode4.text.toString() +
                    etCode5.text.toString() +
                    etCode6.text.toString()
        }
}