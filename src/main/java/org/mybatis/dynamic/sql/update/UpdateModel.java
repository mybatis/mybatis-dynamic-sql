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
package org.mybatis.dynamic.sql.update;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.common.CommonBuilder;
import org.mybatis.dynamic.sql.common.OrderByModel;
import org.mybatis.dynamic.sql.configuration.StatementConfiguration;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.update.render.UpdateRenderer;
import org.mybatis.dynamic.sql.update.render.UpdateStatementProvider;
import org.mybatis.dynamic.sql.util.AbstractColumnMapping;
import org.mybatis.dynamic.sql.util.Validator;
import org.mybatis.dynamic.sql.where.EmbeddedWhereModel;

public class UpdateModel {
    private final SqlTable table;
    private final String tableAlias;
    private final EmbeddedWhereModel whereModel;
    private final List<AbstractColumnMapping> columnMappings;
    private final Long limit;
    private final OrderByModel orderByModel;
    private final StatementConfiguration statementConfiguration;

    private UpdateModel(Builder builder) {
        table = Objects.requireNonNull(builder.table());
        whereModel = builder.whereModel();
        columnMappings = Objects.requireNonNull(builder.columnMappings);
        tableAlias = builder.tableAlias();
        limit = builder.limit();
        orderByModel = builder.orderByModel();
        Validator.assertNotEmpty(columnMappings, "ERROR.17"); //$NON-NLS-1$
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

    public Stream<AbstractColumnMapping> columnMappings() {
        return columnMappings.stream();
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
    public UpdateStatementProvider render(RenderingStrategy renderingStrategy) {
        return UpdateRenderer.withUpdateModel(this)
                .withRenderingStrategy(renderingStrategy)
                .build()
                .render();
    }

    public static Builder withTable(SqlTable table) {
        return new Builder().withTable(table);
    }

    public static class Builder extends CommonBuilder<Builder> {
        private final List<AbstractColumnMapping> columnMappings = new ArrayList<>();

        public Builder withColumnMappings(List<? extends AbstractColumnMapping> columnMappings) {
            this.columnMappings.addAll(columnMappings);
            return this;
        }

        @Override
        protected Builder getThis() {
            return this;
        }

        public UpdateModel build() {
            return new UpdateModel(this);
        }
    }
}
