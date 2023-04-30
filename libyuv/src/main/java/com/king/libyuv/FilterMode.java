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
 * Supported filtering.
 *
 * @author <a href="mailto:jenly1314@gmail.com">Jenly</a>
 * <p>
 * <a href="https://github.com/jenly1314">Follow me</a>
 */
@IntDef({
        FilterMode.FILTER_NONE,
        FilterMode.FILTER_LINEAR,
        FilterMode.FILTER_BILINEAR,
        FilterMode.FILTER_BOX,
})
@Retention(RetentionPolicy.SOURCE)
public @interface FilterMode {
    /**
     * Point sample; Fastest.
     */
    int FILTER_NONE = 0;
    /**
     * Filter horizontally only.
     */
    int FILTER_LINEAR = 1;
    /**
     * Faster than box, but lower quality scaling down.
     */
    int FILTER_BILINEAR = 2;
    /**
     * Highest quality.
     */
    int FILTER_BOX = 3;
}
