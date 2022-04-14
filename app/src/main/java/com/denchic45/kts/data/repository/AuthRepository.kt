package com.denchic45.kts.data.repository

import android.content.Context
import android.util.Log
import com.denchic45.appVersion.AppVersionService
import com.denchic45.kts.data.NetworkService
import com.denchic45.kts.data.Repository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepository @Inject constructor(
    override val context: Context,
    override val networkService: NetworkService,
    override val appVersionService: AppVersionService
) : Repository(context) {
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

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
        Log.d("lol", "A authByEmail: ")
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .await()
    }

    fun createNewUser(email: String, password: String) {
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
}