/**
 *    Copyright 2016 the original author or authors.
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
package org.mybatis.qbe.sql.where.condition;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.mybatis.qbe.ListValueCondition;

public class IsInCaseInsensitive extends ListValueCondition<String> {

    protected IsInCaseInsensitive(Stream<String> values) {
        super(values);
    }
    
    @Override
    public String render(String fieldName, Stream<String> placeholders) {
        return String.format("upper(%s) %s", fieldName, //$NON-NLS-1$
                placeholders
                .collect(Collectors.joining(",", "in (", ")"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    @Override
    public Stream<String> values() {
        return super.values().map(String::toUpperCase);
    }
    
    public static IsInCaseInsensitive of(Stream<String> values) {
        return new IsInCaseInsensitive(values);
    }
}
