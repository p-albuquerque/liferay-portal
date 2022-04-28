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

package com.liferay.object.internal.item.selector;

import com.liferay.info.item.selector.InfoItemSelectorView;
import com.liferay.item.selector.ItemSelectorReturnType;
import com.liferay.item.selector.ItemSelectorView;
import com.liferay.item.selector.ItemSelectorViewDescriptor;
import com.liferay.item.selector.ItemSelectorViewDescriptorRenderer;
import com.liferay.item.selector.criteria.InfoItemItemSelectorReturnType;
import com.liferay.item.selector.criteria.info.item.criterion.InfoItemItemSelectorCriterion;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectField;
import com.liferay.object.scope.ObjectScopeProvider;
import com.liferay.object.scope.ObjectScopeProviderRegistry;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectFieldLocalService;
import com.liferay.object.system.SystemObjectDefinitionMetadata;
import com.liferay.petra.sql.dsl.DSLQueryFactoryUtil;
import com.liferay.petra.sql.dsl.query.DSLQuery;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.dao.search.SearchContainer;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.model.BaseModel;
import com.liferay.portal.kernel.service.PersistedModelLocalService;
import com.liferay.portal.kernel.service.PersistedModelLocalServiceRegistry;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.JavaConstants;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.WebKeys;

import java.io.IOException;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.portlet.PortletRequest;
import javax.portlet.PortletURL;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * @author Gabriel Albuquerque
 */
