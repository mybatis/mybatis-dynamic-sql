/*
 *    Copyright 2016-2025 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.dynamic.sql.where.condition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;
import org.mybatis.dynamic.sql.SqlBuilder;

/**
 * This set of tests verifies that the library handles null values in conditions as expected.
 *
 * <p>In version 2.0, we adopted JSpecify which brought several issues to light.
 * In general, the library does not support passing null values into methods unless the method
 * is a "whenPresent" method. However, from the beginning the library has handled null values in conditions
 * by placing a null into the generated parameter map. We consider this a misuse of the library, but we are
 * keeping that behavior for compatibility.
 *
 * <p>In a future version, we will stop supporting this misuse.
 *
 * <p>This set of tests should be the only tests in the library that verify this behavior. All other tests
 * should use the library properly.
 */
class NullContractTest {
    @SuppressWarnings("DataFlowIssue") // we are deliberately passing nulls into non-null methods for testing
    @Test
    void testIsBetween() {
        IsBetween<Integer> nullCond = SqlBuilder.isBetween((Integer) null).and((Integer) null);
        assertThat(nullCond.isEmpty()).isFalse();

        IsBetween<Integer> cond = SqlBuilder.isBetween(1).and(10);
        IsBetween<Integer> filtered = cond.filter(i -> i >= 1);
        IsBetween<Integer> mapped = filtered.map(i -> null);
        assertThat(mapped.isEmpty()).isFalse();

        mapped = filtered.map(v1 -> null, v2 -> null);
        assertThat(mapped.isEmpty()).isFalse();
        assertThat(mapped.value1()).isNull();
        assertThat(mapped.value2()).isNull();
    }

    @Test
    void testIsBetweenWhenPresent() {
        IsBetweenWhenPresent<Integer> nullCond = SqlBuilder.isBetweenWhenPresent((Integer) null).and((Integer) null);
        assertThat(nullCond.isEmpty()).isTrue();

        IsBetweenWhenPresent<Integer> cond = SqlBuilder.isBetweenWhenPresent(1).and(10);
        IsBetweenWhenPresent<Integer> filtered = cond.filter(i -> i == 1);
        IsBetweenWhenPresent<Integer> mapped = filtered.map(i -> null);
        assertThat(mapped.isEmpty()).isTrue();

        mapped = filtered.map(v1 -> null, v2 -> null);
        assertThat(mapped.isEmpty()).isTrue();
        assertThatExceptionOfType(NoSuchElementException.class).isThrownBy(mapped::value1);
        assertThatExceptionOfType(NoSuchElementException.class).isThrownBy(mapped::value2);
    }

    @SuppressWarnings("DataFlowIssue") // we are deliberately passing nulls into non-null methods for testing
    @Test
    void testIsEqualTo() {
        IsEqualTo<Integer> nullCond = SqlBuilder.isEqualTo((Integer) null); // should be an IDE warning
        assertThat(nullCond.isEmpty()).isFalse();

        IsEqualTo<Integer> cond = SqlBuilder.isEqualTo(1);
        IsEqualTo<Integer> filtered = cond.filter(i -> i == 1);
        IsEqualTo<Integer> mapped = filtered.map(i -> null);  // should be an IDE warning
        assertThat(mapped.isEmpty()).isFalse();
        assertThat(mapped.value()).isNull();
    }

    @Test
    void testIsEqualToWhenPresent() {
        IsEqualToWhenPresent<Integer> nullCond = SqlBuilder.isEqualToWhenPresent((Integer) null);
        assertThat(nullCond.isEmpty()).isTrue();

        IsEqualToWhenPresent<Integer> cond = SqlBuilder.isEqualToWhenPresent(1);
        IsEqualToWhenPresent<Integer> filtered = cond.filter(i -> i == 1);
        IsEqualToWhenPresent<Integer> mapped = filtered.map(i -> null);
        assertThat(mapped.isEmpty()).isTrue();
        assertThatExceptionOfType(NoSuchElementException.class).isThrownBy(mapped::value);
    }

