package org.mybatis.qbe.condition;

/**
 * 
 * @author Jeff Butler
 *
 * @param <T> - even though the type is not directly used in this class,
 *  it is used by the compiler to match fields with conditions so it should
 *  not be removed.
 */
@FunctionalInterface
public interface Condition<T> {
    void accept(ConditionVisitor visitor);
}
