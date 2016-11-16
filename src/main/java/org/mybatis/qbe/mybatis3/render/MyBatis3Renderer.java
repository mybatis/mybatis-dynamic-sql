package org.mybatis.qbe.mybatis3.render;

import java.util.concurrent.atomic.AtomicInteger;

import org.mybatis.qbe.CriterionContainer;
import org.mybatis.qbe.mybatis3.WhereClauseAndParameters;

public class MyBatis3Renderer {
    
    private CriterionContainer criteria;
    
    private MyBatis3Renderer(CriterionContainer criteria) {
        this.criteria = criteria;
    }
    
    public WhereClauseAndParameters render() {
        // we do this so the render method can be called multiple times
        // and return the same result
        return new Renderer().render(criteria);
    }
    
    public WhereClauseAndParameters renderWithoutTableAlias() {
        // we do this so the render method can be called multiple times
        // and return the same result
        return new Renderer().renderWithoutTableAlias(criteria);
    }
    
    public static MyBatis3Renderer of(CriterionContainer criteria) {
        return new MyBatis3Renderer(criteria);
    }

    private static class Renderer extends BaseMyBatis3Renderer {
        private AtomicInteger sequence = new AtomicInteger(1);

        public Renderer() {
            buffer.append("where"); //$NON-NLS-1$
        }
        
        public WhereClauseAndParameters render(CriterionContainer criteria) {
            criteria.visitCriteria(c -> handleCriterion(c, sequence));
            return WhereClauseAndParameters.of(buffer.toString(), parameters);
        }

        public WhereClauseAndParameters renderWithoutTableAlias(CriterionContainer criteria) {
            criteria.visitCriteria(c -> handleCriterionWithoutTableAlias(c, sequence));
            return WhereClauseAndParameters.of(buffer.toString(), parameters);
        }
    }
}
