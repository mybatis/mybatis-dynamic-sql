/*
 *    Copyright 2016-2021 the original author or authors.
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
package org.mybatis.dynamic.sql.where.condition;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.mybatis.dynamic.sql.SqlBuilder;

class FilterTest {

    @Test
    void testIsEqualRenderableTruePredicateShouldReturnSameObject() {
        IsEqualTo<String> cond = SqlBuilder.isEqualTo("Fred");
        IsEqualTo<String> filtered = cond.filter(s -> true);
        assertThat(filtered.shouldRender()).isTrue();
        assertThat(cond).isSameAs(filtered);
    }

    @Test
    void testIsEqualRenderableFalsePredicate() {
        IsEqualTo<String> cond = SqlBuilder.isEqualTo("Fred");
        IsEqualTo<String> filtered = cond.filter(s -> false);
        assertThat(cond.shouldRender()).isTrue();
        assertThat(filtered.shouldRender()).isFalse();
    }

    @Test
    void testIsEqualFilterUnRenderableShouldReturnSameObject() {
        IsEqualTo<String> cond = SqlBuilder.isEqualTo("Fred").filter(s -> false);
        IsEqualTo<String> filtered = cond.filter(s -> true);
        assertThat(filtered.shouldRender()).isFalse();
        assertThat(cond).isSameAs(filtered);
    }

    @Test
    void testIsEqualMapUnRenderableShouldNotThrowNullPointerException() {
        IsEqualTo<String> cond = SqlBuilder.isEqualTo("Fred").filter(s -> false);
        IsEqualTo<String> mapped = cond.map(String::toUpperCase);
        assertThat(cond.shouldRender()).isFalse();
        assertThat(cond.value()).isNull();
        assertThat(cond).isSameAs(mapped);
    }

    @Test
    void testIsNotEqualRenderableTruePredicateShouldReturnSameObject() {
        IsNotEqualTo<String> cond = SqlBuilder.isNotEqualTo("Fred");
        IsNotEqualTo<String> filtered = cond.filter(s -> true);
        assertThat(filtered.shouldRender()).isTrue();
        assertThat(cond).isSameAs(filtered);
    }

    @Test
    void testIsNotEqualRenderableFalsePredicate() {
        IsNotEqualTo<String> cond = SqlBuilder.isNotEqualTo("Fred");
        IsNotEqualTo<String> filtered = cond.filter(s -> false);
        assertThat(cond.shouldRender()).isTrue();
        assertThat(filtered.shouldRender()).isFalse();
    }

    @Test
    void testIsNotEqualFilterUnRenderableShouldReturnSameObject() {
        IsNotEqualTo<String> cond = SqlBuilder.isNotEqualTo("Fred").filter(s -> false);
        IsNotEqualTo<String> filtered = cond.filter(s -> true);
        assertThat(filtered.shouldRender()).isFalse();
        assertThat(cond).isSameAs(filtered);
    }

    @Test
    void testIsNotEqualMapUnRenderableShouldNotThrowNullPointerException() {
        IsNotEqualTo<String> cond = SqlBuilder.isNotEqualTo("Fred").filter(s -> false);
        IsNotEqualTo<String> mapped = cond.map(String::toUpperCase);
        assertThat(cond.shouldRender()).isFalse();
        assertThat(cond.value()).isNull();
        assertThat(cond).isSameAs(mapped);
    }

    @Test
    void testIsLessThanRenderableTruePredicateShouldReturnSameObject() {
        IsLessThan<String> cond = SqlBuilder.isLessThan("Fred");
        IsLessThan<String> filtered = cond.filter(s -> true);
        assertThat(filtered.shouldRender()).isTrue();
        assertThat(cond).isSameAs(filtered);
    }

    @Test
    void testIsLessThanRenderableFalsePredicate() {
        IsLessThan<String> cond = SqlBuilder.isLessThan("Fred");
        IsLessThan<String> filtered = cond.filter(s -> false);
        assertThat(cond.shouldRender()).isTrue();
        assertThat(filtered.shouldRender()).isFalse();
    }

    @Test
    void testIsLessThanFilterUnRenderableShouldReturnSameObject() {
        IsLessThan<String> cond = SqlBuilder.isLessThan("Fred").filter(s -> false);
        IsLessThan<String> filtered = cond.filter(s -> true);
        assertThat(filtered.shouldRender()).isFalse();
        assertThat(cond).isSameAs(filtered);
    }

    @Test
    void testIsLessThanMapUnRenderableShouldNotThrowNullPointerException() {
        IsLessThan<String> cond = SqlBuilder.isLessThan("Fred").filter(s -> false);
        IsLessThan<String> mapped = cond.map(String::toUpperCase);
        assertThat(cond.shouldRender()).isFalse();
        assertThat(cond.value()).isNull();
        assertThat(cond).isSameAs(mapped);
    }

    @Test
    void testIsLessThanOrEqualRenderableTruePredicateShouldReturnSameObject() {
        IsLessThanOrEqualTo<String> cond = SqlBuilder.isLessThanOrEqualTo("Fred");
        IsLessThanOrEqualTo<String> filtered = cond.filter(s -> true);
        assertThat(filtered.shouldRender()).isTrue();
        assertThat(cond).isSameAs(filtered);
    }

    @Test
    void testIsLessThanOrEqualRenderableFalsePredicate() {
        IsLessThanOrEqualTo<String> cond = SqlBuilder.isLessThanOrEqualTo("Fred");
        IsLessThanOrEqualTo<String> filtered = cond.filter(s -> false);
        assertThat(cond.shouldRender()).isTrue();
        assertThat(filtered.shouldRender()).isFalse();
    }

    @Test
    void testIsLessThanOrEqualFilterUnRenderableShouldReturnSameObject() {
        IsLessThanOrEqualTo<String> cond = SqlBuilder.isLessThanOrEqualTo("Fred").filter(s -> false);
        IsLessThanOrEqualTo<String> filtered = cond.filter(s -> true);
        assertThat(filtered.shouldRender()).isFalse();
        assertThat(cond).isSameAs(filtered);
    }

    @Test
    void testIsLessThanOrEqualMapUnRenderableShouldNotThrowNullPointerException() {
        IsLessThanOrEqualTo<String> cond = SqlBuilder.isLessThanOrEqualTo("Fred").filter(s -> false);
        IsLessThanOrEqualTo<String> mapped = cond.map(String::toUpperCase);
        assertThat(cond.shouldRender()).isFalse();
        assertThat(cond.value()).isNull();
        assertThat(cond).isSameAs(mapped);
    }

    @Test
    void testIsGreaterThanRenderableTruePredicateShouldReturnSameObject() {
        IsGreaterThan<String> cond = SqlBuilder.isGreaterThan("Fred");
        IsGreaterThan<String> filtered = cond.filter(s -> true);
        assertThat(filtered.shouldRender()).isTrue();
        assertThat(cond).isSameAs(filtered);
    }

    @Test
    void testIsGreaterThanRenderableFalsePredicate() {
        IsGreaterThan<String> cond = SqlBuilder.isGreaterThan("Fred");
        IsGreaterThan<String> filtered = cond.filter(s -> false);
        assertThat(cond.shouldRender()).isTrue();
        assertThat(filtered.shouldRender()).isFalse();
    }

    @Test
    void testIsGreaterThanFilterUnRenderableShouldReturnSameObject() {
        IsGreaterThan<String> cond = SqlBuilder.isGreaterThan("Fred").filter(s -> false);
        IsGreaterThan<String> filtered = cond.filter(s -> true);
        assertThat(filtered.shouldRender()).isFalse();
        assertThat(cond).isSameAs(filtered);
    }

    @Test
    void testIsGreaterThanMapUnRenderableShouldNotThrowNullPointerException() {
        IsGreaterThan<String> cond = SqlBuilder.isGreaterThan("Fred").filter(s -> false);
        IsGreaterThan<String> mapped = cond.map(String::toUpperCase);
        assertThat(cond.shouldRender()).isFalse();
        assertThat(cond.value()).isNull();
        assertThat(cond).isSameAs(mapped);
    }

    @Test
    void testIsGreaterThanOrEqualRenderableTruePredicateShouldReturnSameObject() {
        IsGreaterThanOrEqualTo<String> cond = SqlBuilder.isGreaterThanOrEqualTo("Fred");
        IsGreaterThanOrEqualTo<String> filtered = cond.filter(s -> true);
        assertThat(filtered.shouldRender()).isTrue();
        assertThat(cond).isSameAs(filtered);
    }

    @Test
    void testIsGreaterThanOrEqualRenderableFalsePredicate() {
        IsGreaterThanOrEqualTo<String> cond = SqlBuilder.isGreaterThanOrEqualTo("Fred");
        IsGreaterThanOrEqualTo<String> filtered = cond.filter(s -> false);
        assertThat(cond.shouldRender()).isTrue();
        assertThat(filtered.shouldRender()).isFalse();
    }

    @Test
    void testIsGreaterThanOrEqualFilterUnRenderableShouldReturnSameObject() {
        IsGreaterThanOrEqualTo<String> cond = SqlBuilder.isGreaterThanOrEqualTo("Fred").filter(s -> false);
        IsGreaterThanOrEqualTo<String> filtered = cond.filter(s -> true);
        assertThat(filtered.shouldRender()).isFalse();
        assertThat(cond).isSameAs(filtered);
    }

    @Test
    void testIsGreaterThanOrEqualMapUnRenderableShouldNotThrowNullPointerException() {
        IsGreaterThanOrEqualTo<String> cond = SqlBuilder.isGreaterThanOrEqualTo("Fred").filter(s -> false);
        IsGreaterThanOrEqualTo<String> mapped = cond.map(String::toUpperCase);
        assertThat(cond.shouldRender()).isFalse();
        assertThat(cond.value()).isNull();
        assertThat(cond).isSameAs(mapped);
    }

    @Test
    void testIsLikeRenderableTruePredicateShouldReturnSameObject() {
        IsLike<String> cond = SqlBuilder.isLike("Fred");
        IsLike<String> filtered = cond.filter(s -> true);
        assertThat(filtered.shouldRender()).isTrue();
        assertThat(cond).isSameAs(filtered);
    }

    @Test
    void testIsLikeRenderableFalsePredicate() {
        IsLike<String> cond = SqlBuilder.isLike("Fred");
        IsLike<String> filtered = cond.filter(s -> false);
        assertThat(cond.shouldRender()).isTrue();
        assertThat(filtered.shouldRender()).isFalse();
    }

    @Test
    void testIsLikeFilterUnRenderableShouldReturnSameObject() {
        IsLike<String> cond = SqlBuilder.isLike("Fred").filter(s -> false);
        IsLike<String> filtered = cond.filter(s -> true);
        assertThat(filtered.shouldRender()).isFalse();
        assertThat(cond).isSameAs(filtered);
    }

    @Test
    void testIsLikeMapUnRenderableShouldNotThrowNullPointerException() {
        IsLike<String> cond = SqlBuilder.isLike("Fred").filter(s -> false);
        IsLike<String> mapped = cond.map(String::toUpperCase);
        assertThat(cond.shouldRender()).isFalse();
        assertThat(cond.value()).isNull();
        assertThat(cond).isSameAs(mapped);
    }

    @Test
    void testIsLikeCaseInsensitiveRenderableTruePredicateShouldReturnSameObject() {
        IsLikeCaseInsensitive cond = SqlBuilder.isLikeCaseInsensitive("Fred");
        IsLikeCaseInsensitive filtered = cond.filter(s -> true);
        assertThat(filtered.value()).isEqualTo("FRED");
        assertThat(filtered.shouldRender()).isTrue();
        assertThat(cond).isSameAs(filtered);
    }

    @Test
    void testIsLikeCaseInsensitiveRenderableFalsePredicate() {
        IsLikeCaseInsensitive cond = SqlBuilder.isLikeCaseInsensitive("Fred");
        IsLikeCaseInsensitive filtered = cond.filter(s -> false);
        assertThat(cond.shouldRender()).isTrue();
        assertThat(filtered.shouldRender()).isFalse();
    }

    @Test
    void testIsLikeCaseInsensitiveFilterUnRenderableShouldReturnSameObject() {
        IsLikeCaseInsensitive cond = SqlBuilder.isLikeCaseInsensitive("Fred").filter(s -> false);
        IsLikeCaseInsensitive filtered = cond.filter(s -> true);
        assertThat(filtered.shouldRender()).isFalse();
        assertThat(cond).isSameAs(filtered);
    }

    @Test
    void testIsLikeCaseInsensitiveMapUnRenderableShouldNotThrowNullPointerException() {
        IsLikeCaseInsensitive cond = SqlBuilder.isLikeCaseInsensitive("Fred").filter(s -> false);
        IsLikeCaseInsensitive mapped = cond.map(String::toUpperCase);
        assertThat(cond.shouldRender()).isFalse();
        assertThat(cond.value()).isNull();
        assertThat(cond).isSameAs(mapped);
    }

    @Test
    void testIsNotLikeRenderableTruePredicateShouldReturnSameObject() {
        IsNotLike<String> cond = SqlBuilder.isNotLike("Fred");
        IsNotLike<String> filtered = cond.filter(s -> true);
        assertThat(filtered.shouldRender()).isTrue();
        assertThat(cond).isSameAs(filtered);
    }

    @Test
    void testIsNotLikeRenderableFalsePredicate() {
        IsNotLike<String> cond = SqlBuilder.isNotLike("Fred");
        IsNotLike<String> filtered = cond.filter(s -> false);
        assertThat(cond.shouldRender()).isTrue();
        assertThat(filtered.shouldRender()).isFalse();
    }

    @Test
    void testIsNotLikeFilterUnRenderableShouldReturnSameObject() {
        IsNotLike<String> cond = SqlBuilder.isNotLike("Fred").filter(s -> false);
        IsNotLike<String> filtered = cond.filter(s -> true);
        assertThat(filtered.shouldRender()).isFalse();
        assertThat(cond).isSameAs(filtered);
    }

    @Test
    void testIsNotLikeMapUnRenderableShouldNotThrowNullPointerException() {
        IsNotLike<String> cond = SqlBuilder.isNotLike("Fred").filter(s -> false);
        IsNotLike<String> mapped = cond.map(String::toUpperCase);
        assertThat(cond.shouldRender()).isFalse();
        assertThat(cond.value()).isNull();
        assertThat(cond).isSameAs(mapped);
    }

    @Test
    void testIsNotLikeCaseInsensitiveRenderableTruePredicateShouldReturnSameObject() {
        IsNotLikeCaseInsensitive cond = SqlBuilder.isNotLikeCaseInsensitive("Fred");
        IsNotLikeCaseInsensitive filtered = cond.filter(s -> true);
        assertThat(filtered.value()).isEqualTo("FRED");
        assertThat(filtered.shouldRender()).isTrue();
        assertThat(cond).isSameAs(filtered);
    }

    @Test
    void testIsNotLikeCaseInsensitiveRenderableFalsePredicate() {
        IsNotLikeCaseInsensitive cond = SqlBuilder.isNotLikeCaseInsensitive("Fred");
        IsNotLikeCaseInsensitive filtered = cond.filter(s -> false);
        assertThat(cond.shouldRender()).isTrue();
        assertThat(filtered.shouldRender()).isFalse();
    }

    @Test
    void testIsNotLikeCaseInsensitiveFilterUnRenderableShouldReturnSameObject() {
        IsNotLikeCaseInsensitive cond = SqlBuilder.isNotLikeCaseInsensitive("Fred").filter(s -> false);
        IsNotLikeCaseInsensitive filtered = cond.filter(s -> true);
        assertThat(filtered.shouldRender()).isFalse();
        assertThat(cond).isSameAs(filtered);
    }

    @Test
    void testIsNotLikeCaseInsensitiveMapUnRenderableShouldNotThrowNullPointerException() {
        IsNotLikeCaseInsensitive cond = SqlBuilder.isNotLikeCaseInsensitive("Fred").filter(s -> false);
        IsNotLikeCaseInsensitive mapped = cond.map(String::toUpperCase);
        assertThat(cond.shouldRender()).isFalse();
        assertThat(cond.value()).isNull();
        assertThat(cond).isSameAs(mapped);
    }
}
