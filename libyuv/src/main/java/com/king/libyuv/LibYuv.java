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
package com.king.libyuv;

import android.graphics.Rect;
import android.media.Image;

import java.nio.ByteBuffer;

import androidx.annotation.NonNull;

/**
 * LibYuv：基于Google的libyuv编译封装的YUV转换类工具库，主要用途是在各种YUV与RGB之间进行相互转换、裁减、旋转、缩放、镜像等。
 * <p>
 * 常用的方法如下：
 * <p>
 * 将I420数据转换为指定格式的数据：{@link #convertFromI420(byte[], int, int, FourCC)}
 * <p>
 * 将指定格式的数据转换为I420数据: {@link #convertToI420(byte[], int, int, FourCC)}
 * <p>
 * YUV转I420：{@link #yuvToI420(ByteBuffer, ByteBuffer, ByteBuffer, int, int, int, int, int, int, int)}
 * <p>
 * 将指定格式的数据进行旋转: {@link #rotate(byte[], int, int, int, FourCC)}
 * <p>
 * 将指定格式的数据进行缩放: {@link #scale(byte[], int, int, int, int, int, FourCC, int)}
 * <p>
 * 将指定格式的数据进行裁减: {@link #crop(byte[], int, int, Rect, FourCC)}
 * <p>
 * 将指定格式的数据进行镜像翻转: {@link #mirror(byte[], int, int, FourCC)}
 * <p>
 *
 * @author <a href="mailto:jenly1314@gmail.com">Jenly</a>
 * <p>
 * <a href="https://github.com/jenly1314">Follow me</a>
 */
@SuppressWarnings("unused")
public final class LibYuv {

    static {
        System.loadLibrary("libyuv");
    }

    private LibYuv() {
        throw new AssertionError();
    }

    /**
     * 将Image转换为I420
     *
     * @param image   图像；{@link Image}
     * @param degrees 需要旋转的角度
     * @return 返回I420数据
     */
    @NonNull
    public static byte[] imageToI420(Image image, int degrees) {
        Image.Plane[] planes = image.getPlanes();
        ByteBuffer yBuffer = planes[0].getBuffer();
        ByteBuffer uBuffer = planes[1].getBuffer();
        ByteBuffer vBuffer = planes[2].getBuffer();
        int yStride = planes[0].getRowStride();
        int uStride = planes[1].getRowStride();
        int vStride = planes[2].getRowStride();
        int pixelStride = planes[2].getPixelStride();
        int width = image.getWidth();
        int height = image.getHeight();
        return yuvToI420(yBuffer, uBuffer, vBuffer, yStride, uStride, vStride, pixelStride, width, height, degrees);
    }

    /**
     * NV21转I420
     *
     * @param nv21Data 源NV21数据
     * @param width    图像宽度
     * @param height   图像高度
     * @return 返回I420数据
     */
    @NonNull
    public static byte[] nv21ToI420(@NonNull byte[] nv21Data, int width, int height) {
        byte[] dstData = new byte[width * height * 3 >> 1];
        NV21ToI420(nv21Data, width, height, dstData);
        return dstData;
    }

    /**
     * I420转NV21
     *
     * @param i420Data 源I420数据
     * @param width    图像宽度
     * @param height   图像高度
     * @return 返回NV21数据
     */
    @NonNull
    public static byte[] i420ToNv21(@NonNull byte[] i420Data, int width, int height) {
        byte[] dstData = new byte[width * height * 3 >> 1];
        I420ToNV21(i420Data, width, height, dstData);
        return dstData;
    }

    /**
     * 将指定格式的数据进行旋转
     *
     * @param srcData 源数据
     * @param width   图像宽度
     * @param height  图像高度
     * @param degrees 需要旋转的角度；{@link  RotationMode}
     * @param fourcc  指定数据格式；{@link FourCC}
     * @return 返回旋转后的数据
     */
    @NonNull
    public static byte[] rotate(@NonNull byte[] srcData, int width, int height, @RotationMode int degrees, @NonNull FourCC fourcc) {
        if (fourcc == FourCC.FOURCC_I420) {
            return i420Rotate(srcData, width, height, degrees);
        }
        byte[] i420Data = convertToI420(srcData, width, height, degrees, fourcc);
        int w = width;
        int h = height;
        if (degrees == RotationMode.ROTATE_90 || degrees == RotationMode.ROTATE_270) {
            w = height;
            h = width;
        }
        return convertFromI420(i420Data, w, h, srcData.length, fourcc);
    }

