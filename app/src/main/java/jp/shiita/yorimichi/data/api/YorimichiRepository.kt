package jp.shiita.yorimichi.data.api

import com.google.gson.Gson
import io.reactivex.Completable
import io.reactivex.Single
import jp.shiita.yorimichi.data.*
import jp.shiita.yorimichi.util.toBase64

class YorimichiRepository(
        private val yorimichiService: YorimichiService,
        private val gson: Gson
) {
    fun createUser(): Single<User> = yorimichiService.createUser()

    fun getUser(uuid: String): Single<User> = yorimichiService.getUser(uuid)
            .map { response ->
                val body = response.body()
                if (response.code() == 204 || body == null) error("no content")
                else body }

    fun getUserPosts(uuid: String): Single<List<Post>> = yorimichiService.getUserPosts(uuid)
            .map { response ->
                val body = response.body()
                if (response.code() == 204 || body == null) emptyList()
                else body.getAsJsonArray("posts_array")
                    .map { gson.fromJson(it, Post::class.java) }}

    fun addPoints(uuid: String, additionalPoints: Int): Single<User> =
            yorimichiService.addPoints(uuid, mapOf("point" to additionalPoints.toString()))

    fun purchaseGoods(uuid: String, goodsId: Int): Single<GoodsResult> =
            yorimichiService.purchaseGoods(uuid, goodsId)

    fun changeIcon(uuid: String, iconId: Int): Single<User> =
            yorimichiService.changeIcon(uuid, iconId)

    fun visitPlace(uuid: String, placeUid: String) = yorimichiService.visitPlace(uuid, placeUid)

    fun getVisitHistory(uuid: String): Single<List<History>> =
            yorimichiService.getVisitHistory(uuid)

    fun getGoods(uuid: String): Single<GoodsResult> =
            yorimichiService.getGoods(uuid)

    fun postPost(uuid: String, placeUid: String, bytes: ByteArray): Completable =
            yorimichiService.postPost(mapOf("uuid" to uuid, "place_uid" to placeUid, "b64image" to bytes.toBase64()))

    fun getPlacePosts(placeUid: String): Single<List<Post>> = yorimichiService.getPlacePosts(placeUid)
            .map { response ->
                val body = response.body()
                if (response.code() == 204 || body == null) emptyList()
                else body.getAsJsonArray("posts_array")
                    .map { gson.fromJson(it, Post::class.java) }}

    fun getPlacesWithType(location: String, radius: Int, type: String): Single<PlaceResult> =
            yorimichiService.getPlacesWithType(location, radius, type)


    fun getPlacesWithKeyword(location: String, radius: Int, keyword: String): Single<PlaceResult> =
            yorimichiService.getPlacesWithKeyword(location, radius, keyword)


    fun getNextPlaces(pageToken: String): Single<PlaceResult> =
            yorimichiService.getNextPlaces(pageToken)

    fun getDirection(origin: String, destination: String): Single<DirectionResult> =
            yorimichiService.getDirection(origin, destination)

    fun getPlaceDetail(placeUid: String): Single<PlaceDetailResult> =
            yorimichiService.getPlaceDetail(placeUid)

    fun getIcon(iconId: Int): Single<Pair<String, String>> = yorimichiService.getIcon(iconId)
            .map { json -> json.getAsJsonPrimitive("bucket").asString to json.getAsJsonPrimitive("filename").asString }
}
