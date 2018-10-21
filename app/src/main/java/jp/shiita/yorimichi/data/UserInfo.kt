package jp.shiita.yorimichi.data

import com.chibatching.kotpref.KotprefModel

object UserInfo : KotprefModel() {
    var latitude by stringPref()
    var longitude by stringPref()
}