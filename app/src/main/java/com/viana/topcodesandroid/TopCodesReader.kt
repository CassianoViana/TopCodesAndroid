package com.viana.topcodesandroid

import android.graphics.Bitmap
import topcodes.Scanner
import topcodes.TopCode

class TopCodesReader {

    private var scanner: Scanner = Scanner()

    fun read(bitmap: Bitmap, listener: Listener) {
        val topCodes = scanner.scan(bitmap)
        listener.onRead(topCodes)
    }

    interface Listener {
        fun onRead(topCodes: MutableList<TopCode>)
    }

}
