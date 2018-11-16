package jp.shiita.yorimichi.data

data class PlaceDetailResult(
        val result: PlaceDetail,
        val status: String
) {
    data class PlaceDetail(
            // PlaceResult.Placeと共通
            val id: String,
            val name: String,
            val geometry: Geometry,
            val icon: String,
            val photos: List<Photo> = emptyList(),
            val placeId: String,
            val rating: Float,
            val reference: String,
            val types: List<String> = emptyList(),
            val vicinity: String,

            // PlaceDetailResult.PlaceDetail特有(一部)
            val formattedAddress: String,
            val formattedPhoneNumber: String
    ) {
        val place: PlaceResult.Place
            get() = PlaceResult.Place(id, name, geometry, icon, photos, placeId, rating, reference, types, vicinity)
    }
}