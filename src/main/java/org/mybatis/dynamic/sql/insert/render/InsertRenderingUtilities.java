/*
 *    Copyright 2016-2023 the original author or authors.
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
package org.mybatis.dynamic.sql.insert.render;

import static org.mybatis.dynamic.sql.util.StringUtilities.spaceBefore;

import org.mybatis.dynamic.sql.SqlTable;

public class InsertRenderingUtilities {
    private InsertRenderingUtilities() {}

    public static String calculateInsertStatement(SqlTable table, FieldAndValueCollector collector) {
        String statementStart = calculateInsertStatementStart(table);
        String columnsPhrase = collector.columnsPhrase();
        String valuesPhrase = collector.valuesPhrase();

        return statementStart + spaceBefore(columnsPhrase) + spaceBefore(valuesPhrase);
    }

    public static String calculateInsertStatementStart(SqlTable table) {
        return "insert into " + table.tableNameAtRuntime(); //$NON-NLS-1$
    }
}
