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
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.mybatis.dynamic.sql.SqlCriterion;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.render.TableAliasCalculator;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;
import org.mybatis.dynamic.sql.util.FragmentCollector;

public class CriterionRenderer<T> {
    private SqlCriterion<T> sqlCriterion;
    private AtomicInteger sequence;
    private RenderingStrategy renderingStrategy;
    private TableAliasCalculator tableAliasCalculator;
    private Optional<String> parameterName = Optional.empty();
    
    private CriterionRenderer(Builder<T> builder) {
        sqlCriterion = Objects.requireNonNull(builder.sqlCriterion);
        sequence = Objects.requireNonNull(builder.sequence);
        renderingStrategy = Objects.requireNonNull(builder.renderingStrategy);
        tableAliasCalculator = Objects.requireNonNull(builder.tableAliasCalculator);
        parameterName = Objects.requireNonNull(builder.parameterName);
    }
    
    public FragmentAndParameters render() {
        if (sqlCriterion.hasSubCriteria()) {
            return renderWithSubcriteria();
        } else {
            return renderWithoutSubcriteria();
        }
    }
    
    private FragmentAndParameters renderWithSubcriteria() {
        String connector = renderConnector();
        FragmentAndParameters renderedCondition = renderCondition();
        
        FragmentCollector renderedSubCriteria = sqlCriterion.mapSubCriteria(this::renderSubCriterion)
                .collect(FragmentCollector.collect());
        String fragment = calculateFragment(connector, renderedCondition, renderedSubCriteria);
        
        return FragmentAndParameters.withFragment(fragment)
                .withParameters(renderedCondition.parameters())
                .withParameters(renderedSubCriteria.parameters())
                .build();
    }

    private FragmentAndParameters renderWithoutSubcriteria() {
        String connector = renderConnector();
        FragmentAndParameters renderedCondition = renderCondition();
        
        String fragment = calculateFragment(connector, renderedCondition);
        
        return FragmentAndParameters.withFragment(fragment)
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
    
    private String renderConnector() {
        return spaceAfter(sqlCriterion.connector());
    }
    
    private <S> FragmentAndParameters renderSubCriterion(SqlCriterion<S> subCriterion) {
        return CriterionRenderer.withCriterion(subCriterion)
                .withSequence(sequence)
                .withRenderingStrategy(renderingStrategy)
                .withTableAliasCalculator(tableAliasCalculator)
                .build()
                .render();
    }
    
    private FragmentAndParameters renderCondition() {
        WhereConditionVisitor<T> visitor = WhereConditionVisitor.withColumn(sqlCriterion.column())
                .withRenderingStrategy(renderingStrategy)
                .withSequence(sequence)
                .withTableAliasCalculator(tableAliasCalculator)
                .withParameterName(parameterName)
                .build();
        return sqlCriterion.condition().accept(visitor);
    }
    
    public static <T> Builder<T> withCriterion(SqlCriterion<T> sqlCriterion) {
        return new Builder<T>().withCriterion(sqlCriterion);
    }
    
    public static class Builder<T> {
        private SqlCriterion<T> sqlCriterion;
        private AtomicInteger sequence;
        private RenderingStrategy renderingStrategy;
        private TableAliasCalculator tableAliasCalculator;
        private Optional<String> parameterName = Optional.empty();
        
        public Builder<T> withCriterion(SqlCriterion<T> sqlCriterion) {
            this.sqlCriterion = sqlCriterion;
            return this;
            
        }
        public Builder<T> withSequence(AtomicInteger sequence) {
            this.sequence = sequence;
            return this;
        }
        
        public Builder<T> withRenderingStrategy(RenderingStrategy renderingStrategy) {
            this.renderingStrategy = renderingStrategy;
            return this;
        }
        
        public Builder<T> withTableAliasCalculator(TableAliasCalculator tableAliasCalculator) {
            this.tableAliasCalculator = tableAliasCalculator;
            return this;
        }

        public Builder<T> withParameterName(Optional<String> parameterName) {
            this.parameterName = parameterName;
            return this;
        }
        
        public CriterionRenderer<T> build() {
            return new CriterionRenderer<>(this);
        }
    }
}
