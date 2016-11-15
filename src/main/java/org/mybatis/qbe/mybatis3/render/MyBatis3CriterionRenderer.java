package org.mybatis.qbe.mybatis3.render;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.mybatis.qbe.Criterion;
import org.mybatis.qbe.condition.ConditionVisitor;
import org.mybatis.qbe.condition.ListValueCondition;
import org.mybatis.qbe.condition.NoValueCondition;
import org.mybatis.qbe.condition.SingleValueCondition;
import org.mybatis.qbe.condition.TwoValueCondition;
import org.mybatis.qbe.field.Field;
import org.mybatis.qbe.mybatis3.WhereClauseAndParameters;

public class MyBatis3CriterionRenderer extends BaseMyBatis3Renderer implements ConditionVisitor {
    private Criterion<?> criterion;
    private AtomicInteger sequence;
    
    private MyBatis3CriterionRenderer(Criterion<?> criterion, AtomicInteger sequence) {
        this.criterion = criterion;
        this.sequence = sequence;
    }
    
    public WhereClauseAndParameters render(boolean ignoreAlias) {
        buffer.append(' ');
        
        handleConnector();

        if (criterion.hasCriteria()) {
            handleEmbeddedCriteria(ignoreAlias);
        } else {
            handleTopLevelCriterion(ignoreAlias);
        }
        
        return WhereClauseAndParameters.of(buffer.toString(), parameters);
    }
    
    private void handleConnector() {
        criterion.connector().ifPresent(c -> {
            buffer.append(c);
            buffer.append(' ');
        });
    }
    
    private void handleEmbeddedCriteria(boolean ignoreAlias) {
        buffer.append('(');
        handleTopLevelCriterion(ignoreAlias);
        criterion.visitCriteria(c -> handleCriterion(c, sequence, ignoreAlias));
        buffer.append(')');
    }

    private void handleTopLevelCriterion(boolean ignoreAlias) {
        if (!ignoreAlias) {
            criterion.field().alias().ifPresent(alias -> {
                buffer.append(alias);
                buffer.append('.');
            });
        }
        
        buffer.append(criterion.field().name());
        buffer.append(' ');
        criterion.condition().accept(this);
    }

    @Override
    public void visit(NoValueCondition<?> condition) {
        buffer.append(condition.apply());
    }

    @Override
    public void visit(SingleValueCondition<?> condition) {
        int number = sequence.getAndIncrement();
        buffer.append(condition.apply(formatMyBatis3Parameter(number, criterion.field())));
        parameters.put(formatParameterName(number), condition.value());
    }

    @Override
    public void visit(TwoValueCondition<?> condition) {
        int number1 = sequence.getAndIncrement();
        int number2 = sequence.getAndIncrement();
        buffer.append(condition.apply(formatMyBatis3Parameter(number1, criterion.field()),
                formatMyBatis3Parameter(number2, criterion.field())));
        parameters.put(formatParameterName(number1), condition.value1());
        parameters.put(formatParameterName(number2), condition.value2());
    }
    
    @Override
    public void visit(ListValueCondition<?> condition) {
        List<String> values = new ArrayList<>();
        
        condition.visitValues(v -> {
            int number = sequence.getAndIncrement();
            values.add(formatMyBatis3Parameter(number, criterion.field()));
            parameters.put(formatParameterName(number), v);
        });
        
        buffer.append(condition.apply(values.stream()));
    }

    public static MyBatis3CriterionRenderer of(Criterion<?> criterion, AtomicInteger sequence) {
        return new MyBatis3CriterionRenderer(criterion, sequence);
    }
    
    private static String formatMyBatis3Parameter(int number, Field<?> field) {
        StringBuilder buffer = new StringBuilder();
        buffer.append("#{parameters.p"); //$NON-NLS-1$
        buffer.append(number);
        buffer.append(",jdbcType="); //$NON-NLS-1$
        buffer.append(field.jdbcType().getName());
        
        field.typeHandler().ifPresent(th -> {
            buffer.append(",typeHandler="); //$NON-NLS-1$
            buffer.append(th);
        });
        
        buffer.append('}');
        return buffer.toString();
    }
    
    private static String formatParameterName(int number) {
        return String.format("p%s", number); //$NON-NLS-1$
    }
}
