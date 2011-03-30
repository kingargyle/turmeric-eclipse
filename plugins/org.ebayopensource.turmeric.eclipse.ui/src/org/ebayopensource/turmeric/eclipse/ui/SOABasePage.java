/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *******************************************************************************/
package org.ebayopensource.turmeric.eclipse.ui;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.ebayopensource.turmeric.eclipse.core.resources.constants.SOAProjectConstants;
import org.ebayopensource.turmeric.eclipse.logging.SOALogger;
import org.ebayopensource.turmeric.eclipse.repositorysystem.core.GlobalRepositorySystem;
import org.ebayopensource.turmeric.eclipse.repositorysystem.core.ISOAHelpProvider;
import org.ebayopensource.turmeric.eclipse.repositorysystem.core.ISOAOrganizationProvider;
import org.ebayopensource.turmeric.eclipse.ui.components.SOACComboControlAdapter;
import org.ebayopensource.turmeric.eclipse.ui.resources.SOAConstants;
import org.ebayopensource.turmeric.eclipse.ui.resources.SOAMessages;
import org.ebayopensource.turmeric.eclipse.utils.ui.UIUtil;
import org.ebayopensource.turmeric.eclipse.utils.ui.UIUtil.ISOAControlDecorator;
import org.ebayopensource.turmeric.eclipse.validator.utils.ValidateUtil;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.fieldassist.AutoCompleteField;
import org.eclipse.jface.fieldassist.ComboContentAdapter;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.osgi.service.prefs.BackingStoreException;


/**
 * This is the base class for all SOA Wizard pages extending the standard
 * eclipse wizard page. Contains the Common code for all SOA Wizard pages.
 * Mainly contains some work space related functionality and some UI component
 * common creation logic. But it can contain anything common to the SOA Wizard
 * pages. Common error UI is another key functionality of this class.
 * 
 * @author smathew
 */
public abstract class SOABasePage extends WizardPage implements ISOAControlDecorator {
	private final Map<Control, ControlDecoration> errorDecorations 
	= new ConcurrentHashMap<Control, ControlDecoration>();
	private final Map<Control, ControlDecoration> controlDecorations 
	= new ConcurrentHashMap<Control, ControlDecoration>();
	private ISOAOrganizationProvider organizationProvider = null;
	
	public static final String DEFAULT_TEXT_VALUE = SOAProjectConstants.EMPTY_STRING;
	
	@Override
	public void dispose() {
		super.dispose();
		for (ControlDecoration dec : errorDecorations.values()) {
			dec.dispose();
		}
		errorDecorations.clear();
		for (ControlDecoration dec : controlDecorations.values()) {
			dec.dispose();
		}
		controlDecorations.clear();
	}

	public Map<Control, ControlDecoration> getErrorDecorations() {
		return errorDecorations;
	}

	public Map<Control, ControlDecoration> getControlDecorations() {
		return controlDecorations;
	}


	/**
	 * Saves the provided work space root to the preference store. Once user
	 * provides the work space root, SOA remembers it and will show it as the
	 * default value next time user opens the wizard.
	 * 
	 * @param workspaceRoot
	 */
	public static void saveWorkspaceRoot(final String workspaceRoot) {
		final IEclipsePreferences preferences = new InstanceScope()
				.getNode(UIActivator.PLUGIN_ID);
		preferences.put(SOAConstants.WORKSPACE_ROOT, workspaceRoot);
		try {
			preferences.flush();

		} catch (final BackingStoreException e) {

		}
	}

	/**
	 * Constructor with no extra logic, Just calls the super.
	 * 
	 * @param pageName
	 */
	protected SOABasePage(String pageName) {
		super(pageName);
	}

	/**
	 * @param pageName -
	 *            Name of the page. Shown in the UI
	 * @param title -
	 *            Title, Again shown in the UI under the name typically.
	 * @param description
	 */
	protected SOABasePage(String pageName, String title, String description) {
		this(pageName);
		setTitle(title);
		setDescription(description);
	}

	/**
	 * Returns the work space root stored in the preference store.
	 * 
	 * @return
	 */
	public static String getWorkspaceRoot() {
		final IEclipsePreferences preferences = new InstanceScope()
				.getNode(UIActivator.PLUGIN_ID);
		return preferences
				.get(SOAConstants.WORKSPACE_ROOT, SOAProjectConstants.EMPTY_STRING);
	}
	
	public void updatePageStatus(final Control control, final IStatus status) {
		updatePageStatus(status, control);
	}

