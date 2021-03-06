/*
 * Copyright (C) 2012 The Android Open Source Project
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eclipse.andmore.android.propertypage;

import org.eclipse.andmore.android.AndroidPlugin;
import org.eclipse.andmore.android.common.log.AndmoreLogger;
import org.eclipse.andmore.android.common.utilities.EclipseUtils;
import org.eclipse.andmore.android.i18n.AndroidNLS;
import org.eclipse.andmore.android.multidex.MultiDexManager;
import org.eclipse.andmore.android.obfuscate.ObfuscatorManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PropertyPage;

/**
 * Andmore properties (obfuscate)
 */
public class AndmorePropertyPage extends PropertyPage implements IWorkbenchPropertyPage {
    private final String PROPERTY_PAGE_HELP = AndroidPlugin.PLUGIN_ID + ".obuscation_property"; //$NON-NLS-1$

    private Button obfuscateCkbox;
    private Button multiDexCkbox;

    private IProject project = null;

    private void addObfuscateSection(Composite parent) {
        Composite group = createDefaultComposite(parent, AndroidNLS.UI_ProjectPropertyPage_ObfuscateGroup);

        Composite composite = new Composite(group, SWT.NULL);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        composite.setLayout(layout);

        GridData data = new GridData();
        data.verticalAlignment = GridData.FILL;
        data.horizontalAlignment = GridData.FILL;
        composite.setLayoutData(data);

        obfuscateCkbox = new Button(composite, SWT.CHECK);

        setDefaultObfuscate();

        Label obfuscateLabel = new Label(composite, SWT.NONE);
        obfuscateLabel.setText(AndroidNLS.UI_ProjectPropertyPage_Obfuscate);

        obfuscateCkbox.addSelectionListener(new SelectionAdapter() {
            /*
             * (non-Javadoc)
             * 
             * @see
             * org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse
             * .swt.events.SelectionEvent)
             */
            @Override
            public void widgetSelected(SelectionEvent e) {
                super.widgetSelected(e);

                boolean showWarningMessage = false;
                if (obfuscateCkbox.getSelection()) {
                    if ((project != null) && project.getLocation().toOSString().contains(" ")) //$NON-NLS-1$
                    {
                        showWarningMessage = true;
                    }
                }
                if (showWarningMessage) {
                    setMessage(AndroidNLS.WRN_Obfuscation_ProjectLocationContainWhitespaces, IMessageProvider.WARNING);

                } else {
                    setMessage("Eclipse Andmore"); //$NON-NLS-1$
                }
            }
        });
    }

    private void addMultiDexSection(Composite parent) {
        Composite group = createDefaultComposite(parent, AndroidNLS.UI_ProjectPropertyPage_MultiDexGroup);

        Composite composite = new Composite(group, SWT.NULL);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        composite.setLayout(layout);

        GridData data = new GridData();
        data.verticalAlignment = GridData.FILL;
        data.horizontalAlignment = GridData.FILL;
        composite.setLayoutData(data);

        multiDexCkbox = new Button(composite, SWT.CHECK);

        setDefaultMultiDex();

        Label obfuscateLabel = new Label(composite, SWT.NONE);
        obfuscateLabel.setText(AndroidNLS.UI_ProjectPropertyPage_MultiDex);

        multiDexCkbox.addSelectionListener(new SelectionAdapter() {
            /*
             * (non-Javadoc)
             * 
             * @see
             * org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse
             * .swt.events.SelectionEvent)
             */
            @Override
            public void widgetSelected(SelectionEvent e) {
                super.widgetSelected(e);

                boolean showWarningMessage = false;
                if (multiDexCkbox.getSelection()) {
                    if ((project != null) && project.getLocation().toOSString().contains(" ")) //$NON-NLS-1$
                    {
                        showWarningMessage = true;
                    }
                }
                if (showWarningMessage) {
                    setMessage(AndroidNLS.WRN_Obfuscation_ProjectLocationContainWhitespaces, IMessageProvider.WARNING);

                } else {
                    setMessage("Eclipse Andmore"); //$NON-NLS-1$
                }
            }
        });
    }

