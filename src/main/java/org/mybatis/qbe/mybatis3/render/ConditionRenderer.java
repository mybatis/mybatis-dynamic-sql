package org.mybatis.qbe.mybatis3.render;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.mybatis.qbe.condition.ConditionVisitor;
import org.mybatis.qbe.condition.ListValueCondition;
import org.mybatis.qbe.condition.NoValueCondition;
import org.mybatis.qbe.condition.Renderable;
import org.mybatis.qbe.condition.SingleValueCondition;
import org.mybatis.qbe.condition.TwoValueCondition;
import org.mybatis.qbe.field.Field;

public class ConditionRenderer implements ConditionVisitor {
    
    private StringBuilder buffer = new StringBuilder();
    private Map<String, Object> parameters = new HashMap<>();
    private AtomicInteger sequence;
    private Field<?> field;
    
    private ConditionRenderer(AtomicInteger sequence, Field<?> field) {
        this.sequence = sequence;
        this.field = field;
    }
    
    public String fragment() {
        return buffer.toString();
    }
    
    public Map<String, Object> parameters() {
        return Collections.unmodifiableMap(parameters);
    }

    @Override
    public void visit(NoValueCondition<?> condition) {
        buffer.append(condition.apply());
    }

    @Override
    public void visit(SingleValueCondition<?> condition) {
        int number = sequence.getAndIncrement();
        buffer.append(condition.apply(ParameterModel.of(number, field)));
        parameters.put(formatParameterName(number), condition.value());
    }

    @Override
    public void visit(TwoValueCondition<?> condition) {
        int number1 = sequence.getAndIncrement();
        int number2 = sequence.getAndIncrement();
        buffer.append(condition.apply(ParameterModel.of(number1, field),
                ParameterModel.of(number2, field)));
        parameters.put(formatParameterName(number1), condition.value1());
        parameters.put(formatParameterName(number2), condition.value2());
    }

    @Override
    public void visit(ListValueCondition<?> condition) {
        List<Renderable> values = new ArrayList<>();
        
        condition.visitValues(v -> {
            int number = sequence.getAndIncrement();
            values.add(ParameterModel.of(number, field));
            parameters.put(formatParameterName(number), v);
        });
        
        buffer.append(condition.apply(values.stream()));
    }

    private String formatParameterName(int number) {
        return String.format("p%s", number); //$NON-NLS-1$
    }

    public static ConditionRenderer of(AtomicInteger sequence, Field<?> field) {
        return new ConditionRenderer(sequence, field);
    }
}
