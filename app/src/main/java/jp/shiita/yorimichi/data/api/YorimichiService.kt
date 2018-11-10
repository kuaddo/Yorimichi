package jp.shiita.yorimichi.data.api

import com.google.gson.JsonObject
import io.reactivex.Completable
import io.reactivex.Single
import jp.shiita.yorimichi.data.DirectionResult
import jp.shiita.yorimichi.data.GoodResult
import jp.shiita.yorimichi.data.PlaceResult
import jp.shiita.yorimichi.data.User
import retrofit2.Response
import retrofit2.http.*

interface YorimichiService {
    @POST("users/")
    fun createUser(): Single<User>

    @GET("users/{uuid}/")
    fun getUser(@Path("uuid") uuid: String): Single<Response<User>>

    @GET("users/{uuid}/posts/")
    fun getUserPosts(@Path("uuid") uuid: String): Single<Response<JsonObject>>

    @POST("users/{uuid}/points/")
    fun addPoints(@Path("uuid") uuid: String, @Body body: Map<String, String>): Single<User>

    @GET("users/{uuid}/goods")
    fun getGoods(@Path("uuid") uuid: String): Single<GoodResult>

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
                             @Query("keyword", encoded = true) keyword: String): Single<PlaceResult>

    @GET("places/getnext/")
    fun getNextPlaces(@Query("pageToken") pageToken: String): Single<PlaceResult>

    @GET("places/direction/")
    fun getDirection(@Query("origin") origin: String,
                     @Query("destination") destination: String): Single<DirectionResult>

    @GET("goods/icons/{icon_id}/")
    fun getIcon(@Path("icon_id") iconId: Int): Single<JsonObject>
}