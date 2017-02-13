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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collector;

import org.mybatis.dynamic.sql.AbstractListValueCondition;
import org.mybatis.dynamic.sql.AbstractNoValueCondition;
import org.mybatis.dynamic.sql.AbstractSingleValueCondition;
import org.mybatis.dynamic.sql.AbstractTwoValueCondition;
import org.mybatis.dynamic.sql.ConditionVisitor;
import org.mybatis.dynamic.sql.SqlColumn;

public class ConditionRenderer<T> implements ConditionVisitor<T, FragmentAndParameters> {
    
    private static final String PARAMETERS_PREFIX = "parameters"; //$NON-NLS-1$
    
    private AtomicInteger sequence;
    private SqlColumn<T> column;
    private Function<SqlColumn<?>, String> nameFunction;
    
    private ConditionRenderer(AtomicInteger sequence, SqlColumn<T> column, Function<SqlColumn<?>, String> nameFunction) {
        this.sequence = sequence;
        this.column = column;
        this.nameFunction = nameFunction;
    }
    
    @Override
    public FragmentAndParameters visit(AbstractNoValueCondition<T> condition) {
        return new FragmentAndParameters.Builder(condition.render(calculateColumnName())).build();
    }

    @Override
    public FragmentAndParameters visit(AbstractSingleValueCondition<T> condition) {
        String mapKey = formatParameterMapKey(sequence.getAndIncrement());
        String fragment = condition.render(calculateColumnName(),
                column.getFormattedJdbcPlaceholder(PARAMETERS_PREFIX, mapKey));

        return new FragmentAndParameters.Builder(fragment)
                .withParameter(mapKey, condition.value())
                .build();
    }

    @Override
    public FragmentAndParameters visit(AbstractTwoValueCondition<T> condition) {
        String mapKey1 = formatParameterMapKey(sequence.getAndIncrement());
        String mapKey2 = formatParameterMapKey(sequence.getAndIncrement());
        String fragment = condition.render(calculateColumnName(),
                column.getFormattedJdbcPlaceholder(PARAMETERS_PREFIX, mapKey1),
                column.getFormattedJdbcPlaceholder(PARAMETERS_PREFIX, mapKey2));
                
        return new FragmentAndParameters.Builder(fragment)
                .withParameter(mapKey1, condition.value1())
                .withParameter(mapKey2, condition.value2())
                .build();
    }

    @Override
    public FragmentAndParameters visit(AbstractListValueCondition<T> condition) {
        TripleCollector tc = condition.values()
                .map(this::toTriple)
                .collect(Collector.of(TripleCollector::new,
                        TripleCollector::add,
                        TripleCollector::merge));
        
        return new FragmentAndParameters.Builder(condition.render(calculateColumnName(), tc.jdbcPlaceholders.stream()))
                .withParameters(tc.parameters)
                .build();
    }
    
    private String formatParameterMapKey(int number) {
        return "p" + number; //$NON-NLS-1$
    }

    private String calculateColumnName() {
        return nameFunction.apply(column);
    }
    
    public static <T> ConditionRenderer<T> of(AtomicInteger sequence, SqlColumn<T> column, Function<SqlColumn<?>, String> nameFunction) {
        return new ConditionRenderer<>(sequence, column, nameFunction);
    }

    private Triple<T> toTriple(T value) {
        Triple<T> t = new Triple<>();
        t.mapKey = formatParameterMapKey(sequence.getAndIncrement());
        t.jdbcPlaceholder = column.getFormattedJdbcPlaceholder(PARAMETERS_PREFIX, t.mapKey);
        t.value = value;
        return t;
    }

    static class Triple<T> {
        String mapKey;
        String jdbcPlaceholder;
        T value;
    }
    
    static class TripleCollector {
        List<String> jdbcPlaceholders = new ArrayList<>();
        Map<String, Object> parameters = new HashMap<>();
        
        public void add(Triple<?> triple) {
            jdbcPlaceholders.add(triple.jdbcPlaceholder);
            parameters.put(triple.mapKey, triple.value);
        }
        
        public TripleCollector merge(TripleCollector other) {
            jdbcPlaceholders.addAll(other.jdbcPlaceholders);
            parameters.putAll(other.parameters);
            return this;
        }
    }
}
