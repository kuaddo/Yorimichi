package jp.shiita.yorimichi.data

data class GoodResult(
        val colors: List<Color>,
        val icons: List<Icon>,
        val pens: List<Pen>
) {
    data class Color(
            val id: Int,
            val name: String,
            val argbCode: String,
            val isPurchased: Boolean
    ) {
        val color: Int get() = android.graphics.Color.parseColor(argbCode)
        var selected: Boolean = false       // RecyclerViewで選択されているかどうか
    }

    data class Icon(
            val id: Int,
            val name: String,
            val bucket: String,
            val filename: String,
            val isPurchased: Boolean
    )

    data class Pen(
            val id: Int,
            val name: String,
            val previewBucket: String,
            val previewFilename: String,
            val textureBucket: String,
            val textureFilename: String,
            val isPurchased: Boolean
    )
}