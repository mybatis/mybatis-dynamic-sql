/**
 *    Copyright 2016 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.qbe.sql;

import java.util.stream.Stream;

import org.mybatis.qbe.Condition;
import org.mybatis.qbe.sql.where.condition.IsBetween;
import org.mybatis.qbe.sql.where.condition.IsEqualTo;
import org.mybatis.qbe.sql.where.condition.IsGreaterThan;
import org.mybatis.qbe.sql.where.condition.IsGreaterThanOrEqualTo;
import org.mybatis.qbe.sql.where.condition.IsIn;
import org.mybatis.qbe.sql.where.condition.IsLessThan;
import org.mybatis.qbe.sql.where.condition.IsLessThanOrEqualTo;
import org.mybatis.qbe.sql.where.condition.IsLike;
import org.mybatis.qbe.sql.where.condition.IsLikeCaseInsensitive;
import org.mybatis.qbe.sql.where.condition.IsNotBetween;
import org.mybatis.qbe.sql.where.condition.IsNotEqualTo;
import org.mybatis.qbe.sql.where.condition.IsNotIn;
import org.mybatis.qbe.sql.where.condition.IsNotLike;
import org.mybatis.qbe.sql.where.condition.IsNotNull;
import org.mybatis.qbe.sql.where.condition.IsNull;

public interface SqlConditions {
    // connectors
    static <T> SqlCriterion<T> or(SqlField<T> field, Condition<T> condition, SqlCriterion<?>...subCriteria) {
        return SqlCriterion.of("or", field, condition, subCriteria); //$NON-NLS-1$
    }

    static <T> SqlCriterion<T> and(SqlField<T> field, Condition<T> condition, SqlCriterion<?>...subCriteria) {
        return SqlCriterion.of("and", field, condition, subCriteria); //$NON-NLS-1$
    }

    // for all data types
    static <T> IsNull<T> isNull() {
        return new IsNull<>();
    }

    static <T> IsNotNull<T> isNotNull() {
        return new IsNotNull<>();
    }

    static <T> IsEqualTo<T> isEqualTo(T value) {
        return IsEqualTo.of(value);
    }

    static <T> IsNotEqualTo<T> isNotEqualTo(T value) {
        return IsNotEqualTo.of(value);
    }

    static <T> IsGreaterThan<T> isGreaterThan(T value) {
        return IsGreaterThan.of(value);
    }
    
    static <T> IsGreaterThanOrEqualTo<T> isGreaterThanOrEqualTo(T value) {
        return IsGreaterThanOrEqualTo.of(value);
    }
    
    static <T> IsLessThan<T> isLessThan(T value) {
        return IsLessThan.of(value);
    }
    
    static <T> IsLessThanOrEqualTo<T> isLessThanOrEqualTo(T value) {
        return IsLessThanOrEqualTo.of(value);
    }
    
    @SafeVarargs
    static <T> IsIn<T> isIn(T...values) {
        return isIn(Stream.of(values));
    }

    @SafeVarargs
    static <T> IsNotIn<T> isNotIn(T...values) {
        return isNotIn(Stream.of(values));
    }
    
    static <T> IsIn<T> isIn(Stream<T> values) {
        return IsIn.of(values);
    }

    static <T> IsNotIn<T> isNotIn(Stream<T> values) {
        return IsNotIn.of(values);
    }
    
    static <T> IsBetween.Builder<T> isBetween(T value1) {
        return IsBetween.Builder.of(value1);
    }
    
    static <T> IsNotBetween.Builder<T> isNotBetween(T value1) {
        return IsNotBetween.Builder.of(value1);
    }
    
    // for strings only
    static IsLike isLike(String value) {
        return IsLike.of(value);
    }
    
    static IsLikeCaseInsensitive isLikeCaseInsensitive(String value) {
        return IsLikeCaseInsensitive.of(value);
    }
    
    static IsNotLike isNotLike(String value) {
        return IsNotLike.of(value);
    }
}
