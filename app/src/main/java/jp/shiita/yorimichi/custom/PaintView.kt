package jp.shiita.yorimichi.custom

import android.animation.Animator
import android.animation.AnimatorSet
import android.content.Context
import android.util.AttributeSet
import android.graphics.*
import android.media.Image
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.widget.ImageView
import android.widget.Scroller
import jp.shiita.yorimichi.R
import kotlin.math.acos
import kotlin.math.sqrt


class PaintView(context: Context, attrs: AttributeSet? = null) : View(context, attrs){


    companion object {
        /*
            デバッグ時のTAG
        */
        const val TAG_ON_DOWN         = "onDown"
        const val TAG_ON_DRAW         = "onDraw"
        const val TAG_ON_SCROLL       = "onScroll"
        const val TAG_ON_FLING        = "onFling"
        const val TAG_ON_SIZE_CHANGED = "onSizeChanged"
        const val TAG_ROTATE_IMAGE    = "rotateImage"
        const val TAG_CLEAR           = "clear"
    }

    // ペンの種類
    enum class Pen(
            val alpha: Int = 255,
            val style: Paint.Style = Paint.Style.STROKE,
            val join: Paint.Join = Paint.Join.ROUND,
            val cap: Paint.Cap = Paint.Cap.ROUND,
            val xfermode: PorterDuffXfermode? = null,
            val isAntiAlias: Boolean = true
    ) {
        Normal(),
        Flat(join = Paint.Join.MITER, cap = Paint.Cap.SQUARE),
        ERASER(alpha = 0, xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR))
        ;
    }




    /*
        画面回転などでonSaveInstanceState()メソッドが呼び出されるときに
        BitmapやCanvas、今選択されているペン等を保存しておくためのクラス
     */
//    private class SavedState : BaseSavedState {
//
//        lateinit var noteBitmap: Bitmap
//        lateinit var wBitmap: Bitmap
//        lateinit var penBitmap: Bitmap
//        var penColor: Int = 0
//
//
//        constructor(source: Parcel) : super(source) {
//            val bitmapClassLoader = Bitmap::class.java.classLoader
//            source.apply {
//                noteBitmap = readParcelable(bitmapClassLoader) as Bitmap
//                wBitmap    = readParcelable(bitmapClassLoader) as Bitmap
//                penBitmap  = readParcelable(bitmapClassLoader) as Bitmap
//                penColor   = readInt()
//            }
//        }
//
//        constructor(superState: Parcelable) : super(superState)
//
//        override fun writeToParcel(out: Parcel?, flags: Int) {
//            super.writeToParcel(out, flags)
//            out?.apply {
//                writeParcelable(noteBitmap, 0)
//                writeParcelable(wBitmap, 0)
//                writeParcelable(penBitmap, 0)
//                writeInt(penColor)
//            }
//
//        }
//
//        companion object {
//            @JvmField
//            val CREATOR = object : Parcelable.Creator<SavedState> {
//                override fun createFromParcel(source: Parcel): SavedState {
//                    return SavedState(source)
//                }
//
//                override fun newArray(size: Int): Array<SavedState?> {
//                    return arrayOfNulls(size)
//                }
//            }
//        }
//    }


    // 1回の描画距離の閾値
    private val touchTolerance: Float = 4f

    private val outOfView: Float = Int.MIN_VALUE.toFloat()

    private val durationFrame: Int = 1000 / 60

    private val interpolationSteps = 1 .. 99

    /*
        スクロールの速度補正 by 公式リファレンス
     */
    private val scaleCorrection: Int = 4

    private val paint: Paint = Paint(Paint.DITHER_FLAG).apply {
        alpha = 255
        isAntiAlias = true
        colorFilter = PorterDuffColorFilter(Color.BLACK, PorterDuff.Mode.DST_OVER)
    }
    private val path: Path = Path()

    // メインの描画対象
    private lateinit var mBitmap: Bitmap
    private lateinit var mCanvas: Canvas
    // 作業用インスタンス
    private lateinit var workBitmap: Bitmap
    private lateinit var workCanvas: Canvas


    // 曲線を滑らかにするための変数
    private val currPenPoint: PointF = PointF(outOfView, outOfView)

    private val prevPenPoint: PointF = PointF(outOfView, outOfView)

    private var viewWidth:  Int = 0
    private var viewHeight: Int = 0

    private lateinit var actionTag: String

    /*
        2次ベジェ曲線による補間を格納
     */
    private var bezierPointList: ArrayList<PointF> = ArrayList()

    private val prevPivot: PointF = PointF(outOfView, outOfView)
    private val currPivot: PointF = PointF(outOfView, outOfView)

    private var penAlpha: Int = 255


    // ペン先の画像 --------------------------------------------------------------------------
    /*
        ペン先の画像
     */
    private lateinit var penImage: Bitmap

    private lateinit var penDrawingImage: Bitmap

    private lateinit var scaledPenImage: Bitmap

