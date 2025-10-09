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
import org.mybatis.dynamic.sql.util.Validator;

public class IsIn<T> extends AbstractListValueCondition<T>
        implements AbstractListValueCondition.Filterable<T>, AbstractListValueCondition.Mappable<T> {
    private static final IsIn<?> EMPTY = new IsIn<>(Collections.emptyList());

    public static <T> IsIn<T> empty() {
        @SuppressWarnings("unchecked")
        IsIn<T> t = (IsIn<T>) EMPTY;
        return t;
    }

    protected IsIn(Collection<T> values) {
        super(values);
    }

    @Override
    public boolean shouldRender(RenderingContext renderingContext) {
        Validator.assertNotEmpty(values, "ERROR.44", "IsIn"); //$NON-NLS-1$ //$NON-NLS-2$
        return true;
    }

    @Override
    public String operator() {
        return "in"; //$NON-NLS-1$
    }

    @Override
    public IsIn<T> filter(Predicate<? super T> predicate) {
        return filterSupport(predicate, IsIn::new, this, IsIn::empty);
    }

    @Override
    public <R> IsIn<R> map(Function<? super T, ? extends R> mapper) {
        return mapSupport(mapper, IsIn::new, IsIn::empty);
    }

    @SafeVarargs
    public static <T> IsIn<T> of(T... values) {
        return of(Arrays.asList(values));
    }

    public static <T> IsIn<T> of(Collection<T> values) {
        return new IsIn<>(values);
    }
}
