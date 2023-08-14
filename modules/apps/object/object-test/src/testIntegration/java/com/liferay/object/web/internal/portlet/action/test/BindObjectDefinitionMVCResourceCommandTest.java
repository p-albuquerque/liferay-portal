/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.object.web.internal.portlet.action.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.object.constants.ObjectDefinitionConstants;
import com.liferay.object.constants.ObjectPortletKeys;
import com.liferay.object.constants.ObjectRelationshipConstants;
import com.liferay.object.definition.tree.Node;
import com.liferay.object.definition.tree.Tree;
import com.liferay.object.definition.tree.TreeFactory;
import com.liferay.object.field.builder.TextObjectFieldBuilder;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectRelationship;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectRelationshipLocalService;
import com.liferay.petra.function.transform.TransformUtil;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.portlet.PortletConfigFactoryUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCResourceCommand;
import com.liferay.portal.kernel.service.PortletLocalService;
import com.liferay.portal.kernel.test.portlet.MockLiferayResourceRequest;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.JavaConstants;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.TimeZoneUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.vulcan.util.LocalizedMapUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;

/**
 * @author Feliphe Marinho
 */
@RunWith(Arquillian.class)
public class BindObjectDefinitionMVCResourceCommandTest {

	@ClassRule
	@Rule
	public static final TestRule testRule = new LiferayIntegrationTestRule();

	@Before
	public void setUp() throws Exception {
		_objectDefinitionA = _createObjectDefinition("A");

		_objectDefinitionAA = _createObjectDefinition("AA");

		_objectRelationshipA_AA = _relateObjectDefinition(
			_objectDefinitionA, _objectDefinitionAA);

		_relateObjectDefinition(
			_objectDefinitionA, _createObjectDefinition("AB"));

		_objectDefinitionAAA = _createObjectDefinition("AAA");

		_objectRelationshipAA_AAA = _relateObjectDefinition(
			_objectDefinitionAA, _objectDefinitionAAA);

		_objectDefinitionAAB = _createObjectDefinition("AAB");

		_objectRelationshipAA_AAB = _relateObjectDefinition(
			_objectDefinitionAA, _objectDefinitionAAB);
	}

	@Test
	public void testBindObjectDefinitions() throws Exception {
		_assertRootObjectDefinitionId(0, _objectDefinitionA);

		// Bind object definitions creating a new hierarchical structure
		// Bind A, AA and AAA to a hierarchical structure where A is the root

		_bindObjectDefinition(
			Arrays.asList(
				_objectRelationshipAA_AAA.getObjectRelationshipId(),
				_objectRelationshipA_AA.getObjectRelationshipId()));

		_assertEdge(true, _objectRelationshipA_AA, _objectRelationshipAA_AAA);

		_assertRootObjectDefinitionId(
			_objectDefinitionA.getObjectDefinitionId(), _objectDefinitionA,
			_objectDefinitionAA, _objectDefinitionAAA);

		Assert.assertEquals(
			"A, AA, AAA",
			_toString(
				_treeFactory.create(
					_objectDefinitionA.getObjectDefinitionId())));

		// Bind one object definition to an existing hierarchical structure
		// Bind AAB to the hierarchical structure where A is the root

		_bindObjectDefinition(
			Arrays.asList(
				_objectRelationshipAA_AAB.getObjectRelationshipId(),
				_objectRelationshipA_AA.getObjectRelationshipId()));

		_assertEdge(true, _objectRelationshipAA_AAB);

		_assertRootObjectDefinitionId(
			_objectDefinitionA.getObjectDefinitionId(), _objectDefinitionAAB);

		Assert.assertEquals(
			"A, AA, AAA, AAB",
			_toString(
				_treeFactory.create(
					_objectDefinitionA.getObjectDefinitionId())));
	}

	private void _assertEdge(
			boolean edge, ObjectRelationship... objectRelationships)
		throws Exception {

		for (ObjectRelationship objectRelationship : objectRelationships) {
			objectRelationship =
				_objectRelationshipLocalService.getObjectRelationship(
					objectRelationship.getObjectRelationshipId());

			Assert.assertEquals(edge, objectRelationship.isEdge());
		}
	}

