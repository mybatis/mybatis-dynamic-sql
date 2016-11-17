package org.mybatis.qbe.mybatis3.render;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.mybatis.qbe.Criterion;
import org.mybatis.qbe.condition.ConditionVisitor;
import org.mybatis.qbe.condition.ListValueCondition;
import org.mybatis.qbe.condition.NoValueCondition;
import org.mybatis.qbe.condition.Renderable;
import org.mybatis.qbe.condition.SingleValueCondition;
import org.mybatis.qbe.condition.TwoValueCondition;

public class CriterionRenderer<T> extends AbstractRenderer implements ConditionVisitor {
    private Criterion<T> criterion;
    private AtomicInteger sequence;
    
    private CriterionRenderer(Criterion<T> criterion, AtomicInteger sequence) {
        this.criterion = criterion;
        this.sequence = sequence;
    }
    
    public RenderedCriterion render() {
        buffer.append(' ');
        
        renderConnector();

        if (criterion.hasCriteria()) {
            renderEmbeddedCriteria();
        } else {
            renderTopLevelCriterion();
        }
        
        return RenderedCriterion.of(buffer.toString(), parameters);
    }
    
    public RenderedCriterion renderWithoutTableAlias() {
        buffer.append(' ');
        
        renderConnector();

        if (criterion.hasCriteria()) {
            renderEmbeddedCriteriaWithoutTableAlias();
        } else {
            renderTopLevelCriterionWithoutTableAlias();
        }
        
        return RenderedCriterion.of(buffer.toString(), parameters);
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
        buffer.append(condition.apply(ParameterModel.of(number, criterion.field())));
        parameters.put(formatParameterName(number), condition.value());
    }

    @Override
    public void visit(TwoValueCondition<?> condition) {
        int number1 = sequence.getAndIncrement();
        int number2 = sequence.getAndIncrement();
        buffer.append(condition.apply(ParameterModel.of(number1, criterion.field()),
                ParameterModel.of(number2, criterion.field())));
        parameters.put(formatParameterName(number1), condition.value1());
        parameters.put(formatParameterName(number2), condition.value2());
    }
    
    @Override
    public void visit(ListValueCondition<?> condition) {
        List<Renderable> values = new ArrayList<>();
        
        condition.visitValues(v -> {
            int number = sequence.getAndIncrement();
            values.add(ParameterModel.of(number, criterion.field()));
            parameters.put(formatParameterName(number), v);
        });
        
        buffer.append(condition.apply(values.stream()));
    }

    public static <T> CriterionRenderer<T> of(Criterion<T> criterion, AtomicInteger sequence) {
        return new CriterionRenderer<>(criterion, sequence);
    }
    
    private static String formatParameterName(int number) {
        return String.format("p%s", number); //$NON-NLS-1$
    }
}