    @SuppressWarnings("DataFlowIssue") // we are deliberately passing nulls into non-null methods for testing
    @Test
    void testIsGreaterThan() {
        IsGreaterThan<Integer> nullCond = SqlBuilder.isGreaterThan((Integer) null); // should be an IDE warning
        assertThat(nullCond.isEmpty()).isFalse();

        IsGreaterThan<Integer> cond = SqlBuilder.isGreaterThan(1);
        IsGreaterThan<Integer> filtered = cond.filter(i -> i == 1);
        IsGreaterThan<Integer> mapped = filtered.map(i -> null);  // should be an IDE warning
        assertThat(mapped.isEmpty()).isFalse();
        assertThat(mapped.value()).isNull();
    }

    @Test
    void testIsGreaterThanWhenPresent() {
        IsGreaterThanWhenPresent<Integer> nullCond = SqlBuilder.isGreaterThanWhenPresent((Integer) null);
        assertThat(nullCond.isEmpty()).isTrue();

        IsGreaterThanWhenPresent<Integer> cond = SqlBuilder.isGreaterThanWhenPresent(1);
        IsGreaterThanWhenPresent<Integer> filtered = cond.filter(i -> i == 1);
        IsGreaterThanWhenPresent<Integer> mapped = filtered.map(i -> null);
        assertThat(mapped.isEmpty()).isTrue();
        assertThatExceptionOfType(NoSuchElementException.class).isThrownBy(mapped::value);
    }

    @SuppressWarnings("DataFlowIssue") // we are deliberately passing nulls into non-null methods for testing
    @Test
    void testIsGreaterThanOrEqualTo() {
        IsGreaterThanOrEqualTo<Integer> nullCond = SqlBuilder.isGreaterThanOrEqualTo((Integer) null); // should be an IDE warning
        assertThat(nullCond.isEmpty()).isFalse();

        IsGreaterThanOrEqualTo<Integer> cond = SqlBuilder.isGreaterThanOrEqualTo(1);
        IsGreaterThanOrEqualTo<Integer> filtered = cond.filter(i -> i == 1);
        IsGreaterThanOrEqualTo<Integer> mapped = filtered.map(i -> null);  // should be an IDE warning
        assertThat(mapped.isEmpty()).isFalse();
        assertThat(mapped.value()).isNull();
    }

    @Test
    void testIsGreaterThanOrEqualToWhenPresent() {
        IsGreaterThanOrEqualToWhenPresent<Integer> nullCond = SqlBuilder.isGreaterThanOrEqualToWhenPresent((Integer) null);
        assertThat(nullCond.isEmpty()).isTrue();

        IsGreaterThanOrEqualToWhenPresent<Integer> cond = SqlBuilder.isGreaterThanOrEqualToWhenPresent(1);
        IsGreaterThanOrEqualToWhenPresent<Integer> filtered = cond.filter(i -> i == 1);
        IsGreaterThanOrEqualToWhenPresent<Integer> mapped = filtered.map(i -> null);
        assertThat(mapped.isEmpty()).isTrue();
        assertThatExceptionOfType(NoSuchElementException.class).isThrownBy(mapped::value);
    }

    @SuppressWarnings("DataFlowIssue") // we are deliberately passing nulls into non-null methods for testing
    @Test
    void testIsLessThan() {
        IsLessThan<Integer> nullCond = SqlBuilder.isLessThan((Integer) null); // should be an IDE warning
        assertThat(nullCond.isEmpty()).isFalse();

        IsLessThan<Integer> cond = SqlBuilder.isLessThan(1);
        IsLessThan<Integer> filtered = cond.filter(i -> i == 1);
        IsLessThan<Integer> mapped = filtered.map(i -> null);  // should be an IDE warning
        assertThat(mapped.isEmpty()).isFalse();
        assertThat(mapped.value()).isNull();
    }

    @Test
    void testIsLessThanWhenPresent() {
        IsLessThanWhenPresent<Integer> nullCond = SqlBuilder.isLessThanWhenPresent((Integer) null);
        assertThat(nullCond.isEmpty()).isTrue();

        IsLessThanWhenPresent<Integer> cond = SqlBuilder.isLessThanWhenPresent(1);
        IsLessThanWhenPresent<Integer> filtered = cond.filter(i -> i == 1);
        IsLessThanWhenPresent<Integer> mapped = filtered.map(i -> null);
        assertThat(mapped.isEmpty()).isTrue();
        assertThatExceptionOfType(NoSuchElementException.class).isThrownBy(mapped::value);
    }

