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

#include <jni.h>
#include "libyuv_jni.h"
#include "libyuv/video_common.h"

/**
 * LibYuv：基于Google的libyuv编译封装的YUV转换类工具库，主要用途是在各种YUV与RGB之间进行相互转换、裁减、旋转、缩放、镜像等。
 *
 * @author <a href="mailto:jenly1314@gmail.com">Jenly</a>
 * <p>
 * <a href="https://github.com/jenly1314">Follow me</a>
 */

/**
 * YUV转I420
 */
extern "C"
JNIEXPORT void JNICALL
Java_com_king_libyuv_LibYuv_YUVToI420(JNIEnv *env, jclass clazz, jobject src_y_buffer,
                                      jobject src_u_buffer, jobject src_v_buffer,
                                      jint stride_y, jint stride_u, jint stride_v,
                                      jint pixel_stride_uv, jint width, jint height,
                                      jbyteArray dst_i420_array, jint degrees) {

    uint8_t *src_y_data = (uint8_t *) env->GetDirectBufferAddress(src_y_buffer);
    uint8_t *src_u_data = (uint8_t *) env->GetDirectBufferAddress(src_u_buffer);
    uint8_t *src_v_data = (uint8_t *) env->GetDirectBufferAddress(src_v_buffer);

    jbyte *dst_i420_data = env->GetByteArrayElements(dst_i420_array, JNI_FALSE);

    jint src_y_size = width * height;
    jint src_u_size = ((width + 1) >> 1) * ((height + 1) >> 1);

    jbyte *dst_i420_y_data = dst_i420_data;
    jbyte *dst_i420_u_data = dst_i420_data + src_y_size;
    jbyte *dst_i420_v_data = dst_i420_data + src_y_size + src_u_size;

    jint dst_stride_y = width;
    if (degrees == libyuv::kRotate90 || degrees == libyuv::kRotate270) {
        dst_stride_y = height;
    }

    libyuv::Android420ToI420Rotate(src_y_data, stride_y,
                                   src_u_data, stride_u,
                                   src_v_data, stride_v,
                                   pixel_stride_uv,
                                   (uint8_t *) dst_i420_y_data, dst_stride_y,
                                   (uint8_t *) dst_i420_u_data, dst_stride_y >> 1,
                                   (uint8_t *) dst_i420_v_data, dst_stride_y >> 1,
                                   width, height,
                                   (libyuv::RotationMode) degrees);

    env->ReleaseByteArrayElements(dst_i420_array, dst_i420_data, 0);

}

/**
 * NV21转I420
 */
extern "C"
JNIEXPORT void JNICALL
Java_com_king_libyuv_LibYuv_NV21ToI420(JNIEnv *env, jclass clazz, jbyteArray src_nv21_array,
                                       jint width, jint height, jbyteArray dst_i420_array) {
    jbyte *src_nv21_data = env->GetByteArrayElements(src_nv21_array, JNI_FALSE);
    jbyte *dst_i420_data = env->GetByteArrayElements(dst_i420_array, JNI_FALSE);

    jint src_y_size = width * height;
    jint src_u_size = ((width + 1) >> 1) * ((height + 1) >> 1);

    jbyte *src_nv21_y_data = src_nv21_data;
    jbyte *src_nv21_vu_data = src_nv21_data + src_y_size;

    jbyte *dst_i420_y_data = dst_i420_data;
    jbyte *dst_i420_u_data = dst_i420_data + src_y_size;
    jbyte *dst_i420_v_data = dst_i420_data + src_y_size + src_u_size;

    libyuv::NV21ToI420((const uint8_t *) src_nv21_y_data, width,
                       (const uint8_t *) src_nv21_vu_data, width,
                       (uint8_t *) dst_i420_y_data, width,
                       (uint8_t *) dst_i420_u_data, width >> 1,
                       (uint8_t *) dst_i420_v_data, width >> 1,
                       width, height);

    env->ReleaseByteArrayElements(src_nv21_array, src_nv21_data, 0);
    env->ReleaseByteArrayElements(dst_i420_array, dst_i420_data, 0);

}

/**
 * I420转NV21
 */
