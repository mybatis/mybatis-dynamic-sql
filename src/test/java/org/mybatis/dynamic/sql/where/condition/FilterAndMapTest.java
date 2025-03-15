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

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import org.junit.jupiter.api.Test;
import org.mybatis.dynamic.sql.SqlBuilder;

class FilterAndMapTest {
    @Test
    void testTypeConversion() {
        var cond = SqlBuilder.isEqualTo("1").map(Integer::parseInt);
        assertThat(cond.isEmpty()).isFalse();
        assertThat(cond.value()).isEqualTo(1);
    }

    @Test
    void testTypeConversionWithNullThrowsException() {
        var cond = SqlBuilder.isEqualTo((String) null);
        assertThatExceptionOfType(NumberFormatException.class).isThrownBy(() ->
                cond.map(Integer::parseInt)
        );
    }

    @Test
    void testTypeConversionWithNullAndFilterDoesNotThrowException() {
        var cond = SqlBuilder.isEqualTo((String) null).filter(Objects::nonNull).map(Integer::parseInt);
        assertThat(cond.isEmpty()).isTrue();
    }

    @Test
    void testIsNullRenderableTruePredicateShouldReturnSameObject() {
        IsNull<String> cond = SqlBuilder.isNull();
        IsNull<String> filtered = cond.filter(() -> true);
        assertThat(filtered.isEmpty()).isFalse();
        assertThat(cond).isSameAs(filtered);
    }

    @Test
    void testIsNullRenderableFalsePredicate() {
        IsNull<String> cond = SqlBuilder.isNull();
        IsNull<String> filtered = cond.filter(() -> false);
        assertThat(cond.isEmpty()).isFalse();
        assertThat(filtered.isEmpty()).isTrue();
    }

    @Test
    void testIsNullFilterUnRenderableShouldReturnSameObject() {
        IsNull<String> cond = SqlBuilder.isNull().filter(() -> false);
        IsNull<String> filtered = cond.filter(() -> true);
        assertThat(filtered.isEmpty()).isTrue();
        assertThat(cond).isSameAs(filtered);
    }

    @Test
    void testIsNotNullRenderableTruePredicateShouldReturnSameObject() {
        IsNotNull<String> cond = SqlBuilder.isNotNull();
        IsNotNull<String> filtered = cond.filter(() -> true);
        assertThat(filtered.isEmpty()).isFalse();
        assertThat(cond).isSameAs(filtered);
    }

    @Test
    void testIsNotNullRenderableFalsePredicate() {
        IsNotNull<String> cond = SqlBuilder.isNotNull();
        IsNotNull<String> filtered = cond.filter(() -> false);
        assertThat(cond.isEmpty()).isFalse();
        assertThat(filtered.isEmpty()).isTrue();
    }

    @Test
    void testIsNotNullFilterUnRenderableShouldReturnSameObject() {
        IsNotNull<String> cond = SqlBuilder.isNotNull().filter(() -> false);
        IsNotNull<String> filtered = cond.filter(() -> true);
        assertThat(filtered.isEmpty()).isTrue();
        assertThat(cond).isSameAs(filtered);
    }

    @Test
    void testIsEqualRenderableTruePredicateShouldReturnSameObject() {
        IsEqualTo<String> cond = SqlBuilder.isEqualTo("Fred");
        IsEqualTo<String> filtered = cond.filter(s -> true);
        assertThat(filtered.isEmpty()).isFalse();
        assertThat(cond).isSameAs(filtered);
    }

    @Test
    void testIsEqualRenderableFalsePredicate() {
        IsEqualTo<String> cond = SqlBuilder.isEqualTo("Fred");
        IsEqualTo<String> filtered = cond.filter(s -> false);
        assertThat(cond.isEmpty()).isFalse();
        assertThat(filtered.isEmpty()).isTrue();
    }

    @Test
    void testIsEqualFilterUnRenderableShouldReturnSameObject() {
        IsEqualTo<String> cond = SqlBuilder.isEqualTo("Fred").filter(s -> false);
        IsEqualTo<String> filtered = cond.filter(s -> true);
        assertThat(filtered.isEmpty()).isTrue();
        assertThat(cond).isSameAs(filtered);
    }