	private void _assertRootObjectDefinitionId(
			long rootObjectDefinitionId, ObjectDefinition... objectDefinitions)
		throws Exception {

		for (ObjectDefinition objectDefinition : objectDefinitions) {
			objectDefinition =
				_objectDefinitionLocalService.getObjectDefinition(
					objectDefinition.getObjectDefinitionId());

			Assert.assertEquals(
				rootObjectDefinitionId,
				objectDefinition.getRootObjectDefinitionId());
		}
	}

	private void _bindObjectDefinition(List<Long> objectRelationshipIds)
		throws Exception {

		MockLiferayResourceRequest mockLiferayResourceRequest =
			new MockLiferayResourceRequest();

		mockLiferayResourceRequest.setAttribute(
			JavaConstants.JAVAX_PORTLET_CONFIG,
			PortletConfigFactoryUtil.create(
				_portletLocalService.getPortletById(
					ObjectPortletKeys.OBJECT_DEFINITIONS),
				null));

		ThemeDisplay themeDisplay = new ThemeDisplay();

		themeDisplay.setLocale(LocaleUtil.getSiteDefault());
		themeDisplay.setTimeZone(TimeZoneUtil.getDefault());

		mockLiferayResourceRequest.setAttribute(
			WebKeys.THEME_DISPLAY, themeDisplay);

		mockLiferayResourceRequest.addParameter(
			"objectRelationshipIds",
			TransformUtil.transformToArray(
				objectRelationshipIds, String::valueOf, String.class));

		_mvcResourceCommand.serveResource(mockLiferayResourceRequest, null);
	}

	private ObjectDefinition _createObjectDefinition(
			String objectDefinitionName)
		throws Exception {

		return _objectDefinitionLocalService.addCustomObjectDefinition(
			TestPropsValues.getUserId(), 0, false, false,
			LocalizedMapUtil.getLocalizedMap(objectDefinitionName),
			objectDefinitionName, null, null,
			LocalizedMapUtil.getLocalizedMap(RandomTestUtil.randomString()),
			false, ObjectDefinitionConstants.SCOPE_COMPANY,
			ObjectDefinitionConstants.STORAGE_TYPE_DEFAULT,
			Collections.singletonList(
				new TextObjectFieldBuilder(
				).labelMap(
					LocalizedMapUtil.getLocalizedMap(
						RandomTestUtil.randomString())
				).name(
					StringUtil.randomId()
				).build()));
	}

	private ObjectRelationship _relateObjectDefinition(
			ObjectDefinition objectDefinition1,
			ObjectDefinition objectDefinition2)
		throws Exception {

		return _objectRelationshipLocalService.addObjectRelationship(
			TestPropsValues.getUserId(),
			objectDefinition1.getObjectDefinitionId(),
			objectDefinition2.getObjectDefinitionId(), 0,
			ObjectRelationshipConstants.DELETION_TYPE_CASCADE,
			LocalizedMapUtil.getLocalizedMap(
				objectDefinition1.getShortName() + " - " +
					objectDefinition2.getShortName()),
			StringUtil.randomId(),
			ObjectRelationshipConstants.TYPE_ONE_TO_MANY);
	}

	private String _toString(Tree tree) {
		Iterator<Node> iterator = tree.iterator();

		StringBundler sb = new StringBundler(2);

		iterator.forEachRemaining(
			node -> {
				if (!node.isRoot()) {
					sb.append(", ");
				}

				ObjectDefinition objectDefinition =
					_objectDefinitionLocalService.fetchObjectDefinition(
						node.getObjectDefinitionId());

				sb.append(objectDefinition.getShortName());
			});

		return sb.toString();
	}

	@Inject(
		filter = "mvc.command.name=/object_definitions/bind_object_definitions"
	)
	private MVCResourceCommand _mvcResourceCommand;

	private ObjectDefinition _objectDefinitionA;
	private ObjectDefinition _objectDefinitionAA;
	private ObjectDefinition _objectDefinitionAAA;
	private ObjectDefinition _objectDefinitionAAB;

	@Inject
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	private ObjectRelationship _objectRelationshipA_AA;
	private ObjectRelationship _objectRelationshipAA_AAA;
	private ObjectRelationship _objectRelationshipAA_AAB;

	@Inject
	private ObjectRelationshipLocalService _objectRelationshipLocalService;

	@Inject
	private PortletLocalService _portletLocalService;

	@Inject
	private TreeFactory _treeFactory;

}