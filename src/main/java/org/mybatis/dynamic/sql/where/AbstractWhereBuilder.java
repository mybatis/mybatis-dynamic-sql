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
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.mybatis.dynamic.sql.Condition;
import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlCriterion;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;
import org.mybatis.dynamic.sql.util.FragmentCollector;
import org.mybatis.dynamic.sql.where.render.CriterionRenderer;

public abstract class AbstractWhereBuilder<T extends AbstractWhereBuilder<T>> {
    private List<SqlCriterion<?>> criteria = new ArrayList<>();
    private AtomicInteger sequence = new AtomicInteger(1);
    
    protected <S> AbstractWhereBuilder(SqlColumn<S> column, Condition<S> condition, SqlCriterion<?>...subCriteria) {
        SqlCriterion<S> criterion = SqlCriterion.of(column, condition, subCriteria);
        addCriterion(criterion);
    }

    public <S> T and(SqlColumn<S> column, Condition<S> condition, SqlCriterion<?>...subCriteria) {
        SqlCriterion<S> criterion = SqlCriterion.of("and", column, condition, subCriteria); //$NON-NLS-1$
        addCriterion(criterion);
        return getThis();
    }
    
    public <S> T or(SqlColumn<S> column, Condition<S> condition, SqlCriterion<?>...subCriteria) {
        SqlCriterion<S> criterion = SqlCriterion.of("or", column, condition, subCriteria); //$NON-NLS-1$
        addCriterion(criterion);
        return getThis();
    }
    
    private <S> void addCriterion(SqlCriterion<S> criterion) {
        criteria.add(criterion);
    }
    
    protected WhereSupport renderCriteria(Function<SqlColumn<?>, String> nameFunction) {
        FragmentCollector fc = criteria.stream()
                .map(c -> renderCriterion(c, nameFunction))
                .collect(FragmentCollector.fragmentAndParameterCollector());

        return WhereSupport.of(fc.fragments().collect(Collectors.joining(" ", "where ", "")), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                fc.parameters());
    }
    
    private FragmentAndParameters renderCriterion(SqlCriterion<?> criterion, Function<SqlColumn<?>, String> nameFunction) {
        return CriterionRenderer.of(sequence, nameFunction).render(criterion);
    }
    
    protected abstract T getThis();
}
