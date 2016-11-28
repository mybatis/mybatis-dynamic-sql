/**
 *    Copyright 2016 the original author or authors.
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

import static examples.simple.SimpleTableFields.*;
import static org.hamcrest.core.Is.*;
import static org.junit.Assert.assertThat;
import static org.mybatis.qbe.sql.SqlConditions.*;
import static org.mybatis.qbe.sql.where.WhereSupportBuilder.*;

import org.junit.Test;
import org.mybatis.qbe.sql.where.WhereSupport;

public class SampleWhereClausesTest {

    @Test
    public void simpleClause1() {
        WhereSupport whereSupport = whereSupport()
                .where(id, isEqualTo(3))
                .build();
        
        assertThat(whereSupport.getWhereClause(),
                is("where a.id = #{parameters.p1,jdbcType=INTEGER}"));
    }
    
    @Test
    public void simpleClause2() {
        WhereSupport whereSupport = whereSupport()
                .where(id, isNull())
                .build();
        
        assertThat(whereSupport.getWhereClause(),
                is("where a.id is null"));
    }
    
    @Test
    public void betweenClause() {
        WhereSupport whereSupport = whereSupport()
                .where(id, isBetween(1).and(4))
                .build();
        
        assertThat(whereSupport.getWhereClause(),
                is("where a.id between #{parameters.p1,jdbcType=INTEGER} and #{parameters.p2,jdbcType=INTEGER}"));
    }

    @Test
    public void complexClause() {
        WhereSupport whereSupport = whereSupport()
                .where(id, isGreaterThan(2))
                .or(occupation, isNull(), and(id, isLessThan(6)))
                .buildIgnoringAlias();
        
        assertThat(whereSupport.getWhereClause(),
                is("where id > #{parameters.p1,jdbcType=INTEGER} or (occupation is null and id < #{parameters.p2,jdbcType=INTEGER})"));
    }
}
