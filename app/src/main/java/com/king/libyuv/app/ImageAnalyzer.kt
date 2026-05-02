package com.king.libyuv.app

import android.annotation.SuppressLint
import android.graphics.ImageFormat
import androidx.camera.core.ImageProxy
import com.king.camera.scan.AnalyzeResult
import com.king.camera.scan.FrameMetadata
import com.king.camera.scan.analyze.Analyzer
import com.king.libyuv.FourCC
import com.king.libyuv.LibYuv

/**
 * @author <a href="mailto:jenly1314@gmail.com">Jenly</a>
 * <p>
 * <a href="https://github.com/jenly1314">Follow me</a>
 */
class ImageAnalyzer : Analyzer<Unit> {
    override fun analyze(
        imageProxy: ImageProxy,
        listener: Analyzer.OnAnalyzeListener<Unit>
    ) {

        @SuppressLint("UnsafeOptInUsageError")
        val image = imageProxy.image ?: return
        // 测试实时转换帧数据
        val i420Data = LibYuv.imageToI420(image)
        val width = imageProxy.width
        val height = imageProxy.height
        // 测试实时转换帧数据
        val dstData = LibYuv.convertFromI420(i420Data, width, height, FourCC.FOURCC_NV21)
        val frameMetadata = FrameMetadata(width, height, imageProxy.imageInfo.rotationDegrees)
        listener.onSuccess(AnalyzeResult(dstData, ImageFormat.NV21, frameMetadata, Unit))
    }


}
