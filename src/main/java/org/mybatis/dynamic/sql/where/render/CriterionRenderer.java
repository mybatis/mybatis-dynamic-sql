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

import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlCriterion;

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
        String prefix = ""; //$NON-NLS-1$
        String suffix = ""; //$NON-NLS-1$
        if (criterion.hasSubCriteria()) {
            prefix = "("; //$NON-NLS-1$
            suffix = ")"; //$NON-NLS-1$
        }
        
        FragmentAndParameters fp = renderCriteria();

        String fragment = 
                criterion.connector().map(c -> c + " ").orElse("") //$NON-NLS-1$ //$NON-NLS-2$
                + prefix
                + fp.fragment()
                + suffix;
        
        return new FragmentAndParameters.Builder(fragment)
                .withParameters(fp.parameters())
                .build();
    }
    
    private FragmentAndParameters renderCriteria() {
        FragmentAndParameters renderedCondition = renderCondition();
        return criterion.subCriteria()
            .map(this::handleSubCriterion)
            .reduce(renderedCondition, (left, right) -> left.merge(right, " ")); //$NON-NLS-1$
    }
    
    private FragmentAndParameters renderCondition() {
        ConditionRenderer<T> visitor = ConditionRenderer.of(sequence, criterion.column(), nameFunction);
        return criterion.condition().accept(visitor);
    }

    private FragmentAndParameters handleSubCriterion(SqlCriterion<?> subCriterion) {
        return CriterionRenderer.of(subCriterion, sequence, nameFunction).render();
    }
    
    public static <T> CriterionRenderer<T> of(SqlCriterion<T> criterion, AtomicInteger sequence, Function<SqlColumn<?>, String> nameFunction) {
        return new CriterionRenderer<>(criterion, sequence, nameFunction);
    }
}
