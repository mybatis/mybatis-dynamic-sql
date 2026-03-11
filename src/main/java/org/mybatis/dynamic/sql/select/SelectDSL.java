/*
 *    Copyright 2016-2026 the original author or authors.
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
import java.util.function.Consumer;
import java.util.function.Function;

import org.jspecify.annotations.Nullable;
import org.mybatis.dynamic.sql.BasicColumn;
import org.mybatis.dynamic.sql.SortSpecification;
import org.mybatis.dynamic.sql.common.OrderByModel;
import org.mybatis.dynamic.sql.configuration.StatementConfiguration;
import org.mybatis.dynamic.sql.dsl.AbstractLimitAndOffsetSupport;
import org.mybatis.dynamic.sql.dsl.ForAndWaitOperations;
import org.mybatis.dynamic.sql.dsl.LimitAndOffsetOperations;
import org.mybatis.dynamic.sql.dsl.OrderByOperations;
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
public class SelectDSL<R> implements ForAndWaitOperations<SelectDSL<R>>,
        LimitAndOffsetOperations<SelectDSL<R>, R>,
        OrderByOperations<SelectDSL<R>>,
        ConfigurableStatement<SelectDSL<R>>,
        Buildable<R> {
    private final Function<SelectModel, R> adapterFunction;
    private final List<QueryExpressionDSL<R>> queryExpressions = new ArrayList<>();
    private @Nullable OrderByModel orderByModel;
    private final LimitAndOffsetSupport limitAndOffsetSupport = new LimitAndOffsetSupport();
    final StatementConfiguration statementConfiguration = new StatementConfiguration();
    private @Nullable String forClause;
    private @Nullable String waitClause;

    private SelectDSL(Function<SelectModel, R> adapterFunction) {
        this.adapterFunction = Objects.requireNonNull(adapterFunction);
    }

    public static QueryExpressionDSL<SelectModel> select(BasicColumn... selectList) {
        return select(Arrays.asList(selectList));
    }

    public static QueryExpressionDSL<SelectModel> select(Collection<? extends BasicColumn> selectList) {
        return select(Function.identity(), selectList);
    }

    public static <R> QueryExpressionDSL<R> select(Function<SelectModel, R> adapterFunction,
            BasicColumn... selectList) {
        return select(adapterFunction, Arrays.asList(selectList));
    }

    public static <R> QueryExpressionDSL<R> select(Function<SelectModel, R> adapterFunction,
            Collection<? extends BasicColumn> selectList) {
        return new QueryExpressionDSL.Builder<R>()
                .withSelectList(selectList)
                .withSelectDSL(new SelectDSL<>(adapterFunction))
                .build();
    }

    public static QueryExpressionDSL<SelectModel> selectDistinct(BasicColumn... selectList) {
        return selectDistinct(Arrays.asList(selectList));
    }

    public static QueryExpressionDSL<SelectModel> selectDistinct(
            Collection<? extends BasicColumn> selectList) {
        return selectDistinct(Function.identity(), selectList);
    }

    public static <R> QueryExpressionDSL<R> selectDistinct(Function<SelectModel, R> adapterFunction,
            BasicColumn... selectList) {
        return selectDistinct(adapterFunction, Arrays.asList(selectList));
    }

    public static <R> QueryExpressionDSL<R> selectDistinct(Function<SelectModel, R> adapterFunction,
            Collection<? extends BasicColumn> selectList) {
        return new QueryExpressionDSL.Builder<R>()
                .withSelectList(selectList)
                .withSelectDSL(new SelectDSL<>(adapterFunction))
                .isDistinct()
                .build();
    }

    void registerQueryExpression(QueryExpressionDSL<R> queryExpression) {
        queryExpressions.add(queryExpression);
    }


    @Override
    public SelectDSL<R> orderBy(Collection<? extends SortSpecification> columns) {
        orderByModel = OrderByModel.of(columns);
        return this;
    }

    @Override
    public LimitFinisher<SelectDSL<R>, R> limitWhenPresent(@Nullable Long limit) {
        return limitAndOffsetSupport.limitWhenPresent(limit);
    }

    @Override
    public OffsetFirstFinisher<SelectDSL<R>, R> offsetWhenPresent(@Nullable Long offset) {
        return limitAndOffsetSupport.offsetWhenPresent(offset);
    }

    @Override
    public FetchFirstFinisher<SelectDSL<R>> fetchFirstWhenPresent(@Nullable Long fetchFirstRows) {
        return limitAndOffsetSupport.fetchFirstWhenPresent(fetchFirstRows);
    }

    @Override
    public SelectDSL<R> setWaitClause(String waitClause) {
        Validator.assertNull(this.waitClause, "ERROR.49"); //$NON-NLS-1$
        this.waitClause = waitClause;
        return this;
    }

    @Override
    public SelectDSL<R> setForClause(String forClause) {
        Validator.assertNull(this.forClause, "ERROR.48"); //$NON-NLS-1$
        this.forClause = forClause;
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
                .withPagingModel(limitAndOffsetSupport.buildPagingModel())
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

    private class LimitAndOffsetSupport extends AbstractLimitAndOffsetSupport<SelectDSL<R>, R> {

        protected LimitAndOffsetSupport() {
            super(SelectDSL.this);
        }

        protected @Nullable PagingModel buildPagingModel() {
            return toPagingModel().orElse(null);
        }

        @Override
        protected SelectDSL<R> getThis() {
            return SelectDSL.this;
        }
    }
}
