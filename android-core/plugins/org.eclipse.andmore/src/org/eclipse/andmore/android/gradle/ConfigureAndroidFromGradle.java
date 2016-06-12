package org.eclipse.andmore.android.gradle;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * 11 июн. 2016 г.
 *
 * @author denis.mirochnik
 */
public class ConfigureAndroidFromGradle extends AbstractHandler {

    private static final class ConfigureAndroidFromGradleJob extends Job {

        private final ArrayList<IProject> mProjects;

        public ConfigureAndroidFromGradleJob(ArrayList<IProject> projectList) {
            super("Configure Gradroid");
            mProjects = projectList;
        }

        @Override
        protected IStatus run(IProgressMonitor monitor) {
            monitor.beginTask("Configuring Gradroid", 1);

            for (IProject project : mProjects) {

                try {
                    Gradroid.get().configureProject(project, monitor);
                } catch (CoreException e1) {
                    // TODO log or smithng
                }

                monitor.worked(1);
            }

            monitor.done();
            return Status.OK_STATUS;
        }

    }

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IWorkbench workbench = PlatformUI.getWorkbench();
        if ((workbench == null) || workbench.isClosing()) {
            return null;
        }

        IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
        if (window == null) {
            return null;
        }

        ISelection selection = window.getSelectionService().getSelection();
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection sselection = (IStructuredSelection) selection;
            Iterator<?> it = sselection.iterator();

            // Construct a list of valid projects to be cleaned
            ArrayList<IProject> projectList = new ArrayList<IProject>(sselection.size());

            while (it.hasNext()) {
                Object resource = it.next();

                // Check if the selected item is a project
                if (resource instanceof IJavaProject) {
                    IJavaProject javaProject = (IJavaProject) resource;
                    projectList.add(javaProject.getProject());
                } else if (resource instanceof IAdaptable) {
                    IAdaptable adaptable = (IAdaptable) resource;
                    projectList.add(adaptable.getAdapter(IProject.class));
                }
            }

            Job job = new ConfigureAndroidFromGradleJob(projectList);
            job.schedule();
        }

        return null;
    }
}
