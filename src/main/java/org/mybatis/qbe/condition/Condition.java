package org.mybatis.qbe.condition;

import org.mybatis.qbe.field.Field;

@FunctionalInterface
public interface Condition<T> {
    void accept(ConditionVisitor visitor);
    
    default String renderFieldName(Field<T> field) {
        StringBuilder sb = new StringBuilder();
        field.alias().ifPresent(a -> {
            sb.append(a);
            sb.append('.');
        });
        sb.append(field.name());
        return sb.toString();
        
    }

    default String renderFieldNameWithoutAlias(Field<T> field) {
        return field.name();
    }
}
