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
package org.mybatis.dynamic.sql.select;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mybatis.dynamic.sql.SqlBuilder.*;

import java.sql.JDBCType;

import org.junit.jupiter.api.Test;
import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;

class HavingModelTest {
    @Test
    void testSimpleHaving() {
        SqlTable table = SqlTable.of("foo");
        SqlColumn<Integer> id = table.column("id", JDBCType.INTEGER);

        HavingApplier havingApplier = having(id, isLessThan(4))
                .or(id, isGreaterThan(14))
                .toHavingApplier();

        SelectStatementProvider selectStatementProvider = select(table.allColumns())
                .from(table)
                .applyHaving(havingApplier)
                .build()
                .render(RenderingStrategies.SPRING_NAMED_PARAMETER);

        assertThat(selectStatementProvider.getSelectStatement()).isEqualTo("select * from foo having id < :p1 or id > :p2");
    }

    @Test
    void testComplexHaving() {
        SqlTable table = SqlTable.of("foo");
        SqlColumn<Integer> id = table.column("id", JDBCType.INTEGER);

        HavingApplier havingApplier = having(group(id, isLessThan(10), and(id, isGreaterThan(4))))
                .or(id, isGreaterThan(14))
                .toHavingApplier();

        SelectStatementProvider selectStatementProvider = select(table.allColumns())
                .from(table)
                .applyHaving(havingApplier)
                .build()
                .render(RenderingStrategies.SPRING_NAMED_PARAMETER);

        assertThat(selectStatementProvider.getSelectStatement()).isEqualTo("select * from foo having (id < :p1 and id > :p2) or id > :p3");
    }
}
