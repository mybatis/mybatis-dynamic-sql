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
package examples.emptywhere;

import static examples.emptywhere.OrderDynamicSqlSupport.*;
import static examples.emptywhere.PersonDynamicSqlSupport.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mybatis.dynamic.sql.SqlBuilder.*;

import java.util.Optional;
import java.util.stream.Stream;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mybatis.dynamic.sql.delete.DeleteDSL;
import org.mybatis.dynamic.sql.delete.DeleteModel;
import org.mybatis.dynamic.sql.delete.render.DeleteStatementProvider;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.select.QueryExpressionDSL;
import org.mybatis.dynamic.sql.select.SelectModel;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.update.UpdateDSL;
import org.mybatis.dynamic.sql.update.UpdateModel;
import org.mybatis.dynamic.sql.update.render.UpdateStatementProvider;
import org.mybatis.dynamic.sql.where.WhereDSL;
import org.mybatis.dynamic.sql.where.render.WhereClauseProvider;

class EmptyWhereTest {
    private static final String FIRST_NAME = "Fred";
    private static final String LAST_NAME = "Flintstone";

    static Stream<Variation> whereVariations() {
        Variation v1 = new Variation(FIRST_NAME, LAST_NAME,
                "where first_name = #{parameters.p1} or last_name = #{parameters.p2}");

        Variation v2 = new Variation(null, LAST_NAME,
                "where last_name = #{parameters.p1}");

        Variation v3 = new Variation(FIRST_NAME, null,
                "where first_name = #{parameters.p1}");

        Variation v4 = new Variation(null, null, "");

        return Stream.of(v1, v2, v3, v4);
    }

    static Stream<Variation> joinWhereVariations() {
        Variation v1 = new Variation(FIRST_NAME, LAST_NAME,
                "where person.first_name = #{parameters.p1} or person.last_name = #{parameters.p2}");

        Variation v2 = new Variation(null, LAST_NAME,
                "where person.last_name = #{parameters.p1}");

        Variation v3 = new Variation(FIRST_NAME, null,
                "where person.first_name = #{parameters.p1}");

        Variation v4 = new Variation(null, null, "");

        return Stream.of(v1, v2, v3, v4);
    }

    static Stream<Variation> updateWhereVariations() {
        Variation v1 = new Variation(FIRST_NAME, LAST_NAME,
                "where first_name = #{parameters.p2} or last_name = #{parameters.p3}");

        Variation v2 = new Variation(null, LAST_NAME,
                "where last_name = #{parameters.p2}");

        Variation v3 = new Variation(FIRST_NAME, null,
                "where first_name = #{parameters.p2}");

        Variation v4 = new Variation(null, null, "");

        return Stream.of(v1, v2, v3, v4);
    }

    @Test
    void testDeleteThreeConditions() {
        DeleteDSL<DeleteModel>.DeleteWhereBuilder builder = deleteFrom(person)
                .where(id, isEqualTo(3));

        builder.and(firstName, isEqualTo(FIRST_NAME));
        builder.and(PersonDynamicSqlSupport.lastName, isEqualTo(LAST_NAME));

        DeleteStatementProvider deleteStatement = builder.build().render(RenderingStrategies.MYBATIS3);

        String expected = "delete from person"
                + " where id = #{parameters.p1}"
                + " and first_name = #{parameters.p2}"
                + " and last_name = #{parameters.p3}";

        assertThat(deleteStatement.getDeleteStatement()).isEqualTo(expected);
    }

    @ParameterizedTest
    @MethodSource("whereVariations")
    void testDeleteVariations(Variation variation) {
        DeleteDSL<DeleteModel>.DeleteWhereBuilder builder = deleteFrom(person)
                .where();

        builder.and(firstName, isEqualToWhenPresent(variation.firstName));
        builder.or(PersonDynamicSqlSupport.lastName, isEqualToWhenPresent(variation.lastName));
        builder.configureStatement(c -> c.setNonRenderingWhereClauseAllowed(true));

        DeleteStatementProvider deleteStatement = builder.build().render(RenderingStrategies.MYBATIS3);

        String expected = "delete from person " + variation.whereClause;

        assertThat(deleteStatement.getDeleteStatement()).isEqualTo(expected.trim());
    }

    @Test
    void testSelectThreeConditions() {
        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder = select(id, firstName, PersonDynamicSqlSupport.lastName)
                .from(person)
                .where(id, isEqualTo(3));

        builder.and(firstName, isEqualTo(FIRST_NAME));
        builder.and(PersonDynamicSqlSupport.lastName, isEqualTo(LAST_NAME));

        SelectStatementProvider selectStatement = builder.build().render(RenderingStrategies.MYBATIS3);

        String expected = "select id, first_name, last_name"
                + " from person"
                + " where id = #{parameters.p1}"
                + " and first_name = #{parameters.p2}"
                + " and last_name = #{parameters.p3}";

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @ParameterizedTest
    @MethodSource("whereVariations")
    void testSelectVariations(Variation variation) {
        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder = select(person.allColumns())
                .from(person)
                .where();

        builder.and(firstName, isEqualToWhenPresent(variation.firstName));
        builder.or(PersonDynamicSqlSupport.lastName, isEqualToWhenPresent(variation.lastName));
        builder.configureStatement(c -> c.setNonRenderingWhereClauseAllowed(true));

        SelectStatementProvider selectStatement = builder.build().render(RenderingStrategies.MYBATIS3);

        String expected = "select * from person " + variation.whereClause;

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected.trim());
    }

