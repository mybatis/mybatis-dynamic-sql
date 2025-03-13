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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;
import java.util.function.Predicate;

import org.mybatis.dynamic.sql.AbstractListValueCondition;
import org.mybatis.dynamic.sql.render.RenderingContext;
import org.mybatis.dynamic.sql.util.StringUtilities;
import org.mybatis.dynamic.sql.util.Validator;

public class IsNotInCaseInsensitive<T> extends AbstractListValueCondition<T>
        implements CaseInsensitiveRenderableCondition<T> {
    private static final IsNotInCaseInsensitive<?> EMPTY = new IsNotInCaseInsensitive<>(Collections.emptyList());

    public static <T> IsNotInCaseInsensitive<T> empty() {
        @SuppressWarnings("unchecked")
        IsNotInCaseInsensitive<T> t = (IsNotInCaseInsensitive<T>) EMPTY;
        return t;
    }

    protected IsNotInCaseInsensitive(Collection<T> values) {
        super(values);
    }

    @Override
    public boolean shouldRender(RenderingContext renderingContext) {
        Validator.assertNotEmpty(values, "ERROR.44", "IsNotInCaseInsensitive"); //$NON-NLS-1$ //$NON-NLS-2$
        return true;
    }

    @Override
    public String operator() {
        return "not in"; //$NON-NLS-1$
    }

    @Override
    public IsNotInCaseInsensitive<T> filter(Predicate<? super T> predicate) {
        return filterSupport(predicate, IsNotInCaseInsensitive::new, this, IsNotInCaseInsensitive::empty);
    }

    @Override
    public <R> IsNotInCaseInsensitive<R> map(Function<? super T, ? extends R> mapper) {
        return mapSupport(mapper, IsNotInCaseInsensitive::new, IsNotInCaseInsensitive::empty);
    }

    public static IsNotInCaseInsensitive<String> of(String... values) {
        return of(Arrays.asList(values));
    }

    public static IsNotInCaseInsensitive<String> of(Collection<String> values) {
        // Keep the null safe upper case utility for backwards compatibility
        //noinspection DataFlowIssue
        return new IsNotInCaseInsensitive<>(values).map(StringUtilities::safelyUpperCase);
    }
}
