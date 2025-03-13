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
package examples.mysql;

import java.util.Objects;
import java.util.function.BooleanSupplier;

import org.jspecify.annotations.NullMarked;
import org.mybatis.dynamic.sql.AbstractNoValueCondition;

@NullMarked
public class MemberOfCondition<T> extends AbstractNoValueCondition<T> {
    private static final MemberOfCondition<?> EMPTY = new MemberOfCondition<>("") {
        @Override
        public boolean isEmpty() {
            return true;
        }
    };

    public static <T> MemberOfCondition<T> empty() {
        @SuppressWarnings("unchecked")
        MemberOfCondition<T> t = (MemberOfCondition<T>) EMPTY;
        return t;
    }

    private final String jsonArray;

    protected MemberOfCondition(String jsonArray) {
        this.jsonArray = Objects.requireNonNull(jsonArray);
    }

    @Override
    public String operator() {
        return "member of(" + jsonArray + ")";
    }

    @Override
    public <S> MemberOfCondition<S> filter(BooleanSupplier booleanSupplier) {
        @SuppressWarnings("unchecked")
        MemberOfCondition<S> self = (MemberOfCondition<S>) this;
        return filterSupport(booleanSupplier, MemberOfCondition::empty, self);
    }

    public static <T> MemberOfCondition<T> memberOf(String jsonArray) {
        return new MemberOfCondition<>(jsonArray);
    }
}
