package com.denchic45.kts.data.repository

import android.util.Log
import com.denchic45.appVersion.AppVersionService
import com.denchic45.appVersion.GoogleAppVersionService
import com.denchic45.kts.data.NetworkService
import com.denchic45.kts.data.Repository
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken
import com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val coroutineScope: CoroutineScope,
    override val networkService: NetworkService,
    override val appVersionService: AppVersionService
) : Repository() {
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    private val authByPhoneNum = Channel<String>()

    private var verificationId: String? = null
    private var forceResendingToken: ForceResendingToken? = null
    private var phoneNum: String? = null
    val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    private var callbacks: OnVerificationStateChangedCallbacks =
        object : OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                coroutineScope.launch {
                    signInWithPhoneAuthCredential(credential)
                }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Log.d("lol", "onVerificationFailed: ", e)
                authByPhoneNum.close(e)
            }

            override fun onCodeSent(
                verificationId: String,
                forceResendingToken: ForceResendingToken
            ) {
                super.onCodeSent(verificationId, forceResendingToken)
                this@AuthRepository.verificationId = verificationId
                this@AuthRepository.forceResendingToken = forceResendingToken
            }
        }

    fun sendUserPhoneNumber(phoneNum: String): Channel<String> {
        this@AuthRepository.phoneNum = phoneNum
        PhoneAuthProvider.verifyPhoneNumber(
            PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber(phoneNum) // Phone number to verify
                .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
                .build()
        )
        return authByPhoneNum
    }

    private suspend fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        try {
            firebaseAuth.signInWithCredential(credential)
                .await()
                .apply {
                    val smsCode = credential.smsCode!!
                    authByPhoneNum.send(smsCode)
                }
        } catch (t: Throwable) {
            authByPhoneNum.close(t)
        }

    }

    suspend fun authByPhoneNum(code: String) {
        signInWithPhoneAuthCredential(PhoneAuthProvider.getCredential(verificationId!!, code))
    }

    fun signOut() {
        firebaseAuth.signOut()
    }

    val listenAuthState: Flow<Boolean> = callbackFlow {
        val listener: (FirebaseAuth) -> Unit = {

            trySend(firebaseAuth.currentUser != null)
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose {
            firebaseAuth.removeAuthStateListener { listener }
        }
    }


    suspend fun authByEmail(email: String, password: String) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .await()
    }

    fun signUpNewUser(email: String, password: String) {
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
            if (it.exception != null) {
                it.exception!!.printStackTrace()
            }
        }
    }

    suspend fun resetPassword(email: String) {
        firebaseAuth.sendPasswordResetEmail(email)
            .await()
    }

    fun resendCodeSms() {
        PhoneAuthProvider.verifyPhoneNumber(
            PhoneAuthOptions
                .newBuilder(FirebaseAuth.getInstance())
                .setPhoneNumber(phoneNum!!)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setCallbacks(callbacks)
                .setForceResendingToken(forceResendingToken!!)
                .build()
        )
    }
}