//    private val penDrawable: BitmapDrawable = ResourcesCompat.getDrawable(
//        resources, R.drawable.abc_ic_star_black_48dp, null
//    ) as BitmapDrawable


    // 描画の細かな動作に関するインスタンス ----------------------------------------------------
    /*
        タッチ中の動作のプロパティ取得のためのインスタンス
     */
    private val mScroller: Scroller = Scroller(context, null, true)

    /*
        スクロール速度の取得
     */
    private var mVelocityTracker: VelocityTracker? = null


    /*
        タッチ中の動作の詳細なリスナ
        タッチされた直後、スクロール中と、フリック中に限定
        つまり描画中の処理のみgestureDetectorで扱う
     */
    private val mGestureListener = object : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean {
            actionTag = TAG_ON_DOWN
            Log.d(TAG_ON_DOWN, "onDown was called")
            Log.d(TAG_ON_DOWN, createMotionEventLog(e, "e"))


            e.apply {
                currPenPoint.set(x, y)
            }

            mVelocityTracker?.clear()
            mVelocityTracker = mVelocityTracker ?: VelocityTracker.obtain()
            mVelocityTracker?.addMovement(e)

            return true
        }

        /*
            スクロール(スワイプ)時の処理
         */
        override fun onScroll(
                e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float
        ): Boolean {
            actionTag = TAG_ON_SCROLL
            Log.d(TAG_ON_SCROLL, "onScroll was called")

            Log.d(TAG_ON_SCROLL, createMotionEventLog(e1, "e1"))

            Log.d(TAG_ON_SCROLL, "mScroller#isFinished = ${mScroller.isFinished}")
            if (!mScroller.isFinished) {
                mScroller.abortAnimation()
            }


            prevPenPoint.apply {
                set(currPenPoint)
                Log.d(TAG_ON_SCROLL, "prevPenPoint = ($x, $y)")
            }


            Log.d(TAG_ON_SCROLL, createMotionEventLog(e2, "e2"))

            e2.apply {
                currPenPoint.set(x, y)
            }

            prevPivot.apply {
                set(currPivot)
            }

            // 速度取得
            mVelocityTracker?.apply {
                addMovement(e2)
                computeCurrentVelocity(durationFrame)
                Log.d(TAG_ON_SCROLL, "VelocityTracker's velocity = ($xVelocity, $yVelocity)")
                currPivot.apply {
                    x = xVelocity + prevPenPoint.x
                    y = yVelocity + prevPenPoint.y
                }
            }


            if (currPenPoint.let {
                        it.x != e1.x && it.y != e1.y
                    }
            ) {
                calculateBezierCurve()
            }

            invalidate()

            return true
        }

        /*
            フリック時の処理
         */
        override fun onFling(
                e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float
        ): Boolean {
            actionTag = TAG_ON_FLING
            Log.d(TAG_ON_FLING, "onFling was called")

            Log.d(TAG_ON_FLING, createMotionEventLog(e1, "e1"))
            Log.d(TAG_ON_FLING, createMotionEventLog(e2, "e2"))

            Log.d(TAG_ON_FLING, "Velocity = ($velocityX, $velocityY)")

            mScroller.apply {
                fling(
                        currX, currY,
                        (velocityX / scaleCorrection).toInt(),
                        (velocityY / scaleCorrection).toInt(),
                        0, viewWidth, 0, viewHeight
                )
            }



            rotateImage(prevPenPoint, currPenPoint)

            postInvalidate()

            return true
        }
    }

    /*
        スクロールとフリックを受け取る
     */
    private val gestureDetector = GestureDetector(context, mGestureListener)




    /*
        初期化処理
        Viewが作成されるときに呼び出される
     */
    init {



        setPenImage(R.drawable.abc_ic_star_black_48dp)


//        changePenWidth(0.5f)

//        penDrawable.colorFilter = PorterDuffColorFilter(Color.RED, PorterDuff.Mode.SRC_IN)

    }


    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event != null) {
            when (event.action) {
                MotionEvent.ACTION_UP -> {
                    mVelocityTracker?.recycle()
                    mVelocityTracker = null
                    bezierPointList = ArrayList()
                    return true
                }
                else -> {
                    return gestureDetector.onTouchEvent(event)
                }
            }
        }
        else {
            return true
        }
    }

    // 実際に描画を行うメソッド
    // 作業用インスタンスを使うのは消しゴムの軌跡をきれいにするため
    override fun onDraw(canvas: Canvas) {
        Log.d(TAG_ON_DRAW, "onDraw was called")

        Log.d(TAG_ON_DRAW, "called by $actionTag")

        val imageHalf: Float = (penDrawingImage.height / 2).toFloat()
//        val left: Int   = (currPenX - imageHalf).toInt()
//        val top: Int    = (currPenY - imageHalf).toInt()
//        val right: Int  = (left + imageSize).toInt()
//        val bottom: Int = (top + imageSize).toInt()

        Log.d(TAG_ON_DRAW, "imageHalf = $imageHalf")
        currPenPoint.apply {
            Log.d(TAG_ON_DRAW, "(currPenX, currPenY)  = ($x, $y)")
        }

        workCanvas.drawBitmap(mBitmap, 0f, 0f, null)
        if (bezierPointList.size != 0) {
            for (i in 0 until bezierPointList.size - 1) {
                rotateImage(bezierPointList[i], bezierPointList[i + 1])
                workCanvas.apply {

                    //            drawPath(path, paint)
//            penDrawable.setBounds(left, top , right, bottom)

                    drawBitmap(
                            penDrawingImage,
                            bezierPointList[i].x - imageHalf,
                            bezierPointList[i].y - imageHalf,
                            paint
                    )

//            penDrawable.draw(this)
                }
            }
        }


        canvas.drawBitmap(workBitmap, 0f, 0f, null)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        actionTag = TAG_ON_SIZE_CHANGED
        Log.d(TAG_ON_SIZE_CHANGED, "onSizeChanged was called")

        super.onSizeChanged(w, h, oldw, oldh)

        viewWidth  = w
        viewHeight = h

        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        mCanvas = Canvas(mBitmap)

        // 作業用インスタンス
        workBitmap = mBitmap.copy(Bitmap.Config.ARGB_8888, true)
        workCanvas = Canvas(workBitmap)
    }

