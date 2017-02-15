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
package org.mybatis.dynamic.sql.where.render;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlCriterion;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;
import org.mybatis.dynamic.sql.util.FragmentCollector;

public class CriterionRenderer<T> {
    private SqlCriterion<T> criterion;
    private AtomicInteger sequence;
    private Function<SqlColumn<?>, String> nameFunction;
    
    protected CriterionRenderer(SqlCriterion<T> criterion, AtomicInteger sequence, Function<SqlColumn<?>, String> nameFunction) {
        this.criterion = criterion;
        this.sequence = sequence;
        this.nameFunction = nameFunction;
    }
    
    public FragmentAndParameters render() {
        if (criterion.hasSubCriteria()) {
            return renderWithSubCriteria();
        } else {
            return renderWithoutSubCriteria();
        }
    }
    
    private FragmentAndParameters renderWithSubCriteria() {
        FragmentAndParameters renderedCondition = renderCondition();
        FragmentCollector renderedSubCriteria = renderSubCriteria();

        StringBuilder buffer = new StringBuilder();
        buffer.append(criterion.connector().map(c -> c + " ").orElse("")) //$NON-NLS-1$ //$NON-NLS-2$
        .append('(') 
        .append(renderedCondition.fragment())
        .append(' ')
        .append(renderedSubCriteria.fragments().collect(Collectors.joining(" "))) //$NON-NLS-1$
        .append(')');
        
        return new FragmentAndParameters.Builder(buffer.toString())
                .withParameters(renderedCondition.parameters())
                .withParameters(renderedSubCriteria.parameters())
                .build();
    }
    
    private FragmentAndParameters renderWithoutSubCriteria() {
        FragmentAndParameters renderedCondition = renderCondition();

        StringBuilder buffer = new StringBuilder();
        buffer.append(criterion.connector().map(c -> c + " ").orElse("")) //$NON-NLS-1$ //$NON-NLS-2$
        .append(renderedCondition.fragment());
        
        return new FragmentAndParameters.Builder(buffer.toString())
                .withParameters(renderedCondition.parameters())
                .build();
    }
    
    private FragmentAndParameters renderCondition() {
        ConditionRenderer<T> visitor = ConditionRenderer.of(sequence, criterion.column(), nameFunction);
        return criterion.condition().accept(visitor);
    }

    private FragmentCollector renderSubCriteria() {
        return criterion.subCriteria()
            .map(this::renderSubCriterion)
            .collect(Collector.of(FragmentCollector::new,
                    FragmentCollector::add,
                    FragmentCollector::merge));
    }
    
    private FragmentAndParameters renderSubCriterion(SqlCriterion<?> subCriterion) {
        return CriterionRenderer.of(subCriterion, sequence, nameFunction).render();
    }
    
    public static <T> CriterionRenderer<T> of(SqlCriterion<T> criterion, AtomicInteger sequence, Function<SqlColumn<?>, String> nameFunction) {
        return new CriterionRenderer<>(criterion, sequence, nameFunction);
    }
}
