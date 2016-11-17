package org.mybatis.qbe.condition;

import java.util.stream.Stream;

import org.mybatis.qbe.Criterion;
import org.mybatis.qbe.field.Field;

public interface Conditions {
    // connectors
    static <T> Criterion<T> or(Field<T> field, Condition<T> condition, Criterion<?>...criteria) {
        return Criterion.of("or", field, condition, criteria); //$NON-NLS-1$
    }

    static <T> Criterion<T> and(Field<T> field, Condition<T> condition, Criterion<?>...criteria) {
        return Criterion.of("and", field, condition, criteria); //$NON-NLS-1$
    }

    // for all data types
    static <T> IsNullCondition<T> isNull() {
        return new IsNullCondition<>();
    }

    static <T> IsNotNullCondition<T> isNotNull() {
        return new IsNotNullCondition<>();
    }

    static <T> IsEqualToCondition<T> isEqualTo(T value) {
        return IsEqualToCondition.of(value);
    }

    static <T> IsNotEqualToCondition<T> isNotEqualTo(T value) {
        return IsNotEqualToCondition.of(value);
    }

    static <T> IsGreaterThanCondition<T> isGreaterThan(T value) {
        return IsGreaterThanCondition.of(value);
    }
    
    static <T> IsGreaterThanOrEqualToCondition<T> isGreaterThanOrEqualTo(T value) {
        return IsGreaterThanOrEqualToCondition.of(value);
    }
    
    static <T> IsLessThanCondition<T> isLessThan(T value) {
        return IsLessThanCondition.of(value);
    }
    
    static <T> IsLessThanOrEqualToCondition<T> isLessThanOrEqualTo(T value) {
        return IsLessThanOrEqualToCondition.of(value);
    }
    
    @SafeVarargs
    static <T> IsInCondition<T> isIn(T...values) {
        return isIn(Stream.of(values));
    }

    @SafeVarargs
    static <T> IsNotInCondition<T> isNotIn(T...values) {
        return isNotIn(Stream.of(values));
    }
    
    static <T> IsInCondition<T> isIn(Stream<T> values) {
        return IsInCondition.of(values);
    }

    static <T> IsNotInCondition<T> isNotIn(Stream<T> values) {
        return IsNotInCondition.of(values);
    }
    
    static <T> IsBetweenCondition.Builder<T> isBetween(T value1) {
        return IsBetweenCondition.Builder.of(value1);
    }
    
    static <T> IsNotBetweenCondition.Builder<T> isNotBetween(T value1) {
        return IsNotBetweenCondition.Builder.of(value1);
    }
    
    // for strings only
    static IsLikeCondition isLike(String value) {
        return IsLikeCondition.of(value);
    }
    
    static IsNotLikeCondition isNotLike(String value) {
        return IsNotLikeCondition.of(value);
    }
}