    /**
     * 将指定格式的数据进行缩放
     *
     * @param srcData    源数据
     * @param width      图像宽度
     * @param height     图像高度
     * @param dstWidth   目标宽
     * @param dstHeight  目标高
     * @param fourcc     指定数据格式；{@link FourCC}
     * @param filterMode 压缩过滤模式；{@link  FilterMode}
     * @return 返回缩放后的数据
     */
    @NonNull
    public static byte[] scale(@NonNull byte[] srcData, int width, int height, int dstWidth, int dstHeight, @NonNull FourCC fourcc, @FilterMode int filterMode) {
        int dstSize = fourcc.getTotalBppSize(dstWidth, dstHeight);
        return scale(srcData, width, height, dstWidth, dstHeight, dstSize, fourcc, filterMode);
    }

    /**
     * 将指定格式的数据进行缩放
     *
     * @param srcData    源数据
     * @param width      图像宽度
     * @param height     图像高度
     * @param dstWidth   目标宽
     * @param dstHeight  目标高
     * @param dstSize    目标数据大小
     * @param fourcc     指定数据格式；{@link FourCC}
     * @param filterMode 压缩过滤模式；{@link  FilterMode}
     * @return 返回缩放后的数据
     */
    @NonNull
    public static byte[] scale(@NonNull byte[] srcData, int width, int height, int dstWidth, int dstHeight, int dstSize, @NonNull FourCC fourcc, @FilterMode int filterMode) {
        if (fourcc == FourCC.FOURCC_I420) {
            return i420Scale(srcData, width, height, dstWidth, dstHeight, filterMode);
        }
        byte[] i420Data = convertToI420(srcData, width, height, fourcc);
        byte[] dstI420Data = i420Scale(i420Data, width, height, dstWidth, dstHeight, filterMode);
        return convertFromI420(dstI420Data, dstWidth, dstHeight, dstSize, fourcc);
    }

    /**
     * 将指定格式的数据进行裁减
     *
     * @param srcData  源数据
     * @param width    图像宽度
     * @param height   图像高度
     * @param cropRect 裁减的矩形区域
     * @param fourcc   指定数据格式；{@link FourCC}
     * @return 返回裁减后的数据
     */
    @NonNull
    public static byte[] crop(@NonNull byte[] srcData, int width, int height, @NonNull Rect cropRect, @NonNull FourCC fourcc) {
        return crop(srcData, width, height, cropRect.left, cropRect.top, cropRect.width(), cropRect.height(), fourcc);
    }

    /**
     * 将指定格式的数据进行裁减
     *
     * @param srcData    源数据
     * @param width      图像宽度
     * @param height     图像高度
     * @param cropX      裁减起始点X坐标
     * @param cropY      裁减起始点Y坐标
     * @param cropWidth  裁减的宽度
     * @param cropHeight 裁减的高度
     * @param fourcc     指定数据格式；{@link FourCC}
     * @return 返回裁减后的数据
     */
    @NonNull
    public static byte[] crop(@NonNull byte[] srcData, int width, int height, int cropX, int cropY, int cropWidth, int cropHeight, @NonNull FourCC fourcc) {
        int dstSize = fourcc.getTotalBppSize(cropWidth, cropHeight);
        return crop(srcData, width, height, cropX, cropY, cropWidth, cropHeight, dstSize, fourcc);
    }

    /**
     * 将指定格式的数据进行裁减
     *
     * @param srcData    源数据
     * @param width      图像宽度
     * @param height     图像高度
     * @param cropX      裁减起始点X坐标
     * @param cropY      裁减起始点Y坐标
     * @param cropWidth  裁减的宽度
     * @param cropHeight 裁减的高度
     * @param dstSize    目标数据大小
     * @param fourcc     指定数据格式；{@link FourCC}
     * @return 返回裁减后的数据
     */
    @NonNull
    public static byte[] crop(@NonNull byte[] srcData, int width, int height, int cropX, int cropY, int cropWidth, int cropHeight, int dstSize, @NonNull FourCC fourcc) {
        if (fourcc == FourCC.FOURCC_I420) {
            return i420Crop(srcData, width, height, cropX, cropY, cropWidth, cropHeight);
        }
        byte[] i420Data = convertToI420(srcData, width, height, cropX, cropY, cropWidth, cropHeight, RotationMode.ROTATE_0, fourcc);
        return convertFromI420(i420Data, cropWidth, cropHeight, dstSize, fourcc);
    }

