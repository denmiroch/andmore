/*
 * Copyright (C) 2016 The Android Open Source Project
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
import java.util.List;

/**
 * Represents an Android dependency that is bundled and will be unbundled.
 */
public interface AndroidBundle extends Library {

    /**
     * Returns an optional configuration name if the library is output by a module
     * that publishes more than one variant.
     */
    
    String getProjectVariant();

    /**
     * Returns the location of the dependency bundle.
     */
    
    File getBundle();

    /**
     * Returns the location of the unzipped bundle folder.
     */
    
    File getFolder();

    /**
     * Returns the list of direct library dependencies of this dependency.
     * The order is important.
     */
    
    List<? extends AndroidLibrary> getLibraryDependencies();

    /**
     * Returns the collection of external Jar files that are included in the dependency.
     * @return a list of JavaDependency. May be empty but not null.
     */
    
    Collection<? extends JavaLibrary> getJavaDependencies();

    /**
     * Returns the location of the manifest.
     */
    
    File getManifest();

    /**
     * Returns the location of the jar file to use for either packaging or compiling depending on
     * the bundle type.
     *
     * @return a File for the jar file. The file may not point to an existing file.
     */
    
    File getJarFile();

}
