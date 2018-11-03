package jp.shiita.yorimichi.di.module

import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import jp.shiita.yorimichi.BuildConfig
import jp.shiita.yorimichi.data.api.YorimichiRepository
import jp.shiita.yorimichi.data.api.YorimichiService
import jp.shiita.yorimichi.scheduler.BaseSchedulerProvider
import jp.shiita.yorimichi.scheduler.SchedulerProvider
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module()
class DataModule {
    @Provides
    @Singleton
    fun provideLowerCaseGson(): Gson = GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create()

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
            .addInterceptor {
                it.proceed(it.request()
                        .newBuilder()
                        .addHeader("X-API-Token", BuildConfig.X_API_TOKEN)
                        .build()) }
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .build()

    @Provides
    @Singleton
    @Named("yorimichi")
    fun provideYorimichiRetrofit(gson: Gson, okHttpClient: OkHttpClient): Retrofit =
            getRetrofit("http://35.200.28.85/v1/", gson, okHttpClient)

    @Provides
    @Singleton
    fun providesYorimichiService(@Named("yorimichi") retrofit: Retrofit): YorimichiService = retrofit.create(YorimichiService::class.java)

    @Provides
    @Singleton
    fun provideYorimichiRepository(yorimichiService: YorimichiService, gson: Gson) = YorimichiRepository(yorimichiService, gson)

    @Provides
    @Singleton
    fun provideBaseSchedulerProvider(): BaseSchedulerProvider = SchedulerProvider

    private fun getRetrofit(baseUrl: String, gson: Gson, okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(okHttpClient)
            .baseUrl(baseUrl)
            .build()
}