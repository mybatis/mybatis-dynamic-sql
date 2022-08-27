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
package examples.spring;

import static examples.spring.PersonDynamicSqlSupport.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mybatis.dynamic.sql.SqlBuilder.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mybatis.dynamic.sql.delete.DeleteModel;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.select.SelectModel;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.update.UpdateModel;
import org.mybatis.dynamic.sql.util.Buildable;
import org.mybatis.dynamic.sql.util.spring.NamedParameterJdbcTemplateExtensions;
import org.mybatis.dynamic.sql.where.WhereApplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

@SpringJUnitConfig(classes = SpringConfiguration.class)
@Transactional
class ReusableWhereTest {

    @Autowired
    private NamedParameterJdbcTemplateExtensions template;

    @Test
    void testCount() {
        Buildable<SelectModel> countStatement = countFrom(person)
                .applyWhere(commonWhere);

        long rows = template.count(countStatement);
        assertThat(rows).isEqualTo(3);
    }

    @Test
    void testDelete() {
        Buildable<DeleteModel> deleteStatement = deleteFrom(person)
                .applyWhere(commonWhere);

        long rows = template.delete(deleteStatement);

        assertThat(rows).isEqualTo(3);
    }

    @Test
    void testSelect() {
        Buildable<SelectModel> selectStatement = select(person.allColumns())
                .from(person)
                .applyWhere(commonWhere);

        List<PersonRecord> rows = template.selectList(selectStatement, PersonTemplateTest.personRowMapper);

        assertThat(rows).hasSize(3);
    }

    @Test
    void testUpdate() {
        Buildable<UpdateModel> updateStatement = update(person)
                .set(occupation).equalToStringConstant("worker")
                .applyWhere(commonWhere);

        int rows = template.update(updateStatement);

        assertThat(rows).isEqualTo(3);
    }

    @Test
    void testComposition() {
        WhereApplier whereApplier = commonWhere.andThen(wa -> wa.and(birthDate, isNotNull()));
        whereApplier = whereApplier.andThen(wa -> wa.or(addressId, isLessThan(3)));

        SelectStatementProvider selectStatement = select(person.allColumns())
                .from(person)
                .applyWhere(whereApplier)
                .build()
                .render(RenderingStrategies.SPRING_NAMED_PARAMETER);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(
                "select * from Person " +
                    "where id = :p1 or occupation is null and birth_date is not null or address_id < :p2");

    }

    private final WhereApplier commonWhere = d -> d.where(id, isEqualTo(1)).or(occupation, isNull());
}
