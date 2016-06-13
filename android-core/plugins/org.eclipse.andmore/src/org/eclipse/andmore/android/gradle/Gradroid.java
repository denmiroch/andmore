package org.eclipse.andmore.android.gradle;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.andmore.AndmoreAndroidConstants;
import org.eclipse.andmore.AndmoreAndroidPlugin;
import org.eclipse.andmore.internal.project.AndroidNature;
import org.eclipse.andmore.internal.project.ProjectHelper;
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
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.android.builder.model.AndroidProject;
import com.android.builder.model.Variant;
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

    //TODO check project.properties and vice versa
    //TODO update on .gradle change
    //TODO provide Total Update button

    public static final String BUILDSHIP_NATURE = GradleProjectNature.ID;
    public static final String ANDMORE_NATURE = AndmoreAndroidConstants.NATURE_DEFAULT;

    private static final Gradroid sInstance = new Gradroid();
    private static final Object LOCK = new Object();

    private static final String PROPERTY_VARIANT_QUALIFIER = "org.eclipse.andmore.gradle.property";
    private static final String PROPERTY_VARIANT_NAME = "variant";

    private HashMap<IProject, Lock> mRequestLocks = new HashMap<IProject, Lock>();
    private HashMap<IProject, AndroidProject> mModels = new HashMap<IProject, AndroidProject>();
    private HashMap<IProject, String> mVariants = new HashMap<IProject, String>();

    private static class SetupProjectJob extends Job {

        private IProject mProject;

        public SetupProjectJob(IProject project) {
            super("Setup Gradroid project");

            mProject = project;
        }

        @Override
        protected IStatus run(IProgressMonitor monitor) {

            monitor.beginTask("Setup Gradroid project", 1);

            AndroidProject androidProject = Gradroid.get().loadAndroidModel(mProject, monitor);

            String variantName = null;

            try {
                variantName = mProject
                        .getPersistentProperty(new QualifiedName(PROPERTY_VARIANT_QUALIFIER, PROPERTY_VARIANT_NAME));
            } catch (CoreException e) {}

            boolean found = false;

            if (variantName != null) {
                Collection<Variant> projectVariatns = androidProject.getVariants();

                for (Variant variant : projectVariatns) {
                    if (variant.getName().equals(variantName)) {
                        Gradroid.get().setProjectVariant(mProject, variantName);
                        found = true;
                        break;
                    }
                }
            }

            if (!found) {
                Gradroid.get().setProjectVariant(mProject, androidProject.getVariants().iterator().next().getName());
            }

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

    public boolean isGradroidProject(IProject project) throws CoreException {
        return project.hasNature(BUILDSHIP_NATURE) && project.hasNature(ANDMORE_NATURE);
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
                AndmoreAndroidPlugin.log(e, "");
                // goto next project
                // TODO log or smthing
            }
        }
    }

    public Variant getProjectVariant(IProject project) {
        synchronized (LOCK) {
            AndroidProject androidProject = mModels.get(project);
            String variantName = mVariants.get(project);
            Collection<Variant> variants = androidProject.getVariants();

            if (variantName == null) {
                Variant variant = variants.iterator().next();
                setProjectVariant(project, variant);
                return variant;
            }

            for (Variant variant : variants) {
                if (variant.getName().equals(variantName)) {
                    return variant;
                }
            }

            return null;
        }
    }

    public String getProjectVariantName(IProject project) {
        return mVariants.get(project);
    }

    public void setProjectVariant(IProject project, Variant variant) {
        setProjectVariant(project, variant.getName());
    }

    public void setProjectVariant(IProject project, String variantName) {
        synchronized (LOCK) {
            String was = mVariants.put(project, variantName);

            try {
                project.setPersistentProperty(new QualifiedName(PROPERTY_VARIANT_QUALIFIER, PROPERTY_VARIANT_NAME),
                        variantName);
            } catch (CoreException e) {
                AndmoreAndroidPlugin.log(e, "");
            }

            if (!variantName.equals(was)) {
                //TODO notify variant changed
            }
        }
    }

    public void setupProject(IProject project) throws CoreException {
        if (project.isOpen() && project.hasNature(ANDMORE_NATURE) && project.hasNature(BUILDSHIP_NATURE)) {
            new SetupProjectJob(project).schedule();
        }
    }

    public void configureProject(IProject project, IProgressMonitor monitor) throws CoreException {

        // TODO search other project?
        if (!(project.hasNature(Gradroid.BUILDSHIP_NATURE) && !project.hasNature(Gradroid.ANDMORE_NATURE))) {
            return;
        }

        AndroidNature.setupProjectNatures(project, monitor, true);
        ProjectHelper.fixProject(project, monitor, false);

        // TODO classpath containers and other things (builders?)
    }

    public AndroidProject loadAndroidModel(IProject project, IProgressMonitor monitor) {

        synchronized (LOCK) {
            AndroidProject model = mModels.get(project);

            if (model != null) {
                return model;
            }
        }

        return reloadAndroidModel(project, monitor);
    }

    public AndroidProject reloadAndroidModel(IProject project, IProgressMonitor monitor) {
        AndroidProject model = requestAndroidModel(project, monitor);

        synchronized (LOCK) {
            AndroidProject was = mModels.put(project, model);

            if (was != null) {
                // TODO notify changes
            } else {
                // TODO notify setup complete
            }
        }

        return model;
    }

    private AndroidProject requestAndroidModel(IProject project, IProgressMonitor monitor) {

        AndroidProject model;
        Lock lock;

        synchronized (LOCK) {
            ReentrantLock value = new ReentrantLock();
            lock = mRequestLocks.putIfAbsent(project, value);

            if (lock == null) {
                lock = value;
            }
        }

        if (lock.tryLock()) {
            try {
                monitor.beginTask("Requesting model", 1);

                // TODO lock multiple requesting

                ModelRequest<AndroidProject> modelRequest = CorePlugin.toolingClient()
                        .newModelRequest(AndroidProject.class);

                modelRequest.projectDir(project.getLocation().toFile());
                modelRequest.arguments(AndroidProject.PROPERTY_BUILD_MODEL_ONLY_VERSIONED + "="
                        + AndroidProject.MODEL_LEVEL_2_DEP_GRAPH);

                // TODO progress, cancelation

                try {
                    model = modelRequest.executeAndWait();
                } catch (Exception e) {
                    model = null;
                }

                monitor.worked(1);
                monitor.done();

                if (model == null) {
                    return null;
                }

                Collection<Variant> projectVariants = model.getVariants();

                monitor.beginTask("Building sources for IDE", projectVariants.size());

                for (Variant variant : projectVariants) {
                    Set<String> tasks = Collections.singleton(variant.getMainArtifact().getCompileTaskName());
                    //                    Set<String> tasks = variant.getMainArtifact().getIdeSetupTaskNames();

                    System.out.println(tasks);

                    BuildLaunchRequest launchRequest = CorePlugin.toolingClient()
                            .newBuildLaunchRequest(LaunchableConfig.forTasks(tasks));

                    // TODO progress, cancelation
                    launchRequest.projectDir(project.getLocation().toFile());
                    launchRequest.executeAndWait();

                    monitor.worked(1);
                }

                monitor.done();
            } finally {
                lock.unlock();
            }
        } else {
            lock.lock();
            lock.unlock();

            synchronized (LOCK) {
                model = mModels.get(project);
            }
        }

        return model;
    }

    private Gradroid() {}
}
