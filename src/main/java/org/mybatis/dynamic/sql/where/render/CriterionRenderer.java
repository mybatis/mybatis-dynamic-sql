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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.mybatis.dynamic.sql.SqlCriterion;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;
import org.mybatis.dynamic.sql.util.FragmentCollector;

public class CriterionRenderer {

    private AtomicInteger sequence;
    private RenderingStrategy renderingStrategy;
    private Map<SqlTable, String> tableAliases = new HashMap<>();
    
    private CriterionRenderer(Builder builder) {
        this.sequence = Objects.requireNonNull(builder.sequence);
        this.renderingStrategy = Objects.requireNonNull(builder.renderingStrategy);
        this.tableAliases.putAll(builder.tableAliases);
    }
    
    public <T> FragmentAndParameters render(SqlCriterion<T> criterion) {
        if (criterion.hasSubCriteria()) {
            return renderWithSubcriteria(criterion);
        } else {
            return renderWithoutSubcriteria(criterion);
        }
    }
    
    private <T> FragmentAndParameters renderWithSubcriteria(SqlCriterion<T> criterion) {
        String connector = renderConnector(criterion);
        FragmentAndParameters renderedCondition = renderCondition(criterion);
        
        FragmentCollector renderedSubCriteria = criterion.mapSubCriteria(this::renderSubCriterion)
                .collect(FragmentCollector.collect());
        String fragment = calculateFragment(connector, renderedCondition, renderedSubCriteria);
        
        return new FragmentAndParameters.Builder()
                .withFragment(fragment)
                .withParameters(renderedCondition.parameters())
                .withParameters(renderedSubCriteria.parameters())
                .build();
    }

    private <T> FragmentAndParameters renderWithoutSubcriteria(SqlCriterion<T> criterion) {
        String connector = renderConnector(criterion);
        FragmentAndParameters renderedCondition = renderCondition(criterion);
        
        String fragment = calculateFragment(connector, renderedCondition);
        
        return new FragmentAndParameters.Builder()
                .withFragment(fragment)
                .withParameters(renderedCondition.parameters())
                .build();
    }
    
    private String calculateFragment(String connector, FragmentAndParameters renderedCondition) {
        return connector + renderedCondition.fragment();
    }
    
    private String calculateFragment(String connector, FragmentAndParameters renderedCondition,
            FragmentCollector renderedSubCriteria) {
        return connector
                + "("  //$NON-NLS-1$
                + renderedCondition.fragment()
                + " " //$NON-NLS-1$
                + renderedSubCriteria.fragments().collect(Collectors.joining(" ")) //$NON-NLS-1$
                + ")"; //$NON-NLS-1$
    }
    
    private String renderConnector(SqlCriterion<?> criterion) {
        return criterion.connector().map(c -> c + " ").orElse(""); //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    private  <T> FragmentAndParameters renderSubCriterion(SqlCriterion<T> subCriterion) {
        return new CriterionRenderer.Builder()
                .withSequence(sequence)
                .withRenderingStrategy(renderingStrategy)
                .withTableAliases(tableAliases)
                .build()
                .render(subCriterion);
    }
    
    private <T> FragmentAndParameters renderCondition(SqlCriterion<T> criterion) {
        WhereConditionVisitor<T> visitor = new WhereConditionVisitor.Builder<T>()
                .withRenderingStrategy(renderingStrategy)
                .withSequence(sequence)
                .withColumn(criterion.column())
                .withTableAliases(tableAliases)
                .build();
        return criterion.condition().accept(visitor);
    }
    
    public static class Builder {
        private AtomicInteger sequence;
        private RenderingStrategy renderingStrategy;
        private Map<SqlTable, String> tableAliases = new HashMap<>();
        
        public Builder withSequence(AtomicInteger sequence) {
            this.sequence = sequence;
            return this;
        }
        
        public Builder withRenderingStrategy(RenderingStrategy renderingStrategy) {
            this.renderingStrategy = renderingStrategy;
            return this;
        }
        
        public Builder withTableAliases(Map<SqlTable, String> tableAliases) {
            this.tableAliases.putAll(tableAliases);
            return this;
        }
        
        public CriterionRenderer build() {
            return new CriterionRenderer(this);
        }
    }
}
