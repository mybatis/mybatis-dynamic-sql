/*
 *    Copyright 2016-2024 the original author or authors.
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
package org.mybatis.dynamic.sql.delete;

import java.util.Objects;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.common.CommonBuilder;
import org.mybatis.dynamic.sql.common.OrderByModel;
import org.mybatis.dynamic.sql.configuration.StatementConfiguration;
import org.mybatis.dynamic.sql.delete.render.DeleteStatementProvider;
import org.mybatis.dynamic.sql.render.RendererFactory;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.where.EmbeddedWhereModel;

public class DeleteModel {
    private final SqlTable table;
    private final String tableAlias;
    private final EmbeddedWhereModel whereModel;
    private final Long limit;
    private final OrderByModel orderByModel;
    private final StatementConfiguration statementConfiguration;

    private DeleteModel(Builder builder) {
        table = Objects.requireNonNull(builder.table());
        whereModel = builder.whereModel();
        tableAlias = builder.tableAlias();
        limit = builder.limit();
        orderByModel = builder.orderByModel();
        statementConfiguration = Objects.requireNonNull(builder.statementConfiguration());
    }

    public SqlTable table() {
        return table;
    }

    public Optional<String> tableAlias() {
        return Optional.ofNullable(tableAlias);
    }

    public Optional<EmbeddedWhereModel> whereModel() {
        return Optional.ofNullable(whereModel);
    }

    public Optional<Long> limit() {
        return Optional.ofNullable(limit);
    }

    public Optional<OrderByModel> orderByModel() {
        return Optional.ofNullable(orderByModel);
    }

    public StatementConfiguration statementConfiguration() {
        return statementConfiguration;
    }

    @NotNull
    public DeleteStatementProvider render(RenderingStrategy renderingStrategy) {
        return RendererFactory.createDeleteRenderer(this)
                .render(renderingStrategy);
    }

    public static Builder withTable(SqlTable table) {
        return new Builder().withTable(table);
    }

    public static class Builder extends CommonBuilder<Builder> {
        @Override
        protected Builder getThis() {
            return this;
        }

        public DeleteModel build() {
            return new DeleteModel(this);
        }
    }
}
