/**
 *    Copyright 2016-2019 the original author or authors.
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

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.jupiter.api.Test;
import org.mybatis.dynamic.sql.SqlBuilder;
import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;

public class ColumnMappingVisitorTest {

    @Test
    public void testThatUnimplementedMethod1ThrowExceptions() {
        TestTable table = new TestTable();
        TestVisitor tv = new TestVisitor();
        ColumnToColumnMapping mapping = ColumnToColumnMapping.of(table.id, table.description);

        assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> tv.visit(mapping));
    }

    @Test
    public void testThatUnimplementedMethod2ThrowExceptions() {
        TestTable table = new TestTable();
        TestVisitor tv = new TestVisitor();
        ValueMapping<Integer> mapping = ValueMapping.of(table.id,  () -> 3);

        assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> tv.visit(mapping));
    }

    @Test
    public void testThatUnimplementedMethod3ThrowExceptions() {
        TestTable table = new TestTable();
        TestVisitor tv = new TestVisitor();
        SelectMapping mapping = SelectMapping.of(table.id, SqlBuilder.select(table.id).from(table));

        assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> tv.visit(mapping));
    }

    @Test
    public void testThatUnimplementedMethod4ThrowExceptions() {
        TestTable table = new TestTable();
        TestVisitor tv = new TestVisitor();
        PropertyMapping mapping = PropertyMapping.of(table.id, "id");

        assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> tv.visit(mapping));
    }
    
    private static class TestTable extends SqlTable {
        public SqlColumn<Integer> id;
        public SqlColumn<String> description;

        public TestTable() {
            super("Test");

            id = column("id");
            description = column("description");
        }
    }

    private static class TestVisitor implements ColumnMappingVisitor<String> {
        @Override
        public String visit(NullMapping mapping) {
            return "Null Mapping";
        }

        @Override
        public String visit(ConstantMapping mapping) {
            return "Constant Mapping";
        }

        @Override
        public String visit(StringConstantMapping mapping) {
            return "String Constant Mapping";
        }
    }
}
