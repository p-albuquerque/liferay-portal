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
import com.liferay.object.exception.ObjectFieldReadOnlyException;
import com.liferay.object.model.ObjectField;
import com.liferay.object.rest.dynamic.data.mapping.expression.ObjectEntryDDMExpressionFieldAccessor;
import com.liferay.object.service.ObjectFieldLocalService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.util.Validator;

import java.util.Map;
import java.util.Objects;

/**
 * @author Paulo Albuquerque
 */
public class ReadOnlyUtil {

	public static void validateReadOnly(
			long objectDefinitionId, Map<String, Object> existingValues,
			Map<String, Object> values,
			DDMExpressionFactory ddmExpressionFactory,
			ObjectFieldLocalService objectFieldLocalService)
		throws PortalException {

		for (Map.Entry<String, Object> entry : values.entrySet()) {
			ObjectField objectField = objectFieldLocalService.getObjectField(
				objectDefinitionId, entry.getKey());

			if (Objects.equals(
					objectField.getReadOnly(),
					ObjectFieldConstants.READ_ONLY_FALSE)) {

				continue;
			}

			if (Objects.equals(
					objectField.getReadOnly(),
					ObjectFieldConstants.READ_ONLY_TRUE)) {

				_verifyReadOnlyTrue(
					entry.getKey(), (String)entry.getValue(), existingValues,
					objectField.getName());

				continue;
			}

			DDMExpression<Boolean> ddmExpression =
				ddmExpressionFactory.createExpression(
					CreateExpressionRequest.Builder.newBuilder(
						objectField.getReadOnlyConditionExpression()
					).withDDMExpressionFieldAccessor(
						new ObjectEntryDDMExpressionFieldAccessor(
							existingValues)
					).build());

			ddmExpression.setVariables(existingValues);

			if (ddmExpression.evaluate()) {
				_verifyReadOnlyTrue(
					entry.getKey(), (String)entry.getValue(), existingValues,
					objectField.getName());
			}
		}
	}

	private static void _verifyReadOnlyTrue(
			String entryKey, String entryValue,
			Map<String, Object> existingValues, String objectFieldName)
		throws PortalException {

		if (!((existingValues.isEmpty() && Validator.isNull(entryValue)) ||
			  Objects.equals(entryValue, existingValues.get(entryKey)))) {

			throw new ObjectFieldReadOnlyException(
				"The object field " + objectFieldName + " is readOnly");
		}
	}

}