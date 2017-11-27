/**
 *    Copyright 2016-2017 the original author or authors.
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
import org.mybatis.dynamic.sql.select.render.SelectStatement;

/**
 * Implements a standard SQL dialect for building model classes.
 * 
 * @author Jeff Butler
 *
 * @param <R> the type of model produced by this builder
 */
public class SelectDSL<R> {

    private Function<SelectModel, R> decoratorFunction;
    private List<QueryExpressionModel> queryExpressions = new ArrayList<>();    
    private OrderByModel orderByModel;
    
    private SelectDSL(Function<SelectModel, R> decoratorFunction) {
        this.decoratorFunction = Objects.requireNonNull(decoratorFunction);
    }

    private QueryExpressionDSL<R> queryExpressionBuilder(BasicColumn...selectList) {
        return new QueryExpressionDSL.Builder<R>()
                .withSelectList(selectList)
                .withSelectModelBuilder(this)
                .build();
    }
    
    private QueryExpressionDSL<R> distinctQueryExpressionBuilder(BasicColumn...selectList) {
        return new QueryExpressionDSL.Builder<R>()
                .withSelectList(selectList)
                .isDistinct()
                .withSelectModelBuilder(this)
                .build();
    }
    
    public static <R> QueryExpressionDSL<R> genericSelect(Function<SelectModel, R> decoratorFunction, BasicColumn...selectList) {
        SelectDSL<R> selectModelBuilder = new SelectDSL<>(decoratorFunction);
        return selectModelBuilder.queryExpressionBuilder(selectList);
    }
    
    public static <R> QueryExpressionDSL<R> genericSelectDistinct(Function<SelectModel, R> decoratorFunction, BasicColumn...selectList) {
        SelectDSL<R> selectModelBuilder = new SelectDSL<>(decoratorFunction);
        return selectModelBuilder.distinctQueryExpressionBuilder(selectList);
    }
    
    public static QueryExpressionDSL<SelectModel> select(BasicColumn...selectList) {
        return genericSelect(Function.identity(), selectList);
    }
    
    public static <T> QueryExpressionDSL<MyBatis3SelectModel<T>> select(Function<SelectStatement, T> mapperMethod, BasicColumn...selectList) {
        return genericSelect(decorate(mapperMethod), selectList);
    }
    
    public static QueryExpressionDSL<SelectModel> selectDistinct(BasicColumn...selectList) {
        return genericSelectDistinct(Function.identity(), selectList);
    }
    
    public static <T> QueryExpressionDSL<MyBatis3SelectModel<T>> selectDistinct(Function<SelectStatement, T> mapperMethod, BasicColumn...selectList) {
        return genericSelectDistinct(decorate(mapperMethod), selectList);
    }
    
    private static <T> Function<SelectModel, MyBatis3SelectModel<T>> decorate(Function<SelectStatement, T> mapperMethod) {
        return selectModel -> MyBatis3SelectModel.of(selectModel, mapperMethod);
    }
    
    void addQueryExpression(QueryExpressionModel queryExpression) {
        queryExpressions.add(queryExpression);
    }
    
    void setOrderByModel(OrderByModel orderByModel) {
        this.orderByModel = orderByModel;
    }
    
    public R build() {
        SelectModel selectModel = new SelectModel.Builder()
                .withQueryExpressions(queryExpressions)
                .withOrderByModel(orderByModel)
                .build();
        return decoratorFunction.apply(selectModel);
    }
}
