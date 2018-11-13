package jp.shiita.yorimichi.util

import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter
import java.util.*

/**
 * "Tue, 13 Nov 2018 09:38:07 GMT"の形式の文字列をパース
 * "GMT"の除去と、日本の標準時への調整(+9hour)を行う
 */
fun parseLocalDateTime(text: String): LocalDateTime =
        LocalDateTime.parse(text.dropLast(4), formatter).plusHours(9)

private val formatter = DateTimeFormatter.ofPattern("E, dd MMM yyyy HH:mm:ss", Locale.US)
