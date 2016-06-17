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
import org.eclipse.andmore.internal.project.LibraryClasspathContainerInitializer;
import org.eclipse.andmore.internal.project.ProjectHelper;
import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.configuration.GradleProjectNature;
import org.eclipse.buildship.core.console.ProcessStreams;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.JavaCore;
import org.gradroid.depexplorer.model.DepModel;

import com.android.builder.model.AndroidArtifact;
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
    //TODO variant decoration?

    public static final String BUILDSHIP_NATURE = GradleProjectNature.ID;
    public static final String ANDMORE_NATURE = AndmoreAndroidConstants.NATURE_DEFAULT;

    private static final Gradroid sInstance = new Gradroid();
    private static final Object LOCK = new Object();

    private static final String PROPERTY_VARIANT_QUALIFIER = "org.eclipse.andmore.gradle.property";
    private static final String PROPERTY_VARIANT_NAME = "variant";

    private HashMap<IProject, Lock> mRequestLocks = new HashMap<IProject, Lock>();
    private HashMap<IProject, AndroidProject> mModels = new HashMap<IProject, AndroidProject>();
    private HashMap<IProject, DepModel> mAptModels = new HashMap<IProject, DepModel>();
    private HashMap<IProject, String> mVariants = new HashMap<IProject, String>();

    private ListenerList mOnProjectModelChangedListeners = new ListenerList();

    public interface OnProjectModelChanged {
        void onProjectModelChanged(IProject project, AndroidProject model);
    }

    public static Gradroid get() {
        return sInstance;
    }

    public void addOnProjectModelChangedListener(OnProjectModelChanged listener) {
        mOnProjectModelChangedListeners.add(listener);
    }

    public void removeOnProjectModelChangedListener(OnProjectModelChanged listener) {
        mOnProjectModelChangedListeners.remove(listener);
    }

    public Set<IProject> getProjects() {
        synchronized (LOCK) {
            return Collections.unmodifiableSet(mModels.keySet());
        }
    }

    public boolean isGradroidProject(IProject project) throws CoreException {
        return project.hasNature(BUILDSHIP_NATURE) && project.hasNature(ANDMORE_NATURE);
    }

    //TODO cache Variant isntance in map
    public Variant getProjectVariant(IProject project) {
        synchronized (LOCK) {
            AndroidProject androidProject = mModels.get(project);
            String variantName = mVariants.get(project);

            if (variantName == null) {
                try {
                    variantName = project.getPersistentProperty(
                            new QualifiedName(PROPERTY_VARIANT_QUALIFIER, PROPERTY_VARIANT_NAME));
                } catch (CoreException e) {}
            }

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
        return getProjectVariant(project).getName();
    }

    private String getProjectVariantNameForModelRequest(IProject project) {
        synchronized (LOCK) {
            String variantName = mVariants.get(project);

            if (variantName == null) {
                try {
                    variantName = project.getPersistentProperty(
                            new QualifiedName(PROPERTY_VARIANT_QUALIFIER, PROPERTY_VARIANT_NAME));
                } catch (CoreException e) {}
            }

            return variantName;
        }
    }

    public void setProjectVariant(IProject project, Variant variant) {
        setProjectVariant(project, variant.getName());
    }

    public void setProjectVariant(final IProject project, String variantName) {
        synchronized (LOCK) {
            String was = mVariants.put(project, variantName);

            try {
                project.setPersistentProperty(new QualifiedName(PROPERTY_VARIANT_QUALIFIER, PROPERTY_VARIANT_NAME),
                        variantName);
            } catch (CoreException e) {
                AndmoreAndroidPlugin.log(e, "");
            }

            class ChangeVariantProjectJob extends Job {

                public ChangeVariantProjectJob() {
                    super("Changing variant");
                }

                @Override
                protected IStatus run(IProgressMonitor monitor) {
                    try {
                        ProjectHelper.fixProject(project, monitor, true);
                    } catch (CoreException e) {
                        AndmoreAndroidPlugin.log(e, "");
                    }

                    LibraryClasspathContainerInitializer.calculateDependencies(JavaCore.create(project), monitor);
                    return Status.OK_STATUS;
                }
            }

            new ChangeVariantProjectJob().schedule();

            if (!variantName.equals(was)) {
                //TODO notify variant changed
                //TODO call fix project?
            }
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

    public DepModel getDepModel(IProject project) {
        synchronized (LOCK) {
            return mAptModels.get(project);
        }
    }

    //TODO make simple method to get model from map without load

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
            mModels.put(project, model);

            Object[] listeners = mOnProjectModelChangedListeners.getListeners();

            for (Object object : listeners) {
                OnProjectModelChanged listener = (OnProjectModelChanged) object;
                listener.onProjectModelChanged(project, model);
            }
        }

        return model;
    }

    //TODO EXCEPTION HANDLING!111
    //TODO request only current cariant
    private AndroidProject requestAndroidModel(IProject project, IProgressMonitor monitor) {

        AndroidProject model;
        DepModel aptModel = null;
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
                monitor.beginTask("Requesting model", 2);

                ModelRequest<AndroidProject> modelRequest = CorePlugin.toolingClient()
                        .newModelRequest(AndroidProject.class);

                ProcessStreams processStreams = CorePlugin.processStreamsProvider().getBackgroundJobProcessStreams();
                modelRequest.standardOutput(processStreams.getOutput());
                modelRequest.standardError(processStreams.getError());
                modelRequest.standardInput(processStreams.getInput());
                modelRequest.arguments("-Pgradroid");

                modelRequest.projectDir(project.getLocation().toFile());
                modelRequest.arguments(AndroidProject.PROPERTY_BUILD_MODEL_ONLY_VERSIONED + "="
                        + AndroidProject.MODEL_LEVEL_2_DEP_GRAPH);

                // TODO progress, cancelation

                try {
                    model = modelRequest.executeAndWait();
                } catch (Exception e) {
                    AndmoreAndroidPlugin.log(e, "");
                    model = null;
                }

                monitor.worked(1);

                ModelRequest<DepModel> depModelRequest = CorePlugin.toolingClient().newModelRequest(DepModel.class);

                processStreams = CorePlugin.processStreamsProvider().getBackgroundJobProcessStreams();
                depModelRequest.standardOutput(processStreams.getOutput());
                depModelRequest.standardError(processStreams.getError());
                depModelRequest.standardInput(processStreams.getInput());
                depModelRequest.projectDir(project.getLocation().toFile());
                depModelRequest.arguments("-Pgradroid");

                // TODO progress, cancelation

                try {
                    aptModel = depModelRequest.executeAndWait();
                } catch (Exception e) {
                    AndmoreAndroidPlugin.log(e, "");
                    aptModel = null;
                }

                monitor.worked(1);
                monitor.done();

                if (model == null) {
                    return null;
                }

                String variantName = getProjectVariantNameForModelRequest(project);
                Collection<Variant> projectVariants = model.getVariants();

                monitor.beginTask("Building sources for IDE", projectVariants.size());

                for (Variant variant : projectVariants) {

                    if (variantName != null) {
                        if (!variant.getName().equals(variantName)){
                            continue;
                        }
                    }

                    //                    Set<String> tasks = Collections.singleton(variant.getMainArtifact().getCompileTaskName());
                    Set<String> tasks = variant.getMainArtifact().getIdeSetupTaskNames();

                    System.out.println(tasks);

                    BuildLaunchRequest launchRequest = CorePlugin.toolingClient()
                            .newBuildLaunchRequest(LaunchableConfig.forTasks(tasks));

                    processStreams = CorePlugin.processStreamsProvider().getBackgroundJobProcessStreams();
                    launchRequest.standardOutput(processStreams.getOutput());
                    launchRequest.standardError(processStreams.getError());
                    launchRequest.standardInput(processStreams.getInput());
                    launchRequest.arguments("-Pgradroid");

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

        synchronized (LOCK) {
            mAptModels.put(project, aptModel);
        }

        return model;
    }

    public void assemble(IProject project, IProgressMonitor monitor) {

        monitor.beginTask("Assemble", 1);

        Variant variant = getProjectVariant(project);
        AndroidArtifact mainArtifact = variant.getMainArtifact();
        String assembleTaskName = mainArtifact.getAssembleTaskName();
        BuildLaunchRequest buildRequest = CorePlugin.toolingClient()
                .newBuildLaunchRequest(LaunchableConfig.forTasks(assembleTaskName));

        ProcessStreams processStreams = CorePlugin.processStreamsProvider().getBackgroundJobProcessStreams();
        buildRequest.standardOutput(processStreams.getOutput());
        buildRequest.standardError(processStreams.getError());
        buildRequest.standardInput(processStreams.getInput());
        buildRequest.arguments("-Pgradroid");

        buildRequest.projectDir(project.getLocation().toFile());
        buildRequest.executeAndWait();

        monitor.worked(1);
        monitor.done();
    }

    private Gradroid() {}
}
