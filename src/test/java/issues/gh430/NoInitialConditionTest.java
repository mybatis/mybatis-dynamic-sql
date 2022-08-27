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
package issues.gh430;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mybatis.dynamic.sql.SqlBuilder.*;
import static org.mybatis.dynamic.sql.subselect.FooDynamicSqlSupport.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mybatis.dynamic.sql.AndOrCriteriaGroup;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;

class NoInitialConditionTest {

    @Test
    void testNoInitialConditionEmptyList() {
        List<AndOrCriteriaGroup> criteria = new ArrayList<>();

        SelectStatementProvider selectStatement = buildSelectStatement(criteria);

        String expected = "select column1, column2 from foo where column1 < :p1";

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testNoInitialConditionSingleSub() {
        List<AndOrCriteriaGroup> criteria = new ArrayList<>();
        criteria.add(or(column2, isEqualTo(3)));

        SelectStatementProvider selectStatement = buildSelectStatement(criteria);

        String expected = "select column1, column2 from foo where column1 < :p1 " +
                "and column2 = :p2";

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testNoInitialConditionMultipleSubs() {
        List<AndOrCriteriaGroup> criteria = new ArrayList<>();
        criteria.add(or(column2, isEqualTo(3)));
        criteria.add(or(column2, isEqualTo(4)));
        criteria.add(or(column2, isEqualTo(5)));

        SelectStatementProvider selectStatement = buildSelectStatement(criteria);

        String expected = "select column1, column2 from foo where column1 < :p1 " +
                "and (column2 = :p2 or column2 = :p3 or column2 = :p4)";

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testNoInitialConditionWhereMultipleSubs() {
        List<AndOrCriteriaGroup> criteria = new ArrayList<>();
        criteria.add(or(column2, isEqualTo(3)));
        criteria.add(or(column2, isEqualTo(4)));
        criteria.add(or(column2, isEqualTo(5)));

        SelectStatementProvider selectStatement = select(column1, column2)
                .from(foo)
                .where(criteria)
                .build()
                .render(RenderingStrategies.SPRING_NAMED_PARAMETER);

        String expected = "select column1, column2 from foo where " +
                "(column2 = :p1 or column2 = :p2 or column2 = :p3)";

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testNoInitialConditionWhereNotMultipleSubs() {
        List<AndOrCriteriaGroup> criteria = new ArrayList<>();
        criteria.add(or(column2, isEqualTo(3)));
        criteria.add(or(column2, isEqualTo(4)));
        criteria.add(or(column2, isEqualTo(5)));

        SelectStatementProvider selectStatement = select(column1, column2)
                .from(foo)
                .where(not(criteria), and(column1, isLessThan(new Date())))
                .build()
                .render(RenderingStrategies.SPRING_NAMED_PARAMETER);

        String expected = "select column1, column2 from foo where (not " +
                "(column2 = :p1 or column2 = :p2 or column2 = :p3) " +
                "and column1 < :p4)";

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testNoInitialConditionWhereGroupMultipleSubs() {
        List<AndOrCriteriaGroup> criteria = new ArrayList<>();
        criteria.add(or(column2, isEqualTo(3)));
        criteria.add(or(column2, isEqualTo(4)));
        criteria.add(or(column2, isEqualTo(5)));

        SelectStatementProvider selectStatement = select(column1, column2)
                .from(foo)
                .where(group(criteria), and(column1, isLessThan(new Date())))
                .build()
                .render(RenderingStrategies.SPRING_NAMED_PARAMETER);

        String expected = "select column1, column2 from foo where " +
                "((column2 = :p1 or column2 = :p2 or column2 = :p3) " +
                "and column1 < :p4)";

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testNoInitialConditionWhereCCAndMultipleSubs() {
        List<AndOrCriteriaGroup> criteria = new ArrayList<>();
        criteria.add(or(column2, isEqualTo(3)));
        criteria.add(or(column2, isEqualTo(4)));
        criteria.add(or(column2, isEqualTo(5)));

        SelectStatementProvider selectStatement = select(column1, column2)
                .from(foo)
                .where(column1, isLessThan(new Date()), and(criteria))
                .build()
                .render(RenderingStrategies.SPRING_NAMED_PARAMETER);

        String expected = "select column1, column2 from foo where " +
                "(column1 < :p1 and (column2 = :p2 or column2 = :p3 or column2 = :p4))";

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testNoInitialConditionWhereCCOrMultipleSubs() {
        List<AndOrCriteriaGroup> criteria = new ArrayList<>();
        criteria.add(or(column2, isEqualTo(3)));
        criteria.add(or(column2, isEqualTo(4)));
        criteria.add(or(column2, isEqualTo(5)));

        SelectStatementProvider selectStatement = select(column1, column2)
                .from(foo)
                .where(column1, isLessThan(new Date()), or(criteria))
                .build()
                .render(RenderingStrategies.SPRING_NAMED_PARAMETER);

        String expected = "select column1, column2 from foo where " +
                "(column1 < :p1 or (column2 = :p2 or column2 = :p3 or column2 = :p4))";

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testNoInitialConditionWhereOrMultipleSubs() {
        List<AndOrCriteriaGroup> criteria = new ArrayList<>();
        criteria.add(or(column2, isEqualTo(3)));
        criteria.add(or(column2, isEqualTo(4)));
        criteria.add(or(column2, isEqualTo(5)));

        SelectStatementProvider selectStatement = select(column1, column2)
                .from(foo)
                .where(column1, isLessThan(new Date()))
                .or(criteria)
                .build()
                .render(RenderingStrategies.SPRING_NAMED_PARAMETER);

        String expected = "select column1, column2 from foo where column1 < :p1 " +
                "or (column2 = :p2 or column2 = :p3 or column2 = :p4)";

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    private SelectStatementProvider buildSelectStatement(List<AndOrCriteriaGroup> criteria) {
        return select(column1, column2)
                .from(foo)
                .where(column1, isLessThan(new Date()))
                .and(criteria)
                .build()
                .render(RenderingStrategies.SPRING_NAMED_PARAMETER);
    }
}
