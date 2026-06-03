package com.powerfulboy.app.di

import android.content.Context
import androidx.room.Room
import com.google.gson.Gson
import com.powerfulboy.app.data.dao.*
import com.powerfulboy.app.data.database.AppDatabase
import com.powerfulboy.app.network.AnthropicApi
import com.powerfulboy.app.network.OpenFoodFactsApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "powerfulboy.db").build()

    @Provides fun provideFoodDao(db: AppDatabase): FoodDao = db.foodDao()
    @Provides fun provideRecipeDao(db: AppDatabase): RecipeDao = db.recipeDao()
    @Provides fun provideMealLogDao(db: AppDatabase): MealLogDao = db.mealLogDao()
    @Provides fun provideWeeklyPlanDao(db: AppDatabase): WeeklyPlanDao = db.weeklyPlanDao()
    @Provides fun provideSettingsDao(db: AppDatabase): SettingsDao = db.settingsDao()

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    @Provides
    @Singleton
    @Named("off")
    fun provideOFFRetrofit(client: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl("https://world.openfoodfacts.org/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Provides
    @Singleton
    @Named("anthropic")
    fun provideAnthropicRetrofit(client: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl("https://api.anthropic.com/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Provides
    @Singleton
    fun provideOpenFoodFactsApi(@Named("off") retrofit: Retrofit): OpenFoodFactsApi =
        retrofit.create(OpenFoodFactsApi::class.java)

    @Provides
    @Singleton
    fun provideAnthropicApi(@Named("anthropic") retrofit: Retrofit): AnthropicApi =
        retrofit.create(AnthropicApi::class.java)

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()
}
