/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.object.internal.upgrade.v9_0_3;

import com.liferay.object.constants.ObjectFieldConstants;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;

/**
 * @author Paulo Albuquerque
 */
public class ObjectFieldUpgradeProcess extends UpgradeProcess {

	@Override
	protected void doUpgrade() throws Exception {
		runSQL(
			StringBundler.concat(
				"update ObjectField set indexed = [$FALSE$] where ",
				"(businessType like '",
				ObjectFieldConstants.BUSINESS_TYPE_AGGREGATION, "') or ",
				"(businessType like '",
				ObjectFieldConstants.BUSINESS_TYPE_FORMULA, "')"));
	}

}