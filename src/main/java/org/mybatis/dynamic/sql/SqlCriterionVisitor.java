package org.mybatis.dynamic.sql;

public interface SqlCriterionVisitor<R> {
    <T> R visit(ColumnBasedCriterion<T> criterion);

    R visit(ExistsCriterion existsCriterion);
}
