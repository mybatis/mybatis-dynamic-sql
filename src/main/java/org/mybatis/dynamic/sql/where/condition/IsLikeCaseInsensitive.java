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

import java.util.NoSuchElementException;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import org.mybatis.dynamic.sql.AbstractSingleValueCondition;
import org.mybatis.dynamic.sql.util.StringUtilities;

public class IsLikeCaseInsensitive extends AbstractSingleValueCondition<String>
        implements CaseInsensitiveVisitableCondition {
    private static final IsLikeCaseInsensitive EMPTY = new IsLikeCaseInsensitive("") { //$NON-NLS-1$
        @Override
        public String value() {
            throw new NoSuchElementException("No value present"); //$NON-NLS-1$
        }

        @Override
        public boolean isEmpty() {
            return true;
        }
    };

    public static IsLikeCaseInsensitive empty() {
        return EMPTY;
    }

    protected IsLikeCaseInsensitive(String value) {
        super(value);
    }

    @Override
    public String operator() {
        return "like"; //$NON-NLS-1$
    }

    @Override
    public IsLikeCaseInsensitive filter(Predicate<? super String> predicate) {
        return filterSupport(predicate, IsLikeCaseInsensitive::empty, this);
    }

    /**
     * If renderable, apply the mapping to the value and return a new condition with the new value. Else return a
     * condition that will not render (this).
     *
     * @param mapper a mapping function to apply to the value, if renderable
     * @return a new condition with the result of applying the mapper to the value of this condition,
     *     if renderable, otherwise a condition that will not render.
     */
    public IsLikeCaseInsensitive map(UnaryOperator<String> mapper) {
        return mapSupport(mapper, IsLikeCaseInsensitive::new, IsLikeCaseInsensitive::empty);
    }

    public static IsLikeCaseInsensitive of(String value) {
        // Keep the null safe upper case utility for backwards compatibility
        //noinspection DataFlowIssue
        return new IsLikeCaseInsensitive(value).map(StringUtilities::safelyUpperCase);
    }
}
