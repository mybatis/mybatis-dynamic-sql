/*
 *    Copyright 2016-2022 the original author or authors.
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
package org.mybatis.dynamic.sql;

import static examples.complexquery.PersonDynamicSqlSupport.firstName;
import static examples.complexquery.PersonDynamicSqlSupport.id;
import static examples.complexquery.PersonDynamicSqlSupport.lastName;
import static examples.complexquery.PersonDynamicSqlSupport.person;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mybatis.dynamic.sql.SqlBuilder.countFrom;
import static org.mybatis.dynamic.sql.SqlBuilder.deleteFrom;
import static org.mybatis.dynamic.sql.SqlBuilder.select;
import static org.mybatis.dynamic.sql.SqlBuilder.update;

import org.junit.jupiter.api.Test;
import org.mybatis.dynamic.sql.delete.render.DeleteStatementProvider;
import org.mybatis.dynamic.sql.exception.UnrenderableWhereClauseException;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.update.render.UpdateStatementProvider;

public class StatementConfigurationTest {
    @Test
    void testCountWhereCalledButNoCriteriaThrowsException() {
        assertThatExceptionOfType(UnrenderableWhereClauseException.class).isThrownBy(() ->
                countFrom(person)
                        .where()
                        .build()
                        .render(RenderingStrategies.MYBATIS3)
        );
    }

    @Test
    void testCountWhereCalledButNoCriteriaRequiresConfiguration() {
        SelectStatementProvider selectStatement = countFrom(person)
                .where()
                .configureStatement(c -> c.setUnrenderableWhereClauseAllowed(true))
                .build()
                .render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement())
                .isEqualTo("select count(*) from Person");
    }

    @Test
    void testCountWhereNotCalledIsOK() {
        SelectStatementProvider selectStatement = countFrom(person)
                .build()
                .render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement())
                .isEqualTo("select count(*) from Person");
    }

    @Test
    void testDeleteWhereCalledButNoCriteriaThrowsException() {
        assertThatExceptionOfType(UnrenderableWhereClauseException.class).isThrownBy(() ->
                deleteFrom(person)
                        .where()
                        .build()
                        .render(RenderingStrategies.MYBATIS3)
        );
    }

    @Test
    void testDeleteWhereCalledButNoCriteriaRequiresConfiguration() {
        DeleteStatementProvider deleteStatement = deleteFrom(person)
                .where()
                .configureStatement(c -> c.setUnrenderableWhereClauseAllowed(true))
                .build()
                .render(RenderingStrategies.MYBATIS3);

        assertThat(deleteStatement.getDeleteStatement())
                .isEqualTo("delete from Person");
    }

    @Test
    void testDeleteWhereNotCalledIsOK() {
        DeleteStatementProvider deleteStatement = deleteFrom(person)
                .build()
                .render(RenderingStrategies.MYBATIS3);

        assertThat(deleteStatement.getDeleteStatement())
                .isEqualTo("delete from Person");
    }

    @Test
    void testSelectWhereCalledButNoCriteriaThrowsException() {
        assertThatExceptionOfType(UnrenderableWhereClauseException.class).isThrownBy(() ->
                select(id, firstName, lastName)
                        .from(person)
                        .where()
                        .build()
                        .render(RenderingStrategies.MYBATIS3)
        );
    }

    @Test
    void testSelectWhereCalledButNoCriteriaRequiresConfiguration() {
        SelectStatementProvider selectStatement = select(id, firstName, lastName)
                .from(person)
                .where()
                .configureStatement(c -> c.setUnrenderableWhereClauseAllowed(true))
                .build()
                .render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement())
                .isEqualTo("select person_id, first_name, last_name from Person");
    }

    @Test
    void testSelectWhereNotCalledIsOK() {
        SelectStatementProvider selectStatement = select(id, firstName, lastName)
                .from(person)
                .build()
                .render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement())
                .isEqualTo("select person_id, first_name, last_name from Person");
    }

    @Test
    void testUpdateWhereCalledButNoCriteriaThrowsException() {
        assertThatExceptionOfType(UnrenderableWhereClauseException.class).isThrownBy(() ->
                update(person)
                        .set(id).equalTo(1)
                        .where()
                        .build()
                        .render(RenderingStrategies.MYBATIS3)
        );
    }

    @Test
    void testUpdateWhereCalledButNoCriteriaRequiresConfiguration() {
        UpdateStatementProvider updateStatement = update(person)
                .set(id).equalTo(1)
                .where()
                .configureStatement(c -> c.setUnrenderableWhereClauseAllowed(true))
                .build()
                .render(RenderingStrategies.MYBATIS3);

        assertThat(updateStatement.getUpdateStatement())
                .isEqualTo("update Person set person_id = #{parameters.p1}");
    }

    @Test
    void testUpdateWhereNotCalledIsOK() {
        UpdateStatementProvider updateStatement = update(person)
                .set(id).equalTo(1)
                .build()
                .render(RenderingStrategies.MYBATIS3);

        assertThat(updateStatement.getUpdateStatement())
                .isEqualTo("update Person set person_id = #{parameters.p1}");
    }
}
