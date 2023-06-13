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

package com.liferay.object.entry.util;

import com.liferay.dynamic.data.mapping.expression.CreateExpressionRequest;
import com.liferay.dynamic.data.mapping.expression.DDMExpression;
import com.liferay.dynamic.data.mapping.expression.DDMExpressionFactory;
import com.liferay.object.constants.ObjectFieldConstants;
import com.liferay.object.dynamic.data.mapping.expression.ObjectEntryDDMExpressionFieldAccessor;
import com.liferay.object.exception.ObjectFieldReadOnlyException;
import com.liferay.object.model.ObjectField;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.feature.flag.FeatureFlagManagerUtil;
import com.liferay.portal.kernel.security.auth.PrincipalThreadLocal;
import com.liferay.portal.kernel.util.Validator;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Paulo Albuquerque
 */
public class ObjectEntryReadOnlyUtil {

	public static void getDefaultValues(
		Map<String, Object> values, List<ObjectField> objectFields) {

		for (ObjectField objectField : objectFields) {
			values.put(objectField.getName(), null);
		}
	}

	public static void validateReadOnly(
			Map<String, Object> values, Map<String, Object> newValues,
			DDMExpressionFactory ddmExpressionFactory,
			List<ObjectField> objectFields)
		throws PortalException {

		if (!FeatureFlagManagerUtil.isEnabled("LPS-170122") ||
			ObjectEntryThreadLocal.isSkipReadOnlyValidation()) {

			return;
		}

		if (values.isEmpty()) {
			getDefaultValues(values, objectFields);
		}

		values.put("currentUserId", PrincipalThreadLocal.getUserId());

		for (Map.Entry<String, Object> entry : newValues.entrySet()) {
			if (Objects.equals(entry.getKey(), "status")) {
				continue;
			}

			String readOnly = null;
			String readOnlyConditionExpression = null;

			for (ObjectField objectField : objectFields) {
				if (Objects.equals(objectField.getName(), entry.getKey())) {
					readOnly = objectField.getReadOnly();
					readOnlyConditionExpression =
						objectField.getReadOnlyConditionExpression();
				}
			}

			if ((readOnly == null) ||
				Objects.equals(
					readOnly, ObjectFieldConstants.READ_ONLY_FALSE)) {

				continue;
			}

			if (Objects.equals(readOnly, ObjectFieldConstants.READ_ONLY_TRUE)) {
				_verifyReadOnlyTrue(entry.getKey(), entry.getValue(), values);

				continue;
			}

			DDMExpression<Boolean> ddmExpression =
				ddmExpressionFactory.createExpression(
					CreateExpressionRequest.Builder.newBuilder(
						readOnlyConditionExpression
					).withDDMExpressionFieldAccessor(
						new ObjectEntryDDMExpressionFieldAccessor(values)
					).build());

			ddmExpression.setVariables(values);

			if (ddmExpression.evaluate()) {
				_verifyReadOnlyTrue(entry.getKey(), entry.getValue(), values);
			}
		}
	}

	private static void _verifyReadOnlyTrue(
			String objectFieldName, Object entryValue,
			Map<String, Object> values)
		throws PortalException {

		Object existingValue = values.get(objectFieldName);

		if (!((Validator.isNull(existingValue) &&
			   Validator.isNull(entryValue)) ||
			  Objects.equals(entryValue, existingValue))) {

			throw new ObjectFieldReadOnlyException(
				"The object field " + objectFieldName + " is readOnly");
		}
	}

}