package jp.shiita.yorimichi.data.api

import com.google.gson.Gson
import io.reactivex.Completable
import io.reactivex.Single
import jp.shiita.yorimichi.data.Post
import jp.shiita.yorimichi.util.toBase64

class YorimichiRepository(
        private val yorimichiService: YorimichiService,
        private val gson: Gson
) {
    fun createUser(): Single<String> = yorimichiService.createUser()
            .map { json -> json.getAsJsonPrimitive("uuid").asString }

    fun getUserPosts(uuid: String): Single<List<Post>> = yorimichiService.getUserPosts(uuid)
            .map { response ->
                val body = response.body()
                if (response.code() == 204 || body == null) emptyList()
                else body.getAsJsonArray("posts_array")
                    .map { gson.fromJson(it, Post::class.java) }}

    fun postPost(uuid: String, placeUid: String, bytes: ByteArray): Completable =
            yorimichiService.postPost(mapOf("uuid" to uuid, "place_uid" to placeUid, "b64image" to bytes.toBase64()))

    fun getPlacePosts(placeUid: String): Single<List<Post>> = yorimichiService.getPlacePosts(placeUid)
            .map { response ->
                val body = response.body()
                if (response.code() == 204 || body == null) emptyList()
                else body.getAsJsonArray("posts_array")
                    .map { gson.fromJson(it, Post::class.java) }}
}