    @Test
    void testIsEqualMapUnRenderableShouldNotThrowNullPointerException() {
        IsEqualTo<String> cond = SqlBuilder.isEqualTo("Fred").filter(s -> false);
        IsEqualTo<String> mapped = cond.map(String::toUpperCase);
        assertThat(cond.isEmpty()).isTrue();
        assertThatExceptionOfType(NoSuchElementException.class).isThrownBy(cond::value);
        assertThat(cond).isSameAs(mapped);
    }

    @Test
    void testIsNotEqualRenderableTruePredicateShouldReturnSameObject() {
        IsNotEqualTo<String> cond = SqlBuilder.isNotEqualTo("Fred");
        IsNotEqualTo<String> filtered = cond.filter(s -> true);
        assertThat(filtered.isEmpty()).isFalse();
        assertThat(cond).isSameAs(filtered);
    }

    @Test
    void testIsNotEqualRenderableFalsePredicate() {
        IsNotEqualTo<String> cond = SqlBuilder.isNotEqualTo("Fred");
        IsNotEqualTo<String> filtered = cond.filter(s -> false);
        assertThat(cond.isEmpty()).isFalse();
        assertThat(filtered.isEmpty()).isTrue();
    }

    @Test
    void testIsNotEqualFilterUnRenderableShouldReturnSameObject() {
        IsNotEqualTo<String> cond = SqlBuilder.isNotEqualTo("Fred").filter(s -> false);
        IsNotEqualTo<String> filtered = cond.filter(s -> true);
        assertThat(filtered.isEmpty()).isTrue();
        assertThat(cond).isSameAs(filtered);
    }

    @Test
    void testIsNotEqualMapUnRenderableShouldNotThrowNullPointerException() {
        IsNotEqualTo<String> cond = SqlBuilder.isNotEqualTo("Fred").filter(s -> false);
        IsNotEqualTo<String> mapped = cond.map(String::toUpperCase);
        assertThat(cond.isEmpty()).isTrue();
        assertThatExceptionOfType(NoSuchElementException.class).isThrownBy(cond::value);
        assertThat(cond).isSameAs(mapped);
    }

    @Test
    void testIsLessThanRenderableTruePredicateShouldReturnSameObject() {
        IsLessThan<String> cond = SqlBuilder.isLessThan("Fred");
        IsLessThan<String> filtered = cond.filter(s -> true);
        assertThat(filtered.isEmpty()).isFalse();
        assertThat(cond).isSameAs(filtered);
    }

    @Test
    void testIsLessThanRenderableFalsePredicate() {
        IsLessThan<String> cond = SqlBuilder.isLessThan("Fred");
        IsLessThan<String> filtered = cond.filter(s -> false);
        assertThat(cond.isEmpty()).isFalse();
        assertThat(filtered.isEmpty()).isTrue();
    }

    @Test
    void testIsLessThanFilterUnRenderableShouldReturnSameObject() {
        IsLessThan<String> cond = SqlBuilder.isLessThan("Fred").filter(s -> false);
        IsLessThan<String> filtered = cond.filter(s -> true);
        assertThat(filtered.isEmpty()).isTrue();
        assertThat(cond).isSameAs(filtered);
    }

    @Test
    void testIsLessThanMapUnRenderableShouldNotThrowNullPointerException() {
        IsLessThan<String> cond = SqlBuilder.isLessThan("Fred").filter(s -> false);
        IsLessThan<String> mapped = cond.map(String::toUpperCase);
        assertThat(cond.isEmpty()).isTrue();
        assertThatExceptionOfType(NoSuchElementException.class).isThrownBy(cond::value);
        assertThat(cond).isSameAs(mapped);
    }

    @Test
    void testIsLessThanOrEqualRenderableTruePredicateShouldReturnSameObject() {
        IsLessThanOrEqualTo<String> cond = SqlBuilder.isLessThanOrEqualTo("Fred");
        IsLessThanOrEqualTo<String> filtered = cond.filter(s -> true);
        assertThat(filtered.isEmpty()).isFalse();
        assertThat(cond).isSameAs(filtered);
    }

    @Test
    void testIsLessThanOrEqualRenderableFalsePredicate() {
        IsLessThanOrEqualTo<String> cond = SqlBuilder.isLessThanOrEqualTo("Fred");
        IsLessThanOrEqualTo<String> filtered = cond.filter(s -> false);
        assertThat(cond.isEmpty()).isFalse();
        assertThat(filtered.isEmpty()).isTrue();
    }