    @SuppressWarnings("DataFlowIssue") // we are deliberately passing nulls into non-null methods for testing
    @Test
    void testIsLessThanOrEqualTo() {
        IsLessThanOrEqualTo<Integer> nullCond = SqlBuilder.isLessThanOrEqualTo((Integer) null); // should be an IDE warning
        assertThat(nullCond.isEmpty()).isFalse();

        IsLessThanOrEqualTo<Integer> cond = SqlBuilder.isLessThanOrEqualTo(1);
        IsLessThanOrEqualTo<Integer> filtered = cond.filter(i -> i == 1);
        IsLessThanOrEqualTo<Integer> mapped = filtered.map(i -> null);  // should be an IDE warning
        assertThat(mapped.isEmpty()).isFalse();
        assertThat(mapped.value()).isNull();
    }

    @Test
    void testIsLessThanOrEqualToWhenPresent() {
        IsLessThanOrEqualToWhenPresent<Integer> nullCond = SqlBuilder.isLessThanOrEqualToWhenPresent((Integer) null);
        assertThat(nullCond.isEmpty()).isTrue();

        IsLessThanOrEqualToWhenPresent<Integer> cond = SqlBuilder.isLessThanOrEqualToWhenPresent(1);
        IsLessThanOrEqualToWhenPresent<Integer> filtered = cond.filter(i -> i == 1);
        IsLessThanOrEqualToWhenPresent<Integer> mapped = filtered.map(i -> null);
        assertThat(mapped.isEmpty()).isTrue();
        assertThatExceptionOfType(NoSuchElementException.class).isThrownBy(mapped::value);
    }

    @SuppressWarnings("DataFlowIssue") // we are deliberately passing nulls into non-null methods for testing
    @Test
    void testIsNotBetween() {
        IsNotBetween<Integer> nullCond = SqlBuilder.isNotBetween((Integer) null).and((Integer) null);
        assertThat(nullCond.isEmpty()).isFalse();

        IsNotBetween<Integer> cond = SqlBuilder.isNotBetween(1).and(10);
        IsNotBetween<Integer> filtered = cond.filter(i -> i >= 1);
        IsNotBetween<Integer> mapped = filtered.map(i -> null);
        assertThat(mapped.isEmpty()).isFalse();

        mapped = filtered.map(v1 -> null, v2 -> null);
        assertThat(mapped.isEmpty()).isFalse();
        assertThat(mapped.value1()).isNull();
        assertThat(mapped.value2()).isNull();
    }

    @Test
    void testIsNotBetweenWhenPresent() {
        IsNotBetweenWhenPresent<Integer> nullCond = SqlBuilder.isNotBetweenWhenPresent((Integer) null).and((Integer) null);
        assertThat(nullCond.isEmpty()).isTrue();

        IsNotBetweenWhenPresent<Integer> cond = SqlBuilder.isNotBetweenWhenPresent(1).and(10);
        IsNotBetweenWhenPresent<Integer> filtered = cond.filter(i -> i == 1);
        IsNotBetweenWhenPresent<Integer> mapped = filtered.map(i -> null);
        assertThat(mapped.isEmpty()).isTrue();

        mapped = filtered.map(v1 -> null, v2 -> null);
        assertThat(mapped.isEmpty()).isTrue();
        assertThatExceptionOfType(NoSuchElementException.class).isThrownBy(mapped::value1);
        assertThatExceptionOfType(NoSuchElementException.class).isThrownBy(mapped::value2);
    }

    @SuppressWarnings("DataFlowIssue") // we are deliberately passing nulls into non-null methods for testing
    @Test
    void testIsNotEqualTo() {
        IsNotEqualTo<Integer> nullCond = SqlBuilder.isNotEqualTo((Integer) null); // should be an IDE warning
        assertThat(nullCond.isEmpty()).isFalse();

        IsNotEqualTo<Integer> cond = SqlBuilder.isNotEqualTo(1);
        IsNotEqualTo<Integer> filtered = cond.filter(i -> i == 1);
        IsNotEqualTo<Integer> mapped = filtered.map(i -> null); // should be an IDE warning
        assertThat(mapped.isEmpty()).isFalse();
        assertThat(mapped.value()).isNull();
    }

