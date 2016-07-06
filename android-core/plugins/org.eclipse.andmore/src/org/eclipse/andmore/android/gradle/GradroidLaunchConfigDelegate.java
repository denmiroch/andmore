package org.eclipse.andmore.android.gradle;

import org.eclipse.andmore.internal.launch.LaunchConfigDelegate;
import org.eclipse.andmore.internal.project.ApkInstallManager;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;

/**
 * 15 июн. 2016 г.
 *
 * @author denis.mirochnik
 */
public class GradroidLaunchConfigDelegate extends LaunchConfigDelegate {
    public final static String GRADROID_LAUNCH_TYPE_ID = "org.eclipse.andmore.debug.GradroidLaunchConfigType"; //$NON-NLS-1$

    @Override
    public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
            throws CoreException {
        ApkInstallManager.getInstance().resetInstallationFor(getProject(configuration));
        super.launch(configuration, mode, launch, monitor);
    }

    @Override
    protected boolean isGradroidLaunch() {
        return true;
    }
}
