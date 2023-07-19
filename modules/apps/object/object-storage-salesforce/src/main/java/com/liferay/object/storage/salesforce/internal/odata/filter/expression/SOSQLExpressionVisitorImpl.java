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

package com.liferay.object.storage.salesforce.internal.odata.filter.expression;

import com.liferay.object.model.ObjectField;
import com.liferay.object.service.ObjectFieldLocalService;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.util.DateFormatFactoryUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.odata.filter.expression.BinaryExpression;
import com.liferay.portal.odata.filter.expression.Expression;
import com.liferay.portal.odata.filter.expression.ExpressionVisitException;
import com.liferay.portal.odata.filter.expression.ExpressionVisitor;
import com.liferay.portal.odata.filter.expression.ListExpression;
import com.liferay.portal.odata.filter.expression.LiteralExpression;
import com.liferay.portal.odata.filter.expression.MemberExpression;
import com.liferay.portal.odata.filter.expression.MethodExpression;
import com.liferay.portal.odata.filter.expression.PrimitivePropertyExpression;

import java.util.List;
import java.util.Objects;

/**
 * @author Paulo Albuquerque
 */
public class SOSQLExpressionVisitorImpl implements ExpressionVisitor<Object> {

	public SOSQLExpressionVisitorImpl(
		long objectDefinitionId,
		ObjectFieldLocalService objectFieldLocalService) {

		_objectDefinitionId = objectDefinitionId;
		_objectFieldLocalService = objectFieldLocalService;
	}

	@Override
	public String visitBinaryExpressionOperation(
			BinaryExpression.Operation operation, Object left, Object right)
		throws ExpressionVisitException {

		StringBuilder sb = new StringBuilder();

		if (Objects.equals(BinaryExpression.Operation.AND, operation)) {
			_setBinaryOperator(left, right, " AND ", sb);
		}
		else if (Objects.equals(BinaryExpression.Operation.OR, operation)) {
			_setBinaryOperator(left, right, " OR ", sb);
		}

		if (Objects.equals(BinaryExpression.Operation.EQ, operation)) {
			_setBinaryOperator(left, right, " = ", sb);
		}
		else if (Objects.equals(BinaryExpression.Operation.GE, operation)) {
			_setBinaryOperator(left, right, " >= ", sb);
		}
		else if (Objects.equals(BinaryExpression.Operation.GT, operation)) {
			_setBinaryOperator(left, right, " > ", sb);
		}
		else if (Objects.equals(BinaryExpression.Operation.LE, operation)) {
			_setBinaryOperator(left, right, " <= ", sb);
		}
		else if (Objects.equals(BinaryExpression.Operation.LT, operation)) {
			_setBinaryOperator(left, right, " < ", sb);
		}
		else if (Objects.equals(BinaryExpression.Operation.NE, operation)) {
			_setBinaryOperator(left, right, " != ", sb);
		}

		return sb.toString();
	}

	@Override
	public Object visitListExpressionOperation(
			ListExpression.Operation operation, Object left, List<Object> right)
		throws ExpressionVisitException {

		return null;
	}

	@Override
	public Object visitLiteralExpression(LiteralExpression literalExpression)
		throws ExpressionVisitException {

		if (Objects.equals(
				LiteralExpression.Type.BOOLEAN, literalExpression.getType())) {

			return GetterUtil.getBoolean(literalExpression.getText());
		}
		else if (Objects.equals(
					LiteralExpression.Type.DATE, literalExpression.getType())) {

			return GetterUtil.getDate(
				literalExpression.getText(),
				DateFormatFactoryUtil.getSimpleDateFormat("yyyy-MM-dd"));
		}
		else if (Objects.equals(
					LiteralExpression.Type.DOUBLE,
					literalExpression.getType())) {

			return GetterUtil.getDouble(literalExpression.getText());
		}
		else if (Objects.equals(
					LiteralExpression.Type.INTEGER,
					literalExpression.getType())) {

			return GetterUtil.getLong(literalExpression.getText());
		}
		else if (Objects.equals(
					LiteralExpression.Type.NULL, literalExpression.getType())) {

			return null;
		}
		else if (Objects.equals(
					LiteralExpression.Type.STRING,
					literalExpression.getType())) {

			return StringUtil.replace(
				literalExpression.getText(), StringPool.DOUBLE_APOSTROPHE,
				StringPool.APOSTROPHE);
		}

		return literalExpression.getText();
	}

	@Override
	public Object visitMemberExpression(MemberExpression memberExpression)
		throws ExpressionVisitException {

		Expression expression = memberExpression.getExpression();

		return expression.accept(this);
	}

	@Override
	public Object visitMethodExpression(
			List<Object> expressions, MethodExpression.Type type)
		throws ExpressionVisitException {

		return null;
	}

	@Override
	public Object visitPrimitivePropertyExpression(
			PrimitivePropertyExpression primitivePropertyExpression)
		throws ExpressionVisitException {

		ObjectField objectField = _objectFieldLocalService.fetchObjectField(
			_objectDefinitionId, primitivePropertyExpression.getName());

		if (objectField == null) {
			return primitivePropertyExpression.getName();
		}

		return objectField.getExternalReferenceCode();
	}

	private void _setBinaryOperator(
		Object left, Object right, String operator, StringBuilder sb) {

		sb.append(left);
		sb.append(operator);
		sb.append(right);
	}

	private final long _objectDefinitionId;
	private final ObjectFieldLocalService _objectFieldLocalService;

}