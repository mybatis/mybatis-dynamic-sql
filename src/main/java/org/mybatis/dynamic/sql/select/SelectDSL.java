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
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import org.mybatis.dynamic.sql.BasicColumn;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.util.Buildable;

/**
 * Implements a standard SQL dialect for building model classes.
 * 
 * @author Jeff Butler
 *
 * @param <R> the type of model produced by this builder
 */
public class SelectDSL<R> implements Buildable<R> {

    private Function<SelectModel, R> adapterFunction;
    private List<QueryExpressionModel> queryExpressions = new ArrayList<>();    
    private OrderByModel orderByModel;
    private PagingModel pagingModel;
    
    private SelectDSL(Function<SelectModel, R> adapterFunction) {
        this.adapterFunction = Objects.requireNonNull(adapterFunction);
    }

    public static QueryExpressionDSL.FromGatherer<SelectModel> select(BasicColumn...selectList) {
        return select(Function.identity(), selectList);
    }
    
    public static <R> QueryExpressionDSL.FromGatherer<R> select(Function<SelectModel, R> adapterFunction,
            BasicColumn...selectList) {
        SelectDSL<R> selectDSL = new SelectDSL<>(adapterFunction);
        return QueryExpressionDSL.select(selectDSL, selectList);
    }
    
    public static QueryExpressionDSL.FromGatherer<SelectModel> selectDistinct(BasicColumn...selectList) {
        return selectDistinct(Function.identity(), selectList);
    }
    
    public static <R> QueryExpressionDSL.FromGatherer<R> selectDistinct(Function<SelectModel, R> adapterFunction,
            BasicColumn...selectList) {
        SelectDSL<R> selectDSL = new SelectDSL<>(adapterFunction);
        return QueryExpressionDSL.selectDistinct(selectDSL, selectList);
    }
    
    public static <T> QueryExpressionDSL.FromGatherer<MyBatis3SelectModelAdapter<T>> selectWithMapper(
            Function<SelectStatementProvider, T> mapperMethod, BasicColumn...selectList) {
        return select(selectModel -> MyBatis3SelectModelAdapter.of(selectModel, mapperMethod), selectList);
    }
    
    public static <T> QueryExpressionDSL.FromGatherer<MyBatis3SelectModelAdapter<T>> selectDistinctWithMapper(
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
    
    public LimitFinisher limit(long limit) {
        return new LimitFinisher(limit);
    }

    public OffsetFirstFinisher offset(long offset) {
        return new OffsetFirstFinisher(offset);
    }

    public FetchFirstFinisher fetchFirst(long fetchFirstRows) {
        return new FetchFirstFinisher(fetchFirstRows);
    }

    @Override
    public R build() {
        SelectModel selectModel = SelectModel.withQueryExpressions(queryExpressions)
                .withOrderByModel(orderByModel)
                .withPagingModel(pagingModel)
                .build();
        return adapterFunction.apply(selectModel);
    }
    
    public class LimitFinisher implements Buildable<R> {
        private long limit;
        
        public LimitFinisher(long limit) {
            this.limit = limit;
        }
        
        public OffsetFinisher offset(long offset) {
            return new OffsetFinisher(limit, offset);
        }
        
        @Override
        public R build() {
            SelectDSL.this.pagingModel = new LimitAndOffsetPagingModel.Builder()
                    .withLimit(limit)
                    .build();
            return SelectDSL.this.build();
        }
    }

    public class OffsetFinisher implements Buildable<R> {
        public OffsetFinisher(long limit, long offset) {
            SelectDSL.this.pagingModel = new LimitAndOffsetPagingModel.Builder()
                    .withLimit(limit)
                    .withOffset(offset)
                    .build();
        }
        
        @Override
        public R build() {
            return SelectDSL.this.build();
        }
    }

    public class OffsetFirstFinisher implements Buildable<R> {
        private long offset;

        public OffsetFirstFinisher(long offset) {
            this.offset = offset;
        }
        
        public FetchFirstFinisher fetchFirst(long fetchFirstRows) {
            return new FetchFirstFinisher(offset, fetchFirstRows);
        }
        
        @Override
        public R build() {
            SelectDSL.this.pagingModel = new FetchFirstPagingModel.Builder()
                    .withOffset(offset)
                    .build();
            return SelectDSL.this.build();
        }
    }
    
    public class FetchFirstFinisher {
        public FetchFirstFinisher(long fetchFirstRows) {
            SelectDSL.this.pagingModel = new FetchFirstPagingModel.Builder()
                    .withFetchFirstRows(fetchFirstRows)
                    .build();
        }

        public FetchFirstFinisher(long offset, long fetchFirstRows) {
            SelectDSL.this.pagingModel = new FetchFirstPagingModel.Builder()
                    .withOffset(offset)
                    .withFetchFirstRows(fetchFirstRows)
                    .build();
        }

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
