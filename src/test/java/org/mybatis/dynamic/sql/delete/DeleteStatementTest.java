/**
 *    Copyright 2016-2019 the original author or authors.
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
package org.mybatis.dynamic.sql.delete;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mybatis.dynamic.sql.SqlBuilder.*;

import java.sql.JDBCType;

import org.junit.jupiter.api.Test;
import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.delete.render.DeleteStatementProvider;
import org.mybatis.dynamic.sql.render.RenderingStrategies;

public class DeleteStatementTest {
    private static final SqlTable foo = SqlTable.of("foo");
    private static final SqlColumn<Integer> id = foo.column("id", JDBCType.INTEGER);
    private static final SqlColumn<String> firstName = foo.column("first_name", JDBCType.VARCHAR);

    @Test
    public void testFullStatement() {
        DeleteStatementProvider deleteStatement = deleteFrom(foo)
                .where(id, isEqualTo(3), and(firstName, isEqualTo("Betty")))
                .or(firstName, isLikeCaseInsensitive("%Fr%"))
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expectedFullStatement = "delete from foo where (id = #{parameters.p1,jdbcType=INTEGER} and first_name = #{parameters.p2,jdbcType=VARCHAR}) or upper(first_name) like #{parameters.p3,jdbcType=VARCHAR}";

        assertAll(
                () -> assertThat(deleteStatement.getDeleteStatement()).isEqualTo(expectedFullStatement),
                () -> assertThat(deleteStatement.getParameters().size()).isEqualTo(3),
                () -> assertThat(deleteStatement.getParameters().get("p1")).isEqualTo(3),
                () -> assertThat(deleteStatement.getParameters().get("p2")).isEqualTo("Betty"),
                () -> assertThat(deleteStatement.getParameters().get("p3")).isEqualTo("%FR%")
        );
    }

    @Test
    public void testFullStatementWithoutWhere() {
        DeleteStatementProvider deleteStatement = deleteFrom(foo)
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expectedFullStatement = "delete from foo";

        assertAll(
                () -> assertThat(deleteStatement.getDeleteStatement()).isEqualTo(expectedFullStatement),
                () -> assertThat(deleteStatement.getParameters().size()).isEqualTo(0)
        );
    }
}
