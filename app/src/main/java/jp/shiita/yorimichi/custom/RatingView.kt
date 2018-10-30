package jp.shiita.yorimichi.custom

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import jp.shiita.yorimichi.R

class RatingView(context: Context, attrs: AttributeSet? = null) : LinearLayout(context, attrs) {
    private val starViews: List<StarView>

    init {
        LayoutInflater.from(context).inflate(R.layout.view_rating, this, true)
        starViews = listOf(R.id.starView1, R.id.starView2, R.id.starView3, R.id.starView4, R.id.starView5)
                .map { findViewById<StarView>(it) }

        if (attrs != null) {
            context.theme.obtainStyledAttributes(attrs, R.styleable.RatingView, 0, 0).run {
                if (hasValue(R.styleable.RatingView_rating)) setRating(getFloat(R.styleable.RatingView_rating, 0f))
                if (hasValue(R.styleable.RatingView_starSize)) setStarSize(getDimension(R.styleable.RatingView_starSize, 24f))
                recycle()
            }
        }
    }

    fun setRating(rating: Float) {
        check(rating in 0..5)

        starViews.forEachIndexed { i, starView -> starView.setRatio(minOf(1f, maxOf(0f, rating - i))) }
    }

    fun setStarSize(size: Float) {
        starViews.forEach { it.layoutParams = it.layoutParams.apply {
            width = size.toInt()
            height = size.toInt()
        } }
    }
}