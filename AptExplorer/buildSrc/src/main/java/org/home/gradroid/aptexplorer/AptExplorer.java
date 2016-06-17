package org.home.gradroid.aptexplorer;

import java.util.ArrayList;
import java.util.Set;

import javax.inject.Inject;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.tooling.provider.model.ToolingModelBuilder;
import org.gradle.tooling.provider.model.ToolingModelBuilderRegistry;
import org.home.gradroid.aptexplorer.model.AptModel;

/**
 * 17 июн. 2016 г.
 *
 * @author dmirochnik
 */
public class AptExplorer implements Plugin<Project> {

    private final ToolingModelBuilderRegistry mRegistry;

    @Inject
    public AptExplorer(ToolingModelBuilderRegistry registry) {
        mRegistry = registry;
    }

    @Override
    public void apply(Project project) {
        mRegistry.register(new AptModelBuilder());
    }

    private static class AptModelBuilder implements ToolingModelBuilder {

        @Override
        public AptModel buildAll(String modelName, Project project) {
            //TODO use android model for full list of possible config names?

            ArrayList<String> jars = new ArrayList<>();

            Configuration config = project.getConfigurations().getByName("apt");
            Set<ResolvedArtifact> resolvedArtifacts = config.getResolvedConfiguration().getResolvedArtifacts();
            resolvedArtifacts.stream().map(a -> a.getFile().getAbsolutePath()).forEach(jars::add);

            return new AptModelImpl(jars);
        }

        @Override
        public boolean canBuild(String modelName) {
            return modelName.equals(AptModel.class.getName());
        }
    }
}
