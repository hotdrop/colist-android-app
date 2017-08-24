package jp.hotdrop.compl.repository

import jp.hotdrop.compl.BuildConfig
import jp.hotdrop.compl.api.ComlisClient
import jp.hotdrop.compl.api.service.CdsService
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class MockClient {

    // TODO This class is terrible.
    // Should be in fact implements Client Class, and make some mock method.

    // But, now I want to try to connect remote server with same implementation as main so I made it the
    // same as the implementation of ApplicationModule.
    private val httpClient = OkHttpClient.Builder().build()

    fun create(): ComlisClient {
        val cdsService = Retrofit.Builder()
                .client(httpClient)
                .baseUrl(BuildConfig.API_ROOT)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(CdsService::class.java)
        return ComlisClient(cdsService)
    }
}