    /**
     * 将指定格式的数据进行镜像翻转
     *
     * @param srcData 源数据
     * @param width   图像宽度
     * @param height  图像高度
     * @param fourcc  指定数据格式；{@link FourCC}
     * @return 返回镜像翻转后的数据
     */
    @NonNull
    public static byte[] mirror(@NonNull byte[] srcData, int width, int height, @NonNull FourCC fourcc) {
        if (fourcc == FourCC.FOURCC_I420) {
            return i420Mirror(srcData, width, height);
        }
        byte[] i420Data = convertToI420(srcData, width, height, fourcc);
        byte[] dstI420Data = i420Mirror(i420Data, width, height);
        return convertFromI420(dstI420Data, width, height, srcData.length, fourcc);
    }

    /**
     * 将I420数据转换为指定格式的数据
     *
     * @param i420Data 源I420数据
     * @param width    图像宽度
     * @param height   图像高度
     * @param fourcc   指定数据格式；{@link FourCC}
     * @return 返回转换成指定格式后的数据
     */
    @NonNull
    public static byte[] convertFromI420(@NonNull byte[] i420Data, int width, int height, @NonNull FourCC fourcc) {
        int dstSize = fourcc.getTotalBppSize(width, height);
        return convertFromI420(i420Data, width, height, dstSize, fourcc);
    }

    /**
     * 将I420数据转换为指定格式的数据
     *
     * @param i420Data 源I420数据
     * @param width    图像宽度
     * @param height   图像高度
     * @param dstSize  目标数据占用字节大小
     * @param fourcc   指定数据格式；{@link FourCC}
     * @return 返回转换成指定格式后的数据
     */
    @NonNull
    public static byte[] convertFromI420(@NonNull byte[] i420Data, int width, int height, int dstSize, @NonNull FourCC fourcc) {
        byte[] dstData = new byte[dstSize];
        ConvertFromI420(i420Data, width, height, dstData, 0, fourcc.getCode());
        return dstData;
    }

    /**
     * 将指定格式的数据转换为I420数据
     *
     * @param srcData 源数据
     * @param width   图像宽度
     * @param height  图像高度
     * @param fourcc  指定数据格式；{@link FourCC}
     * @return 返回I420数据
     */
    @NonNull
    public static byte[] convertToI420(@NonNull byte[] srcData, int width, int height, @NonNull FourCC fourcc) {
        return convertToI420(srcData, width, height, RotationMode.ROTATE_0, fourcc);
    }

    /**
     * 将指定格式的数据转换为I420数据
     *
     * @param srcData 源数据
     * @param width   图像宽度
     * @param height  图像高度
     * @param degrees 需要旋转的角度；{@link  RotationMode}
     * @param fourcc  指定数据格式；{@link FourCC}
     * @return 返回I420数据
     */
    @NonNull
    public static byte[] convertToI420(@NonNull byte[] srcData, int width, int height, @RotationMode int degrees, @NonNull FourCC fourcc) {
        return convertToI420(srcData, width, height, 0, 0, width, height, degrees, fourcc);
    }

    /**
     * 将指定格式的数据转换为I420数据
     *
     * @param srcData  源数据
     * @param width    图像宽度
     * @param height   图像高度
     * @param cropRect 裁减的矩形
     * @param degrees  需要旋转的角度；{@link  RotationMode}
     * @param fourcc   指定数据格式；{@link FourCC}
     * @return 返回I420数据
     */
    @NonNull
    public static byte[] convertToI420(@NonNull byte[] srcData, int width, int height, @NonNull Rect cropRect, @RotationMode int degrees, @NonNull FourCC fourcc) {
        return convertToI420(srcData, width, height, cropRect.left, cropRect.top, cropRect.width(), cropRect.height(), degrees, fourcc);
    }

