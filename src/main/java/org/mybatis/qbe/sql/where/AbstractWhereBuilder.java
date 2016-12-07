/**
 *    Copyright 2016 the original author or authors.
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
package org.mybatis.qbe.sql.where;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.mybatis.qbe.Condition;
import org.mybatis.qbe.sql.SqlCriterion;
import org.mybatis.qbe.sql.SqlField;
import org.mybatis.qbe.sql.where.render.CriterionRenderer;
import org.mybatis.qbe.sql.where.render.RenderedCriterion;

public abstract class AbstractWhereBuilder<T extends AbstractWhereBuilder<T>> {
    private List<CriterionWrapper> criteria = new ArrayList<>();
    private AtomicInteger sequence = new AtomicInteger(1);
    
    protected <S> AbstractWhereBuilder(SqlField<S> field, Condition<S> condition, SqlCriterion<?>...subCriteria) {
        criteria.add(CriterionWrapper.of(SqlCriterion.of(field, condition, subCriteria), sequence));
    }
    
    public <S> T and(SqlField<S> field, Condition<S> condition, SqlCriterion<?>...subCriteria) {
        criteria.add(CriterionWrapper.of(SqlCriterion.of("and", field, condition, subCriteria), sequence)); //$NON-NLS-1$
        return getThis();
    }
    
    public <S> T or(SqlField<S> field, Condition<S> condition, SqlCriterion<?>...subCriteria) {
        criteria.add(CriterionWrapper.of(SqlCriterion.of("or", field, condition, subCriteria), sequence)); //$NON-NLS-1$
        return getThis();
    }
    
    protected WhereSupport renderCriteria(Function<SqlField<?>, String> nameFunction) {
        return criteria.stream().collect(Collector.of(
                () -> new CollectorSupport(nameFunction),
                CollectorSupport::add,
                CollectorSupport::merge,
                CollectorSupport::getWhereSupport));
    }
    
    public abstract T getThis();
    
    static class CriterionWrapper {
        SqlCriterion<?> criterion;
        AtomicInteger sequence;
        
        static CriterionWrapper of(SqlCriterion<?> criterion, AtomicInteger sequence) {
            CriterionWrapper wrapper = new CriterionWrapper();
            wrapper.criterion = criterion;
            wrapper.sequence = sequence;
            return wrapper;
        }
    }
    
    static class CollectorSupport {
        Map<String, Object> parameters = new HashMap<>();
        List<String> phrases = new ArrayList<>();
        Function<SqlField<?>, String> nameFunction;
        
        CollectorSupport(Function<SqlField<?>, String> nameFunction) {
            this.nameFunction = nameFunction;
        }
        
        void add(CriterionWrapper criterionWrapper) {
            RenderedCriterion rc = CriterionRenderer.of(criterionWrapper.criterion, criterionWrapper.sequence, nameFunction)
                    .render();
            phrases.add(rc.whereClauseFragment());
            parameters.putAll(rc.fragmentParameters());
        }
        
        CollectorSupport merge(CollectorSupport other) {
            parameters.putAll(other.parameters);
            phrases.addAll(other.phrases);
            return this;
        }
        
        WhereSupport getWhereSupport() {
            String whereClause = phrases.stream().collect(Collectors.joining("", "where", "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            return WhereSupport.of(whereClause, parameters);
        }
    }
}
