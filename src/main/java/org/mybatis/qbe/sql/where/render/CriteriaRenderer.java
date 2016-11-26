package org.mybatis.qbe.sql.where.render;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.mybatis.qbe.sql.SqlCriterion;
import org.mybatis.qbe.sql.where.WhereSupport;

public class CriteriaRenderer {
    
    private List<SqlCriterion<?>> criteria = new ArrayList<>();
    
    private CriteriaRenderer(Stream<SqlCriterion<?>> criteria) {
        criteria.forEach(this.criteria::add);
    }
    
    public WhereSupport render() {
        // we do this so the render method can be called multiple times
        // and return the same result
        return new Renderer(new AtomicInteger(1)).render(criteria.stream());
    }
    
    public WhereSupport render(AtomicInteger sequence) {
        // we do this so the render method can be called multiple times
        // and return the same result
        return new Renderer(sequence).render(criteria.stream());
    }

    public static CriteriaRenderer of(Stream<SqlCriterion<?>> criteria) {
        return new CriteriaRenderer(criteria);
    }

    private static class Renderer {
        private AtomicInteger sequence;
        private StringBuilder buffer = new StringBuilder();
        private Map<String, Object> parameters = new HashMap<>();

        public Renderer(AtomicInteger sequence) {
            this.sequence = sequence;
            buffer.append("where"); //$NON-NLS-1$
        }
        
        public WhereSupport render(Stream<SqlCriterion<?>> criteria) {
            criteria.forEach(c -> {
                RenderedCriterion rc = CriterionRenderer.of(c, sequence).render();
                buffer.append(rc.whereClauseFragment());
                parameters.putAll(rc.fragmentParameters());
            });
            return WhereSupport.of(buffer.toString(), parameters);
        }
    }
}
