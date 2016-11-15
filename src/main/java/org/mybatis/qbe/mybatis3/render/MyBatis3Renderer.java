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
        return new Renderer().render(criteria, false);
    }
    
    public WhereClauseAndParameters renderWithoutTableAlias() {
        // we do this so the render method can be called multiple times
        // and return the same result
        return new Renderer().render(criteria, true);
    }
    
    public static MyBatis3Renderer of(CriterionContainer criteria) {
        return new MyBatis3Renderer(criteria);
    }

    private static class Renderer extends BaseMyBatis3Renderer {
        public WhereClauseAndParameters render(CriterionContainer criteria, boolean ignoreAlias) {
            AtomicInteger sequence = new AtomicInteger(1);
            buffer.append("where"); //$NON-NLS-1$
            criteria.visitCriteria(c -> handleCriterion(c, sequence, ignoreAlias));
            return WhereClauseAndParameters.of(buffer.toString(), parameters);
        }
    }
}
