package org.home.gradroid.aptexplorer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.home.gradroid.aptexplorer.model.AptModel;

/**
 * 17 июн. 2016 г.
 *
 * @author denis.mirochnik
 */
public class AptModelImpl implements AptModel, Serializable {
    private static final long serialVersionUID = 1L;

    private ArrayList<String> mAptJars;

    AptModelImpl(ArrayList<String> aptJars) {
        mAptJars = aptJars;
    }

    @Override
    public List<String> getAptJars() {
        return mAptJars;
    }
}
