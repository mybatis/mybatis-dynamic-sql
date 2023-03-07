package org.mybatis.dynamic.sql.insert;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mybatis.dynamic.sql.SqlBuilder.insert;
import static org.mybatis.dynamic.sql.SqlBuilder.insertMultiple;

import java.sql.JDBCType;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.insert.render.InsertStatementProvider;
import org.mybatis.dynamic.sql.insert.render.MultiRowInsertStatementProvider;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
class MapToRowTest {
    private static final SqlTable foo = SqlTable.of("foo");
    private static final SqlColumn<Integer> id1 = foo.column("id1", JDBCType.INTEGER);
    private static final SqlColumn<Integer> id2 = foo.column("id2", JDBCType.INTEGER);

    @Test
    void testBasicInsertMultipleWithMyBatis() {
        class Record {
            public Record() {}

            public Record(Integer id1, Integer id2) {
                this.id1 = id1;
                this.id2 = id2;
            }

            public Integer id1;
            public Integer id2;
        }

        List<Record> records = new ArrayList<>();
        records.add(new Record(33, 1));
        records.add(new Record(33, 2));
        records.add(new Record(33, 3));


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
    void testBasicInsertMultipleRowMappingWithMyBatis() {
        List<Integer> integers = new ArrayList<>();
        integers.add(1);
        integers.add(2);
        integers.add(3);

        MultiRowInsertStatementProvider<Integer> insertStatement = insertMultiple(integers)
                .into(foo)
                .map(id1).toConstant("22")
                .map(id2).toRow()
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = "insert into foo (id1, id2) values (22, #{records[0],jdbcType=INTEGER}), (22, #{records[1],jdbcType=INTEGER}), (22, #{records[2],jdbcType=INTEGER})";
        assertThat(insertStatement.getInsertStatement()).isEqualTo(expected);
    }
}
