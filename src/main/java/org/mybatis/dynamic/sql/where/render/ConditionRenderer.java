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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.mybatis.dynamic.sql.AbstractListValueCondition;
import org.mybatis.dynamic.sql.AbstractNoValueCondition;
import org.mybatis.dynamic.sql.AbstractSingleValueCondition;
import org.mybatis.dynamic.sql.AbstractTwoValueCondition;
import org.mybatis.dynamic.sql.ConditionVisitor;
import org.mybatis.dynamic.sql.SqlColumn;

public class ConditionRenderer<T> implements ConditionVisitor<T> {
    
    private StringBuilder buffer = new StringBuilder();
    private Map<String, Object> parameters = new HashMap<>();
    private AtomicInteger sequence;
    private SqlColumn<?> column;
    private Function<SqlColumn<?>, String> nameFunction;
    
    private ConditionRenderer(AtomicInteger sequence, SqlColumn<?> column, Function<SqlColumn<?>, String> nameFunction) {
        this.sequence = sequence;
        this.column = column;
        this.nameFunction = nameFunction;
    }
    
    public String fragment() {
        return buffer.toString();
    }
    
    public Map<String, Object> parameters() {
        return Collections.unmodifiableMap(parameters);
    }

    @Override
    public void visit(AbstractNoValueCondition<T> condition) {
        buffer.append(condition.render(calculateColumnName()));
    }

    @Override
    public void visit(AbstractSingleValueCondition<T> condition) {
        int number = sequence.getAndIncrement();
        buffer.append(condition.render(calculateColumnName(), column.getFormattedJdbcPlaceholder(formatParameterName(number))));
        parameters.put(formatParameterMapKey(number), condition.value());
    }

    @Override
    public void visit(AbstractTwoValueCondition<T> condition) {
        int number1 = sequence.getAndIncrement();
        int number2 = sequence.getAndIncrement();
        buffer.append(condition.render(calculateColumnName(), column.getFormattedJdbcPlaceholder(formatParameterName(number1)),
                column.getFormattedJdbcPlaceholder(formatParameterName(number2))));
        parameters.put(formatParameterMapKey(number1), condition.value1());
        parameters.put(formatParameterMapKey(number2), condition.value2());
    }

    @Override
    public void visit(AbstractListValueCondition<T> condition) {
        List<String> placeholders = new ArrayList<>();
        
        condition.values().forEach(v -> {
            int number = sequence.getAndIncrement();
            placeholders.add(column.getFormattedJdbcPlaceholder(formatParameterName(number)));
            parameters.put(formatParameterMapKey(number), v);
        });
        
        buffer.append(condition.render(calculateColumnName(), placeholders.stream()));
    }

    private String formatParameterMapKey(int number) {
        return String.format("p%s", number); //$NON-NLS-1$
    }

    private String formatParameterName(int number) {
        return String.format("parameters.p%s", number); //$NON-NLS-1$
    }
    
    private String calculateColumnName() {
        return nameFunction.apply(column);
    }
    
    public static <T> ConditionRenderer<T> of(AtomicInteger sequence, SqlColumn<T> column, Function<SqlColumn<?>, String> nameFunction) {
        return new ConditionRenderer<>(sequence, column, nameFunction);
    }
}
