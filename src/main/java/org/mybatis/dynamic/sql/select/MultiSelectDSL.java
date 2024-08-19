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
package org.mybatis.dynamic.sql.select;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;
import org.mybatis.dynamic.sql.SortSpecification;
import org.mybatis.dynamic.sql.common.OrderByModel;
import org.mybatis.dynamic.sql.configuration.StatementConfiguration;
import org.mybatis.dynamic.sql.util.Buildable;
import org.mybatis.dynamic.sql.util.ConfigurableStatement;

public class MultiSelectDSL implements Buildable<MultiSelectModel>, ConfigurableStatement<MultiSelectDSL>,
        PagingDSL<MultiSelectModel> {
    private final List<UnionQuery> unionQueries = new ArrayList<>();
    private final SelectModel initialSelect;
    private OrderByModel orderByModel;
    private Long limit;
    private Long offset;
    private Long fetchFirstRows;
    private final StatementConfiguration statementConfiguration = new StatementConfiguration();

    public MultiSelectDSL(Buildable<SelectModel> builder) {
        initialSelect = builder.build();
    }

    public MultiSelectDSL union(Buildable<SelectModel> builder) {
        unionQueries.add(new UnionQuery("union", builder.build())); //$NON-NLS-1$
        return this;
    }

    public MultiSelectDSL unionAll(Buildable<SelectModel> builder) {
        unionQueries.add(new UnionQuery("union all", builder.build())); //$NON-NLS-1$
        return this;
    }

    public MultiSelectDSL orderBy(SortSpecification... columns) {
        return orderBy(Arrays.asList(columns));
    }

    public MultiSelectDSL orderBy(Collection<? extends SortSpecification> columns) {
        orderByModel = OrderByModel.of(columns);
        return this;
    }

    @Override
    public LimitFinisher<MultiSelectModel> limitWhenPresent(Long limit) {
        this.limit = limit;
        return new LocalLimitFinisher();
    }

    @Override
    public OffsetFirstFinisher<MultiSelectModel> offsetWhenPresent(Long offset) {
        this.offset = offset;
        return new LocalOffsetFirstFinisher();
    }

    @Override
    public FetchFirstFinisher<MultiSelectModel> fetchFirstWhenPresent(Long fetchFirstRows) {
        this.fetchFirstRows = fetchFirstRows;
        return () -> this;
    }

    @NotNull
    @Override
    public MultiSelectModel build() {
        return new MultiSelectModel.Builder()
                .withInitialSelect(initialSelect)
                .withUnionQueries(unionQueries)
                .withOrderByModel(orderByModel)
                .withPagingModel(buildPagingModel().orElse(null))
                .withStatementConfiguration(statementConfiguration)
                .build();
    }

    private Optional<PagingModel> buildPagingModel() {
        return new PagingModel.Builder()
                .withLimit(limit)
                .withOffset(offset)
                .withFetchFirstRows(fetchFirstRows)
                .build();
    }

    @Override
    public MultiSelectDSL configureStatement(Consumer<StatementConfiguration> consumer) {
        consumer.accept(statementConfiguration);
        return this;
    }

    abstract class BaseBuildable implements Buildable<MultiSelectModel> {
        @NotNull
        @Override
        public MultiSelectModel build() {
            return MultiSelectDSL.this.build();
        }
    }

    class LocalOffsetFirstFinisher extends BaseBuildable implements OffsetFirstFinisher<MultiSelectModel> {
        @Override
        public FetchFirstFinisher<MultiSelectModel> fetchFirstWhenPresent(Long fetchFirstRows) {
            MultiSelectDSL.this.fetchFirstRows = fetchFirstRows;
            return () -> MultiSelectDSL.this;
        }
    }

    class LocalLimitFinisher extends BaseBuildable implements LimitFinisher<MultiSelectModel> {
        @Override
        public Buildable<MultiSelectModel> offsetWhenPresent(Long offset) {
            MultiSelectDSL.this.offset = offset;
            return MultiSelectDSL.this;
        }
    }
}
