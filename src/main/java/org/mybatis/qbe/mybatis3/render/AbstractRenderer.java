package org.mybatis.qbe.mybatis3.render;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.mybatis.qbe.Criterion;
import org.mybatis.qbe.mybatis3.WhereClauseAndParameters;

public abstract class AbstractRenderer {

    protected Map<String, Object> parameters = new HashMap<>();
    protected StringBuilder buffer = new StringBuilder();
    
    protected <T> void handleCriterion(Criterion<T> criterion, AtomicInteger sequence) {
        WhereClauseAndParameters rc = CriterionRenderer.of(criterion, sequence).render();
        buffer.append(rc.getWhereClause());
        parameters.putAll(rc.getParameters());
    }

    protected <T> void handleCriterionWithoutTableAlias(Criterion<T> criterion, AtomicInteger sequence) {
        WhereClauseAndParameters rc = CriterionRenderer.of(criterion, sequence)
                .renderWithoutTableAlias();
        buffer.append(rc.getWhereClause());
        parameters.putAll(rc.getParameters());
    }
}