	/**
	 * Update the message of the wizard page along with the appropriate icon. If
	 * the status is not OK, then the page will be marked as not completed.
	 * 
	 * @param status
	 */
	public void updatePageStatus(final IStatus status, Control... controls) {
		String message = null;
		int messageType = WizardPage.NONE;
		if (status != null) {
			switch (status.getSeverity()) {
			case IStatus.WARNING:
				messageType = WizardPage.WARNING;
				break;
			case IStatus.INFO:
				messageType = WizardPage.INFORMATION;
				break;
			case IStatus.ERROR:
				messageType = WizardPage.ERROR;
				break;
			}
			if (status != null) {
				message = ValidateUtil.getBasicFormattedUIErrorMessage(status);
			}
		}
		if (messageType == WizardPage.ERROR) {
			setErrorMessage(message);
			updateStatus(message, controls);
		} else {
			updateStatus(null);
			setMessage(message, messageType);
		}
		setPageComplete(status == null || status.getSeverity() != IStatus.ERROR);
	}

	/**
	 * Update the status of the wizard page. Sets the message, error message and
	 * decides the page complete status based on the message passed.
	 * 
	 * @param message
	 *            Non-blank message messages error state, or null indicates OK.
	 */
	public void updateStatus(final String message) {
		updateStatus(message, (Control)null);
	}
	
	public void updateStatus(final Control control, final String message) {
		updateStatus(message, control);
	}
	
	public void updateStatus(final String message, final Control... controls) {
		for (ControlDecoration decoration : this.errorDecorations.values()) {
			decoration.hide();
			decoration.setDescriptionText(null);
		}
		for (ControlDecoration decoration : this.controlDecorations.values()) {
			decoration.show();
		}
		if (controls != null) {
			for (Control control : controls) {
				if (control != null) {
					ControlDecoration controlDecoration = this.errorDecorations.get(control);
					if (controlDecoration == null) {
						controlDecoration = new ControlDecoration(control, 
								SWT.LEFT | SWT.TOP);
						FieldDecoration fieldDecoration = FieldDecorationRegistry
						.getDefault().getFieldDecoration(
								FieldDecorationRegistry.DEC_ERROR);
						controlDecoration.setImage(fieldDecoration.getImage());
					} else {
						controlDecoration.show();
					}

					controlDecoration.setDescriptionText(message);
					this.errorDecorations.put(control, controlDecoration);
					if (controlDecorations.containsKey(control)) {
						controlDecorations.get(control).hide();
					}
				}
			}
		}
		setMessage(message);
		setErrorMessage(message);
		setPageComplete(message == null);
	}

	/**
	 * Creates the parent container. Additionally sets the help context id also.
	 * 
	 * @param parent
	 * @param columnCount
	 * @return
	 */
	protected Composite createParentControl(Composite parent, int columnCount) {
		final Composite container = new Composite(parent, SWT.NONE);
		if (columnCount < 1) {
			columnCount = 1;
		}
		GridLayout layout = new GridLayout(columnCount, false);
		layout.verticalSpacing = 8;
		layout.marginLeft = 5;
		container.setLayout(layout);

		setControl(container);
		UIUtil.getHelpSystem().setHelp(container, getHelpContextID());
		return container;
	}