extern "C"
JNIEXPORT void JNICALL
Java_com_king_libyuv_LibYuv_I420ToNV21(JNIEnv *env, jclass clazz, jbyteArray src_i420_array,
                                       jint width, jint height, jbyteArray dst_nv21_array) {
    jbyte *src_i420_data = env->GetByteArrayElements(src_i420_array, JNI_FALSE);
    jbyte *dst_nv21_data = env->GetByteArrayElements(dst_nv21_array, JNI_FALSE);

    jint src_y_size = width * height;
    jint src_u_size = ((width + 1) >> 1) * ((height + 1) >> 1);

    jbyte *src_i420_y_data = src_i420_data;
    jbyte *src_i420_u_data = src_i420_data + src_y_size;
    jbyte *src_i420_v_data = src_i420_data + src_y_size + src_u_size;

    jbyte *dst_nv21_y_data = dst_nv21_data;
    jbyte *dst_nv21_uv_data = dst_nv21_data + src_y_size;

    libyuv::I420ToNV21(
            (const uint8_t *) src_i420_y_data, width,
            (const uint8_t *) src_i420_u_data, width >> 1,
            (const uint8_t *) src_i420_v_data, width >> 1,
            (uint8_t *) dst_nv21_y_data, width,
            (uint8_t *) dst_nv21_uv_data, width,
            width, height);

    env->ReleaseByteArrayElements(src_i420_array, src_i420_data, 0);
    env->ReleaseByteArrayElements(dst_nv21_array, dst_nv21_data, 0);
}

/**
 * 将I420转换为指定格式
 */
extern "C"
JNIEXPORT void JNICALL
Java_com_king_libyuv_LibYuv_ConvertFromI420(JNIEnv *env, jclass clazz,
                                            jbyteArray src_i420_array, jint width, jint height,
                                            jbyteArray dst_sample_array, jint dst_sample_stride,
                                            jlong fourcc) {

    jbyte *src_i420_data = env->GetByteArrayElements(src_i420_array, JNI_FALSE);
    jbyte *dst_sample_data = env->GetByteArrayElements(dst_sample_array, JNI_FALSE);

    jint src_y_size = width * height;
    jint src_u_size = ((width + 1) >> 1) * ((height + 1) >> 1);

    jbyte *src_i420_y_data = src_i420_data;
    jbyte *src_i420_u_data = src_i420_data + src_y_size;
    jbyte *src_i420_v_data = src_i420_data + src_y_size + src_u_size;

    libyuv::ConvertFromI420(
            (const uint8_t *) src_i420_y_data, width,
            (const uint8_t *) src_i420_u_data, width >> 1,
            (const uint8_t *) src_i420_v_data, width >> 1,
            (uint8_t *) dst_sample_data,
            dst_sample_stride,
            width, height, fourcc);

    env->ReleaseByteArrayElements(src_i420_array, src_i420_data, 0);
    env->ReleaseByteArrayElements(dst_sample_array, dst_sample_data, 0);
}

/**
 * I420裁减
 */
extern "C"
JNIEXPORT void JNICALL
Java_com_king_libyuv_LibYuv_ConvertToI420(JNIEnv *env, jclass clazz, jbyteArray src_array,
                                          jint src_size, jint width, jint height,
                                          jbyteArray dst_i420_array,
                                          jint crop_x, jint crop_y, jint crop_width,
                                          jint crop_height,
                                          jint degrees, jlong fourcc) {

    jbyte *src_data = env->GetByteArrayElements(src_array, JNI_FALSE);
    jbyte *dst_i420_data = env->GetByteArrayElements(dst_i420_array, JNI_FALSE);

    jint dst_i420_y_size = crop_width * crop_height;
    jint dst_i420_u_size = ((crop_width + 1) >> 1) * ((crop_height + 1) >> 1);
    jbyte *dst_i420_y_data = dst_i420_data;
    jbyte *dst_i420_u_data = dst_i420_data + dst_i420_y_size;
    jbyte *dst_i420_v_data = dst_i420_data + dst_i420_y_size + dst_i420_u_size;

    jint dst_stride_y = crop_width;
    if (degrees == libyuv::kRotate90 || degrees == libyuv::kRotate270) {
        dst_stride_y = crop_height;
    }

    libyuv::ConvertToI420((const uint8_t *) src_data, src_size,
                          (uint8_t *) dst_i420_y_data, dst_stride_y,
                          (uint8_t *) dst_i420_u_data, dst_stride_y >> 1,
                          (uint8_t *) dst_i420_v_data, dst_stride_y >> 1,
                          crop_x, crop_y,
                          width, height,
                          crop_width, crop_height,
                          (libyuv::RotationMode) degrees, fourcc);

    env->ReleaseByteArrayElements(src_array, src_data, 0);
    env->ReleaseByteArrayElements(dst_i420_array, dst_i420_data, 0);
}

