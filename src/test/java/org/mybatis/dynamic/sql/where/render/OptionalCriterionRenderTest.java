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
package org.mybatis.dynamic.sql.where.render;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mybatis.dynamic.sql.SqlBuilder.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.render.RenderingStrategies;

class OptionalCriterionRenderTest {
    private static final SqlTable person = SqlTable.of("person");
    private static final SqlColumn<Integer> id = person.column("id");
    private static final SqlColumn<String> firstName = person.column("first_name");
    private static final SqlColumn<String> lastName = person.column("last_name");

    @Test
    void testNoRenderableCriteria() {
        Optional<WhereClauseProvider> whereClause = where(id, isEqualToWhenPresent((Integer) null))
                .configureStatement(c -> c.setNonRenderingWhereClauseAllowed(true))
                .build()
                .render(RenderingStrategies.SPRING_NAMED_PARAMETER);

        assertThat(whereClause).isEmpty();
    }

    @Test
    void testDisabledIsNull() {
        Optional<WhereClauseProvider> whereClause = where(id, isNull().filter(() -> false))
                .configureStatement(c -> c.setNonRenderingWhereClauseAllowed(true))
                .build()
                .render(RenderingStrategies.SPRING_NAMED_PARAMETER);

        assertThat(whereClause).isEmpty();
    }

    @Test
    void testEnabledIsNull() {
        Optional<WhereClauseProvider> whereClause = where(id, isNull().filter(() -> true))
                .build()
                .render(RenderingStrategies.SPRING_NAMED_PARAMETER);

        assertThat(whereClause).hasValueSatisfying(wc -> {
            assertThat(wc.getWhereClause()).isEqualTo("where id is null");
            assertThat(wc.getParameters()).isEmpty();
        });
    }

    @Test
    void testDisabledIsNotNull() {
        Optional<WhereClauseProvider> whereClause = where(id, isNotNull().filter(() -> false))
                .configureStatement(c -> c.setNonRenderingWhereClauseAllowed(true))
                .build()
                .render(RenderingStrategies.SPRING_NAMED_PARAMETER);

        assertThat(whereClause).isEmpty();
    }

    @Test
    void testEnabledIsNotNull() {
        Optional<WhereClauseProvider> whereClause = where(id, isNotNull().filter(() -> true))
                .build()
                .render(RenderingStrategies.SPRING_NAMED_PARAMETER);

        assertThat(whereClause).hasValueSatisfying(wc -> {
            assertThat(wc.getWhereClause()).isEqualTo("where id is not null");
            assertThat(wc.getParameters()).isEmpty();
        });
    }

    @Test
    void testOneRenderableCriteriaBeforeNull() {
        Optional<WhereClauseProvider> whereClause = where(id, isEqualToWhenPresent(22))
                .and(firstName, isEqualToWhenPresent((String) null))
                .build()
                .render(RenderingStrategies.SPRING_NAMED_PARAMETER);

        assertThat(whereClause).hasValueSatisfying(wc -> {
            assertThat(wc.getParameters()).containsExactly(entry("p1", 22));
            assertThat(wc.getWhereClause()).isEqualTo("where id = :p1");
        });
    }

    @Test
    void testOneRenderableCriteriaBeforeNull2() {
        Optional<WhereClauseProvider> whereClause = where(id, isEqualToWhenPresent(22), and(firstName, isEqualToWhenPresent((String) null)))
                .build()
                .render(RenderingStrategies.SPRING_NAMED_PARAMETER);

        assertThat(whereClause).hasValueSatisfying(wc -> {
            assertThat(wc.getParameters()).containsExactly(entry("p1", 22));
            assertThat(wc.getWhereClause()).isEqualTo("where id = :p1");
        });
    }

    @Test
    void testOneRenderableCriteriaAfterNull() {
        Optional<WhereClauseProvider> whereClause = where(id, isEqualToWhenPresent((Integer) null))
                .and(firstName, isEqualToWhenPresent("fred"))
                .build()
                .render(RenderingStrategies.SPRING_NAMED_PARAMETER);

        assertThat(whereClause).hasValueSatisfying(wc -> {
            assertThat(wc.getParameters()).containsExactly(entry("p1", "fred"));
            assertThat(wc.getWhereClause()).isEqualTo("where first_name = :p1");
        });
    }

    @Test
    void testOneRenderableCriteriaAfterNull2() {
        Optional<WhereClauseProvider> whereClause = where(id, isEqualToWhenPresent((Integer) null), and(firstName, isEqualToWhenPresent("fred")))
                .build()
                .render(RenderingStrategies.SPRING_NAMED_PARAMETER);

        assertThat(whereClause).hasValueSatisfying(wc -> {
            assertThat(wc.getParameters()).containsExactly(entry("p1", "fred"));
            assertThat(wc.getWhereClause()).isEqualTo("where first_name = :p1");
        });
    }