    /**
     * Checks if project have Proguard settings and, if so, update checkbox
     * state
     */
    private void setDefaultObfuscate() {
        project = null;
        if (getElement() instanceof IResource) {
            IResource resource = (IResource) getElement();
            if (resource != null) {
                project = resource.getProject();
            }
        } else if (getElement() instanceof JavaProject) {
            JavaProject javaProject = (JavaProject) getElement();
            project = javaProject.getProject();
        }

        if (project != null) {
            obfuscateCkbox.setSelection(ObfuscatorManager.isProguardSet(project));
        } else {
            // project not found
            obfuscateCkbox.setSelection(false);
        }
    }

    /**
     * Checks if project has MultiDex settings and, if so, update checkbox
     * state
     */
    private void setDefaultMultiDex() {
        project = null;
        if (getElement() instanceof IResource) {
            IResource resource = (IResource) getElement();
            if (resource != null) {
                project = resource.getProject();
            }
        } else if (getElement() instanceof JavaProject) {
            JavaProject javaProject = (JavaProject) getElement();
            project = javaProject.getProject();
        }

        if (project != null) {
            multiDexCkbox.setSelection(MultiDexManager.isMultiDexEnabled(project));
        } else {
            // project not found
            multiDexCkbox.setSelection(false);
        }
    }

    /**
     * @see PreferencePage#createContents(Composite)
     */
    @Override
    protected Control createContents(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        composite.setLayout(layout);
        GridData data = new GridData(GridData.FILL);
        data.grabExcessHorizontalSpace = true;
        composite.setLayoutData(data);

        addObfuscateSection(composite);
        addMultiDexSection(composite);

        PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, PROPERTY_PAGE_HELP);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, PROPERTY_PAGE_HELP);

        return composite;
    }

    private Composite createDefaultComposite(Composite parent, String groupText) {
        GridData data = new GridData(SWT.FILL, SWT.NONE, true, false);
        Group groupForComposite = new Group(parent, SWT.SHADOW_ETCHED_IN);
        groupForComposite.setLayout(new GridLayout());
        groupForComposite.setLayoutData(data);
        groupForComposite.setFont(parent.getFont());
        groupForComposite.setText(groupText);

        return groupForComposite;
    }

    @Override
    protected void performDefaults() {
        super.performDefaults();
        setDefaultObfuscate();
        setDefaultMultiDex();
    }

    /**
     * Add or remove Proguard setting depending on obfuscateCkbox checkbox state
     */
    @Override
    public boolean performOk() {
        IProject project = null;

        if (getElement() instanceof IResource) {
            IResource resource = (IResource) getElement();
            if (resource != null) {
                project = resource.getProject();
            }
        } else if (getElement() instanceof JavaProject) {
            JavaProject javaProject = (JavaProject) getElement();
            project = javaProject.getProject();
        }
        if (project != null) {
            IStatus status = null;
            try {
                // check obfuscate state
                Boolean needToObfuscate = obfuscateCkbox.getSelection();
                if (needToObfuscate.booleanValue()) {
                    status = ObfuscatorManager.obfuscate(project, null);
                } else {
                    status = ObfuscatorManager.unobfuscate(project);
                }

                // check obfuscate state
                Boolean enableMultiDex = multiDexCkbox.getSelection();
                if (enableMultiDex.booleanValue()) {
                    status = MultiDexManager.enableMultiDex(project, null);
                } else {
                    status = MultiDexManager.disableMultiDex(project);
                }

                project.refreshLocal(IResource.DEPTH_INFINITE, null);
            } catch (Exception e) {
                EclipseUtils.showErrorDialog(AndroidNLS.AndmorePropertyPage_ChangeProguardSettingsProblem,
                        status.getMessage(), status);
                AndmoreLogger.error(AndmorePropertyPage.class, e.getMessage(), e);
                return false;
            }
        }

        return true;
    }
}
