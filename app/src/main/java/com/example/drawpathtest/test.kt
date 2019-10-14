package com.example.drawpathtest

import android.content.Context
import android.graphics.Canvas
import android.view.ViewGroup

/**
 *  @author  xiaolanlaia
 *
 *  @create  2019/9/28 16:41
 *
 */


class test : ViewGroup {
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
    }

    constructor(context: Context) : super(context)
}