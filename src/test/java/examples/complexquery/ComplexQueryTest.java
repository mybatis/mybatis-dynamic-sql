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
package examples.complexquery;

import static examples.complexquery.PersonDynamicSqlSupport.firstName;
import static examples.complexquery.PersonDynamicSqlSupport.id;
import static examples.complexquery.PersonDynamicSqlSupport.lastName;
import static examples.complexquery.PersonDynamicSqlSupport.person;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mybatis.dynamic.sql.SqlBuilder.*;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.select.QueryExpressionDSL;
import org.mybatis.dynamic.sql.select.SelectModel;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;

class ComplexQueryTest {

    @Test
    void testId() {
        SelectStatementProvider selectStatement = search(2, null, null);

        String expected = "select person_id, first_name, last_name"
                + " from Person"
                + " where person_id = #{parameters.p1}"
                + " order by last_name, first_name"
                + " fetch first #{parameters.p2} rows only";

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
        assertThat(selectStatement.getParameters()).containsEntry("p1", 2);
        assertThat(selectStatement.getParameters()).containsEntry("p2", 50L);
    }

    @Test
    void testFirstNameOnly() {
        SelectStatementProvider selectStatement = search(null, "fred", null);

        String expected = "select person_id, first_name, last_name"
                + " from Person"
                + " where first_name like #{parameters.p1}"
                + " order by last_name, first_name"
                + " fetch first #{parameters.p2} rows only";

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
        assertThat(selectStatement.getParameters()).containsEntry("p1", "%fred%");
        assertThat(selectStatement.getParameters()).containsEntry("p2", 50L);
    }

    @Test
    void testLastNameOnly() {
        SelectStatementProvider selectStatement = search(null, null, "flintstone");

        String expected = "select person_id, first_name, last_name"
                + " from Person"
                + " where last_name like #{parameters.p1}"
                + " order by last_name, first_name"
                + " fetch first #{parameters.p2} rows only";

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
        assertThat(selectStatement.getParameters()).containsEntry("p1", "%flintstone%");
        assertThat(selectStatement.getParameters()).containsEntry("p2", 50L);
    }

    @Test
    void testBothNames() {
        SelectStatementProvider selectStatement = search(null, "fred", "flintstone");

        String expected = "select person_id, first_name, last_name"
                + " from Person"
                + " where first_name like #{parameters.p1}"
                + " and last_name like #{parameters.p2}"
                + " order by last_name, first_name"
                + " fetch first #{parameters.p3} rows only";

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
        assertThat(selectStatement.getParameters()).containsEntry("p1", "%fred%");
        assertThat(selectStatement.getParameters()).containsEntry("p2", "%flintstone%");
        assertThat(selectStatement.getParameters()).containsEntry("p3", 50L);
    }

    @Test
    void testAllNull() {
        SelectStatementProvider selectStatement = search(null, null, null);

        String expected = "select person_id, first_name, last_name"
                + " from Person"
                + " order by last_name, first_name"
                + " fetch first #{parameters.p1} rows only";

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
        assertThat(selectStatement.getParameters()).containsEntry("p1", 50L);
    }

    SelectStatementProvider search(@Nullable Integer targetId, @Nullable String fName, @Nullable String lName) {
        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder = select(id, firstName, lastName)
                .from(person)
                .where()
                .configureStatement(c -> c.setNonRenderingWhereClauseAllowed(true));

        if (targetId != null) {
            builder.and(id, isEqualTo(targetId));
        } else {
            builder
                .and(firstName, isLikeWhenPresent(fName).map(s -> "%" + s + "%"))
                .and(lastName, isLikeWhenPresent(lName).map(SearchUtils::addWildcards));
        }

        builder
            .orderBy(lastName, firstName)
            .fetchFirst(50).rowsOnly();

        return builder.build().render(RenderingStrategies.MYBATIS3);
    }
}
