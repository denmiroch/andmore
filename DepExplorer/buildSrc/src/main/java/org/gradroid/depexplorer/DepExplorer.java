package org.gradroid.depexplorer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.artifacts.component.ModuleComponentIdentifier;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.artifacts.result.ArtifactResult;
import org.gradle.api.artifacts.result.ComponentArtifactsResult;
import org.gradle.api.artifacts.result.ResolvedArtifactResult;
import org.gradle.internal.component.external.model.DefaultModuleComponentIdentifier;
import org.gradle.jvm.JvmLibrary;
import org.gradle.language.base.artifact.SourcesArtifact;
import org.gradle.tooling.provider.model.ToolingModelBuilder;
import org.gradle.tooling.provider.model.ToolingModelBuilderRegistry;
import org.gradroid.depexplorer.model.DepModel;
import org.gradroid.depexplorer.model.DepVariant;

import com.android.builder.model.AndroidLibrary;
import com.android.builder.model.AndroidProject;
import com.android.builder.model.Dependencies;
import com.android.builder.model.Library;
import com.android.builder.model.MavenCoordinates;
import com.android.builder.model.Variant;

/**
 * 17 июн. 2016 г.
 *
 * @author dmirochnik
 */
public class DepExplorer implements Plugin<Project> {

    private final ToolingModelBuilderRegistry mRegistry;

    @Inject
    public DepExplorer(ToolingModelBuilderRegistry registry) {
        mRegistry = registry;
    }

    @Override
    public void apply(Project project) {

        System.out.println("IAMHEREEEEEEEEEEE!!!!!!!!!!!!!!!!!");

        mRegistry.register(new DepModelBuilder(mRegistry));
    }

    private static class DepModelBuilder implements ToolingModelBuilder {

        private ToolingModelBuilderRegistry mRegistry;

        public DepModelBuilder(ToolingModelBuilderRegistry registry) {
            mRegistry = registry;
        }

        @SuppressWarnings({ "deprecation" })
        @Override
        public DepModel buildAll(String modelName, Project project) {
            DependencyHandler dependencyHandler = project.getDependencies();

            ToolingModelBuilder builder = mRegistry.getBuilder(AndroidProject.class.getName());
            AndroidProject androidProject = (AndroidProject) builder.buildAll(AndroidProject.class.getName(), project);

            HashMap<String, String> variantDeps;
            ArrayList<DepVariant> variants = new ArrayList<>();

            for (Variant variant : androidProject.getVariants()) {
                Dependencies dependencies = variant.getMainArtifact().getDependencies();

                variantDeps = new HashMap<>();

                variantDeps.putAll(process(dependencyHandler, dependencies.getJavaLibraries()));
                variantDeps.putAll(process(dependencyHandler, dependencies.getLibraries()));

                variants.add(new DepVariantImpl(variantDeps, variant.getName()));
            }

            ArrayList<String> jars = new ArrayList<>();
            //TODO fine another apt configs from android model
            Configuration config = project.getConfigurations().getByName("apt");

            Set<ResolvedArtifact> resolvedArtifacts = config.getResolvedConfiguration().getResolvedArtifacts();
            resolvedArtifacts.stream().map(a -> a.getFile().getAbsolutePath()).forEach(jars::add);

            return new DepModelImpl(jars, variants);
        }

        private Map<String, String> process(DependencyHandler dependencyHandler,
                Collection<? extends Library> libraries) {

            HashMap<String, String> deps = new HashMap<>();

            for (Library library : libraries) {
                MavenCoordinates coordinates = library.getResolvedCoordinates();
                ModuleComponentIdentifier componentIdentifier = DefaultModuleComponentIdentifier
                        .newId(coordinates.getGroupId(), coordinates.getArtifactId(), coordinates.getVersion());

                String file = query(dependencyHandler, componentIdentifier);

                if (file != null) {
                    deps.put(componentIdentifier.getDisplayName(), file);
                }

                if (library instanceof AndroidLibrary) {
                    deps.putAll(process(dependencyHandler, ((AndroidLibrary) library).getLibraryDependencies()));
                }
            }

            return deps;
        }

        @SuppressWarnings("unchecked")
        private String query(DependencyHandler dependencyHandler, ModuleComponentIdentifier identifier) {
            Set<ComponentArtifactsResult> resolvedComponents = dependencyHandler.createArtifactResolutionQuery()
                    .withArtifacts(JvmLibrary.class, SourcesArtifact.class).forComponents(identifier).execute()
                    .getResolvedComponents();

            ComponentArtifactsResult next = resolvedComponents.iterator().next();
            ArtifactResult result = next.getArtifacts(SourcesArtifact.class).iterator().next();

            if (result instanceof ResolvedArtifactResult) {
                return ((ResolvedArtifactResult) result).getFile().getAbsolutePath();
            }

            return null;
        }

        @Override
        public boolean canBuild(String modelName) {
            return modelName.equals(DepModel.class.getName());
        }
    }
}
