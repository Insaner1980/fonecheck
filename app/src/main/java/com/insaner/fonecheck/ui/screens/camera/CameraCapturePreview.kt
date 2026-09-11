package com.insaner.fonecheck.ui.screens.camera

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

private const val MAX_PREVIEW_EDGE = 1600

/** Decodes only a screen-sized JPEG, then applies CameraX's target rotation. Never writes a file. */
internal fun decodeCapturePreview(
    bytes: ByteArray,
    rotationDegrees: Int,
): ImageBitmap {
    val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
    require(options.outWidth > 0 && options.outHeight > 0)
    options.inSampleSize = 1
    while (maxOf(options.outWidth, options.outHeight) / options.inSampleSize > MAX_PREVIEW_EDGE) {
        options.inSampleSize *= 2
    }
    options.inJustDecodeBounds = false
    val decoded = requireNotNull(BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options))
    if (rotationDegrees == 0) return decoded.asImageBitmap()
    val rotated =
        Bitmap.createBitmap(
            decoded,
            0,
            0,
            decoded.width,
            decoded.height,
            Matrix().apply { postRotate(rotationDegrees.toFloat()) },
            true,
        )
    if (rotated !== decoded) decoded.recycle()
    return rotated.asImageBitmap()
}
