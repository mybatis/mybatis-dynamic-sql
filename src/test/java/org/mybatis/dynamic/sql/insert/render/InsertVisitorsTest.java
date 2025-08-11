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
package org.mybatis.dynamic.sql.insert.render;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.jupiter.api.Test;
import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.exception.InvalidSqlException;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.util.MappedColumnMapping;
import org.mybatis.dynamic.sql.util.Messages;

class InsertVisitorsTest {
    @Test
    void testThatMultiRowInsertVisitorErrorsForMappedColumnWhenPropertyIsMissing() {
        TestTable table = new TestTable();
        MultiRowValuePhraseVisitor tv = new MultiRowValuePhraseVisitor(RenderingStrategies.MYBATIS3, "prefix");
        MappedColumnMapping mapping = MappedColumnMapping.of(table.id);

        assertThatExceptionOfType(InvalidSqlException.class).isThrownBy(() -> tv.visit(mapping))
                .withMessage(Messages.getString("ERROR.50", table.id.name()));
    }

    @Test
    void testThatValuePhraseVisitorErrorsForMappedColumnWhenPropertyIsMissing() {
        TestTable table = new TestTable();
        ValuePhraseVisitor tv = new ValuePhraseVisitor(RenderingStrategies.MYBATIS3);
        MappedColumnMapping mapping = MappedColumnMapping.of(table.id);

        assertThatExceptionOfType(InvalidSqlException.class).isThrownBy(() -> tv.visit(mapping))
                .withMessage(Messages.getString("ERROR.50", table.id.name()));
    }

    private static class TestTable extends SqlTable {
        public final SqlColumn<Integer> id;
        public final SqlColumn<String> description;

        public TestTable() {
            super("Test");

            id = column("id");
            description = column("description");
        }
    }
}
