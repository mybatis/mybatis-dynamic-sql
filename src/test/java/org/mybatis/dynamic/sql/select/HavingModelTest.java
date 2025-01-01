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
package org.mybatis.dynamic.sql.select;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mybatis.dynamic.sql.SqlBuilder.*;

import java.sql.JDBCType;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.configuration.StatementConfiguration;
import org.mybatis.dynamic.sql.render.RenderingContext;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.select.render.HavingRenderer;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;

class HavingModelTest {
    @Test
    void testSimpleHaving() {
        SqlTable table = SqlTable.of("foo");
        SqlColumn<Integer> id = table.column("id", JDBCType.INTEGER);

        HavingModel model = having(id, isLessThan(4))
                .or(id, isGreaterThan(14))
                .build();

        Optional<FragmentAndParameters> havingClause = renderHavingModel(model);

        assertThat(havingClause).hasValueSatisfying(hc ->
                assertThat(hc.fragment()).isEqualTo("having id < :p1 or id > :p2")
        );
    }

    @Test
    void testComplexHaving() {
        SqlTable table = SqlTable.of("foo");
        SqlColumn<Integer> id = table.column("id", JDBCType.INTEGER);

        HavingModel model = having(group(id, isLessThan(10), and(id, isGreaterThan(4))))
                .or(id, isGreaterThan(14))
                .build();

        Optional<FragmentAndParameters> havingClause = renderHavingModel(model);

        assertThat(havingClause).hasValueSatisfying(hc ->
                assertThat(hc.fragment()).isEqualTo("having (id < :p1 and id > :p2) or id > :p3")
        );
    }

    private Optional<FragmentAndParameters> renderHavingModel(HavingModel havingModel) {
        RenderingContext renderingContext = RenderingContext
                .withRenderingStrategy(RenderingStrategies.SPRING_NAMED_PARAMETER)
                .withStatementConfiguration(new StatementConfiguration())
                .build();

        return HavingRenderer.withHavingModel(havingModel)
                .withRenderingContext(renderingContext)
                .build()
                .render();
    }
}
