/**
 *    Copyright 2016 the original author or authors.
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
package org.mybatis.qbe.sql.where.render;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.mybatis.qbe.ConditionVisitor;
import org.mybatis.qbe.ListValueCondition;
import org.mybatis.qbe.NoValueCondition;
import org.mybatis.qbe.SingleValueCondition;
import org.mybatis.qbe.TwoValueCondition;
import org.mybatis.qbe.sql.SqlField;

public class ConditionRenderer<T> implements ConditionVisitor<T> {
    
    private StringBuilder buffer = new StringBuilder();
    private Map<String, Object> parameters = new HashMap<>();
    private AtomicInteger sequence;
    private SqlField<?> field;
    private Function<SqlField<?>, String> nameFunction;
    
    private ConditionRenderer(AtomicInteger sequence, SqlField<?> field, Function<SqlField<?>, String> nameFunction) {
        this.sequence = sequence;
        this.field = field;
        this.nameFunction = nameFunction;
    }
    
    public String fragment() {
        return buffer.toString();
    }
    
    public Map<String, Object> parameters() {
        return Collections.unmodifiableMap(parameters);
    }

    @Override
    public void visit(NoValueCondition<T> condition) {
        buffer.append(condition.render(calculateFieldName()));
    }

    @Override
    public void visit(SingleValueCondition<T> condition) {
        int number = sequence.getAndIncrement();
        buffer.append(condition.render(calculateFieldName(), field.getFormattedJdbcPlaceholder(formatParameterName(number))));
        parameters.put(formatParameterMapKey(number), condition.value());
    }

    @Override
    public void visit(TwoValueCondition<T> condition) {
        int number1 = sequence.getAndIncrement();
        int number2 = sequence.getAndIncrement();
        buffer.append(condition.render(calculateFieldName(), field.getFormattedJdbcPlaceholder(formatParameterName(number1)),
                field.getFormattedJdbcPlaceholder(formatParameterName(number2))));
        parameters.put(formatParameterMapKey(number1), condition.value1());
        parameters.put(formatParameterMapKey(number2), condition.value2());
    }

    @Override
    public void visit(ListValueCondition<T> condition) {
        List<String> placeholders = new ArrayList<>();
        
        condition.values().forEach(v -> {
            int number = sequence.getAndIncrement();
            placeholders.add(field.getFormattedJdbcPlaceholder(formatParameterName(number)));
            parameters.put(formatParameterMapKey(number), v);
        });
        
        buffer.append(condition.render(calculateFieldName(), placeholders.stream()));
    }

    private String formatParameterMapKey(int number) {
        return String.format("p%s", number); //$NON-NLS-1$
    }

    private String formatParameterName(int number) {
        return String.format("parameters.p%s", number); //$NON-NLS-1$
    }
    
    private String calculateFieldName() {
        return nameFunction.apply(field);
    }
    
    public static <T> ConditionRenderer<T> of(AtomicInteger sequence, SqlField<T> field, Function<SqlField<?>, String> nameFunction) {
        return new ConditionRenderer<>(sequence, field, nameFunction);
    }
}