/**
 * I420旋转
 */
extern "C"
JNIEXPORT void JNICALL
Java_com_king_libyuv_LibYuv_I420Rotate(JNIEnv *env, jclass clazz, jbyteArray src_i420_array,
                                       jint width, jint height, jbyteArray dst_i420_array,
                                       jint degrees) {
    jbyte *src_i420_data = env->GetByteArrayElements(src_i420_array, JNI_FALSE);
    jbyte *dst_i420_data = env->GetByteArrayElements(dst_i420_array, JNI_FALSE);

    jint src_i420_y_size = width * height;
    jint src_i420_u_size = ((width + 1) >> 1) * ((height + 1) >> 1);

    jbyte *src_i420_y_data = src_i420_data;
    jbyte *src_i420_u_data = src_i420_data + src_i420_y_size;
    jbyte *src_i420_v_data = src_i420_data + src_i420_y_size + src_i420_u_size;

    jbyte *dst_i420_y_data = dst_i420_data;
    jbyte *dst_i420_u_data = dst_i420_data + src_i420_y_size;
    jbyte *dst_i420_v_data = dst_i420_data + src_i420_y_size + src_i420_u_size;

    jint dst_stride_y = width;
    if (degrees == libyuv::kRotate90 || degrees == libyuv::kRotate270) {
        dst_stride_y = height;
    }

    libyuv::I420Rotate((const uint8_t *) src_i420_y_data, width,
                       (const uint8_t *) src_i420_u_data, width >> 1,
                       (const uint8_t *) src_i420_v_data, width >> 1,
                       (uint8_t *) dst_i420_y_data, dst_stride_y,
                       (uint8_t *) dst_i420_u_data, dst_stride_y >> 1,
                       (uint8_t *) dst_i420_v_data, dst_stride_y >> 1,
                       width, height,
                       (libyuv::RotationMode) degrees);

    env->ReleaseByteArrayElements(src_i420_array, src_i420_data, 0);
    env->ReleaseByteArrayElements(dst_i420_array, dst_i420_data, 0);
}

/**
 * I420缩放
 */
extern "C"
JNIEXPORT void JNICALL
Java_com_king_libyuv_LibYuv_I420Scale(JNIEnv *env, jclass clazz, jbyteArray src_i420_array,
                                      jint width, jint height, jbyteArray dst_i420_array,
                                      jint dst_width, jint dst_height, jint filtering) {
    jbyte *src_i420_data = env->GetByteArrayElements(src_i420_array, JNI_FALSE);
    jbyte *dst_i420_data = env->GetByteArrayElements(dst_i420_array, JNI_FALSE);

    jint src_i420_y_size = width * height;
    jint src_i420_u_size = ((width + 1) >> 1) * ((height + 1) >> 1);
    jbyte *src_i420_y_data = src_i420_data;
    jbyte *src_i420_u_data = src_i420_data + src_i420_y_size;
    jbyte *src_i420_v_data = src_i420_data + src_i420_y_size + src_i420_u_size;

    jint dst_i420_y_size = dst_width * dst_height;
    jint dst_i420_u_size = ((dst_width + 1) >> 1) * ((dst_height + 1) >> 1);
    jbyte *dst_i420_y_data = dst_i420_data;
    jbyte *dst_i420_u_data = dst_i420_data + dst_i420_y_size;
    jbyte *dst_i420_v_data = dst_i420_data + dst_i420_y_size + dst_i420_u_size;

    libyuv::I420Scale((const uint8_t *) src_i420_y_data, width,
                      (const uint8_t *) src_i420_u_data, width >> 1,
                      (const uint8_t *) src_i420_v_data, width >> 1,
                      width, height,
                      (uint8_t *) dst_i420_y_data, dst_width,
                      (uint8_t *) dst_i420_u_data, dst_width >> 1,
                      (uint8_t *) dst_i420_v_data, dst_width >> 1,
                      dst_width, dst_height,
                      (libyuv::FilterMode) filtering);

    env->ReleaseByteArrayElements(src_i420_array, src_i420_data, 0);
    env->ReleaseByteArrayElements(dst_i420_array, dst_i420_data, 0);
}

