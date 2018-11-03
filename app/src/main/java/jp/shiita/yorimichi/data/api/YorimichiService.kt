package jp.shiita.yorimichi.data.api

import com.google.gson.JsonObject
import io.reactivex.Completable
import io.reactivex.Single
import jp.shiita.yorimichi.data.PlaceResult
import retrofit2.Response
import retrofit2.http.*

interface YorimichiService {
    @POST("users/")
    fun createUser(): Single<JsonObject>

    @GET("users/{uuid}/")
    fun getUser(@Path("uuid") uuid: String): Single<Response<JsonObject>>

    @GET("users/{uuid}/posts/")
    fun getUserPosts(@Path("uuid") uuid: String): Single<Response<JsonObject>>

    @POST("users/{uuid}/points/")
    fun addPoints(@Path("uuid") uuid: String, @Body body: Map<String, String>): Completable

    @POST("posts/")
    fun postPost(@Body body: Map<String, String>): Completable

    @GET("places/{place_uid}/posts/")
    fun getPlacePosts(@Path("place_uid") placeUid: String): Single<Response<JsonObject>>

    @GET("places/searchbytype/")
    fun getPlacesWithType(@Query("location") location: String,
                          @Query("radius") radius: Int,
                          @Query("type") type: String): Single<PlaceResult>


    @GET("places/searchbykeyword/")
    fun getPlacesWithKeyword(@Query("location") location: String,
                             @Query("radius") radius: Int,
                             @Query("keyword") keyword: String): Single<PlaceResult>

    @GET("places/getnext/")
    fun getNextPlaces(@Query("pageToken") pageToken: String): Single<PlaceResult>
}