//    override fun onSaveInstanceState(): Parcelable? {
//        val parent = super.onSaveInstanceState() ?: return super.onSaveInstanceState()
//        return SavedState(parent).apply {
//            noteBitmap = mBitmap
//            wBitmap    = workBitmap
//            penBitmap  = penImage
//            penColor   = paint.color
//        }
//    }
//
//    override fun onRestoreInstanceState(state: Parcelable?) {
//        if (state is SavedState) {
//            super.onRestoreInstanceState(state.superState)
//            apply {
//                mBitmap    = state.noteBitmap
//                workBitmap = state.wBitmap
//                penImage   = state.penBitmap
//                changePenColor(state.penColor)
//            }
//        }
//        else {
//            super.onRestoreInstanceState(state)
//        }
//    }



    // このメソッドの挙動は試してない
    fun changePenWidth(ratio: Float) {
        val penWidth  = penImage.width
        val penHeight = penImage.height

        val matrix = Matrix().apply {
            preScale(ratio, ratio)
        }

        scaledPenImage = Bitmap.createBitmap(penImage, 0, 0, penWidth, penHeight, matrix, true)
    }


    /*
        色の変更
     */
    fun changePenColor(color: Int) {
        paint.apply {
            colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
            this.color = color
        }
    }


    // 実際に描画されているBitmapの取得
    fun getMainBitmap() : Bitmap = workBitmap


    // 描画された内容の削除
    // デバッグ用
    fun clear() {
        actionTag = TAG_CLEAR
        mBitmap = Bitmap.createBitmap(mBitmap.width, mBitmap.height, Bitmap.Config.ARGB_8888)
        workBitmap = mBitmap.copy(Bitmap.Config.ARGB_8888, true)
        mCanvas = Canvas(mBitmap)
        workCanvas = Canvas(workBitmap)
        path.reset()

        currPenPoint.apply {
            set(outOfView, outOfView)
        }

        bezierPointList.clear()

        invalidate()
    }

    fun setPen(pen: Pen) {
        paint.apply {
            alpha = pen.alpha
            style = pen.style
            strokeJoin = pen.join
            strokeCap = pen.cap
            xfermode = pen.xfermode
            isAntiAlias = pen.isAntiAlias

        }
    }

    fun setPenImage(id: Int) {
        penImage = BitmapFactory.decodeResource(
                resources, id
        )

        changePenWidth(1f)
        penDrawingImage = penImage.copy(Bitmap.Config.ARGB_8888, true)

    }

    /*
        スクロールの向きに応じてペン画像を回転させる
        ペン画像そのものは変更しない
     */
    private fun rotateImage(prevPoint: PointF, currPoint: PointF) {
        Log.d(TAG_ROTATE_IMAGE, "rotateImage was called")

        val penWidth  = scaledPenImage.width
        val penHeight = scaledPenImage.height


        val dPoint = PointF(
                currPoint.x - prevPoint.x,
                currPoint.y - prevPoint.y
        ).apply {
            Log.d(TAG_ROTATE_IMAGE, "(dx, dy) = ($x, $y)")
        }

        val degree: Float = dPoint.let {
            Math.signum(it.y) * Math.toDegrees(-acos(it.x / it.length().toDouble())).toFloat()
        }

        Log.d(TAG_ROTATE_IMAGE, "calculated Degree = $degree")


        val matrix = Matrix().apply {
            setRotate(degree, penWidth * 0.5f, penHeight * 0.5f)
        }

        penDrawingImage = Bitmap.createBitmap(scaledPenImage, 0, 0, penWidth, penHeight, matrix, true)
    }


    /*
        2次ベジェ曲線の計算
     */
    private fun calculateBezierCurve() {
        bezierPointList = ArrayList()
        bezierPointList.add(prevPenPoint)
//        val pivot = calculateBezierPivot()
        for (i in interpolationSteps) {
            val t = i / 100.0f
            val term1 = (1 - t) * (1 - t)
            val term2 = 2 * t * (1 - t)
            val term3 = t * t
            bezierPointList.add(
                    PointF().apply {
                        x = term1 * prevPenPoint.x + term2 * currPivot.x + term3 * currPenPoint.x
                        y = term1 * prevPenPoint.y + term2 * currPivot.y + term3 * currPenPoint.y
                    }
            )
        }
        bezierPointList.add(currPenPoint)
    }

    /*
        ピボットの計算
     */
