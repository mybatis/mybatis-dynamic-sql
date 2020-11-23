package org.mybatis.dynamic.sql;

public interface SqlCriterionVisitor<R> {
    <T> R visit(ColumnAndConditionCriterion<T> criterion);

    R visit(ExistsCriterion criterion);
}
