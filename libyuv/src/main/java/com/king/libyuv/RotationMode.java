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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.IntDef;

/**
 * Supported rotation.
 *
 * @author <a href="mailto:jenly1314@gmail.com">Jenly</a>
 * <p>
 * <a href="https://github.com/jenly1314">Follow me</a>
 */
@IntDef({
        RotationMode.ROTATE_0,
        RotationMode.ROTATE_90,
        RotationMode.ROTATE_180,
        RotationMode.ROTATE_270,
})
@Retention(RetentionPolicy.SOURCE)
public @interface RotationMode {
    /**
     * No rotation.
     */
    int ROTATE_0 = 0;
    /**
     * Rotate 90 degrees clockwise.
     */
    int ROTATE_90 = 90;
    /**
     * Rotate 180 degrees.
     */
    int ROTATE_180 = 180;
    /**
     * Rotate 270 degrees clockwise.
     */
    int ROTATE_270 = 270;
}
