/*
 *    Copyright 2016-2022 the original author or authors.
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
package org.mybatis.dynamic.sql;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mybatis.dynamic.sql.SqlBuilder.insert;
import static org.mybatis.dynamic.sql.SqlBuilder.insertInto;
import static org.mybatis.dynamic.sql.SqlBuilder.update;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mybatis.dynamic.sql.exception.InvalidSqlException;
import org.mybatis.dynamic.sql.insert.BatchInsertModel;
import org.mybatis.dynamic.sql.insert.GeneralInsertModel;
import org.mybatis.dynamic.sql.insert.InsertColumnListModel;
import org.mybatis.dynamic.sql.insert.InsertModel;
import org.mybatis.dynamic.sql.insert.MultiRowInsertModel;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.select.GroupByModel;
import org.mybatis.dynamic.sql.select.OrderByModel;
import org.mybatis.dynamic.sql.select.QueryExpressionModel;
import org.mybatis.dynamic.sql.select.SelectModel;
import org.mybatis.dynamic.sql.select.join.JoinModel;
import org.mybatis.dynamic.sql.select.join.JoinSpecification;
import org.mybatis.dynamic.sql.select.join.JoinType;
import org.mybatis.dynamic.sql.update.UpdateModel;

class InvalidSQLTest {

    private static final SqlTable person = new SqlTable("person");
    private static final SqlColumn<Integer> id = person.column("id");

    @Test
    void testInvalidGeneralInsertStatement() {
        GeneralInsertModel.Builder builder = new GeneralInsertModel.Builder()
                .withTable(person);

        assertThatExceptionOfType(InvalidSqlException.class).isThrownBy(builder::build)
                .withMessage("General insert statements must have at least one column mapping");
    }

    @Test
    void testInvalidGeneralInsertStatementWhenAllOptionalsAreDropped() {
        GeneralInsertModel model = insertInto(person)
                .set(id).toValueWhenPresent((Integer) null)
                .build();

        assertThatExceptionOfType(InvalidSqlException.class)
                .isThrownBy(() -> model.render(RenderingStrategies.SPRING_NAMED_PARAMETER))
                .withMessage("All optional set phrases were dropped when rendering the general insert statement");
    }

    @Test
    void testInvalidInsertStatement() {
        InsertModel.Builder<String> builder = new InsertModel.Builder<String>()
                .withTable(person)
                .withRow("fred");

        assertThatExceptionOfType(InvalidSqlException.class).isThrownBy(builder::build)
                .withMessage("Insert statements must have at least one column mapping");
    }

    @Test
    void testInvalidInsertStatementWhenAllOptionalsAreDropped() {
        TestRow testRow = new TestRow();

        InsertModel<TestRow> model = insert(testRow)
                .into(person)
                .map(id).toPropertyWhenPresent("id", testRow::getId)
                .build();

        assertThatExceptionOfType(InvalidSqlException.class)
                .isThrownBy(() -> model.render(RenderingStrategies.SPRING_NAMED_PARAMETER))
                .withMessage("All optional column mappings were dropped when rendering the insert statement");
    }

    @Test
    void testInvalidMultipleInsertStatementNoRecords() {
        MultiRowInsertModel.Builder<String> builder = new MultiRowInsertModel.Builder<String>()
                .withTable(person);

        assertThatExceptionOfType(InvalidSqlException.class).isThrownBy(builder::build)
                .withMessage("Multi row insert statements must have at least one column mapping");
    }

    @Test
    void testInvalidMultipleInsertStatementNoMappings() {
        List<String> records = new ArrayList<>();
        records.add("fred");

        MultiRowInsertModel.Builder<String> builder = new MultiRowInsertModel.Builder<String>()
                .withRecords(records)
                .withTable(person);

        assertThatExceptionOfType(InvalidSqlException.class).isThrownBy(builder::build)
                .withMessage("Multi row insert statements must have at least one column mapping");
    }

    @Test
    void testInvalidBatchInsertStatementNoRecords() {
        BatchInsertModel.Builder<String> builder = new BatchInsertModel.Builder<String>()
                .withTable(person);

        assertThatExceptionOfType(InvalidSqlException.class).isThrownBy(builder::build)
                .withMessage("Batch insert statements must have at least one column mapping");
    }

    @Test
    void testInvalidBatchInsertStatementNoMappings() {
        List<String> records = new ArrayList<>();
        records.add("fred");

        BatchInsertModel.Builder<String> builder = new BatchInsertModel.Builder<String>()
                .withRecords(records)
                .withTable(person);

        assertThatExceptionOfType(InvalidSqlException.class).isThrownBy(builder::build)
                .withMessage("Batch insert statements must have at least one column mapping");
    }

    @Test
    void testInvalidEmptyInsertColumnList() {
        List<SqlColumn<?>> list = Collections.emptyList();
        assertThatExceptionOfType(InvalidSqlException.class).isThrownBy(() -> InsertColumnListModel.of(list))
                .withMessage("Insert select statements require at least one column in the column list");
    }

    @Test
    void testInvalidNullInsertColumnList() {
        assertThatExceptionOfType(NullPointerException.class).isThrownBy(() -> InsertColumnListModel.of(null));
    }

    @Test
    void testInvalidSelectStatementWithoutQueryExpressions() {
        SelectModel.Builder builder = new SelectModel.Builder();

        assertThatExceptionOfType(InvalidSqlException.class).isThrownBy(builder::build)
                .withMessage("Select statements must have at least one query expression");
    }

    @Test
    void testInvalidSelectStatementWithoutColumnList() {
        QueryExpressionModel.Builder builder = new QueryExpressionModel.Builder()
                .withTable(person);

        assertThatExceptionOfType(InvalidSqlException.class).isThrownBy(builder::build)
                .withMessage("Query expressions must have at least one column in the select list");
    }

    @Test
    void testInvalidSelectStatementEmptyJoinModel() {
        List<JoinSpecification> list = Collections.emptyList();
        assertThatExceptionOfType(InvalidSqlException.class).isThrownBy(() -> JoinModel.of(list))
                .withMessage("Joins must have at least one join specification");
    }

    @Test
    void testInvalidSelectStatementNullJoinModel() {
        assertThatExceptionOfType(NullPointerException.class).isThrownBy(() -> JoinModel.of(null));
    }

    @Test
    void testInvalidSelectStatementJoinSpecification() {
        JoinSpecification.Builder builder = new JoinSpecification.Builder()
                .withJoinTable(person)
                .withJoinType(JoinType.LEFT);

        assertThatExceptionOfType(InvalidSqlException.class).isThrownBy(builder::build)
                .withMessage("Join specifications must have at least one join criterion");
    }
    @Test
    void testInvalidSelectStatementWithEmptyOrderByList() {
        List<SortSpecification> list = Collections.emptyList();
        assertThatExceptionOfType(InvalidSqlException.class).isThrownBy(() -> OrderByModel.of(list))
                .withMessage("Order by expressions must have at least one column");
    }

    @Test
    void testInvalidSelectStatementWithEmptyGroupByList() {
        List<BasicColumn> list = Collections.emptyList();
        assertThatExceptionOfType(InvalidSqlException.class).isThrownBy(() -> GroupByModel.of(list))
                .withMessage("Group by expressions must have at least one column");
    }

    @Test
    void testInvalidUpdateStatement() {
        UpdateModel.Builder builder = new UpdateModel.Builder()
                .withTable(person);

        assertThatExceptionOfType(InvalidSqlException.class).isThrownBy(builder::build)
                .withMessage("Update statements must have at least one set phrase");
    }

    @Test
    void testInvalidUpdateStatementWhenAllOptionalsAreDropped() {
        UpdateModel model = update(person)
                .set(id).equalToWhenPresent((Integer) null)
                .build();

        assertThatExceptionOfType(InvalidSqlException.class)
                .isThrownBy(() -> model.render(RenderingStrategies.SPRING_NAMED_PARAMETER))
                .withMessage("All optional set phrases were dropped when rendering the update statement");
    }

    static class TestRow {
        private Integer id;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }
    }
}
