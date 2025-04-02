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
package org.mybatis.dynamic.sql.insert;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mybatis.dynamic.sql.SqlBuilder.insertBatch;
import static org.mybatis.dynamic.sql.SqlBuilder.insertMultiple;

import java.sql.JDBCType;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.insert.render.BatchInsert;
import org.mybatis.dynamic.sql.insert.render.MultiRowInsertStatementProvider;
import org.mybatis.dynamic.sql.render.RenderingStrategies;

class MapToRowTest {
    private static final SqlTable foo = SqlTable.of("foo");
    private static final SqlColumn<Integer> id1 = foo.column("id1", JDBCType.INTEGER);
    private static final SqlColumn<Integer> id2 = foo.column("id2", JDBCType.INTEGER);

    @Test
    void testBasicInsertMultipleWithMyBatis() {
        List<Record> records = List.of(
                new Record(33, 1),
                new Record(33, 2),
                new Record(33, 3));

        MultiRowInsertStatementProvider<Record> insertStatement = insertMultiple(records)
                .into(foo)
                .map(id1).toConstant("22")
                .map(id2).toProperty("id2")
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = "insert into foo (id1, id2) values (22, #{records[0].id2,jdbcType=INTEGER}), (22, #{records[1].id2,jdbcType=INTEGER}), (22, #{records[2].id2,jdbcType=INTEGER})";
        assertThat(insertStatement.getInsertStatement()).isEqualTo(expected);
    }

    @Test
    void testBasicInsertMultipleWithSpring() {
        List<Record> records = List.of(
                new Record(33, 1),
                new Record(33, 2),
                new Record(33, 3));

        MultiRowInsertStatementProvider<Record> insertStatement = insertMultiple(records)
                .into(foo)
                .map(id1).toConstant("22")
                .map(id2).toProperty("id2")
                .build()
                .render(RenderingStrategies.SPRING_NAMED_PARAMETER);

        String expected = "insert into foo (id1, id2) values (22, :records[0].id2), (22, :records[1].id2), (22, :records[2].id2)";
        assertThat(insertStatement.getInsertStatement()).isEqualTo(expected);
    }

    @Test
    void testBasicInsertMultipleRowMappingWithMyBatis() {
        List<Integer> integers = List.of(1, 2, 3);

        MultiRowInsertStatementProvider<Integer> insertStatement = insertMultiple(integers)
                .into(foo)
                .map(id1).toConstant("22")
                .map(id2).toRow()
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = "insert into foo (id1, id2) values (22, #{records[0],jdbcType=INTEGER}), (22, #{records[1],jdbcType=INTEGER}), (22, #{records[2],jdbcType=INTEGER})";
        assertThat(insertStatement.getInsertStatement()).isEqualTo(expected);
    }

    @Test
    void testBasicInsertMultipleRowMappingWithSpring() {
        List<Integer> integers = List.of(1, 2, 3);

        MultiRowInsertStatementProvider<Integer> insertStatement = insertMultiple(integers)
                .into(foo)
                .map(id1).toConstant("22")
                .map(id2).toRow()
                .build()
                .render(RenderingStrategies.SPRING_NAMED_PARAMETER);

        String expected = "insert into foo (id1, id2) values (22, :records[0]), (22, :records[1]), (22, :records[2])";
        assertThat(insertStatement.getInsertStatement()).isEqualTo(expected);
    }

    @Test
    void testBatchInsertWithMyBatis() {
        List<Record> records = List.of(
                new Record(33, 1),
                new Record(33, 2),
                new Record(33, 3));

        BatchInsert<Record> batchInsert = insertBatch(records)
                .into(foo)
                .map(id1).toConstant("22")
                .map(id2).toProperty("id2")
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = "insert into foo (id1, id2) values (22, #{row.id2,jdbcType=INTEGER})";
        assertThat(batchInsert.getInsertStatementSQL()).isEqualTo(expected);
    }

    @Test
    void testBatchInsertWithSpring() {
        List<Record> records = List.of(
                new Record(33, 1),
                new Record(33, 2),
                new Record(33, 3));

        BatchInsert<Record> batchInsert = insertBatch(records)
                .into(foo)
                .map(id1).toConstant("22")
                .map(id2).toProperty("id2")
                .build()
                .render(RenderingStrategies.SPRING_NAMED_PARAMETER);

        String expected = "insert into foo (id1, id2) values (22, :row.id2)";
        assertThat(batchInsert.getInsertStatementSQL()).isEqualTo(expected);
    }

    @Test
    void testBatchInsertRowMappingWithMyBatis() {
        List<Integer> integers = List.of(1, 2, 3);

        BatchInsert<Integer> batchInsert = insertBatch(integers)
                .into(foo)
                .map(id1).toConstant("22")
                .map(id2).toRow()
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = "insert into foo (id1, id2) values (22, #{row,jdbcType=INTEGER})";
        assertThat(batchInsert.getInsertStatementSQL()).isEqualTo(expected);
    }

    @Test
    void testBatchInsertRowMappingWithSpring() {
        List<Integer> integers = List.of(1, 2, 3);

        BatchInsert<Integer> batchInsert = insertBatch(integers)
                .into(foo)
                .map(id1).toConstant("22")
                .map(id2).toRow()
                .build()
                .render(RenderingStrategies.SPRING_NAMED_PARAMETER);

        String expected = "insert into foo (id1, id2) values (22, :row)";
        assertThat(batchInsert.getInsertStatementSQL()).isEqualTo(expected);
    }

    record Record(Integer id1, Integer id2) { }
}
