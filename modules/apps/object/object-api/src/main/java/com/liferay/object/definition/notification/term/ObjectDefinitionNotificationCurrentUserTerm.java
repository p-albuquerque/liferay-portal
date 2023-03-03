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

package com.liferay.object.definition.notification.term;

/**
 * @author Paulo Albuquerque
 */
public enum ObjectDefinitionNotificationCurrentUserTerm {

	CURRENT_USER_EMAIL("current-user-email-address", "[%CURRENT_USER_EMAIL%]"),
	CURRENT_USER_FIRST_NAME(
		"current-user-first-name", "[%CURRENT_USER_FIRSTNAME%]"),
	CURRENT_USER_ID("current-user-id", "[%CURRENT_USER_ID%]"),
	CURRENT_USER_LAST_NAME(
		"current-user-last-name", "[%CURRENT_USER_LASTNAME%]"),
	CURRENT_USER_MIDDLE_NAME(
		"current-user-middle-name", "[%CURRENT_USER_MIDDLENAME%]"),
	CURRENT_USER_PREFIX("current-user-prefix", "[%CURRENT_USER_PREFIX%]"),
	CURRENT_USER_SUFFIX("current-user-suffix", "[%CURRENT_USER_SUFFIX%]");

	public boolean contains(String termName) {
		for (ObjectDefinitionNotificationCurrentUserTerm
				objectDefinitionNotificationCurrentUserTerm : values()) {

			if (StringUtil.equals(
					termName,
					objectDefinitionNotificationCurrentUserTerm._termName)) {

				return true;
			}
		}

		return false;
	}

	public String getKey() {
		return _key;
	}

	public String getTermName() {
		return _termName;
	}

	private ObjectDefinitionNotificationCurrentUserTerm(
		String key, String termName) {

		_key = key;
		_termName = termName;
	}

	private final String _key;
	private final String _termName;

}