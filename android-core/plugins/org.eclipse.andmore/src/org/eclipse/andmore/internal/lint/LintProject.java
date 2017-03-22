package org.eclipse.andmore.internal.lint;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.eclipse.andmore.AdtUtils;
import org.eclipse.andmore.android.gradle.Gradroid;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

import com.android.builder.model.AndroidLibrary;
import com.android.builder.model.AndroidProject;
import com.android.builder.model.Variant;
import com.android.sdklib.AndroidVersion;
import com.android.sdklib.IAndroidTarget;
import com.android.tools.lint.client.api.LintClient;
import com.android.tools.lint.detector.api.Project;

/**
 * 22 мар. 2017 г.
 *
 * @author denis.mirochnik
 */
public class LintProject extends Project {

    private IProject mProject;

    public LintProject(LintClient client, File dir, File referenceDir) {
        super(client, dir, referenceDir);
    }

    public void setProject(IProject project) {
        mProject = project;
    }

    @Override
    public boolean isGradleProject() {
        try {
            return Gradroid.get().isGradroidProject(mProject);
        } catch (CoreException e) {
            return false;
        }
    }

    @Override
    public boolean isAndroidProject() {
        return super.isAndroidProject();
    }

    @Override
    public boolean isLibrary() {
        return super.isLibrary();
    }

    @Override
    public AndroidLibrary getGradleLibraryModel() {
        return super.getGradleLibraryModel();
    }

    @Override
    public AndroidProject getGradleProjectModel() {
        return Gradroid.get().loadAndroidModel(mProject, new NullProgressMonitor());
    }

    @Override
    public List<File> getGradleBuildScripts() {
        return super.getGradleBuildScripts();
    }

    @Override
    public Variant getCurrentVariant() {
        return Gradroid.get().getProjectVariant(mProject);
    }

    @Override
    public List<File> getManifestFiles() {
        // TODO or maybe original manifests?
        // return
        // Collections.singletonList(AdtUtils.workspacePathToFile(ProjectHelper.getManifest(mProject).getFullPath()));
        return Collections.singletonList(getGradleProjectModel().getDefaultConfig()
                                                                .getSourceProvider()
                                                                .getManifestFile());
    }

    @Override
    public int getMinSdk() {
        return getGradleProjectModel().getDefaultConfig().getProductFlavor().getMinSdkVersion().getApiLevel();
    }

    @Override
    public AndroidVersion getMinSdkVersion() {
        return AdtUtils.getAndroidVersionForApi(getMinSdk());
    }

    @Override
    public int getTargetSdk() {
        return getGradleProjectModel().getDefaultConfig().getProductFlavor().getTargetSdkVersion().getApiLevel();
    }

    @Override
    public AndroidVersion getTargetSdkVersion() {
        return AdtUtils.getAndroidVersionForApi(getTargetSdk());
    }

    @Override
    public int getBuildSdk() {
        return super.getBuildSdk();
    }

    @Override
    public IAndroidTarget getBuildTarget() {
        return super.getBuildTarget();
    }
}
