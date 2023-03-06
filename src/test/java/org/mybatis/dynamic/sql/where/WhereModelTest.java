/*
 *    Copyright 2016-2023 the original author or authors.
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
package org.mybatis.dynamic.sql.where;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mybatis.dynamic.sql.SqlBuilder.*;

import java.sql.JDBCType;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.exception.NonRenderingWhereClauseException;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.where.render.WhereClauseProvider;

class WhereModelTest {

    @Test
    void testThatParameterNameCarriesToSubCriteria() {
        SqlTable table = SqlTable.of("foo");
        SqlColumn<Integer> id = table.column("id", JDBCType.INTEGER);

        Optional<WhereClauseProvider> whereClause = where(id, isEqualTo(3), or(id, isEqualTo(4))).build()
                .render(RenderingStrategies.MYBATIS3, "myName");

        assertThat(whereClause.map(WhereClauseProvider::getWhereClause)).hasValueSatisfying(wc ->
            assertThat(wc).isEqualTo("where id = #{myName.parameters.p1,jdbcType=INTEGER} or id = #{myName.parameters.p2,jdbcType=INTEGER}")
        );
    }

    @Test
    void testNonRenderingWhereDisallowed() {
        WhereModel model = where().build();

        assertThatExceptionOfType(NonRenderingWhereClauseException.class).isThrownBy(() ->
                model.render(RenderingStrategies.MYBATIS3)
        );
    }

    @Test
    void testNonRenderingWhereAllowed() {
        Optional<WhereClauseProvider> whereClause = where()
                .configureStatement(c -> c.setNonRenderingWhereClauseAllowed(true))
                .build()
                .render(RenderingStrategies.MYBATIS3);

        assertThat(whereClause).isEmpty();
    }
}
