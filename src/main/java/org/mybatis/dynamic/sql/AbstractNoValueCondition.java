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

public abstract class AbstractNoValueCondition <T> extends Condition<T> {

    @Override
    protected FragmentAndParameters render(AtomicInteger sequence, SqlColumn<T> column, String columnName) {
        return new FragmentAndParameters.Builder(renderCondition(columnName)).build();
    }
    
    protected abstract String renderCondition(String columnName);
}
