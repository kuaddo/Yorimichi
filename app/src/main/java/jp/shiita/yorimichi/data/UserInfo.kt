package jp.shiita.yorimichi.data

import com.chibatching.kotpref.KotprefModel

object UserInfo : KotprefModel() {
    var userId by stringPref()
    var latitude by stringPref()
    var longitude by stringPref()
}