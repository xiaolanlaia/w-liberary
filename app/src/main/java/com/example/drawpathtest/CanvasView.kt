package com.example.drawpathtest

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import java.util.ArrayList

/**
 *  @author  xiaolanlaia
 *
 *  @create  2019/9/25 16:58
 *
 */


class CanvasView : View {


    var mPath = Path()
    var mPaint = Paint()
    var mBitmap : Bitmap? = null
    var mCanvas = Canvas()

    init{
        mPaint.isAntiAlias = true
//        mPaint.color = resources.getColor(R.color.colorAccent)
        mPaint.color = Color.RED
        mPaint.strokeCap = Paint.Cap.ROUND
        mPaint.style = Paint.Style.STROKE
        mPaint.strokeWidth = 7f
    }

    constructor(context: Context) : super(context)

    constructor(context: Context,attr : AttributeSet) : super(context,attr)

    constructor(context: Context,attr : AttributeSet, defStyleAttr : Int) : super(context,attr,defStyleAttr)

    override fun onDraw(canvas: Canvas) {

        canvas.drawBitmap(mBitmap!!,0f,0f,null)


    }
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_4444)
        mCanvas = Canvas(mBitmap!!)
    }



    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null){
            return false
        }
        when(event.action){

            MotionEvent.ACTION_DOWN ->{
                mPath.moveTo(event.x,event.y)

            }



            MotionEvent.ACTION_MOVE ->{
                mPath.lineTo(event.x,event.y)

            }
            MotionEvent.ACTION_UP ->{
//                mPath.lineTo(event.x,event.y)

            }
        }

        mCanvas.drawPath(mPath,mPaint)
        invalidate()
        return true
    }

    fun getPaths() : Array<Path>{

        var paths = ArrayList<Path>()
        var pathMeasure = PathMeasure()

        pathMeasure.setPath(mPath,false)

        var path : Path

        do {
            path = Path()
            path.rLineTo(0f,0f)
            pathMeasure.getSegment(0f,pathMeasure.length,path,true)
            if (!path.isEmpty){
                paths.add(path)
            }
        }while (pathMeasure.nextContour())

        return paths.toTypedArray()

    }

    fun setPath(path : Path){
        mPath = path
        mBitmap = Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_4444)
        mCanvas = Canvas(mBitmap)
        mCanvas.drawPath(mPath,mPaint)
        invalidate()
    }

    fun clear(){
        setPath(Path())
    }

    fun setLineWidth(width : Int){
        mPaint.strokeWidth = width.toFloat()
        invalidate()
    }

}