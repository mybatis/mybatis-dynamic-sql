package org.mybatis.qbe;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class WhereClause {
    private List<Criterion<?>> criteria = new ArrayList<>();

    private WhereClause(Stream<Criterion<?>> criteria) {
        criteria.forEach(this.criteria::add);
    }
    
    public void visitCriteria(Consumer<Criterion<?>> consumer) {
        criteria.stream().forEach(consumer);
    }
    
    public static WhereClause of(Stream<Criterion<?>> criteria) {
        return new WhereClause(criteria);
    }
}
