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
package examples.column.comparison;

import static examples.column.comparison.ColumnComparisonDynamicSqlSupport.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mybatis.dynamic.sql.SqlBuilder.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig(classes = ColumnComparisonConfiguration.class)
class ColumnComparisonTest {

    @Autowired
    private ColumnComparisonMapper mapper;

    @Test
    void testColumnComparisonLessThan() {
        SelectStatementProvider selectStatement = select(number1, number2)
                .from(columnComparison)
                .where(number1, isLessThan(number2))
                .orderBy(number1, number2)
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = "select number1, number2 "
                + "from ColumnComparison "
                + "where number1 < number2 "
                + "order by number1, number2";

        List<ColumnComparisonRecord> records = mapper.selectMany(selectStatement);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
        assertThat(records).hasSize(5);
        assertThat(records.get(0).getNumber1()).isEqualTo(1);
        assertThat(records.get(4).getNumber1()).isEqualTo(5);
    }

    @Test
    void testColumnComparisonLessThanOrEqual() {
        SelectStatementProvider selectStatement = select(number1, number2)
                .from(columnComparison)
                .where(number1, isLessThanOrEqualTo(number2))
                .orderBy(number1, number2)
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = "select number1, number2 "
                + "from ColumnComparison "
                + "where number1 <= number2 "
                + "order by number1, number2";

        List<ColumnComparisonRecord> records = mapper.selectMany(selectStatement);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
        assertThat(records).hasSize(6);
        assertThat(records.get(0).getNumber1()).isEqualTo(1);
        assertThat(records.get(5).getNumber1()).isEqualTo(6);
    }

    @Test
    void testColumnComparisonGreaterThan() {
        SelectStatementProvider selectStatement = select(number1, number2)
                .from(columnComparison)
                .where(number1, isGreaterThan(number2))
                .orderBy(number1, number2)
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = "select number1, number2 "
                + "from ColumnComparison "
                + "where number1 > number2 "
                + "order by number1, number2";

        List<ColumnComparisonRecord> records = mapper.selectMany(selectStatement);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
        assertThat(records).hasSize(5);
        assertThat(records.get(0).getNumber1()).isEqualTo(7);
        assertThat(records.get(4).getNumber1()).isEqualTo(11);
    }

    @Test
    void testColumnComparisonGreaterThanOrEqual() {
        SelectStatementProvider selectStatement = select(number1, number2)
                .from(columnComparison)
                .where(number1, isGreaterThanOrEqualTo(number2))
                .orderBy(number1, number2)
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = "select number1, number2 "
                + "from ColumnComparison "
                + "where number1 >= number2 "
                + "order by number1, number2";

        List<ColumnComparisonRecord> records = mapper.selectMany(selectStatement);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
        assertThat(records).hasSize(6);
        assertThat(records.get(0).getNumber1()).isEqualTo(6);
        assertThat(records.get(5).getNumber1()).isEqualTo(11);
    }

    @Test
    void testColumnComparisonEqual() {
        SelectStatementProvider selectStatement = select(number1, number2)
                .from(columnComparison)
                .where(number1, isEqualTo(number2))
                .orderBy(number1, number2)
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = "select number1, number2 "
                + "from ColumnComparison "
                + "where number1 = number2 "
                + "order by number1, number2";

        List<ColumnComparisonRecord> records = mapper.selectMany(selectStatement);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
        assertThat(records).hasSize(1);
        assertThat(records.get(0).getNumber1()).isEqualTo(6);
    }

    @Test
    void testColumnComparisonNotEqual() {
        SelectStatementProvider selectStatement = select(number1, number2)
                .from(columnComparison)
                .where(number1, isNotEqualTo(number2))
                .orderBy(number1, number2)
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = "select number1, number2 "
                + "from ColumnComparison "
                + "where number1 <> number2 "
                + "order by number1, number2";

        List<ColumnComparisonRecord> records = mapper.selectMany(selectStatement);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
        assertThat(records).hasSize(10);
        assertThat(records.get(0).getNumber1()).isEqualTo(1);
        assertThat(records.get(9).getNumber1()).isEqualTo(11);
    }

    @Test
    void testHelperMethod() {
        List<ColumnComparisonRecord> records = mapper.select(d ->
                d.where(number1, isNotEqualTo(number2))
                        .orderBy(number1, number2)
        );

        assertThat(records).hasSize(10);
        assertThat(records.get(0).getNumber1()).isEqualTo(1);
        assertThat(records.get(9).getNumber1()).isEqualTo(11);
    }
}
