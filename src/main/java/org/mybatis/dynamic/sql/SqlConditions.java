/**
 *    Copyright 2016-2017 the original author or authors.
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
package org.mybatis.dynamic.sql;

import java.util.Arrays;
import java.util.stream.Stream;

import org.mybatis.dynamic.sql.select.SelectModelBuilder;
import org.mybatis.dynamic.sql.select.aggregate.Avg;
import org.mybatis.dynamic.sql.select.aggregate.Count;
import org.mybatis.dynamic.sql.select.aggregate.CountAll;
import org.mybatis.dynamic.sql.select.aggregate.Max;
import org.mybatis.dynamic.sql.select.aggregate.Min;
import org.mybatis.dynamic.sql.select.aggregate.Sum;
import org.mybatis.dynamic.sql.where.condition.IsBetween;
import org.mybatis.dynamic.sql.where.condition.IsEqualTo;
import org.mybatis.dynamic.sql.where.condition.IsEqualToWithSubselect;
import org.mybatis.dynamic.sql.where.condition.IsGreaterThan;
import org.mybatis.dynamic.sql.where.condition.IsGreaterThanOrEqualTo;
import org.mybatis.dynamic.sql.where.condition.IsIn;
import org.mybatis.dynamic.sql.where.condition.IsInCaseInsensitive;
import org.mybatis.dynamic.sql.where.condition.IsInWithSubselect;
import org.mybatis.dynamic.sql.where.condition.IsLessThan;
import org.mybatis.dynamic.sql.where.condition.IsLessThanOrEqualTo;
import org.mybatis.dynamic.sql.where.condition.IsLike;
import org.mybatis.dynamic.sql.where.condition.IsLikeCaseInsensitive;
import org.mybatis.dynamic.sql.where.condition.IsNotBetween;
import org.mybatis.dynamic.sql.where.condition.IsNotEqualTo;
import org.mybatis.dynamic.sql.where.condition.IsNotEqualToWithSubselect;
import org.mybatis.dynamic.sql.where.condition.IsNotIn;
import org.mybatis.dynamic.sql.where.condition.IsNotInCaseInsensitive;
import org.mybatis.dynamic.sql.where.condition.IsNotInWithSubselect;
import org.mybatis.dynamic.sql.where.condition.IsNotLike;
import org.mybatis.dynamic.sql.where.condition.IsNotLikeCaseInsensitive;
import org.mybatis.dynamic.sql.where.condition.IsNotNull;
import org.mybatis.dynamic.sql.where.condition.IsNull;

public interface SqlConditions {
    // connectors
    static <T> SqlCriterion<T> or(SqlColumn<T> column, VisitableCondition<T> condition) {
        return new SqlCriterion.Builder<T>()
                .withConnector("or") //$NON-NLS-1$
                .withColumn(column)
                .withCondition(condition)
                .build();
    }

    static <T> SqlCriterion<T> or(SqlColumn<T> column, VisitableCondition<T> condition, SqlCriterion<?>...subCriteria) {
        return new SqlCriterion.Builder<T>()
                .withConnector("or") //$NON-NLS-1$
                .withColumn(column)
                .withCondition(condition)
                .withSubCriteria(Arrays.asList(subCriteria))
                .build();
    }

    static <T> SqlCriterion<T> and(SqlColumn<T> column, VisitableCondition<T> condition) {
        return new SqlCriterion.Builder<T>()
                .withConnector("and") //$NON-NLS-1$
                .withColumn(column)
                .withCondition(condition)
                .build();
    }

    static <T> SqlCriterion<T> and(SqlColumn<T> column, VisitableCondition<T> condition, SqlCriterion<?>...subCriteria) {
        return new SqlCriterion.Builder<T>()
                .withConnector("and") //$NON-NLS-1$
                .withColumn(column)
                .withCondition(condition)
                .withSubCriteria(Arrays.asList(subCriteria))
                .build();
    }

    // aggregate support
    static CountAll count() {
        return new CountAll();
    }
    
    static <T> Count<T> count(SqlColumn<T> column) {
        return new Count<>(column);
    }
    
    static <T> Max<T> max(SqlColumn<T> column) {
        return new Max<>(column);
    }
    
    static <T> Min<T> min(SqlColumn<T> column) {
        return new Min<>(column);
    }

    static <T> Avg<T> avg(SqlColumn<T> column) {
        return new Avg<>(column);
    }

    static <T> Sum<T> sum(SqlColumn<T> column) {
        return new Sum<>(column);
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

    static <T> IsEqualToWithSubselect<T> isEqualTo(SelectModelBuilder.Buildable selectModelBuilder) {
        return IsEqualToWithSubselect.of(selectModelBuilder);
    }

    static <T> IsNotEqualTo<T> isNotEqualTo(T value) {
        return IsNotEqualTo.of(value);
    }

    static <T> IsNotEqualToWithSubselect<T> isNotEqualTo(SelectModelBuilder.Buildable selectModelBuilder) {
        return IsNotEqualToWithSubselect.of(selectModelBuilder);
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

    static <T> IsIn<T> isIn(Stream<T> values) {
        return IsIn.of(values);
    }
    
    static <T> IsInWithSubselect<T> isIn(SelectModelBuilder.Buildable selectModelBuilder) {
        return IsInWithSubselect.of(selectModelBuilder);
    }

    @SafeVarargs
    static <T> IsNotIn<T> isNotIn(T...values) {
        return isNotIn(Stream.of(values));
    }
    
    static <T> IsNotIn<T> isNotIn(Stream<T> values) {
        return IsNotIn.of(values);
    }
    
    static <T> IsNotInWithSubselect<T> isNotIn(SelectModelBuilder.Buildable selectModelBuilder) {
        return IsNotInWithSubselect.of(selectModelBuilder);
    }

    static <T> IsBetween.Builder<T> isBetween(T value1) {
        return IsBetween.isBetween(value1);
    }
    
    static <T> IsNotBetween.Builder<T> isNotBetween(T value1) {
        return IsNotBetween.isNotBetween(value1);
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
    
    static IsNotLikeCaseInsensitive isNotLikeCaseInsensitive(String value) {
        return IsNotLikeCaseInsensitive.of(value);
    }

    static IsInCaseInsensitive isInCaseInsensitive(String...values) {
        return IsInCaseInsensitive.of(Stream.of(values));
    }

    static IsNotInCaseInsensitive isNotInCaseInsensitive(String...values) {
        return IsNotInCaseInsensitive.of(Stream.of(values));
    }
}
