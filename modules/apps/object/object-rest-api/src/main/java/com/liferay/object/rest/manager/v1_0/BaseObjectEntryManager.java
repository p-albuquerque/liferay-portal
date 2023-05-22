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

package com.liferay.object.rest.manager.v1_0;

import com.liferay.depot.service.DepotEntryLocalService;
import com.liferay.dynamic.data.mapping.expression.DDMExpressionFactory;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.rest.dto.v1_0.ObjectEntry;
import com.liferay.object.rest.util.ReadOnlyUtil;
import com.liferay.object.scope.ObjectScopeProvider;
import com.liferay.object.scope.ObjectScopeProviderRegistry;
import com.liferay.object.service.ObjectFieldLocalService;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.vulcan.dto.converter.DTOConverterContext;
import com.liferay.portal.vulcan.util.GroupUtil;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import org.osgi.service.component.annotations.Reference;

/**
 * @author Guilherme Camacho
 */
public abstract class BaseObjectEntryManager implements ObjectEntryManager {

	protected long getGroupId(
		ObjectDefinition objectDefinition, String scopeKey) {

		ObjectScopeProvider objectScopeProvider =
			objectScopeProviderRegistry.getObjectScopeProvider(
				objectDefinition.getScope());

		if (objectScopeProvider.isGroupAware()) {
			if (Objects.equals(objectDefinition.getScope(), "site")) {
				return GetterUtil.getLong(
					GroupUtil.getGroupId(
						objectDefinition.getCompanyId(), scopeKey,
						groupLocalService));
			}

			return GetterUtil.getLong(
				GroupUtil.getDepotGroupId(
					scopeKey, objectDefinition.getCompanyId(),
					depotEntryLocalService, groupLocalService));
		}

		return 0;
	}

	protected void validateReadOnly(
			long companyId, DTOConverterContext dtoConverterContext,
			String externalReferenceCode, ObjectDefinition objectDefinition,
			ObjectEntry objectEntry, String scopeKey)
		throws Exception {

		Map<String, Object> existingValues = Collections.emptyMap();

		if (!Objects.isNull(externalReferenceCode)) {
			companyId =
				Objects.isNull(companyId) ? objectDefinition.getCompanyId() :
					companyId;
			scopeKey =
				Objects.isNull(scopeKey) ? objectDefinition.getScope() :
					scopeKey;

			ObjectEntry existingObjectEntry = getObjectEntry(
				companyId, dtoConverterContext, externalReferenceCode,
				objectDefinition, scopeKey);

			existingValues = existingObjectEntry.getProperties();
		}

		ReadOnlyUtil.validateReadOnly(
			objectDefinition.getObjectDefinitionId(), existingValues,
			objectEntry.getProperties(), ddmExpressionFactory,
			objectFieldLocalService);
	}

	@Reference
	protected DDMExpressionFactory ddmExpressionFactory;

	@Reference
	protected DepotEntryLocalService depotEntryLocalService;

	@Reference
	protected GroupLocalService groupLocalService;

	@Reference
	protected ObjectFieldLocalService objectFieldLocalService;

	@Reference
	protected ObjectScopeProviderRegistry objectScopeProviderRegistry;

}