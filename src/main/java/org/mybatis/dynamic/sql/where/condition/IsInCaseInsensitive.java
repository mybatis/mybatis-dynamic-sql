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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.mybatis.dynamic.sql.AbstractListValueCondition;

public class IsInCaseInsensitive extends AbstractListValueCondition<String> {

    protected IsInCaseInsensitive(Builder builder) {
        super(builder);
    }
    
    @Override
    public String renderCondition(String columnName, Stream<String> placeholders) {
        return "upper(" + columnName + ") " + //$NON-NLS-1$ //$NON-NLS-2$
                placeholders.collect(Collectors.joining(",", "in (", ")")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    @Override
    public String mapValue(String value) {
        return value.toUpperCase();
    }
    
    public static IsInCaseInsensitive of(List<String> values) {
        return new IsInCaseInsensitive.Builder().withValues(values).build();
    }
    
    public static class Builder extends AbstractBuilder<String, Builder> {

        @Override
        public Builder getThis() {
            return this;
        }
        
        public IsInCaseInsensitive build() {
            return new IsInCaseInsensitive(this);
        }
    }
}
