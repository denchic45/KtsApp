package com.denchic45.kts.di.modules

import com.denchic45.kts.data.remote.storage.UserRemoteStorage
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides

@Module
object StorageModule {

    @Provides
    fun provideFirebaseStorage(): FirebaseStorage = FirebaseStorage.getInstance()

    @Provides
    fun provideUserRemoteStorage(storage: FirebaseStorage) = UserRemoteStorage(storage)

}