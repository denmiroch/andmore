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
import java.util.Collection;
import java.util.Map;

/**
 * Entry point for the model of the Android native support.
 */
public interface NativeAndroidProject {
    String BUILD_SYSTEM_UNKNOWN = "unknown";
    String BUILD_SYSTEM_GRADLE = "gradle";
    String BUILD_SYSTEM_CMAKE = "cmake";
    String BUILD_SYSTEM_NDK_BUILD = "ndk-build";

    /**
    * Returns the model version. This is a string in the format X.Y.Z
    */
    
    String getModelVersion();

    /**
    * Returns the model api version.
    * <p>
    * This is different from {@link #getModelVersion()} in a way that new model
    * version might increment model version but keep existing api. That means that
    * code which was built against particular 'api version' might be safely re-used for all
    * new model versions as long as they don't change the api.
    * <p>
    * Every new model version is assumed to return an 'api version' value which
    * is equal or greater than the value used by the previous model version.
    */
    int getApiVersion();

    /**
    * Returns the name of the module.
    */
    
    String getName();

    /**
    * Returns a collection of files that affects the build.
    */
    
    Collection<File> getBuildFiles();

    /**
    * Returns a collection of native artifacts.
    */
    
    Collection<NativeArtifact> getArtifacts();

    /**
    * Returns a collection of toolchains.
    */
    
    Collection<NativeToolchain> getToolChains();

    /**
    * Returns a collection of all compile settings.
    */
    
    Collection<NativeSettings> getSettings();

    /**
    * Return a map of file extension to each file type.
    *
    * The key is the file extension, the value is either "c" or "c++".
    */
    
    Map<String, String> getFileExtensions();

    /**
     * Return the names of build systems used to create the native artifacts.
     */
    
    Collection<String> getBuildSystems();
}
