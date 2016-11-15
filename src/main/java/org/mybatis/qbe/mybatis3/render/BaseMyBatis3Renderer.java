package org.mybatis.qbe.mybatis3.render;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.mybatis.qbe.Criterion;
import org.mybatis.qbe.mybatis3.WhereClauseAndParameters;

public abstract class BaseMyBatis3Renderer {

    protected Map<String, Object> parameters = new HashMap<>();
    protected StringBuilder buffer = new StringBuilder();
    
    protected void handleCriterion(Criterion<?> criterion, AtomicInteger sequence, boolean ignoreAlias) {
        MyBatis3CriterionRenderer renderer = MyBatis3CriterionRenderer.of(criterion, sequence);
        WhereClauseAndParameters rc = renderer.render(ignoreAlias);
        buffer.append(rc.getWhereClause());
        parameters.putAll(rc.getParameters());
    }
}
