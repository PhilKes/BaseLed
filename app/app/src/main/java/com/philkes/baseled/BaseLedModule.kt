package com.philkes.baseled

import android.content.Context
import com.philkes.baseled.service.EspRestClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class BaseLedModule {

    @Singleton
    @Provides
    fun espSearchService(settings: Settings) =
        EspRestClient(settings)


    @Singleton
    @Provides
    fun settings(@ApplicationContext context: Context) = Settings(context)

}