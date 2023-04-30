package com.king.libyuv.app

import android.annotation.SuppressLint
import androidx.camera.core.ImageProxy
import com.king.libyuv.FourCC
import com.king.libyuv.LibYuv
import com.king.libyuv.app.util.BitmapUtil
import com.king.mlkit.vision.camera.AnalyzeResult
import com.king.mlkit.vision.camera.analyze.Analyzer

/**
 * @author <a href="mailto:jenly1314@gmail.com">Jenly</a>
 * <p>
 * <a href="https://github.com/jenly1314">Follow me</a>
 */
class ImageAnalyzer : Analyzer<Unit> {

    override fun analyze(
        imageProxy: ImageProxy,
        listener: Analyzer.OnAnalyzeListener<AnalyzeResult<Unit>>
    ) {
        val degrees = imageProxy.imageInfo.rotationDegrees
        @SuppressLint("UnsafeOptInUsageError")
        val i420Data = LibYuv.imageToI420(imageProxy.image, degrees)
        var width = imageProxy.width
        var height = imageProxy.height
        if(degrees == 90 || degrees == 270) {
            width = imageProxy.height
            height = imageProxy.width
        }
        val dstData = LibYuv.convertFromI420(i420Data, width, height, FourCC.FOURCC_ABGR)
        val bitmap = BitmapUtil.bitmapFromRgba(width, height, dstData)
        listener.onSuccess(AnalyzeResult(bitmap, Unit))
    }
}