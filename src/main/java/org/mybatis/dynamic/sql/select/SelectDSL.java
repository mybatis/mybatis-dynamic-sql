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
package org.mybatis.dynamic.sql.select;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import org.jspecify.annotations.Nullable;
import org.mybatis.dynamic.sql.BasicColumn;
import org.mybatis.dynamic.sql.SortSpecification;
import org.mybatis.dynamic.sql.common.OrderByModel;
import org.mybatis.dynamic.sql.configuration.StatementConfiguration;
import org.mybatis.dynamic.sql.select.QueryExpressionDSL.FromGatherer;
import org.mybatis.dynamic.sql.util.Buildable;
import org.mybatis.dynamic.sql.util.ConfigurableStatement;
import org.mybatis.dynamic.sql.util.Validator;

/**
 * Implements a SQL DSL for building select statements.
 *
 * @author Jeff Butler
 *
 * @param <R>
 *            the type of model produced by this builder, typically SelectModel
 */
public class SelectDSL<R> implements Buildable<R>, ConfigurableStatement<SelectDSL<R>> {

    private final Function<SelectModel, R> adapterFunction;
    private final List<QueryExpressionDSL<R>> queryExpressions = new ArrayList<>();
    private @Nullable OrderByModel orderByModel;
    private @Nullable Long limit;
    private @Nullable Long offset;
    private @Nullable Long fetchFirstRows;
    final StatementConfiguration statementConfiguration = new StatementConfiguration();
    private @Nullable String forClause;
    private @Nullable String waitClause;

    private SelectDSL(Function<SelectModel, R> adapterFunction) {
        this.adapterFunction = Objects.requireNonNull(adapterFunction);
    }

    public static QueryExpressionDSL.FromGatherer<SelectModel> select(BasicColumn... selectList) {
        return select(Arrays.asList(selectList));
    }

    public static QueryExpressionDSL.FromGatherer<SelectModel> select(Collection<? extends BasicColumn> selectList) {
        return select(Function.identity(), selectList);
    }

    public static <R> QueryExpressionDSL.FromGatherer<R> select(Function<SelectModel, R> adapterFunction,
            BasicColumn... selectList) {
        return select(adapterFunction, Arrays.asList(selectList));
    }

    public static <R> QueryExpressionDSL.FromGatherer<R> select(Function<SelectModel, R> adapterFunction,
            Collection<? extends BasicColumn> selectList) {
        return new FromGatherer.Builder<R>()
                .withSelectList(selectList)
                .withSelectDSL(new SelectDSL<>(adapterFunction))
                .build();
    }

    public static QueryExpressionDSL.FromGatherer<SelectModel> selectDistinct(BasicColumn... selectList) {
        return selectDistinct(Function.identity(), selectList);
    }

    public static QueryExpressionDSL.FromGatherer<SelectModel> selectDistinct(
            Collection<? extends BasicColumn> selectList) {
        return selectDistinct(Function.identity(), selectList);
    }

    public static <R> QueryExpressionDSL.FromGatherer<R> selectDistinct(Function<SelectModel, R> adapterFunction,
            BasicColumn... selectList) {
        return selectDistinct(adapterFunction, Arrays.asList(selectList));
    }

    public static <R> QueryExpressionDSL.FromGatherer<R> selectDistinct(Function<SelectModel, R> adapterFunction,
            Collection<? extends BasicColumn> selectList) {
        return new FromGatherer.Builder<R>()
                .withSelectList(selectList)
                .withSelectDSL(new SelectDSL<>(adapterFunction))
                .isDistinct()
                .build();
    }

    void registerQueryExpression(QueryExpressionDSL<R> queryExpression) {
        queryExpressions.add(queryExpression);
    }

    void orderBy(Collection<? extends SortSpecification> columns) {
        orderByModel = OrderByModel.of(columns);
    }

    public LimitFinisher limit(long limit) {
        return limitWhenPresent(limit);
    }

    public LimitFinisher limitWhenPresent(@Nullable Long limit) {
        this.limit = limit;
        return new LimitFinisher();
    }

    public OffsetFirstFinisher offset(long offset) {
        return offsetWhenPresent(offset);
    }

