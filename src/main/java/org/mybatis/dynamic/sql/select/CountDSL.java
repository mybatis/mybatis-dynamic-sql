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

import java.util.Objects;
import java.util.function.Function;

import org.mybatis.dynamic.sql.BindableColumn;
import org.mybatis.dynamic.sql.SqlBuilder;
import org.mybatis.dynamic.sql.SqlCriterion;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.VisitableCondition;
import org.mybatis.dynamic.sql.util.Buildable;
import org.mybatis.dynamic.sql.where.AbstractWhereDSL;

/**
 * DSL for building count queries. Count queries are specializations of select queries. They have joins and where
 * clauses, but not the other parts of a select (group by, order by, etc.) Count queries always return
 * a long. If these restrictions are not acceptable, then use the Select DSL for an unrestricted select statement.
 *
 * @param <R> the type of model built by this Builder. Typically SelectModel.
 *
 * @author Jeff Butler
 */
public class CountDSL<R> extends AbstractQueryExpressionDSL<CountDSL<R>, R> implements Buildable<R> {

    private Function<SelectModel, R> adapterFunction;
    private CountWhereBuilder whereBuilder = new CountWhereBuilder();
    
    private CountDSL(SqlTable table, Function<SelectModel, R> adapterFunction) {
        super(table);
        this.adapterFunction = Objects.requireNonNull(adapterFunction);
    }
    
    public CountWhereBuilder where() {
        return whereBuilder;
    }

    public <T> CountWhereBuilder where(BindableColumn<T> column, VisitableCondition<T> condition,
            SqlCriterion<?>...subCriteria) {
        whereBuilder.and(column, condition, subCriteria);
        return whereBuilder;
    }

    @Override
    public R build() {
        return adapterFunction.apply(buildModel());
    }

    private SelectModel buildModel() {
        QueryExpressionModel.Builder b = new QueryExpressionModel.Builder()
                .withSelectColumn(SqlBuilder.count())
                .withTable(table())
                .withTableAliases(tableAliases)
                .withWhereModel(whereBuilder.buildWhereModel());
        
        buildJoinModel().ifPresent(b::withJoinModel);
        
        return new SelectModel.Builder()
                .withQueryExpression(b.build())
                .build();
    }
    
    public static CountDSL<SelectModel> countFrom(SqlTable table) {
        return countFrom(Function.identity(), table);
    }
    
    public static <R> CountDSL<R> countFrom(Function<SelectModel, R> adapterFunction, SqlTable table) {
        return new CountDSL<>(table, adapterFunction);
    }
    
    @Override
    protected CountDSL<R> getThis() {
        return this;
    }
    
    public class CountWhereBuilder extends AbstractWhereDSL<CountWhereBuilder>
            implements Buildable<R> {
        private <T> CountWhereBuilder() {}

        @Override
        public R build() {
            return CountDSL.this.build();
        }
        
        @Override
        protected CountWhereBuilder getThis() {
            return this;
        }
    }
}