    @Test
    void testIsLessThanOrEqualFilterUnRenderableShouldReturnSameObject() {
        IsLessThanOrEqualTo<String> cond = SqlBuilder.isLessThanOrEqualTo("Fred").filter(s -> false);
        IsLessThanOrEqualTo<String> filtered = cond.filter(s -> true);
        assertThat(filtered.isEmpty()).isTrue();
        assertThat(cond).isSameAs(filtered);
    }

    @Test
    void testIsLessThanOrEqualMapUnRenderableShouldNotThrowNullPointerException() {
        IsLessThanOrEqualTo<String> cond = SqlBuilder.isLessThanOrEqualTo("Fred").filter(s -> false);
        IsLessThanOrEqualTo<String> mapped = cond.map(String::toUpperCase);
        assertThat(cond.isEmpty()).isTrue();
        assertThatExceptionOfType(NoSuchElementException.class).isThrownBy(cond::value);
        assertThat(cond).isSameAs(mapped);
    }

    @Test
    void testIsGreaterThanRenderableTruePredicateShouldReturnSameObject() {
        IsGreaterThan<String> cond = SqlBuilder.isGreaterThan("Fred");
        IsGreaterThan<String> filtered = cond.filter(s -> true);
        assertThat(filtered.isEmpty()).isFalse();
        assertThat(cond).isSameAs(filtered);
    }

    @Test
    void testIsGreaterThanRenderableFalsePredicate() {
        IsGreaterThan<String> cond = SqlBuilder.isGreaterThan("Fred");
        IsGreaterThan<String> filtered = cond.filter(s -> false);
        assertThat(cond.isEmpty()).isFalse();
        assertThat(filtered.isEmpty()).isTrue();
    }

    @Test
    void testIsGreaterThanFilterUnRenderableShouldReturnSameObject() {
        IsGreaterThan<String> cond = SqlBuilder.isGreaterThan("Fred").filter(s -> false);
        IsGreaterThan<String> filtered = cond.filter(s -> true);
        assertThat(filtered.isEmpty()).isTrue();
        assertThat(cond).isSameAs(filtered);
    }

    @Test
    void testIsGreaterThanMapUnRenderableShouldNotThrowNullPointerException() {
        IsGreaterThan<String> cond = SqlBuilder.isGreaterThan("Fred").filter(s -> false);
        IsGreaterThan<String> mapped = cond.map(String::toUpperCase);
        assertThat(cond.isEmpty()).isTrue();
        assertThatExceptionOfType(NoSuchElementException.class).isThrownBy(cond::value);
        assertThat(cond).isSameAs(mapped);
    }

    @Test
    void testIsGreaterThanOrEqualRenderableTruePredicateShouldReturnSameObject() {
        IsGreaterThanOrEqualTo<String> cond = SqlBuilder.isGreaterThanOrEqualTo("Fred");
        IsGreaterThanOrEqualTo<String> filtered = cond.filter(s -> true);
        assertThat(filtered.isEmpty()).isFalse();
        assertThat(cond).isSameAs(filtered);
    }

    @Test
    void testIsGreaterThanOrEqualRenderableFalsePredicate() {
        IsGreaterThanOrEqualTo<String> cond = SqlBuilder.isGreaterThanOrEqualTo("Fred");
        IsGreaterThanOrEqualTo<String> filtered = cond.filter(s -> false);
        assertThat(cond.isEmpty()).isFalse();
        assertThat(filtered.isEmpty()).isTrue();
    }

    @Test
    void testIsGreaterThanOrEqualFilterUnRenderableShouldReturnSameObject() {
        IsGreaterThanOrEqualTo<String> cond = SqlBuilder.isGreaterThanOrEqualTo("Fred").filter(s -> false);
        IsGreaterThanOrEqualTo<String> filtered = cond.filter(s -> true);
        assertThat(filtered.isEmpty()).isTrue();
        assertThat(cond).isSameAs(filtered);
    }