    @Test
    void testIsNotEqualToWhenPresent() {
        IsNotEqualToWhenPresent<Integer> nullCond = SqlBuilder.isNotEqualToWhenPresent((Integer) null);
        assertThat(nullCond.isEmpty()).isTrue();

        IsNotEqualToWhenPresent<Integer> cond = SqlBuilder.isNotEqualToWhenPresent(1);
        IsNotEqualToWhenPresent<Integer> filtered = cond.filter(i -> i == 1);
        IsNotEqualToWhenPresent<Integer> mapped = filtered.map(i -> null);
        assertThat(mapped.isEmpty()).isTrue();
        assertThatExceptionOfType(NoSuchElementException.class).isThrownBy(mapped::value);
    }

    @SuppressWarnings("DataFlowIssue") // we are deliberately passing nulls into non-null methods for testing
    @Test
    void testIsLike() {
        IsLike<String> nullCond = SqlBuilder.isLike((String) null); // should be an IDE warning
        assertThat(nullCond.isEmpty()).isFalse();

        IsLike<String> cond = SqlBuilder.isLike("fred");
        IsLike<String> filtered = cond.filter(i -> i.equals("fred"));
        IsLike<String> mapped = filtered.map(i -> null); // should be an IDE warning
        assertThat(mapped.isEmpty()).isFalse();
        assertThat(mapped.value()).isNull();
    }

    @Test
    void testIsLikeWhenPresent() {
        IsLikeWhenPresent<String> nullCond = SqlBuilder.isLikeWhenPresent((String) null);
        assertThat(nullCond.isEmpty()).isTrue();

        IsLikeWhenPresent<String> cond = SqlBuilder.isLikeWhenPresent("fred");
        IsLikeWhenPresent<String> filtered = cond.filter(i -> i.equals("fred"));
        IsLikeWhenPresent<String> mapped = filtered.map(i -> null);
        assertThat(mapped.isEmpty()).isTrue();
        assertThatExceptionOfType(NoSuchElementException.class).isThrownBy(mapped::value);
    }

    @SuppressWarnings("DataFlowIssue") // we are deliberately passing nulls into non-null methods for testing
    @Test
    void testIsLikeCaseInsensitive() {
        IsLikeCaseInsensitive<String> nullCond = SqlBuilder.isLikeCaseInsensitive((String) null); // should be an IDE warning
        assertThat(nullCond.isEmpty()).isFalse();

        IsLikeCaseInsensitive<String> cond = SqlBuilder.isLikeCaseInsensitive("fred");
        IsLikeCaseInsensitive<String> filtered = cond.filter(i -> i.equals("FRED"));
        IsLikeCaseInsensitive<String> mapped = filtered.map(i -> null); // should be an IDE warning
        assertThat(mapped.isEmpty()).isFalse();
        assertThat(mapped.value()).isNull();
    }

    @Test
    void testIsLikeCaseInsensitiveWhenPresent() {
        IsLikeCaseInsensitiveWhenPresent<String> nullCond = SqlBuilder.isLikeCaseInsensitiveWhenPresent((String) null);
        assertThat(nullCond.isEmpty()).isTrue();

        IsLikeCaseInsensitiveWhenPresent<String> cond = SqlBuilder.isLikeCaseInsensitiveWhenPresent("fred");
        IsLikeCaseInsensitiveWhenPresent<String> filtered = cond.filter(i -> i.equals("fred"));
        IsLikeCaseInsensitiveWhenPresent<String> mapped = filtered.map(i -> null);
        assertThat(mapped.isEmpty()).isTrue();
        assertThatExceptionOfType(NoSuchElementException.class).isThrownBy(mapped::value);
    }

    @SuppressWarnings("DataFlowIssue") // we are deliberately passing nulls into non-null methods for testing
    @Test
    void testIsNotLike() {
        IsNotLike<String> nullCond = SqlBuilder.isNotLike((String) null); // should be an IDE warning
        assertThat(nullCond.isEmpty()).isFalse();

        IsNotLike<String> cond = SqlBuilder.isNotLike("fred");
        IsNotLike<String> filtered = cond.filter(i -> i.equals("fred"));
        IsNotLike<String> mapped = filtered.map(i -> null); // should be an IDE warning
        assertThat(mapped.isEmpty()).isFalse();
        assertThat(mapped.value()).isNull();
    }

