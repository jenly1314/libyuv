package com.king.libyuv.app

import android.widget.ImageView
import com.king.mlkit.vision.camera.AnalyzeResult
import com.king.mlkit.vision.camera.BaseCameraScanActivity
import com.king.mlkit.vision.camera.analyze.Analyzer
import com.king.mlkit.vision.camera.config.ResolutionCameraConfig

/**
 * @author <a href="mailto:jenly1314@gmail.com">Jenly</a>
 * <p>
 * <a href="https://github.com/jenly1314">Follow me</a>
 */
class ImageScanActivity : BaseCameraScanActivity<Unit>() {

    lateinit var ivImage: ImageView

    override fun getLayoutId(): Int {
        return R.layout.activity_image_scan
    }

    override fun initUI() {
        super.initUI()
        ivImage = findViewById(R.id.ivImage)
    }

    override fun onScanResultCallback(result: AnalyzeResult<Unit>) {
        result.bitmap?.let {
            ivImage.setImageBitmap(it)
        }
    }

    override fun createAnalyzer(): Analyzer<Unit>? {
        return ImageAnalyzer()
    }
}