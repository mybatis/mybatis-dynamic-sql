package org.mybatis.qbe;

@FunctionalInterface
public interface Condition<T> {
    void accept(ConditionVisitor visitor);
    
    default String renderField(Field<T> field) {
        return field.render();
    }
}
