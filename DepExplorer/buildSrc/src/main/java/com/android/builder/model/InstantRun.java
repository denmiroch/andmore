/*
 * Copyright (C) 2015 The Android Open Source Project
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.builder.model;

import java.io.File;

/**
 * Model for InstantRun related information.
 */
public interface InstantRun {

    /**
     * Returns the name of the task used to generate the incremental .dex files from the last
     * build.
     */
    
    String getIncrementalAssembleTaskName();

    /**
     * Returns the last incremental build information, including success or failure, verifier
     * reason for requesting a restart, etc...
     * @return a file location, possibly not existing.
     */
    
    File getInfoFile();

    /**
     * Whether the owner artifact supports Instant Run. This may depend on the toolchain used.
     */
    boolean isSupportedByArtifact();
}
