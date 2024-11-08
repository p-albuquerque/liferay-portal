/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.dynamic.data.mapping.form.web.internal.upgrade.v1_1_0;

import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.dao.orm.common.SQLTransformer;
import com.liferay.portal.kernel.dao.jdbc.AutoBatchPreparedStatementUtil;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * @author Paulo Albuquerque
 */
public class DDMFormPortletPreferencesUpgradeProcess extends UpgradeProcess {

	@Override
	protected void doUpgrade() throws Exception {
		try (PreparedStatement preparedStatement1 = connection.prepareStatement(
			SQLTransformer.transform(_getPreparedStatement()));

			 PreparedStatement insertPreparedStatement =
				 AutoBatchPreparedStatementUtil.concurrentAutoBatch(
					 connection,
					 StringBundler.concat(
						 "insert into PortletPreferenceValue (",
						 "mvccVersion, ctCollectionId, companyId, ",
						 "portletPreferencesId, index_, largeValue, name, ",
						 "readOnly, smallValue)",
						 "values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"));
			 ResultSet resultSet = preparedStatement1.executeQuery()) {

			while (resultSet.next()) {
				long companyId = resultSet.getLong(1);
				long portletPreferencesId = resultSet.getLong(2);

				_addBatch(
					companyId, portletPreferencesId,
					"ddmStructureExternalReferenceCode", resultSet.getString(6),
					insertPreparedStatement);

				_addBatch(
					companyId, portletPreferencesId,
					"groupExternalReferenceCode", resultSet.getString(8),
					insertPreparedStatement);
			}

			insertPreparedStatement.executeBatch();
		}
	}

	private void _addBatch(
			long companyId, long portletPreferencesId, String name,
			String smallValue, PreparedStatement preparedStatement)
		throws Exception {

		preparedStatement.setLong(1, 0);
		preparedStatement.setLong(2, 0);
		preparedStatement.setLong(3, companyId);
		preparedStatement.setLong(4, portletPreferencesId);
		preparedStatement.setLong(5, 0);
		preparedStatement.setString(6, StringPool.BLANK);
		preparedStatement.setString(7, name);
		preparedStatement.setLong(8, 0);
		preparedStatement.setString(9, smallValue);

		preparedStatement.addBatch();
	}

	private String _getPreparedStatement() {
		return StringBundler.concat(
			"select PortletPreferenceValue.companyId, ",
			"PortletPreferenceValue.portletPreferencesId, ",
			"PortletPreferenceValue.name, PortletPreferenceValue.smallValue, ",
			"DDMFormInstance.structureId, DDMStructure.externalReferenceCode, ",
			"DDMStructure.groupId, Group_.externalReferenceCode from ",
			"PortletPreferenceValue inner join DDMFormInstance on ",
			"PortletPreferenceValue.smallValue = ",
			"DDMFormInstance.formInstanceId inner join DDMStructure on ",
			"DDMFormInstance.structureId = DDMStructure.structureId inner ",
			"join Group_ on DDMStructure.groupId = Group_.groupId where ",
			"PortletPreferenceValue like \"formInstanceId\"");
	}

}