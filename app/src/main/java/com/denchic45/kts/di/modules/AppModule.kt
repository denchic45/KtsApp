package com.denchic45.kts.di.modules

import android.app.Application
import android.content.Context
import com.denchic45.appVersion.AppVersionService
import com.denchic45.appVersion.GoogleAppVersionService
import com.denchic45.kts.data.prefs.AppPreference
import com.denchic45.kts.data.prefs.TimestampPreference
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import retrofit2.Retrofit
import javax.inject.Singleton


@Module
class AppModule(private val application: Application) {

    @Singleton
    @Provides
    fun provideAppVersionService(
        context: Context,
        coroutineScope: CoroutineScope,
        timestampPreference: TimestampPreference,
        appPreference: AppPreference
    ): AppVersionService =
        GoogleAppVersionService(context, coroutineScope, appPreference, timestampPreference)

    @Provides
    @Singleton
    fun provideApplicationScope() = CoroutineScope(SupervisorJob())

    @Provides
    @Singleton
    fun providesApplication(): Application = application

    @Provides
    @Singleton
    fun providesApplicationContext(): Context = application

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("http://localhost/")
            .build()
    }
}