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
package org.mybatis.dynamic.sql.where;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.mybatis.dynamic.sql.BindableColumn;
import org.mybatis.dynamic.sql.SqlCriterion;
import org.mybatis.dynamic.sql.VisitableCondition;

public abstract class AbstractWhereDSL<T extends AbstractWhereDSL<T>> {
    private List<SqlCriterion<?>> criteria = new ArrayList<>();
    
    protected <S> AbstractWhereDSL() {
        super();
    }
    
    protected <S> AbstractWhereDSL(BindableColumn<S> column, VisitableCondition<S> condition) {
        SqlCriterion<S> criterion = SqlCriterion.withColumn(column)
                .withCondition(condition)
                .build();
        criteria.add(criterion);
    }
    
    protected <S> AbstractWhereDSL(BindableColumn<S> column, VisitableCondition<S> condition,
            SqlCriterion<?>...subCriteria) {
        SqlCriterion<S> criterion = SqlCriterion.withColumn(column)
                .withCondition(condition)
                .withSubCriteria(Arrays.asList(subCriteria))
                .build();
        criteria.add(criterion);
    }
    
    public <S> T and(BindableColumn<S> column, VisitableCondition<S> condition) {
        addCriterion("and", column, condition); //$NON-NLS-1$
        return getThis();
    }
    
    public <S> T and(BindableColumn<S> column, VisitableCondition<S> condition, SqlCriterion<?>...subCriteria) {
        addCriterion("and", column, condition, Arrays.asList(subCriteria)); //$NON-NLS-1$
        return getThis();
    }
    
    public <S> T and(BindableColumn<S> column, VisitableCondition<S> condition, List<SqlCriterion<?>> subCriteria) {
        addCriterion("and", column, condition, subCriteria); //$NON-NLS-1$
        return getThis();
    }
    
    public <S> T or(BindableColumn<S> column, VisitableCondition<S> condition) {
        addCriterion("or", column, condition); //$NON-NLS-1$
        return getThis();
    }
    
    public <S> T or(BindableColumn<S> column, VisitableCondition<S> condition, SqlCriterion<?>...subCriteria) {
        addCriterion("or", column, condition, Arrays.asList(subCriteria)); //$NON-NLS-1$
        return getThis();
    }
    
    public <S> T or(BindableColumn<S> column, VisitableCondition<S> condition, List<SqlCriterion<?>> subCriteria) {
        addCriterion("or", column, condition, subCriteria); //$NON-NLS-1$
        return getThis();
    }
    
    private <S> void addCriterion(String connector, BindableColumn<S> column, VisitableCondition<S> condition) {
        SqlCriterion<S> criterion = SqlCriterion.withColumn(column)
                .withConnector(connector)
                .withCondition(condition)
                .build();
        criteria.add(criterion);
    }
    
    private <S> void addCriterion(String connector, BindableColumn<S> column, VisitableCondition<S> condition,
            List<SqlCriterion<?>> subCriteria) {
        SqlCriterion<S> criterion = SqlCriterion.withColumn(column)
                .withConnector(connector)
                .withCondition(condition)
                .withSubCriteria(subCriteria)
                .build();
        criteria.add(criterion);
    }
    
    public WhereModel buildWhereModel() {
        return WhereModel.of(criteria);
    }
    
    protected abstract T getThis();
}
