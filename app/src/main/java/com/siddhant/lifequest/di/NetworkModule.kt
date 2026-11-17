package com.siddhant.lifequest.di

import com.siddhant.lifequest.BuildConfig
import com.siddhant.lifequest.data.preferences.TokenManager
import com.siddhant.lifequest.data.remote.api.AuthApi
import com.siddhant.lifequest.data.remote.api.LifeQuestApi
import com.siddhant.lifequest.data.remote.api.QuestApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides @Singleton
    fun provideAuthInterceptor(tokenManager: TokenManager): Interceptor = Interceptor { chain ->
        val request = chain.request()
        val path = request.url.encodedPath
        val isAuthCall = path.endsWith("/api/auth/login") || path.endsWith("/api/auth/register")
        val token = if (isAuthCall) null else tokenManager.currentToken
        val builder = request.newBuilder()
        if (!token.isNullOrBlank()) builder.header("Authorization", "Bearer $token")
        chain.proceed(builder.build())
    }

    @Provides @Singleton
    fun provideOkHttp(interceptor: Interceptor): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC else HttpLoggingInterceptor.Level.NONE
            redactHeader("Authorization")
        }
        return OkHttpClient.Builder().addInterceptor(interceptor).addInterceptor(logging)
            .connectTimeout(20, TimeUnit.SECONDS).readTimeout(20, TimeUnit.SECONDS).build()
    }

    @Provides @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_URL).client(client).addConverterFactory(GsonConverterFactory.create()).build()

    @Provides @Singleton fun provideAuthApi(retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)
    @Provides @Singleton fun provideQuestApi(retrofit: Retrofit): QuestApi = retrofit.create(QuestApi::class.java)
    @Provides @Singleton fun provideLifeQuestApi(retrofit: Retrofit): LifeQuestApi = retrofit.create(LifeQuestApi::class.java)
}
