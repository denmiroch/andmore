/*
 * Copyright (C) 2010 The Android Open Source Project
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

package org.eclipse.andmore.internal.build;

/**
 * Exception thrown when aapt reports an error in the resources.
 *
 */
public final class ProguardResultException extends ExecResultException {
    private static final long serialVersionUID = 1L;

    ProguardResultException(int errorCode, String[] output) {
        super(errorCode, output);
    }

    @Override
    public String getLabel() {
        return "Proguard";
    }
}