    @Test
    void testIsGreaterThanOrEqualMapUnRenderableShouldNotThrowNullPointerException() {
        IsGreaterThanOrEqualTo<String> cond = SqlBuilder.isGreaterThanOrEqualTo("Fred").filter(s -> false);
        IsGreaterThanOrEqualTo<String> mapped = cond.map(String::toUpperCase);
        assertThat(cond.isEmpty()).isTrue();
        assertThatExceptionOfType(NoSuchElementException.class).isThrownBy(cond::value);
        assertThat(cond).isSameAs(mapped);
    }

    @Test
    void testIsLikeRenderableTruePredicateShouldReturnSameObject() {
        IsLike<String> cond = SqlBuilder.isLike("Fred");
        IsLike<String> filtered = cond.filter(s -> true);
        assertThat(filtered.isEmpty()).isFalse();
        assertThat(cond).isSameAs(filtered);
    }

    @Test
    void testIsLikeRenderableFalsePredicate() {
        IsLike<String> cond = SqlBuilder.isLike("Fred");
        IsLike<String> filtered = cond.filter(s -> false);
        assertThat(cond.isEmpty()).isFalse();
        assertThat(filtered.isEmpty()).isTrue();
    }

    @Test
    void testIsLikeFilterUnRenderableShouldReturnSameObject() {
        IsLike<String> cond = SqlBuilder.isLike("Fred").filter(s -> false);
        IsLike<String> filtered = cond.filter(s -> true);
        assertThat(filtered.isEmpty()).isTrue();
        assertThat(cond).isSameAs(filtered);
    }

    @Test
    void testIsLikeMapUnRenderableShouldNotThrowNullPointerException() {
        IsLike<String> cond = SqlBuilder.isLike("Fred").filter(s -> false);
        IsLike<String> mapped = cond.map(String::toUpperCase);
        assertThat(cond.isEmpty()).isTrue();
        assertThatExceptionOfType(NoSuchElementException.class).isThrownBy(cond::value);
        assertThat(cond).isSameAs(mapped);
    }

    @Test
    void testIsLikeCaseInsensitiveRenderableTruePredicateShouldReturnSameObject() {
        var cond = SqlBuilder.isLikeCaseInsensitive("Fred");
        var filtered = cond.filter(s -> true);
        assertThat(filtered.value()).isEqualTo("FRED");
        assertThat(filtered.isEmpty()).isFalse();
        assertThat(cond).isSameAs(filtered);
    }

    @Test
    void testIsLikeCaseInsensitiveRenderableFalsePredicate() {
        var cond = SqlBuilder.isLikeCaseInsensitive("Fred");
        var filtered = cond.filter(s -> false);
        assertThat(cond.isEmpty()).isFalse();
        assertThat(filtered.isEmpty()).isTrue();
    }

    @Test
    void testIsLikeCaseInsensitiveFilterUnRenderableShouldReturnSameObject() {
        var cond = SqlBuilder.isLikeCaseInsensitive("Fred").filter(s -> false);
        var filtered = cond.filter(s -> true);
        assertThat(filtered.isEmpty()).isTrue();
        assertThat(cond).isSameAs(filtered);
    }

    @Test
    void testIsLikeCaseInsensitiveMapUnRenderableShouldNotThrowNullPointerException() {
        IsLikeCaseInsensitive<String> cond = SqlBuilder.isLikeCaseInsensitive("Fred").filter(s -> false);
        IsLikeCaseInsensitive<String> mapped = cond.map(String::toUpperCase);
        assertThat(cond.isEmpty()).isTrue();
        assertThatExceptionOfType(NoSuchElementException.class).isThrownBy(cond::value);
        assertThat(cond).isSameAs(mapped);
    }

    @Test
    void testIsNotLikeRenderableTruePredicateShouldReturnSameObject() {
        IsNotLike<String> cond = SqlBuilder.isNotLike("Fred");
        IsNotLike<String> filtered = cond.filter(s -> true);
        assertThat(filtered.isEmpty()).isFalse();
        assertThat(cond).isSameAs(filtered);
    }

    @Test
    void testIsNotLikeRenderableFalsePredicate() {
        IsNotLike<String> cond = SqlBuilder.isNotLike("Fred");
        IsNotLike<String> filtered = cond.filter(s -> false);
        assertThat(cond.isEmpty()).isFalse();
        assertThat(filtered.isEmpty()).isTrue();
    }

