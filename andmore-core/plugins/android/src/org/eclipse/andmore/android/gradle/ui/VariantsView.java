package org.eclipse.andmore.android.gradle.ui;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

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

public class VariantsView extends ViewPart {

    /**
     * The ID of the view as specified by the extension.
     */
    public static final String ID = "org.eclipse.andmore.android.gradle.VariantsView";

    private TableViewer viewer;
    private Action action1;
    private Action action2;

    public static class ProjectVariants {
        public String name;
        public String[] variants;
        public int selected = 0;

        @Override
        public String toString() {
            return name;
        }
    }

    private ProjectVariants[] mData;

    {
        mData = new ProjectVariants[2];

        ProjectVariants variants;

        variants = new ProjectVariants();
        variants.name = "ProjectOne";
        variants.variants = new String[] { "VariantOne", "VarianteTwo" };
        mData[0] = variants;

        variants = new ProjectVariants();
        variants.name = "ProjectTwo";
        variants.variants = new String[] { "VariantOne", "VarianteTwo" };
        mData[1] = variants;
    }

    /**
     * The constructor.
     */
    public VariantsView() {}

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
        projectViewerColumn.setLabelProvider(new ColumnLabelProvider());

        TableViewerColumn variantViewerColumn = new TableViewerColumn(viewer, SWT.NONE);
        TableColumn variantColumn = variantViewerColumn.getColumn();
        variantColumn.setWidth(100);
        variantColumn.setText("Variant");
        variantColumn.setResizable(true);
        variantColumn.setMoveable(false);
        variantViewerColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                ProjectVariants variants = (ProjectVariants) element;
                return super.getText(variants.variants[variants.selected]);
            }
        });
        variantViewerColumn.setEditingSupport(new VariantEditingSupport(viewer));

        viewer.setContentProvider(ArrayContentProvider.getInstance());
        viewer.setInput(mData);

        getSite().setSelectionProvider(viewer);

        makeActions();
        hookContextMenu();

        contributeToActionBars();
    }

    private void hookContextMenu() {
        MenuManager menuMgr = new MenuManager("#PopupMenu");

        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(new IMenuListener() {
            @Override
            public void menuAboutToShow(IMenuManager manager) {
                VariantsView.this.fillContextMenu(manager);
            }
        });

        Menu menu = menuMgr.createContextMenu(viewer.getControl());

        viewer.getControl().setMenu(menu);

        getSite().registerContextMenu(menuMgr, viewer);
    }

    private void contributeToActionBars() {

        IActionBars bars = getViewSite().getActionBars();

        fillLocalPullDown(bars.getMenuManager());
        fillLocalToolBar(bars.getToolBarManager());
    }

    private void fillLocalPullDown(IMenuManager manager) {
        manager.add(action1);
        manager.add(new Separator());
        manager.add(action2);
    }

    private void fillContextMenu(IMenuManager manager) {
        manager.add(action1);
        manager.add(action2);
        // Other plug-ins can contribute there actions here
        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
    }

    private void fillLocalToolBar(IToolBarManager manager) {
        manager.add(action1);
        manager.add(action2);
    }

    private void makeActions() {
        action1 = new Action() {
            @Override
            public void run() {
                showMessage("Action 1 executed");
            }
        };
        action1.setText("Action 1");
        action1.setToolTipText("Action 1 tooltip");
        action1.setImageDescriptor(
                PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));

        action2 = new Action() {
            @Override
            public void run() {
                showMessage("Action 2 executed");
            }
        };
        action2.setText("Action 2");
        action2.setToolTipText("Action 2 tooltip");
        action2.setImageDescriptor(
                PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
    }

    private void showMessage(String message) {
        MessageDialog.openInformation(viewer.getControl().getShell(), "Variants", message);
    }

    /**
     * Passing the focus request to the viewer's control.
     */
    @Override
    public void setFocus() {
        viewer.getControl().setFocus();
    }
}
