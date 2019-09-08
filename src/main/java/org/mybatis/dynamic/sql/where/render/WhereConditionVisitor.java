/**
 *    Copyright 2016-2019 the original author or authors.
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

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.mybatis.dynamic.sql.AbstractColumnComparisonCondition;
import org.mybatis.dynamic.sql.AbstractListValueCondition;
import org.mybatis.dynamic.sql.AbstractNoValueCondition;
import org.mybatis.dynamic.sql.AbstractSingleValueCondition;
import org.mybatis.dynamic.sql.AbstractSubselectCondition;
import org.mybatis.dynamic.sql.AbstractTwoValueCondition;
import org.mybatis.dynamic.sql.BindableColumn;
import org.mybatis.dynamic.sql.ConditionVisitor;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.render.TableAliasCalculator;
import org.mybatis.dynamic.sql.select.render.SelectRenderer;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;
import org.mybatis.dynamic.sql.util.FragmentCollector;

public class WhereConditionVisitor<T> implements ConditionVisitor<T, Optional<FragmentAndParameters>> {
    
    private RenderingStrategy renderingStrategy;
    private AtomicInteger sequence;
    private BindableColumn<T> column;
    private TableAliasCalculator tableAliasCalculator;
    private String parameterPrefix;
    
    private WhereConditionVisitor(Builder<T> builder) {
        renderingStrategy = Objects.requireNonNull(builder.renderingStrategy);
        sequence = Objects.requireNonNull(builder.sequence);
        column = Objects.requireNonNull(builder.column);
        tableAliasCalculator = Objects.requireNonNull(builder.tableAliasCalculator);
        parameterPrefix = Objects.requireNonNull(builder.parameterPrefix);
    }

    @Override
    public Optional<FragmentAndParameters> visit(AbstractListValueCondition<T> condition) {
        FragmentCollector fc = condition.mapValues(this::toFragmentAndParameters)
                .collect(FragmentCollector.collect());
        
        if (fc.isEmpty()) {
            return Optional.empty();
        }
        
        return FragmentAndParameters.withFragment(condition.renderCondition(columnName(), fc.fragments()))
                .withParameters(fc.parameters())
                .buildOptional();
    }

    @Override
    public Optional<FragmentAndParameters> visit(AbstractNoValueCondition<T> condition) {
        return FragmentAndParameters.withFragment(condition.renderCondition(columnName()))
                .buildOptional();
    }

    @Override
    public Optional<FragmentAndParameters> visit(AbstractSingleValueCondition<T> condition) {
        String mapKey = formatParameterMapKey(sequence.getAndIncrement());
        String fragment = condition.renderCondition(columnName(),
                getFormattedJdbcPlaceholder(mapKey));

        return FragmentAndParameters.withFragment(fragment)
                .withParameter(mapKey, condition.value())
                .buildOptional();
    }

    @Override
    public Optional<FragmentAndParameters> visit(AbstractTwoValueCondition<T> condition) {
        String mapKey1 = formatParameterMapKey(sequence.getAndIncrement());
        String mapKey2 = formatParameterMapKey(sequence.getAndIncrement());
        String fragment = condition.renderCondition(columnName(),
                getFormattedJdbcPlaceholder(mapKey1),
                getFormattedJdbcPlaceholder(mapKey2));
                
        return FragmentAndParameters.withFragment(fragment)
                .withParameter(mapKey1, condition.value1())
                .withParameter(mapKey2, condition.value2())
                .buildOptional();
    }
    

    @Override
    public Optional<FragmentAndParameters> visit(AbstractSubselectCondition<T> condition) {
        SelectStatementProvider selectStatement = SelectRenderer.withSelectModel(condition.selectModel())
                .withRenderingStrategy(renderingStrategy)
                .withSequence(sequence)
                .build()
                .render();
        
        String fragment = condition.renderCondition(columnName(), selectStatement.getSelectStatement());
        
        return FragmentAndParameters.withFragment(fragment)
                .withParameters(selectStatement.getParameters())
                .buildOptional();
    }
    
    @Override
    public Optional<FragmentAndParameters> visit(AbstractColumnComparisonCondition<T> condition) {
        String fragment = condition.renderCondition(columnName(), tableAliasCalculator);
        return FragmentAndParameters.withFragment(fragment).buildOptional();
    }
    
    private FragmentAndParameters toFragmentAndParameters(T value) {
        String mapKey = formatParameterMapKey(sequence.getAndIncrement());
        
        return FragmentAndParameters.withFragment(getFormattedJdbcPlaceholder(mapKey))
                .withParameter(mapKey, value)
                .build();
    }

    private String formatParameterMapKey(int number) {
        return "p" + number; //$NON-NLS-1$
    }
    
    private String getFormattedJdbcPlaceholder(String mapKey) {
        return renderingStrategy.getFormattedJdbcPlaceholder(column, parameterPrefix, mapKey);        
    }
    
    private String columnName() {
        return column.renderWithTableAlias(tableAliasCalculator);
    }
    
    public static <T> Builder<T> withColumn(BindableColumn<T> column) {
        return new Builder<T>().withColumn(column);
    }
    
    public static class Builder<T> {
        private RenderingStrategy renderingStrategy;
        private AtomicInteger sequence;
        private BindableColumn<T> column;
        private TableAliasCalculator tableAliasCalculator;
        private String parameterPrefix = RenderingStrategy.DEFAULT_PARAMETER_PREFIX;
        
        public Builder<T> withSequence(AtomicInteger sequence) {
            this.sequence = sequence;
            return this;
        }
        
        public Builder<T> withRenderingStrategy(RenderingStrategy renderingStrategy) {
            this.renderingStrategy = renderingStrategy;
            return this;
        }
        
        public Builder<T> withColumn(BindableColumn<T> column) {
            this.column = column;
            return this;
        }
        
        public Builder<T> withTableAliasCalculator(TableAliasCalculator tableAliasCalculator) {
            this.tableAliasCalculator = tableAliasCalculator;
            return this;
        }

        public Builder<T> withParameterName(String parameterName) {
            if (parameterName != null) {
                parameterPrefix = parameterName + "." + RenderingStrategy.DEFAULT_PARAMETER_PREFIX; //$NON-NLS-1$
            }
            return this;
        }
        
        public WhereConditionVisitor<T> build() {
            return new WhereConditionVisitor<>(this);
        }
    }
}
