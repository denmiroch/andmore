package org.eclipse.andmore.android.gradle;

import org.eclipse.andmore.internal.launch.LaunchConfigDelegate;

/**
 * 15 июн. 2016 г.
 *
 * @author denis.mirochnik
 */
public class GradroidLaunchConfigDelegate extends LaunchConfigDelegate {
    public final static String GRADROID_LAUNCH_TYPE_ID = "org.eclipse.andmore.debug.GradroidLaunchConfigType"; //$NON-NLS-1$

    @Override
    protected boolean isGradroidLaunch() {
        return true;
    }
}