    /**
     * 将指定格式的数据转换为I420数据
     *
     * @param srcData    源数据
     * @param width      图像宽度
     * @param height     图像高度
     * @param cropX      裁减起始点X坐标
     * @param cropY      裁减起始点Y坐标
     * @param cropWidth  裁减的宽度
     * @param cropHeight 裁减的高度
     * @param degrees    需要旋转的角度；{@link  RotationMode}
     * @param fourcc     指定数据格式；{@link FourCC}
     * @return 返回I420数据
     */
    @NonNull
    public static byte[] convertToI420(@NonNull byte[] srcData, int width, int height, int cropX, int cropY, int cropWidth, int cropHeight, @RotationMode int degrees, @NonNull FourCC fourcc) {
        byte[] dstI420Data = new byte[cropWidth * cropHeight * 3 >> 1];
        ConvertToI420(srcData, srcData.length, width, height, dstI420Data, cropX, cropY, cropWidth, cropHeight, degrees, fourcc.getCode());
        return dstI420Data;
    }

    /**
     * I420旋转
     *
     * @param srcI420Data 源I420数据
     * @param width       图像宽度
     * @param height      图像高度
     * @param degrees     需要旋转的角度；{@link  RotationMode}
     * @return 返回旋转后的数据
     */
    @NonNull
    public static byte[] i420Rotate(@NonNull byte[] srcI420Data, int width, int height, @RotationMode int degrees) {
        byte[] dstData = new byte[srcI420Data.length];
        I420Rotate(srcI420Data, width, height, dstData, degrees);
        return dstData;
    }

    /**
     * I420镜像
     *
     * @param srcI420Data 源I420数据
     * @param width       图像宽度
     * @param height      图像高度
     * @return 返回镜像后的数据
     */
    @NonNull
    public static byte[] i420Mirror(@NonNull byte[] srcI420Data, int width, int height) {
        byte[] dstData = new byte[srcI420Data.length];
        I420Mirror(srcI420Data, width, height, dstData);
        return dstData;
    }

    /**
     * I420缩放
     *
     * @param srcI420Data 源I420数据
     * @param width       图像宽度
     * @param height      图像高度
     * @param dstWidth    目标宽
     * @param dstHeight   目标高
     * @param filterMode  压缩过滤模式；{@link  FilterMode}
     * @return 返回缩放后的数据
     */
    @NonNull
    public static byte[] i420Scale(@NonNull byte[] srcI420Data, int width, int height, int dstWidth, int dstHeight, @FilterMode int filterMode) {
        byte[] dstData = new byte[dstWidth * dstHeight * 3 >> 1];
        I420Scale(srcI420Data, width, height, dstData, dstWidth, dstHeight, filterMode);
        return dstData;
    }

    /**
     * I420裁减
     *
     * @param srcI420Data 源I420数据
     * @param width       图像宽度
     * @param height      图像高度
     * @param cropX       裁减起始点X坐标
     * @param cropY       裁减起始点Y坐标
     * @param cropWidth   裁减的宽度
     * @param cropHeight  裁减的高度
     * @return 返回裁减后的数据
     */
    @NonNull
    public static byte[] i420Crop(@NonNull byte[] srcI420Data, int width, int height, int cropX, int cropY, int cropWidth, int cropHeight) {
        byte[] dstData = new byte[cropWidth * cropHeight * 3 >> 1];
        I420Crop(srcI420Data, width, height, dstData, cropX, cropY, cropWidth, cropHeight);
        return dstData;
    }

    /**
     * YUV转I420
     *
     * @param srcYData      源Y数据
     * @param srcUData      源U数据
     * @param srcVData      源V数据
     * @param yStride       源Y跨距
     * @param uStride       源U跨距
     * @param vStride       源V跨距
     * @param uvPixelStride UV像素跨距
     * @param width         图像宽度
     * @param height        图像高度
     * @param degrees       需要旋转的角度；{@link  RotationMode}
     * @return 返回I420数据
     */
    @NonNull
    public static byte[] yuvToI420(ByteBuffer srcYData, ByteBuffer srcUData, ByteBuffer srcVData, int yStride, int uStride, int vStride, int uvPixelStride, int width, int height, @RotationMode int degrees) {
        byte[] dstI420Data = new byte[width * height * 3 >> 1];
        YUVToI420(srcYData, srcUData, srcVData, yStride, uStride, vStride, uvPixelStride, width, height, dstI420Data, degrees);
        return dstI420Data;
    }

