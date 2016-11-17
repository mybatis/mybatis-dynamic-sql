package org.mybatis.qbe.mybatis3.render;

import java.util.concurrent.atomic.AtomicInteger;

import org.mybatis.qbe.Criterion;

public class CriterionRenderer<T> extends AbstractRenderer {
    private Criterion<T> criterion;
    private AtomicInteger sequence;
    
    private CriterionRenderer(Criterion<T> criterion, AtomicInteger sequence) {
        this.criterion = criterion;
        this.sequence = sequence;
    }
    
    public RenderedCriterion render() {
        buffer.append(' ');
        
        renderConnector();

        if (criterion.hasSubCriteria()) {
            renderTopLevelAndSubCriteria();
        } else {
            renderTopLevelCriterion();
        }
        
        return RenderedCriterion.of(buffer.toString(), parameters);
    }
    
    public RenderedCriterion renderWithoutTableAlias() {
        buffer.append(' ');
        
        renderConnector();

        if (criterion.hasSubCriteria()) {
            renderTopLevelAndSubCriteriaWithoutTableAlias();
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
    
    private void renderTopLevelAndSubCriteria() {
        buffer.append('(');
        renderTopLevelCriterion();
        criterion.visitSubCriteria(c -> handleCriterion(c, sequence));
        buffer.append(')');
    }

    private void renderTopLevelAndSubCriteriaWithoutTableAlias() {
        buffer.append('(');
        renderTopLevelCriterionWithoutTableAlias();
        criterion.visitSubCriteria(c -> handleCriterionWithoutTableAlias(c, sequence));
        buffer.append(')');
    }
    
    private void renderTopLevelCriterion() {
        buffer.append(criterion.fieldName());
        buffer.append(' ');
        visitCondition();
    }

    private void renderTopLevelCriterionWithoutTableAlias() {
        buffer.append(criterion.fieldNameWithoutAlias());
        buffer.append(' ');
        visitCondition();
    }

    private void visitCondition() {
        ConditionRenderer visitor = ConditionRenderer.of(sequence, criterion.field());
        criterion.condition().accept(visitor);
        buffer.append(visitor.fragment());
        parameters.putAll(visitor.parameters());
    }

    public static <T> CriterionRenderer<T> of(Criterion<T> criterion, AtomicInteger sequence) {
        return new CriterionRenderer<>(criterion, sequence);
    }
}
