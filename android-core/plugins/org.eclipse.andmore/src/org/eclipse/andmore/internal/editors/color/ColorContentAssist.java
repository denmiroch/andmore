/*
 * Copyright (C) 2007 The Android Open Source Project
 * Licensed under the Eclipse Public License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.eclipse.org/org/documents/epl-v10.php
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.eclipse.andmore.internal.editors.color;

import org.eclipse.andmore.internal.editors.AndroidContentAssist;
import org.eclipse.andmore.internal.sdk.AndroidTargetData;

import com.android.annotations.VisibleForTesting;

/**
 * Content Assist Processor for /res/color XML files
 */
@VisibleForTesting
public final class ColorContentAssist extends AndroidContentAssist {
    public ColorContentAssist() {
        super(AndroidTargetData.DESCRIPTOR_COLOR);
    }
}
