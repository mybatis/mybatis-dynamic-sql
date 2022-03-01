/*
 *    Copyright 2016-2022 the original author or authors.
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
package org.mybatis.dynamic.sql.render;

import java.util.Objects;
import java.util.Optional;

import org.mybatis.dynamic.sql.SqlTable;

public class TableAliasCalculatorWithParent implements TableAliasCalculator {
    private final TableAliasCalculator parent;
    private final TableAliasCalculator child;

    public TableAliasCalculatorWithParent(TableAliasCalculator parent, TableAliasCalculator child) {
        this.parent = Objects.requireNonNull(parent);
        this.child = Objects.requireNonNull(child);
    }

    @Override
    public Optional<String> aliasForColumn(SqlTable table) {
        Optional<String> answer = child.aliasForColumn(table);
        if (answer.isPresent()) {
            return answer;
        }
        return parent.aliasForColumn(table);
    }

    @Override
    public Optional<String> aliasForTable(SqlTable table) {
        Optional<String> answer = child.aliasForTable(table);
        if (answer.isPresent()) {
            return answer;
        }
        return parent.aliasForTable(table);
    }
}
