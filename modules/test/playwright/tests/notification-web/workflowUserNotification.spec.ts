/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {expect, mergeTests} from '@playwright/test';

import {apiHelpersTest} from '../../fixtures/apiHelpersTest';
import {isolatedSiteTest} from '../../fixtures/isolatedSiteTest';
import {loginTest} from '../../fixtures/loginTest';
import {workflowPagesTest} from '../../fixtures/workflowPagesTest';
import getRandomString from '../../utils/getRandomString';
import performLogin, {performLogout} from '../../utils/performLogin';
import {blogsPagesTest} from '../blogs-web/fixtures/blogsPagesTest';
import {journalPagesTest} from '../journal-web/fixtures/journalPagesTest';
import { assert } from 'console';

export const test = mergeTests(
	apiHelpersTest,
	loginTest(),
	journalPagesTest,
	blogsPagesTest,
	workflowPagesTest,
	isolatedSiteTest
);

let user;

test.beforeEach(async ({apiHelpers}) => {
	user = await apiHelpers.headlessAdminUser.getUserAccountByEmailAddress(
		'demo.unprivileged@liferay.com'
	);
	await apiHelpers.headlessAdminUser.assignUserToRole(
		'Portal Content Reviewer',
		user.id
	);
});

test.afterEach(async ({apiHelpers, blogsEditBlogEntryPage, page, workflowPage}) => {
    const role = await apiHelpers.headlessAdminUser.getRoles('Portal Content Reviewer');

	await apiHelpers.headlessAdminUser.deleteRoleUserAccountAssociation(
		role.id,
		user.id
	);

	await workflowPage.goto();

	const row = await page
		.getByRole('row')
		.filter({hasText: 'Blogs Entry'});

	const workflowEnabled = await row
		.getByTitle('Workflow Definition')
		.filter({hasText: 'Single Approver'});

	if (workflowEnabled) {
		await workflowPage.changeWorkflow(
			'Blogs Entry',
			'No Workflow',
			{
				disable: true,
			}
		);
	}

    await blogsEditBlogEntryPage.goto();

    await page.locator('[id="_com_liferay_blogs_web_portlet_BlogsAdminPortlet_blogEntries_1"]').getByLabel('', { exact: true }).check();

    await page.getByRole('button', { name: 'Delete' }).click();
});

test('Notification from comment subscription', async ({
	blogsEditBlogEntryPage,
	page,
	workflowPage,
	workflowTasksPage,
}) => {
	await workflowPage.goto();
	await workflowPage.changeWorkflow('Blogs Entry', 'Single Approver');

	await blogsEditBlogEntryPage.goto();

	const title = getRandomString();

	await blogsEditBlogEntryPage.editBlogEntry({
		content: getRandomString(),
		publish: false,
		title,
	});

	await page
		.getByRole('button', {
			name: 'Submit for Workflow',
		})
		.click();

	await performLogout(page);

	await performLogin(page, user.alternateName);

	await workflowTasksPage.goToAssignedToMyRoles();

	await workflowTasksPage.assignToMe(title);

	await workflowTasksPage.reject(title);

	await performLogout(page);

	await performLogin(page, 'test');

	await workflowTasksPage.goto();

	await workflowTasksPage.subscribeToTaskComments(title);

	await performLogout(page);

	await performLogin(page, user.alternateName);

	await workflowTasksPage.goto();

	await workflowTasksPage.writeTaskComment(title, getRandomString());

	await performLogout(page);

	await performLogin(page, 'test');

	await page.getByLabel('Test Test User Profile').click();
	await page.getByRole('menuitem', {name: 'Notification'}).click();

	await page
		.getByRole('link', {
			name: `${user.name} added a new comment to ${title}`,
		})
		.click();

    await expect(page.locator('div[id$=_com_liferay_blogs_web_portlet_BlogsPortlet_discussionContainer]')).toBeVisible();
});
