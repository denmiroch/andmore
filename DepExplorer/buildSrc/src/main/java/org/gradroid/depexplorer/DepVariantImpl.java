package org.gradroid.depexplorer;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.gradroid.depexplorer.model.DepVariant;

/**
 * 17 июн. 2016 г.
 *
 * @author denis.mirochnik
 */
public class DepVariantImpl implements DepVariant, Serializable {

    private static final long serialVersionUID = 1L;

    private final HashMap<String, String> mSources;
    private final String mName;

    DepVariantImpl(HashMap<String, String> sources, String name) {
        mSources = sources;
        mName = name;
    }

    @Override
    public Map<String, String> getSources() {
        return mSources;
    }

    @Override
    public String getName() {
        return mName;
    }
}