    /**
     * YUV转I420
     *
     * @param srcYData      源Y数据
     * @param srcUData      源U数据
     * @param srcVData      源V数据
     * @param yStride       源Y跨距
     * @param uStride       源U跨距
     * @param vStride       源V跨距
     * @param uvPixelStride UV像素跨距
     * @param width         图像宽度
     * @param height        图像高度
     * @param dstI420Data   目标I420数据
     * @param degrees       需要旋转的角度；{@link  RotationMode}
     */
    static native void YUVToI420(ByteBuffer srcYData, ByteBuffer srcUData, ByteBuffer srcVData, int yStride, int uStride, int vStride, int uvPixelStride, int width, int height, byte[] dstI420Data, @RotationMode int degrees);

    /**
     * NV21转I420
     *
     * @param srcNv21Data 源NV21数据
     * @param width       图像宽度
     * @param height      图像高度sca
     * @param dstI420Data 目标I420数据
     */
    static native void NV21ToI420(byte[] srcNv21Data, int width, int height, byte[] dstI420Data);

    /**
     * I420转NV21
     *
     * @param srcI420Data 源I420数据
     * @param width       图像宽度
     * @param height      图像高度
     * @param dstNv21Data 目标NV21数据
     */
    static native void I420ToNV21(byte[] srcI420Data, int width, int height, byte[] dstNv21Data);

    /**
     * 将I420数据转换为指定格式的数据
     *
     * @param srcI420Data 源I420数据
     * @param width       图像宽度
     * @param height      图像高度
     * @param dstData     目标数据
     * @param dstStride   目标跨距
     * @param fourcc      指定格式
     */
    static native void ConvertFromI420(byte[] srcI420Data, int width, int height, byte[] dstData, int dstStride, long fourcc);

    /**
     * 将指定格式的数据转换为I420数据
     *
     * @param srcData     源数据
     * @param srcSize     源数据大小
     * @param width       图像宽度
     * @param height      图像高度
     * @param dstI420Data 目标数据
     * @param cropX       裁减起始点X坐标
     * @param cropY       裁减起始点Y坐标
     * @param cropWidth   裁减的宽度
     * @param cropHeight  裁减的高度
     * @param degrees     需要旋转的角度；{@link  RotationMode}
     * @param fourcc      指定格式
     */
    static native void ConvertToI420(byte[] srcData, int srcSize, int width, int height, byte[] dstI420Data, int cropX, int cropY, int cropWidth, int cropHeight, @RotationMode int degrees, long fourcc);

    /**
     * I420旋转
     *
     * @param srcI420Data 源I420数据
     * @param width       图像宽度
     * @param height      图像高度
     * @param dstI420Data 目标I420数据
     * @param degrees     需要旋转的角度；{@link  RotationMode}
     */
    static native void I420Rotate(byte[] srcI420Data, int width, int height, byte[] dstI420Data, @RotationMode int degrees);

    /**
     * I420缩放
     *
     * @param srcI420Data 源I420数据
     * @param width       图像宽度
     * @param height      图像高度
     * @param dstI420Data 目标I420数据
     * @param dstWidth    目标宽
     * @param dstHeight   目标高
     * @param filterMode  压缩过滤模式；{@link  FilterMode}
     */
    static native void I420Scale(byte[] srcI420Data, int width, int height, byte[] dstI420Data, int dstWidth, int dstHeight, @FilterMode int filterMode);

    /**
     * I420裁减
     *
     * @param srcI420Data 源I420数据
     * @param width       图像宽度
     * @param height      图像高度
     * @param dstI420Data 目标I420数据
     * @param cropX       裁减起始点X坐标
     * @param cropY       裁减起始点Y坐标
     * @param cropWidth   裁减的宽度
     * @param cropHeight  裁减的高度
     */
    static native void I420Crop(byte[] srcI420Data, int width, int height, byte[] dstI420Data, int cropX, int cropY, int cropWidth, int cropHeight);

    /**
     * I420镜像
     *
     * @param srcI420Data 源I420数据
     * @param width       图像宽度
     * @param height      图像高度
     * @param dstI420Data 目标I420数据
     */
    static native void I420Mirror(byte[] srcI420Data, int width, int height, byte[] dstI420Data);
}