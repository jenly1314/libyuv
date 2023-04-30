/*
 * Copyright (C) Jenly
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.king.libyuv.app

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.king.libyuv.FilterMode
import com.king.libyuv.FourCC
import com.king.libyuv.LibYuv
import com.king.libyuv.RotationMode
import com.king.libyuv.app.databinding.ActivityMainBinding
import com.king.libyuv.app.util.BitmapUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * @author <a href="mailto:jenly1314@gmail.com">Jenly</a>
 * <p>
 * <a href="https://github.com/jenly1314">Follow me</a>
 */
class MainActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private var degrees = RotationMode.ROTATE_0

    private lateinit var srcBitmap: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.ivSrc.setImageResource(R.drawable.ic_image)

        srcBitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_image)
    }

    /**
     * 旋转
     */
    private suspend fun rotate(bitmap: Bitmap, degrees: Int) = withContext(Dispatchers.IO) {
        val srcRgba = BitmapUtil.bitmapToRgba(bitmap)
        val width = bitmap.width
        val height = bitmap.height
        // RGBA顺序排列时，在使用libyuv时，是用ABGR来表示这个排列
        val dstData = LibYuv.rotate(srcRgba, width, height, degrees, FourCC.FOURCC_ABGR)
        var w = width
        var h = height
        if (degrees == RotationMode.ROTATE_90 || degrees == RotationMode.ROTATE_270) {
            w = height
            h = width
        }
        BitmapUtil.bitmapFromRgba(w, h, dstData)
    }

    /**
     * 旋转
     */
    private fun rotate() {
        lifecycleScope.launch {
            binding.tvText.text = "..."
            binding.ivDst.setImageResource(0)
            degrees = (degrees + RotationMode.ROTATE_90) % 360
            val bitmap = rotate(srcBitmap, degrees)
            binding.ivDst.setImageBitmap(bitmap)
            binding.tvText.text = "Rotate $degrees"
        }
    }

    /**
     * 图像转换成指定格式；然后还原进行显示
     */
    private suspend fun convert(bitmap: Bitmap, fourcc: FourCC) = withContext(Dispatchers.IO) {
            val srcRgba = BitmapUtil.bitmapToRgba(bitmap)
            var width = bitmap.width
            var height = bitmap.height
            // RGBA顺序排列时，在使用libyuv时，是用ABGR来表示这个排列
            val srcI420 = LibYuv.convertToI420(srcRgba, width, height, degrees, FourCC.FOURCC_ABGR)

            if (degrees == RotationMode.ROTATE_90 || degrees == RotationMode.ROTATE_270) {
                width = bitmap.height
                height = bitmap.width
            }
            val dstData = LibYuv.convertFromI420(srcI420, width, height, fourcc)
            val dstI420 = LibYuv.convertToI420(dstData, width, height, fourcc)

            val dstRgba = LibYuv.convertFromI420(dstI420, width, height, FourCC.FOURCC_ABGR)
            BitmapUtil.bitmapFromRgba(width, height, dstRgba)
        }

    /**
     * 转换成指定格式
     */
    private fun convert(fourcc: FourCC) {
        lifecycleScope.launch {
            binding.tvText.text = "..."
            binding.ivDst.setImageResource(0)
            val bitmap = convert(
                srcBitmap,
                fourcc
            )
            binding.ivDst.setImageBitmap(bitmap)
            binding.tvText.text = "Convert to $fourcc"
        }
    }

    /**
     * 裁减
     */
    private suspend fun crop(bitmap: Bitmap, left: Int, top: Int, cropWidth: Int, cropHeight: Int) =
        withContext(Dispatchers.IO) {
            val srcRgba = BitmapUtil.bitmapToRgba(bitmap)
            val width = bitmap.width
            val height = bitmap.height

            val dstRgba = LibYuv.crop(
                srcRgba,
                width,
                height,
                left,
                top,
                cropWidth,
                cropHeight,
                FourCC.FOURCC_ABGR
            )
            BitmapUtil.bitmapFromRgba(cropWidth, cropHeight, dstRgba)
        }

    /**
     * 裁减
     */
    private fun crop() {
        lifecycleScope.launch {
            binding.tvText.text = "..."
            binding.ivDst.setImageResource(0)
            val left = srcBitmap.width / 3
            val top = 0
            val cropWidth = srcBitmap.width / 3
            val cropHeight = (srcBitmap.height / 1.3f).toInt()
            val bitmap = crop(srcBitmap, left, top, cropWidth, cropHeight)
            binding.ivDst.setImageBitmap(bitmap)
            binding.tvText.text = "Crop"
        }
    }

    /**
     * 缩放
     */
    private suspend fun scale(bitmap: Bitmap, dstWidth: Int, dstHeight: Int) =
        withContext(Dispatchers.IO) {
            val srcRgba = BitmapUtil.bitmapToRgba(bitmap)
            val width = bitmap.width
            val height = bitmap.height
            val dstRgba = LibYuv.scale(
                srcRgba,
                width,
                height,
                dstWidth,
                dstHeight,
                FourCC.FOURCC_ABGR,
                FilterMode.FILTER_NONE
            )
            BitmapUtil.bitmapFromRgba(dstWidth, dstHeight, dstRgba)
        }

    /**
     * 缩放
     */
    private fun scale() {
        lifecycleScope.launch {
            binding.tvText.text = "..."
            binding.ivDst.setImageResource(0)
            val w = srcBitmap.width.times(0.6f).toInt()
            val h = srcBitmap.height.times(0.6f).toInt()
            val bitmap = scale(srcBitmap, w, h)
            binding.ivDst.setImageBitmap(bitmap)
            binding.tvText.text = "Scale"
        }
    }

    /**
     * 镜像
     */
    private suspend fun mirror(bitmap: Bitmap) = withContext(Dispatchers.IO) {
        val srcRgba = BitmapUtil.bitmapToRgba(bitmap)
        val width = bitmap.width
        val height = bitmap.height
        // RGBA顺序排列时，在使用libyuv时，是用ABGR来表示这个排列
        val dstRgba = LibYuv.mirror(srcRgba, width, height, FourCC.FOURCC_ABGR)
        BitmapUtil.bitmapFromRgba(width, height, dstRgba)
    }

    /**
     * 镜像
     */
    private fun mirror() {
        lifecycleScope.launch {
            binding.tvText.text = "..."
            binding.ivDst.setImageResource(0)
            val bitmap = mirror(srcBitmap)
            binding.ivDst.setImageBitmap(bitmap)
            binding.tvText.text = "Mirror"
        }
    }

    fun onClick(v: View) {
        when (v.id) {
            R.id.btnRotate -> rotate()
            R.id.btnNv21 -> convert(FourCC.FOURCC_NV21)
            R.id.btnARGB -> convert(FourCC.FOURCC_ARGB)
            R.id.btnCrop -> crop()
            R.id.btnScale -> scale()
            R.id.btnMirror -> mirror()
            R.id.btnPreview -> startActivity(Intent(this, ImageScanActivity::class.java))
        }
    }
}