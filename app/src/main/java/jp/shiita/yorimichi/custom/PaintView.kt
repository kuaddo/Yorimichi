package jp.shiita.yorimichi.custom

import android.content.Context
import android.util.AttributeSet
import android.graphics.*
import android.view.MotionEvent
import android.view.View


class PaintView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    // ペンの種類
    enum class Pen (
            val alpha: Int = 255,
            val style: Paint.Style = Paint.Style.STROKE,
            val join: Paint.Join = Paint.Join.ROUND,
            val cap: Paint.Cap = Paint.Cap.ROUND,
            val xfermode: PorterDuffXfermode? = null,
            val isAntiAlias: Boolean = true
    ) {
        Normal(),
        Flat(join = Paint.Join.BEVEL, cap = Paint.Cap.SQUARE),
        ERASER(alpha = 0, xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR))
        ;
    }


    // 1回の描画距離の閾値
    private val touchTolerance: Float = 4f

    private val paint: Paint = Paint(Paint.DITHER_FLAG)
    private val path: Path = Path()

    private lateinit var mBitmap: Bitmap
    private lateinit var mCanvas: Canvas
    // 作業用インスタンス
    private lateinit var workBitmap: Bitmap
    private lateinit var workCanvas: Canvas

    // 曲線を滑らかにするための変数
    private var mX: Float = 0f
    private var mY: Float = 0f


    init {
        paint.apply {
            alpha = 255
            color = Color.rgb(0, 0, 0)
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            strokeWidth = 20f
            isAntiAlias = true
        }

        setPen(Pen.Flat)
    }


    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event != null) {
            val x = event.x
            val y = event.y

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    swipeAtBeginning(x, y)
                    invalidate()
                }

                MotionEvent.ACTION_MOVE -> {
                    swipeInMoving(x, y)
                    invalidate()
                }

                MotionEvent.ACTION_UP -> {
                    swipeAtEnd()
                    invalidate()
                }
            }
        }

        return true
    }

    // 実際に描画を行うメソッド
    // 作業用インスタンスを使うのは消しゴムの軌跡をきれいにするため
    override fun onDraw(canvas: Canvas?) {
        workCanvas.apply {
            drawBitmap(mBitmap, 0f, 0f, null)
            drawPath(path, paint)
        }
        canvas?.drawBitmap(workBitmap, 0f, 0f, null)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        mCanvas = Canvas(mBitmap)

        // 作業用インスタンス
        workBitmap = mBitmap.copy(Bitmap.Config.ARGB_8888, true)
        workCanvas = Canvas(workBitmap)
    }


    // このメソッドの挙動は試してない
    fun changePenWidth(width: Float) {
        paint.strokeWidth = width
    }


    // 色変更はこれで可能
    fun changePenColor(color: Int) {
        paint.apply {
            this.color = color
        }
    }


    // 実際に描画されているBitmapの取得
    fun getMainBitmap() : Bitmap = mBitmap


    // 描画された内容の削除
    // デバッグ用
    fun clear() {
        mBitmap = Bitmap.createBitmap(mBitmap.width, mBitmap.height, Bitmap.Config.ARGB_8888)
        workBitmap = mBitmap.copy(Bitmap.Config.ARGB_8888, true)
        mCanvas = Canvas(mBitmap)
        workCanvas = Canvas(workBitmap)
        path.reset()

        invalidate()
    }

    fun setPen(pen: Pen) {
        paint.apply {
            alpha = pen.alpha
            style = pen.style
            strokeJoin = pen.join
            strokeCap = pen.cap
            isAntiAlias = pen.isAntiAlias
            xfermode = pen.xfermode
        }
    }


    // スワイプ始めの挙動
    private fun swipeAtBeginning(x: Float, y: Float) {
        path.apply {
            reset()
            moveTo(x, y)
        }
        mX = x
        mY = y
    }

    // スワイプ中の挙動
    private fun swipeInMoving(x: Float, y: Float) {
        val dx: Float = Math.abs(x - mX)
        val dy: Float = Math.abs(y - mY)
        if (dx >= touchTolerance || dy >= touchTolerance) {
            path.quadTo(mX, mY, (x + mX)/2, (y + mY)/2)
            mX = x
            mY = y
        }
    }

    // スワイプ終わりの挙動
    private fun swipeAtEnd() {
        path.lineTo(mX, mY)
        mCanvas.drawPath(path, paint)
        path.reset()
    }
}