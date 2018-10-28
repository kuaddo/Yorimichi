package jp.shiita.yorimichi.data

data class Post(
        val id: Int,
        val userId: Int,
        val placeUid: String,
        val bucketName: String,
        val imageName: String,
        val createdAt: String,
        val updatedAt: String
)