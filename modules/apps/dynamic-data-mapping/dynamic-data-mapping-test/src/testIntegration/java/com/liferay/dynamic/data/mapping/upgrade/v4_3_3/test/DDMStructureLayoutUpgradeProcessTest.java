/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.dynamic.data.mapping.upgrade.v4_3_3.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.change.tracking.test.util.BaseCTUpgradeProcessTestCase;
import com.liferay.document.library.kernel.model.DLFileEntryMetadata;
import com.liferay.dynamic.data.mapping.model.DDMForm;
import com.liferay.dynamic.data.mapping.model.DDMFormField;
import com.liferay.dynamic.data.mapping.model.DDMFormInstance;
import com.liferay.dynamic.data.mapping.model.DDMFormLayout;
import com.liferay.dynamic.data.mapping.model.DDMFormLayoutColumn;
import com.liferay.dynamic.data.mapping.model.DDMFormLayoutPage;
import com.liferay.dynamic.data.mapping.model.DDMFormLayoutRow;
import com.liferay.dynamic.data.mapping.model.DDMStructure;
import com.liferay.dynamic.data.mapping.model.DDMStructureLayout;
import com.liferay.dynamic.data.mapping.model.DDMStructureVersion;
import com.liferay.dynamic.data.mapping.model.LocalizedValue;
import com.liferay.dynamic.data.mapping.service.DDMStructureLayoutLocalService;
import com.liferay.dynamic.data.mapping.service.DDMStructureLocalService;
import com.liferay.dynamic.data.mapping.service.DDMStructureVersionLocalService;
import com.liferay.dynamic.data.mapping.test.util.DDMFormTestUtil;
import com.liferay.dynamic.data.mapping.test.util.DDMStructureLayoutTestHelper;
import com.liferay.dynamic.data.mapping.test.util.DDMStructureTestHelper;
import com.liferay.dynamic.data.mapping.test.util.DDMStructureTestUtil;
import com.liferay.dynamic.data.mapping.util.DDM;
import com.liferay.journal.model.JournalArticle;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.change.tracking.CTModel;
import com.liferay.portal.kernel.service.ClassNameLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.change.tracking.CTService;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;
import com.liferay.portal.upgrade.registry.UpgradeStepRegistrator;
import com.liferay.portal.upgrade.test.util.UpgradeTestUtil;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.runner.RunWith;

import java.util.List;

/**
 * @author Paulo Albuquerque
 */
