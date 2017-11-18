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
package org.mybatis.dynamic.sql.insert;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import org.mybatis.dynamic.sql.SqlColumn;

public class InsertColumnListModel {
    private List<SqlColumn<?>> columns = new ArrayList<>();
    
    private InsertColumnListModel(List<SqlColumn<?>> columns) {
        this.columns.addAll(columns);
    }

    public <R> Stream<R> mapColumns(Function<SqlColumn<?>, R> mapper) {
        return columns.stream().map(mapper);
    }
    
    public static InsertColumnListModel of(List<SqlColumn<?>> columns) {
        return new InsertColumnListModel(columns);
    }
}
