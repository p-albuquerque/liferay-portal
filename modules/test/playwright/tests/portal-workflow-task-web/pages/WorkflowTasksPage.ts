/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

<<<<<<< Updated upstream
import {expect, Locator, Page} from '@playwright/test';
=======
<<<<<<< Updated upstream
import {Locator, Page} from '@playwright/test';
=======
import {Locator, Page, expect} from '@playwright/test';
>>>>>>> Stashed changes
>>>>>>> Stashed changes

import {clickAndExpectToBeVisible} from '../../../utils/clickAndExpectToBeVisible';
import {PORTLET_URLS} from '../../../utils/portletUrls';
import {waitForSuccessAlert} from '../../../utils/waitForSuccessAlert';

export class WorkflowTasksPage {
	readonly page: Page;

	readonly assignedToMyRolesLink: Locator;

	constructor(page: Page) {
		this.page = page;

		this.assignedToMyRolesLink = page.getByRole('link', {
			name: 'Assigned to my roles',
		});
	}

	async goto(siteUrl?: Site['friendlyUrlPath']) {
		await this.page.goto(
			`/group${siteUrl || '/guest'}${PORTLET_URLS.myWorkflowTasks}`
		);
	}

	async goToAssignedToMyRoles(siteUrl?: Site['friendlyUrlPath']) {
		await this.goto(siteUrl);

		await this.assignedToMyRolesLink.click();
	}

	async approve(articleTitle: string) {
		const row = await this.page
			.getByRole('row')
			.filter({hasText: articleTitle});

		await clickAndExpectToBeVisible({
			autoClick: true,
			target: this.page
				.locator('.dropdown-menu:visible')
				.getByText('Approve', {exact: true}),
			trigger: row.locator('.dropdown-toggle'),
		});

		await this.page.getByRole('button', {name: 'Done'}).click();

		await waitForSuccessAlert(this.page);
	}

	async assignToMe(articleTitle: string) {
		const row = await this.page
			.getByRole('row')
			.filter({hasText: articleTitle});

		await clickAndExpectToBeVisible({
			autoClick: true,
			target: this.page
				.locator('.dropdown-menu:visible')
				.getByText('Assign to Me', {exact: true}),
			trigger: row.locator('.dropdown-toggle'),
		});

		await this.page
			.frameLocator(`iframe[title="Assign to Me"]`)
			.getByRole('button', {name: 'Done'})
			.waitFor();

		await this.page
			.frameLocator(`iframe[title="Assign to Me"]`)
			.getByRole('button', {name: 'Done'})
			.click();

		await waitForSuccessAlert(this.page);
	}

<<<<<<< Updated upstream
	async openTaskCommentsSection(assetTitle: string) {
		await this.page.getByRole('link', { name: assetTitle}).click();

		await this.page.getByRole('button', { name: 'Comments'}).click();

		await expect(
			this.page.getByRole('button', { name: 'Comments', exact: true })
=======
<<<<<<< Updated upstream
=======
	async openTaskCommentsSection(assetTitle: string) {
		await this.page.getByRole('link', {name: assetTitle}).click();

		await this.page.getByRole('button', {name: 'Comments'}).click();

		await expect(
			this.page.getByRole('button', {name: 'Comments', exact: true})
>>>>>>> Stashed changes
		).toBeVisible();
	}

	async subscribeToTaskComments(assetTitle: string) {
		await this.openTaskCommentsSection(assetTitle);

		await this.page.getByLabel('Subscribe to Comments').click();

		await expect(
			this.page.getByText('Success:Your request completed successfully.')
		).toBeVisible();
	}

<<<<<<< Updated upstream
=======
>>>>>>> Stashed changes
>>>>>>> Stashed changes
	async reject(articleTitle: string) {
		const row = await this.page
			.getByRole('row')
			.filter({hasText: articleTitle});

		await clickAndExpectToBeVisible({
			autoClick: true,
			target: this.page
				.locator('.dropdown-menu:visible')
				.getByText('Reject', {exact: true}),
			trigger: row.locator('.dropdown-toggle'),
		});

		await this.page.getByRole('button', {name: 'Done'}).click();

		await waitForSuccessAlert(this.page);
	}

	async resubmit(articleTitle: string) {
		await this.goto();

		await this.page.reload();

		const row = await this.page
			.getByRole('row')
			.filter({hasText: articleTitle});

		await clickAndExpectToBeVisible({
			autoClick: true,
			target: this.page
				.locator('.dropdown-menu:visible')
				.getByText('Resubmit', {exact: true}),
			trigger: row.locator('.dropdown-toggle'),
		});

		await this.page.getByRole('button', {name: 'Done'}).click();

		await waitForSuccessAlert(this.page);
	}
<<<<<<< Updated upstream
=======
<<<<<<< Updated upstream
=======
>>>>>>> Stashed changes

	async writeTaskComment(assetTitle: string, comment: string) {
		await this.openTaskCommentsSection(assetTitle);

<<<<<<< Updated upstream
		await this.page.frameLocator('iframe').getByRole('textbox').fill(comment);

		await this.page.getByRole('button', { name: 'Reply' }).click();
	}
=======
		await this.page
			.frameLocator('iframe')
			.getByRole('textbox')
			.fill(comment);

		await this.page.getByRole('button', {name: 'Reply'}).click();
	}
>>>>>>> Stashed changes
>>>>>>> Stashed changes
}
