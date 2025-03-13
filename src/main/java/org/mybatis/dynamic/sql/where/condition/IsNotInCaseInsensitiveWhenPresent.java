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
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import org.jspecify.annotations.Nullable;
import org.mybatis.dynamic.sql.AbstractListValueCondition;
import org.mybatis.dynamic.sql.util.StringUtilities;
import org.mybatis.dynamic.sql.util.Utilities;

public class IsNotInCaseInsensitiveWhenPresent extends AbstractListValueCondition<String>
        implements CaseInsensitiveRenderableCondition<String> {
    private static final IsNotInCaseInsensitiveWhenPresent EMPTY =
            new IsNotInCaseInsensitiveWhenPresent(Collections.emptyList());

    public static IsNotInCaseInsensitiveWhenPresent empty() {
        return EMPTY;
    }

    protected IsNotInCaseInsensitiveWhenPresent(Collection<@Nullable String> values) {
        super(Utilities.removeNullElements(values));
    }

    @Override
    public String operator() {
        return "not in"; //$NON-NLS-1$
    }

    @Override
    public IsNotInCaseInsensitiveWhenPresent filter(Predicate<? super String> predicate) {
        return filterSupport(predicate, IsNotInCaseInsensitiveWhenPresent::new,
                this, IsNotInCaseInsensitiveWhenPresent::empty);
    }

    /**
     * If not empty, apply the mapping to each value in the list return a new condition with the mapped values.
     *     Else return an empty condition (this).
     *
     * @param mapper a mapping function to apply to the values, if not empty
     * @return a new condition with mapped values if renderable, otherwise an empty condition
     */
    public IsNotInCaseInsensitiveWhenPresent map(UnaryOperator<String> mapper) {
        return mapSupport(mapper, IsNotInCaseInsensitiveWhenPresent::new, IsNotInCaseInsensitiveWhenPresent::empty);
    }

    public static IsNotInCaseInsensitiveWhenPresent of(@Nullable String... values) {
        return of(Arrays.asList(values));
    }

    public static IsNotInCaseInsensitiveWhenPresent of(Collection<@Nullable String> values) {
        // Keep the null safe upper case utility for backwards compatibility
        //noinspection DataFlowIssue
        return new IsNotInCaseInsensitiveWhenPresent(values).map(StringUtilities::safelyUpperCase);
    }
}
