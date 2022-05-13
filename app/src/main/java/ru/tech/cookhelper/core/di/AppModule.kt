package ru.tech.cookhelper.core.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import ru.tech.cookhelper.core.constants.Constants.BASE_URL
import ru.tech.cookhelper.data.local.CookHelperDatabase
import ru.tech.cookhelper.data.remote.api.CookHelperApi
import ru.tech.cookhelper.data.repository.CookHelperRepositoryImpl
import ru.tech.cookhelper.domain.repository.CookHelperRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideCookHelperApi(): CookHelperApi = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()
        .create(CookHelperApi::class.java)

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext applicationContext: Context
    ): CookHelperDatabase = Room.databaseBuilder(
        applicationContext,
        CookHelperDatabase::class.java,
        "kastybiy_db"
    ).fallbackToDestructiveMigration().fallbackToDestructiveMigration().build()

    @Provides
    @Singleton
    fun provideCookHelperRepository(
        api: CookHelperApi,
        db: CookHelperDatabase
    ): CookHelperRepository =
        CookHelperRepositoryImpl(api, db.favRecipeDao, db.fridgeDao, db.settingsDao)

}