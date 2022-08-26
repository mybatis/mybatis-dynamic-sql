/*
 *    Copyright 2016-2022 the original author or authors.
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

import org.junit.jupiter.api.Test;
import org.mybatis.dynamic.sql.SqlBuilder;

// series of tests to check that the Supplier based DSL methods function correctly

class SupplierTest {
    @Test
    void testIsBetween() {
        IsBetween<Integer> cond = SqlBuilder.isBetween(() -> 3).and(() -> 4);
        assertThat(cond.value1()).isEqualTo(3);
        assertThat(cond.value2()).isEqualTo(4);
        assertThat(cond.shouldRender()).isTrue();
    }

    @Test
    void testIsBetweenNull() {
        IsBetween<Integer> cond = SqlBuilder.isBetween(() -> (Integer) null).and(() -> null);
        assertThat(cond.value1()).isNull();
        assertThat(cond.value2()).isNull();
        assertThat(cond.shouldRender()).isTrue();
    }

    @Test
    void testIsBetweenWhenPresent() {
        IsBetween<Integer> cond = SqlBuilder.isBetweenWhenPresent(() -> 3).and(() -> 4);
        assertThat(cond.value1()).isEqualTo(3);
        assertThat(cond.value2()).isEqualTo(4);
        assertThat(cond.shouldRender()).isTrue();
    }

    @Test
    void testIsBetweenWhenPresentNull() {
        IsBetween<Integer> cond = SqlBuilder.isBetweenWhenPresent(() -> (Integer) null).and(() -> null);
        assertThat(cond.value1()).isNull();
        assertThat(cond.value2()).isNull();
        assertThat(cond.shouldRender()).isFalse();
    }

    @Test
    void testIsNotBetween() {
        IsNotBetween<Integer> cond = SqlBuilder.isNotBetween(() -> 3).and(() -> 4);
        assertThat(cond.value1()).isEqualTo(3);
        assertThat(cond.value2()).isEqualTo(4);
        assertThat(cond.shouldRender()).isTrue();
    }

    @Test
    void testIsNotBetweenNull() {
        IsNotBetween<Integer> cond = SqlBuilder.isNotBetween(() -> (Integer) null).and(() -> null);
        assertThat(cond.value1()).isNull();
        assertThat(cond.value2()).isNull();
        assertThat(cond.shouldRender()).isTrue();
    }

    @Test
    void testIsNotBetweenWhenPresent() {
        IsNotBetween<Integer> cond = SqlBuilder.isNotBetweenWhenPresent(() -> 3).and(() -> 4);
        assertThat(cond.value1()).isEqualTo(3);
        assertThat(cond.value2()).isEqualTo(4);
        assertThat(cond.shouldRender()).isTrue();
    }

    @Test
    void testIsNotBetweenWhenPresentNull() {
        IsNotBetween<Integer> cond = SqlBuilder.isNotBetweenWhenPresent(() -> (Integer) null).and(() -> null);
        assertThat(cond.value1()).isNull();
        assertThat(cond.value2()).isNull();
        assertThat(cond.shouldRender()).isFalse();
    }

    @Test
    void testIsEqualToWhenPresent() {
        IsEqualTo<Integer> cond = SqlBuilder.isEqualToWhenPresent(() -> 3);
        assertThat(cond.value()).isEqualTo(3);
        assertThat(cond.shouldRender()).isTrue();
    }

    @Test
    void testIsEqualToWhenPresentNull() {
        IsEqualTo<Integer> cond = SqlBuilder.isEqualToWhenPresent(() -> null);
        assertThat(cond.value()).isNull();
        assertThat(cond.shouldRender()).isFalse();
    }

    @Test
    void testIsNotEqualTo() {
        IsNotEqualTo<Integer> cond = SqlBuilder.isNotEqualTo(() -> 3);
        assertThat(cond.value()).isEqualTo(3);
        assertThat(cond.shouldRender()).isTrue();
    }

    @Test
    void testIsNotEqualToNull() {
        IsNotEqualTo<Integer> cond = SqlBuilder.isNotEqualTo(() -> (Integer) null);
        assertThat(cond.value()).isNull();
        assertThat(cond.shouldRender()).isTrue();
    }

    @Test
    void testIsNotEqualToWhenPresent() {
        IsNotEqualTo<Integer> cond = SqlBuilder.isNotEqualToWhenPresent(() -> 3);
        assertThat(cond.value()).isEqualTo(3);
        assertThat(cond.shouldRender()).isTrue();
    }

    @Test
    void testIsNotEqualToWhenPresentNull() {
        IsNotEqualTo<Integer> cond = SqlBuilder.isNotEqualToWhenPresent(() -> null);
        assertThat(cond.value()).isNull();
        assertThat(cond.shouldRender()).isFalse();
    }

    @Test
    void testIsGreaterThan() {
        IsGreaterThan<Integer> cond = SqlBuilder.isGreaterThan(() -> 3);
        assertThat(cond.value()).isEqualTo(3);
        assertThat(cond.shouldRender()).isTrue();
    }

    @Test
    void testIsGreaterThanNull() {
        IsGreaterThan<Integer> cond = SqlBuilder.isGreaterThan(() -> (Integer) null);
        assertThat(cond.value()).isNull();
        assertThat(cond.shouldRender()).isTrue();
    }

    @Test
    void testIsGreaterThanWhenPresent() {
        IsGreaterThan<Integer> cond = SqlBuilder.isGreaterThanWhenPresent(() -> 3);
        assertThat(cond.value()).isEqualTo(3);
        assertThat(cond.shouldRender()).isTrue();
    }

    @Test
    void testIsGreaterThanWhenPresentNull() {
        IsGreaterThan<Integer> cond = SqlBuilder.isGreaterThanWhenPresent(() -> null);
        assertThat(cond.value()).isNull();
        assertThat(cond.shouldRender()).isFalse();
    }

    @Test
    void testIsGreaterThanOrEqualTo() {
        IsGreaterThanOrEqualTo<Integer> cond = SqlBuilder.isGreaterThanOrEqualTo(() -> 3);
        assertThat(cond.value()).isEqualTo(3);
        assertThat(cond.shouldRender()).isTrue();
    }

    @Test
    void testIsGreaterThanOrEqualToNull() {
        IsGreaterThanOrEqualTo<Integer> cond = SqlBuilder.isGreaterThanOrEqualTo(() -> (Integer) null);
        assertThat(cond.value()).isNull();
        assertThat(cond.shouldRender()).isTrue();
    }

    @Test
    void testIsGreaterThanOrEqualToWhenPresent() {
        IsGreaterThanOrEqualTo<Integer> cond = SqlBuilder.isGreaterThanOrEqualToWhenPresent(() -> 3);
        assertThat(cond.value()).isEqualTo(3);
        assertThat(cond.shouldRender()).isTrue();
    }

    @Test
    void testIsGreaterThanOrEqualToWhenPresentNull() {
        IsGreaterThanOrEqualTo<Integer> cond = SqlBuilder.isGreaterThanOrEqualToWhenPresent(() -> null);
        assertThat(cond.value()).isNull();
        assertThat(cond.shouldRender()).isFalse();
    }

    @Test
    void testIsLessThan() {
        IsLessThan<Integer> cond = SqlBuilder.isLessThan(() -> 3);
        assertThat(cond.value()).isEqualTo(3);
        assertThat(cond.shouldRender()).isTrue();
    }

    @Test
    void testIsLessThanNull() {
        IsLessThan<Integer> cond = SqlBuilder.isLessThan(() -> (Integer) null);
        assertThat(cond.value()).isNull();
        assertThat(cond.shouldRender()).isTrue();
    }

    @Test
    void testIsLessThanWhenPresent() {
        IsLessThan<Integer> cond = SqlBuilder.isLessThanWhenPresent(() -> 3);
        assertThat(cond.value()).isEqualTo(3);
        assertThat(cond.shouldRender()).isTrue();
    }

    @Test
    void testIsLessThanWhenPresentNull() {
        IsLessThan<Integer> cond = SqlBuilder.isLessThanWhenPresent(() -> null);
        assertThat(cond.value()).isNull();
        assertThat(cond.shouldRender()).isFalse();
    }

    @Test
    void testIsLessThanOrEqualTo() {
        IsLessThanOrEqualTo<Integer> cond = SqlBuilder.isLessThanOrEqualTo(() -> 3);
        assertThat(cond.value()).isEqualTo(3);
        assertThat(cond.shouldRender()).isTrue();
    }

    @Test
    void testIsLessThanOrEqualToNull() {
        IsLessThanOrEqualTo<Integer> cond = SqlBuilder.isLessThanOrEqualTo(() -> (Integer) null);
        assertThat(cond.value()).isNull();
        assertThat(cond.shouldRender()).isTrue();
    }

    @Test
    void testIsLessThanOrEqualToWhenPresent() {
        IsLessThanOrEqualTo<Integer> cond = SqlBuilder.isLessThanOrEqualToWhenPresent(() -> 3);
        assertThat(cond.value()).isEqualTo(3);
        assertThat(cond.shouldRender()).isTrue();
    }

    @Test
    void testIsLessThanOrEqualToWhenPresentNull() {
        IsLessThanOrEqualTo<Integer> cond = SqlBuilder.isLessThanOrEqualToWhenPresent(() -> null);
        assertThat(cond.value()).isNull();
        assertThat(cond.shouldRender()).isFalse();
    }

    @Test
    void testIsLike() {
        IsLike<String> cond = SqlBuilder.isLike(() -> "%F%");
        assertThat(cond.value()).isEqualTo("%F%");
        assertThat(cond.shouldRender()).isTrue();
    }

    @Test
    void testIsLikeNull() {
        IsLike<String> cond = SqlBuilder.isLike(() -> null);
        assertThat(cond.value()).isNull();
        assertThat(cond.shouldRender()).isTrue();
    }

    @Test
    void testIsLikeCaseInsensitive() {
        IsLikeCaseInsensitive cond = SqlBuilder.isLikeCaseInsensitive(() -> "%f%");
        assertThat(cond.value()).isEqualTo("%F%");
        assertThat(cond.shouldRender()).isTrue();
    }

    @Test
    void testIsLikeCaseInsensitiveNull() {
        IsLikeCaseInsensitive cond = SqlBuilder.isLikeCaseInsensitive(() -> null);
        assertThat(cond.value()).isNull();
        assertThat(cond.shouldRender()).isTrue();
    }

    @Test
    void testIsLikeCaseInsensitiveWhenPresent() {
        IsLikeCaseInsensitive cond = SqlBuilder.isLikeCaseInsensitiveWhenPresent(() -> "%f%");
        assertThat(cond.value()).isEqualTo("%F%");
        assertThat(cond.shouldRender()).isTrue();
    }

    @Test
    void testIsLikeCaseInsensitiveWhenPresentNull() {
        IsLikeCaseInsensitive cond = SqlBuilder.isLikeCaseInsensitiveWhenPresent(() -> null);
        assertThat(cond.value()).isNull();
        assertThat(cond.shouldRender()).isFalse();
    }

    @Test
    void testIsLikeWhenPresent() {
        IsLike<String> cond = SqlBuilder.isLikeWhenPresent(() -> "%F%");
        assertThat(cond.value()).isEqualTo("%F%");
        assertThat(cond.shouldRender()).isTrue();
    }

    @Test
    void testIsLikeWhenPresentNull() {
        IsLike<String> cond = SqlBuilder.isLikeWhenPresent(() -> null);
        assertThat(cond.value()).isNull();
        assertThat(cond.shouldRender()).isFalse();
    }

    @Test
    void testIsNotLike() {
        IsNotLike<String> cond = SqlBuilder.isNotLike(() -> "%F%");
        assertThat(cond.value()).isEqualTo("%F%");
        assertThat(cond.shouldRender()).isTrue();
    }

    @Test
    void testIsNotLikeNull() {
        IsNotLike<String> cond = SqlBuilder.isNotLike(() -> null);
        assertThat(cond.value()).isNull();
        assertThat(cond.shouldRender()).isTrue();
    }

    @Test
    void testIsNotLikeWhenPresent() {
        IsNotLike<String> cond = SqlBuilder.isNotLikeWhenPresent(() -> "%F%");
        assertThat(cond.value()).isEqualTo("%F%");
        assertThat(cond.shouldRender()).isTrue();
    }

    @Test
    void testIsNotLikeWhenPresentNull() {
        IsNotLike<String> cond = SqlBuilder.isNotLikeWhenPresent(() -> null);
        assertThat(cond.value()).isNull();
        assertThat(cond.shouldRender()).isFalse();
    }

    @Test
    void testIsNotLikeCaseInsensitive() {
        IsNotLikeCaseInsensitive cond = SqlBuilder.isNotLikeCaseInsensitive(() -> "%f%");
        assertThat(cond.value()).isEqualTo("%F%");
        assertThat(cond.shouldRender()).isTrue();
    }

    @Test
    void testIsNotLikeCaseInsensitiveNull() {
        IsNotLikeCaseInsensitive cond = SqlBuilder.isNotLikeCaseInsensitive(() -> null);
        assertThat(cond.value()).isNull();
        assertThat(cond.shouldRender()).isTrue();
    }

    @Test
    void testIsNotLikeCaseInsensitiveWhenPresent() {
        IsNotLikeCaseInsensitive cond = SqlBuilder.isNotLikeCaseInsensitiveWhenPresent(() -> "%f%");
        assertThat(cond.value()).isEqualTo("%F%");
        assertThat(cond.shouldRender()).isTrue();
    }

    @Test
    void testIsNotLikeCaseInsensitiveWhenPresentNull() {
        IsNotLikeCaseInsensitive cond = SqlBuilder.isNotLikeCaseInsensitiveWhenPresent(() -> null);
        assertThat(cond.value()).isNull();
        assertThat(cond.shouldRender()).isFalse();
    }
}