public class SystemObjectEntryItemSelectorView
	implements InfoItemSelectorView,
			   ItemSelectorView<InfoItemItemSelectorCriterion> {

	public SystemObjectEntryItemSelectorView(
		ItemSelectorViewDescriptorRenderer<InfoItemItemSelectorCriterion>
			itemSelectorViewDescriptorRenderer,
		ObjectDefinition objectDefinition,
		ObjectDefinitionLocalService objectDefinitionLocalService,
		ObjectFieldLocalService objectFieldLocalService,
		ObjectScopeProviderRegistry objectScopeProviderRegistry,
		PersistedModelLocalServiceRegistry persistedModelLocalServiceRegistry,
		SystemObjectDefinitionMetadata systemObjectDefinitionMetadata,
		Portal portal) {

		System.out.println("SystemObjectEntryItemSelectorView - HERE");

		_itemSelectorViewDescriptorRenderer =
			itemSelectorViewDescriptorRenderer;
		_objectDefinition = objectDefinition;
		_objectDefinitionLocalService = objectDefinitionLocalService;
		_objectFieldLocalService = objectFieldLocalService;
		_objectScopeProviderRegistry = objectScopeProviderRegistry;
		_persistedModelLocalServiceRegistry =
			persistedModelLocalServiceRegistry;
		_systemObjectDefinitionMetadata = systemObjectDefinitionMetadata;
		_portal = portal;
	}

	@Override
	public String getClassName() {
		return _objectDefinition.getClassName();
	}

	@Override
	public Class<InfoItemItemSelectorCriterion>
		getItemSelectorCriterionClass() {

		return InfoItemItemSelectorCriterion.class;
	}

	@Override
	public List<ItemSelectorReturnType> getSupportedItemSelectorReturnTypes() {
		return _supportedItemSelectorReturnTypes;
	}

	@Override
	public String getTitle(Locale locale) {
		return _objectDefinition.getPluralLabel(locale);
	}

	@Override
	public void renderHTML(
			ServletRequest servletRequest, ServletResponse servletResponse,
			InfoItemItemSelectorCriterion infoItemItemSelectorCriterion,
			PortletURL portletURL, String itemSelectedEventName, boolean search)
		throws IOException, ServletException {

		_itemSelectorViewDescriptorRenderer.renderHTML(
			servletRequest, servletResponse, infoItemItemSelectorCriterion,
			portletURL, itemSelectedEventName, search,
			new ObjectItemSelectorViewDescriptor(
				(HttpServletRequest)servletRequest,
				infoItemItemSelectorCriterion, _objectDefinition,
				_objectScopeProviderRegistry, portletURL));
	}

	private static final List<ItemSelectorReturnType>
		_supportedItemSelectorReturnTypes = Collections.singletonList(
			new InfoItemItemSelectorReturnType());

	private final ItemSelectorViewDescriptorRenderer
		<InfoItemItemSelectorCriterion> _itemSelectorViewDescriptorRenderer;
	private final ObjectDefinition _objectDefinition;
	private final ObjectDefinitionLocalService _objectDefinitionLocalService;
	private final ObjectFieldLocalService _objectFieldLocalService;
	private final ObjectScopeProviderRegistry _objectScopeProviderRegistry;
	private final PersistedModelLocalServiceRegistry
		_persistedModelLocalServiceRegistry;
	private final Portal _portal;
	private final SystemObjectDefinitionMetadata
		_systemObjectDefinitionMetadata;

	private class ObjectItemSelectorViewDescriptor
		implements ItemSelectorViewDescriptor<BaseModel<?>> {

		public ObjectItemSelectorViewDescriptor(
			HttpServletRequest httpServletRequest,
			InfoItemItemSelectorCriterion infoItemItemSelectorCriterion,
			ObjectDefinition objectDefinition,
			ObjectScopeProviderRegistry objectScopeProviderRegistry,
			PortletURL portletURL) {

			_httpServletRequest = httpServletRequest;
			_infoItemItemSelectorCriterion = infoItemItemSelectorCriterion;
			_objectDefinition = objectDefinition;
			_objectScopeProviderRegistry = objectScopeProviderRegistry;
			_portletURL = portletURL;

			_portletRequest = (PortletRequest)_httpServletRequest.getAttribute(
				JavaConstants.JAVAX_PORTLET_REQUEST);
		}

		@Override
		public String getDefaultDisplayStyle() {
			return "descriptive";
		}

		@Override
		public ItemDescriptor getItemDescriptor(BaseModel<?> baseModel) {
			return new SystemObjectEntryItemDescriptor(
				baseModel, _httpServletRequest);
		}

		@Override
		public ItemSelectorReturnType getItemSelectorReturnType() {
			return new InfoItemItemSelectorReturnType();
		}

		@Override
		public SearchContainer<BaseModel<?>> getSearchContainer()
			throws PortalException {

			SearchContainer<BaseModel<?>> searchContainer =
				new SearchContainer<>(
					_portletRequest, _portletURL, null,
					"no-entries-were-found");

			PersistedModelLocalService persistedModelLocalService =
				_persistedModelLocalServiceRegistry.
					getPersistedModelLocalService(
						_systemObjectDefinitionMetadata.getModelClassName());

			DSLQuery dslQuery = DSLQueryFactoryUtil.select(
				_systemObjectDefinitionMetadata.getTable()
			).from(
				_systemObjectDefinitionMetadata.getTable()
			);

			List<BaseModel<?>> baseModels = persistedModelLocalService.dslQuery(
				dslQuery);

			//TODO Filter it right. With groupId, etc

			dslQuery = DSLQueryFactoryUtil.select(
				_systemObjectDefinitionMetadata.getTable()
			).from(
				_systemObjectDefinitionMetadata.getTable()
			);

			// TODO Why count is not working?

			// int count = persistedModelLocalService.dslQueryCount(dslQuery);

			searchContainer.setResultsAndTotal(
				() -> baseModels, baseModels.size());

			return searchContainer;
		}

		private long _getGroupId() throws PortalException {
			ObjectScopeProvider objectScopeProvider =
				_objectScopeProviderRegistry.getObjectScopeProvider(
					_objectDefinition.getScope());

			if (!objectScopeProvider.isGroupAware()) {
				return 0;
			}

			ServiceContext serviceContext =
				ServiceContextThreadLocal.getServiceContext();

			return objectScopeProvider.getGroupId(serviceContext.getRequest());
		}

		private final HttpServletRequest _httpServletRequest;
		private final InfoItemItemSelectorCriterion
			_infoItemItemSelectorCriterion;
		private final ObjectDefinition _objectDefinition;
		private final ObjectScopeProviderRegistry _objectScopeProviderRegistry;
		private final PortletRequest _portletRequest;
		private final PortletURL _portletURL;

	}

	private class SystemObjectEntryItemDescriptor
		implements ItemSelectorViewDescriptor.ItemDescriptor {

		public SystemObjectEntryItemDescriptor(
			BaseModel<?> baseModel, HttpServletRequest httpServletRequest) {

			_baseModel = baseModel;
			_httpServletRequest = httpServletRequest;
		}

		@Override
		public String getIcon() {
			return null;
		}

		@Override
		public String getImageURL() {
			return null;
		}

		@Override
		public Date getModifiedDate() {
			Map<String, Object> modelAttributes =
				_baseModel.getModelAttributes();

			return (Date)modelAttributes.get("modifiedDate");
		}

		@Override
		public String getPayload() {
			ThemeDisplay themeDisplay =
				(ThemeDisplay)_httpServletRequest.getAttribute(
					WebKeys.THEME_DISPLAY);

			return JSONUtil.put(
				"className", _objectDefinition.getClassName()
			).put(
				"classNameId",
				_portal.getClassNameId(_objectDefinition.getClassName())
			).put(
				"classPK", _baseModel.getPrimaryKeyObj()
			).put(
				"title",
				StringBundler.concat(
					_objectDefinition.getLabel(themeDisplay.getLocale()),
					StringPool.SPACE, _baseModel.getPrimaryKeyObj())
			).toString();
		}

		@Override
		public String getSubtitle(Locale locale) {
			return String.valueOf(_baseModel.getPrimaryKeyObj());
		}

		@Override
		public String getTitle(Locale locale) {
			ObjectField objectField = _objectFieldLocalService.fetchObjectField(
				_objectDefinition.getTitleObjectFieldId());

			if (objectField != null) {
				Map<String, Object> modelAttributes =
					_baseModel.getModelAttributes();

				return (String)modelAttributes.get(
					objectField.getDBColumnName());
			}

			return StringPool.BLANK;
		}

		@Override
		public long getUserId() {
			Map<String, Object> modelAttributes =
				_baseModel.getModelAttributes();

			return (Long)modelAttributes.get("userId");
		}

		@Override
		public String getUserName() {
			Map<String, Object> modelAttributes =
				_baseModel.getModelAttributes();

			return _portal.getUserName(
				(Long)modelAttributes.get("userId"), StringPool.BLANK);
		}

		private final BaseModel<?> _baseModel;
		private final HttpServletRequest _httpServletRequest;

	}

}