	/**
	 * Create the options specification widgets.
	 * 
	 * @param parent
	 *            org.eclipse.swt.widgets.Composite
	 */
	protected void createOptionsGroup(Composite parent, String tooltip) {
		Composite panel = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 1;
		layout.marginWidth = 1;
		layout.numColumns = 1;
		panel.setLayout(layout);

		panel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));

		Group optionsGroup = new Group(panel, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 1;
		optionsGroup.setLayout(layout);
		optionsGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		optionsGroup.setText(SOAMessages.OPTIONS);
		optionsGroup.setFont(parent.getFont());
		UIUtil.decorateControl(this, optionsGroup, tooltip);
	}

	/**
	 * Create an empty label widget.
	 * 
	 * @param composite
	 * @param columnCount
	 * @return
	 */
	protected Label createEmptyLabel(Composite composite, int columnCount) {
		Label label = new Label(composite, SWT.NONE);
		GridData gridData = new GridData(SWT.BEGINNING);
		gridData.horizontalAlignment = columnCount;
		label.setLayoutData(gridData);
		return label;
	}

	/**
	 * @param composite
	 * @param labelText
	 * @param defaultText
	 * @param modifyListener
	 * @return
	 */
	protected Text createLabelTextField(final Composite composite,
			final String labelText, final String defaultText,
			final ModifyListener modifyListener, final String tooltip) {
		return createLabelTextField(composite, labelText, defaultText,
				modifyListener, true, true, tooltip);
	}

	/**
	 * @param composite
	 * @param labelText
	 * @param defaultText
	 * @param modifyListener
	 * @param textEditable
	 * @return
	 */
	protected Text createLabelTextField(final Composite composite,
			final String labelText, final String defaultText,
			final ModifyListener modifyListener, final boolean textEditable, 
			final String tooltip) {
		return createLabelTextField(composite, labelText, defaultText,
				modifyListener, true, textEditable, -1, tooltip);
	}

	/**
	 * @param composite
	 * @param labelText
	 * @param defaultText
	 * @param modifyListener
	 * @param needEmptyLabel
	 * @param textEditable
	 * @return
	 */
	protected Text createLabelTextField(final Composite composite,
			final String labelText, final String defaultText,
			final ModifyListener modifyListener, final boolean needEmptyLabel,
			final boolean textEditable, final String tooltip) {
		return createLabelTextField(composite, labelText, defaultText,
				modifyListener, needEmptyLabel, textEditable, -1, tooltip);
	}

	/**
	 * @param composite
	 * @param labelText
	 * @param defaultText
	 * @param modifyListener
	 * @param needEmptyLabel
	 * @param textEditable
	 * @param textStyle
	 * @return
	 */
	protected Text createLabelTextField(final Composite composite,
			final String labelText, final String defaultText,
			final ModifyListener modifyListener, final boolean needEmptyLabel,
			final boolean textEditable, final int textStyle, 
			final String tooltip) {
		final Label label = new Label(composite, SWT.LEFT);
		label.setText(labelText);
		final Text textField = new Text(composite, textStyle != -1 ? textStyle
				: SWT.BORDER | SWT.SINGLE);
		textField.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false,
				2, 1));
		textField.setEditable(textEditable);
		if (modifyListener != null)
			textField.addModifyListener(modifyListener);
		if (defaultText != null)
			textField.setText(defaultText);
		UIUtil.decorateControl(this, textField, tooltip);
		if (needEmptyLabel)
			createEmptyLabel(composite, 1);
		
		return textField;
	}

	/**
	 * Create an Override button for the given Text field.
	 * 
	 * @param parent
	 * @param relatedText
	 * @param listener
	 * @return
	 */
	protected Button createButton(final Composite parent, String label, String tooltip) {
		final Button button = new Button(parent, SWT.CHECK);
		button.setAlignment(SWT.LEFT);
		button.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 4, 1));
		button.setText(label);
		button.setSelection(true);
		UIUtil.decorateControl(this, button, tooltip);
		return button;
	}

	/**
	 * Create an Override button for the given Text field.
	 * 
	 * @param parent
	 * @param relatedText
	 * @param listener
	 * @return
	 */
	protected Button createOverrideButton(final Composite parent,
			final Text relatedText, final SelectionListener listener) {
		final Button overrideButton = new Button(parent, SWT.CHECK);
		overrideButton.setAlignment(SWT.LEFT);
		overrideButton.setText(SOAMessages.OVERRIDE);
		overrideButton.setSelection(false);
		if (listener == null) {
			final SelectionListener overrideInterfaceListener = new SelectionListener() {
				public void widgetDefaultSelected(final SelectionEvent e) {
					widgetSelected(e);
				}

				public void widgetSelected(final SelectionEvent e) {
					if (overrideButton.getSelection()) {
						relatedText.setEnabled(true);
						relatedText.setEditable(true);
					} else {
						relatedText.setEditable(false);
						relatedText.setText(getDefaultValue(relatedText));
						dialogChanged();
					}
				}
			};
			overrideButton.addSelectionListener(overrideInterfaceListener);
		} else {
			overrideButton.addSelectionListener(listener);
		}
		UIUtil.decorateControl(this, overrideButton, SOAMessages.OVERRIDE);

		return overrideButton;
	}
	
	public void addControlDecoration(Control control, ControlDecoration controlDecoration) {
		if (control != null && controlDecoration != null
				&& controlDecoration.getControl() == control) {
			controlDecorations.put(control, controlDecoration);
		}
	}

	/**
	 * Create a combo box widget in standard SOA dimension and style.
	 * 
	 * @param composite
	 * @param labelText
	 * @param editable
	 * @param items
	 * @param horizontalSpan
	 * @return
	 */
	public CCombo createCCombo(final Composite composite,
			final String labelText, final boolean editable, final String[] items, 
			final String tooltip) {

		final Label label = new Label(composite, SWT.LEFT);
		label.setText(labelText);
		final int defaultStyle = SWT.BORDER | SWT.DROP_DOWN;
		final int style = editable ? defaultStyle : SWT.READ_ONLY | defaultStyle;
		final CCombo combo = new CCombo(composite, style);
		if (editable == false) {
			// we still want it look like modifiable although it is ready only.
			combo.setBackground(UIUtil.display()
					.getSystemColor(SWT.COLOR_LIST_BACKGROUND));
		} else {
			combo.setTextLimit(100);
		}
		combo.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 
				3, 1));
		if (items != null && items.length > 0) {
			combo.setItems(items);
			combo.select(0);
			if (editable == true)
				new AutoCompleteField(combo, new SOACComboControlAdapter(), items);
		}
		UIUtil.decorateControl(this, combo, tooltip);
		return combo;
	}
	
	public Combo createCombo(final Composite composite,
			final String labelText, final boolean editable, final String[] items, 
			final String tooltip) {

		final Label label = new Label(composite, SWT.LEFT);
		label.setText(labelText);
		final int defaultStyle = SWT.BORDER | SWT.DROP_DOWN;
		final int style = editable ? defaultStyle : SWT.READ_ONLY | defaultStyle;
		final Combo combo = new Combo(composite, style);
		if (editable == false) {
			// we still want it look like modifiable although it is ready only.
			combo.setBackground(UIUtil.display()
					.getSystemColor(SWT.COLOR_LIST_BACKGROUND));
		} else {
			combo.setTextLimit(100);
		}
		combo.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 
				3, 1));
		if (items != null && items.length > 0) {
			combo.setItems(items);
			combo.select(0);
			if (editable == true)
				new AutoCompleteField(combo, new ComboContentAdapter(), items);
		}
		UIUtil.decorateControl(this, combo, tooltip);
		return combo;
	}

	/**
	 * Validate the state of the current wizard page
	 * 
	 * @return true is passed validation of false otherwise
	 */
	protected boolean dialogChanged() {
		updateStatus(null);
		return true;
	}

	/**
	 * This is a poorly named method. It is not just checking the validation
	 * result, but it is displaying the status object. It checks if severity is
	 * error and if it is then it will display it in the standard message area
	 * of the wizard or ignore it otherwise.
	 * 
	 * @param validationModel
	 * @return
	 */
	protected boolean checkValidationResult(IStatus validationModel, Control... controls) {
		if (validationModel != null
				&& validationModel.getSeverity() == IStatus.ERROR) {
			updateStatus(ValidateUtil
					.getBasicFormattedUIErrorMessage(validationModel), controls);
			return false;
		}
		return true;
	}
	
	protected boolean checkValidationResult(Control control, IStatus validationModel) {
		return checkValidationResult(validationModel, control);
	}

	/**
	 * Standard way of processing SOA exception occured in a wizard page. Simple -
	 * show it to the user in the standard message area of the wizard page and
	 * log the exception.
	 * 
	 * @param exception
	 * @return
	 */
	protected boolean processException(Exception exception) {
		if (exception != null) {
			updateStatus(exception.getMessage());
			SOALogger.getLogger().error(exception);
			return false;
		}
		return true;
	}

	protected final ModifyListener modifyListener = new ModifyListener() {
		public void modifyText(final ModifyEvent e) {
			if (e != null && e.getSource() instanceof Control) {
				// we only do validation if the current control has user focus
				Control control = (Control) e.getSource();
				if (control.isEnabled() && control.isFocusControl()) {
					if (control instanceof Text
							&& ((Text) control).getEditable() == false) {// this
						// text control is not editable
						return;
					}
					dialogChanged();
				}
			}
		}
	};

	/**
	 * Gets the text value of the given control. This is a convenience method to
	 * reduce the code redundancy in finding the text value of any widget.It can
	 * handle Text, Label, Button, Combo box
	 * 
	 * @param control
	 *            Currently only support Text, CCombo, Label and Button
	 * @return The value of the given control.
	 */
	protected String getTextValue(final Control control) {
		String value = DEFAULT_TEXT_VALUE;
		if (control instanceof Text)
			value = ((Text) control).getText();
		else if (control instanceof CCombo)
			value = ((CCombo) control).getText();
		else if (control instanceof Label)
			value = ((Label) control).getText();
		else if (control instanceof Button)
			value = ((Button) control).getText();
		else if (control instanceof Combo)
			value = ((Combo) control).getText();
		return value.trim();
	}

	/**
	 * This id associates the widget with the tiny question mark button on the
	 * left.
	 * 
	 * @return The help context ID
	 */
	public String getHelpContextID() {
		return GlobalRepositorySystem.instanceOf().getActiveRepositorySystem()
				.getHelpProvider().getHelpContextID(
						ISOAHelpProvider.SOA_TUTORIAL);
	}

	public ISOAOrganizationProvider getOrganizationProvider() {
		if (this.organizationProvider == null) {
			this.organizationProvider = GlobalRepositorySystem.instanceOf()
			.getActiveRepositorySystem().getActiveOrganizationProvider();
		}
		return organizationProvider;
	}

	/**
	 * This method is used for ensuring that the default value could be
	 * retrieved for a particular Text widget, when user un-select the Override
	 * button.
	 * 
	 * @param text
	 * @return
	 */
	public abstract String getDefaultValue(Text text);
}
