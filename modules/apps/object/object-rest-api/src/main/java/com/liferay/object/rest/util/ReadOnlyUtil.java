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

package com.liferay.object.rest.util;

import com.liferay.dynamic.data.mapping.expression.CreateExpressionRequest;
import com.liferay.dynamic.data.mapping.expression.DDMExpression;
import com.liferay.dynamic.data.mapping.expression.DDMExpressionFactory;
import com.liferay.object.constants.ObjectFieldConstants;
import com.liferay.object.constants.ObjectFieldSettingConstants;
import com.liferay.object.model.ObjectField;
import com.liferay.object.model.ObjectFieldSetting;
import com.liferay.object.rest.dynamic.data.mapping.expression.ObjectEntryDDMExpressionFieldAccessor;
import com.liferay.object.rest.dynamic.data.mapping.expression.ObjectEntryDDMExpressionParameterAccessor;
import com.liferay.object.service.ObjectFieldLocalService;
import com.liferay.object.service.ObjectFieldSettingLocalService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.util.ListUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Paulo Albuquerque
 */
public class ReadOnlyUtil {

	public static HashMap<String, Object> executeReadOnly(
			long objectDefinitionId, Map<String, Object> oldValues,
			Map<String, Object> values,
			DDMExpressionFactory ddmExpressionFactory,
			ObjectFieldLocalService objectFieldLocalService,
			ObjectFieldSettingLocalService objectFieldSettingLocalService)
		throws PortalException {

		HashMap<String, Object> newValues = new HashMap<>();

		for (Map.Entry<String, Object> entry : values.entrySet()) {
			ObjectField objectField = objectFieldLocalService.getObjectField(
				objectDefinitionId, entry.getKey());

			if (Objects.equals(
					objectField.getReadOnly(),
					ObjectFieldConstants.READ_ONLY_FALSE)) {

				newValues.put(entry.getKey(), entry.getValue());

				continue;
			}

			if (Objects.equals(
					objectField.getReadOnly(),
					ObjectFieldConstants.READ_ONLY_TRUE)) {

				newValues.put(entry.getKey(), oldValues.get(entry.getKey()));

				continue;
			}

			ObjectFieldSetting objectFieldSetting =
				objectFieldSettingLocalService.fetchObjectFieldSetting(
					objectField.getObjectFieldId(),
					ObjectFieldSettingConstants.NAME_DYNAMIC_READ_ONLY);

			if (Objects.equals(
					objectFieldSetting.getValue(),
					ObjectFieldConstants.READ_ONLY_FALSE)) {

				newValues.put(entry.getKey(), entry.getValue());

				continue;
			}

			newValues.put(entry.getKey(), oldValues.get(entry.getKey()));
		}

		for (ObjectField objectField :
				ListUtil.filter(
					objectFieldLocalService.getObjectFields(objectDefinitionId),
					objectField -> Objects.equals(
						objectField.getReadOnly(),
						ObjectFieldConstants.READ_ONLY_CONDITIONAL))) {

			ObjectFieldSetting objectFieldSetting =
				objectFieldSettingLocalService.fetchObjectFieldSetting(
					objectField.getObjectFieldId(),
					ObjectFieldSettingConstants.NAME_DYNAMIC_READ_ONLY);

			if (objectFieldSetting == null) {
				objectFieldSetting =
					objectFieldSettingLocalService.createObjectFieldSetting(0L);

				objectFieldSetting.setName(
					ObjectFieldSettingConstants.NAME_DYNAMIC_READ_ONLY);
			}

			DDMExpression<Boolean> ddmExpression =
				ddmExpressionFactory.createExpression(
					CreateExpressionRequest.Builder.newBuilder(
						objectField.getReadOnlyConditionExpression()
					).withDDMExpressionFieldAccessor(
						new ObjectEntryDDMExpressionFieldAccessor(values)
					).withDDMExpressionParameterAccessor(
						new ObjectEntryDDMExpressionParameterAccessor(oldValues)
					).build());

			ddmExpression.setVariables(values);

			if (ddmExpression.evaluate()) {
				objectFieldSetting.setValue(
					ObjectFieldConstants.READ_ONLY_TRUE);
			}
			else {
				objectFieldSetting.setValue(
					ObjectFieldConstants.READ_ONLY_FALSE);
			}

			objectFieldSettingLocalService.addOrUpdateObjectFieldSetting(
				objectFieldSetting.getObjectFieldSettingId(),
				objectField.getUserId(), objectField.getObjectFieldId(),
				objectFieldSetting.getName(), objectFieldSetting.getValue());
		}

		return newValues;
	}

}