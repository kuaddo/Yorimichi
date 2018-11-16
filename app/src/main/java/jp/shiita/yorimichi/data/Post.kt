package jp.shiita.yorimichi.data

import jp.shiita.yorimichi.util.parseLocalDateTime
import org.threeten.bp.LocalDateTime

data class Post(
        val id: Int,
        val userId: Int,
        val placeUid: String,
        val bucket: String,
        val imageName: String,
        val createdAt: String,
        val updatedAt: String
) {
    val createdAtDateTime: LocalDateTime
        get() = parseLocalDateTime(createdAt)
    val updatedAtDateTime: LocalDateTime
        get() = parseLocalDateTime(updatedAt)
}