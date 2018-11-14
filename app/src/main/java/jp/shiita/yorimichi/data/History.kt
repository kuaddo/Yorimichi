package jp.shiita.yorimichi.data

import jp.shiita.yorimichi.util.parseLocalDateTime
import org.threeten.bp.LocalDateTime

data class History(
        val id: Int,
        val userId: Int,
        val placeUid: String,
        val isValid: Boolean,
        val createdAt: String,
        val updatedAt: String
) {
    val createdAtDateTime: LocalDateTime
        get() = parseLocalDateTime(createdAt)
    val updatedAtDateTime: LocalDateTime
        get() = parseLocalDateTime(updatedAt)
}