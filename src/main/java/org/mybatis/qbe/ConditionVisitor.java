package org.mybatis.qbe;

public interface ConditionVisitor {
    void visit(NoValueCondition<?> condition);
    void visit(SingleValueCondition<?> condition);
    void visit(TwoValueCondition<?> condition);
    void visit(ListValueCondition<?> condition);
}
