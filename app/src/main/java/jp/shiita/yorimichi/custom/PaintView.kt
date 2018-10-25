package jp.shiita.yorimichi.custom

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class PaintView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    // 1回の描画距離の閾値
    private val TOUCH_TOLERANCE: Float = 4f

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


    constructor(context: Context): this(context, null)

    init {
        paint.apply {
            alpha = 255
            color = Color.rgb(0, 0, 0)
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            strokeWidth = 10f
            isAntiAlias = true
        }
    }


    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event != null) {
            val x = event.x
            val y = event.y

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    touch_start(x, y)
                    invalidate()
                }

                MotionEvent.ACTION_MOVE -> {
                    touch_move(x, y)
                    invalidate()
                }

                MotionEvent.ACTION_UP -> {
                    touch_up()
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

    // このメソッドの挙動は試してない
    fun changePenJoin(join: Paint.Join) {
        paint.strokeJoin = join
    }

    // このメソッドの挙動は試してない
    fun changePenCap(cap: Paint.Cap) {
        paint.strokeCap = cap
    }

    // 色変更はこれで可能
    // 消しゴム使用後はxfermodeプロパティはnullに戻さないといけない
    fun changePenColor(color: Int) {
        paint.apply {
            this.color = color
            xfermode = null

        }
    }

    // 消しゴムはpaintの設定を変える必要がある
    // colorじゃなくてalphaプロパティでも大丈夫なのかも
    fun setEraser() {
        paint.apply {
            color = Color.argb(0, 255, 255, 255)
            xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        }
    }

    // スワイプ始めの挙動
    private fun touch_start(x: Float, y: Float) {
        path.apply {
            reset()
            moveTo(x, y)
        }
        mX = x
        mY = y
    }

    // スワイプ中の挙動
    private fun touch_move(x: Float, y: Float) {
        val dx: Float = Math.abs(x - mX)
        val dy: Float = Math.abs(y - mY)
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            path.quadTo(mX, mY, (x + mX)/2, (y + mY)/2)
            mX = x
            mY = y
        }
    }

    // スワイプ終わりの挙動
    private fun touch_up() {
        path.lineTo(mX, mY)
        mCanvas.drawPath(path, paint)
        path.reset()
    }
}