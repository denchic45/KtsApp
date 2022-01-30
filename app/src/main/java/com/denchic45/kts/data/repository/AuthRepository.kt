package com.denchic45.kts.data.repository

import android.content.Context
import android.util.Log
import com.denchic45.kts.data.NetworkService
import com.denchic45.kts.data.Repository
import com.denchic45.kts.data.Resource
import com.denchic45.kts.di.modules.IoDispatcher
import com.google.android.gms.tasks.TaskExecutors
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken
import com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.CompletableEmitter
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleEmitter
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AuthRepository @Inject constructor(
    context: Context,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
    private val coroutineScope: CoroutineScope,
    override val networkService: NetworkService
) : Repository(context) {
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var emitter: SingleEmitter<Resource<String>>
    private var verificationId: String? = null
    private var forceResendingToken: ForceResendingToken? = null
    private var callbacks: OnVerificationStateChangedCallbacks?
    private var phoneNum: String? = null
    val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    fun sendUserPhoneNumber(phoneNum: String): Single<Resource<String>> {
        return Single.create { emitter: SingleEmitter<Resource<String>> ->
            this@AuthRepository.phoneNum = phoneNum
            this@AuthRepository.emitter = emitter
            this@AuthRepository.phoneNum = phoneNum
            PhoneAuthProvider.verifyPhoneNumber(
                PhoneAuthOptions.newBuilder(firebaseAuth)
                    .setPhoneNumber(phoneNum) // Phone number to verify
                    .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                    .setCallbacks(callbacks!!) // OnVerificationStateChangedCallbacks
                    .build()
            )
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(TaskExecutors.MAIN_THREAD) { task ->
                if (task.isSuccessful) {
                    val smsCode = credential.smsCode!!
                    emitter.onSuccess(Resource.Success(smsCode))
                    callbacks = null
                } else {
                    emitter.onSuccess(Resource.Error(task.exception!!))
                }
            }
    }

    fun authByPhoneNum(code: String?) {
        signInWithPhoneAuthCredential(PhoneAuthProvider.getCredential(verificationId!!, code!!))
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


    fun authByEmail(mail: String, password: String): Completable {
        return Completable.create { emitter: CompletableEmitter ->
            firebaseAuth.signInWithEmailAndPassword(
                mail, password
            )
                .addOnSuccessListener { emitter.onComplete() }
                .addOnFailureListener { t: Exception -> emitter.onError(t) }
        }
    }

    fun signUpNewUser(email: String?, password: String?) {
        firebaseAuth.createUserWithEmailAndPassword(email!!, password!!)
    }

    fun resetPassword(email: String): Completable {
        return Completable.create { emitter: CompletableEmitter ->
            firebaseAuth.sendPasswordResetEmail(
                email
            )
                .addOnSuccessListener { emitter.onComplete() }
                .addOnFailureListener {
                    it.printStackTrace()
                    emitter.onError(it)
                }
        }
    }

    fun resendCodeSms() {
        PhoneAuthProvider.verifyPhoneNumber(
            PhoneAuthOptions
                .newBuilder(FirebaseAuth.getInstance())
                .setPhoneNumber(phoneNum!!)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setCallbacks(callbacks!!)
                .setForceResendingToken(forceResendingToken!!)
                .build()
        )
    }

    init {
        callbacks = object : OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Log.d("lol", "onVerificationFailed: ", e)
                emitter.onError(e)
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
    }
}