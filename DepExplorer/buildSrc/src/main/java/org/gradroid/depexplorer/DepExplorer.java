package org.gradroid.depexplorer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
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
import org.gradle.language.java.artifact.JavadocArtifact;
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

            HashMap<String, String> variantSources;
            HashMap<String, String> variantJavadocs;
            ArrayList<DepVariant> variants = new ArrayList<>();

            for (Variant variant : androidProject.getVariants()) {
                Dependencies dependencies = variant.getMainArtifact().getDependencies();

                variantSources = new HashMap<>();
                variantJavadocs = new HashMap<>();

                process(dependencyHandler, variantSources, variantJavadocs, dependencies.getJavaLibraries());
                process(dependencyHandler, variantSources, variantJavadocs, dependencies.getLibraries());

                variants.add(new DepVariantImpl(variantSources, variantJavadocs, variant.getName()));
            }

            ArrayList<String> jars = new ArrayList<>();
            //TODO fine another apt configs from android model
            Configuration config = project.getConfigurations().getByName("apt");

            Set<ResolvedArtifact> resolvedArtifacts = config.getResolvedConfiguration().getResolvedArtifacts();
            resolvedArtifacts.stream().map(a -> a.getFile().getAbsolutePath()).forEach(jars::add);

            return new DepModelImpl(jars, variants);
        }

        private void process(DependencyHandler dependencyHandler,
                HashMap<String, String> variantSources, HashMap<String, String> variantJavadocs, Collection<? extends Library> libraries) {

            for (Library library : libraries) {
                MavenCoordinates coordinates = library.getResolvedCoordinates();

                if (coordinates == null) {
                    continue;
                }

                ModuleComponentIdentifier componentIdentifier = DefaultModuleComponentIdentifier
                        .newId(coordinates.getGroupId(), coordinates.getArtifactId(), coordinates.getVersion());

                Para<String, String> files = query(dependencyHandler, componentIdentifier);

                String source = files.getLeft();
                String javadoc = files.getRight();

                if (source != null) {
                    variantSources.put(componentIdentifier.getDisplayName(), source);
                }

                if (javadoc != null) {
                    variantJavadocs.put(componentIdentifier.getDisplayName(), javadoc);
                }

                if (library instanceof AndroidLibrary) {
                    process(dependencyHandler, variantSources, variantJavadocs,
                            ((AndroidLibrary) library).getLibraryDependencies());
                }
            }
        }

        @SuppressWarnings("unchecked")
        private Para<String, String> query(DependencyHandler dependencyHandler, ModuleComponentIdentifier identifier) {
            Set<ComponentArtifactsResult> resolvedComponents = dependencyHandler.createArtifactResolutionQuery()
                    .withArtifacts(JvmLibrary.class, SourcesArtifact.class, JavadocArtifact.class)
                    .forComponents(identifier).execute()
                    .getResolvedComponents();

            if (resolvedComponents.isEmpty()) {
                return Para.of(null, null);
            }

            String source = null;
            String javadoc = null;

            for (ComponentArtifactsResult componentArtifactsResult : resolvedComponents) {

                Iterator<ArtifactResult> iter;

                iter = componentArtifactsResult.getArtifacts(SourcesArtifact.class).iterator();
                if (iter.hasNext()) {
                    ArtifactResult result = iter.next();
                    if (result instanceof ResolvedArtifactResult) {
                        source = ((ResolvedArtifactResult) result).getFile().getAbsolutePath();
                    }
                }

                iter = componentArtifactsResult.getArtifacts(JavadocArtifact.class).iterator();
                if (iter.hasNext()) {
                    ArtifactResult result = iter.next();
                    if (result instanceof ResolvedArtifactResult) {
                        javadoc = ((ResolvedArtifactResult) result).getFile().getAbsolutePath();
                    }
                }

                if (source != null && javadoc != null) {
                    break;
                }
            }

            return Para.of(source, javadoc);
        }

        @Override
        public boolean canBuild(String modelName) {
            return modelName.equals(DepModel.class.getName());
        }
    }
}
