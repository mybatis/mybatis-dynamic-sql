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
package examples.emptywhere;

import static examples.emptywhere.OrderDynamicSqlSupport.*;
import static examples.emptywhere.PersonDynamicSqlSupport.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mybatis.dynamic.sql.SqlBuilder.*;

import java.util.*;
import java.util.stream.Stream;

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

    static List<Variation> baseVariations() {
        String firstName = "Fred";
        String lastName = "Flintstone";

        Variation v1 = new Variation(firstName, lastName,
                "where first_name = #{parameters.p1} or last_name = #{parameters.p2}");

        Variation v2 = new Variation(null, lastName,
                "where last_name = #{parameters.p1}");

        Variation v3 = new Variation(firstName, null,
                "where first_name = #{parameters.p1}");

        Variation v4 = new Variation(null, null, "");

        List<Variation> answer = new ArrayList<>();
        answer.add(v1);
        answer.add(v2);
        answer.add(v3);
        answer.add(v4);
        return answer;
    }

    static Stream<Variation> whereVariations() {
        return baseVariations().stream();
    }

    static Stream<Variation> joinWhereVariations() {
        List<Variation> baseVariations = baseVariations();

        baseVariations.get(0).whereClause =
                "where person.first_name = #{parameters.p1} or person.last_name = #{parameters.p2}";
        baseVariations.get(1).whereClause = "where person.last_name = #{parameters.p1}";
        baseVariations.get(2).whereClause = "where person.first_name = #{parameters.p1}";

        return baseVariations.stream();
    }

    static Stream<Variation> updateWhereVariations() {
        List<Variation> baseVariations = baseVariations();

        baseVariations.get(0).whereClause =
                "where first_name = #{parameters.p2} or last_name = #{parameters.p3}";
        baseVariations.get(1).whereClause ="where last_name = #{parameters.p2}";
        baseVariations.get(2).whereClause = "where first_name = #{parameters.p2}";

        return baseVariations.stream();
    }

    @Test
    void testDeleteThreeConditions() {
        String fName = "Fred";
        String lName = "Flintstone";

        DeleteDSL<DeleteModel>.DeleteWhereBuilder builder = deleteFrom(person)
                .where(id, isEqualTo(3));

        builder.and(firstName, isEqualTo(fName).filter(Objects::nonNull));
        builder.and(PersonDynamicSqlSupport.lastName, isEqualTo(lName).filter(Objects::nonNull));

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

        builder.and(firstName, isEqualTo(variation.firstName).filter(Objects::nonNull));
        builder.or(PersonDynamicSqlSupport.lastName, isEqualTo(variation.lastName).filter(Objects::nonNull));
        builder.configureStatement(c -> c.setNonRenderingWhereClauseAllowed(true));

        DeleteStatementProvider deleteStatement = builder.build().render(RenderingStrategies.MYBATIS3);

        String expected = "delete from person " + variation.whereClause;

        assertThat(deleteStatement.getDeleteStatement()).isEqualTo(expected.trim());
    }

    @Test
    void testSelectThreeConditions() {
        String fName = "Fred";
        String lName = "Flintstone";

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder = select(id, firstName, PersonDynamicSqlSupport.lastName)
                .from(person)
                .where(id, isEqualTo(3));

        builder.and(firstName, isEqualTo(fName).filter(Objects::nonNull));
        builder.and(PersonDynamicSqlSupport.lastName, isEqualTo(lName).filter(Objects::nonNull));

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

        builder.and(firstName, isEqualTo(variation.firstName).filter(Objects::nonNull));
        builder.or(PersonDynamicSqlSupport.lastName, isEqualTo(variation.lastName).filter(Objects::nonNull));
        builder.configureStatement(c -> c.setNonRenderingWhereClauseAllowed(true));

        SelectStatementProvider selectStatement = builder.build().render(RenderingStrategies.MYBATIS3);

        String expected = "select * from person " + variation.whereClause;

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected.trim());
    }

    @Test
    void testJoinThreeConditions() {
        String fName = "Fred";
        String lName = "Flintstone";

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder = select(id, firstName, PersonDynamicSqlSupport.lastName, orderDate)
                .from(person).join(order).on(person.id, equalTo(order.personId))
                .where(id, isEqualTo(3));

        builder.and(firstName, isEqualTo(fName).filter(Objects::nonNull));
        builder.and(PersonDynamicSqlSupport.lastName, isEqualTo(lName).filter(Objects::nonNull));

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
                .from(person).join(order).on(person.id, equalTo(order.personId))
                .where();

        builder.and(firstName, isEqualTo(variation.firstName).filter(Objects::nonNull));
        builder.or(PersonDynamicSqlSupport.lastName, isEqualTo(variation.lastName).filter(Objects::nonNull));
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
        String fName = "Fred";
        String lName = "Flintstone";

        UpdateDSL<UpdateModel>.UpdateWhereBuilder builder = update(person)
                .set(id).equalTo(3)
                .where(id, isEqualTo(3));

        builder.and(firstName, isEqualTo(fName).filter(Objects::nonNull));
        builder.and(PersonDynamicSqlSupport.lastName, isEqualTo(lName).filter(Objects::nonNull));

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

        builder.and(firstName, isEqualTo(variation.firstName).filter(Objects::nonNull));
        builder.or(PersonDynamicSqlSupport.lastName, isEqualTo(variation.lastName).filter(Objects::nonNull));
        builder.configureStatement(c -> c.setNonRenderingWhereClauseAllowed(true));

        UpdateStatementProvider updateStatement = builder.build().render(RenderingStrategies.MYBATIS3);

        String expected = "update person"
                + " set id = #{parameters.p1} "
                + variation.whereClause;

        assertThat(updateStatement.getUpdateStatement()).isEqualTo(expected.trim());
    }

    @Test
    void testWhereThreeConditions() {
        String fName = "Fred";
        String lName = "Flintstone";

        WhereDSL builder = where(id, isEqualTo(3));

        builder.and(firstName, isEqualTo(fName).filter(Objects::nonNull));
        builder.and(PersonDynamicSqlSupport.lastName, isEqualTo(lName).filter(Objects::nonNull));

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
        WhereDSL builder = where();

        builder.and(firstName, isEqualTo(variation.firstName).filter(Objects::nonNull));
        builder.or(PersonDynamicSqlSupport.lastName, isEqualTo(variation.lastName).filter(Objects::nonNull));
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

    private static class Variation {
        String firstName;
        String lastName;
        String whereClause;

        public Variation(String firstName, String lastName, String whereClause) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.whereClause = whereClause;
        }
    }
}
