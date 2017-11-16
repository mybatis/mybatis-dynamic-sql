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

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import org.mybatis.dynamic.sql.AbstractListValueCondition;
import org.mybatis.dynamic.sql.AbstractNoValueCondition;
import org.mybatis.dynamic.sql.AbstractSingleValueCondition;
import org.mybatis.dynamic.sql.AbstractSubselectCondition;
import org.mybatis.dynamic.sql.AbstractTwoValueCondition;
import org.mybatis.dynamic.sql.ConditionVisitor;
import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.render.TableAliasCalculator;
import org.mybatis.dynamic.sql.select.render.SelectRenderer;
import org.mybatis.dynamic.sql.select.render.SelectSupport;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;
import org.mybatis.dynamic.sql.util.FragmentCollector;

public class WhereConditionVisitor<T> implements ConditionVisitor<T, FragmentAndParameters> {
    
    private static final String PARAMETERS_PREFIX = "parameters"; //$NON-NLS-1$
    private RenderingStrategy renderingStrategy;
    private AtomicInteger sequence;
    private SqlColumn<T> column;
    private TableAliasCalculator tableAliasCalculator;
    
    private WhereConditionVisitor(Builder<T> builder) {
        renderingStrategy = Objects.requireNonNull(builder.renderingStrategy);
        sequence = Objects.requireNonNull(builder.sequence);
        column = Objects.requireNonNull(builder.column);
        tableAliasCalculator = Objects.requireNonNull(builder.tableAliasCalculator);
    }

    @Override
    public FragmentAndParameters visit(AbstractListValueCondition<T> condition) {
        FragmentCollector fc = condition.mapValues(this::toFragmentAndParameters)
                .collect(FragmentCollector.collect());
        
        return new FragmentAndParameters.Builder()
                .withFragment(condition.renderCondition(columnName(), fc.fragments()))
                .withParameters(fc.parameters())
                .build();
    }

    @Override
    public FragmentAndParameters visit(AbstractNoValueCondition<T> condition) {
        return new FragmentAndParameters.Builder()
                .withFragment(condition.renderCondition(columnName()))
                .build();
    }

    @Override
    public FragmentAndParameters visit(AbstractSingleValueCondition<T> condition) {
        String mapKey = formatParameterMapKey(sequence.getAndIncrement());
        String fragment = condition.renderCondition(columnName(),
                getFormattedJdbcPlaceholder(mapKey));

        return new FragmentAndParameters.Builder()
                .withFragment(fragment)
                .withParameter(mapKey, condition.value())
                .build();
    }

    @Override
    public FragmentAndParameters visit(AbstractTwoValueCondition<T> condition) {
        String mapKey1 = formatParameterMapKey(sequence.getAndIncrement());
        String mapKey2 = formatParameterMapKey(sequence.getAndIncrement());
        String fragment = condition.renderCondition(columnName(),
                getFormattedJdbcPlaceholder(mapKey1),
                getFormattedJdbcPlaceholder(mapKey2));
                
        return new FragmentAndParameters.Builder()
                .withFragment(fragment)
                .withParameter(mapKey1, condition.value1())
                .withParameter(mapKey2, condition.value2())
                .build();
    }
    

    @Override
    public FragmentAndParameters visit(AbstractSubselectCondition<T> condition) {
        SelectSupport ss = new SelectRenderer.Builder()
                .withSelectModel(condition.selectModel())
                .withRenderingStrategy(renderingStrategy)
                .withSequence(sequence)
                .build()
                .render();
        
        return new FragmentAndParameters.Builder()
                .withFragment(condition.renderCondition(columnName(), ss.getFullSelectStatement()))
                .withParameters(ss.getParameters())
                .build();
    }
    
    private FragmentAndParameters toFragmentAndParameters(Object value) {
        String mapKey = formatParameterMapKey(sequence.getAndIncrement());
        
        return new FragmentAndParameters.Builder()
                .withFragment(getFormattedJdbcPlaceholder(mapKey))
                .withParameter(mapKey, value)
                .build();
    }

    private String formatParameterMapKey(int number) {
        return "p" + number; //$NON-NLS-1$
    }
    
    private String getFormattedJdbcPlaceholder(String mapKey) {
        return renderingStrategy.getFormattedJdbcPlaceholder(column, PARAMETERS_PREFIX, mapKey);        
    }
    
    private String columnName() {
        return column.applyTableAliasToName(tableAliasCalculator);
    }
    
    public static class Builder<T> {
        private RenderingStrategy renderingStrategy;
        private AtomicInteger sequence;
        private SqlColumn<T> column;
        private TableAliasCalculator tableAliasCalculator;
        
        public Builder<T> withSequence(AtomicInteger sequence) {
            this.sequence = sequence;
            return this;
        }
        
        public Builder<T> withRenderingStrategy(RenderingStrategy renderingStrategy) {
            this.renderingStrategy = renderingStrategy;
            return this;
        }
        
        public Builder<T> withColumn(SqlColumn<T> column) {
            this.column = column;
            return this;
        }
        
        public Builder<T> withTableAliasCalculator(TableAliasCalculator tableAliasCalculator) {
            this.tableAliasCalculator = tableAliasCalculator;
            return this;
        }
        
        public WhereConditionVisitor<T> build() {
            return new WhereConditionVisitor<>(this);
        }
    }
}
