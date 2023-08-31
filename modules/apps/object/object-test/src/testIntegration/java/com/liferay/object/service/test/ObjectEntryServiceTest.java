/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.object.service.test;

import com.liferay.account.constants.AccountConstants;
import com.liferay.account.model.AccountEntry;
import com.liferay.account.service.AccountEntryLocalService;
import com.liferay.account.service.AccountEntryUserRelLocalService;
import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.object.constants.ObjectActionKeys;
import com.liferay.object.constants.ObjectFieldConstants;
import com.liferay.object.constants.ObjectRelationshipConstants;
import com.liferay.object.definition.tree.Node;
import com.liferay.object.definition.tree.Tree;
import com.liferay.object.definition.tree.TreeFactory;
import com.liferay.object.field.builder.TextObjectFieldBuilder;
import com.liferay.object.field.util.ObjectFieldUtil;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.model.ObjectRelationship;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.object.service.ObjectEntryService;
import com.liferay.object.service.ObjectRelationshipLocalService;
import com.liferay.object.service.test.util.ObjectDefinitionTestUtil;
import com.liferay.object.service.test.util.TreeTestUtil;
import com.liferay.petra.function.UnsafeConsumer;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.ResourceConstants;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.model.role.RoleConstants;
import com.liferay.portal.kernel.search.BaseModelSearchResult;
import com.liferay.portal.kernel.security.auth.PrincipalException;
import com.liferay.portal.kernel.security.auth.PrincipalThreadLocal;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.PermissionCheckerFactoryUtil;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.service.ResourcePermissionLocalService;
import com.liferay.portal.kernel.service.RoleLocalService;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.service.permission.ModelPermissionsFactory;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.vulcan.util.LocalizedMapUtil;

import java.io.Serializable;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;

import org.apache.commons.lang3.RandomStringUtils;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Marco Leo
 */