//    private fun calculateBezierPivot(): PointF {
//        return PointF().apply {
//            x = (prevPivot.x + currPivot.x) / 2
//            y = ((currPivot.y - prevPivot.y) / (currPivot.x - prevPivot.x)) * (x - currPivot.x) + currPivot.y
//        }
//    }


//    // スワイプ始めの挙動
//    private fun swipeAtBeginning(x: Float, y: Float) {
//        path.apply {
//            reset()
//            moveTo(x, y)
//        }
//
//        currPenPoint.set(x, y)
//    }
//
//    // スワイプ中の挙動
//    private fun swipeInMoving(x: Float, y: Float) {
////        mCanvas.drawBitmap(penImage, currPenX - imageSize/2, currPenY - imageSize/2, paint)
//        if (currPenPoint.let {
//                Math.abs(x - it.x) >= touchTolerance || Math.abs(y - it.y) >= touchTolerance
//            }
//        ) {
//            currPenPoint.apply {
//                path.quadTo(this.x, this.y, (x + this.x) / 2, (y + this.y) / 2)
//                set(x, y)
//            }
//        }
//    }
//
//    // スワイプ終わりの挙動
//    private fun swipeAtEnd() {
//        path.apply {
//            currPenPoint.apply {
//                lineTo(x, y)
//            }
//            reset()
//        }
//    }

    private fun createMotionEventLog(event: MotionEvent?, eventName: String): String {
        val e = event ?: return "$eventName is null"

        return """[$eventName info]
            historySize  = ${e.historySize}
            pointerCount = ${e.pointerCount}
            (x, y)       = (${e.x}, ${e.y})
        """.trimIndent()
    }
}