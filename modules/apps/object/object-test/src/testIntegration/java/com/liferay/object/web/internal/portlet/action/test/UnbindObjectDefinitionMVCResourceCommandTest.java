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

import java.util.Collections;
import java.util.Iterator;

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
public class UnbindObjectDefinitionMVCResourceCommandTest {

	@ClassRule
	@Rule
	public static final TestRule testRule = new LiferayIntegrationTestRule();

	@Before
	public void setUp() throws Exception {
		_objectDefinitionA =
			_objectDefinitionLocalService.addCustomObjectDefinition(
				TestPropsValues.getUserId(), 0, false, false,
				LocalizedMapUtil.getLocalizedMap("A"), "A", null, null,
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

		_objectDefinitionA =
			_objectDefinitionLocalService.updateRootObjectDefinitionId(
				_objectDefinitionA.getObjectDefinitionId(),
				_objectDefinitionA.getObjectDefinitionId());

		_objectDefinitionAA = _createObjectDefinition("AA");

		_objectRelationshipA_AA = _relateObjectDefinition(
			_objectDefinitionA, _objectDefinitionAA);

		_objectDefinitionAB = _createObjectDefinition("AB");

		_objectRelationshipA_AB = _relateObjectDefinition(
			_objectDefinitionA, _objectDefinitionAB);

		_objectDefinitionAAA = _createObjectDefinition("AAA");

		_objectRelationshipAA_AAA = _relateObjectDefinition(
			_objectDefinitionAA, _objectDefinitionAAA);

		_objectDefinitionAAB = _createObjectDefinition("AAB");

		_objectRelationshipAA_AAB = _relateObjectDefinition(
			_objectDefinitionAA, _objectDefinitionAAB);
	}

	@Test
	public void testUnbindObjectDefinition() throws Exception {
		Assert.assertEquals(
			"A, AA, AB, AAA, AAB",
			_toString(
				_treeFactory.create(
					_objectDefinitionA.getObjectDefinitionId())));

		_assertEdge(
			true, _objectRelationshipA_AA, _objectRelationshipA_AB,
			_objectRelationshipAA_AAA, _objectRelationshipAA_AAB);

		_assertRootObjectDefinitionId(
			_objectDefinitionA.getObjectDefinitionId(), _objectDefinitionA,
			_objectDefinitionAA, _objectDefinitionAAA, _objectDefinitionAAB,
			_objectDefinitionAB);

		// unbind object definition internal node

		_unbindObjectDefinition(_objectDefinitionAA.getObjectDefinitionId());

		Assert.assertEquals(
			"A, AB",
			_toString(
				_treeFactory.create(
					_objectDefinitionA.getObjectDefinitionId())));

		_assertEdge(
			false, _objectRelationshipA_AA, _objectRelationshipAA_AAA,
			_objectRelationshipAA_AAB);

		_assertRootObjectDefinitionId(
			0, _objectDefinitionAA, _objectDefinitionAAA, _objectDefinitionAAB);

		// unbind object definition leaf node

		_unbindObjectDefinition(_objectDefinitionAB.getObjectDefinitionId());

		Assert.assertEquals(
			"A",
			_toString(
				_treeFactory.create(
					_objectDefinitionA.getObjectDefinitionId())));

		_assertEdge(false, _objectRelationshipA_AB);

		_assertRootObjectDefinitionId(0, _objectDefinitionAB);

		// unbind object definition root node

		_unbindObjectDefinition(_objectDefinitionA.getObjectDefinitionId());

		_assertRootObjectDefinitionId(0, _objectDefinitionA);

		Assert.assertNull(
			_treeFactory.create(_objectDefinitionA.getObjectDefinitionId()));
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

	private ObjectDefinition _createObjectDefinition(
			String objectDefinitionName)
		throws Exception {

		ObjectDefinition objectDefinition =
			_objectDefinitionLocalService.addCustomObjectDefinition(
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

		return _objectDefinitionLocalService.updateRootObjectDefinitionId(
			objectDefinition.getObjectDefinitionId(),
			_objectDefinitionA.getObjectDefinitionId());
	}

	private ObjectRelationship _relateObjectDefinition(
			ObjectDefinition objectDefinition1,
			ObjectDefinition objectDefinition2)
		throws Exception {

		ObjectRelationship objectRelationship =
			_objectRelationshipLocalService.addObjectRelationship(
				TestPropsValues.getUserId(),
				objectDefinition1.getObjectDefinitionId(),
				objectDefinition2.getObjectDefinitionId(), 0,
				ObjectRelationshipConstants.DELETION_TYPE_CASCADE,
				LocalizedMapUtil.getLocalizedMap(
					objectDefinition1.getShortName() + " - " +
						objectDefinition2.getShortName()),
				StringUtil.randomId(),
				ObjectRelationshipConstants.TYPE_ONE_TO_MANY);

		return _objectRelationshipLocalService.updateObjectRelationship(
			objectRelationship.getObjectRelationshipId(), 0,
			ObjectRelationshipConstants.DELETION_TYPE_CASCADE, true,
			objectRelationship.getLabelMap());
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

	private void _unbindObjectDefinition(long objectDefinitionId)
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
			"objectDefinitionId", String.valueOf(objectDefinitionId));

		_mvcResourceCommand.serveResource(mockLiferayResourceRequest, null);
	}

	@Inject(
		filter = "mvc.command.name=/object_definitions/unbind_object_definition"
	)
	private MVCResourceCommand _mvcResourceCommand;

	private ObjectDefinition _objectDefinitionA;
	private ObjectDefinition _objectDefinitionAA;
	private ObjectDefinition _objectDefinitionAAA;
	private ObjectDefinition _objectDefinitionAAB;
	private ObjectDefinition _objectDefinitionAB;

	@Inject
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	private ObjectRelationship _objectRelationshipA_AA;
	private ObjectRelationship _objectRelationshipA_AB;
	private ObjectRelationship _objectRelationshipAA_AAA;
	private ObjectRelationship _objectRelationshipAA_AAB;

	@Inject
	private ObjectRelationshipLocalService _objectRelationshipLocalService;

	@Inject
	private PortletLocalService _portletLocalService;

	@Inject
	private TreeFactory _treeFactory;

}