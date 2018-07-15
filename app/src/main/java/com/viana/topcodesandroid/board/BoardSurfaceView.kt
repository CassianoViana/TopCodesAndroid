package com.viana.topcodesandroid.board

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import topcodes.TopCode

class BoardSurfaceView
@JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyle: Int = 0
) : SurfaceView(context, attrs, defStyle),
        SurfaceHolder.Callback {

    private lateinit var topCodes: List<TopCode>

    init {
        setBackgroundColor(Color.TRANSPARENT)
    }

    fun updateTopCodes(topCodes: List<TopCode>) {
        this.topCodes = topCodes
        var lockCanvas: Canvas? = null
        try {
            lockCanvas = holder.lockCanvas()
            clear(lockCanvas)
            super.draw(lockCanvas)
            drawTopCodes(lockCanvas)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            holder.unlockCanvasAndPost(lockCanvas)
        }
    }

    override fun surfaceChanged(p0: SurfaceHolder?, p1: Int, p2: Int, p3: Int) {
    }

    override fun surfaceCreated(surfaceHolder: SurfaceHolder) {
    }

    override fun surfaceDestroyed(p0: SurfaceHolder?) {
    }

    private fun drawTopCodes(canvas: Canvas) {
        topCodes.forEach {
            it.draw(canvas)
        }
    }

    private fun clear(canvas: Canvas) {
        canvas.drawColor(0, PorterDuff.Mode.CLEAR)
    }
}