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

import static org.mybatis.dynamic.sql.util.StringUtilities.spaceAfter;
import static org.mybatis.dynamic.sql.util.StringUtilities.spaceBefore;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.mybatis.dynamic.sql.SqlCriterion;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.render.TableAliasCalculator;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;
import org.mybatis.dynamic.sql.util.FragmentCollector;

public class CriterionRenderer {

    private AtomicInteger sequence;
    private RenderingStrategy renderingStrategy;
    private TableAliasCalculator tableAliasCalculator;
    
    private CriterionRenderer(Builder builder) {
        sequence = Objects.requireNonNull(builder.sequence);
        renderingStrategy = Objects.requireNonNull(builder.renderingStrategy);
        tableAliasCalculator = Objects.requireNonNull(builder.tableAliasCalculator);
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
                + spaceBefore(renderedSubCriteria.fragments().collect(Collectors.joining(" "))) //$NON-NLS-1$
                + ")"; //$NON-NLS-1$
    }
    
    private String renderConnector(SqlCriterion<?> criterion) {
        return spaceAfter(criterion.connector());
    }
    
    private  <T> FragmentAndParameters renderSubCriterion(SqlCriterion<T> subCriterion) {
        return new CriterionRenderer.Builder()
                .withSequence(sequence)
                .withRenderingStrategy(renderingStrategy)
                .withTableAliasCalculator(tableAliasCalculator)
                .build()
                .render(subCriterion);
    }
    
    private <T> FragmentAndParameters renderCondition(SqlCriterion<T> criterion) {
        WhereConditionVisitor<T> visitor = new WhereConditionVisitor.Builder<T>()
                .withRenderingStrategy(renderingStrategy)
                .withSequence(sequence)
                .withColumn(criterion.column())
                .withTableAliasCalculator(tableAliasCalculator)
                .build();
        return criterion.condition().accept(visitor);
    }
    
    public static class Builder {
        private AtomicInteger sequence;
        private RenderingStrategy renderingStrategy;
        private TableAliasCalculator tableAliasCalculator;
        
        public Builder withSequence(AtomicInteger sequence) {
            this.sequence = sequence;
            return this;
        }
        
        public Builder withRenderingStrategy(RenderingStrategy renderingStrategy) {
            this.renderingStrategy = renderingStrategy;
            return this;
        }
        
        public Builder withTableAliasCalculator(TableAliasCalculator tableAliasCalculator) {
            this.tableAliasCalculator = tableAliasCalculator;
            return this;
        }
        
        public CriterionRenderer build() {
            return new CriterionRenderer(this);
        }
    }
}