    @Test
    void testIsNotLikeWhenPresent() {
        IsNotLikeWhenPresent<String> nullCond = SqlBuilder.isNotLikeWhenPresent((String) null);
        assertThat(nullCond.isEmpty()).isTrue();

        IsNotLikeWhenPresent<String> cond = SqlBuilder.isNotLikeWhenPresent("fred");
        IsNotLikeWhenPresent<String> filtered = cond.filter(i -> i.equals("fred"));
        IsNotLikeWhenPresent<String> mapped = filtered.map(i -> null);
        assertThat(mapped.isEmpty()).isTrue();
        assertThatExceptionOfType(NoSuchElementException.class).isThrownBy(mapped::value);
    }

    @SuppressWarnings("DataFlowIssue") // we are deliberately passing nulls into non-null methods for testing
    @Test
    void testIsNotLikeCaseInsensitive() {
        IsNotLikeCaseInsensitive<String> nullCond = SqlBuilder.isNotLikeCaseInsensitive((String) null); // should be an IDE warning
        assertThat(nullCond.isEmpty()).isFalse();

        IsNotLikeCaseInsensitive<String> cond = SqlBuilder.isNotLikeCaseInsensitive("fred");
        IsNotLikeCaseInsensitive<String> filtered = cond.filter(i -> i.equals("FRED"));
        IsNotLikeCaseInsensitive<String> mapped = filtered.map(i -> null); // should be an IDE warning
        assertThat(mapped.isEmpty()).isFalse();
        assertThat(mapped.value()).isNull();
    }

    @Test
    void testIsNotLikeCaseInsensitiveWhenPresent() {
        IsNotLikeCaseInsensitiveWhenPresent<String> nullCond = SqlBuilder.isNotLikeCaseInsensitiveWhenPresent((String) null);
        assertThat(nullCond.isEmpty()).isTrue();

        IsNotLikeCaseInsensitiveWhenPresent<String> cond = SqlBuilder.isNotLikeCaseInsensitiveWhenPresent("fred");
        IsNotLikeCaseInsensitiveWhenPresent<String> filtered = cond.filter(i -> i.equals("FRED"));
        IsNotLikeCaseInsensitiveWhenPresent<String> mapped = filtered.map(i -> null);
        assertThat(mapped.isEmpty()).isTrue();
        assertThatExceptionOfType(NoSuchElementException.class).isThrownBy(mapped::value);
    }

    @SuppressWarnings("DataFlowIssue") // we are deliberately passing nulls into non-null methods for testing
    @Test
    void testIsIn() {
        IsIn<Integer> nullCond = SqlBuilder.isIn((Integer) null); // should be an IDE warning
        assertThat(nullCond.isEmpty()).isFalse();

        IsIn<Integer> cond = SqlBuilder.isIn(1);
        IsIn<Integer> filtered = cond.filter(i -> i == 1);
        IsIn<Integer> mapped = filtered.map(i -> null); // should be an IDE warning
        assertThat(mapped.isEmpty()).isFalse();
        assertThat(mapped.values().toList()).containsExactly((Integer) null);
    }

    @Test
    void testIsInWhenPresent() {
        IsInWhenPresent<Integer> nullCond = SqlBuilder.isInWhenPresent((Integer) null);
        assertThat(nullCond.isEmpty()).isTrue();

        IsInWhenPresent<Integer> cond = SqlBuilder.isInWhenPresent(1);
        IsInWhenPresent<Integer> filtered = cond.filter(i -> i == 1);
        IsInWhenPresent<Integer> mapped = filtered.map(i -> null);
        assertThat(mapped.isEmpty()).isTrue();
        assertThat(mapped.values().toList()).isEmpty();
    }

