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
package org.mybatis.dynamic.sql.util;

import java.util.Objects;
import java.util.function.Function;

import org.mybatis.dynamic.sql.SqlColumn;

public abstract class AbstractColumnMapping {
    protected SqlColumn<?> column;
    
    protected AbstractColumnMapping(SqlColumn<?> column) {
        this.column = Objects.requireNonNull(column);
    }
    
    public <R> R mapColumn(Function<SqlColumn<?>, R> mapper) {
        return mapper.apply(column);
    }
}
 