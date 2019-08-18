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
package org.mybatis.dynamic.sql.where;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mybatis.dynamic.sql.SqlBuilder.*;

import java.sql.JDBCType;

import org.junit.jupiter.api.Test;
import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.where.render.WhereClauseProvider;

public class WhereModelTest {

    @Test
    public void testThatParameterNameCarriesToSubCriteria() {
        SqlTable table = SqlTable.of("foo");
        SqlColumn<Integer> id = table.column("id", JDBCType.INTEGER);
        
        WhereClauseProvider wc = where(id, isEqualTo(3), or(id, isEqualTo(4)))
                .build()
                .render(RenderingStrategies.MYBATIS3, "myName");

        assertThat(wc.getWhereClause()).isEqualTo("where (id = #{myName.parameters.p1,jdbcType=INTEGER} or id = #{myName.parameters.p2,jdbcType=INTEGER})");
    }
}
