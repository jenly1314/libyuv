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
package com.king.libyuv.app.util;

import android.graphics.Bitmap;

/**
 * @author <a href="mailto:jenly1314@gmail.com">Jenly</a>
 * <p>
 * <a href="https://github.com/jenly1314">Follow me</a>
 */
public final class BitmapUtil {

    private BitmapUtil(){
        throw new AssertionError();
    }

    /**
     * Bitmap 转 RGBA
     * @param bitmap
     * @return
     */
    public static byte[] bitmapToRgba(Bitmap bitmap) {
        if (bitmap.getConfig() != Bitmap.Config.ARGB_8888)
            throw new IllegalArgumentException("Bitmap must be in ARGB_8888 format");
        int[] pixels = new int[bitmap.getWidth() * bitmap.getHeight()];
        byte[] bytes = new byte[pixels.length * 4];
        bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        int i = 0;
        for (int pixel : pixels) {
            // Get components assuming is ARGB
            int A = (pixel >> 24) & 0xff;
            int R = (pixel >> 16) & 0xff;
            int G = (pixel >> 8) & 0xff;
            int B = pixel & 0xff;
            bytes[i++] = (byte) R;
            bytes[i++] = (byte) G;
            bytes[i++] = (byte) B;
            bytes[i++] = (byte) A;
        }
        return bytes;
    }

    /**
     * 将RGBA数据转为Bitmap
     * @param width
     * @param height
     * @param bytes
     * @return
     */
    public static Bitmap bitmapFromRgba(int width, int height, byte[] bytes) {
        int[] pixels = new int[bytes.length / 4];
        int j = 0;

        for (int i = 0; i < pixels.length; i++) {
            int R = bytes[j++] & 0xff;
            int G = bytes[j++] & 0xff;
            int B = bytes[j++] & 0xff;
            int A = bytes[j++] & 0xff;

            int pixel = (A << 24) | (R << 16) | (G << 8) | B;
            pixels[i] = pixel;
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }
}