    @SuppressWarnings("DataFlowIssue") // we are deliberately passing nulls into non-null methods for testing
    @Test
    void testIsInCaseInsensitive() {
        IsInCaseInsensitive<String> nullCond = SqlBuilder.isInCaseInsensitive((String) null); // should be an IDE warning
        assertThat(nullCond.isEmpty()).isFalse();

        IsInCaseInsensitive<String> cond = SqlBuilder.isInCaseInsensitive("fred");
        IsInCaseInsensitive<String> filtered = cond.filter(i -> i.equals("FRED"));
        IsInCaseInsensitive<String> mapped = filtered.map(i -> null); // should be an IDE warning
        assertThat(mapped.isEmpty()).isFalse();
        assertThat(mapped.values().toList()).containsExactly((String) null);
    }

    @Test
    void testIsInCaseInsensitiveWhenPresent() {
        IsInCaseInsensitiveWhenPresent<String> nullCond = SqlBuilder.isInCaseInsensitiveWhenPresent((String) null);
        assertThat(nullCond.isEmpty()).isTrue();

        IsInCaseInsensitiveWhenPresent<String> cond = SqlBuilder.isInCaseInsensitiveWhenPresent("fred");
        IsInCaseInsensitiveWhenPresent<String> filtered = cond.filter(i -> i.equals("FRED"));
        IsInCaseInsensitiveWhenPresent<String> mapped = filtered.map(i -> null);
        assertThat(mapped.isEmpty()).isTrue();
        assertThat(mapped.values().toList()).isEmpty();
    }

    @SuppressWarnings("DataFlowIssue") // we are deliberately passing nulls into non-null methods for testing
    @Test
    void testIsNotIn() {
        IsNotIn<Integer> nullCond = SqlBuilder.isNotIn((Integer) null); // should be an IDE warning
        assertThat(nullCond.isEmpty()).isFalse();

        IsNotIn<Integer> cond = SqlBuilder.isNotIn(1);
        IsNotIn<Integer> filtered = cond.filter(i -> i == 1);
        IsNotIn<Integer> mapped = filtered.map(i -> null); // should be an IDE warning
        assertThat(mapped.isEmpty()).isFalse();
        assertThat(mapped.values().toList()).containsExactly((Integer) null);
    }

    @Test
    void testIsNotInWhenPresent() {
        IsNotInWhenPresent<Integer> nullCond = SqlBuilder.isNotInWhenPresent((Integer) null);
        assertThat(nullCond.isEmpty()).isTrue();

        IsNotInWhenPresent<Integer> cond = SqlBuilder.isNotInWhenPresent(1);
        IsNotInWhenPresent<Integer> filtered = cond.filter(i -> i == 1);
        IsNotInWhenPresent<Integer> mapped = filtered.map(i -> null);
        assertThat(mapped.isEmpty()).isTrue();
        assertThat(mapped.values().toList()).isEmpty();
    }

    @SuppressWarnings("DataFlowIssue") // we are deliberately passing nulls into non-null methods for testing
    @Test
    void testIsNotInCaseInsensitive() {
        IsNotInCaseInsensitive<String> nullCond = SqlBuilder.isNotInCaseInsensitive((String) null); // should be an IDE warning
        assertThat(nullCond.isEmpty()).isFalse();

        IsNotInCaseInsensitive<String> cond = SqlBuilder.isNotInCaseInsensitive("fred");
        IsNotInCaseInsensitive<String> filtered = cond.filter(i -> i.equals("FRED"));
        IsNotInCaseInsensitive<String> mapped = filtered.map(i -> null); // should be an IDE warning
        assertThat(mapped.isEmpty()).isFalse();
        assertThat(mapped.values().toList()).containsExactly((String) null);
    }

    @Test
    void testIsNotInCaseInsensitiveWhenPresent() {
        IsNotInCaseInsensitiveWhenPresent<String> nullCond = SqlBuilder.isNotInCaseInsensitiveWhenPresent((String) null);
        assertThat(nullCond.isEmpty()).isTrue();

        IsNotInCaseInsensitiveWhenPresent<String> cond = SqlBuilder.isNotInCaseInsensitiveWhenPresent("fred");
        IsNotInCaseInsensitiveWhenPresent<String> filtered = cond.filter(i -> i.equals("FRED"));
        IsNotInCaseInsensitiveWhenPresent<String> mapped = filtered.map(i -> null);
        assertThat(mapped.isEmpty()).isTrue();
        assertThat(mapped.values().toList()).isEmpty();
    }

