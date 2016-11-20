package org.mybatis.qbe.sql.render;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.mybatis.qbe.sql.SqlCriterion;

public class CriterionRenderer<T> {
    private StringBuilder buffer = new StringBuilder();
    private Map<String, Object> parameters = new HashMap<>();
    private SqlCriterion<T> criterion;
    private AtomicInteger sequence;
    
    protected CriterionRenderer(SqlCriterion<T> criterion, AtomicInteger sequence) {
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
        buffer.append(criterion.renderField());
        buffer.append(' ');
        renderCondition();
        criterion.visitSubCriteria(c -> handleSubCriterion(c, sequence));
    }

    private void renderCondition() {
        ConditionRenderer visitor = ConditionRenderer.of(sequence, criterion.field());
        criterion.condition().accept(visitor);
        buffer.append(visitor.fragment());
        parameters.putAll(visitor.parameters());
    }

    private void handleSubCriterion(SqlCriterion<?> subCriterion, AtomicInteger sequence) {
        RenderedCriterion rc = new CriterionRenderer<>(subCriterion, sequence).render();
        buffer.append(rc.whereClauseFragment());
        parameters.putAll(rc.fragmentParameters());
    }
    
    public static <T> CriterionRenderer<T> of(SqlCriterion<T> criterion, AtomicInteger sequence) {
        return new CriterionRenderer<>(criterion, sequence);
    }
}
