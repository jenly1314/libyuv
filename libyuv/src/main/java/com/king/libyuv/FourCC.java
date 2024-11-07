/*
 * Copyright (C) Jenly
 *
 * Licensed under the Apache License, Version 2.0 (the "License")),
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


import androidx.annotation.NonNull;

/**
 * Four-Character Codes
 *
 * @author <a href="mailto:jenly1314@gmail.com">Jenly</a>
 * <p>
 * <a href="https://github.com/jenly1314">Follow me</a>
 */
public enum FourCC {
    /**
     * YUY2
     */
    FOURCC_YUY2("YUY2"),
    /**
     * UYVY
     */
    FOURCC_UYVY("UYVY"),
    /**
     * rgb565 LE.
     */
    FOURCC_RGBP("RGBP"),
    /**
     * argb1555 LE.
     */
    FOURCC_RGBO("RGBO"),
    /**
     * argb4444 LE.
     */
    FOURCC_R444("R444"),
    /**
     * RGB
     */
    FOURCC_24BG("24BG"),
    /**
     * RAW
     */
    FOURCC_RAW("RAW "),
    /**
     * ARGB
     */
    FOURCC_ARGB("ARGB"),
    /**
     * BGRA
     */
    FOURCC_BGRA("BGRA"),
    /**
     * ABGR
     */
    FOURCC_ABGR("ABGR"),
    /**
     * RGBA
     */
    FOURCC_RGBA("RGBA"),
    /**
     * ABGR version of 10 bit
     */
    FOURCC_AR30("AR30"),
    /**
     * I400
     */
    FOURCC_I400("I400"),
    /**
     * NV12
     */
    FOURCC_NV12("NV12"),
    /**
     * NV21
     */
    FOURCC_NV21("NV21"),
    /**
     * I420
     */
    FOURCC_I420("I420"),
    /**
     * YV12
     */
    FOURCC_YV12("YV12"),
    /**
     * I422
     */
    FOURCC_I422("I422"),
    /**
     * YV16
     */
    FOURCC_YV16("YV16"),
    /**
     * I444
     */
    FOURCC_I444("I444"),
    /**
     * YV24
     */
    FOURCC_YV24("YV24");

    private final String format;
    private final long code;

    FourCC(@NonNull String format) {
        this.format = format;
        this.code = obtainFourccCode(format);
    }

    /**
     * 获取数据格式对应的代码值
     *
     * @param format 格式
     * @return 返回数据格式对应的代码值
     */
    private long obtainFourccCode(@NonNull String format) {
        char[] chars = format.toCharArray();
        return chars[0] | chars[1] << 8 | (long) chars[2] << 16 | (long) chars[3] << 24;
    }

    @NonNull
    @Override
    public String toString() {
        return format;
    }

    /**
     * 获取数据格式对应的代码值
     *
     * @return 返回数据格式对应的代码值
     */
    long getCode() {
        return code;
    }

    /**
     * 根据宽高获取数据占用字节大小
     *
     * @param width  图像宽度
     * @param height 图像高度
     * @return 返回数据占用字节大小
     */
    int getTotalBppSize(int width, int height) {
        int size;
        switch (this) {
            // 16
            case FOURCC_YUY2:
            case FOURCC_UYVY:
            case FOURCC_I422:

            case FOURCC_RGBP:
            case FOURCC_RGBO:
            case FOURCC_R444:

            case FOURCC_YV16:
                size = width * height << 1;
                break;
            // 24
            case FOURCC_24BG:
            case FOURCC_RAW:
            case FOURCC_YV24:
                size = width * height * 3;
                break;
            // 32
            case FOURCC_ARGB:
            case FOURCC_BGRA:
            case FOURCC_ABGR:
            case FOURCC_RGBA:
            case FOURCC_I444:
            case FOURCC_AR30:
                size = width * height << 2;
                break;
            // 8
            case FOURCC_I400:
                size = width * height;
                break;
            // 12
            case FOURCC_NV12:
            case FOURCC_NV21:
            case FOURCC_I420:
            case FOURCC_YV12:
                size = width * height * 3 >> 1;
                break;
            default:
                throw new IllegalArgumentException();
        }
        return size;
    }
}
