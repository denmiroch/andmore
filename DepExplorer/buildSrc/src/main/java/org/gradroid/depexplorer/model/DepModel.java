package org.gradroid.depexplorer.model;

import java.util.Collection;

/**
 * 17 июн. 2016 г.
 *
 * @author denis.mirochnik
 */
public interface DepModel {
    Collection<String> getAptJars();

    Collection<DepVariant> getDepVariants();
}
