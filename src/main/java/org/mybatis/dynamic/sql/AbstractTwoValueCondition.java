/**
 *    Copyright 2016-2017 the original author or authors.
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
package org.mybatis.dynamic.sql;

import java.util.concurrent.atomic.AtomicInteger;

import org.mybatis.dynamic.sql.util.FragmentAndParameters;

public abstract class AbstractTwoValueCondition<T> extends Condition<T> {
    private T value1;
    private T value2;
    
    protected AbstractTwoValueCondition(T value1, T value2) {
        this.value1 = value1;
        this.value2 = value2;
    }

    protected T value1() {
        return value1;
    }

    protected T value2() {
        return value2;
    }

    @Override
    protected FragmentAndParameters render(AtomicInteger sequence, SqlColumn<T> column, String columnName) {
        String mapKey1 = formatParameterMapKey(sequence.getAndIncrement());
        String mapKey2 = formatParameterMapKey(sequence.getAndIncrement());
        String fragment = renderCondition(columnName,
                column.getFormattedJdbcPlaceholder(PARAMETERS_PREFIX, mapKey1),
                column.getFormattedJdbcPlaceholder(PARAMETERS_PREFIX, mapKey2));
                
        return new FragmentAndParameters.Builder(fragment)
                .withParameter(mapKey1, value1())
                .withParameter(mapKey2, value2())
                .build();
    }

    protected abstract String renderCondition(String columnName, String placeholder1, String placeholder2);
}
