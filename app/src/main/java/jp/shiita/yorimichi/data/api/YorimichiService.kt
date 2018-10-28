package jp.shiita.yorimichi.data.api

import com.google.gson.JsonObject
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.*

interface YorimichiService {
    @POST("users/")
    fun createUser(): Single<JsonObject>

    @GET("users/{uuid}/posts/")
    fun getUserPosts(@Path("uuid") uuid: String): Single<Response<JsonObject>>

    @POST("posts/")
    fun postPost(@Body body: Map<String, String>): Completable

    @GET("places/{place_uid}/posts/")
    fun getPlacePosts(@Path("place_uid") placeUid: String): Single<Response<JsonObject>>
}