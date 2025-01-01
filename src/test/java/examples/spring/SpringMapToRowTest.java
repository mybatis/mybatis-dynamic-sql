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
package examples.spring;

import static examples.spring.CompoundKeyDynamicSqlSupport.compoundKey;
import static examples.spring.CompoundKeyDynamicSqlSupport.id1;
import static examples.spring.CompoundKeyDynamicSqlSupport.id2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mybatis.dynamic.sql.SqlBuilder.insert;
import static org.mybatis.dynamic.sql.SqlBuilder.insertBatch;
import static org.mybatis.dynamic.sql.SqlBuilder.insertMultiple;
import static org.mybatis.dynamic.sql.SqlBuilder.select;

import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.mybatis.dynamic.sql.insert.render.BatchInsert;
import org.mybatis.dynamic.sql.insert.render.InsertStatementProvider;
import org.mybatis.dynamic.sql.insert.render.MultiRowInsertStatementProvider;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.select.SelectModel;
import org.mybatis.dynamic.sql.util.Buildable;
import org.mybatis.dynamic.sql.util.spring.NamedParameterJdbcTemplateExtensions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

@SpringJUnitConfig(classes = SpringConfiguration.class)
@Transactional
class SpringMapToRowTest {
    @Autowired
    private NamedParameterJdbcTemplateExtensions template;

    @Test
    void testInsertOne() {
        Integer i = 1;

        InsertStatementProvider<Integer> insertStatement = insert(i)
                .into(compoundKey)
                .map(id1).toConstant("22")
                .map(id2).toRow()
                .build()
                .render(RenderingStrategies.SPRING_NAMED_PARAMETER);

        String expected = "insert into CompoundKey (id1, id2) values (22, :row)";
        assertThat(insertStatement.getInsertStatement()).isEqualTo(expected);

        int rows = template.insert(insertStatement);
        assertThat(rows).isEqualTo(1);

        Buildable<SelectModel> selectStatement = select(id1, id2)
                .from(compoundKey)
                .orderBy(id1, id2);

        List<CompoundKeyRow> records = template.selectList(selectStatement, rowMapper);
        assertThat(records).hasSize(1);
    }

    @Test
    void testInsertMultiple() {
        List<Integer> integers = List.of(1, 2, 3);

        MultiRowInsertStatementProvider<Integer> insertStatement = insertMultiple(integers)
                .into(compoundKey)
                .map(id1).toConstant("22")
                .map(id2).toRow()
                .build()
                .render(RenderingStrategies.SPRING_NAMED_PARAMETER);

        String expected = "insert into CompoundKey (id1, id2) values (22, :records[0]), (22, :records[1]), (22, :records[2])";
        assertThat(insertStatement.getInsertStatement()).isEqualTo(expected);

        int rows = template.insertMultiple(insertStatement);
        assertThat(rows).isEqualTo(3);

        Buildable<SelectModel> selectStatement = select(id1, id2)
                .from(compoundKey)
                .orderBy(id1, id2);

        List<CompoundKeyRow> records = template.selectList(selectStatement, rowMapper);
        assertThat(records).hasSize(3);
    }

    @Test
    void testInsertBatch() {
        List<Integer> integers = List.of(1, 2, 3);

        BatchInsert<Integer> insertStatement = insertBatch(integers)
                .into(compoundKey)
                .map(id1).toConstant("22")
                .map(id2).toRow()
                .build()
                .render(RenderingStrategies.SPRING_NAMED_PARAMETER);

        String expected = "insert into CompoundKey (id1, id2) values (22, :row)";
        assertThat(insertStatement.getInsertStatementSQL()).isEqualTo(expected);

        int[] rowCounts = template.insertBatch(insertStatement);

        assertThat(IntStream.of(rowCounts).sum()).isEqualTo(3);

        Buildable<SelectModel> selectStatement = select(id1, id2)
                .from(compoundKey)
                .orderBy(id1, id2);

        List<CompoundKeyRow> records = template.selectList(selectStatement, rowMapper);
        assertThat(records).hasSize(3);
    }

    static RowMapper<CompoundKeyRow> rowMapper =
            (rs, i) -> {
                CompoundKeyRow answer = new CompoundKeyRow();
                answer.setId1(rs.getInt("ID1"));
                answer.setId2(rs.getInt("ID2"));
                return answer;
            };
}
