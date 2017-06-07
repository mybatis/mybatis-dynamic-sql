/**
 *    Copyright 2016-2017 the original author or authors.
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
package examples.simple;

import static examples.simple.SimpleTableDynamicSqlSupport.*;
import static org.assertj.core.api.Assertions.*;
import static org.mybatis.dynamic.sql.SqlBuilder.*;
import static org.mybatis.dynamic.sql.SqlConditions.*;

import org.junit.jupiter.api.Test;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.select.render.SelectSupport;

public class SampleWhereClausesTest {

    @Test
    public void simpleClause1() {
        SelectSupport selectSupport = select(count())
                .from(simpleTable)
                .where(id, isEqualTo(3))
                .buildAndRender(RenderingStrategy.MYBATIS3);
        
        assertThat(selectSupport.getWhereClause())
                .isEqualTo("where a.id = #{parameters.p1,jdbcType=INTEGER}");
    }
    
    @Test
    public void simpleClause2() {
        SelectSupport selectSupport = select(count())
                .from(simpleTable)
                .where(id, isNull())
                .buildAndRender(RenderingStrategy.MYBATIS3);
        
        assertThat(selectSupport.getWhereClause()).isEqualTo("where a.id is null");
    }
    
    @Test
    public void betweenClause() {
        SelectSupport selectSupport = select(count())
                .from(simpleTable)
                .where(id, isBetween(1).and(4))
                .buildAndRender(RenderingStrategy.MYBATIS3);
        
        assertThat(selectSupport.getWhereClause())
            .isEqualTo("where a.id between #{parameters.p1,jdbcType=INTEGER} and #{parameters.p2,jdbcType=INTEGER}");
    }

    @Test
    public void complexClause() {
        SelectSupport selectSupport = select(count())
                .from(simpleTable)
                .where(id, isGreaterThan(2))
                .or(occupation, isNull(), and(id, isLessThan(6)))
                .buildAndRender(RenderingStrategy.MYBATIS3);
        
        assertThat(selectSupport.getWhereClause())
            .isEqualTo("where a.id > #{parameters.p1,jdbcType=INTEGER} or (a.occupation is null and a.id < #{parameters.p2,jdbcType=INTEGER})");
    }
}
