/**
 *    Copyright 2016-2019 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
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
import java.util.function.Function;
import java.util.stream.Collectors;

import org.mybatis.dynamic.sql.BasicColumn;
import org.mybatis.dynamic.sql.SortSpecification;
import org.mybatis.dynamic.sql.select.QueryExpressionDSL.FromGatherer;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.util.Buildable;
import org.mybatis.dynamic.sql.util.mybatis3.MyBatis3Utils;

/**
 * Implements a SQL DSL for building select statements.
 * 
 * @author Jeff Butler
 *
 * @param <R> the type of model produced by this builder, typically SelectModel
 */
public class SelectDSL<R> implements Buildable<R> {

    private Function<SelectModel, R> adapterFunction;
    private List<QueryExpressionDSL<R>> queryExpressions = new ArrayList<>();
    private OrderByModel orderByModel;
    private Long limit;
    private Long offset;
    private Long fetchFirstRows;
    
    private SelectDSL(Function<SelectModel, R> adapterFunction) {
        this.adapterFunction = Objects.requireNonNull(adapterFunction);
    }

    public static QueryExpressionDSL.FromGatherer<SelectModel> select(BasicColumn...selectList) {
        return select(Arrays.asList(selectList));
    }
    
    public static QueryExpressionDSL.FromGatherer<SelectModel> select(Collection<BasicColumn> selectList) {
        return select(Function.identity(), selectList);
    }
    
    public static <R> QueryExpressionDSL.FromGatherer<R> select(Function<SelectModel, R> adapterFunction,
            BasicColumn...selectList) {
        return select(adapterFunction, Arrays.asList(selectList));
    }
    
    public static <R> QueryExpressionDSL.FromGatherer<R> select(Function<SelectModel, R> adapterFunction,
            Collection<BasicColumn> selectList) {
        return new FromGatherer.Builder<R>()
                .withSelectList(selectList)
                .withSelectDSL(new SelectDSL<>(adapterFunction))
                .build();
    }
    
    public static QueryExpressionDSL.FromGatherer<SelectModel> selectDistinct(BasicColumn...selectList) {
        return selectDistinct(Arrays.asList(selectList));
    }
    
    public static QueryExpressionDSL.FromGatherer<SelectModel> selectDistinct(Collection<BasicColumn> selectList) {
        return selectDistinct(Function.identity(), selectList);
    }
    
    public static <R> QueryExpressionDSL.FromGatherer<R> selectDistinct(Function<SelectModel, R> adapterFunction,
            BasicColumn...selectList) {
        return selectDistinct(adapterFunction, Arrays.asList(selectList));
    }
    
    public static <R> QueryExpressionDSL.FromGatherer<R> selectDistinct(Function<SelectModel, R> adapterFunction,
            Collection<BasicColumn> selectList) {
        return new FromGatherer.Builder<R>()
                .withSelectList(selectList)
                .withSelectDSL(new SelectDSL<>(adapterFunction))
                .isDistinct()
                .build();
    }
    
    /**
     * Select records by executing a MyBatis3 Mapper.
     * 
     * @deprecated in favor of various select methods in {@link MyBatis3Utils}.
     *     This method will be removed without direct replacement in a future version
     * @param <T> the return type from a MyBatis mapper - typically a List or a single record
     * @param mapperMethod MyBatis3 mapper method that performs the select
     * @param selectList the column list to select
     * @return the partially created query
     */
    @Deprecated
    public static <T> QueryExpressionDSL.FromGatherer<MyBatis3SelectModelAdapter<T>> selectWithMapper(
            Function<SelectStatementProvider, T> mapperMethod, BasicColumn...selectList) {
        return select(selectModel -> MyBatis3SelectModelAdapter.of(selectModel, mapperMethod), selectList);
    }
    
    /**
     * Select records by executing a MyBatis3 Mapper.
     * 
     * @deprecated in favor of various select methods in {@link MyBatis3Utils}.
     *     This method will be removed without direct replacement in a future version
     * @param <T> the return type from a MyBatis mapper - typically a List or a single record
     * @param mapperMethod MyBatis3 mapper method that performs the select
     * @param selectList the column list to select
     * @return the partially created query
     */
    @Deprecated
    public static <T> QueryExpressionDSL.FromGatherer<MyBatis3SelectModelAdapter<T>> selectDistinctWithMapper(
            Function<SelectStatementProvider, T> mapperMethod, BasicColumn...selectList) {
        return selectDistinct(selectModel -> MyBatis3SelectModelAdapter.of(selectModel, mapperMethod),
                selectList);
    }
    
    QueryExpressionDSL<R> newQueryExpression(FromGatherer<R> fromGatherer) {
        QueryExpressionDSL<R> queryExpression = new QueryExpressionDSL<>(fromGatherer);
        queryExpressions.add(queryExpression);
        return queryExpression;
    }
    
    QueryExpressionDSL<R> newQueryExpression(FromGatherer<R> fromGatherer, String tableAlias) {
        QueryExpressionDSL<R> queryExpression = new QueryExpressionDSL<>(fromGatherer, tableAlias);
        queryExpressions.add(queryExpression);
        return queryExpression;
    }
    
    void orderBy(SortSpecification...columns) {
        orderByModel = OrderByModel.of(columns);
    }
    
    public LimitFinisher limit(long limit) {
        this.limit = limit;
        return new LimitFinisher();
    }

    public OffsetFirstFinisher offset(long offset) {
        this.offset = offset;
        return new OffsetFirstFinisher();
    }

    public FetchFirstFinisher fetchFirst(long fetchFirstRows) {
        this.fetchFirstRows = fetchFirstRows;
        return new FetchFirstFinisher();
    }

    @Override
    public R build() {
        SelectModel selectModel = SelectModel.withQueryExpressions(buildModels())
                .withOrderByModel(orderByModel)
                .withPagingModel(buildPagingModel())
                .build();
        return adapterFunction.apply(selectModel);
    }
    
    private List<QueryExpressionModel> buildModels() {
        return queryExpressions.stream()
                .map(QueryExpressionDSL::buildModel)
                .collect(Collectors.toList());
    }
    
    private PagingModel buildPagingModel() {
        return new PagingModel.Builder()
                .withLimit(limit)
                .withOffset(offset)
                .withFetchFirstRows(fetchFirstRows)
                .build();
    }
    
    public class LimitFinisher implements Buildable<R> {
        public OffsetFinisher offset(long offset) {
            SelectDSL.this.offset = offset;
            return new OffsetFinisher();
        }
        
        @Override
        public R build() {
            return SelectDSL.this.build();
        }
    }

    public class OffsetFinisher implements Buildable<R> {
        @Override
        public R build() {
            return SelectDSL.this.build();
        }
    }

    public class OffsetFirstFinisher implements Buildable<R> {
        public FetchFirstFinisher fetchFirst(long fetchFirstRows) {
            SelectDSL.this.fetchFirstRows = fetchFirstRows;
            return new FetchFirstFinisher();
        }
        
        @Override
        public R build() {
            return SelectDSL.this.build();
        }
    }
    
    public class FetchFirstFinisher {
        public RowsOnlyFinisher rowsOnly() {
            return new RowsOnlyFinisher();
        }
    }
    
    public class RowsOnlyFinisher implements Buildable<R> {
        @Override
        public R build() {
            return SelectDSL.this.build();
        }
    }
}
