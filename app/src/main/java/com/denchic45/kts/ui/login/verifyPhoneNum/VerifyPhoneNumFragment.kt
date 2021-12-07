package com.denchic45.kts.ui.login.verifyPhoneNum

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import by.kirich1409.viewbindingdelegate.viewBinding
import com.denchic45.kts.R
import com.denchic45.kts.databinding.FragmentVerifyPhoneNumberBinding
import com.denchic45.kts.ui.BaseFragment
import com.denchic45.kts.ui.login.LoginViewModel
import com.denchic45.kts.ui.login.verifyPhoneNum.VerifyPhoneNumViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import com.jakewharton.rxbinding4.widget.textChanges
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.*

class VerifyPhoneNumFragment :
    BaseFragment<VerifyPhoneNumViewModel, FragmentVerifyPhoneNumberBinding>() {
    override val binding: FragmentVerifyPhoneNumberBinding by viewBinding(
        FragmentVerifyPhoneNumberBinding::bind
    )
    override val viewModel: VerifyPhoneNumViewModel by viewModels { viewModelFactory }
    private lateinit var tnlCode1: TextInputLayout
    private lateinit var tnlCode2: TextInputLayout
    private lateinit var tnlCode3: TextInputLayout
    private lateinit var tnlCode4: TextInputLayout
    private lateinit var tnlCode5: TextInputLayout
    private lateinit var tnlCode6: TextInputLayout
    private var etCode1: EditText? = null
    private var etCode2: EditText? = null
    private var etCode3: EditText? = null
    private var etCode4: EditText? = null
    private var etCode5: EditText? = null
    private var etCode6: EditText? = null
    private var btnAuth: Button? = null
    private lateinit var pbCountDown: ProgressBar
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_verify_phone_number, container, false)
        tnlCode1 = root.findViewById(R.id.textInputLayout_code_1)
        tnlCode2 = root.findViewById(R.id.textInputLayout_code_2)
        tnlCode3 = root.findViewById(R.id.textInputLayout_code_3)
        tnlCode4 = root.findViewById(R.id.textInputLayout_code_4)
        tnlCode5 = root.findViewById(R.id.textInputLayout_code_5)
        tnlCode6 = root.findViewById(R.id.textInputLayout_code_6)
        etCode1 = root.findViewById(R.id.editText_code_1)
        etCode2 = root.findViewById(R.id.editText_code_2)
        etCode3 = root.findViewById(R.id.editText_code_3)
        etCode4 = root.findViewById(R.id.editText_code_4)
        etCode5 = root.findViewById(R.id.editText_code_5)
        etCode6 = root.findViewById(R.id.editText_code_6)
        btnAuth = root.findViewById(R.id.button_auth)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pbCountDown = view.findViewById(R.id.progressbar_countdown_resend_code)
        val btnResendCode = view.findViewById<Button>(R.id.btn_resend_code)
        val loginViewModel = ViewModelProvider(requireActivity()).get(
            LoginViewModel::class.java
        )
        loginViewModel.verifyUser.observe(viewLifecycleOwner, { phoneNum: String? ->
            viewModel.onVerifyClick(
                phoneNum!!
            )
        })
        viewModel.authSuccessful.observe(viewLifecycleOwner, { code: String? ->
            fillFieldsWithCode(code)
            loginViewModel.onSuccessfulLogin()
        })
        viewModel.showProgressTimeOut.observe(viewLifecycleOwner, { progress: Int? ->
            pbCountDown.setProgress(
                progress!!, true
            )
        })
        viewModel.enableResendCode.observe(
            viewLifecycleOwner,
            { enabled: Boolean? -> btnResendCode.isEnabled = enabled!! })
        etCode1!!.textChanges()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { s: CharSequence ->
                viewModel.onCharTyped(typedCode)
                if (!s.toString().isEmpty()) etCode2!!.requestFocus()
            }
        etCode2!!.textChanges()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { s: CharSequence ->
                viewModel.onCharTyped(typedCode)
                if (!s.toString().isEmpty()) etCode3!!.requestFocus()
            }
        etCode3!!.textChanges()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map { obj: CharSequence -> obj.toString() }
            .subscribe { s: String ->
                viewModel.onCharTyped(s)
                if (!s.isEmpty()) etCode4!!.requestFocus()
            }
        etCode4!!.textChanges()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { s: CharSequence ->
                viewModel.onCharTyped(typedCode)
                if (!s.toString().isEmpty()) etCode5!!.requestFocus()
            }
        etCode5!!.textChanges()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { s: CharSequence ->
                viewModel.onCharTyped(typedCode)
                if (!s.toString().isEmpty()) etCode6!!.requestFocus()
            }
        etCode6!!.textChanges()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { s: CharSequence ->
                viewModel.onCharTyped(typedCode)
                if (!s.toString().isEmpty()) {
                    etCode6!!.clearFocus()
                    val imm =
                        requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(etCode6!!.applicationWindowToken, 0)
                }
            }
        btnAuth!!.setOnClickListener { view1: View? ->
            viewModel.tryAuthWithPhoneNumByCode(
                typedCode
            )
        }
        btnResendCode.setOnClickListener { v: View? -> viewModel.onResendCodeClick() }
        viewModel.errorToManyRequest.observe(
            viewLifecycleOwner,
            { o: Any? -> showErrorManyRequest() })
        viewModel.errorInvalidRequest.observe(
            viewLifecycleOwner,
            { o: Any? -> showErrorInvalidCode() })
        viewModel.btnAuthVisibility.observe(viewLifecycleOwner, { visible: Boolean ->
            Log.d("lol", "onCreateView: $visible")
            if (visible) {
                btnAuth!!.visibility = View.VISIBLE
                btnAuth!!.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setInterpolator(FastOutSlowInInterpolator())
                    .setDuration(300)
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            super.onAnimationEnd(animation)
                            btnAuth!!.scaleX = 1f
                            btnAuth!!.scaleY = 1f
                        }
                    })
                Log.d("lol", "anim: ")
            } else {
                btnAuth!!.animate()
                    .scaleX(0f)
                    .scaleY(0f)
                    .setInterpolator(FastOutSlowInInterpolator())
                    .setDuration(300)
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            super.onAnimationEnd(animation)
                            btnAuth!!.visibility = View.INVISIBLE
                        }
                    })
            }
        })
    }

    private fun showErrorManyRequest() {
        val builder = MaterialAlertDialogBuilder(
            requireActivity(), R.style.MaterialAlertDialog_Rounded
        )
            .setTitle("Ошибка")
            .setMessage("Превышен лимит попыток входа. Повторите позже")
            .setPositiveButton("Ок") { dialogInterface: DialogInterface, i: Int -> dialogInterface.dismiss() }
        builder.create().show()
    }

    private fun showErrorInvalidCode() {
        val textInputLayoutList: List<TextInputLayout> =
            listOf(tnlCode1, tnlCode2, tnlCode3, tnlCode4, tnlCode5, tnlCode6)
        for (i in 0..5) {
            textInputLayoutList[i].error = " "
        }
        Toast.makeText(activity, "Неправильный код!", Toast.LENGTH_SHORT).show()
    }

    private fun fillFieldsWithCode(code: String?) {
        if (code == null) {
            return
        }
        val editTextList = Arrays.asList(etCode1, etCode2, etCode3, etCode4, etCode5, etCode6)
        for (i in 0 until code.length) {
            editTextList[i]!!.setText(code[i].toString())
        }
    }

    private val typedCode: String
        private get() = etCode1!!.text.toString() +
                etCode2!!.text.toString() +
                etCode3!!.text.toString() +
                etCode4!!.text.toString() +
                etCode5!!.text.toString() +
                etCode6!!.text.toString()
}