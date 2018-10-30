package jp.shiita.yorimichi.data.api

import io.reactivex.Single
import jp.shiita.yorimichi.data.Place

class PlaceRepository(private val placeService: PlaceService) {
    fun getPlacesWithType(location: String, radius: Int, type: String): Single<Place> =
            placeService.getPlacesWithType(location, radius, type)

    fun getPlacesWithKeyword(location: String, radius: Int, keyword: String): Single<Place> =
            placeService.getPlacesWithKeyword(location, radius, keyword)
}
