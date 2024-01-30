/*
 *    Copyright 2016-2024 the original author or authors.
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mybatis.dynamic.sql.SqlBuilder.insert;
import static org.mybatis.dynamic.sql.SqlBuilder.insertInto;
import static org.mybatis.dynamic.sql.SqlBuilder.select;
import static org.mybatis.dynamic.sql.SqlBuilder.update;
import static org.mybatis.dynamic.sql.SqlBuilder.value;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mybatis.dynamic.sql.common.OrderByModel;
import org.mybatis.dynamic.sql.exception.DynamicSqlException;
import org.mybatis.dynamic.sql.exception.InvalidSqlException;
import org.mybatis.dynamic.sql.insert.BatchInsertModel;
import org.mybatis.dynamic.sql.insert.GeneralInsertModel;
import org.mybatis.dynamic.sql.insert.InsertColumnListModel;
import org.mybatis.dynamic.sql.insert.InsertModel;
import org.mybatis.dynamic.sql.insert.MultiRowInsertModel;
import org.mybatis.dynamic.sql.render.RenderingContext;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.render.TableAliasCalculator;
import org.mybatis.dynamic.sql.select.GroupByModel;
import org.mybatis.dynamic.sql.select.PagingModel;
import org.mybatis.dynamic.sql.select.QueryExpressionModel;
import org.mybatis.dynamic.sql.select.SelectModel;
import org.mybatis.dynamic.sql.select.join.JoinModel;
import org.mybatis.dynamic.sql.select.join.JoinSpecification;
import org.mybatis.dynamic.sql.select.join.JoinType;
import org.mybatis.dynamic.sql.select.render.FetchFirstPagingModelRenderer;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.update.UpdateModel;
import org.mybatis.dynamic.sql.util.InternalError;
import org.mybatis.dynamic.sql.util.Messages;

class InvalidSQLTest {

    private static final SqlTable person = new SqlTable("person");
    private static final SqlColumn<Integer> id = person.column("id");

    @Test
    void testInvalidGeneralInsertStatement() {
        GeneralInsertModel.Builder builder = new GeneralInsertModel.Builder()
                .withTable(person);

        assertThatExceptionOfType(InvalidSqlException.class).isThrownBy(builder::build)
                .withMessage(Messages.getString("ERROR.6"));
    }

    @Test
    void testInvalidGeneralInsertStatementWhenAllOptionalsAreDropped() {
        GeneralInsertModel model = insertInto(person)
                .set(id).toValueWhenPresent((Integer) null)
                .build();

        assertThatExceptionOfType(InvalidSqlException.class)
                .isThrownBy(() -> model.render(RenderingStrategies.SPRING_NAMED_PARAMETER))
                .withMessage(Messages.getString("ERROR.9"));
    }

    @Test
    void testInvalidInsertStatement() {
        InsertModel.Builder<TestRow> builder = new InsertModel.Builder<TestRow>()
                .withTable(person)
                .withRow(new TestRow());

        assertThatExceptionOfType(InvalidSqlException.class).isThrownBy(builder::build)
                .withMessage(Messages.getString("ERROR.7"));
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
                .withMessage(Messages.getString("ERROR.10"));
    }

    @Test
    void testInvalidMultipleInsertStatementNoRecords() {
        MultiRowInsertModel.Builder<TestRow> builder = new MultiRowInsertModel.Builder<TestRow>()
                .withTable(person);

        assertThatExceptionOfType(InvalidSqlException.class).isThrownBy(builder::build)
                .withMessage(Messages.getString("ERROR.20"));
    }

    @Test
    void testInvalidMultipleInsertStatementNoMappings() {
        List<TestRow> records = new ArrayList<>();
        records.add(new TestRow());

        MultiRowInsertModel.Builder<TestRow> builder = new MultiRowInsertModel.Builder<TestRow>()
                .withRecords(records)
                .withTable(person);

        assertThatExceptionOfType(InvalidSqlException.class).isThrownBy(builder::build)
                .withMessage(Messages.getString("ERROR.8"));
    }

    @Test
    void testInvalidBatchInsertStatementNoRecords() {
        BatchInsertModel.Builder<TestRow> builder = new BatchInsertModel.Builder<TestRow>()
                .withTable(person);

        assertThatExceptionOfType(InvalidSqlException.class).isThrownBy(builder::build)
                .withMessage(Messages.getString("ERROR.19"));
    }

    @Test
    void testInvalidBatchInsertStatementNoMappings() {
        List<TestRow> records = new ArrayList<>();
        records.add(new TestRow());

        BatchInsertModel.Builder<TestRow> builder = new BatchInsertModel.Builder<TestRow>()
                .withRecords(records)
                .withTable(person);

        assertThatExceptionOfType(InvalidSqlException.class).isThrownBy(builder::build)
                .withMessage(Messages.getString("ERROR.5"));
    }

    @Test
    void testInvalidEmptyInsertColumnList() {
        List<SqlColumn<?>> list = Collections.emptyList();
        assertThatExceptionOfType(InvalidSqlException.class).isThrownBy(() -> InsertColumnListModel.of(list))
                .withMessage(Messages.getString("ERROR.4"));
    }

    @Test
    void testInvalidNullInsertColumnList() {
        assertThatExceptionOfType(NullPointerException.class).isThrownBy(() -> InsertColumnListModel.of(null));
    }

    @Test
    void testInvalidSelectStatementWithoutQueryExpressions() {
        SelectModel.Builder builder = new SelectModel.Builder();

        assertThatExceptionOfType(InvalidSqlException.class).isThrownBy(builder::build)
                .withMessage(Messages.getString("ERROR.14"));
    }

    @Test
    void testInvalidSelectStatementWithoutColumnList() {
        QueryExpressionModel.Builder builder = new QueryExpressionModel.Builder()
                .withTable(person);

        assertThatExceptionOfType(InvalidSqlException.class).isThrownBy(builder::build)
                .withMessage(Messages.getString("ERROR.13"));
    }

    @Test
    void testInvalidSelectStatementEmptyJoinModel() {
        List<JoinSpecification> list = Collections.emptyList();
        assertThatExceptionOfType(InvalidSqlException.class).isThrownBy(() -> JoinModel.of(list))
                .withMessage(Messages.getString("ERROR.15"));
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
                .withMessage(Messages.getString("ERROR.16"));
    }
    @Test
    void testInvalidSelectStatementWithEmptyOrderByList() {
        List<SortSpecification> list = Collections.emptyList();
        assertThatExceptionOfType(InvalidSqlException.class).isThrownBy(() -> OrderByModel.of(list))
                .withMessage(Messages.getString("ERROR.12"));
    }

    @Test
    void testInvalidSelectStatementWithEmptyGroupByList() {
        List<BasicColumn> list = Collections.emptyList();
        assertThatExceptionOfType(InvalidSqlException.class).isThrownBy(() -> GroupByModel.of(list))
                .withMessage(Messages.getString("ERROR.11"));
    }

    @Test
    void testInvalidUpdateStatement() {
        UpdateModel.Builder builder = new UpdateModel.Builder()
                .withTable(person);

        assertThatExceptionOfType(InvalidSqlException.class).isThrownBy(builder::build)
                .withMessage(Messages.getString("ERROR.17"));
    }

    @Test
    void testInvalidUpdateStatementWhenAllOptionalsAreDropped() {
        UpdateModel model = update(person)
                .set(id).equalToWhenPresent((Integer) null)
                .build();

        assertThatExceptionOfType(InvalidSqlException.class)
                .isThrownBy(() -> model.render(RenderingStrategies.SPRING_NAMED_PARAMETER))
                .withMessage(Messages.getString("ERROR.18"));
    }

    @Test
    void testMissingMessage() {
        assertThatExceptionOfType(MissingResourceException.class)
                .isThrownBy(() -> Messages.getString("MISSING_MESSAGE"));
    }

    @Test
    void testInvalidPagingModel() {
        Optional<PagingModel> pagingModel = new PagingModel.Builder().withLimit(22L).build();

        RenderingContext renderingContext = RenderingContext
                .withRenderingStrategy(RenderingStrategies.MYBATIS3)
                .build();

        assertThat(pagingModel).isPresent();

        FetchFirstPagingModelRenderer renderer = new FetchFirstPagingModelRenderer(renderingContext, pagingModel.get());

        assertThatExceptionOfType(InvalidSqlException.class)
                .isThrownBy(renderer::render)
                .withMessage(Messages.getInternalErrorString(InternalError.INTERNAL_ERROR_13));
    }

    @Test
    void testInvalidValueAlias() {
        BoundValue<Integer> foo = value(1);

        assertThat(foo.alias()).isEmpty();
        assertThatExceptionOfType(InvalidSqlException.class)
                .isThrownBy(() -> foo.as("foo"))
                .withMessage(Messages.getString("ERROR.38"));
    }

    @Test
    void testBadColumn() {
        SelectModel selectModel = select(new BadCount<>()).from(person).build();
        assertThatExceptionOfType(DynamicSqlException.class)
                .isThrownBy(() -> selectModel.render(RenderingStrategies.MYBATIS3))
                .withMessage(Messages.getString("ERROR.36"));
    }

    @Test
    void testDeprecatedColumn() {
        SelectStatementProvider selectStatement = select(new DeprecatedCount<>())
                .from(person)
                .build()
                .render(RenderingStrategies.MYBATIS3);
        assertThat(selectStatement.getSelectStatement()).isEqualTo("select count(*) from person");
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

    static class BadCount<T> implements BindableColumn<T> {
        private String alias;

        @Override
        public Optional<String> alias() {
            return Optional.ofNullable(alias);
        }

        @Override
        public BindableColumn<T> as(String alias) {
            BadCount<T> newCount = new BadCount<>();
            newCount.alias = alias;
            return newCount;
        }
    }

    static class DeprecatedCount<T> implements BindableColumn<T> {
        private String alias;

        @Override
        public String renderWithTableAlias(TableAliasCalculator tableAliasCalculator) {
            return "count(*)";
        }

        @Override
        public Optional<String> alias() {
            return Optional.ofNullable(alias);
        }

        @Override
        public BindableColumn<T> as(String alias) {
            DeprecatedCount<T> newCount = new DeprecatedCount<>();
            newCount.alias = alias;
            return newCount;
        }
    }
}
