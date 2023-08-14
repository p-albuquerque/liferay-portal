/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.object.internal.security.permission.resource.util;

import com.liferay.object.constants.ObjectActionKeys;
import com.liferay.object.constants.ObjectActionTriggerConstants;
import com.liferay.object.constants.ObjectDefinitionConstants;
import com.liferay.object.model.ObjectAction;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.service.ObjectActionLocalService;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.security.permission.ResourceActions;
import com.liferay.portal.kernel.service.PortletLocalService;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.xml.Document;
import com.liferay.portal.kernel.xml.SAXReaderUtil;

/**
 * @author Carolina Barbosa
 */
public class ObjectDefinitionResourcePermissionUtil {

	public static void populateResourceActions(
			ObjectActionLocalService objectActionLocalService,
			ObjectDefinition objectDefinition,
			PortletLocalService portletLocalService,
			ResourceActions resourceActions)
		throws Exception {

		ClassLoader classLoader =
			ObjectDefinitionResourcePermissionUtil.class.getClassLoader();

		String objectActionPermissionKeys = StringPool.BLANK;

		for (ObjectAction objectAction :
				objectActionLocalService.getObjectActions(
					objectDefinition.getObjectDefinitionId(),
					ObjectActionTriggerConstants.KEY_STANDALONE)) {

			objectActionPermissionKeys = StringBundler.concat(
				objectActionPermissionKeys, "<action-key>",
				objectAction.getName(), "</action-key>");
		}

		String resourceActionsFileName =
			"resource-actions/resource-actions.xml.tpl";

		if (!StringUtil.equals(
				objectDefinition.getStorageType(),
				ObjectDefinitionConstants.STORAGE_TYPE_DEFAULT)) {

			resourceActionsFileName =
				"resource-actions/resource-actions-nondefault-storage-type." +
					"xml.tpl";
		}

		String childStandalone = StringBundler.concat(
			"<model-resource>", "\n\t\t<model-name>child 1</model-name>",
			"\n\t\t<portlet-ref>", "\n\t\t\t<portlet-name>", objectDefinition.getPortletId(),"</portlet-name>","\n\t\t</portlet-ref>",
			"\n\t\t<weight>3</weight>", "\n\t\t<permissions>", "\n\t\t\t<supports>", "\n\t\t\t\t<action-key>action1</action-key>", "\n\t\t\t</supports>",
			"\n\t\t\t<guest-defaults />", "\n\t\t\t<guest-unsupported />", "\n\t\t</permissions>", "\n\t</model-resource>");

		Document document = SAXReaderUtil.read(
			StringUtil.replace(
				StringUtil.read(classLoader, resourceActionsFileName),
				new String[] {
					"[$MODEL_NAME$]", "[$PERMISSIONS_GUEST_UNSUPPORTED$]",
					"[$PERMISSIONS_SUPPORTS$]", "[$PORTLET_NAME$]",
					"[$RESOURCE_NAME$]", "[$CHILD_STANDALONE_ACTIONS$]"
				},
				new String[] {
					objectDefinition.getClassName(),
					_getPermissionsGuestUnsupported(objectDefinition) +
						objectActionPermissionKeys,
					_getPermissionsSupports(objectDefinition) +
						objectActionPermissionKeys,
					objectDefinition.getPortletId(),
					objectDefinition.getResourceName(),
					childStandalone
				}));

		resourceActions.populateModelResources(document);
		resourceActions.populatePortletResource(
			portletLocalService.getPortletById(
				objectDefinition.getCompanyId(),
				objectDefinition.getPortletId()),
			classLoader, document);
	}

	private static String _getPermissionsGuestUnsupported(
		ObjectDefinition objectDefinition) {

		if (!objectDefinition.isEnableComments()) {
			return StringPool.BLANK;
		}

		return "<action-key>DELETE_DISCUSSION</action-key>" +
			"<action-key>UPDATE_DISCUSSION</action-key>";
	}

	private static String _getPermissionsSupports(
		ObjectDefinition objectDefinition) {

		String permissionsSupports = StringPool.BLANK;

		if (objectDefinition.isEnableComments()) {
			permissionsSupports = StringBundler.concat(
				"<action-key>ADD_DISCUSSION</action-key>",
				"<action-key>DELETE_DISCUSSION</action-key>",
				"<action-key>UPDATE_DISCUSSION</action-key>");
		}

		if (objectDefinition.isEnableObjectEntryHistory()) {
			permissionsSupports = StringBundler.concat(
				permissionsSupports, "<action-key>",
				ObjectActionKeys.OBJECT_ENTRY_HISTORY, "</action-key>");
		}

		return permissionsSupports;
	}

}