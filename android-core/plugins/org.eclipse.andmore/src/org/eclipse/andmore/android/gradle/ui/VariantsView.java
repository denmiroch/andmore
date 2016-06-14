package org.eclipse.andmore.android.gradle.ui;

import java.util.Collection;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.andmore.AndmoreAndroidPlugin;
import org.eclipse.andmore.android.gradle.Gradroid;
import org.eclipse.andmore.android.gradle.Gradroid.OnProjectModelChanged;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

import com.android.builder.model.AndroidProject;
import com.android.builder.model.Variant;

/**
 * This sample class demonstrates how to plug-in a new
 * workbench view. The view shows data obtained from the
 * model. The sample creates a dummy model on the fly,
 * but a real implementation would connect to the model
 * available either in this or another plug-in (e.g. the workspace).
 * The view is connected to the model using a content provider.
 * <p>
 * The view uses a label provider to define how model
 * objects should be presented in the view. Each
 * view can present the same model objects using
 * different labels and icons, if needed. Alternatively,
 * a single label provider can be shared between views
 * in order to ensure that objects of the same type are
 * presented in the same way everywhere.
 * <p>
 */

public class VariantsView extends ViewPart implements OnProjectModelChanged {

    /**
     * The ID of the view as specified by the extension.
     */
    public static final String ID = "org.eclipse.andmore.android.gradle.VariantsView";

    private TableViewer viewer;

    static class ProjectData {
        public final IProject project;
        public final String[] variants;
        public String selectedVariant;

        public ProjectData(IProject project, String[] variants, String selectedVariant) {
            this.project = project;
            this.variants = variants;
            this.selectedVariant = selectedVariant;
        }
    }

    /**
     * The constructor.
     */
    public VariantsView() {}

    @Override
    public void init(IViewSite site) throws PartInitException {
        super.init(site);

        Gradroid.get().addOnProjectModelChangedListener(this);
    }

    /**
     * This is a callback that will allow us
     * to create the viewer and initialize it.
     */
    @Override
    public void createPartControl(Composite parent) {
        viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);

        Table table = viewer.getTable();

        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        TableViewerColumn projectViewerColumn = new TableViewerColumn(viewer, SWT.NONE);
        TableColumn projectColumn = projectViewerColumn.getColumn();
        projectColumn.setText("Project");
        projectColumn.setWidth(100);
        projectColumn.setResizable(true);
        projectColumn.setMoveable(false);
        projectViewerColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                ProjectData data = (ProjectData) element;
                return data.project.getName();
            }
        });

        TableViewerColumn variantViewerColumn = new TableViewerColumn(viewer, SWT.NONE);
        TableColumn variantColumn = variantViewerColumn.getColumn();
        variantColumn.setWidth(100);
        variantColumn.setText("Variant");
        variantColumn.setResizable(true);
        variantColumn.setMoveable(false);
        variantViewerColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                ProjectData data = (ProjectData) element;
                return data.selectedVariant;
            }
        });
        variantViewerColumn.setEditingSupport(new VariantEditingSupport(this, viewer));

        viewer.setContentProvider(ArrayContentProvider.getInstance());

        update();

        getSite().setSelectionProvider(viewer);
    }

    /**
     * Passing the focus request to the viewer's control.
     */
    @Override
    public void setFocus() {
        viewer.getControl().setFocus();
    }

    @Override
    public void dispose() {
        super.dispose();

        Gradroid.get().removeOnProjectModelChangedListener(this);
    }

    void setProjectVariant(IProject project, String variantName) {
        Gradroid.get().setProjectVariant(project, variantName);
    }

    private void update() {

        Set<IProject> projects = Gradroid.get().getProjects();
        final ProjectData[] data = new ProjectData[projects.size()];

        int i = 0;
        for (IProject project : projects) {
            AndroidProject model = Gradroid.get().loadAndroidModel(project, new NullProgressMonitor());
            Variant projectVariant = Gradroid.get().getProjectVariant(project);
            Collection<Variant> variants = model.getVariants();

            Set<String> variantsNamesSet = variants.stream().map(new Function<Variant, String>() {
                @Override
                public String apply(Variant v) {
                    return v.getName();
                }
            }).collect(Collectors.<String> toSet());

            String[] names = variantsNamesSet.toArray(new String[variantsNamesSet.size()]);
            data[i++] = new ProjectData(project, names, projectVariant.getName());
        }

        AndmoreAndroidPlugin.getDisplay().syncExec(new Runnable() {

            @Override
            public void run() {
                viewer.setInput(data);
            }
        });
    }

    @Override
    public void onProjectModelChanged(IProject project, AndroidProject model) {
        update();
    }

}
