/**
 *    Copyright 2016-2018 the original author or authors.
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
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import org.mybatis.dynamic.sql.BasicColumn;
import org.mybatis.dynamic.sql.select.QueryExpressionDSL.FromGatherer;
import org.mybatis.dynamic.sql.select.QueryExpressionDSL.FromGathererBuilder;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;

/**
 * Implements a standard SQL dialect for building model classes.
 * 
 * @author Jeff Butler
 *
 * @param <R> the type of model produced by this builder
 */
public class SelectDSL<R> {

    private Function<SelectModel, R> adapterFunction;
    private List<QueryExpressionModel> queryExpressions = new ArrayList<>();    
    private OrderByModel orderByModel;
    
    private SelectDSL(Function<SelectModel, R> adapterFunction) {
        this.adapterFunction = Objects.requireNonNull(adapterFunction);
    }

    private FromGatherer<R> queryExpressionBuilder(BasicColumn...selectList) {
        return new FromGathererBuilder<R>()
                .withSelectDSL(this)
                .withSelectList(selectList)
                .build();
    }
    
    private FromGatherer<R> distinctQueryExpressionBuilder(BasicColumn...selectList) {
        return new FromGathererBuilder<R>()
                .withSelectDSL(this)
                .withSelectList(selectList)
                .isDistinct()
                .build();
    }
    
    public static FromGatherer<SelectModel> select(BasicColumn...selectList) {
        return select(Function.identity(), selectList);
    }
    
    public static <R> FromGatherer<R> select(Function<SelectModel, R> adapterFunction,
            BasicColumn...selectList) {
        SelectDSL<R> selectModelBuilder = new SelectDSL<>(adapterFunction);
        return selectModelBuilder.queryExpressionBuilder(selectList);
    }
    
    public static FromGatherer<SelectModel> selectDistinct(BasicColumn...selectList) {
        return selectDistinct(Function.identity(), selectList);
    }
    
    public static <R> FromGatherer<R> selectDistinct(Function<SelectModel, R> adapterFunction,
            BasicColumn...selectList) {
        SelectDSL<R> selectModelBuilder = new SelectDSL<>(adapterFunction);
        return selectModelBuilder.distinctQueryExpressionBuilder(selectList);
    }
    
    public static <T> FromGatherer<MyBatis3SelectModelAdapter<T>> selectWithMapper(
            Function<SelectStatementProvider, T> mapperMethod, BasicColumn...selectList) {
        return select(selectModel -> MyBatis3SelectModelAdapter.of(selectModel, mapperMethod), selectList);
    }
    
    public static <T> FromGatherer<MyBatis3SelectModelAdapter<T>> selectDistinctWithMapper(
            Function<SelectStatementProvider, T> mapperMethod, BasicColumn...selectList) {
        return selectDistinct(selectModel -> MyBatis3SelectModelAdapter.of(selectModel, mapperMethod),
                selectList);
    }
    
    void addQueryExpression(QueryExpressionModel queryExpression) {
        queryExpressions.add(queryExpression);
    }
    
    void setOrderByModel(OrderByModel orderByModel) {
        this.orderByModel = orderByModel;
    }
    
    public R build() {
        SelectModel selectModel = SelectModel.withQueryExpressions(queryExpressions)
                .withOrderByModel(orderByModel)
                .build();
        return adapterFunction.apply(selectModel);
    }
}
