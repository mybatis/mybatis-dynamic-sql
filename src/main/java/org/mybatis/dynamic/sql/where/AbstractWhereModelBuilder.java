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
package org.mybatis.dynamic.sql.where;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.mybatis.dynamic.sql.Condition;
import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlCriterion;

public abstract class AbstractWhereModelBuilder<T extends AbstractWhereModelBuilder<T>> {
    private List<SqlCriterion<?>> criteria = new ArrayList<>();
    
    protected <S> AbstractWhereModelBuilder(SqlColumn<S> column, Condition<S> condition) {
        SqlCriterion<S> criterion = new SqlCriterion.Builder<S>()
                .withColumn(column)
                .withCondition(condition)
                .build();
        criteria.add(criterion);
    }
    
    protected <S> AbstractWhereModelBuilder(SqlColumn<S> column, Condition<S> condition, SqlCriterion<?>...subCriteria) {
        SqlCriterion<S> criterion = new SqlCriterion.Builder<S>()
                .withColumn(column)
                .withCondition(condition)
                .withSubCriteria(Arrays.asList(subCriteria))
                .build();
        criteria.add(criterion);
    }
    
    public <S> T and(SqlColumn<S> column, Condition<S> condition) {
        addCriterion("and", column, condition); //$NON-NLS-1$
        return getThis();
    }
    
    public <S> T and(SqlColumn<S> column, Condition<S> condition, SqlCriterion<?>...subCriteria) {
        addCriterion("and", column, condition, subCriteria); //$NON-NLS-1$
        return getThis();
    }
    
    public <S> T or(SqlColumn<S> column, Condition<S> condition) {
        addCriterion("or", column, condition); //$NON-NLS-1$
        return getThis();
    }
    
    public <S> T or(SqlColumn<S> column, Condition<S> condition, SqlCriterion<?>...subCriteria) {
        addCriterion("or", column, condition, subCriteria); //$NON-NLS-1$
        return getThis();
    }
    
    private <S> void addCriterion(String connector, SqlColumn<S> column, Condition<S> condition) {
        SqlCriterion<S> criterion = new SqlCriterion.Builder<S>()
                .withConnector(connector)
                .withColumn(column)
                .withCondition(condition)
                .build();
        criteria.add(criterion);
    }
    
    private <S> void addCriterion(String connector, SqlColumn<S> column, Condition<S> condition, SqlCriterion<?>...subCriteria) {
        SqlCriterion<S> criterion = new SqlCriterion.Builder<S>()
                .withConnector(connector)
                .withColumn(column)
                .withCondition(condition)
                .withSubCriteria(Arrays.asList(subCriteria))
                .build();
        criteria.add(criterion);
    }
    
    protected WhereModel buildWhereModel() {
        return WhereModel.of(criteria.stream());
    }
    
    protected abstract T getThis();
}
