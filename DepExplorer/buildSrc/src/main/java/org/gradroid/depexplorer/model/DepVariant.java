package org.gradroid.depexplorer.model;

import java.util.Map;

/**
 * 17 июн. 2016 г.
 *
 * @author denis.mirochnik
 */
public interface DepVariant {
    String getName();

    Map<String, String> getSources();

    Map<String, String> getJavadocs();
}
