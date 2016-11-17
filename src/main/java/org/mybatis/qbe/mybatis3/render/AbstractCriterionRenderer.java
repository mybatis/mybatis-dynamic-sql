package org.mybatis.qbe.mybatis3.render;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.mybatis.qbe.Criterion;

public abstract class AbstractCriterionRenderer<T> {
    private StringBuilder buffer = new StringBuilder();
    private Map<String, Object> parameters = new HashMap<>();
    private Criterion<T> criterion;
    private AtomicInteger sequence;
    
    protected AbstractCriterionRenderer(Criterion<T> criterion, AtomicInteger sequence) {
        this.criterion = criterion;
        this.sequence = sequence;
    }
    
    public RenderedCriterion render() {
        buffer.append(' ');
        
        renderConnector();

        if (criterion.hasSubCriteria()) {
            buffer.append('(');
            renderCriteria();
            buffer.append(')');
        } else {
            renderCriteria();
        }
        
        return RenderedCriterion.of(buffer.toString(), parameters);
    }
    
    private void renderConnector() {
        criterion.connector().ifPresent(c -> {
            buffer.append(c);
            buffer.append(' ');
        });
    }
    
    private void renderCriteria() {
        buffer.append(fieldName(criterion));
        buffer.append(' ');
        visitCondition();
        criterion.visitSubCriteria(c -> handleCriterion(c, sequence));
    }

    private void visitCondition() {
        ConditionRenderer visitor = ConditionRenderer.of(sequence, criterion.field());
        criterion.condition().accept(visitor);
        buffer.append(visitor.fragment());
        parameters.putAll(visitor.parameters());
    }

    private <S> void handleCriterion(Criterion<S> criterion, AtomicInteger sequence) {
        RenderedCriterion rc = newRenderer(criterion, sequence).render();
        buffer.append(rc.whereClauseFragment());
        parameters.putAll(rc.fragmentParameters());
    }

    protected abstract <S> AbstractCriterionRenderer<S> newRenderer(Criterion<S> criterion, AtomicInteger sequence);
    protected abstract String fieldName(Criterion<T> criterion);
}
