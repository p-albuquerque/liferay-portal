/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.portal.workflow.task.web.internal.display.template;

import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.comment.CommentManager;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.model.Release;
import com.liferay.portal.kernel.portletdisplaytemplate.BasePortletDisplayTemplateHandler;
import com.liferay.portal.kernel.portletdisplaytemplate.PortletDisplayTemplateManager;
import com.liferay.portal.kernel.template.TemplateHandler;
import com.liferay.portal.kernel.template.TemplateVariableGroup;
import com.liferay.portal.kernel.trash.helper.TrashHelper;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.PortletKeys;
import com.liferay.portal.kernel.util.ResourceBundleUtil;
import com.liferay.portal.workflow.task.web.internal.configuration.WorkflowTaskWebConfiguration;
import com.liferay.portal.workflow.task.web.internal.display.context.WorkflowTaskDisplayContext;
import com.liferay.portal.workflow.task.web.internal.util.WorkflowTaskPortletUtil;
import com.liferay.taglib.security.PermissionsURLTag;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Nara Andrade
 */
@Component(
	configurationPid = "com.liferay.portal.workflow.task.web.internal.configuration.WorkflowTaskWebConfiguration",
	property = "javax.portlet.name=" + PortletKeys.MY_WORKFLOW_TASK,
	service = TemplateHandler.class
)
public class MyWorkflowTaskPortletDisplayTemplateHandler
	extends BasePortletDisplayTemplateHandler {

	@Override
	public String getClassName() {
		return "";
	}

	@Override
	public Map<String, Object> getCustomContextObjects() {
		return HashMapBuilder.<String, Object>put(
			"workflowTaskPermission", true
		).put(
			"workflowTaskPortletUtil", _workflowTaskPortletUtil
		).put(
			"commentManager", _commentManager
		).put(
			"language", _language
		).put(
			"permissionsURLTag", new PermissionsURLTag()
		).put(
			"trashHelper", _trashHelper
		).build();
	}

	@Override
	public String getName(Locale locale) {
		String portletTitle = _portal.getPortletTitle(
			PortletKeys.MY_WORKFLOW_TASK,
			ResourceBundleUtil.getBundle(
				"content.Language", locale, getClass()));

		return _language.format(locale, "x-template", portletTitle, false);
	}

	@Override
	public String getResourceName() {
		return PortletKeys.MY_WORKFLOW_TASK;
	}

	@Override
	public Map<String, TemplateVariableGroup> getTemplateVariableGroups(
			long classPK, String language, Locale locale)
		throws Exception {

		Map<String, TemplateVariableGroup> templateVariableGroups =
			super.getTemplateVariableGroups(classPK, language, locale);

		String[] restrictedVariables = getRestrictedVariables(language);

		TemplateVariableGroup workflowTaskPortletUtilTemplateVariableGroup =
			new TemplateVariableGroup("workflow-task-util", restrictedVariables);

		workflowTaskPortletUtilTemplateVariableGroup.addVariable(
			"workflow-task-portlet-util", WorkflowTaskPortletUtil.class,
			"workflowTaskPortletUtil");

		templateVariableGroups.put(
			"workflow-task-util", workflowTaskPortletUtilTemplateVariableGroup);

		TemplateVariableGroup workflowTaskServicesTemplateVariableGroup =
			new TemplateVariableGroup("workflow-task-services", restrictedVariables);

		workflowTaskServicesTemplateVariableGroup.setAutocompleteEnabled(false);

		workflowTaskServicesTemplateVariableGroup.addServiceLocatorVariables(
			null, null);

		templateVariableGroups.put(
			workflowTaskServicesTemplateVariableGroup.getLabel(),
			workflowTaskServicesTemplateVariableGroup);

		TemplateVariableGroup fieldsTemplateVariableGroup =
			templateVariableGroups.get("fields");

		fieldsTemplateVariableGroup.empty();

		fieldsTemplateVariableGroup.addCollectionVariable(
			"workflow-tasks", List.class, PortletDisplayTemplateManager.ENTRIES,
			"workflow-task", WorkflowTaskDisplayContext.class, "curWorkflowTask", "title");

		return templateVariableGroups;
	}

	@Activate
	@Modified
	protected void activate(Map<String, Object> properties) {
		_workflowTaskWebConfiguration = ConfigurableUtil.createConfigurable(
			WorkflowTaskWebConfiguration.class, properties);
	}

	@Override
	protected String getTemplatesConfigPath() {
		return _workflowTaskWebConfiguration.defaultDisplayView();
	}

	private volatile WorkflowTaskWebConfiguration _workflowTaskWebConfiguration;

	@Reference
	private WorkflowTaskPortletUtil _workflowTaskPortletUtil;

	@Reference
	private CommentManager _commentManager;

	@Reference
	private Language _language;

	@Reference
	private Portal _portal;

	@Reference(
		target = "(&(release.bundle.symbolic.name=com.liferay.portal.workflow.api.service)(&(release.schema.version>=3.0.0)(!(release.schema.version>=4.0.0))))"
	)
	private Release _release;

	@Reference
	private TrashHelper _trashHelper;

}