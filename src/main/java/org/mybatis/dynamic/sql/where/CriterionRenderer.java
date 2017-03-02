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

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.mybatis.dynamic.sql.SqlCriterion;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;
import org.mybatis.dynamic.sql.util.FragmentCollector;

public abstract class CriterionRenderer {
    
    private CriterionRenderer() {
        super();
    }
    
    public <T> FragmentAndParameters render(SqlCriterion<T> criterion) {
        FragmentAndParameters renderedCondition = renderCondition(criterion);
        if (criterion.hasSubCriteria()) {
            return renderWithSubCriteria(criterion, renderedCondition);
        } else {
            return renderWithoutSubCriteria(criterion, renderedCondition);
        }
    }
    
    private <T> FragmentAndParameters renderWithSubCriteria(SqlCriterion<T> criterion, FragmentAndParameters renderedCondition) {
        FragmentCollector renderedSubCriteria = criterion.subCriteria()
                .map(this::renderSubCriterion)
                .collect(FragmentCollector.fragmentAndParameterCollector());

        String fragment = renderConnector(criterion)
                + "("  //$NON-NLS-1$
                + renderedCondition.fragment()
                + " " //$NON-NLS-1$
                + renderedSubCriteria.fragments().collect(Collectors.joining(" ")) //$NON-NLS-1$
                + ")"; //$NON-NLS-1$
        
        return new FragmentAndParameters.Builder(fragment)
                .withParameters(renderedCondition.parameters())
                .withParameters(renderedSubCriteria.parameters())
                .build();
    }
    
    private <T> FragmentAndParameters renderWithoutSubCriteria(SqlCriterion<T> criterion, FragmentAndParameters renderedCondition) {
        String fragment = renderConnector(criterion)
                + renderedCondition.fragment();
        
        return new FragmentAndParameters.Builder(fragment)
                .withParameters(renderedCondition.parameters())
                .build();
    }
    
    private String renderConnector(SqlCriterion<?> criterion) {
        return criterion.connector().map(c -> c + " ").orElse(""); //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    protected abstract <T> FragmentAndParameters renderCondition(SqlCriterion<T> criterion);
    protected abstract  <T> FragmentAndParameters renderSubCriterion(SqlCriterion<T> subCriterion);
    
    public static CriterionRenderer newRendererIgnoringTableAlias(AtomicInteger sequence) {
        return new CriterionRenderer() {
            @Override
            protected <T> FragmentAndParameters renderCondition(SqlCriterion<T> criterion) {
                return criterion.condition().renderIgnoringTableAlias(sequence, criterion.column());
            }
            
            @Override
            protected <T> FragmentAndParameters renderSubCriterion(SqlCriterion<T> subCriterion) {
                return CriterionRenderer.newRendererIgnoringTableAlias(sequence).render(subCriterion);
            }
        };
    }

    public static CriterionRenderer newRendererIncludingTableAlias(AtomicInteger sequence) {
        return new CriterionRenderer() {
            @Override
            protected <T> FragmentAndParameters renderCondition(SqlCriterion<T> criterion) {
                return criterion.condition().renderIncludingTableAlias(sequence, criterion.column());
            }
            
            @Override
            protected <T> FragmentAndParameters renderSubCriterion(SqlCriterion<T> subCriterion) {
                return CriterionRenderer.newRendererIncludingTableAlias(sequence).render(subCriterion);
            }
        };
    }
}
