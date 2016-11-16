package org.mybatis.qbe.condition;

import org.mybatis.qbe.field.Field;

@FunctionalInterface
public interface Condition<T> {
    void accept(ConditionVisitor visitor);
    
    default String fieldName(Field<T> field) {
        return field.aliasedName();
    }

    default String fieldNameWithoutAlias(Field<T> field) {
        return field.name();
    }
}
