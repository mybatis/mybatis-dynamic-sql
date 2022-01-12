/*
 *    Copyright 2016-2022 the original author or authors.
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
package org.mybatis.dynamic.sql.where.render;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mybatis.dynamic.sql.SqlBuilder.*;

import java.util.Objects;

import org.junit.jupiter.api.Test;
import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.render.RenderingStrategies;

class OptionalCriterionRenderTest {
    private static SqlTable person = SqlTable.of("person");
    private static SqlColumn<Integer> id = person.column("id");
    private static SqlColumn<String> firstName = person.column("first_name");
    private static SqlColumn<String> lastName = person.column("last_name");

    @Test
    void testNoRenderableCriteria() {
        Integer nullId = null;

        WhereClauseProvider whereClause = where(id, isEqualToWhenPresent(nullId))
                .build()
                .render(RenderingStrategies.SPRING_NAMED_PARAMETER);

        assertAll(
                () -> assertThat(whereClause.getWhereClause()).isEmpty(),
                () -> assertThat(whereClause.getParameters()).isEmpty()
        );
    }

    @Test
    void testNoRenderableCriteriaWithIf() {
        Integer nullId = null;

        WhereClauseProvider whereClause = where(id, isEqualTo(nullId).when(Objects::nonNull))
                .build()
                .render(RenderingStrategies.SPRING_NAMED_PARAMETER);

        assertAll(
                () -> assertThat(whereClause.getWhereClause()).isEmpty(),
                () -> assertThat(whereClause.getParameters()).isEmpty()
        );
    }

    @Test
    void testDisabledIsNull() {
        WhereClauseProvider whereClause = where(id, isNull().when(() -> false))
                .build()
                .render(RenderingStrategies.SPRING_NAMED_PARAMETER);

        assertAll(
                () -> assertThat(whereClause.getWhereClause()).isEmpty(),
                () -> assertThat(whereClause.getParameters()).isEmpty()
        );
    }

    @Test
    void testEnabledIsNull() {
        WhereClauseProvider whereClause = where(id, isNull().when(() -> true))
                .build()
                .render(RenderingStrategies.SPRING_NAMED_PARAMETER);

        assertAll(
                () -> assertThat(whereClause.getWhereClause()).isEqualTo("where id is null"),
                () -> assertThat(whereClause.getParameters()).isEmpty()
        );
    }

    @Test
    void testDisabledIsNotNull() {
        WhereClauseProvider whereClause = where(id, isNotNull().when(() -> false))
                .build()
                .render(RenderingStrategies.SPRING_NAMED_PARAMETER);

        assertAll(
                () -> assertThat(whereClause.getWhereClause()).isEmpty(),
                () -> assertThat(whereClause.getParameters()).isEmpty()
        );
    }

    @Test
    void testEnabledIsNotNull() {
        WhereClauseProvider whereClause = where(id, isNotNull().when(() -> true))
                .build()
                .render(RenderingStrategies.SPRING_NAMED_PARAMETER);

        assertAll(
                () -> assertThat(whereClause.getWhereClause()).isEqualTo("where id is not null"),
                () -> assertThat(whereClause.getParameters()).isEmpty()
        );
    }

    @Test
    void testOneRenderableCriteriaBeforeNull() {
        String nullFirstName = null;

        WhereClauseProvider whereClause = where(id, isEqualToWhenPresent(22))
                .and(firstName, isEqualToWhenPresent(nullFirstName))
                .build()
                .render(RenderingStrategies.SPRING_NAMED_PARAMETER);

        assertAll(
            () -> assertThat(whereClause.getParameters()).containsExactly(entry("p1", 22)),
            () -> assertThat(whereClause.getWhereClause()).isEqualTo("where id = :p1")
        );
    }

    @Test
    void testOneRenderableCriteriaBeforeNull2() {
        String nullFirstName = null;

        WhereClauseProvider whereClause = where(id, isEqualToWhenPresent(22), and(firstName, isEqualToWhenPresent(nullFirstName)))
                .build()
                .render(RenderingStrategies.SPRING_NAMED_PARAMETER);

        assertAll(
            () -> assertThat(whereClause.getParameters()).containsExactly(entry("p1", 22)),
            () -> assertThat(whereClause.getWhereClause()).isEqualTo("where id = :p1")
        );
    }

    @Test
    void testOneRenderableCriteriaAfterNull() {
        Integer nullId = null;

        WhereClauseProvider whereClause = where(id, isEqualToWhenPresent(nullId))
                .and(firstName, isEqualToWhenPresent("fred"))
                .build()
                .render(RenderingStrategies.SPRING_NAMED_PARAMETER);

        assertAll(
            () -> assertThat(whereClause.getParameters()).containsExactly(entry("p1", "fred")),
            () -> assertThat(whereClause.getWhereClause()).isEqualTo("where first_name = :p1")
        );
    }

    @Test
    void testOneRenderableCriteriaAfterNull2() {
        Integer nullId = null;

        WhereClauseProvider whereClause = where(id, isEqualToWhenPresent(nullId), and(firstName, isEqualToWhenPresent("fred")))
                .build()
                .render(RenderingStrategies.SPRING_NAMED_PARAMETER);

        assertAll(
            () -> assertThat(whereClause.getParameters()).containsExactly(entry("p1", "fred")),
            () -> assertThat(whereClause.getWhereClause()).isEqualTo("where first_name = :p1")
        );
    }

    @Test
    void testOverrideFirstConnector() {
        Integer nullId = null;

        WhereClauseProvider whereClause = where(id, isEqualToWhenPresent(nullId), and(firstName, isEqualToWhenPresent("fred")), or(lastName, isEqualTo("flintstone")))
                .build()
                .render(RenderingStrategies.SPRING_NAMED_PARAMETER);

        assertAll(
            () -> assertThat(whereClause.getParameters()).containsExactly(entry("p1", "fred"), entry("p2", "flintstone")),
            () -> assertThat(whereClause.getWhereClause()).isEqualTo("where (first_name = :p1 or last_name = :p2)")
        );
    }

    @Test
    void testWhereExists() {
        WhereClauseProvider whereClause = where(
                exists(
                        select(person.allColumns())
                        .from(person)
                        .where(id, isEqualTo(3))
                ))
                .build()
                .render(RenderingStrategies.SPRING_NAMED_PARAMETER);

        assertThat(whereClause.getParameters()).containsExactly(entry("p1", 3));
        assertThat(whereClause.getWhereClause()).isEqualTo("where exists (select * from person where id = :p1)");
    }

    @Test
    void testWhereExistsOr() {
        WhereClauseProvider whereClause = where(
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
                .build()
                .render(RenderingStrategies.SPRING_NAMED_PARAMETER);

        String expected = "where (exists (select * from person where id = :p1) " +
                "or exists (select * from person where id = :p2))";

        assertThat(whereClause.getParameters()).containsExactly(entry("p1", 3), entry("p2", 4));
        assertThat(whereClause.getWhereClause()).isEqualTo(expected);
    }

    @Test
    void testWhereExistsOrOr() {
        WhereClauseProvider whereClause = where(
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
                .build()
                .render(RenderingStrategies.SPRING_NAMED_PARAMETER);

        String expected = "where (exists (select * from person where id = :p1) " +
                "or (exists (select * from person where id = :p2) " +
                "or exists (select * from person where id = :p3)))";

        assertThat(whereClause.getParameters()).containsExactly(entry("p1", 3), entry("p2", 4), entry("p3", 5));
        assertThat(whereClause.getWhereClause()).isEqualTo(expected);
    }

    @Test
    void testWhereExistsAnd() {
        WhereClauseProvider whereClause = where(
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
                .build()
                .render(RenderingStrategies.SPRING_NAMED_PARAMETER);

        String expected = "where (exists (select * from person where id = :p1) " +
                "and exists (select * from person where id = :p2))";

        assertThat(whereClause.getParameters()).containsExactly(entry("p1", 3), entry("p2", 4));
        assertThat(whereClause.getWhereClause()).isEqualTo(expected);
    }

    @Test
    void testWhereExistsAndAnd() {
        WhereClauseProvider whereClause = where(
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
                .build()
                .render(RenderingStrategies.SPRING_NAMED_PARAMETER);

        String expected = "where (exists (select * from person where id = :p1) " +
                "and (exists (select * from person where id = :p2) " +
                "and exists (select * from person where id = :p3)))";

        assertThat(whereClause.getParameters()).containsExactly(entry("p1", 3), entry("p2", 4), entry("p3", 5));
        assertThat(whereClause.getWhereClause()).isEqualTo(expected);
    }

    @Test
    void testCollapsingCriteriaGroup1() {
        String name1 = null;

        WhereClauseProvider whereClause = where(
                group(firstName, isEqualToWhenPresent(name1)), or(lastName, isEqualToWhenPresent(name1)))
                .build()
                .render(RenderingStrategies.SPRING_NAMED_PARAMETER);

        assertThat(whereClause.getWhereClause()).isEmpty();
    }

    @Test
    void testCollapsingCriteriaGroup2() {
        String name1 = null;

        WhereClauseProvider whereClause = where(
                group(firstName, isEqualTo("Fred")), or(lastName, isEqualToWhenPresent(name1)))
                .build()
                .render(RenderingStrategies.SPRING_NAMED_PARAMETER);

        String expected = "where first_name = :p1";

        assertThat(whereClause.getParameters()).containsExactly(entry("p1", "Fred"));
        assertThat(whereClause.getWhereClause()).isEqualTo(expected);
    }

    @Test
    void testCollapsingCriteriaGroup3() {
        String name1 = null;

        WhereClauseProvider whereClause = where(
                group(firstName, isEqualTo("Fred")), or(lastName, isEqualToWhenPresent(name1)), or(firstName, isEqualTo("Betty")))
                .build()
                .render(RenderingStrategies.SPRING_NAMED_PARAMETER);

        String expected = "where (first_name = :p1 or first_name = :p2)";

        assertThat(whereClause.getParameters()).containsExactly(entry("p1", "Fred"), entry("p2", "Betty"));
        assertThat(whereClause.getWhereClause()).isEqualTo(expected);
    }
}
