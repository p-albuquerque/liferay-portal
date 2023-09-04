/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.object.test.util;

import com.liferay.object.definition.tree.Edge;
import com.liferay.object.definition.tree.Node;
import com.liferay.object.definition.tree.Tree;
import com.liferay.object.definition.tree.TreeFactory;
import com.liferay.object.field.builder.TextObjectFieldBuilder;
import com.liferay.object.field.util.ObjectFieldUtil;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectRelationship;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectRelationshipLocalService;
import com.liferay.petra.function.UnsafeConsumer;
import com.liferay.petra.function.transform.TransformUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.test.AssertUtils;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.vulcan.util.LocalizedMapUtil;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Feliphe Marinho
 */
public class TreeTestUtil {

	public static void assertTree(
		Map<String, String[]> expectedMap, Tree actualTree,
		ObjectDefinitionLocalService objectDefinitionLocalService)
		throws PortalException {

		Map<String, String[]> actualMap = new LinkedHashMap<>();

		Iterator<Node> iterator = actualTree.iterator();

		while (iterator.hasNext()) {
			Node node = iterator.next();

			actualMap.put(
				_getShortName(node, objectDefinitionLocalService),
				TransformUtil.transformToArray(
					node.getChildNodes(),
					childNode -> _getShortName(
						childNode, objectDefinitionLocalService),
					String.class));
		}

		AssertUtils.assertEquals(expectedMap, actualMap);
	}

	public static void bind(
		ObjectDefinitionLocalService objectDefinitionLocalService,
		List<ObjectRelationship> objectRelationships)
		throws PortalException {

		objectDefinitionLocalService.bindObjectDefinitions(
			TransformUtil.transformToLongArray(
				objectRelationships,
				ObjectRelationship::getObjectRelationshipId));
	}

	public static Tree createTree(
		ObjectDefinitionLocalService objectDefinitionLocalService,
		ObjectRelationshipLocalService objectRelationshipLocalService,
		TreeFactory treeFactory)
		throws PortalException {

		ObjectDefinition objectDefinitionA =
			ObjectDefinitionTestUtil.addObjectDefinition(
				"A1", objectDefinitionLocalService);

		ObjectDefinition objectDefinitionAA =
			ObjectDefinitionTestUtil.addObjectDefinition(
				"AA1", objectDefinitionLocalService);

		bind(
			objectDefinitionLocalService,
			Arrays.asList(
				ObjectRelationshipTestUtil.addObjectRelationship(
					objectRelationshipLocalService, objectDefinitionA,
					objectDefinitionAA),
				ObjectRelationshipTestUtil.addObjectRelationship(
					objectRelationshipLocalService, objectDefinitionAA,
					ObjectDefinitionTestUtil.addObjectDefinition(
						"AAA1", objectDefinitionLocalService)),
				ObjectRelationshipTestUtil.addObjectRelationship(
					objectRelationshipLocalService, objectDefinitionAA,
					ObjectDefinitionTestUtil.addObjectDefinition(
						"AAB1", objectDefinitionLocalService)),
				ObjectRelationshipTestUtil.addObjectRelationship(
					objectRelationshipLocalService, objectDefinitionA,
					ObjectDefinitionTestUtil.addObjectDefinition(
						"AB1", objectDefinitionLocalService))));

		return treeFactory.create(objectDefinitionA.getObjectDefinitionId());
	}

