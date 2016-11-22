package org.mybatis.qbe.sql.where.render;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.mybatis.qbe.sql.where.WhereClause;

public class WhereClauseRenderer {
    
    private WhereClause whereClause;
    
    private WhereClauseRenderer(WhereClause whereClause) {
        this.whereClause = whereClause;
    }
    
    public RenderedWhereClause render() {
        // we do this so the render method can be called multiple times
        // and return the same result
        return new Renderer(new AtomicInteger(1)).render(whereClause);
    }
    
    public RenderedWhereClause render(AtomicInteger sequence) {
        // we do this so the render method can be called multiple times
        // and return the same result
        return new Renderer(sequence).render(whereClause);
    }

    public static WhereClauseRenderer of(WhereClause whereClause) {
        return new WhereClauseRenderer(whereClause);
    }

    private static class Renderer {
        private AtomicInteger sequence;
        private StringBuilder buffer = new StringBuilder();
        private Map<String, Object> parameters = new HashMap<>();

        public Renderer(AtomicInteger sequence) {
            this.sequence = sequence;
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
