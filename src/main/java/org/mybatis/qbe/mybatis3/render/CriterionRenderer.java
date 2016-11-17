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
import org.mybatis.qbe.mybatis3.RenderedWhereClause;

public class CriterionRenderer<T> extends AbstractRenderer implements ConditionVisitor {
    private Criterion<T> criterion;
    private AtomicInteger sequence;
    
    private CriterionRenderer(Criterion<T> criterion, AtomicInteger sequence) {
        this.criterion = criterion;
        this.sequence = sequence;
    }
    
    public RenderedWhereClause render() {
        buffer.append(' ');
        
        renderConnector();

        if (criterion.hasCriteria()) {
            renderEmbeddedCriteria();
        } else {
            renderTopLevelCriterion();
        }
        
        return RenderedWhereClause.of(buffer.toString(), parameters);
    }
    
    public RenderedWhereClause renderWithoutTableAlias() {
        buffer.append(' ');
        
        renderConnector();

        if (criterion.hasCriteria()) {
            renderEmbeddedCriteriaWithoutTableAlias();
        } else {
            renderTopLevelCriterionWithoutTableAlias();
        }
        
        return RenderedWhereClause.of(buffer.toString(), parameters);
    }

    private void renderConnector() {
        criterion.connector().ifPresent(c -> {
            buffer.append(c);
            buffer.append(' ');
        });
    }
    
    private void renderEmbeddedCriteria() {
        buffer.append('(');
        renderTopLevelCriterion();
        criterion.visitCriteria(c -> handleCriterion(c, sequence));
        buffer.append(')');
    }

    private void renderEmbeddedCriteriaWithoutTableAlias() {
        buffer.append('(');
        renderTopLevelCriterionWithoutTableAlias();
        criterion.visitCriteria(c -> handleCriterionWithoutTableAlias(c, sequence));
        buffer.append(')');
    }
    
    private void renderTopLevelCriterion() {
        buffer.append(criterion.fieldName());
        buffer.append(' ');
        criterion.condition().accept(this);
    }

    private void renderTopLevelCriterionWithoutTableAlias() {
        buffer.append(criterion.fieldNameWithoutAlias());
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

    public static <T> CriterionRenderer<T> of(Criterion<T> criterion, AtomicInteger sequence) {
        return new CriterionRenderer<>(criterion, sequence);
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
