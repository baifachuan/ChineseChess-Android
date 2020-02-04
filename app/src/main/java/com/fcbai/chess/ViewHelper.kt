package com.fcbai.chess

import android.graphics.*
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.LinearInterpolator

object ViewHelper {
    fun getAlphaAnimationForBlink(): AlphaAnimation {
        val animation = AlphaAnimation(1f, 0f)
        animation.duration = 500
        animation.interpolator = LinearInterpolator()
        animation.repeatCount = Animation.INFINITE
        animation.repeatMode = Animation.REVERSE
        return animation
    }

    fun combineImage(original: Bitmap, overlay: Bitmap, isRotate: Boolean = false): Bitmap {
        val result = Bitmap.createBitmap(
            original.width, original
                .height, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(result)
        val paint = Paint()
        paint.isAntiAlias = true
        canvas.drawBitmap(
            original,
            ((canvas.width - original.width)/2).toFloat(),
            (canvas.height - original.height)/2.toFloat(),
            paint)

        val m = Matrix()
        m.postScale(1F, -1F)

        canvas.drawBitmap(
            if(isRotate)  convert(overlay) else overlay,
            ((canvas.width - overlay.width)/2).toFloat(),
            (canvas.height - overlay.height)/2.toFloat(),
            paint)
        return result
    }


    private fun convert(a: Bitmap): Bitmap {
        val w = a.width
        val h = a.height
        val newb = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)// 创建一个新的和SRC长度宽度一样的位图
        val cv = Canvas(newb)
        val m = Matrix()
        m.postScale(1F, -1F)
        val new2 = Bitmap.createBitmap(a, 0, 0, w, h, m, true)
        cv.drawBitmap(new2, Rect(0, 0, new2.width, new2.height), Rect(0, 0, w, h), null)
        return newb
    }
}