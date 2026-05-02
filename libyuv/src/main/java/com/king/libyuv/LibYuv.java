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
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.media.Image;
import android.util.Log;

import java.nio.ByteBuffer;
import java.util.Objects;

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

    private static final String TAG = "LibYuv";

    static {
        System.loadLibrary("yuv");
    }

    private LibYuv() {
        throw new AssertionError();
    }

    /**
     * 将Image转换为I420
     *
     * @param image   图像；{@link Image}
     * @return 返回I420数据
     */
    @NonNull
    public static byte[] imageToI420(@NonNull Image image) {
        return imageToI420(image, RotationMode.ROTATE_0);
    }

    /**
     * 将Image转换为I420（显式指定源格式）
     * <p>
     * 适用场景：用于packed格式Image手动指定源格式解释方式。
     * 当前支持的常用对应关系如下：
     * <p>
     * {@link ImageFormat#YUV_420_888} -> 内部固定按YUV_420_888处理（忽略fourcc）
     * <p>
     * {@link ImageFormat#YUV_422_888} -> {@link FourCC#FOURCC_I422}
     * <p>
     * {@link ImageFormat#YUV_444_888} -> {@link FourCC#FOURCC_I444}
     * <p>
     * {@link ImageFormat#FLEX_RGBA_8888} / {@link PixelFormat#RGBA_8888} / {@link PixelFormat#RGBX_8888}
     * -> {@link FourCC#FOURCC_RGBA} / {@link FourCC#FOURCC_ARGB} / {@link FourCC#FOURCC_BGRA} /
     * {@link FourCC#FOURCC_ABGR} / {@link FourCC#FOURCC_AR30}
     * <p>
     * {@link ImageFormat#FLEX_RGB_888} / {@link PixelFormat#RGB_888}
     * -> {@link FourCC#FOURCC_RAW} / {@link FourCC#FOURCC_24BG}
     * <p>
     * {@link PixelFormat#RGB_565} -> {@link FourCC#FOURCC_RGBP}
     * <p>
     * {@link PixelFormat#RGBA_5551} -> {@link FourCC#FOURCC_RGBO}
     * <p>
     * {@link PixelFormat#RGBA_4444} -> {@link FourCC#FOURCC_R444}
     *
     * @param image  图像；{@link Image}
     * @param fourcc 指定源数据格式；{@link FourCC}
     * @return 返回I420数据
     */
    @NonNull
    public static byte[] imageToI420(@NonNull Image image, @NonNull FourCC fourcc) {
        return imageToI420(image, RotationMode.ROTATE_0, fourcc);
    }

    /**
     * 将Image转换为I420
     * <p>
     * 自动识别当前支持的Image格式如下：
     * <p>
     * {@link ImageFormat#YUV_420_888}
     * <p>
     * {@link ImageFormat#YUV_422_888}
     * <p>
     * {@link ImageFormat#YUV_444_888}
     * <p>
     * {@link ImageFormat#FLEX_RGBA_8888}
     * <p>
     * {@link PixelFormat#RGBA_8888}
     * <p>
     * {@link PixelFormat#RGBX_8888}
     * <p>
     * {@link ImageFormat#FLEX_RGB_888}
     * <p>
     * {@link PixelFormat#RGB_888}
     * <p>
     * {@link PixelFormat#RGB_565}
     * <p>
     * {@link PixelFormat#RGBA_5551}
     * <p>
     * {@link PixelFormat#RGBA_4444}
     *
     * @param image   图像；{@link Image}
     * @param degrees 需要旋转的角度
     * @return 返回I420数据
     */
    @NonNull
    public static byte[] imageToI420(@NonNull Image image, @RotationMode int degrees) {
        Objects.requireNonNull(image, "image is null");
        checkRotationMode(degrees);
        int format = image.getFormat();
        if (format == ImageFormat.YUV_420_888) {
            return imageYuv420888ToI420(image, degrees);
        }
        FourCC fourcc = requireImageFormatFourcc(format);
        return imageToI420(image, degrees, fourcc);
    }

    /**
     * 将Image转换为I420（显式指定源格式）
     * <p>
     * 若传入的是 {@link ImageFormat#YUV_420_888}，则始终走YUV_420_888专用路径。
     * <p>
     * 若传入的是其他支持格式，则要求image格式与fourcc匹配，例如：
     * <p>
     * {@link ImageFormat#YUV_422_888} -> {@link FourCC#FOURCC_I422}
     * <p>
     * {@link ImageFormat#YUV_444_888} -> {@link FourCC#FOURCC_I444}
     * <p>
     * {@link ImageFormat#FLEX_RGBA_8888} / {@link PixelFormat#RGBA_8888} / {@link PixelFormat#RGBX_8888}
     * -> 4字节packed FourCC
     * <p>
     * {@link ImageFormat#FLEX_RGB_888} / {@link PixelFormat#RGB_888}
     * -> 3字节packed FourCC
     * <p>
     * {@link PixelFormat#RGB_565} / {@link PixelFormat#RGBA_5551} / {@link PixelFormat#RGBA_4444}
     * -> 2字节packed FourCC
     *
     * @param image   图像；{@link Image}
     * @param degrees 需要旋转的角度
     * @param fourcc  指定源数据格式；{@link FourCC}
     * @return 返回I420数据
     */
    @NonNull
    public static byte[] imageToI420(@NonNull Image image, @RotationMode int degrees, @NonNull FourCC fourcc) {
        Objects.requireNonNull(image, "image is null");
        Objects.requireNonNull(fourcc, "fourcc is null");
        checkRotationMode(degrees);
        int format = image.getFormat();
        if (format == ImageFormat.YUV_420_888) {
            return imageYuv420888ToI420(image, degrees);
        }
        if (format == ImageFormat.YUV_422_888) {
            if (fourcc != FourCC.FOURCC_I422) {
                Log.w(TAG, "imageToI420: format=YUV_422_888 ignores fourcc=" + fourcc + ", fallback to FOURCC_I422");
            }
            return imagePlanarYuvToI420(image, degrees, FourCC.FOURCC_I422);
        }
        if (format == ImageFormat.YUV_444_888) {
            if (fourcc != FourCC.FOURCC_I444) {
                Log.w(TAG, "imageToI420: format=YUV_444_888 ignores fourcc=" + fourcc + ", fallback to FOURCC_I444");
            }
            return imagePlanarYuvToI420(image, degrees, FourCC.FOURCC_I444);
        }
        int bytesPerPixel = getPackedBytesPerPixel(fourcc);
        checkPackedImageFormat(format, bytesPerPixel);
        return imagePackedToI420(image, degrees, fourcc, bytesPerPixel);
    }

    @NonNull
    private static byte[] imageYuv420888ToI420(@NonNull Image image, @RotationMode int degrees) {
        checkYuvImagePlanes(image);
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

    @NonNull
    private static byte[] imagePackedToI420(@NonNull Image image, @RotationMode int degrees, @NonNull FourCC fourcc, int bytesPerPixel) {
        checkPackedImagePlane(image, bytesPerPixel);
        int width = image.getWidth();
        int height = image.getHeight();
        byte[] packedData = readPackedImageBytes(image.getPlanes()[0], width, height, bytesPerPixel);
        return convertToI420(packedData, width, height, degrees, fourcc);
    }

    @NonNull
    private static byte[] imagePlanarYuvToI420(@NonNull Image image, @RotationMode int degrees, @NonNull FourCC fourcc) {
        checkPlanarYuvImagePlanes(image);
        int width = image.getWidth();
        int height = image.getHeight();
        byte[] srcData = readPlanarYuvImageBytes(image, fourcc, width, height);
        return convertToI420(srcData, width, height, degrees, fourcc);
    }

    @NonNull
    private static byte[] readPlanarYuvImageBytes(@NonNull Image image, @NonNull FourCC fourcc, int width, int height) {
        Image.Plane[] planes = image.getPlanes();
        int chromaWidth;
        int chromaHeight;
        switch (fourcc) {
            case FOURCC_I422:
                chromaWidth = (width + 1) >> 1;
                chromaHeight = height;
                break;
            case FOURCC_I444:
                chromaWidth = width;
                chromaHeight = height;
                break;
            default:
                throw new IllegalArgumentException("unsupported planar yuv fourcc: " + fourcc);
        }

        int ySize = width * height;
        int chromaSize = chromaWidth * chromaHeight;
        byte[] dstData = new byte[fourcc.getTotalBppSize(width, height)];
        readPlaneBytes(planes[0], width, height, dstData, 0);
        readPlaneBytes(planes[1], chromaWidth, chromaHeight, dstData, ySize);
        readPlaneBytes(planes[2], chromaWidth, chromaHeight, dstData, ySize + chromaSize);
        return dstData;
    }

    private static void readPlaneBytes(@NonNull Image.Plane plane, int planeWidth, int planeHeight, @NonNull byte[] dstData, int dstOffset) {
        ByteBuffer buffer = plane.getBuffer();
        int rowStride = plane.getRowStride();
        int pixelStride = plane.getPixelStride();
        if (pixelStride == 1 && rowStride == planeWidth) {
            ByteBuffer duplicate = buffer.duplicate();
            duplicate.position(0);
            duplicate.get(dstData, dstOffset, planeWidth * planeHeight);
            return;
        }

        int requiredCapacity = rowStride * (planeHeight - 1) + planeWidth * pixelStride;
        checkBufferCapacity(buffer, requiredCapacity, "image plane buffer is too small");

        for (int y = 0; y < planeHeight; y++) {
            int rowStart = y * rowStride;
            int dstRowStart = dstOffset + y * planeWidth;
            for (int x = 0; x < planeWidth; x++) {
                dstData[dstRowStart + x] = buffer.get(rowStart + x * pixelStride);
            }
        }
    }

    @NonNull
    private static byte[] readPackedImageBytes(@NonNull Image.Plane plane, int width, int height, int bytesPerPixel) {
        ByteBuffer buffer = plane.getBuffer();
        int rowStride = plane.getRowStride();
        int pixelStride = plane.getPixelStride();
        int srcRowBytes = width * pixelStride;
        byte[] dstData = new byte[width * height * bytesPerPixel];
        if (pixelStride == bytesPerPixel && rowStride == width * bytesPerPixel) {
            ByteBuffer duplicate = buffer.duplicate();
            duplicate.position(0);
            duplicate.get(dstData);
            return dstData;
        }

        int requiredCapacity = rowStride * (height - 1) + srcRowBytes;
        checkBufferCapacity(buffer, requiredCapacity, "packed image buffer is too small");

        for (int y = 0; y < height; y++) {
            int rowStart = y * rowStride;
            int dstRowStart = y * width * bytesPerPixel;
            for (int x = 0; x < width; x++) {
                int srcStart = rowStart + x * pixelStride;
                int dstStart = dstRowStart + x * bytesPerPixel;
                for (int i = 0; i < bytesPerPixel; i++) {
                    dstData[dstStart + i] = buffer.get(srcStart + i);
                }
            }
        }
        return dstData;
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
        byte[] dstData = new byte[i420Size(width, height)];
        nv21ToI420(nv21Data, width, height, dstData);
        return dstData;
    }

    /**
     * NV21转I420（复用目标数组）
     */
    public static void nv21ToI420(@NonNull byte[] nv21Data, int width, int height, @NonNull byte[] dstData) {
        checkDimensions(width, height);
        checkArraySize(nv21Data, FourCC.FOURCC_NV21.getTotalBppSize(width, height), "nv21Data");
        checkArraySize(dstData, i420Size(width, height), "dstData");
        NV21ToI420(nv21Data, width, height, dstData);
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
        byte[] dstData = new byte[i420Size(width, height)];
        i420ToNv21(i420Data, width, height, dstData);
        return dstData;
    }

    /**
     * I420转NV21（复用目标数组）
     */
    public static void i420ToNv21(@NonNull byte[] i420Data, int width, int height, @NonNull byte[] dstData) {
        checkDimensions(width, height);
        checkArraySize(i420Data, i420Size(width, height), "i420Data");
        checkArraySize(dstData, FourCC.FOURCC_NV21.getTotalBppSize(width, height), "dstData");
        I420ToNV21(i420Data, width, height, dstData);
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
        checkDimensions(width, height);
        checkRotationMode(degrees);
        int dstWidth = rotatedWidth(width, height, degrees);
        int dstHeight = rotatedHeight(width, height, degrees);
        byte[] dstData = new byte[fourcc.getTotalBppSize(dstWidth, dstHeight)];
        rotate(srcData, width, height, degrees, fourcc, dstData);
        return dstData;
    }

    /**
     * 将指定格式的数据进行旋转（复用目标数组）
     */
    public static void rotate(@NonNull byte[] srcData, int width, int height, @RotationMode int degrees, @NonNull FourCC fourcc, @NonNull byte[] dstData) {
        checkDimensions(width, height);
        checkRotationMode(degrees);
        int dstWidth = rotatedWidth(width, height, degrees);
        int dstHeight = rotatedHeight(width, height, degrees);
        int requiredSize = fourcc.getTotalBppSize(dstWidth, dstHeight);
        checkArraySize(dstData, requiredSize, "dstData");
        if (fourcc == FourCC.FOURCC_I420) {
            i420Rotate(srcData, width, height, dstData, degrees);
            return;
        }
        byte[] i420Data = convertToI420(srcData, width, height, RotationMode.ROTATE_0, fourcc);
        byte[] rotatedI420Data = i420Rotate(i420Data, width, height, degrees);
        convertFromI420(rotatedI420Data, dstWidth, dstHeight, dstData, fourcc);
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
        byte[] dstData = new byte[dstSize];
        scale(srcData, width, height, dstWidth, dstHeight, dstSize, fourcc, filterMode, dstData);
        return dstData;
    }

    /**
     * 将指定格式的数据进行缩放（复用目标数组）
     */
    public static void scale(@NonNull byte[] srcData, int width, int height, int dstWidth, int dstHeight, @NonNull FourCC fourcc, @FilterMode int filterMode, @NonNull byte[] dstData) {
        int dstSize = fourcc.getTotalBppSize(dstWidth, dstHeight);
        scale(srcData, width, height, dstWidth, dstHeight, dstSize, fourcc, filterMode, dstData);
    }

    /**
     * 将指定格式的数据进行缩放（复用目标数组）
     */
    public static void scale(@NonNull byte[] srcData, int width, int height, int dstWidth, int dstHeight, int dstSize, @NonNull FourCC fourcc, @FilterMode int filterMode, @NonNull byte[] dstData) {
        checkDimensions(width, height);
        checkDimensions(dstWidth, dstHeight);
        checkDstSize(dstSize);
        checkArraySize(dstData, dstSize, "dstData");
        if (fourcc == FourCC.FOURCC_I420) {
            i420Scale(srcData, width, height, dstData, dstWidth, dstHeight, filterMode);
            return;
        }
        byte[] i420Data = convertToI420(srcData, width, height, fourcc);
        byte[] dstDataI420 = i420Scale(i420Data, width, height, dstWidth, dstHeight, filterMode);
        convertFromI420(dstDataI420, dstWidth, dstHeight, dstData, fourcc);
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
        byte[] dstData = new byte[dstSize];
        crop(srcData, width, height, cropX, cropY, cropWidth, cropHeight, dstSize, fourcc, dstData);
        return dstData;
    }

    /**
     * 将指定格式的数据进行裁减（复用目标数组）
     */
    public static void crop(@NonNull byte[] srcData, int width, int height, int cropX, int cropY, int cropWidth, int cropHeight, @NonNull FourCC fourcc, @NonNull byte[] dstData) {
        int dstSize = fourcc.getTotalBppSize(cropWidth, cropHeight);
        crop(srcData, width, height, cropX, cropY, cropWidth, cropHeight, dstSize, fourcc, dstData);
    }

    /**
     * 将指定格式的数据进行裁减（复用目标数组）
     */
    public static void crop(@NonNull byte[] srcData, int width, int height, int cropX, int cropY, int cropWidth, int cropHeight, int dstSize, @NonNull FourCC fourcc, @NonNull byte[] dstData) {
        checkDimensions(width, height);
        checkCropBounds(width, height, cropX, cropY, cropWidth, cropHeight);
        checkDstSize(dstSize);
        checkArraySize(dstData, dstSize, "dstData");
        if (fourcc == FourCC.FOURCC_I420) {
            i420Crop(srcData, width, height, cropX, cropY, cropWidth, cropHeight, dstData);
            return;
        }
        byte[] i420Data = convertToI420(srcData, width, height, cropX, cropY, cropWidth, cropHeight, RotationMode.ROTATE_0, fourcc);
        convertFromI420(i420Data, cropWidth, cropHeight, dstData, fourcc);
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
        checkDimensions(width, height);
        int dstSize = fourcc.getTotalBppSize(width, height);
        byte[] dstData = new byte[dstSize];
        mirror(srcData, width, height, fourcc, dstData);
        return dstData;
    }

    /**
     * 将指定格式的数据进行镜像翻转（复用目标数组）
     */
    public static void mirror(@NonNull byte[] srcData, int width, int height, @NonNull FourCC fourcc, @NonNull byte[] dstData) {
        checkDimensions(width, height);
        int requiredSize = fourcc.getTotalBppSize(width, height);
        checkArraySize(dstData, requiredSize, "dstData");
        if (fourcc == FourCC.FOURCC_I420) {
            i420Mirror(srcData, width, height, dstData);
            return;
        }
        byte[] i420Data = convertToI420(srcData, width, height, fourcc);
        byte[] mirrorI420Data = i420Mirror(i420Data, width, height);
        convertFromI420(mirrorI420Data, width, height, dstData, fourcc);
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
        checkDimensions(width, height);
        checkDstSize(dstSize);
        byte[] dstData = new byte[dstSize];
        convertFromI420(i420Data, width, height, dstData, fourcc);
        return dstData;
    }

    /**
     * 将I420数据转换为指定格式的数据（复用目标数组）
     */
    public static void convertFromI420(@NonNull byte[] i420Data, int width, int height, @NonNull byte[] dstData, @NonNull FourCC fourcc) {
        checkDimensions(width, height);
        checkArraySize(dstData, fourcc.getTotalBppSize(width, height), "dstData");
        ConvertFromI420(i420Data, width, height, dstData, 0, fourcc.getCode());
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
        checkDimensions(width, height);
        checkRotationMode(degrees);
        checkCropBounds(width, height, cropX, cropY, cropWidth, cropHeight);
        byte[] dstData = new byte[i420Size(cropWidth, cropHeight)];
        convertToI420(srcData, width, height, cropX, cropY, cropWidth, cropHeight, degrees, fourcc, dstData);
        return dstData;
    }

    /**
     * 将指定格式的数据转换为I420数据（复用目标数组）
     */
    public static void convertToI420(@NonNull byte[] srcData, int width, int height, @RotationMode int degrees, @NonNull FourCC fourcc, @NonNull byte[] dstData) {
        convertToI420(srcData, width, height, 0, 0, width, height, degrees, fourcc, dstData);
    }

    /**
     * 将指定格式的数据转换为I420数据（复用目标数组）
     */
    public static void convertToI420(@NonNull byte[] srcData, int width, int height, int cropX, int cropY, int cropWidth, int cropHeight, @RotationMode int degrees, @NonNull FourCC fourcc, @NonNull byte[] dstData) {
        checkDimensions(width, height);
        checkRotationMode(degrees);
        checkCropBounds(width, height, cropX, cropY, cropWidth, cropHeight);
        checkArraySize(dstData, i420Size(cropWidth, cropHeight), "dstData");
        ConvertToI420(srcData, srcData.length, width, height, dstData, cropX, cropY, cropWidth, cropHeight, degrees, fourcc.getCode());
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
        checkDimensions(width, height);
        checkRotationMode(degrees);
        int dstWidth = rotatedWidth(width, height, degrees);
        int dstHeight = rotatedHeight(width, height, degrees);
        byte[] dstData = new byte[i420Size(dstWidth, dstHeight)];
        i420Rotate(srcI420Data, width, height, dstData, degrees);
        return dstData;
    }

    /**
     * I420旋转（复用目标数组）
     */
    public static void i420Rotate(@NonNull byte[] srcI420Data, int width, int height, @NonNull byte[] dstData, @RotationMode int degrees) {
        checkDimensions(width, height);
        checkRotationMode(degrees);
        int dstWidth = rotatedWidth(width, height, degrees);
        int dstHeight = rotatedHeight(width, height, degrees);
        checkArraySize(dstData, i420Size(dstWidth, dstHeight), "dstData");
        I420Rotate(srcI420Data, width, height, dstData, degrees);
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
        checkDimensions(width, height);
        byte[] dstData = new byte[i420Size(width, height)];
        i420Mirror(srcI420Data, width, height, dstData);
        return dstData;
    }

    /**
     * I420镜像（复用目标数组）
     */
    public static void i420Mirror(@NonNull byte[] srcI420Data, int width, int height, @NonNull byte[] dstData) {
        checkDimensions(width, height);
        checkArraySize(dstData, i420Size(width, height), "dstData");
        I420Mirror(srcI420Data, width, height, dstData);
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
        byte[] dstData = new byte[i420Size(dstWidth, dstHeight)];
        i420Scale(srcI420Data, width, height, dstData, dstWidth, dstHeight, filterMode);
        return dstData;
    }

    /**
     * I420缩放（复用目标数组）
     */
    public static void i420Scale(@NonNull byte[] srcI420Data, int width, int height, @NonNull byte[] dstData, int dstWidth, int dstHeight, @FilterMode int filterMode) {
        checkDimensions(width, height);
        checkDimensions(dstWidth, dstHeight);
        checkArraySize(dstData, i420Size(dstWidth, dstHeight), "dstData");
        I420Scale(srcI420Data, width, height, dstData, dstWidth, dstHeight, filterMode);
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
        checkDimensions(width, height);
        checkCropBounds(width, height, cropX, cropY, cropWidth, cropHeight);
        byte[] dstData = new byte[i420Size(cropWidth, cropHeight)];
        i420Crop(srcI420Data, width, height, cropX, cropY, cropWidth, cropHeight, dstData);
        return dstData;
    }

    /**
     * I420裁减（复用目标数组）
     */
    public static void i420Crop(@NonNull byte[] srcI420Data, int width, int height, int cropX, int cropY, int cropWidth, int cropHeight, @NonNull byte[] dstData) {
        checkDimensions(width, height);
        checkCropBounds(width, height, cropX, cropY, cropWidth, cropHeight);
        checkArraySize(dstData, i420Size(cropWidth, cropHeight), "dstData");
        I420Crop(srcI420Data, width, height, dstData, cropX, cropY, cropWidth, cropHeight);
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
        byte[] dstData = new byte[i420Size(width, height)];
        yuvToI420(srcYData, srcUData, srcVData, yStride, uStride, vStride, uvPixelStride, width, height, dstData, degrees);
        return dstData;
    }

    /**
     * YUV转I420（复用目标数组）
     */
    public static void yuvToI420(@NonNull ByteBuffer srcYData, @NonNull ByteBuffer srcUData, @NonNull ByteBuffer srcVData,
                                 int yStride, int uStride, int vStride, int uvPixelStride,
                                 int width, int height, @NonNull byte[] dstData, @RotationMode int degrees) {
        checkDimensions(width, height);
        checkRotationMode(degrees);
        checkArraySize(dstData, i420Size(width, height), "dstData");
        if (yStride <= 0 || uStride <= 0 || vStride <= 0) {
            throw new IllegalArgumentException("rowStride must be positive");
        }
        if (uvPixelStride <= 0) {
            throw new IllegalArgumentException("uvPixelStride must be positive");
        }
        if (!srcYData.isDirect() || !srcUData.isDirect() || !srcVData.isDirect()) {
            throw new IllegalArgumentException("source buffers must be direct");
        }
        YUVToI420(srcYData, srcUData, srcVData, yStride, uStride, vStride, uvPixelStride, width, height, dstData, degrees);
    }

    private static int i420Size(int width, int height) {
        checkDimensions(width, height);
        return FourCC.FOURCC_I420.getTotalBppSize(width, height);
    }

    private static void checkDimensions(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("width and height must be positive");
        }
    }

    private static void checkRotationMode(int degrees) {
        if (degrees != RotationMode.ROTATE_0
                && degrees != RotationMode.ROTATE_90
                && degrees != RotationMode.ROTATE_180
                && degrees != RotationMode.ROTATE_270) {
            throw new IllegalArgumentException("invalid rotation mode: " + degrees);
        }
    }

    private static void checkDstSize(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("dstSize must be positive");
        }
    }

    private static void checkArraySize(@NonNull byte[] data, int minSize, @NonNull String name) {
        Objects.requireNonNull(data, name + " is null");
        if (minSize < 0) {
            throw new IllegalArgumentException("minSize must be non-negative");
        }
        if (data.length < minSize) {
            throw new IllegalArgumentException(name + " length(" + data.length + ") < required(" + minSize + ")");
        }
    }

    private static void checkCropBounds(int width, int height, int cropX, int cropY, int cropWidth, int cropHeight) {
        checkDimensions(width, height);
        checkDimensions(cropWidth, cropHeight);
        if (cropX < 0 || cropY < 0 || cropX + cropWidth > width || cropY + cropHeight > height) {
            throw new IllegalArgumentException("invalid crop bounds");
        }
    }

    private static void checkYuvImagePlanes(@NonNull Image image) {
        Image.Plane[] planes = image.getPlanes();
        if (planes == null || planes.length != 3) {
            throw new IllegalArgumentException("YUV_420_888 image must contain 3 planes");
        }
        for (Image.Plane plane : planes) {
            if (plane.getRowStride() <= 0) {
                throw new IllegalArgumentException("image plane rowStride must be positive");
            }
        }
        int uvPixelStride = planes[2].getPixelStride();
        if (uvPixelStride <= 0) {
            throw new IllegalArgumentException("image UV pixelStride must be positive");
        }
    }

    private static void checkPlanarYuvImagePlanes(@NonNull Image image) {
        Image.Plane[] planes = image.getPlanes();
        if (planes == null || planes.length != 3) {
            throw new IllegalArgumentException("planar YUV image must contain 3 planes");
        }
        for (Image.Plane plane : planes) {
            if (plane.getRowStride() <= 0 || plane.getPixelStride() <= 0) {
                throw new IllegalArgumentException("image plane stride must be positive");
            }
        }
    }

    private static void checkPackedImagePlane(@NonNull Image image, int bytesPerPixel) {
        Image.Plane[] planes = image.getPlanes();
        if (planes == null || planes.length < 1) {
            throw new IllegalArgumentException("packed image must contain at least 1 plane");
        }
        Image.Plane plane = planes[0];
        if (plane.getRowStride() <= 0 || plane.getPixelStride() <= 0) {
            throw new IllegalArgumentException("packed image stride must be positive");
        }
        if (plane.getPixelStride() < bytesPerPixel) {
            throw new IllegalArgumentException("packed image pixelStride is too small");
        }
    }

    private static void checkPackedImageFormat(int format, int bytesPerPixel) {
        switch (bytesPerPixel) {
            case 4:
                if (format != ImageFormat.FLEX_RGBA_8888
                        && format != PixelFormat.RGBA_8888
                        && format != PixelFormat.RGBX_8888) {
                    throw new IllegalArgumentException("image format does not match 4-byte packed data: " + format);
                }
                return;
            case 3:
                if (format != ImageFormat.FLEX_RGB_888 && format != PixelFormat.RGB_888) {
                    throw new IllegalArgumentException("image format does not match 3-byte packed data: " + format);
                }
                return;
            case 2:
                if (format != PixelFormat.RGB_565
                        && format != PixelFormat.RGBA_5551
                        && format != PixelFormat.RGBA_4444) {
                    throw new IllegalArgumentException("image format does not match 2-byte packed data: " + format);
                }
                return;
            default:
                throw new IllegalArgumentException("unsupported packed bytesPerPixel: " + bytesPerPixel);
        }
    }

    private static int getPackedBytesPerPixel(@NonNull FourCC fourcc) {
        switch (fourcc) {
            case FOURCC_ARGB:
            case FOURCC_BGRA:
            case FOURCC_ABGR:
            case FOURCC_RGBA:
            case FOURCC_AR30:
                return 4;
            case FOURCC_24BG:
            case FOURCC_RAW:
                return 3;
            case FOURCC_RGBP:
            case FOURCC_RGBO:
            case FOURCC_R444:
                return 2;
            default:
                throw new IllegalArgumentException("unsupported packed fourcc: " + fourcc);
        }
    }

    private static FourCC requireImageFormatFourcc(int format) {
        FourCC fourcc = getImageFormatFourcc(format);
        if (fourcc == null) {
            throw new IllegalArgumentException("unsupported image format: " + format);
        }
        return fourcc;
    }

    private static FourCC getImageFormatFourcc(int format) {
        switch (format) {
            case ImageFormat.YUV_422_888:
                return FourCC.FOURCC_I422;
            case ImageFormat.YUV_444_888:
                return FourCC.FOURCC_I444;
            case ImageFormat.FLEX_RGBA_8888:
            case PixelFormat.RGBA_8888:
            case PixelFormat.RGBX_8888:
                return FourCC.FOURCC_RGBA;
            case ImageFormat.FLEX_RGB_888:
            case PixelFormat.RGB_888:
                return FourCC.FOURCC_RAW;
            case PixelFormat.RGB_565:
                return FourCC.FOURCC_RGBP;
            case PixelFormat.RGBA_5551:
                return FourCC.FOURCC_RGBO;
            case PixelFormat.RGBA_4444:
                return FourCC.FOURCC_R444;
            default:
                return null;
        }
    }

    private static boolean isQuarterTurn(@RotationMode int degrees) {
        return degrees == RotationMode.ROTATE_90 || degrees == RotationMode.ROTATE_270;
    }

    private static int rotatedWidth(int width, int height, @RotationMode int degrees) {
        return isQuarterTurn(degrees) ? height : width;
    }

    private static int rotatedHeight(int width, int height, @RotationMode int degrees) {
        return isQuarterTurn(degrees) ? width : height;
    }

    private static void checkBufferCapacity(@NonNull ByteBuffer buffer, int requiredCapacity, @NonNull String message) {
        if (buffer.capacity() < requiredCapacity) {
            throw new IllegalArgumentException(message);
        }
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
     * @param dstData       目标I420数据
     * @param degrees       需要旋转的角度；{@link  RotationMode}
     */
    static native void YUVToI420(ByteBuffer srcYData, ByteBuffer srcUData, ByteBuffer srcVData, int yStride, int uStride, int vStride, int uvPixelStride, int width, int height, byte[] dstData, @RotationMode int degrees);

    /**
     * NV21转I420
     *
     * @param srcNv21Data 源NV21数据
     * @param width       图像宽度
     * @param height      图像高度
     * @param dstData 目标I420数据
     */
    static native void NV21ToI420(byte[] srcNv21Data, int width, int height, byte[] dstData);

    /**
     * I420转NV21
     *
     * @param srcI420Data 源I420数据
     * @param width       图像宽度
     * @param height      图像高度
     * @param dstData 目标NV21数据
     */
    static native void I420ToNV21(byte[] srcI420Data, int width, int height, byte[] dstData);

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
     * @param dstData  目标数据
     * @param cropX       裁减起始点X坐标
     * @param cropY       裁减起始点Y坐标
     * @param cropWidth   裁减的宽度
     * @param cropHeight  裁减的高度
     * @param degrees     需要旋转的角度；{@link  RotationMode}
     * @param fourcc      指定格式
     */
    static native void ConvertToI420(byte[] srcData, int srcSize, int width, int height, byte[] dstData, int cropX, int cropY, int cropWidth, int cropHeight, @RotationMode int degrees, long fourcc);

    /**
     * I420旋转
     *
     * @param srcI420Data 源I420数据
     * @param width       图像宽度
     * @param height      图像高度
     * @param dstData     目标I420数据
     * @param degrees     需要旋转的角度；{@link  RotationMode}
     */
    static native void I420Rotate(byte[] srcI420Data, int width, int height, byte[] dstData, @RotationMode int degrees);

    /**
     * I420缩放
     *
     * @param srcI420Data 源I420数据
     * @param width       图像宽度
     * @param height      图像高度
     * @param dstData     目标I420数据
     * @param dstWidth    目标宽
     * @param dstHeight   目标高
     * @param filterMode  压缩过滤模式；{@link  FilterMode}
     */
    static native void I420Scale(byte[] srcI420Data, int width, int height, byte[] dstData, int dstWidth, int dstHeight, @FilterMode int filterMode);

    /**
     * I420裁减
     *
     * @param srcI420Data 源I420数据
     * @param width       图像宽度
     * @param height      图像高度
     * @param dstData     目标I420数据
     * @param cropX       裁减起始点X坐标
     * @param cropY       裁减起始点Y坐标
     * @param cropWidth   裁减的宽度
     * @param cropHeight  裁减的高度
     */
    static native void I420Crop(byte[] srcI420Data, int width, int height, byte[] dstData, int cropX, int cropY, int cropWidth, int cropHeight);

    /**
     * I420镜像
     *
     * @param srcI420Data 源I420数据
     * @param width       图像宽度
     * @param height      图像高度
     * @param dstData     目标I420数据
     */
    static native void I420Mirror(byte[] srcI420Data, int width, int height, byte[] dstData);
}
