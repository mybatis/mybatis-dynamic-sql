/**
 *    Copyright 2016-2018 the original author or authors.
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
package org.mybatis.dynamic.sql.where.render;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.assertj.core.api.Assertions.*;
import static org.mybatis.dynamic.sql.SqlBuilder.*;

import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.render.RenderingStrategy;

@RunWith(JUnitPlatform.class)
public class OptionalCriterionRenderTest {
    private static SqlTable person = SqlTable.of("person");
    private static SqlColumn<Integer> id = person.column("id");
    private static SqlColumn<String> firstName = person.column("first_name");
    private static SqlColumn<String> lastName = person.column("last_name");
    
    @Test
    public void testNoRenderableCriteria() {
        Integer nullId = null;
        
        WhereClauseProvider whereClause = where(id, isEqualToWhenPresent(nullId))
                .build()
                .render(RenderingStrategy.SPRING_NAMED_PARAMETER);
 
        assertAll(
                () -> assertThat(whereClause.getWhereClause()).isEqualTo(""),
                () -> assertThat(whereClause.getParameters().isEmpty()).isTrue()
        );
    }

    @Test
    public void testOneRenderableCriteriaBeforeNull() {
        String nullFirstName = null;
        
        WhereClauseProvider whereClause = where(id, isEqualToWhenPresent(22))
                .and(firstName, isEqualToWhenPresent(nullFirstName))
                .build()
                .render(RenderingStrategy.SPRING_NAMED_PARAMETER);
 
        assertAll(
            () -> assertThat(whereClause.getParameters().size()).isEqualTo(1),
            () -> assertThat(whereClause.getParameters().get("p1")).isEqualTo(22),
            () -> assertThat(whereClause.getWhereClause()).isEqualTo("where id = :p1")
        );
    }

    @Test
    public void testOneRenderableCriteriaBeforeNull2() {
        String nullFirstName = null;
        
        WhereClauseProvider whereClause = where(id, isEqualToWhenPresent(22), and(firstName, isEqualToWhenPresent(nullFirstName)))
                .build()
                .render(RenderingStrategy.SPRING_NAMED_PARAMETER);
 
        assertAll(
            () -> assertThat(whereClause.getParameters().size()).isEqualTo(1),
            () -> assertThat(whereClause.getParameters().get("p1")).isEqualTo(22),
            () -> assertThat(whereClause.getWhereClause()).isEqualTo("where id = :p1")
        );
    }

    @Test
    public void testOneRenderableCriteriaAfterNull() {
        Integer nullId = null;
        
        WhereClauseProvider whereClause = where(id, isEqualToWhenPresent(nullId))
                .and(firstName, isEqualToWhenPresent("fred"))
                .build()
                .render(RenderingStrategy.SPRING_NAMED_PARAMETER);
 
        assertAll(
            () -> assertThat(whereClause.getParameters().size()).isEqualTo(1),
            () -> assertThat(whereClause.getParameters().get("p1")).isEqualTo("fred"),
            () -> assertThat(whereClause.getWhereClause()).isEqualTo("where first_name = :p1")
        );
    }

    @Test
    public void testOneRenderableCriteriaAfterNull2() {
        Integer nullId = null;
        
        WhereClauseProvider whereClause = where(id, isEqualToWhenPresent(nullId), and(firstName, isEqualToWhenPresent("fred")))
                .build()
                .render(RenderingStrategy.SPRING_NAMED_PARAMETER);
 
        assertAll(
            () -> assertThat(whereClause.getParameters().size()).isEqualTo(1),
            () -> assertThat(whereClause.getParameters().get("p1")).isEqualTo("fred"),
            () -> assertThat(whereClause.getWhereClause()).isEqualTo("where first_name = :p1")
        );
    }
    
    @Test
    public void testOverrideFirstConnector() {
        Integer nullId = null;
        
        WhereClauseProvider whereClause = where(id, isEqualToWhenPresent(nullId), and(firstName, isEqualToWhenPresent("fred")), or(lastName, isEqualTo("flintstone")))
                .build()
                .render(RenderingStrategy.SPRING_NAMED_PARAMETER);
 
        assertAll(
            () -> assertThat(whereClause.getParameters().size()).isEqualTo(2),
            () -> assertThat(whereClause.getParameters().get("p1")).isEqualTo("fred"),
            () -> assertThat(whereClause.getParameters().get("p2")).isEqualTo("flintstone"),
            () -> assertThat(whereClause.getWhereClause()).isEqualTo("where (first_name = :p1 or last_name = :p2)")
        );
    }
}
