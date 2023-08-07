/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.object.rest.internal.manager.v1_0.test;

import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.rest.dto.v1_0.ObjectEntry;
import com.liferay.object.rest.manager.v1_0.DefaultObjectEntryManager;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.search.Sort;
import com.liferay.portal.kernel.search.SortFactoryUtil;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.vulcan.dto.converter.DTOConverterContext;
import com.liferay.portal.vulcan.pagination.Page;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.junit.Assert;

/**
 * @author Paulo Albuquerque
 */
public class BaseObjectEntryManagerImplTest {

	protected void assertEquals(
			List<ObjectEntry> expectedObjectEntries,
			List<ObjectEntry> actualObjectEntries)
		throws Exception {

		Assert.assertEquals(
			actualObjectEntries.toString(), expectedObjectEntries.size(),
			actualObjectEntries.size());

		for (int i = 0; i < expectedObjectEntries.size(); i++) {
			assertEquals(
				expectedObjectEntries.get(i), actualObjectEntries.get(i));
		}
	}

	protected String buildEqualsExpressionFilterString(
		String fieldName, Object value) {

		return StringBundler.concat(
			"( ", fieldName, " eq ", getValue(value), ")");
	}

	protected String getValue(Object value) {
		if (value instanceof String) {
			return StringUtil.quote(String.valueOf(value));
		}

		return String.valueOf(value);
	}

	protected void testGetObjectEntries(
			Map<String, String> context, ObjectEntry... expectedObjectEntries)
		throws Exception {

		Sort[] sorts = null;

		if (context.containsKey("sort")) {
			String[] sort = StringUtil.split(context.get("sort"), ":");

			sorts = new Sort[] {
				SortFactoryUtil.create(sort[0], Objects.equals(sort[1], "desc"))
			};
		}

		Page<ObjectEntry> page = objectEntryManager.getObjectEntries(
			companyId, objectDefinition2, null, null, dtoConverterContext,
			context.get("filter"), null, context.get("search"), sorts);

		assertEquals(
			ListUtil.fromArray(expectedObjectEntries),
			(List<ObjectEntry>)page.getItems());
	}

	protected static long companyId;
	protected static DTOConverterContext dtoConverterContext;
	protected static DefaultObjectEntryManager objectEntryManager;

	@DeleteAfterTestRun
	protected ObjectDefinition objectDefinition2;

}