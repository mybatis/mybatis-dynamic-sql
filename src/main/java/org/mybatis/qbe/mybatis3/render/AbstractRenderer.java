package org.mybatis.qbe.mybatis3.render;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.mybatis.qbe.Criterion;

public abstract class AbstractRenderer {

    protected Map<String, Object> parameters = new HashMap<>();
    protected StringBuilder buffer = new StringBuilder();
    
    protected <T> void handleCriterion(Criterion<T> criterion, AtomicInteger sequence) {
        RenderedCriterion rc = CriterionRenderer.of(criterion, sequence).render();
        buffer.append(rc.whereClauseFragment());
        parameters.putAll(rc.fragmentParameters());
    }

    protected <T> void handleCriterionWithoutTableAlias(Criterion<T> criterion, AtomicInteger sequence) {
        RenderedCriterion rc = CriterionRenderer.of(criterion, sequence)
                .renderWithoutTableAlias();
        buffer.append(rc.whereClauseFragment());
        parameters.putAll(rc.fragmentParameters());
    }
}
