package org.eclipse.andmore.android.gradle.ui;

import java.util.Arrays;

import org.eclipse.andmore.android.gradle.ui.VariantsView.ProjectData;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;

/**
 * 11 июн. 2016 г.
 *
 * @author denis.mirochnik
 */
public class VariantEditingSupport extends EditingSupport {

    private TableViewer viewer;
    private VariantsView mView;

    public VariantEditingSupport(VariantsView view, TableViewer viewer) {
        super(viewer);
        mView = view;

        this.viewer = viewer;
    }

    @Override
    protected CellEditor getCellEditor(Object element) {
        ProjectData data = (ProjectData) element;
        return new ComboBoxCellEditor(viewer.getTable(), data.variants);
    }

    @Override
    protected boolean canEdit(Object element) {
        return true;
    }

    @Override
    protected Object getValue(Object element) {
        ProjectData data = (ProjectData) element;
        return Arrays.binarySearch(data.variants, data.selectedVariant);
    }

    @Override
    protected void setValue(Object element, Object value) {
        int index = (Integer) value;
        ProjectData data = (ProjectData) element;
        data.selectedVariant = data.variants[index];
        viewer.update(element, null);

        mView.setProjectVariant(data.project, data.selectedVariant);
    }
}