    @Test
    void testIsNotLikeFilterUnRenderableShouldReturnSameObject() {
        IsNotLike<String> cond = SqlBuilder.isNotLike("Fred").filter(s -> false);
        IsNotLike<String> filtered = cond.filter(s -> true);
        assertThat(filtered.isEmpty()).isTrue();
        assertThat(cond).isSameAs(filtered);
    }

    @Test
    void testIsNotLikeMapUnRenderableShouldNotThrowNullPointerException() {
        IsNotLike<String> cond = SqlBuilder.isNotLike("Fred").filter(s -> false);
        IsNotLike<String> mapped = cond.map(String::toUpperCase);
        assertThat(cond.isEmpty()).isTrue();
        assertThatExceptionOfType(NoSuchElementException.class).isThrownBy(cond::value);
        assertThat(cond).isSameAs(mapped);
    }

    @Test
    void testIsNotLikeCaseInsensitiveRenderableTruePredicateShouldReturnSameObject() {
        var cond = SqlBuilder.isNotLikeCaseInsensitive("Fred");
        var filtered = cond.filter(s -> true);
        assertThat(filtered.value()).isEqualTo("FRED");
        assertThat(filtered.isEmpty()).isFalse();
        assertThat(cond).isSameAs(filtered);
    }

    @Test
    void testIsNotLikeCaseInsensitiveRenderableFalsePredicate() {
        var cond = SqlBuilder.isNotLikeCaseInsensitive("Fred");
        var filtered = cond.filter(s -> false);
        assertThat(cond.isEmpty()).isFalse();
        assertThat(filtered.isEmpty()).isTrue();
    }

    @Test
    void testIsNotLikeCaseInsensitiveFilterUnRenderableShouldReturnSameObject() {
        var cond = SqlBuilder.isNotLikeCaseInsensitive("Fred").filter(s -> false);
        var filtered = cond.filter(s -> true);
        assertThat(filtered.isEmpty()).isTrue();
        assertThat(cond).isSameAs(filtered);
    }

    @Test
    void testIsNotLikeCaseInsensitiveMapUnRenderableShouldNotThrowNullPointerException() {
        var cond = SqlBuilder.isNotLikeCaseInsensitive("Fred").filter(s -> false);
        var mapped = cond.map(String::toUpperCase);
        assertThat(cond.isEmpty()).isTrue();
        assertThatExceptionOfType(NoSuchElementException.class).isThrownBy(cond::value);
        assertThat(cond).isSameAs(mapped);
    }

    @Test
    void testIsInRenderableMapShouldReturnMappedObject() {
        IsIn<String> cond = SqlBuilder.isIn("Fred", "Wilma");
        assertThat(cond.isEmpty()).isFalse();
        IsIn<String> mapped = cond.map(String::toUpperCase);
        List<String> mappedValues = mapped.values().toList();
        assertThat(mappedValues).containsExactly("FRED", "WILMA");
    }

    @Test
    void testIsNotInRenderableMapShouldReturnMappedObject() {
        IsNotIn<String> cond = SqlBuilder.isNotIn("Fred", "Wilma");
        assertThat(cond.isEmpty()).isFalse();
        IsNotIn<String> mapped = cond.map(String::toUpperCase);
        List<String> mappedValues = mapped.values().toList();
        assertThat(mappedValues).containsExactly("FRED", "WILMA");
    }

    @Test
    void testIsNotInCaseInsensitiveRenderableMapShouldReturnMappedObject() {
        var cond = SqlBuilder.isNotInCaseInsensitive("Fred  ", "Wilma  ");
        var values = cond.values().toList();
        assertThat(values).containsExactly("FRED  ", "WILMA  ");
        assertThat(cond.isEmpty()).isFalse();

        var mapped = cond.map(String::trim);
        var mappedValues = mapped.values().toList();
        assertThat(mappedValues).containsExactly("FRED", "WILMA");
    }

    @Test
    void testIsInCaseInsensitiveRenderableMapShouldReturnMappedObject() {
        var cond = SqlBuilder.isInCaseInsensitive("Fred  ", "Wilma  ");
        var values = cond.values().toList();
        assertThat(values).containsExactly("FRED  ", "WILMA  ");
        assertThat(cond.isEmpty()).isFalse();

        var mapped = cond.map(String::trim);
        var mappedValues = mapped.values().toList();
        assertThat(mappedValues).containsExactly("FRED", "WILMA");
    }