@RunWith(Arquillian.class)
public class ObjectEntryServiceTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Before
	public void setUp() throws Exception {
		_adminUser = TestPropsValues.getUser();
		_guestUser = _userLocalService.getGuestUser(
			TestPropsValues.getCompanyId());

		_objectDefinition = ObjectDefinitionTestUtil.addObjectDefinition(
			false, _objectDefinitionLocalService,
			Arrays.asList(
				ObjectFieldUtil.createObjectField(
					ObjectFieldConstants.BUSINESS_TYPE_TEXT,
					ObjectFieldConstants.DB_TYPE_STRING, true, false, null,
					"First Name", "firstName", false),
				ObjectFieldUtil.createObjectField(
					ObjectFieldConstants.BUSINESS_TYPE_TEXT,
					ObjectFieldConstants.DB_TYPE_STRING, true, false, null,
					"Last Name", "lastName", false)));

		_objectDefinition =
			_objectDefinitionLocalService.publishCustomObjectDefinition(
				TestPropsValues.getUserId(),
				_objectDefinition.getObjectDefinitionId());

		_originalPermissionChecker =
			PermissionThreadLocal.getPermissionChecker();
		_user = UserTestUtil.addUser();
	}

	@After
	public void tearDown() {
		PermissionThreadLocal.setPermissionChecker(_originalPermissionChecker);
	}

	@Test
	public void testAddObjectEntry() throws Exception {
		_setUser(_adminUser);

		Assert.assertNotNull(
			_objectEntryService.addObjectEntry(
				0, _objectDefinition.getObjectDefinitionId(),
				HashMapBuilder.<String, Serializable>put(
					"firstName", RandomStringUtils.randomAlphabetic(5)
				).build(),
				ServiceContextTestUtil.getServiceContext(
					TestPropsValues.getGroupId(), _adminUser.getUserId())));

		Tree tree = _createTreeAndPublishObjectDefinitions();

		_iterateNodeObjectDefinitions(
			tree,
			objectDefinition -> Assert.assertNotNull(
				_objectEntryService.addObjectEntry(
					0, objectDefinition.getObjectDefinitionId(),
					HashMapBuilder.<String, Serializable>put(
						"firstName", RandomStringUtils.randomAlphabetic(5)
					).build(),
					ServiceContextTestUtil.getServiceContext(
						TestPropsValues.getGroupId(),
						_adminUser.getUserId()))));

		_setUser(_guestUser);

		_assertPrincipalException(
			ObjectActionKeys.ADD_OBJECT_ENTRY, _objectDefinition, null);

		_iterateNodeObjectDefinitions(
			tree,
			objectDefinition -> _assertPrincipalException(
				ObjectActionKeys.ADD_OBJECT_ENTRY, _objectDefinition, null));

		_setUser(_user);

		_assertPrincipalException(
			ObjectActionKeys.ADD_OBJECT_ENTRY, _objectDefinition, null);

		_iterateNodeObjectDefinitions(
			tree,
			objectDefinition -> _assertPrincipalException(
				ObjectActionKeys.ADD_OBJECT_ENTRY, _objectDefinition, null));

		_setUser(_guestUser);

		Role guestRole = _roleLocalService.getRole(
			TestPropsValues.getCompanyId(), RoleConstants.GUEST);

		_resourcePermissionLocalService.addResourcePermission(
			TestPropsValues.getCompanyId(), _objectDefinition.getResourceName(),
			ResourceConstants.SCOPE_COMPANY,
			String.valueOf(TestPropsValues.getCompanyId()),
			guestRole.getRoleId(), ObjectActionKeys.ADD_OBJECT_ENTRY);

		Assert.assertNotNull(
			_objectEntryService.addObjectEntry(
				0, _objectDefinition.getObjectDefinitionId(),
				HashMapBuilder.<String, Serializable>put(
					"firstName", RandomStringUtils.randomAlphabetic(5)
				).build(),
				ServiceContextTestUtil.getServiceContext(
					TestPropsValues.getGroupId(), _guestUser.getUserId())));

		_iterateNodeObjectDefinitions(
			tree,
			objectDefinition -> {
				if (objectDefinition.isRegularNode()) {
					_resourcePermissionLocalService.addResourcePermission(
						TestPropsValues.getCompanyId(),
						objectDefinition.getResourceName(),
						ResourceConstants.SCOPE_COMPANY,
						String.valueOf(TestPropsValues.getCompanyId()),
						guestRole.getRoleId(),
						ObjectActionKeys.ADD_OBJECT_ENTRY);
				}

				_assertPrincipalException(
					ObjectActionKeys.ADD_OBJECT_ENTRY, objectDefinition, null);
			});

		_resourcePermissionLocalService.addResourcePermission(
			TestPropsValues.getCompanyId(),
			_getRootObjectDefinition(
				tree
			).getResourceName(),
			ResourceConstants.SCOPE_COMPANY,
			String.valueOf(TestPropsValues.getCompanyId()),
			guestRole.getRoleId(), ObjectActionKeys.ADD_OBJECT_ENTRY);

		_iterateNodeObjectDefinitions(
			tree,
			objectDefinition -> Assert.assertNotNull(
				_objectEntryService.addObjectEntry(
					0, _objectDefinition.getObjectDefinitionId(),
					HashMapBuilder.<String, Serializable>put(
						"firstName", RandomStringUtils.randomAlphabetic(5)
					).build(),
					ServiceContextTestUtil.getServiceContext(
						TestPropsValues.getGroupId(),
						_guestUser.getUserId()))));

		_setUser(_user);

		Assert.assertNotNull(
			_objectEntryService.addObjectEntry(
				0, _objectDefinition.getObjectDefinitionId(),
				HashMapBuilder.<String, Serializable>put(
					"firstName", RandomStringUtils.randomAlphabetic(5)
				).build(),
				ServiceContextTestUtil.getServiceContext(
					TestPropsValues.getGroupId(), _guestUser.getUserId())));

		_iterateNodeObjectDefinitions(
			tree,
			objectDefinition -> Assert.assertNotNull(
				_objectEntryService.addObjectEntry(
					0, _objectDefinition.getObjectDefinitionId(),
					HashMapBuilder.<String, Serializable>put(
						"firstName", RandomStringUtils.randomAlphabetic(5)
					).build(),
					ServiceContextTestUtil.getServiceContext(
						TestPropsValues.getGroupId(),
						_guestUser.getUserId()))));

		TreeTestUtil.tearDown(_objectDefinitionLocalService);
	}

	@Test
	public void testDeleteObjectEntry() throws Exception {
		_setUser(_user);

		_assertPrincipalException(ActionKeys.DELETE, _objectDefinition, null);

		_testDeleteObjectEntry(_objectDefinition, _adminUser, _adminUser);

		_testDeleteObjectEntry(_objectDefinition, _user, _user);

		Tree tree = _createTreeAndPublishObjectDefinitions();

		_iterateNodeObjectDefinitions(
			tree,
			objectDefinition -> {
				_setUser(_user);

				_assertPrincipalException(
					ActionKeys.DELETE, objectDefinition, null);

				_testDeleteObjectEntry(
					objectDefinition, _adminUser, _adminUser);
				_testDeleteObjectEntry(objectDefinition, _user, _user);
			});

		Role role = _roleLocalService.getRole(
			TestPropsValues.getCompanyId(), RoleConstants.USER);

		_setUser(_user);

		_iterateNodeObjectDefinitions(
			tree,
			objectDefinition -> {
				if (objectDefinition.isRegularNode()) {
					_resourcePermissionLocalService.addResourcePermission(
						TestPropsValues.getCompanyId(),
						objectDefinition.getClassName(),
						ResourceConstants.SCOPE_COMPANY,
						String.valueOf(TestPropsValues.getCompanyId()),
						role.getRoleId(), ActionKeys.DELETE);
					_assertPrincipalException(
						ActionKeys.DELETE, objectDefinition, null);
				}
			});

		_resourcePermissionLocalService.addResourcePermission(
			TestPropsValues.getCompanyId(),
			_getRootObjectDefinition(
				tree
			).getClassName(),
			ResourceConstants.SCOPE_COMPANY,
			String.valueOf(TestPropsValues.getCompanyId()), role.getRoleId(),
			ActionKeys.DELETE);

		_iterateNodeObjectDefinitions(
			tree,
			objectDefinition -> _testDeleteObjectEntry(
				objectDefinition, _adminUser, _user));

		TreeTestUtil.tearDown(_objectDefinitionLocalService);
	}

	@Test
	public void testGetObjectEntry() throws Exception {
		_setUser(_adminUser);

		ObjectEntry adminObjectEntry = _addObjectEntry(
			_objectDefinition, _adminUser);

		Assert.assertNotNull(
			_objectEntryService.getObjectEntry(
				adminObjectEntry.getObjectEntryId()));

		_setUser(_user);

		ObjectEntry userObjectEntry = _addObjectEntry(_objectDefinition, _user);

		Assert.assertNotNull(
			_objectEntryService.getObjectEntry(
				userObjectEntry.getObjectEntryId()));

		_assertPrincipalException(
			ActionKeys.VIEW, _objectDefinition, adminObjectEntry);

		_setUser(_guestUser);

		_assertPrincipalException(
			ActionKeys.VIEW, _objectDefinition, adminObjectEntry);

		ObjectEntry guestUserObjectEntry = _addObjectEntry(
			_objectDefinition, _guestUser);

		_assertPrincipalException(
			ActionKeys.VIEW, _objectDefinition, guestUserObjectEntry);

		Role guestRole = _roleLocalService.getRole(
			TestPropsValues.getCompanyId(), RoleConstants.GUEST);

		_resourcePermissionLocalService.addResourcePermission(
			TestPropsValues.getCompanyId(), _objectDefinition.getClassName(),
			ResourceConstants.SCOPE_COMPANY,
			String.valueOf(TestPropsValues.getCompanyId()),
			guestRole.getRoleId(), ActionKeys.VIEW);

		Assert.assertNotNull(
			_objectEntryService.getObjectEntry(
				adminObjectEntry.getObjectEntryId()));
	}

	@Test
	public void testGetOrDeleteObjectEntryWithAccountEntryRestricted()
		throws Exception {

		_objectDefinition.setAccountEntryRestricted(true);

		ObjectDefinition accountEntryObjectDefinition =
			_objectDefinitionLocalService.fetchObjectDefinition(
				TestPropsValues.getCompanyId(), "accountEntry");

		ObjectRelationship objectRelationship =
			_objectRelationshipLocalService.addObjectRelationship(
				TestPropsValues.getUserId(),
				accountEntryObjectDefinition.getObjectDefinitionId(),
				_objectDefinition.getObjectDefinitionId(), 0,
				ObjectRelationshipConstants.DELETION_TYPE_PREVENT,
				LocalizedMapUtil.getLocalizedMap(RandomTestUtil.randomString()),
				"relationship", ObjectRelationshipConstants.TYPE_ONE_TO_MANY);

		_objectDefinition.setAccountEntryRestrictedObjectFieldId(
			objectRelationship.getObjectFieldId2());

		_objectDefinition =
			_objectDefinitionLocalService.updateObjectDefinition(
				_objectDefinition);

		AccountEntry accountEntry = _accountEntryLocalService.addAccountEntry(
			TestPropsValues.getUserId(),
			AccountConstants.PARENT_ACCOUNT_ENTRY_ID_DEFAULT, "account", null,
			null, null, null, null,
			AccountConstants.ACCOUNT_ENTRY_TYPE_BUSINESS,
			WorkflowConstants.STATUS_APPROVED,
			ServiceContextTestUtil.getServiceContext());

		ObjectEntry objectEntry = _objectEntryLocalService.addObjectEntry(
			TestPropsValues.getUserId(), 0,
			_objectDefinition.getObjectDefinitionId(),
			HashMapBuilder.<String, Serializable>put(
				"r_relationship_accountEntryId",
				accountEntry.getAccountEntryId()
			).build(),
			ServiceContextTestUtil.getServiceContext(
				TestPropsValues.getGroupId(), TestPropsValues.getUserId()));

		_setUser(_user);

		_resourcePermissionLocalService.addModelResourcePermissions(
			TestPropsValues.getCompanyId(), TestPropsValues.getGroupId(),
			_user.getUserId(), _objectDefinition.getClassName(),
			String.valueOf(objectEntry.getObjectEntryId()),
			ModelPermissionsFactory.create(
				HashMapBuilder.put(
					RoleConstants.USER,
					new String[] {ActionKeys.DELETE, ActionKeys.VIEW}
				).build(),
				_objectDefinition.getClassName()));

		Assert.assertNotNull(
			_objectEntryService.getObjectEntry(objectEntry.getObjectEntryId()));

		_objectEntryService.deleteObjectEntry(objectEntry.getObjectEntryId());

		_accountEntryLocalService.deleteAccountEntry(accountEntry);
	}

	@Test
	public void testSearchObjectEntries() throws Exception {
		_setUser(_adminUser);

		ObjectEntry objectEntry1 = _addObjectEntry(
			_objectDefinition, _adminUser);
		ObjectEntry objectEntry2 = _addObjectEntry(
			_objectDefinition, _adminUser);

		BaseModelSearchResult<ObjectEntry> baseModelSearchResult =
			_objectEntryLocalService.searchObjectEntries(
				0, _objectDefinition.getObjectDefinitionId(), null, 0, 20);

		Assert.assertEquals(2, baseModelSearchResult.getLength());

		_setUser(_user);

		baseModelSearchResult = _objectEntryLocalService.searchObjectEntries(
			0, _objectDefinition.getObjectDefinitionId(), null, 0, 20);

		Assert.assertEquals(0, baseModelSearchResult.getLength());

		_objectEntryLocalService.deleteObjectEntry(objectEntry1);
		_objectEntryLocalService.deleteObjectEntry(objectEntry2);
	}

	private ObjectEntry _addObjectEntry(
			ObjectDefinition objectDefinition, User user)
		throws Exception {

		return _objectEntryLocalService.addObjectEntry(
			user.getUserId(), 0, objectDefinition.getObjectDefinitionId(),
			HashMapBuilder.<String, Serializable>put(
				"firstName", RandomStringUtils.randomAlphabetic(5)
			).put(
				"LastName", RandomStringUtils.randomAlphabetic(5)
			).build(),
			ServiceContextTestUtil.getServiceContext(
				TestPropsValues.getGroupId(), user.getUserId()));
	}

	private void _assertPrincipalException(
			String action, ObjectDefinition objectDefinition,
			ObjectEntry objectEntry)
		throws Exception {

		PermissionChecker permissionChecker =
			PermissionThreadLocal.getPermissionChecker();

		try {
			if (Objects.equals(action, ActionKeys.DELETE)) {
				_testDeleteObjectEntry(_objectDefinition, _adminUser, null);
			}
			else if (Objects.equals(action, ActionKeys.VIEW)) {
				_objectEntryService.getObjectEntry(
					objectEntry.getObjectEntryId());
			}
			else {
				_objectEntryService.addObjectEntry(
					0, objectDefinition.getObjectDefinitionId(),
					HashMapBuilder.<String, Serializable>put(
						"firstName", RandomStringUtils.randomAlphabetic(5)
					).build(),
					ServiceContextTestUtil.getServiceContext(
						TestPropsValues.getGroupId(),
						permissionChecker.getUserId()));
			}

			Assert.fail();
		}
		catch (PrincipalException.MustHavePermission principalException) {
			String message = principalException.getMessage();

			Assert.assertTrue(
				message.contains(
					StringBundler.concat(
						"User ", permissionChecker.getUserId(), " must have ",
						action, " permission for")));
		}
	}

	private Tree _createTreeAndPublishObjectDefinitions() throws Exception {
		Tree tree = TreeTestUtil.createTree(
			_objectDefinitionLocalService, _objectRelationshipLocalService,
			_treeFactory);

		// This way to publish objects in a Root Context will be
		// changed when LPS-193250 be merged
		// >>>

		_iterateNodeObjectDefinitions(
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

				_objectDefinitionLocalService.publishCustomObjectDefinition(
					_adminUser.getUserId(),
					objectDefinition.getObjectDefinitionId());
			});

		// <<<

		return tree;
	}

	private ObjectDefinition _getRootObjectDefinition(Tree tree)
		throws PortalException {

		Iterator<Node> iterator = tree.iterator();

		Node node = iterator.next();

		long rootObjectDefinitionId =
			_objectDefinitionLocalService.getObjectDefinition(
				node.getObjectDefinitionId()
			).getRootObjectDefinitionId();

		return _objectDefinitionLocalService.getObjectDefinition(
			rootObjectDefinitionId);
	}

	private void _iterateNodeObjectDefinitions(
			Tree tree,
			UnsafeConsumer<ObjectDefinition, Exception> unsafeConsumer)
		throws Exception {

		Iterator<Node> iterator = tree.iterator();

		while (iterator.hasNext()) {
			Node node = iterator.next();

			unsafeConsumer.accept(
				_objectDefinitionLocalService.getObjectDefinition(
					node.getObjectDefinitionId()));
		}
	}

	private void _setUser(User user) throws Exception {
		PermissionThreadLocal.setPermissionChecker(
			PermissionCheckerFactoryUtil.create(user));

		PrincipalThreadLocal.setName(user.getUserId());
	}

	private void _testDeleteObjectEntry(
			ObjectDefinition objectDefinition, User ownerUser, User user)
		throws Exception {

		ObjectEntry deleteObjectEntry = null;
		ObjectEntry objectEntry = null;

		try {
			if (user != null) {
				_setUser(user);
			}

			objectEntry = _addObjectEntry(objectDefinition, ownerUser);

			deleteObjectEntry = _objectEntryService.deleteObjectEntry(
				objectEntry.getObjectEntryId());
		}
		finally {
			if (deleteObjectEntry == null) {
				_objectEntryLocalService.deleteObjectEntry(objectEntry);
			}
		}
	}

	@Inject
	private AccountEntryLocalService _accountEntryLocalService;

	@Inject
	private AccountEntryUserRelLocalService _accountEntryUserRelLocalService;

	private User _adminUser;
	private User _guestUser;

	@DeleteAfterTestRun
	private ObjectDefinition _objectDefinition;

	@Inject
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	@Inject
	private ObjectEntryLocalService _objectEntryLocalService;

	@Inject
	private ObjectEntryService _objectEntryService;

	@Inject
	private ObjectRelationshipLocalService _objectRelationshipLocalService;

	private PermissionChecker _originalPermissionChecker;

	@Inject
	private ResourcePermissionLocalService _resourcePermissionLocalService;

	@Inject
	private RoleLocalService _roleLocalService;

	@Inject
	private TreeFactory _treeFactory;

	private User _user;

	@Inject(type = UserLocalService.class)
	private UserLocalService _userLocalService;

}