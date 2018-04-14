/**
 *    Copyright 2016-2018 the original author or authors.
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

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class IsInWhenPresent<T> extends IsIn<T> {

    private boolean shouldRender;
    
    protected IsInWhenPresent(FilteringBuilder<T> builder) {
        super(builder);
        shouldRender = builder.shouldRender;
    }
    
    @Override
    public boolean shouldRender() {
        return shouldRender;
    }

    public static <T> IsInWhenPresent<T> of(List<T> values) {
        return new IsInWhenPresent.FilteringBuilder<T>().withValues(values).build();
    }
    
    public static class FilteringBuilder<T> extends IsIn.Builder<T> {
        
        private boolean shouldRender;

        @Override
        public FilteringBuilder<T> withValues(List<T> values) {
            if (values != null) {
                List<T> filteredValues = values.stream().filter(Objects::nonNull).collect(Collectors.toList());
                super.withValues(filteredValues);
                shouldRender = !filteredValues.isEmpty();
            }
            return this;
        }

        @Override
        public FilteringBuilder<T> getThis() {
            return this;
        }

        @Override
        public IsInWhenPresent<T> build() {
            return new IsInWhenPresent<>(this);
        }
    }
}
