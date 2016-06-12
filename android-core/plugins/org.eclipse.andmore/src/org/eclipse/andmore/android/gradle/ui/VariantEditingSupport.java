package org.eclipse.andmore.android.gradle.ui;

import org.eclipse.andmore.android.gradle.ui.VariantsView.ProjectVariants;
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

    public VariantEditingSupport(TableViewer viewer) {
        super(viewer);

        this.viewer = viewer;
    }

    @Override
    protected CellEditor getCellEditor(Object element) {
        ProjectVariants variants = (ProjectVariants) element;
        return new ComboBoxCellEditor(viewer.getTable(), variants.variants);
    }

    @Override
    protected boolean canEdit(Object element) {
        return true;
    }

    @Override
    protected Object getValue(Object element) {
        ProjectVariants variants = (ProjectVariants) element;
        return variants.selected;
    }

    @Override
    protected void setValue(Object element, Object value) {
        ProjectVariants variants = (ProjectVariants) element;
        variants.selected = (Integer) value;
        viewer.update(element, null);
    }
}
