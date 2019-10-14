package com.example.drawpathtest

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Switch
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var mContainer: ViewGroup? = null
    private var mPathViews: Array<PathView?>? = null
    private var mCanvasView: CanvasView? = null
    private var mLineWidth = 5f
    private var mDuration: Long = 1000
    private var mMode = PathView.MODE_AIRPLANE
    private var isRepeat: Boolean = false
    private var mToast: Toast? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT)
        mContainer = findViewById(R.id.container)
        mCanvasView = findViewById<CanvasView>(R.id.canvas)
        (findViewById<View>(R.id.mode) as Switch).setOnCheckedChangeListener { compoundButton, b ->
            mMode = if (b) PathView.MODE_TRAIN else PathView.MODE_AIRPLANE
        }
        (findViewById<View>(R.id.repeat) as Switch).setOnCheckedChangeListener { compoundButton, b -> isRepeat = b }
        val listener = object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                when (seekBar.id) {
                    R.id.duration -> mDuration = i.toLong()
                    R.id.line_width -> {
                        mLineWidth = i.toFloat()
                        mCanvasView?.setLineWidth(i)
                    }
                    else -> {
                    }
                }
                mToast?.setText(i.toString())
                mToast?.show()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        }
        (findViewById<View>(R.id.duration) as SeekBar).setOnSeekBarChangeListener(listener)
        (findViewById<View>(R.id.line_width) as SeekBar).setOnSeekBarChangeListener(listener)

    }

    fun draw(view: View) {
        mCanvasView!!.clear()
        mCanvasView!!.visibility = View.VISIBLE
        stopAnimations()
    }

    fun play(view: View) {
        stopAnimations()
        val paths = mCanvasView!!.getPaths()
        if (paths.isNotEmpty()) {
            mCanvasView!!.visibility = View.INVISIBLE
            mPathViews = ArrayList<PathView>(paths.size).toTypedArray()
            mPathViews = arrayOfNulls(paths.size)
            Log.d("__size","${paths.size}")

            for(i in paths.indices){
                Log.d("__mPathViews","${mPathViews!!.size}")
                val path = paths[i]
                val pathView = PathView(this)
                //设置线宽
                pathView.setLineWidth(mLineWidth)
                //动画时长
                pathView.setDuration(mDuration)
                //动画模式
                pathView.setMode(mMode)
                //设置路径
                pathView.setPath(path)
                //重复播放
                pathView.setRepeat(isRepeat)
                mPathViews!![i] = pathView
                mContainer?.addView(pathView)

            }

            for (pathView in mPathViews!!) {
                pathView!!.start()
            }

        }
    }

    private fun stopAnimations() {
        if (mPathViews != null) {
            for (tmp in mPathViews!!) {
                tmp?.stop()
                mContainer?.removeView(tmp)
            }
        }
    }
}
