package com.siddhant.lifequest.di

import com.siddhant.lifequest.BuildConfig
import com.siddhant.lifequest.data.preferences.TokenManager
import com.siddhant.lifequest.data.remote.api.AuthApi
import com.siddhant.lifequest.data.remote.api.QuestApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideAuthInterceptor(tokenManager: TokenManager) = Interceptor { chain ->
        val request = chain.request()
        val isAuthCall = request.url.encodedPath.endsWith("/api/auth/login") ||
            request.url.encodedPath.endsWith("/api/auth/register")
        val token = if (isAuthCall) null else runBlocking { tokenManager.token.first() }
        val builder = request.newBuilder()
        if (!token.isNullOrBlank()) builder.header("Authorization", "Bearer $token")
        chain.proceed(builder.build())
    }

    @Provides
    @Singleton
    fun provideOkHttp(interceptor: Interceptor): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC
            else HttpLoggingInterceptor.Level.NONE
            redactHeader("Authorization")
        }
        return OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .addInterceptor(logging)
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)

    @Provides @Singleton
    fun provideQuestApi(retrofit: Retrofit): QuestApi = retrofit.create(QuestApi::class.java)
}
