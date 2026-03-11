/*
 *    Copyright 2016-2026 the original author or authors.
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
package org.mybatis.dynamic.sql.where.render;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mybatis.dynamic.sql.SqlBuilder.*;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.dsl.WhereDSL;
import org.mybatis.dynamic.sql.where.WhereApplier;

class OptionalCriterionRenderTest {
    private static final SqlTable person = SqlTable.of("person");
    private static final SqlColumn<Integer> id = person.column("id");
    private static final SqlColumn<String> firstName = person.column("first_name");
    private static final SqlColumn<String> lastName = person.column("last_name");

    @ParameterizedTest
    @MethodSource("testVariations")
    void testVariations(Variation variation) {
        SelectStatementProvider selectStatement = select(person.allColumns())
                .from(person)
                .applyWhere(variation.whereApplier)
                .configureStatement(c -> c.setNonRenderingWhereClauseAllowed(true))
                .build()
                .render(RenderingStrategies.SPRING_NAMED_PARAMETER);

        String expected = "select * from person " + variation.expected;
        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected.trim());
    }

    private record Variation(WhereApplier whereApplier, String expected) {}

    static Stream<Arguments> testVariations() {
        return Stream.of(
                testNoRenderableCriteria(),
                testDisabledIsNull(),
                testEnabledIsNull(),
                testDisabledIsNotNull(),
                testEnabledIsNotNull(),
                testOneRenderableCriteriaBeforeNull(),
                testOneRenderableCriteriaBeforeNull2(),
                testOneRenderableCriteriaAfterNull(),
                testOneRenderableCriteriaAfterNull2(),
                testOverrideFirstConnector(),
                testWhereExists(),
                testWhereExistsOr(),
                testWhereExistsOrOr(),
                testWhereExistsAnd(),
                testWhereExistsAndAnd(),
                testCollapsingCriteriaGroup1(),
                testCollapsingCriteriaGroup2(),
                testCollapsingCriteriaGroup3(),
                testCollapsingCriteriaGroup4()
        );
    }

    static Arguments testNoRenderableCriteria() {
        WhereApplier whereClause = where(id, isEqualToWhenPresent((Integer) null))
                .toWhereApplier();
        return Arguments.argumentSet("No Renderable Criteria", new Variation(whereClause, ""));
    }

    static Arguments testDisabledIsNull() {
        WhereApplier whereClause = where(id, isNull().filter(() -> false))
                .toWhereApplier();
        return Arguments.argumentSet("Disabled Is Null", new Variation(whereClause, ""));
    }

    static Arguments testEnabledIsNull() {
        WhereApplier whereClause = where(id, isNull().filter(() -> true))
                .toWhereApplier();
        return Arguments.argumentSet("Enabled Is Null", new Variation(whereClause, "where id is null"));
    }

    static Arguments testDisabledIsNotNull() {
        WhereApplier whereClause = where(id, isNotNull().filter(() -> false))
                .toWhereApplier();
        return Arguments.argumentSet("Disabled Is Not Null", new Variation(whereClause, ""));
    }

    static Arguments testEnabledIsNotNull() {
        WhereApplier whereClause = where(id, isNotNull().filter(() -> true))
                .toWhereApplier();
        return Arguments.argumentSet("Enabled Is Not Null", new Variation(whereClause, "where id is not null"));
    }

    static Arguments testOneRenderableCriteriaBeforeNull() {
        WhereApplier whereClause = where(id, isEqualToWhenPresent(22))
                .and(firstName, isEqualToWhenPresent((String) null))
                .toWhereApplier();
        return Arguments.argumentSet("One Renderable Criteria Before Null", new Variation(whereClause, "where id = :p1"));
    }

    static Arguments testOneRenderableCriteriaBeforeNull2() {
        WhereApplier whereClause = where(id, isEqualToWhenPresent(22), and(firstName, isEqualToWhenPresent((String) null)))
                .toWhereApplier();
        return Arguments.argumentSet("One Renderable Criteria Before Null 2", new Variation(whereClause, "where id = :p1"));
    }

    static Arguments testOneRenderableCriteriaAfterNull() {
        WhereApplier whereClause = where(id, isEqualToWhenPresent((Integer) null))
                .and(firstName, isEqualToWhenPresent("Fred"))
                .toWhereApplier();
        return Arguments.argumentSet("One Renderable Criteria After Null", new Variation(whereClause, "where first_name = :p1"));
    }

    static Arguments testOneRenderableCriteriaAfterNull2() {
        WhereApplier whereClause = where(id, isEqualToWhenPresent((Integer) null), and(firstName, isEqualToWhenPresent("Fred")))
                .toWhereApplier();
        return Arguments.argumentSet("One Renderable Criteria After Null 2", new Variation(whereClause, "where first_name = :p1"));
    }

    static Arguments testOverrideFirstConnector() {
        WhereApplier whereClause = where(id, isEqualToWhenPresent((Integer) null), and(firstName, isEqualToWhenPresent("Fred")), or(lastName, isEqualTo("flintstone")))
                .toWhereApplier();
        return Arguments.argumentSet("Override First Connector", new Variation(whereClause, "where first_name = :p1 or last_name = :p2"));
    }

    static Arguments testWhereExists() {
        WhereApplier whereClause = where(
                exists(
                        select(person.allColumns())
                        .from(person)
                        .where(id, isEqualTo(3))
                ))
                .toWhereApplier();
        return Arguments.argumentSet("Where Exists", new Variation(whereClause, "where exists (select * from person where id = :p1)"));
    }

    static Arguments testWhereExistsOr() {
        WhereApplier whereClause = where(
                exists(
                        select(person.allColumns())
                        .from(person)
                        .where(id, isEqualTo(3))
                ),
                or(exists(
                        select(person.allColumns())
                        .from(person)
                        .where(id, isEqualTo(4))
                )))
                .toWhereApplier();

        String expected = "where exists (select * from person where id = :p1) " +
                "or exists (select * from person where id = :p2)";
        return Arguments.argumentSet("Where Exists Or", new Variation(whereClause, expected));
    }

    static Arguments testWhereExistsOrOr() {
        WhereApplier whereClause = where(
                exists(
                        select(person.allColumns())
                                .from(person)
                                .where(id, isEqualTo(3))
                ),
                or(exists(
                        select(person.allColumns())
                                .from(person)
                                .where(id, isEqualTo(4))
                ), or(exists(
                        select(person.allColumns())
                                .from(person)
                                .where(id, isEqualTo(5))

                        )
                )))
                .toWhereApplier();

        String expected = "where exists (select * from person where id = :p1) " +
                "or (exists (select * from person where id = :p2) " +
                "or exists (select * from person where id = :p3))";

        return Arguments.argumentSet("Where Exists Or Or", new Variation(whereClause, expected));
    }

    static Arguments testWhereExistsAnd() {
        WhereApplier whereClause = where(
                exists(
                        select(person.allColumns())
                                .from(person)
                                .where(id, isEqualTo(3))
                ),
                and(exists(
                        select(person.allColumns())
                                .from(person)
                                .where(id, isEqualTo(4))
                )))
                .toWhereApplier();

        String expected = "where exists (select * from person where id = :p1) " +
                "and exists (select * from person where id = :p2)";

        return Arguments.argumentSet("Where Exists And", new Variation(whereClause, expected));
    }

    static Arguments testWhereExistsAndAnd() {
        WhereApplier whereClause = where(
                exists(
                        select(person.allColumns())
                                .from(person)
                                .where(id, isEqualTo(3))
                ),
                and(exists(
                        select(person.allColumns())
                                .from(person)
                                .where(id, isEqualTo(4))
                ), and(exists(
                        select(person.allColumns())
                                .from(person)
                                .where(id, isEqualTo(5))

                        )
                )))
                .toWhereApplier();

        String expected = "where exists (select * from person where id = :p1) " +
                "and (exists (select * from person where id = :p2) " +
                "and exists (select * from person where id = :p3))";

        return Arguments.argumentSet("Where Exists And And", new Variation(whereClause, expected));
    }

    static Arguments testCollapsingCriteriaGroup1() {
        WhereApplier whereClause = where(
                group(firstName, isEqualToWhenPresent((String) null)), or(lastName, isEqualToWhenPresent((String) null)))
                .toWhereApplier();
        return Arguments.argumentSet("Collapsing Criteria Group", new Variation(whereClause, ""));
    }

    static Arguments testCollapsingCriteriaGroup2() {
        WhereApplier whereClause = where(
                group(firstName, isEqualTo("Fred")), or(lastName, isEqualToWhenPresent((String) null)))
                .toWhereApplier();

        String expected = "where first_name = :p1";

        return Arguments.argumentSet("Collapsing Criteria Group 2", new Variation(whereClause, expected));
    }

    static Arguments testCollapsingCriteriaGroup3() {
        WhereApplier whereClause = where(
                group(firstName, isEqualTo("Fred")), or(lastName, isEqualToWhenPresent((String) null)), or(firstName, isEqualTo("Betty")))
                .toWhereApplier();

        String expected = "where first_name = :p1 or first_name = :p2";

        return Arguments.argumentSet("Collapsing Criteria Group 3", new Variation(whereClause, expected));
    }

    static Arguments testCollapsingCriteriaGroup4() {
        WhereDSL whereBuilder = where();

        WhereApplier whereClause = whereBuilder.and(group(firstName, isEqualTo("Fred")), or(lastName, isEqualToWhenPresent((String) null)), or(firstName, isEqualTo("Betty")))
                .toWhereApplier();

        String expected = "where first_name = :p1 or first_name = :p2";

        return Arguments.argumentSet("Collapsing Criteria Group 4", new Variation(whereClause, expected));
    }
}
