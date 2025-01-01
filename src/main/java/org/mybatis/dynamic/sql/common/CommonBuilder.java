/*
 *    Copyright 2016-2025 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.dynamic.sql.common;

import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.configuration.StatementConfiguration;
import org.mybatis.dynamic.sql.where.EmbeddedWhereModel;

/**
 * Builder class shared between the delete and update model builders.
 *
 * @param <T> type of the implementing builder
 */
public abstract class CommonBuilder<T extends CommonBuilder<T>> {
    private SqlTable table;
    private String tableAlias;
    private EmbeddedWhereModel whereModel;
    private Long limit;
    private OrderByModel orderByModel;
    private StatementConfiguration statementConfiguration;

    public SqlTable table() {
        return table;
    }

    public String tableAlias() {
        return tableAlias;
    }

    public EmbeddedWhereModel whereModel() {
        return whereModel;
    }

    public Long limit() {
        return limit;
    }

    public OrderByModel orderByModel() {
        return orderByModel;
    }

    public StatementConfiguration statementConfiguration() {
        return statementConfiguration;
    }

    public T withTable(SqlTable table) {
        this.table = table;
        return getThis();
    }

    public T withTableAlias(String tableAlias) {
        this.tableAlias = tableAlias;
        return getThis();
    }

    public T withWhereModel(EmbeddedWhereModel whereModel) {
        this.whereModel = whereModel;
        return getThis();
    }

    public T withLimit(Long limit) {
        this.limit = limit;
        return getThis();
    }

    public T withOrderByModel(OrderByModel orderByModel) {
        this.orderByModel = orderByModel;
        return getThis();
    }

    public T withStatementConfiguration(StatementConfiguration statementConfiguration) {
        this.statementConfiguration = statementConfiguration;
        return getThis();
    }

    protected abstract T getThis();
}
