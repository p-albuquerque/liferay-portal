/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.dynamic.data.mapping.form.web.internal.upgrade.v1_1_0;

import com.liferay.dynamic.data.mapping.constants.DDMPortletKeys;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.portlet.PortletPreferencesFactoryUtil;
import com.liferay.portal.kernel.upgrade.BasePortletPreferencesUpgradeProcess;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.Validator;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.portlet.PortletPreferences;

/**
 * @author Paulo Albuquerque
 */
public class DDMFormPortletPreferencesUpgradeProcess
	extends BasePortletPreferencesUpgradeProcess {

	@Override
	protected String[] getPortletIds() {
		return new String[] {
			DDMPortletKeys.DYNAMIC_DATA_MAPPING_FORM + "_INSTANCE_%"
		};
	}

	@Override
	protected String upgradePreferences(
			long companyId, long ownerId, int ownerType, long plid,
			String portletId, String xml)
		throws Exception {

		PortletPreferences portletPreferences =
			PortletPreferencesFactoryUtil.fromXML(
				companyId, ownerId, ownerType, plid, portletId, xml);

		String formInstanceId = portletPreferences.getValue(
			"formInstanceId", StringPool.BLANK);

		if (Validator.isNotNull(formInstanceId)) {
			try (PreparedStatement preparedStatement =
					connection.prepareStatement(
						StringBundler.concat(
							"Select DDMStructure.externalReferenceCode, ",
							"Group_.externalReferenceCode from ",
							"DDMStructure inner join DDMFormInstance on ",
							"DDMStructure.structureId = ",
							"DDMFormInstance.structureId inner join Group_ on ",
							"DDMStructure.groupId = Group_.groupId where ",
							"DDMFormInstance.formInstanceId = ?"))) {

				preparedStatement.setLong(
					1, GetterUtil.getLong(formInstanceId));

				ResultSet resultSet = preparedStatement.executeQuery();

				while (resultSet.next()) {
					portletPreferences.setValue(
						"ddmStructureExternalReferenceCode",
						resultSet.getString(1));
					portletPreferences.setValue(
						"groupExternalReferenceCode", resultSet.getString(2));
				}
			}
		}

		return PortletPreferencesFactoryUtil.toXML(portletPreferences);
	}

}