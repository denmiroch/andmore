package org.eclipse.andmore.android.gradle;

import org.eclipse.andmore.internal.launch.AndroidLaunchController;
import org.eclipse.andmore.internal.sdk.ProjectState;
import org.eclipse.andmore.internal.sdk.Sdk;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;

public class GradroidLaunchShortcut implements ILaunchShortcut {

    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.ILaunchShortcut#launch(
     * org.eclipse.jface.viewers.ISelection, java.lang.String)
     */
    @Override
    public void launch(ISelection selection, String mode) {
        if (selection instanceof IStructuredSelection) {

            // get the object and the project from it
            IStructuredSelection structSelect = (IStructuredSelection) selection;
            Object o = structSelect.getFirstElement();

            // get the first (and normally only) element
            if (o instanceof IAdaptable) {
                IResource r = ((IAdaptable) o).getAdapter(IResource.class);

                // get the project from the resource
                if (r != null) {
                    IProject project = r.getProject();

                    if (project != null) {
                        ProjectState state = Sdk.getProjectState(project);
                        if (state != null && state.isLibrary()) {

                            MessageDialog.openError(PlatformUI.getWorkbench().getDisplay().getActiveShell(),
                                    "Android Launch", "Android library projects cannot be launched.");
                        } else {
                            // and launch
                            launch(project, mode);
                        }
                    }
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.ILaunchShortcut#launch(
     * org.eclipse.ui.IEditorPart, java.lang.String)
     */
    @Override
    public void launch(IEditorPart editor, String mode) {
        // since we force the shortcut to only work on selection in the
        // package explorer, this will never be called.
    }

    /**
     * Launch a config for the specified project.
     * @param project The project to launch
     * @param mode The launch mode ("debug", "run" or "profile")
     */
    private void launch(IProject project, String mode) {
        // get an existing or new launch configuration
        ILaunchConfiguration config = AndroidLaunchController.getLaunchConfig(project,
                GradroidLaunchConfigDelegate.GRADROID_LAUNCH_TYPE_ID);

        if (config != null) {
            // and launch!
            DebugUITools.launch(config, mode);
        }
    }
}