	public static Tree createTreeAndPublishObjectDefinitions(ObjectDefinitionLocalService objectDefinitionLocalService,
															 ObjectRelationshipLocalService objectRelationshipLocalService,
															 TreeFactory treeFactory) throws Exception {
		Tree tree = createTree(
			objectDefinitionLocalService, objectRelationshipLocalService,
			treeFactory);

		// This way to publish objects in a Root Context will be
		// changed when LPS-193250 be merged
		// >>>

		iterateNodeObjectDefinitions(
			objectDefinitionLocalService,
			tree,
			objectDefinition -> {
				ObjectFieldUtil.addCustomObjectField(
					new TextObjectFieldBuilder(
					).userId(
						TestPropsValues.getUserId()
					).indexed(
						true
					).indexedAsKeyword(
						true
					).labelMap(
						LocalizedMapUtil.getLocalizedMap("First Name")
					).name(
						"firstName"
					).objectDefinitionId(
						objectDefinition.getObjectDefinitionId()
					).build());

				ObjectFieldUtil.addCustomObjectField(
					new TextObjectFieldBuilder(
					).userId(
						TestPropsValues.getUserId()
					).indexed(
						true
					).indexedAsKeyword(
						true
					).labelMap(
						LocalizedMapUtil.getLocalizedMap("Last Name")
					).name(
						"lastName"
					).objectDefinitionId(
						objectDefinition.getObjectDefinitionId()
					).build());

				objectDefinitionLocalService.publishCustomObjectDefinition(
					TestPropsValues.getUserId(),
					objectDefinition.getObjectDefinitionId());
			});

		// <<<

		return tree;
	}

	public static ObjectRelationship getEdgeObjectRelationship(
		ObjectDefinition objectDefinition,
		ObjectRelationshipLocalService objectRelationshipLocalService,
		Tree tree)
		throws PortalException {

		Node node = tree.getNode(objectDefinition.getObjectDefinitionId());

		Edge edge = node.getEdge();

		return objectRelationshipLocalService.getObjectRelationship(
			edge.getObjectRelationshipId());
	}

	public static ObjectDefinition getRootObjectDefinition(ObjectDefinitionLocalService objectDefinitionLocalService,
														   Tree tree)
		throws PortalException {
		Iterator<Node> iterator = tree.iterator();

		Node node = iterator.next();

		long rootObjectDefinitionId =
			objectDefinitionLocalService.getObjectDefinition(
				node.getObjectDefinitionId()
			).getRootObjectDefinitionId();

		return objectDefinitionLocalService.getObjectDefinition(
			rootObjectDefinitionId);
	}

	public static void iterateNodeObjectDefinitions(ObjectDefinitionLocalService objectDefinitionLocalService,
		Tree tree,
		UnsafeConsumer<ObjectDefinition, Exception> unsafeConsumer)
		throws Exception {

		Iterator<Node> iterator = tree.iterator();

		while (iterator.hasNext()) {
			Node node = iterator.next();

			unsafeConsumer.accept(
				objectDefinitionLocalService.getObjectDefinition(
					node.getObjectDefinitionId()));
		}
	}


	public static void tearDown(
		ObjectDefinitionLocalService objectDefinitionLocalService)
		throws PortalException {

		for (String objectDefinitionName :
			new String[] {"C_A1", "C_AA1", "C_AAA1", "C_AAB1", "C_AB1"}) {

			ObjectDefinition objectDefinition =
				objectDefinitionLocalService.fetchObjectDefinition(
					TestPropsValues.getCompanyId(), objectDefinitionName);

			if (objectDefinition == null) {
				continue;
			}

			if (objectDefinition.getRootObjectDefinitionId() != 0) {
				unbind(objectDefinitionLocalService, objectDefinitionName);
			}

			objectDefinitionLocalService.deleteObjectDefinition(
				objectDefinition.getObjectDefinitionId());
		}
	}

	public static void unbind(
		ObjectDefinitionLocalService objectDefinitionLocalService,
		String objectDefinitionName)
		throws PortalException {

		ObjectDefinition objectDefinition =
			objectDefinitionLocalService.fetchObjectDefinition(
				TestPropsValues.getCompanyId(), objectDefinitionName);

		objectDefinitionLocalService.unbindObjectDefinition(
			objectDefinition.getObjectDefinitionId());
	}

	private static String _getShortName(
		Node node,
		ObjectDefinitionLocalService objectDefinitionLocalService)
		throws PortalException {

		ObjectDefinition objectDefinition =
			objectDefinitionLocalService.getObjectDefinition(
				node.getObjectDefinitionId());

		return objectDefinition.getShortName();
	}

}