    @Test
    void testBetweenUnRenderableFilterShouldReturnSameObject() {
        IsBetween<Integer> cond = SqlBuilder.isBetween(3).and(4).filter((i1, i2) -> false);
        assertThat(cond.isEmpty()).isTrue();
        IsBetween<Integer> filtered = cond.filter((v1, v2) -> true);
        assertThat(cond).isSameAs(filtered);
    }

    @Test
    void testBetweenUnRenderableFirstNullFilterShouldReturnSameObject() {
        IsBetween<Integer> cond = SqlBuilder.isBetween((Integer) null).and(4).filter(Objects::nonNull);
        assertThat(cond.isEmpty()).isTrue();
        IsBetween<Integer> filtered = cond.filter(v -> true);
        assertThat(cond).isSameAs(filtered);
    }

    @Test
    void testBetweenUnRenderableSecondNullFilterShouldReturnSameObject() {
        IsBetween<Integer> cond = SqlBuilder.isBetween(3).and((Integer) null).filter(Objects::nonNull);
        assertThat(cond.isEmpty()).isTrue();
        IsBetween<Integer> filtered = cond.filter(v -> true);
        assertThat(cond).isSameAs(filtered);
    }

    @Test
    void testBetweenMapWithSingleMapper() {
        IsBetween<Integer> cond = SqlBuilder.isBetween("3").and("4").map(Integer::parseInt);
        assertThat(cond.isEmpty()).isFalse();
        assertThat(cond.value1()).isEqualTo(3);
        assertThat(cond.value2()).isEqualTo(4);
    }

    @Test
    void testNotBetweenUnRenderableFilterShouldReturnSameObject() {
        IsNotBetween<Integer> cond = SqlBuilder.isNotBetween(3).and(4).filter((i1, i2) -> false);
        assertThat(cond.isEmpty()).isTrue();
        IsNotBetween<Integer> filtered = cond.filter((v1, v2) -> true);
        assertThat(cond).isSameAs(filtered);
    }

    @Test
    void testNotBetweenUnRenderableFirstNullFilterShouldReturnSameObject() {
        IsNotBetween<Integer> cond = SqlBuilder.isNotBetween((Integer) null).and(4).filter(Objects::nonNull);
        assertThat(cond.isEmpty()).isTrue();
        IsNotBetween<Integer> filtered = cond.filter(v -> true);
        assertThat(cond).isSameAs(filtered);
    }

    @Test
    void testNotBetweenUnRenderableSecondNullFilterShouldReturnSameObject() {
        IsNotBetween<Integer> cond = SqlBuilder.isNotBetween(3).and((Integer) null).filter(Objects::nonNull);
        assertThat(cond.isEmpty()).isTrue();
        IsNotBetween<Integer> filtered = cond.filter(v -> true);
        assertThat(cond).isSameAs(filtered);
    }

    @Test
    void testNotBetweenMapWithSingleMapper() {
        IsNotBetween<Integer> cond = SqlBuilder.isNotBetween("3").and("4").map(Integer::parseInt);
        assertThat(cond.isEmpty()).isFalse();
        assertThat(cond.value1()).isEqualTo(3);
        assertThat(cond.value2()).isEqualTo(4);
    }

    @Test
    void testMappingAnEmptyListCondition() {
        var cond = SqlBuilder.isNotIn("Fred", "Wilma");
        var filtered = cond.filter(s -> false);
        var mapped = filtered.map(s -> s);
        assertThat(mapped.isEmpty()).isTrue();
        assertThat(filtered).isSameAs(mapped);
    }

    @Test
    void testIsInCaseInsensitiveWhenPresentMapCaseInsensitive() {
        var cond = SqlBuilder.isInCaseInsensitiveWhenPresent("Fred", "Wilma");
        var mapped = cond.map(s -> s + " Flintstone");
        assertThat(mapped.values().toList()).containsExactly("FRED FLINTSTONE", "WILMA FLINTSTONE");
    }

    @Test
    void testIsNotInCaseInsensitiveWhenPresentMapCaseInsensitive() {
        var cond = SqlBuilder.isNotInCaseInsensitiveWhenPresent("Fred", "Wilma");
        var mapped = cond.map(s -> s + " Flintstone");
        assertThat(mapped.values().toList()).containsExactly("FRED FLINTSTONE", "WILMA FLINTSTONE");
    }
}