    public OffsetFirstFinisher offsetWhenPresent(@Nullable Long offset) {
        this.offset = offset;
        return new OffsetFirstFinisher();
    }

    public FetchFirstFinisher fetchFirst(long fetchFirstRows) {
        return fetchFirstWhenPresent(fetchFirstRows);
    }

    public FetchFirstFinisher fetchFirstWhenPresent(@Nullable Long fetchFirstRows) {
        this.fetchFirstRows = fetchFirstRows;
        return new FetchFirstFinisher();
    }

    public SelectDSL<R> forUpdate() {
        Validator.assertNull(forClause, "ERROR.48"); //$NON-NLS-1$
        forClause = "for update"; //$NON-NLS-1$
        return this;
    }

    public SelectDSL<R> forNoKeyUpdate() {
        Validator.assertNull(forClause, "ERROR.48"); //$NON-NLS-1$
        forClause = "for no key update"; //$NON-NLS-1$
        return this;
    }

    public SelectDSL<R> forShare() {
        Validator.assertNull(forClause, "ERROR.48"); //$NON-NLS-1$
        forClause = "for share"; //$NON-NLS-1$
        return this;
    }

    public SelectDSL<R> forKeyShare() {
        Validator.assertNull(forClause, "ERROR.48"); //$NON-NLS-1$
        forClause = "for key share"; //$NON-NLS-1$
        return this;
    }

    public SelectDSL<R> skipLocked() {
        Validator.assertNull(waitClause, "ERROR.49"); //$NON-NLS-1$
        waitClause = "skip locked"; //$NON-NLS-1$
        return this;
    }

    public SelectDSL<R> nowait() {
        Validator.assertNull(waitClause, "ERROR.49"); //$NON-NLS-1$
        waitClause = "nowait"; //$NON-NLS-1$
        return this;
    }

    @Override
    public SelectDSL<R> configureStatement(Consumer<StatementConfiguration> consumer) {
        consumer.accept(statementConfiguration);
        return this;
    }

    @Override
    public R build() {
        SelectModel selectModel = SelectModel.withQueryExpressions(buildModels())
                .withOrderByModel(orderByModel)
                .withPagingModel(buildPagingModel().orElse(null))
                .withStatementConfiguration(statementConfiguration)
                .withForClause(forClause)
                .withWaitClause(waitClause)
                .build();
        return adapterFunction.apply(selectModel);
    }

    private List<QueryExpressionModel> buildModels() {
        return queryExpressions.stream()
                .map(QueryExpressionDSL::buildModel)
                .toList();
    }

    private Optional<PagingModel> buildPagingModel() {
        return new PagingModel.Builder()
                .withLimit(limit)
                .withOffset(offset)
                .withFetchFirstRows(fetchFirstRows)
                .build();
    }

    public class OffsetFirstFinisher implements SelectDSLForAndWaitOperations<R>, Buildable<R> {
        public FetchFirstFinisher fetchFirst(long fetchFirstRows) {
            return fetchFirstWhenPresent(fetchFirstRows);
        }

        public FetchFirstFinisher fetchFirstWhenPresent(@Nullable Long fetchFirstRows) {
            SelectDSL.this.fetchFirstRows = fetchFirstRows;
            return new FetchFirstFinisher();
        }

        @Override
        public SelectDSL<R> getSelectDSL() {
            return SelectDSL.this;
        }

        @Override
        public R build() {
            return SelectDSL.this.build();
        }
    }

    public class LimitFinisher implements SelectDSLForAndWaitOperations<R>, Buildable<R> {
        public SelectDSL<R> offset(long offset) {
            return offsetWhenPresent(offset);
        }

        public SelectDSL<R> offsetWhenPresent(@Nullable Long offset) {
            SelectDSL.this.offset = offset;
            return SelectDSL.this;
        }

        @Override
        public SelectDSL<R> getSelectDSL() {
            return SelectDSL.this;
        }

        @Override
        public R build() {
            return SelectDSL.this.build();
        }
    }

    public class FetchFirstFinisher {
        public SelectDSL<R> rowsOnly() {
            return SelectDSL.this;
        }
    }
}