@RunWith(Arquillian.class)
public class DDMStructureLayoutUpgradeProcessTest
	extends BaseCTUpgradeProcessTestCase {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Before
	public void setUp() throws Exception {
		_group = GroupTestUtil.addGroup();

		_ddmStructureTestHelper = new DDMStructureTestHelper(
			PortalUtil.getClassNameId(DDMFormInstance.class.getName()), _group);
		_ddmStructureLayoutTestHelper = new DDMStructureLayoutTestHelper(_group);

		_ddmFormLayout = new DDMFormLayout();

		_ddmFormLayout.setDefaultLocale(LocaleUtil.US);
		_ddmFormLayout.setPaginationMode(DDMFormLayout.WIZARD_MODE);

		List<DDMFormLayoutColumn> ddmFormLayoutColumns =
			_ddmStructureLayoutTestHelper.createDDMFormLayoutColumns(
				"Text1", "Text2");

		DDMFormLayoutRow ddmFormLayoutRow =
			_ddmStructureLayoutTestHelper.createDDMFormLayoutRow(ddmFormLayoutColumns);

		DDMFormLayoutPage ddmFormLayoutPage = createDDMFormLayoutPage(
			ddmFormLayoutRow);

		_ddmFormLayout.addDDMFormLayoutPage(ddmFormLayoutPage);

		DDMFormField ddmFormField = DDMFormTestUtil.createTextDDMFormField(
			"TextField1", false, true, false);

		_ddmForm = DDMFormTestUtil.createDDMForm();

		_ddmForm.addDDMFormField(ddmFormField);

		_ddmStructure = _ddmStructureTestHelper.addStructure(
			_ddmForm, _ddmFormLayout);

		_serviceContext = ServiceContextTestUtil.getServiceContext(
			_group.getGroupId(), TestPropsValues.getUserId());
	}

	@Override
	protected CTModel<?> addCTModel() throws Exception {
		return _ddmStructure.fetchDDMStructureLayout();
	}

	@Override
	protected CTService<?> getCTService() {
		return _ddmStructureVersionLocalService;
	}

	@Override
	protected void runUpgrade() throws Exception {
		UpgradeProcess upgradeProcess = UpgradeTestUtil.getUpgradeStep(
			_upgradeStepRegistrator, _CLASS_NAME);

		upgradeProcess.upgrade();
	}

	@Override
	protected CTModel<?> updateCTModel(CTModel<?> ctModel) throws Exception {
		_ddmFormLayout = new DDMFormLayout();

		_ddmFormLayout.setDefaultLocale(LocaleUtil.US);

		List<DDMFormLayoutColumn> ddmFormLayoutColumns =
			_ddmStructureLayoutTestHelper.createDDMFormLayoutColumns("Text3, Text4");

		DDMFormLayoutRow ddmFormLayoutRow =
			_ddmStructureLayoutTestHelper.createDDMFormLayoutRow(
				ddmFormLayoutColumns);

		DDMFormLayoutPage ddmFormLayoutPage = createDDMFormLayoutPage(
			ddmFormLayoutRow);

		_ddmFormLayout.addDDMFormLayoutPage(ddmFormLayoutPage);

		_ddmStructure = _ddmStructureTestHelper.updateStructure(_ddmStructure.getStructureId(),
			_ddmStructure.getName(), _ddmStructure.getDescription(), _ddmForm,
			_ddmFormLayout);

		return _ddmStructure.fetchDDMStructureLayout();
	}

	private void setUpDDMForm(String... fieldName) {
		_ddmForm = DDMFormTestUtil.createDDMForm(fieldName);
	}

	private void setUpDDMFormLayout(String... ddmFormFieldName) {
		_ddmFormLayout = new DDMFormLayout();

		_ddmFormLayout.setDefaultLocale(LocaleUtil.US);

		List<DDMFormLayoutColumn> ddmFormLayoutColumns =
			_ddmStructureLayoutTestHelper.createDDMFormLayoutColumns(ddmFormFieldName);

		DDMFormLayoutRow ddmFormLayoutRow =
			_ddmStructureLayoutTestHelper.createDDMFormLayoutRow(
			ddmFormLayoutColumns);

		DDMFormLayoutPage ddmFormLayoutPage = createDDMFormLayoutPage(
			ddmFormLayoutRow);

		_ddmFormLayout.addDDMFormLayoutPage(ddmFormLayoutPage);
	}

	private DDMFormLayoutPage createDDMFormLayoutPage(
		DDMFormLayoutRow ddmFormLayoutRow) {

		DDMFormLayoutPage ddmFormLayoutPage = new DDMFormLayoutPage();

		LocalizedValue ddmFormLayoutPageTitle = new LocalizedValue(
			LocaleUtil.US);

		ddmFormLayoutPageTitle.addString(LocaleUtil.US, "Page1");

		ddmFormLayoutPage.setTitle(ddmFormLayoutPageTitle);

		ddmFormLayoutPage.addDDMFormLayoutRow(ddmFormLayoutRow);

		return ddmFormLayoutPage;
	}

	private DDMFormLayout _ddmFormLayout;
	private static final String _CLASS_NAME =
		"com.liferay.dynamic.data.mapping.internal.upgrade.v4_3_3." +
			"DDMStructureLayoutUpgradeProcess";

	@Inject(
		filter = "(&(component.name=com.liferay.dynamic.data.mapping.internal.upgrade.registry.DDMServiceUpgradeStepRegistrator))"
	)
	private static UpgradeStepRegistrator _upgradeStepRegistrator;

	@Inject
	private ClassNameLocalService _classNameLocalService;
	private DDMForm _ddmForm;

	@DeleteAfterTestRun
	private DDMStructure _ddmStructure;

	protected DDMStructureTestHelper _ddmStructureTestHelper;

	private DDMStructureLayoutTestHelper _ddmStructureLayoutTestHelper;

	@Inject
	private DDMStructureLayoutLocalService _ddmStructureLayoutLocalService;

	@Inject
	private DDMStructureVersionLocalService _ddmStructureVersionLocalService;


	@DeleteAfterTestRun
	private Group _group;

	@Inject
	private DDM _ddm;

	@Inject
	private JSONFactory _jsonFactory;

	private ServiceContext _serviceContext;

	@Inject
	private DDMStructureLocalService _ddmStructureLocalService;

}