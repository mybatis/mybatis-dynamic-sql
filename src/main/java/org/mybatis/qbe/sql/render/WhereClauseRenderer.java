package org.mybatis.qbe.sql.render;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.mybatis.qbe.sql.WhereClause;

public class WhereClauseRenderer {
    
    private WhereClause whereClause;
    
    private WhereClauseRenderer(WhereClause whereClause) {
        this.whereClause = whereClause;
    }
    
    public RenderedWhereClause render() {
        // we do this so the render method can be called multiple times
        // and return the same result
        return new Renderer().render(whereClause);
    }
    
    public static WhereClauseRenderer of(WhereClause whereClause) {
        return new WhereClauseRenderer(whereClause);
    }

    private static class Renderer {
        private AtomicInteger sequence = new AtomicInteger(1);
        private StringBuilder buffer = new StringBuilder();
        private Map<String, Object> parameters = new HashMap<>();

        public Renderer() {
            buffer.append("where"); //$NON-NLS-1$
        }
        
        public RenderedWhereClause render(WhereClause whereClause) {
            whereClause.visitCriteria(c -> {
                RenderedCriterion rc = CriterionRenderer.of(c, sequence).render();
                buffer.append(rc.whereClauseFragment());
                parameters.putAll(rc.fragmentParameters());
            });
            return RenderedWhereClause.of(buffer.toString(), parameters);
        }

    }
}
