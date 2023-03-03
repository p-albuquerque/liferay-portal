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

package com.liferay.notification.web.internal.portlet.action;

import com.liferay.notification.constants.NotificationPortletKeys;
import com.liferay.object.definition.notification.term.ObjectDefinitionNotificationCurrentUserTerm;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCResourceCommand;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.osgi.service.component.annotations.Component;

/**
 * @author Paulo Albuquerque
 */
@Component(
	property = {
		"javax.portlet.name=" + NotificationPortletKeys.NOTIFICATION_TEMPLATES,
		"mvc.command.name=/notification_templates/general_notification_template_terms"
	},
	service = MVCResourceCommand.class
)
public class GeneralNotificationTemplateTermsMVCResourceCommand
	extends BaseNotificationTemplateTermsMVCResourceCommand {

	@Override
	protected Set<Map.Entry<String, String>> getEntrySet() {
		Map<String, String> map = new LinkedHashMap<>();

		for (ObjectDefinitionNotificationCurrentUserTerm currentUserTerm :
				ObjectDefinitionNotificationCurrentUserTerm.values()) {

			map.put(currentUserTerm.getKey(), currentUserTerm.getTermName());
		}

		return map.entrySet();
	}

}