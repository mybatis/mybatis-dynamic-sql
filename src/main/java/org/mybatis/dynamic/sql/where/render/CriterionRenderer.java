/**
 *    Copyright 2016-2020 the original author or authors.
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

import java.util.List;
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
    private final SqlCriterion<T> sqlCriterion;
    private final AtomicInteger sequence;
    private final RenderingStrategy renderingStrategy;
    private final TableAliasCalculator tableAliasCalculator;
    private final String parameterName;
    
    private CriterionRenderer(Builder<T> builder) {
        sqlCriterion = Objects.requireNonNull(builder.sqlCriterion);
        sequence = Objects.requireNonNull(builder.sequence);
        renderingStrategy = Objects.requireNonNull(builder.renderingStrategy);
        tableAliasCalculator = Objects.requireNonNull(builder.tableAliasCalculator);
        parameterName = builder.parameterName;
    }
    
    public Optional<RenderedCriterion> render() {
        FragmentAndParameters initialCondition = null;
        if (sqlCriterion.condition().shouldRender()) {
            initialCondition = renderCondition();
        }

        List<RenderedCriterion> subCriteria = sqlCriterion.mapSubCriteria(this::renderSubCriterion)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        RenderedCriterion rc;
        if (initialCondition == null) {
            rc = renderWithoutInitialCondition(subCriteria);
        } else {
            rc = renderWithInitialCondition(initialCondition, subCriteria);
        }

        return Optional.ofNullable(rc);
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

    private <S> Optional<RenderedCriterion> renderSubCriterion(SqlCriterion<S> subCriterion) {
        return CriterionRenderer.withCriterion(subCriterion)
                .withSequence(sequence)
                .withRenderingStrategy(renderingStrategy)
                .withTableAliasCalculator(tableAliasCalculator)
                .withParameterName(parameterName)
                .build()
                .render();
    }
    
    private RenderedCriterion renderWithoutInitialCondition(List<RenderedCriterion> subCriteria) {
        if (subCriteria.isEmpty()) {
            return null;
        }

        return calculateRenderedCriterion(subCriteria);
    }

    private RenderedCriterion renderWithInitialCondition(FragmentAndParameters initialCondition,
            List<RenderedCriterion> subCriteria) {
        if (subCriteria.isEmpty()) {
            return fromFragmentAndParameters(initialCondition);
        }

        return calculateRenderedCriterion(initialCondition, subCriteria);
    }

    private RenderedCriterion fromFragmentAndParameters(FragmentAndParameters fragmentAndParameters) {
        RenderedCriterion.Builder builder = new RenderedCriterion.Builder()
                .withFragmentAndParameters(fragmentAndParameters);

        sqlCriterion.connector().ifPresent(builder::withConnector);

        return builder.build();
    }

    private RenderedCriterion calculateRenderedCriterion(List<RenderedCriterion> subCriteria) {
        return calculateRenderedCriterion(subCriteria.get(0).fragmentAndParameters(),
                subCriteria.subList(1, subCriteria.size()));
    }

    private RenderedCriterion calculateRenderedCriterion(FragmentAndParameters initialCondition, List<RenderedCriterion> subCriteria) {
        FragmentCollector fc = subCriteria.stream()
                .map(RenderedCriterion::fragmentAndParametersWithConnector)
                .collect(FragmentCollector.collect(initialCondition));
        String fragment = calculateFragment(fc);
        return fromFragmentAndParameters(FragmentAndParameters.withFragment(fragment)
                .withParameters(fc.parameters())
                .build());
    }

    private String calculateFragment(FragmentCollector collector) {
        if (collector.hasMultipleFragments()) {
            return collector.fragments()
                    .collect(Collectors.joining(" ", "(", ")")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        } else {
            return collector.fragments().findFirst().orElse(""); //$NON-NLS-1$
        }
    }

    public static <T> Builder<T> withCriterion(SqlCriterion<T> sqlCriterion) {
        return new Builder<T>().withCriterion(sqlCriterion);
    }
    
    public static class Builder<T> {
        private SqlCriterion<T> sqlCriterion;
        private AtomicInteger sequence;
        private RenderingStrategy renderingStrategy;
        private TableAliasCalculator tableAliasCalculator;
        private String parameterName;
        
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

        public Builder<T> withParameterName(String parameterName) {
            this.parameterName = parameterName;
            return this;
        }
        
        public CriterionRenderer<T> build() {
            return new CriterionRenderer<>(this);
        }
    }
}