/**
 * I420裁减
 */
extern "C"
JNIEXPORT void JNICALL
Java_com_king_libyuv_LibYuv_I420Crop(JNIEnv *env, jclass clazz, jbyteArray src_i420_array,
                                     jint width, jint height, jbyteArray dst_i420_array,
                                     jint crop_x, jint crop_y, jint crop_width, jint crop_height) {

    jbyte *src_i420_data = env->GetByteArrayElements(src_i420_array, JNI_FALSE);
    jbyte *dst_i420_data = env->GetByteArrayElements(dst_i420_array, JNI_FALSE);

    jint src_i420_size = width * height * 3 >> 1;

    jint dst_i420_y_size = crop_width * crop_height;
    jint dst_i420_u_size = ((crop_width + 1) >> 1) * ((crop_height + 1) >> 1);
    jbyte *dst_i420_y_data = dst_i420_data;
    jbyte *dst_i420_u_data = dst_i420_data + dst_i420_y_size;
    jbyte *dst_i420_v_data = dst_i420_data + dst_i420_y_size + dst_i420_u_size;

    libyuv::ConvertToI420((const uint8_t *) src_i420_data, src_i420_size,
                          (uint8_t *) dst_i420_y_data, crop_width,
                          (uint8_t *) dst_i420_u_data, crop_width >> 1,
                          (uint8_t *) dst_i420_v_data, crop_width >> 1,
                          crop_x, crop_y,
                          width, height,
                          crop_width, crop_height,
                          libyuv::kRotate0, libyuv::FOURCC_I420);

    env->ReleaseByteArrayElements(src_i420_array, src_i420_data, 0);
    env->ReleaseByteArrayElements(dst_i420_array, dst_i420_data, 0);
}

/**
 * I420镜像
 */
extern "C"
JNIEXPORT void JNICALL
Java_com_king_libyuv_LibYuv_I420Mirror(JNIEnv *env, jclass clazz, jbyteArray src_i420_array,
                                       jint width, jint height, jbyteArray dst_i420_array) {
    jbyte *src_i420_data = env->GetByteArrayElements(src_i420_array, JNI_FALSE);
    jbyte *dst_i420_data = env->GetByteArrayElements(dst_i420_array, JNI_FALSE);

    jint src_i420_y_size = width * height;
    jint src_i420_u_size = src_i420_y_size >> 2;

    jbyte *src_i420_y_data = src_i420_data;
    jbyte *src_i420_u_data = src_i420_data + src_i420_y_size;
    jbyte *src_i420_v_data = src_i420_data + src_i420_y_size + src_i420_u_size;

    jbyte *dst_i420_y_data = dst_i420_data;
    jbyte *dst_i420_u_data = dst_i420_data + src_i420_y_size;
    jbyte *dst_i420_v_data = dst_i420_data + src_i420_y_size + src_i420_u_size;

    libyuv::I420Mirror((const uint8_t *) src_i420_y_data, width,
                       (const uint8_t *) src_i420_u_data, width >> 1,
                       (const uint8_t *) src_i420_v_data, width >> 1,
                       (uint8_t *) dst_i420_y_data, width,
                       (uint8_t *) dst_i420_u_data, width >> 1,
                       (uint8_t *) dst_i420_v_data, width >> 1,
                       width, height);

    env->ReleaseByteArrayElements(src_i420_array, src_i420_data, 0);
    env->ReleaseByteArrayElements(dst_i420_array, dst_i420_data, 0);
}