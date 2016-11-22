package org.mybatis.qbe.sql.where.render;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.mybatis.qbe.ConditionVisitor;
import org.mybatis.qbe.Field;
import org.mybatis.qbe.ListValueCondition;
import org.mybatis.qbe.NoValueCondition;
import org.mybatis.qbe.Renderer;
import org.mybatis.qbe.SingleValueCondition;
import org.mybatis.qbe.TwoValueCondition;

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
        buffer.append(condition.render());
    }

    @Override
    public void visit(SingleValueCondition<?> condition) {
        int number = sequence.getAndIncrement();
        buffer.append(condition.render(field.getParameterRenderer(number)));
        parameters.put(formatParameterName(number), condition.value());
    }

    @Override
    public void visit(TwoValueCondition<?> condition) {
        int number1 = sequence.getAndIncrement();
        int number2 = sequence.getAndIncrement();
        buffer.append(condition.render(field.getParameterRenderer(number1),
                field.getParameterRenderer(number2)));
        parameters.put(formatParameterName(number1), condition.value1());
        parameters.put(formatParameterName(number2), condition.value2());
    }

    @Override
    public void visit(ListValueCondition<?> condition) {
        List<Renderer> parameterRenderers = new ArrayList<>();
        
        condition.visitValues(v -> {
            int number = sequence.getAndIncrement();
            parameterRenderers.add(field.getParameterRenderer(number));
            parameters.put(formatParameterName(number), v);
        });
        
        buffer.append(condition.render(parameterRenderers.stream()));
    }

    private String formatParameterName(int number) {
        return String.format("p%s", number); //$NON-NLS-1$
    }

    public static ConditionRenderer of(AtomicInteger sequence, Field<?> field) {
        return new ConditionRenderer(sequence, field);
    }
}
