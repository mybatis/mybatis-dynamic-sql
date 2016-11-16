package org.mybatis.qbe.mybatis3.render;

import java.util.concurrent.atomic.AtomicInteger;

import org.mybatis.qbe.WhereClause;
import org.mybatis.qbe.mybatis3.WhereClauseAndParameters;

public class WhereClauseRenderer {
    
    private WhereClause whereClause;
    
    private WhereClauseRenderer(WhereClause whereClause) {
        this.whereClause = whereClause;
    }
    
    public WhereClauseAndParameters render() {
        // we do this so the render method can be called multiple times
        // and return the same result
        return new Renderer().render(whereClause);
    }
    
    public WhereClauseAndParameters renderWithoutTableAlias() {
        // we do this so the render method can be called multiple times
        // and return the same result
        return new Renderer().renderWithoutTableAlias(whereClause);
    }
    
    public static WhereClauseRenderer of(WhereClause whereClause) {
        return new WhereClauseRenderer(whereClause);
    }

    private static class Renderer extends AbstractRenderer {
        private AtomicInteger sequence = new AtomicInteger(1);

        public Renderer() {
            buffer.append("where"); //$NON-NLS-1$
        }
        
        public WhereClauseAndParameters render(WhereClause whereClause) {
            whereClause.visitCriteria(c -> handleCriterion(c, sequence));
            return WhereClauseAndParameters.of(buffer.toString(), parameters);
        }

        public WhereClauseAndParameters renderWithoutTableAlias(WhereClause whereClause) {
            whereClause.visitCriteria(c -> handleCriterionWithoutTableAlias(c, sequence));
            return WhereClauseAndParameters.of(buffer.toString(), parameters);
        }
    }
}
