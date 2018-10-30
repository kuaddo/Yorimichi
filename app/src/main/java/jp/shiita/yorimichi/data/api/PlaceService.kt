package jp.shiita.yorimichi.data.api

import io.reactivex.Single
import jp.shiita.yorimichi.BuildConfig
import jp.shiita.yorimichi.data.PlaceResult
import retrofit2.http.GET
import retrofit2.http.Query

interface PlaceService {
    @GET("nearbysearch/json?language=ja&opennow&rankby=distance&key=${BuildConfig.GOOGLE_MAPS}")
    fun getPlacesWithType(@Query("location") location: String,
                          @Query("radius") radius: Int,
                          @Query("type") type: String): Single<PlaceResult>

    @GET("nearbysearch/json?language=ja&opennow&rankby=distance&key=${BuildConfig.GOOGLE_MAPS}")
    fun getPlacesWithKeyword(@Query("location") location: String,
                             @Query("radius") radius: Int,
                             @Query("keyword") keyword: String): Single<PlaceResult>

    @GET("nearbysearch/json?key=${BuildConfig.GOOGLE_MAPS}")
    fun getNextPlaces(@Query("pagetoken") pageToken: String): Single<PlaceResult>
}