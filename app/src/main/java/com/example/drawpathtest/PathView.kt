package com.example.drawpathtest

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.support.annotation.ColorInt
import android.support.annotation.ColorRes
import android.support.annotation.IntDef
import android.util.AttributeSet
import android.util.Log
import android.view.View
import java.util.*
import java.util.concurrent.Semaphore

/**
 *  @author  xiaolanlaia
 *
 *  @create  2019/9/27 15:59
 *
 */


@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class PathView : View {


    companion object{
        const val MODE_AIRPLANE = 0
        const val MODE_TRAIN = 1
    }

    @IntDef(MODE_TRAIN, MODE_AIRPLANE)
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    private annotation class Mode


    private var mLightLineSemaphore: Semaphore? = null
    private var mDarkLineSemaphore:Semaphore? = null
    private var mKeyframes: Keyframes? = null
    private var mMode: Int = 0
    private var mLightPoints: FloatArray? = null
    private var mDarkPoints: FloatArray? = null
    private var mLightLineColor: Int = 0
    private var mDarkLineColor: Int = 0
    private var mProgressAnimator: ValueAnimator? = null
    private var mAlphaAnimator:ValueAnimator? = null
    private var mAnimationDuration: Long = 0
    private var mPaint : Paint? = null
    private var isRepeat: Boolean = false
    private var mAlpha: Int = 0
    private var mOnAnimationEndListener: OnAnimationEndListener? = null
    @Volatile
    private var isStopped: Boolean = false

    constructor(context: Context) : super(context){
        init()
    }

    constructor(context: Context,attr : AttributeSet) : super(context,attr)

    constructor(context: Context,attr : AttributeSet,defStyleAttr : Int) : super(context,attr,defStyleAttr)

    private fun init(){
        //初始化画笔
        mPaint = Paint()
        mPaint!!.style = Paint.Style.STROKE
        mPaint!!.isAntiAlias = true

        //默认动画时长
        mAnimationDuration = 1000L

        //默认颜色
        mLightLineColor = Color.parseColor("#F17F94")
        mDarkLineColor = Color.parseColor("#D8D5D7")

        mLightLineSemaphore = Semaphore(1)
        mDarkLineSemaphore = Semaphore(1)
    }

    /**
     * 设置条线动画模式
     *
     * @param mode {@link #MODE_AIRPLANE} 一开始不显示灰色线条，粉红色线条走过后才留下灰色线条,
     *             {@link #MODE_TRAIN} 一开始就显示灰色线条，并且一直显示，直到动画结束。
     */
    fun setMode(@Mode mode: Int) : PathView{

        if((mAlphaAnimator != null && mAlphaAnimator?.isRunning!!) || (mProgressAnimator != null && mProgressAnimator?.isRunning!!)){
            return this
        }

        mMode = mode
        return this

    }

    /**
     * 设置path
     */
    fun setPath(path : Path) : PathView{

        mKeyframes = Keyframes(path)
        mAlpha = 0
        return this
    }

    /**
     * 设置动画的时长
     */
    fun setDuration(duration : Long) : PathView{
        mAnimationDuration = duration
        return this
    }

    /**
     * 开始播放动画
     */

    @Synchronized fun start(){
        setDarkLineProgress(if (mMode == MODE_TRAIN) 1f else 0f, 0f)
        if (mAlphaAnimator != null && mAlphaAnimator?.isRunning!!){
            mAlphaAnimator?.cancel()
        }
        if (mProgressAnimator != null && mProgressAnimator?.isRunning!!) {
            mProgressAnimator?.cancel()
        }

        mAlphaAnimator = ValueAnimator.ofInt(0,255).setDuration((mAnimationDuration * .2F).toLong())
        mAlphaAnimator?.addUpdateListener { animation ->
            mAlpha = animation?.animatedValue as Int
            invalidate()
        }
        mAlphaAnimator?.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animator : Animator){
                startUpdateProgress()
            }
        })

        mAlphaAnimator?.start()

    }

    /**
     * 停止动画
     */

    @Synchronized fun stop(){

        isStopped = true
        mDarkPoints = null
        if(mDarkLineSemaphore != null){
            try {
                mDarkLineSemaphore?.acquire()
            }catch (e : InterruptedException){
                e.printStackTrace()
            }
            mDarkLineSemaphore?.release()
            mDarkLineSemaphore = null
        }

        mLightPoints = null
        if (mLightLineSemaphore != null) {
            try {
                mLightLineSemaphore?.acquire()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }

            mLightLineSemaphore?.release()
            mLightLineSemaphore = null
        }
        if (mAlphaAnimator != null && mAlphaAnimator!!.isRunning) {
            mAlphaAnimator?.cancel()
            mAlphaAnimator = null
        }
        if (mProgressAnimator != null && mProgressAnimator?.isRunning!!) {
            mProgressAnimator?.cancel()
            mProgressAnimator = null
        }
        if (mKeyframes != null) {
            mKeyframes?.release()
            mKeyframes = null
        }
        mOnAnimationEndListener = null
    }


    /**
     * 设置重复播放动画
     * @param isRepeat 是否重复
     */

    fun setRepeat(isRepeat : Boolean){
        this.isRepeat = isRepeat
    }

    /**
     *设置线宽
     * @param width 线条宽度 ( 单位 ：px )
     */

    fun setLineWidth(width : Float){
        mPaint?.strokeWidth = width
    }

    /**
     * 设置高亮线条颜色
     */
    fun setLightLineColor(@ColorInt color : Int){
        mLightLineColor = color
    }

    /**
     * 设置暗色线条颜色
     */
    fun setDarkLineColor(@ColorInt color : Int){
        mDarkLineColor = color
    }

    /**
     * 设置高亮线条颜色
     */
    fun setLightLineColorRes(@ColorRes color : Int){
        mLightLineColor = resources.getColor(color)
    }

    /**
     * 设置暗色线条颜色
     */
    fun setDarkLineColorRes(@ColorRes color: Int){
        mDarkLineColor = resources.getColor(color)
    }

    /**
     * 设置动画播放完毕监听器
     */
    fun setOnAnimationEndListener(listener : OnAnimationEndListener){

        mOnAnimationEndListener = listener
    }


    interface OnAnimationEndListener {
        fun onAnimationEnd()

    }

    fun startUpdateProgress(){
        mAlphaAnimator = null
        mProgressAnimator = ValueAnimator.ofFloat(-.6f,1f).setDuration(mAnimationDuration)
        mProgressAnimator?.addUpdateListener { animation ->
            val currentProgress = animation?.animatedValue as Float
            var lightLineStartProgress: Float
            //粉色线头
            var lightLineEndProgress: Float//粉色线尾
            var darkLineStartProgress: Float
            //灰色线头
            var darkLineEndProgress: Float//灰色线尾

            darkLineEndProgress = currentProgress

            //                粉色线头从0开始，并且初始速度是灰色线尾的2.5倍
            lightLineStartProgress = (.6f + currentProgress) * 2.5f
            darkLineStartProgress = lightLineStartProgress

            //                粉色线尾从-0.25开始，速度跟灰色线尾速度一样
            lightLineEndProgress = .35f + currentProgress

            //                粉色线尾走到30%时，速度变为原来速度的2.5倍
            if (lightLineEndProgress > .3f) {
                lightLineEndProgress = (.35f + currentProgress - .3f) * 2.5f + .3f
            }

            //                当粉色线头走到65%时，速度变为原来速度的0.35倍
            if (darkLineStartProgress > .65f) {
                lightLineStartProgress = ((.6f + currentProgress) * 2.5f - .65f) * .35f + .65f
                darkLineStartProgress = lightLineStartProgress
            }
            if (lightLineEndProgress < 0) {
                lightLineEndProgress = 0f
            }
            if (darkLineEndProgress < 0) {
                darkLineEndProgress = 0f
            }

            //                当粉色线尾走到90%时，播放透明渐变动画
            if (lightLineEndProgress > .9f) {
                if (mAlphaAnimator == null) {
                    mAlphaAnimator =
                        ValueAnimator.ofInt(255, 0).setDuration((mAnimationDuration * .3f).toLong())// 时长是总时长的30%
                    mAlphaAnimator?.addUpdateListener(ValueAnimator.AnimatorUpdateListener { animation ->
                        mAlpha = animation.animatedValue as Int
                    })
                    mAlphaAnimator?.start()
                }
            }
            if (lightLineStartProgress > 1) {
                lightLineStartProgress = 1f
                darkLineStartProgress = lightLineStartProgress
            }

            setLightLineProgress(lightLineStartProgress, lightLineEndProgress)

            //                飞机模式才更新灰色线条
            if (mMode == MODE_AIRPLANE) {
                setDarkLineProgress(darkLineStartProgress, darkLineEndProgress)
            }
            invalidate()
        }
        mProgressAnimator?.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                invalidate()
                if (mOnAnimationEndListener != null) {
                    mOnAnimationEndListener?.onAnimationEnd()
                }
                if (isRepeat) {
                    start()
                }
            }
        })
        mProgressAnimator?.start()
    }

    private fun setLightLineProgress(start: Float, end: Float) {
        updateLineProgress(start, end, true)
    }

    private fun setDarkLineProgress(start: Float, end: Float) {
        updateLineProgress(start, end, false)
    }

    private fun updateLineProgress(start: Float, end: Float, isLightPoints: Boolean) {
        if (isStopped) {
            return
        }
        if (mKeyframes == null) {
            throw IllegalStateException("path not set yet!")
        }
        if (isLightPoints) {
            try {
                mLightLineSemaphore?.acquire()
            } catch (e: Exception) {
                return
            }

            mLightPoints = mKeyframes?.getRangeValue(start, end)
            mLightLineSemaphore?.release()
        } else {
            try {
                mDarkLineSemaphore?.acquire()
            } catch (e: Exception) {
                return
            }

            mDarkPoints = mKeyframes?.getRangeValue(start, end)
            mDarkLineSemaphore?.release()
        }
    }

    public override fun onDraw(canvas: Canvas) {
        try {
            mDarkLineSemaphore?.acquire()
        } catch (e: Exception) {
            return
        }

        if (mDarkPoints != null) {
            mPaint?.color = mDarkLineColor
            mPaint?.alpha = mAlpha
            canvas.drawPoints(mDarkPoints, mPaint)


        }
        mDarkLineSemaphore?.release()
        try {
            mLightLineSemaphore?.acquire()
        } catch (e: Exception) {
            return
        }

        if (mLightPoints != null) {
            mPaint?.color = mLightLineColor
            mPaint?.alpha = mAlpha
            canvas.drawPoints(mLightPoints, mPaint)

        }
        mLightLineSemaphore?.release()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stop()
    }

    private class Keyframes  constructor(path: Path) {
        internal var numPoints: Int = 0
        internal var mData: FloatArray? = null

        companion object {

            internal const val PRECISION = 1f //精度我们用1就够了 (数值越少 numPoints 就越大)
        }

        init {
            init(path)
        }

         fun init(path: Path) {
            val pathMeasure = PathMeasure(path, false)

            val pathLength = pathMeasure.length
            numPoints = (pathLength / PRECISION).toInt() + 1
            mData = FloatArray(numPoints * 2)
            val position = FloatArray(2)
            var index = 0
            for (i in 0 until numPoints) {
                val distance = i * pathLength / (numPoints - 1)
                pathMeasure.getPosTan(distance, position, null)
                mData!![index] = position[0]
                mData!![index + 1] = position[1]
                index += 2
            }
            numPoints = mData!!.size
        }

        /**
         * 拿到start和end之间的x,y数据
         *
         * @param start 开始百分比
         * @param end   结束百分比
         * @return 裁剪后的数据
         */
        internal fun getRangeValue(start: Float, end: Float): FloatArray? {
            var startIndex = (numPoints * start).toInt()
            var endIndex = (numPoints * end).toInt()

            //必须是偶数，因为需要float[]{x,y}这样x和y要配对的
            if (startIndex % 2 != 0) {
                //直接减，不用担心 < 0  因为0是偶数，哈哈
                --startIndex
            }
            if (endIndex % 2 != 0) {
                //不用检查越界
                ++endIndex
            }
            //根据起止点裁剪
            return if (startIndex > endIndex) Arrays.copyOfRange(mData!!, endIndex, startIndex) else null
        }

        internal fun release() {
            mData = null
        }


    }

}