package org.eclipse.andmore.android.gradle;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import org.eclipse.andmore.android.AndroidPlugin;
import org.eclipse.andmore.android.SdkUtils;
import org.eclipse.andmore.internal.project.AndroidNature;
import org.eclipse.andmore.internal.sdk.Sdk;
import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.configuration.GradleProjectNature;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.android.builder.model.AndroidProject;
import com.android.builder.model.Variant;
import com.android.sdklib.IAndroidTarget;
import com.gradleware.tooling.toolingclient.BuildLaunchRequest;
import com.gradleware.tooling.toolingclient.LaunchableConfig;
import com.gradleware.tooling.toolingclient.ModelRequest;

/**
 * 11 июн. 2016 г.
 *
 * @author denis.mirochnik
 */
@SuppressWarnings("restriction")
public class Gradroid {

    public static final String BUILDSHIP_NATURE = GradleProjectNature.ID;
    public static final String ANDMORE_NATURE = AndroidPlugin.Android_Nature;

    private static final Gradroid sInstance = new Gradroid();
    private static final Object LOCK = new Object();

    private HashMap<IProject, AndroidProject> models = new HashMap<IProject, AndroidProject>();

    private static class SetupProjectJob extends Job {

        private IProject mProject;

        public SetupProjectJob(IProject project) {
            super("Setup Gradroid project");

            mProject = project;
        }

        @Override
        protected IStatus run(IProgressMonitor monitor) {

            monitor.beginTask("Setup Gradroid project", 1);

            Gradroid.get().loadAndroidModel(mProject, monitor);

            // TODO execute get task? (or builder should do this?)

            monitor.worked(1);
            monitor.done();

            return Status.OK_STATUS;
        }
    }

    private static class ProjectOpenListener implements IResourceChangeListener, IResourceDeltaVisitor {

        @Override
        public void resourceChanged(IResourceChangeEvent event) {
            if (event == null || event.getDelta() == null) {
                return;
            }

            try {
                event.getDelta().accept(this);
            } catch (CoreException e) {
                // TODO log or smthing
            }
        }

        @Override
        public boolean visit(IResourceDelta delta) throws CoreException {
            if (delta.getKind() == IResourceDelta.OPEN) {
                IResource resource = delta.getResource();
                if (resource instanceof IProject) {
                    Gradroid.get().setupProject((IProject) resource);
                }
            }
            return false;
        }
    }

    public static Gradroid get() {
        return sInstance;
    }

    public void setup() {
        // TODO setup Gradroid (load projects' models, populate views, start
        // builds?)

        IWorkspace workspace = ResourcesPlugin.getWorkspace();

        workspace.addResourceChangeListener(new ProjectOpenListener());

        IProject[] projects = workspace.getRoot().getProjects();

        for (IProject project : projects) {
            try {
                setupProject(project);
            } catch (CoreException e) {
                // goto next project
                // TODO log or smthing
            }
        }
    }

    private void setupProject(IProject project) throws CoreException {
        if (project.isOpen() && project.hasNature(ANDMORE_NATURE) && project.hasNature(BUILDSHIP_NATURE)) {
            new SetupProjectJob(project).schedule();
        }
    }

    public void configureProject(IProject project, IProgressMonitor monitor) throws CoreException {

        // TODO search other project?
        if (!(project.hasNature(Gradroid.BUILDSHIP_NATURE) && !project.hasNature(Gradroid.ANDMORE_NATURE))) {
            return;
        }

        AndroidProject androidProject = Gradroid.get().loadAndroidModel(project, monitor);

//        String compileTarget = androidProject.getCompileTarget();

//        IAndroidTarget androidTarget = Sdk.getCurrent().getTargetFromHashString(compileTarget);

//        SdkUtils.associate(project, androidTarget);

//        AndroidNature.setupProjectNatures(project, monitor, true);

        // TODO classpath containers and other things (builders?)
    }

    public AndroidProject loadAndroidModel(IProject project, IProgressMonitor monitor) {

        synchronized (LOCK) {
            AndroidProject model = models.get(project);

            if (model != null) {
                return model;
            }
        }

        return reloadeAndroidModel(project, monitor);
    }

    public AndroidProject reloadeAndroidModel(IProject project, IProgressMonitor monitor) {
        AndroidProject model = requestAndroidModel(project, monitor);

        synchronized (LOCK) {
            AndroidProject was = models.put(project, model);

            if (was != null) {
                // TODO notify changes
            } else {
                // TODO notify setup complete
            }
        }

        return model;
    }

    private AndroidProject requestAndroidModel(IProject project, IProgressMonitor monitor) {
        monitor.beginTask("Requesting model", 1);

        ModelRequest<AndroidProject> modelRequest = CorePlugin.toolingClient().newModelRequest(AndroidProject.class);

        modelRequest.projectDir(project.getLocation().toFile());
        modelRequest.arguments(
                AndroidProject.PROPERTY_BUILD_MODEL_ONLY_VERSIONED + "=" + AndroidProject.MODEL_LEVEL_2_DEP_GRAPH);

        // TODO progress, cancelation
        AndroidProject model = modelRequest.executeAndWait();

        monitor.worked(1);
        monitor.done();

        Collection<Variant> variants = model.getVariants();

        monitor.beginTask("Building sources for IDE", variants.size());

        for (Variant variant : variants) {
            Set<String> tasks = variant.getMainArtifact().getIdeSetupTaskNames();

            System.out.println(tasks);

            BuildLaunchRequest launchRequest = CorePlugin.toolingClient()
                    .newBuildLaunchRequest(LaunchableConfig.forTasks(tasks));

            // TODO progress, cancelation
            launchRequest.projectDir(project.getLocation().toFile());
            launchRequest.executeAndWait();

            monitor.worked(1);
        }

        monitor.done();

        return model;
    }

    private Gradroid() {}
}