    @SuppressWarnings("DataFlowIssue") // we are deliberately passing nulls into non-null methods for testing
    @Test
    void testIsBetweenNull() {
        IsBetween<Integer> cond = SqlBuilder.isBetween(() -> (Integer) null).and(() -> null);
        assertThat(cond.value1()).isNull();
        assertThat(cond.value2()).isNull();
        assertThat(cond.isEmpty()).isFalse();
    }

    @SuppressWarnings("DataFlowIssue") // we are deliberately passing nulls into non-null methods for testing
    @Test
    void testIsNotBetweenNull() {
        IsNotBetween<Integer> cond = SqlBuilder.isNotBetween(() -> (Integer) null).and(() -> null);
        assertThat(cond.value1()).isNull();
        assertThat(cond.value2()).isNull();
        assertThat(cond.isEmpty()).isFalse();
    }

    @SuppressWarnings("DataFlowIssue") // we are deliberately passing nulls into non-null methods for testing
    @Test
    void testIsNotEqualToNull() {
        IsNotEqualTo<Integer> cond = SqlBuilder.isNotEqualTo(() -> (Integer) null);
        assertThat(cond.value()).isNull();
        assertThat(cond.isEmpty()).isFalse();
    }

    @SuppressWarnings("DataFlowIssue") // we are deliberately passing nulls into non-null methods for testing
    @Test
    void testIsGreaterThanNull() {
        IsGreaterThan<Integer> cond = SqlBuilder.isGreaterThan(() -> (Integer) null);
        assertThat(cond.value()).isNull();
        assertThat(cond.isEmpty()).isFalse();
    }

    @SuppressWarnings("DataFlowIssue") // we are deliberately passing nulls into non-null methods for testing
    @Test
    void testIsGreaterThanOrEqualToNull() {
        IsGreaterThanOrEqualTo<Integer> cond = SqlBuilder.isGreaterThanOrEqualTo(() -> (Integer) null);
        assertThat(cond.value()).isNull();
        assertThat(cond.isEmpty()).isFalse();
    }

    @SuppressWarnings("DataFlowIssue") // we are deliberately passing nulls into non-null methods for testing
    @Test
    void testIsLessThanNull() {
        IsLessThan<Integer> cond = SqlBuilder.isLessThan(() -> (Integer) null);
        assertThat(cond.value()).isNull();
        assertThat(cond.isEmpty()).isFalse();
    }

    @SuppressWarnings("DataFlowIssue") // we are deliberately passing nulls into non-null methods for testing
    @Test
    void testIsLessThanOrEqualToNull() {
        IsLessThanOrEqualTo<Integer> cond = SqlBuilder.isLessThanOrEqualTo(() -> (Integer) null);
        assertThat(cond.value()).isNull();
        assertThat(cond.isEmpty()).isFalse();
    }

    @SuppressWarnings("DataFlowIssue") // we are deliberately passing nulls into non-null methods for testing
    @Test
    void testIsLikeNull() {
        IsLike<String> cond = SqlBuilder.isLike(() -> null);
        assertThat(cond.value()).isNull();
        assertThat(cond.isEmpty()).isFalse();
    }

    @SuppressWarnings("DataFlowIssue") // we are deliberately passing nulls into non-null methods for testing
    @Test
    void testIsLikeCaseInsensitiveNull() {
        IsLikeCaseInsensitive<String> cond = SqlBuilder.isLikeCaseInsensitive(() -> null);
        assertThat(cond.value()).isNull();
        assertThat(cond.isEmpty()).isFalse();
    }

    @SuppressWarnings("DataFlowIssue") // we are deliberately passing nulls into non-null methods for testing
    @Test
    void testIsNotLikeNull() {
        IsNotLike<String> cond = SqlBuilder.isNotLike(() -> null);
        assertThat(cond.value()).isNull();
        assertThat(cond.isEmpty()).isFalse();
    }

    @SuppressWarnings("DataFlowIssue") // we are deliberately passing nulls into non-null methods for testing
    @Test
    void testIsNotLikeCaseInsensitiveNull() {
        IsNotLikeCaseInsensitive<String> cond = SqlBuilder.isNotLikeCaseInsensitive(() -> null);
        assertThat(cond.value()).isNull();
        assertThat(cond.isEmpty()).isFalse();
    }
}
