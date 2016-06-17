package org.gradroid.depexplorer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.gradroid.depexplorer.model.DepModel;
import org.gradroid.depexplorer.model.DepVariant;

/**
 * 17 июн. 2016 г.
 *
 * @author denis.mirochnik
 */
public class DepModelImpl implements DepModel, Serializable {
    private static final long serialVersionUID = 1L;

    private final ArrayList<String> mAptJars;
    private final ArrayList<DepVariant> mDepVariants;

    DepModelImpl(ArrayList<String> aptJars, ArrayList<DepVariant> depVariants) {
        mAptJars = aptJars;
        mDepVariants = depVariants;
    }

    @Override
    public List<String> getAptJars() {
        return mAptJars;
    }

    @Override
    public Collection<DepVariant> getDepVariants() {
        return mDepVariants;
    }
}