    @Test
    void testOverrideFirstConnector() {
        Optional<WhereClauseProvider> whereClause = where(id, isEqualToWhenPresent((Integer) null), and(firstName, isEqualToWhenPresent("fred")), or(lastName, isEqualTo("flintstone")))
                .build()
                .render(RenderingStrategies.SPRING_NAMED_PARAMETER);

        assertThat(whereClause).hasValueSatisfying(wc -> {
            assertThat(wc.getParameters()).containsExactly(entry("p1", "fred"), entry("p2", "flintstone"));
            assertThat(wc.getWhereClause()).isEqualTo("where first_name = :p1 or last_name = :p2");
        });
    }

    @Test
    void testWhereExists() {
        Optional<WhereClauseProvider> whereClause = where(
                exists(
                        select(person.allColumns())
                        .from(person)
                        .where(id, isEqualTo(3))
                ))
                .build()
                .render(RenderingStrategies.SPRING_NAMED_PARAMETER);

        assertThat(whereClause).hasValueSatisfying(wc -> {
            assertThat(wc.getParameters()).containsExactly(entry("p1", 3));
            assertThat(wc.getWhereClause()).isEqualTo("where exists (select * from person where id = :p1)");
        });
    }

    @Test
    void testWhereExistsOr() {
        Optional<WhereClauseProvider> whereClause = where(
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

        String expected = "where exists (select * from person where id = :p1) " +
                "or exists (select * from person where id = :p2)";

        assertThat(whereClause).hasValueSatisfying(wc -> {
            assertThat(wc.getParameters()).containsExactly(entry("p1", 3), entry("p2", 4));
            assertThat(wc.getWhereClause()).isEqualTo(expected);
        });
    }

    @Test
    void testWhereExistsOrOr() {
        Optional<WhereClauseProvider> whereClause = where(
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

        String expected = "where exists (select * from person where id = :p1) " +
                "or (exists (select * from person where id = :p2) " +
                "or exists (select * from person where id = :p3))";

        assertThat(whereClause).hasValueSatisfying(wc -> {
            assertThat(wc.getParameters()).containsExactly(entry("p1", 3), entry("p2", 4), entry("p3", 5));
            assertThat(wc.getWhereClause()).isEqualTo(expected);
        });
    }

    @Test
    void testWhereExistsAnd() {
        Optional<WhereClauseProvider> whereClause = where(
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

        String expected = "where exists (select * from person where id = :p1) " +
                "and exists (select * from person where id = :p2)";

        assertThat(whereClause).hasValueSatisfying(wc -> {
            assertThat(wc.getParameters()).containsExactly(entry("p1", 3), entry("p2", 4));
            assertThat(wc.getWhereClause()).isEqualTo(expected);
        });
    }

    @Test
    void testWhereExistsAndAnd() {
        Optional<WhereClauseProvider> whereClause = where(
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

        String expected = "where exists (select * from person where id = :p1) " +
                "and (exists (select * from person where id = :p2) " +
                "and exists (select * from person where id = :p3))";

        assertThat(whereClause).hasValueSatisfying(wc -> {
            assertThat(wc.getParameters()).containsExactly(entry("p1", 3), entry("p2", 4), entry("p3", 5));
            assertThat(wc.getWhereClause()).isEqualTo(expected);
        });
    }

    @Test
    void testCollapsingCriteriaGroup1() {
        Optional<WhereClauseProvider> whereClause = where(
                group(firstName, isEqualToWhenPresent((String) null)), or(lastName, isEqualToWhenPresent((String) null)))
                .configureStatement(c -> c.setNonRenderingWhereClauseAllowed(true))
                .build()
                .render(RenderingStrategies.SPRING_NAMED_PARAMETER);

        assertThat(whereClause).isEmpty();
    }

    @Test
    void testCollapsingCriteriaGroup2() {
        Optional<WhereClauseProvider> whereClause = where(
                group(firstName, isEqualTo("Fred")), or(lastName, isEqualToWhenPresent((String) null)))
                .build()
                .render(RenderingStrategies.SPRING_NAMED_PARAMETER);

        String expected = "where first_name = :p1";

        assertThat(whereClause).hasValueSatisfying(wc -> {
            assertThat(wc.getParameters()).containsExactly(entry("p1", "Fred"));
            assertThat(wc.getWhereClause()).isEqualTo(expected);
        });
    }

    @Test
    void testCollapsingCriteriaGroup3() {
        Optional<WhereClauseProvider> whereClause = where(
                group(firstName, isEqualTo("Fred")), or(lastName, isEqualToWhenPresent((String) null)), or(firstName, isEqualTo("Betty")))
                .build()
                .render(RenderingStrategies.SPRING_NAMED_PARAMETER);

        String expected = "where first_name = :p1 or first_name = :p2";

        assertThat(whereClause).hasValueSatisfying(wc -> {
            assertThat(wc.getParameters()).containsExactly(entry("p1", "Fred"), entry("p2", "Betty"));
            assertThat(wc.getWhereClause()).isEqualTo(expected);
        });
    }
}