    @Test
    void testJoinThreeConditions() {
        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder = select(id, firstName, PersonDynamicSqlSupport.lastName, orderDate)
                .from(person).join(order).on(person.id, isEqualTo(order.personId))
                .where(id, isEqualTo(3));

        builder.and(firstName, isEqualTo(FIRST_NAME));
        builder.and(PersonDynamicSqlSupport.lastName, isEqualTo(LAST_NAME));

        SelectStatementProvider selectStatement = builder.build().render(RenderingStrategies.MYBATIS3);

        String expected = "select person.id, person.first_name, person.last_name, order.order_date"
                + " from person"
                + " join order on person.id = order.person_id"
                + " where person.id = #{parameters.p1}"
                + " and person.first_name = #{parameters.p2}"
                + " and person.last_name = #{parameters.p3}";

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @ParameterizedTest
    @MethodSource("joinWhereVariations")
    void testJoinVariations(Variation variation) {
        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder = select(id, firstName, PersonDynamicSqlSupport.lastName, orderDate)
                .from(person).join(order).on(person.id, isEqualTo(order.personId))
                .where();

        builder.and(firstName, isEqualToWhenPresent(variation.firstName));
        builder.or(PersonDynamicSqlSupport.lastName, isEqualToWhenPresent(variation.lastName));
        builder.configureStatement(c -> c.setNonRenderingWhereClauseAllowed(true));

        SelectStatementProvider selectStatement = builder.build().render(RenderingStrategies.MYBATIS3);

        String expected = "select person.id, person.first_name, person.last_name, order.order_date"
                + " from person"
                + " join order on person.id = order.person_id "
                + variation.whereClause;

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected.trim());
    }

    @Test
    void testUpdateThreeConditions() {
        UpdateDSL<UpdateModel>.UpdateWhereBuilder builder = update(person)
                .set(id).equalTo(3)
                .where(id, isEqualTo(3));

        builder.and(firstName, isEqualTo(FIRST_NAME));
        builder.and(PersonDynamicSqlSupport.lastName, isEqualTo(LAST_NAME));

        UpdateStatementProvider updateStatement = builder.build().render(RenderingStrategies.MYBATIS3);

        String expected = "update person"
                + " set id = #{parameters.p1}"
                + " where id = #{parameters.p2}"
                + " and first_name = #{parameters.p3}"
                + " and last_name = #{parameters.p4}";

        assertThat(updateStatement.getUpdateStatement()).isEqualTo(expected);
    }

    @ParameterizedTest
    @MethodSource("updateWhereVariations")
    void testUpdateVariations(Variation variation) {
        UpdateDSL<UpdateModel>.UpdateWhereBuilder builder = update(person)
                .set(id).equalTo(3)
                .where();

        builder.and(firstName, isEqualToWhenPresent(variation.firstName));
        builder.or(PersonDynamicSqlSupport.lastName, isEqualToWhenPresent(variation.lastName));
        builder.configureStatement(c -> c.setNonRenderingWhereClauseAllowed(true));

        UpdateStatementProvider updateStatement = builder.build().render(RenderingStrategies.MYBATIS3);

        String expected = "update person"
                + " set id = #{parameters.p1} "
                + variation.whereClause;

        assertThat(updateStatement.getUpdateStatement()).isEqualTo(expected.trim());
    }

    @Test
    void testWhereThreeConditions() {
        WhereDSL.StandaloneWhereFinisher builder = where(id, isEqualTo(3));

        builder.and(firstName, isEqualTo(FIRST_NAME));
        builder.and(PersonDynamicSqlSupport.lastName, isEqualTo(LAST_NAME));

        Optional<WhereClauseProvider> whereClause = builder.build().render(RenderingStrategies.MYBATIS3);

        String expected = "where id = #{parameters.p1}"
                + " and first_name = #{parameters.p2}"
                + " and last_name = #{parameters.p3}";

        assertThat(whereClause.map(WhereClauseProvider::getWhereClause)).hasValueSatisfying(wc ->
            assertThat(wc).isEqualTo(expected)
        );
    }

    @ParameterizedTest
    @MethodSource("whereVariations")
    void testWhereVariations(Variation variation) {
        WhereDSL.StandaloneWhereFinisher builder = where();

        builder.and(firstName, isEqualToWhenPresent(variation.firstName));
        builder.or(PersonDynamicSqlSupport.lastName, isEqualToWhenPresent(variation.lastName));
        builder.configureStatement(c -> c.setNonRenderingWhereClauseAllowed(true));

        Optional<WhereClauseProvider> whereClause = builder.build().render(RenderingStrategies.MYBATIS3);

        if (variation.firstName == null && variation.lastName == null) {
            assertThat(whereClause).isEmpty();
        } else {
            assertThat(whereClause.map(WhereClauseProvider::getWhereClause)).hasValueSatisfying(wc ->
                    assertThat(wc).isEqualTo(variation.whereClause)
            );
        }
    }

    private record Variation (@Nullable String firstName, @Nullable String lastName, String whereClause) {}
}
